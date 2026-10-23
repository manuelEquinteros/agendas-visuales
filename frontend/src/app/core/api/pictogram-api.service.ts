import {
    Injectable,
    inject
} from '@angular/core';

import {
    HttpClient,
    HttpParams
} from '@angular/common/http';

import {
    Observable
} from 'rxjs';

import {
    environment
} from '../../../environments/environment';

import {
    PictogramaCandidate
} from '../models/pictogram.models';


@Injectable({
    providedIn: 'root'
})
export class PictogramApiService {

    private readonly http =
        inject(HttpClient);


    private readonly baseUrl =
        `${environment.apiUrl}/pictograms`;


    search(
        term: string
    ): Observable<PictogramaCandidate[]> {

        const params =
            new HttpParams()
                .set(
                    'q',
                    term.trim()
                );


        return this.http.get<
            PictogramaCandidate[]
        >(
            `${this.baseUrl}/search`,
            {
                params
            }
        );
    }
}