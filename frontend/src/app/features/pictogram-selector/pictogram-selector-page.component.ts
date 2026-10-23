import { Component, inject, OnInit, signal } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { HttpErrorResponse } from '@angular/common/http';

import { ActivatedRoute, Router } from '@angular/router';

import { PictogramApiService } from '../../core/api/pictogram-api.service';

import { PictogramaCandidate } from '../../core/models/pictogram.models';

import { getApiErrorCode } from '../../core/models/api-error.models';

import { ActividadAgendaDraft } from '../agenda-editor/models/agenda-draft.models';

import { AgendaEditorStateService } from '../agenda-editor/services/agenda-editor-state.service';

import { PictogramCandidateCardComponent } from './components/pictogram-candidate-card/pictogram-candidate-card.component';

import { PictogramPreviewComponent } from './components/pictogram-preview/pictogram-preview.component';

import { PictogramCandidateCacheService } from '../../core/services/pictogram-candidate-cache.service';

@Component({
  selector: 'app-pictogram-selector-page',
  standalone: true,
  imports: [FormsModule, PictogramCandidateCardComponent, PictogramPreviewComponent],
  templateUrl: './pictogram-selector-page.component.html',
  styleUrl: './pictogram-selector-page.component.scss',
})
export class PictogramSelectorPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly editorState = inject(AgendaEditorStateService);

  private readonly pictogramApiService = inject(PictogramApiService);

  private readonly candidateCache = inject(PictogramCandidateCacheService);

  readonly activity = signal<ActividadAgendaDraft | null>(null);

  readonly candidates = signal<PictogramaCandidate[]>([]);

  readonly selectedCandidate = signal<PictogramaCandidate | null>(null);

  readonly loading = signal(false);

  readonly errorMessage = signal<string | null>(null);

  readonly hasSearched = signal(false);

  readonly lastSearchTerm = signal('');

  searchTerm = '';

  ngOnInit(): void {
    const activityClientId = this.route.snapshot.paramMap.get('activityId');

    const agenda = this.editorState.agenda();

    if (!activityClientId || !agenda) {
      this.errorMessage.set('No fue posible recuperar la actividad seleccionada.');

      return;
    }

    const activity = agenda.dias
      .flatMap((dia) => dia.actividades)
      .find((item) => item.clientId === activityClientId);

    if (!activity) {
      this.errorMessage.set('No se encontró la actividad que querés modificar.');

      return;
    }

    this.activity.set(activity);

    this.searchTerm = (activity.conceptoVisual || activity.titulo).trim();

    if (this.searchTerm) {
      const cachedCandidates = this.candidateCache.get(activity.clientId, this.searchTerm);

      if (cachedCandidates) {
        this.candidates.set(cachedCandidates);

        this.lastSearchTerm.set(this.searchTerm);

        this.hasSearched.set(true);

        const selected = activity.pictograma
          ? (cachedCandidates.find(
              (candidate) => candidate.idArasaac === activity.pictograma?.idArasaac,
            ) ?? null)
          : null;

        this.selectedCandidate.set(selected);

        return;
      }

      this.search();
    }
  }

  search(): void {
    const term = this.searchTerm.trim();

    if (!term) {
      this.candidates.set([]);

      this.selectedCandidate.set(null);

      this.hasSearched.set(false);

      this.errorMessage.set('Ingresá una palabra para buscar pictogramas.');

      return;
    }

    this.loading.set(true);

    this.errorMessage.set(null);

    this.lastSearchTerm.set(term);

    this.pictogramApiService.search(term).subscribe({
      next: (candidates) => {
        const activity = this.activity();

        if (activity) {
          this.candidateCache.set(activity.clientId, term, candidates);
        }

        this.candidates.set(candidates);

        const selected = activity?.pictograma
          ? (candidates.find(
              (candidate) => candidate.idArasaac === activity.pictograma?.idArasaac,
            ) ?? null)
          : null;

        this.selectedCandidate.set(selected);

        this.hasSearched.set(true);

        this.loading.set(false);
      },

      error: (error: HttpErrorResponse) => {
        this.candidates.set([]);

        this.selectedCandidate.set(null);

        this.hasSearched.set(true);

        this.loading.set(false);

        const errorCode = getApiErrorCode(error);

        if (errorCode === 'ARASAAC_UNAVAILABLE') {
          this.errorMessage.set(
            'No fue posible consultar ARASAAC en este momento. ' + 'Podés volver a intentarlo.',
          );

          return;
        }

        if (error.status === 0) {
          this.errorMessage.set(
            'No fue posible conectar con el servidor de pictogramas. ' +
              'Podés volver a intentarlo.',
          );

          return;
        }

        this.errorMessage.set('No fue posible consultar los pictogramas en este momento.');
      },
    });
  }

  selectCandidate(candidate: PictogramaCandidate): void {
    this.selectedCandidate.set(candidate);
  }

  backToEditor(): void {
    const agenda = this.editorState.agenda();

    if (!agenda) {
      this.router.navigate(['/agendas']);

      return;
    }

    if (agenda.id === null) {
      this.router.navigate(['/agendas/new']);

      return;
    }

    this.router.navigate(['/agendas', agenda.id, 'edit']);
  }

  confirmAndApply(): void {
    const candidate = this.selectedCandidate();

    const activity = this.activity();

    if (!candidate || !activity) {
      return;
    }

    this.editorState.assignPictogram(activity.clientId, {
      idArasaac: candidate.idArasaac,

      descripcion: candidate.descripcion,

      urlImagen: candidate.urlImagen,
    });

    this.backToEditor();
  }
}
