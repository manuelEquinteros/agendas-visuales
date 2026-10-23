import {
  HttpClient
} from '@angular/common/http';

import {
  inject,
  Injectable
} from '@angular/core';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../environments/environment';

import {
  InterpretAgendaRequest,
  InterpretationResult
} from '../models/assistant.models';


@Injectable({
  providedIn: 'root'
})
export class AssistantApiService {

  private readonly http =
    inject(HttpClient);


  private readonly baseUrl =
    `${environment.apiUrl}/assistant`;


  interpret(
    request: InterpretAgendaRequest
  ): Observable<InterpretationResult> {

    return this.http
      .post<InterpretationResult>(
        `${this.baseUrl}/interpret`,
        request
      );
  }
}