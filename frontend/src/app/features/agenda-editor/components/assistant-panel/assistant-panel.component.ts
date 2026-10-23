import { Component, ElementRef, inject, signal, ViewChild } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { AssistantApiService } from '../../../../core/api/assistant-api.service';

import { AgendaRequest } from '../../../../core/models/agenda.models';

import { InterpretationResult } from '../../../../core/models/assistant.models';

import { AgendaEditorStateService } from '../../services/agenda-editor-state.service';

import { PictogramSuggestionService } from '../../../../core/services/pictogram-suggestion.service';

import { getApiErrorCode } from '../../../../core/models/api-error.models';

interface AssistantMessage {
  author: 'user' | 'assistant';

  text: string;
}

@Component({
  selector: 'app-assistant-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './assistant-panel.component.html',
  styleUrl: './assistant-panel.component.scss',
})
export class AssistantPanelComponent {
  private readonly assistantApiService = inject(AssistantApiService);

  private readonly editorState = inject(AgendaEditorStateService);

  @ViewChild('messagesContainer')
  private messagesContainer?: ElementRef<HTMLDivElement>;

  readonly messages = signal<AssistantMessage[]>([
    {
      author: 'assistant',
      text: 'Contame qué agenda querés crear o adaptar.',
    },
  ]);

  readonly loading = signal(false);

  readonly lastResult = signal<InterpretationResult | null>(null);

  readonly pendingContext = signal<AgendaRequest | null>(null);

  private readonly pictogramSuggestionService = inject(PictogramSuggestionService);

  message = '';

  private scrollToBottom(): void {
    requestAnimationFrame(() => {
      const container = this.messagesContainer?.nativeElement;

      if (!container) {
        return;
      }

      container.scrollTo({
        top: container.scrollHeight,
        behavior: 'smooth',
      });
    });
  }

  private resolvePreviousTurn(): {
    previousUserMessage: string | null;
    previousAssistantMessage: string | null;
  } {
    const currentMessages = this.messages();

    let previousUserIndex = -1;

    for (let index = currentMessages.length - 1; index >= 0; index--) {
      if (currentMessages[index].author === 'user') {
        previousUserIndex = index;

        break;
      }
    }

    if (previousUserIndex === -1) {
      return {
        previousUserMessage: null,
        previousAssistantMessage: null,
      };
    }

    const previousUserMessage = currentMessages[previousUserIndex].text;

    let previousAssistantMessage: string | null = null;

    for (let index = currentMessages.length - 1; index > previousUserIndex; index--) {
      if (currentMessages[index].author === 'assistant') {
        previousAssistantMessage = currentMessages[index].text;

        break;
      }
    }

    return {
      previousUserMessage,
      previousAssistantMessage,
    };
  }

  private resolveContext(): AgendaRequest | null {
    const pending = this.pendingContext();

    /*
     * Si estamos respondiendo una aclaración
     * de un INCOMPLETE, continuamos usando
     * ese borrador parcial.
     */
    if (pending) {
      return pending;
    }

    const current = this.editorState.agenda();

    if (!current) {
      return null;
    }

    /*
     * El editor crea inicialmente un borrador vacío.
     * No lo enviamos como contexto porque todavía
     * no representa una agenda real.
     */
    const hasActivities = current.dias.some((dia) => dia.actividades.length > 0);

    const hasDate = !!current.fechaInicio;

    if (!hasActivities && !hasDate) {
      return null;
    }

    /*
     * Para una adaptación normal usamos siempre
     * el estado ACTUAL del editor.
     */
    return this.editorState.toRequest();
  }

  onComposerKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();

      this.sendMessage();
    }
  }

  sendMessage(): void {
    const text = this.message.trim();

    if (!text || this.loading()) {
      return;
    }

    const previousTurn = this.resolvePreviousTurn();

    this.messages.update((messages) => [
      ...messages,
      {
        author: 'user',
        text,
      },
    ]);

    this.scrollToBottom();

    this.message = '';

    this.loading.set(true);

    this.scrollToBottom();

    this.assistantApiService
      .interpret({
        message: text,

        previousUserMessage: previousTurn.previousUserMessage,

        previousAssistantMessage: previousTurn.previousAssistantMessage,

        context: this.resolveContext(),
      })
      .subscribe({
        next: (result) => {
          this.lastResult.set(result);

          this.messages.update((messages) => [
            ...messages,
            {
              author: 'assistant',
              text: result.assistantMessage,
            },
          ]);

          this.scrollToBottom();

          if (result.status === 'INCOMPLETE') {
            this.pendingContext.set(result.agenda);

            this.loading.set(false);

            return;
          }

          if (result.status === 'COMPLETE' && result.agenda) {
            this.pendingContext.set(null);

            this.editorState.applyGeneratedAgenda(result.agenda);

            this.pictogramSuggestionService.suggestMissingPictograms();
          }

          this.loading.set(false);
        },

        error: (error) => {
          /*
           * Ante una falla del asistente:
           *
           * - NO modificamos AgendaEditorStateService;
           * - NO reemplazamos la agenda actual;
           * - NO borramos pendingContext;
           * - el usuario puede volver a intentar.
           */

          this.loading.set(false);

          const errorCode = getApiErrorCode(error);

          let errorText: string;

          switch (errorCode) {
            case 'AI_UNAVAILABLE':
              errorText =
                'El asistente no está disponible en este momento. ' +
                'La agenda actual no fue modificada y podés volver a intentarlo.';

              break;

            case 'AI_INVALID_RESPONSE':
              errorText =
                'No fue posible interpretar la solicitud de forma segura. ' +
                'La agenda actual no fue modificada.';

              break;

            default:
              if (error?.status === 0) {
                errorText =
                  'No fue posible conectar con el servidor. ' +
                  'La agenda actual no fue modificada y podés volver a intentarlo.';
              } else {
                errorText =
                  'No fue posible interpretar la solicitud. ' +
                  'La agenda actual no fue modificada y podés volver a intentarlo.';
              }
          }

          this.messages.update((messages) => [
            ...messages,
            {
              author: 'assistant',
              text: errorText,
            },
          ]);

          this.scrollToBottom();
        },
      });
  }
}
