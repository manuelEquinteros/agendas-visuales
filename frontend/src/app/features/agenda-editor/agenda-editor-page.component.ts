import { Component, inject, OnInit, signal } from '@angular/core';

import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AgendaApiService } from '../../core/api/agenda-api.service';

import { getApiErrorCode } from '../../core/models/api-error.models';

import { AgendaEditorStateService } from './services/agenda-editor-state.service';

import { AssistantPanelComponent } from './components/assistant-panel/assistant-panel.component';

import { AgendaCanvasComponent } from './components/agenda-canvas/agenda-canvas.component';

import { FinalAgendaViewComponent } from './components/final-agenda-view/final-agenda-view.component';

import {
  AgendaSettingsDialogComponent,
  AgendaSettingsValue,
} from './components/agenda-settings-dialog/agenda-settings-dialog.component';

@Component({
  selector: 'app-agenda-editor-page',
  standalone: true,
  imports: [
    RouterLink,
    AssistantPanelComponent,
    AgendaCanvasComponent,
    FinalAgendaViewComponent,
    AgendaSettingsDialogComponent,
  ],
  templateUrl: './agenda-editor-page.component.html',
  styleUrl: './agenda-editor-page.component.scss',
})
export class AgendaEditorPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly agendaApiService = inject(AgendaApiService);

  private readonly editorState = inject(AgendaEditorStateService);

  readonly agenda = this.editorState.agenda;

  readonly dirty = this.editorState.dirty;

  readonly loading = signal(false);

  readonly saving = signal(false);

  readonly errorMessage = signal<string | null>(null);

  readonly saveMessage = signal<string | null>(null);

  readonly settingsOpen = signal(false);

  readonly viewMode = signal<'edit' | 'final'>('edit');

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      const current = this.editorState.agenda();

      if (!current || current.id !== null) {
        this.editorState.createEmptyDraft();
      }

      this.settingsOpen.set(true);

      return;
    }

    const id = Number(idParam);

    if (Number.isNaN(id)) {
      this.errorMessage.set('El identificador de la agenda no es válido.');

      return;
    }

    const current = this.editorState.agenda();

    if (current?.id === id) {
      return;
    }

    this.loadAgenda(id);
  }

  showEditMode(): void {
    this.viewMode.set('edit');
  }

  showFinalMode(): void {
    if (!this.agenda() || this.loading()) {
      return;
    }

    this.settingsOpen.set(false);
    this.viewMode.set('final');
  }

  printAgenda(): void {
    if (this.viewMode() !== 'final' || !this.agenda()) {
      return;
    }

    window.print();
  }

  openSettings(): void {
    this.saveMessage.set(null);

    this.errorMessage.set(null);

    this.settingsOpen.set(true);
  }

  onSettingsSaved(changes: AgendaSettingsValue): void {
    this.editorState.updateAgendaSettings(changes);

    this.settingsOpen.set(false);

    this.saveMessage.set(null);

    this.errorMessage.set(null);
  }

  closeSettings(): void {
    this.settingsOpen.set(false);
  }

  openPictogramSelector(activityClientId: string): void {
    const currentAgenda = this.editorState.agenda();

    if (!currentAgenda) {
      return;
    }

    if (currentAgenda.id === null) {
      this.router.navigate(['/agendas', 'new', 'activities', activityClientId, 'pictogram']);

      return;
    }

    this.router.navigate([
      '/agendas',
      currentAgenda.id,
      'activities',
      activityClientId,
      'pictogram',
    ]);
  }

  onSaveAgenda(): void {
    if (this.saving()) {
      return;
    }

    const validationMessage = this.editorState.validateForSave();

    if (validationMessage) {
      this.errorMessage.set(validationMessage);

      this.saveMessage.set(null);

      return;
    }

    const request = this.editorState.toRequest();

    const currentAgenda = this.editorState.agenda();

    if (!request || !currentAgenda) {
      this.errorMessage.set('No fue posible preparar la agenda para guardar.');

      this.saveMessage.set(null);

      return;
    }

    this.saving.set(true);

    this.errorMessage.set(null);

    this.saveMessage.set(null);

    const isNew = currentAgenda.id === null;

    const operation = isNew
      ? this.agendaApiService.create(request)
      : this.agendaApiService.update(currentAgenda.id!, request);

    operation.subscribe({
      next: (response) => {
        /*
         * Sólo reemplazamos el borrador
         * cuando el backend confirmó
         * correctamente la persistencia.
         */
        this.editorState.loadFromResponse(response);

        this.saving.set(false);

        this.saveMessage.set('Agenda guardada correctamente.');

        if (isNew) {
          this.router.navigate(['/agendas', response.id, 'edit'], {
            replaceUrl: true,
          });
        }
      },

      error: (error) => {
        /*
         * Ante un error de persistencia
         * conservamos intacto el borrador.
         */
        this.saving.set(false);

        this.saveMessage.set(null);

        const errorCode = getApiErrorCode(error);

        if (errorCode === 'PERSISTENCE_ERROR') {
          this.errorMessage.set(
            'No fue posible guardar los datos en este momento. ' +
              'Tus cambios permanecen en el borrador y podés volver a intentarlo.',
          );

          return;
        }

        if (error?.status === 0) {
          this.errorMessage.set(
            'No fue posible conectar con el servidor. ' +
              'Tus cambios permanecen en el borrador y podés volver a intentarlo.',
          );

          return;
        }

        this.errorMessage.set(
          'No fue posible guardar la agenda. ' +
            'Tus cambios permanecen en el borrador y podés volver a intentarlo.',
        );
      },
    });
  }

  private loadAgenda(id: number): void {
    this.loading.set(true);

    this.errorMessage.set(null);

    this.agendaApiService.get(id).subscribe({
      next: (agenda) => {
        this.editorState.loadFromResponse(agenda);

        this.loading.set(false);
      },

      error: (error) => {
        this.loading.set(false);

        const errorCode = getApiErrorCode(error);

        if (errorCode === 'AGENDA_NOT_FOUND') {
          this.errorMessage.set('La agenda solicitada ya no existe o no pudo encontrarse.');

          return;
        }

        if (errorCode === 'PERSISTENCE_ERROR') {
          this.errorMessage.set(
            'No fue posible recuperar los datos de la agenda. Intentá nuevamente.',
          );

          return;
        }

        if (error?.status === 0) {
          this.errorMessage.set('No fue posible conectar con el servidor.');

          return;
        }

        this.errorMessage.set('No fue posible cargar la agenda.');
      },
    });
  }
}
