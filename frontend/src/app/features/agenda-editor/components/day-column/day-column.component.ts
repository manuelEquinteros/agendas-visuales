import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';

import { ActividadAgendaDraft, DiaAgendaDraft } from '../../models/agenda-draft.models';

import { AgendaEditorStateService } from '../../services/agenda-editor-state.service';

import { ActivityCardComponent } from '../activity-card/activity-card.component';

import {
  ActivityEditDialogComponent,
  ActivityEditValue,
} from '../activity-edit-dialog/activity-edit-dialog.component';

import { PictogramSuggestionService } from '../../../../core/services/pictogram-suggestion.service';

@Component({
  selector: 'app-day-column',
  standalone: true,
  imports: [ActivityCardComponent, ActivityEditDialogComponent],
  templateUrl: './day-column.component.html',
  styleUrl: './day-column.component.scss',
})
export class DayColumnComponent {
  private readonly editorState = inject(AgendaEditorStateService);

  private readonly pictogramSuggestionService = inject(PictogramSuggestionService);

  readonly agenda = this.editorState.agenda;

  readonly editingActivity = signal<ActividadAgendaDraft | null>(null);

  @Input({ required: true })
  dia!: DiaAgendaDraft;

  @Output()
  selectPictogram = new EventEmitter<string>();

  formatDayName(fecha: string): string {
    if (!fecha) {
      return 'Día';
    }

    const date = this.parseDate(fecha);

    const value = new Intl.DateTimeFormat('es-AR', {
      weekday: 'long',
      timeZone: 'UTC',
    }).format(date);

    return value.charAt(0).toUpperCase() + value.slice(1);
  }

  formatDateDescription(fecha: string): string {
    if (!fecha) {
      return 'Sin fecha';
    }

    const date = this.parseDate(fecha);

    return new Intl.DateTimeFormat('es-AR', {
      day: 'numeric',
      month: 'long',
      timeZone: 'UTC',
    }).format(date);
  }

  private parseDate(fecha: string): Date {
    const [year, month, day] = fecha.substring(0, 10).split('-').map(Number);

    return new Date(Date.UTC(year, month - 1, day));
  }

  onAddActivity(): void {
    this.editorState.addActivity(this.dia.clientId);
  }

  onRemoveActivity(activityClientId: string): void {
    this.editorState.deleteActivity(this.dia.clientId, activityClientId);
  }

  onMoveUp(activityClientId: string): void {
    this.editorState.moveActivity(this.dia.clientId, activityClientId, 'up');
  }

  onMoveDown(activityClientId: string): void {
    this.editorState.moveActivity(this.dia.clientId, activityClientId, 'down');
  }

  onChangeDay(event: { activityClientId: string; targetDayClientId: string }): void {
    this.editorState.moveActivityToDay(
      this.dia.clientId,
      event.activityClientId,
      event.targetDayClientId,
    );
  }

  onEditActivity(actividad: ActividadAgendaDraft): void {
    this.editingActivity.set(actividad);
  }

  onSaveActivity(changes: ActivityEditValue): void {
    const actividad = this.editingActivity();

    if (!actividad) {
      return;
    }

    this.editorState.updateActivity(this.dia.clientId, actividad.clientId, changes);

    const updatedAgenda = this.editorState.agenda();

    const updatedActivity = updatedAgenda?.dias
      .flatMap((dia) => dia.actividades)
      .find((item) => item.clientId === actividad.clientId);

    if (updatedActivity && !updatedActivity.pictograma) {
      const term = (updatedActivity.conceptoVisual || updatedActivity.titulo).trim();

      if (term) {
        this.pictogramSuggestionService.suggestForActivity(updatedActivity.clientId, term);
      }
    }

    this.editingActivity.set(null);
  }

  onCancelEdit(): void {
    this.editingActivity.set(null);
  }

  onSelectPictogram(activityClientId: string): void {
    this.selectPictogram.emit(activityClientId);
  }
}
