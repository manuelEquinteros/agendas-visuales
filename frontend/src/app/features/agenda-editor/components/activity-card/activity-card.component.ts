import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  ActividadAgendaDraft,
  DiaAgendaDraft
} from '../../models/agenda-draft.models';


@Component({
  selector: 'app-activity-card',
  standalone: true,
  imports: [],
  templateUrl: './activity-card.component.html',
  styleUrl: './activity-card.component.scss'
})
export class ActivityCardComponent {

  @Input({ required: true })
  actividad!: ActividadAgendaDraft;


  @Input()
  canMoveUp = true;


  @Input()
  canMoveDown = true;


  @Input()
  availableDays: DiaAgendaDraft[] = [];


  @Input()
  currentDayClientId = '';


  @Input()
  showDaySelector = false;


  @Output()
  moveUp =
    new EventEmitter<string>();


  @Output()
  moveDown =
    new EventEmitter<string>();


  @Output()
  changeDay =
    new EventEmitter<{
      activityClientId: string;
      targetDayClientId: string;
    }>();


  @Output()
  remove =
    new EventEmitter<string>();


  @Output()
  edit =
    new EventEmitter<ActividadAgendaDraft>();


  @Output()
  selectPictogram =
    new EventEmitter<string>();


  formatTime(
    hora: string | null
  ): string | null {

    if (!hora) {
      return null;
    }

    return hora.substring(0, 5);
  }


  formatDate(
    fecha: string
  ): string {

    if (!fecha) {
      return 'Sin fecha';
    }

    const [
      year,
      month,
      day
    ] =
      fecha
        .substring(0, 10)
        .split('-');

    return `${day}/${month}/${year}`;
  }


  onMoveUp(): void {

    this.moveUp.emit(
      this.actividad.clientId
    );
  }


  onMoveDown(): void {

    this.moveDown.emit(
      this.actividad.clientId
    );
  }


  onDayChange(
    event: Event
  ): void {

    const select =
      event.target as HTMLSelectElement;

    const targetDayClientId =
      select.value;


    if (
      !targetDayClientId ||
      targetDayClientId ===
      this.currentDayClientId
    ) {
      return;
    }


    this.changeDay.emit({
      activityClientId:
        this.actividad.clientId,

      targetDayClientId
    });
  }


  onRemove(): void {

    this.remove.emit(
      this.actividad.clientId
    );
  }


  onEdit(): void {

    this.edit.emit(
      this.actividad
    );
  }


  onSelectPictogram(): void {

    this.selectPictogram.emit(
      this.actividad.clientId
    );
  }
}