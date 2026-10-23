import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AgendaRequest, AgendaResponse } from '../models/agenda.models';

@Injectable({
  providedIn: 'root',
})
export class AgendaApiService {
  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.apiUrl}/agendas`;

  list(): Observable<AgendaResponse[]> {
    return this.http.get<AgendaResponse[]>(this.baseUrl);
  }

  get(id: number): Observable<AgendaResponse> {
    return this.http.get<AgendaResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: AgendaRequest): Observable<AgendaResponse> {
    return this.http.post<AgendaResponse>(this.baseUrl, request);
  }

  update(id: number, request: AgendaRequest): Observable<AgendaResponse> {
    return this.http.put<AgendaResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
