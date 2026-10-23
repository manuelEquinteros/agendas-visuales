import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  AgendaDraft
} from '../../models/agenda-draft.models';

import {
  DayColumnComponent
} from '../day-column/day-column.component';


@Component({
  selector: 'app-agenda-canvas',
  standalone: true,
  imports: [
    DayColumnComponent
  ],
  templateUrl: './agenda-canvas.component.html',
  styleUrl: './agenda-canvas.component.scss'
})
export class AgendaCanvasComponent {

  @Input()
  agenda: AgendaDraft | null = null;


  @Output()
  selectPictogram =
    new EventEmitter<string>();


  onSelectPictogram(
    activityClientId: string
  ): void {

    this.selectPictogram.emit(
      activityClientId
    );
  }
}