import { Component, inject, OnInit, signal } from '@angular/core';

import { AgendaApiService } from '../../core/api/agenda-api.service';
import { AgendaResponse } from '../../core/models/agenda.models';

import { AgendaSummaryCardComponent } from './components/agenda-summary-card/agenda-summary-card.component';

import { LoadingStateComponent } from './components/loading-state/loading-state.component';

import { EmptyStateComponent } from './components/empty-state/empty-state.component';

import { ErrorStateComponent } from './components/error-state/error-state.component';

import { RouterLink } from '@angular/router';

import { getApiErrorCode } from '../../core/models/api-error.models';

@Component({
  selector: 'app-agendas-list-page',
  standalone: true,
  imports: [
    RouterLink,
    AgendaSummaryCardComponent,
    LoadingStateComponent,
    EmptyStateComponent,
    ErrorStateComponent,
  ],
  templateUrl: './agendas-list-page.component.html',
  styleUrl: './agendas-list-page.component.scss',
})
export class AgendasListPageComponent implements OnInit {
  private readonly agendaApiService = inject(AgendaApiService);

  readonly agendas = signal<AgendaResponse[]>([]);

  readonly loading = signal(false);

  readonly errorMessage = signal<string | null>(null);

  readonly deletingId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadAgendas();
  }

  loadAgendas(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.agendaApiService.list().subscribe({
      next: (agendas) => {
        this.agendas.set(agendas);
        this.loading.set(false);
      },

      error: (error) => {
        this.agendas.set([]);

        const errorCode = getApiErrorCode(error);

        if (errorCode === 'PERSISTENCE_ERROR') {
          this.errorMessage.set(
            'No fue posible recuperar las agendas en este momento. Intentá nuevamente.',
          );
        } else if (error?.status === 0) {
          this.errorMessage.set('No fue posible conectar con el servidor.');
        } else {
          this.errorMessage.set('No fue posible obtener las agendas.');
        }

        this.loading.set(false);
      },
    });
  }

  deleteAgenda(agenda: AgendaResponse): void {
    if (this.deletingId() !== null) {
      return;
    }

    const confirmed = window.confirm(
      `¿Querés eliminar la agenda "${agenda.titulo}"?\n\nEsta acción no se puede deshacer.`,
    );

    if (!confirmed) {
      return;
    }

    this.deletingId.set(agenda.id);

    this.errorMessage.set(null);

    this.agendaApiService.delete(agenda.id).subscribe({
      next: () => {
        this.agendas.update((agendas) => agendas.filter((item) => item.id !== agenda.id));

        this.deletingId.set(null);
      },

      error: () => {
        this.errorMessage.set('No fue posible eliminar la agenda.');

        this.deletingId.set(null);
      },
    });
  }
}
