import {
  Injectable
} from '@angular/core';

import {
  PictogramaCandidate
} from '../models/pictogram.models';


interface CachedPictogramCandidates {
  term: string;
  candidates: PictogramaCandidate[];
}


@Injectable({
  providedIn: 'root'
})
export class PictogramCandidateCacheService {

  private readonly cache =
    new Map<
      string,
      CachedPictogramCandidates
    >();


  get(
    activityClientId: string,
    term: string
  ): PictogramaCandidate[] | null {

    const entry =
      this.cache.get(
        activityClientId
      );


    if (!entry) {
      return null;
    }


    if (
      entry.term !==
      this.normalizeTerm(term)
    ) {
      return null;
    }


    return entry.candidates.map(
      candidate => ({
        ...candidate
      })
    );
  }


  set(
    activityClientId: string,
    term: string,
    candidates: PictogramaCandidate[]
  ): void {

    this.cache.set(
      activityClientId,
      {
        term:
          this.normalizeTerm(term),

        candidates:
          candidates.map(
            candidate => ({
              ...candidate
            })
          )
      }
    );
  }


  clear(
    activityClientId: string
  ): void {

    this.cache.delete(
      activityClientId
    );
  }


  clearAll(): void {

    this.cache.clear();
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