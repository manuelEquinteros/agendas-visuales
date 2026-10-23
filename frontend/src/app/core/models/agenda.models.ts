export type TipoAgenda = 'DIARIA' | 'SEMANAL';


export interface Pictograma {
  idArasaac: number;
  descripcion: string;
  urlImagen: string;
}


export interface ActividadAgendaRequest {
  id?: number | null;

  titulo: string;
  descripcion?: string | null;

  orden: number;

  horaInicio?: string | null;
  horaFin?: string | null;

  acompaniante?: string | null;

  conceptoVisual: string;

  pictograma?: Pictograma | null;
}


export interface DiaAgendaRequest {
  id?: number | null;

  fecha: string;
  orden: number;

  actividades: ActividadAgendaRequest[];
}


export interface AgendaRequest {
  titulo: string;

  tipoAgenda: TipoAgenda;

  fechaInicio: string;
  fechaFin?: string | null;

  dias: DiaAgendaRequest[];
}


export interface ActividadAgendaResponse {
  id: number;

  titulo: string;
  descripcion: string | null;

  orden: number;

  horaInicio: string | null;
  horaFin: string | null;

  acompaniante: string | null;

  conceptoVisual: string;

  pictograma: Pictograma | null;
}


export interface DiaAgendaResponse {
  id: number;

  fecha: string;
  orden: number;

  actividades: ActividadAgendaResponse[];
}


export interface AgendaResponse {
  id: number;

  titulo: string;

  tipoAgenda: TipoAgenda;

  fechaInicio: string;
  fechaFin: string | null;

  fechaCreacion: string;
  fechaModificacion: string;

  dias: DiaAgendaResponse[];
}