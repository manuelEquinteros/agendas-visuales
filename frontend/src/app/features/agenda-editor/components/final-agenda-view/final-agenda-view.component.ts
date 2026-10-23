import { Component, Input } from '@angular/core';

import {
  AgendaDraft,
  DiaAgendaDraft,
  ActividadAgendaDraft,
} from '../../models/agenda-draft.models';

@Component({
  selector: 'app-final-agenda-view',
  standalone: true,
  templateUrl: './final-agenda-view.component.html',
  styleUrl: './final-agenda-view.component.scss',
})
export class FinalAgendaViewComponent {
  @Input()
  agenda: AgendaDraft | null = null;

  formatDayName(fecha: string): string {
    const date = this.parseDate(fecha);

    if (!date) {
      return '';
    }

    const value = new Intl.DateTimeFormat('es-AR', {
      weekday: 'long',
      timeZone: 'UTC',
    }).format(date);

    return this.capitalize(value);
  }

  formatShortDate(fecha: string): string {
    const date = this.parseDate(fecha);

    if (!date) {
      return '';
    }

    return new Intl.DateTimeFormat('es-AR', {
      day: 'numeric',
      month: 'long',
      timeZone: 'UTC',
    }).format(date);
  }

  formatLongDate(fecha: string): string {
    const date = this.parseDate(fecha);

    if (!date) {
      return '';
    }

    const value = new Intl.DateTimeFormat('es-AR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      timeZone: 'UTC',
    }).format(date);

    return this.capitalize(value);
  }

  formatTime(value: string | null): string {
    if (!value) {
      return '';
    }

    return value.substring(0, 5).replace(':', '.');
  }

  formatSchedule(actividad: ActividadAgendaDraft): string {
    const start = this.formatTime(actividad.horaInicio);

    const end = this.formatTime(actividad.horaFin);

    if (start && end) {
      return `${start} a ${end} hs.`;
    }

    if (start) {
      return `${start} hs.`;
    }

    return '';
  }

  dayColorClass(dia: DiaAgendaDraft): string {
    const date = this.parseDate(dia.fecha);

    if (!date) {
      return 'day-neutral';
    }

    switch (date.getUTCDay()) {
      case 1:
        return 'day-monday';

      case 2:
        return 'day-tuesday';

      case 3:
        return 'day-wednesday';

      case 4:
        return 'day-thursday';

      case 5:
        return 'day-friday';

      case 6:
        return 'day-saturday';

      case 0:
        return 'day-sunday';

      default:
        return 'day-neutral';
    }
  }

  private parseDate(value: string): Date | null {
    if (!value) {
      return null;
    }

    const parts = value.split('-').map(Number);

    if (parts.length !== 3 || parts.some((part) => Number.isNaN(part))) {
      return null;
    }

    const [year, month, day] = parts;

    return new Date(Date.UTC(year, month - 1, day));
  }

  private capitalize(value: string): string {
    if (!value) {
      return value;
    }

    return value.charAt(0).toUpperCase() + value.slice(1);
  }
}
