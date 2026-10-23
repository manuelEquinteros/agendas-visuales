import { Component, EventEmitter, Input, Output } from '@angular/core';

import { RouterLink } from '@angular/router';

import { AgendaResponse } from '../../../../core/models/agenda.models';

@Component({
  selector: 'app-agenda-summary-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './agenda-summary-card.component.html',
  styleUrl: './agenda-summary-card.component.scss',
})
export class AgendaSummaryCardComponent {
  @Input({ required: true })
  agenda!: AgendaResponse;

  @Input()
  deleting = false;

  @Output()
  deleteRequested = new EventEmitter<void>();

  formatDate(fecha: string): string {
    const [year, month, day] = fecha.substring(0, 10).split('-');

    return `${day}/${month}/${year}`;
  }
}
