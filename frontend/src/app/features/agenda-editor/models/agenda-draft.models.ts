import {
  Pictograma,
  TipoAgenda
} from '../../../core/models/agenda.models';


export interface ActividadAgendaDraft {

  clientId: string;

  id: number | null;

  titulo: string;
  descripcion: string | null;

  orden: number;

  horaInicio: string | null;
  horaFin: string | null;

  acompaniante: string | null;

  conceptoVisual: string;

  pictograma: Pictograma | null;
}


export interface DiaAgendaDraft {

  clientId: string;

  id: number | null;

  fecha: string;
  orden: number;

  actividades: ActividadAgendaDraft[];
}


export interface AgendaDraft {

  id: number | null;

  titulo: string;

  tipoAgenda: TipoAgenda;

  fechaInicio: string;
  fechaFin: string | null;

  dias: DiaAgendaDraft[];
}