import {
  Injectable,
  inject
} from '@angular/core';

import {
  PictogramApiService
} from '../api/pictogram-api.service';

import {
  PictogramaCandidate
} from '../models/pictogram.models';

import {
  AgendaEditorStateService
} from '../../features/agenda-editor/services/agenda-editor-state.service';

import {
  PictogramCandidateCacheService
} from './pictogram-candidate-cache.service';


@Injectable({
  providedIn: 'root'
})
export class PictogramSuggestionService {

  private readonly pictogramApiService =
    inject(PictogramApiService);

  private readonly editorState =
    inject(AgendaEditorStateService);

  private readonly candidateCache =
    inject(PictogramCandidateCacheService);


  suggestMissingPictograms(): void {

    const agenda =
      this.editorState.agenda();


    if (!agenda) {
      return;
    }


    for (const dia of agenda.dias) {

      for (const actividad of dia.actividades) {

        if (actividad.pictograma) {
          continue;
        }


        const term =
          (
            actividad.conceptoVisual
            || actividad.titulo
          ).trim();


        if (
          !term ||
          this.normalizeTerm(term) ===
            'nueva actividad'
        ) {
          continue;
        }


        this.suggestForActivity(
          actividad.clientId,
          term
        );
      }
    }
  }


  suggestForActivity(
    activityClientId: string,
    term: string
  ): void {

    const normalizedTerm =
      term.trim();


    if (!normalizedTerm) {
      return;
    }


    const cachedCandidates =
      this.candidateCache.get(
        activityClientId,
        normalizedTerm
      );


    if (cachedCandidates !== null) {

      this.assignFirstCandidate(
        activityClientId,
        normalizedTerm,
        cachedCandidates
      );

      return;
    }


    this.pictogramApiService
      .search(normalizedTerm)
      .subscribe({

        next: candidates => {

          this.candidateCache.set(
            activityClientId,
            normalizedTerm,
            candidates
          );


          this.assignFirstCandidate(
            activityClientId,
            normalizedTerm,
            candidates
          );
        },


        /*
         * ARASAAC es un enriquecimiento.
         * Una falla nunca debe impedir que
         * la actividad exista.
         */
        error: () => {
          // Sin acción.
        }

      });
  }


  private assignFirstCandidate(
    activityClientId: string,
    originalTerm: string,
    candidates: PictogramaCandidate[]
  ): void {

    if (candidates.length === 0) {
      return;
    }


    const agenda =
      this.editorState.agenda();


    if (!agenda) {
      return;
    }


    const actividad =
      agenda.dias
        .flatMap(
          dia =>
            dia.actividades
        )
        .find(
          item =>
            item.clientId ===
            activityClientId
        );


    /*
     * La actividad pudo haber sido eliminada
     * mientras ARASAAC respondía.
     */
    if (!actividad) {
      return;
    }


    /*
     * Si mientras esperábamos el usuario
     * eligió un pictograma manualmente,
     * nunca lo reemplazamos.
     */
    if (actividad.pictograma) {
      return;
    }


    const currentTerm =
      (
        actividad.conceptoVisual
        || actividad.titulo
      ).trim();


    /*
     * Si cambió el concepto mientras la
     * consulta estaba en curso, ignoramos
     * la respuesta vieja.
     */
    if (
      this.normalizeTerm(currentTerm) !==
      this.normalizeTerm(originalTerm)
    ) {
      return;
    }


    const firstCandidate =
      candidates[0];


    this.editorState
      .assignPictogram(
        activityClientId,
        {
          idArasaac:
            firstCandidate.idArasaac,

          descripcion:
            firstCandidate.descripcion,

          urlImagen:
            firstCandidate.urlImagen
        }
      );
  }


  private normalizeTerm(
    value: string
  ): string {

    return value
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(
        /[\u0300-\u036f]/g,
        ''
      );
  }
}