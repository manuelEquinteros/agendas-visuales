import { Injectable, signal } from '@angular/core';

import { AgendaRequest, AgendaResponse, Pictograma } from '../../../core/models/agenda.models';

import { AgendaDraft, ActividadAgendaDraft, DiaAgendaDraft } from '../models/agenda-draft.models';

@Injectable({
  providedIn: 'root',
})
export class AgendaEditorStateService {
  private readonly agendaSignal = signal<AgendaDraft | null>(null);

  private readonly dirtySignal = signal(false);

  readonly agenda = this.agendaSignal.asReadonly();

  readonly dirty = this.dirtySignal.asReadonly();

  loadFromResponse(response: AgendaResponse): void {
    const draft: AgendaDraft = {
      id: response.id,

      titulo: response.titulo,

      tipoAgenda: response.tipoAgenda,

      fechaInicio: response.fechaInicio,

      fechaFin: response.fechaFin,

      dias: response.dias.map((dia) => ({
        clientId: `dia-${dia.id}`,

        id: dia.id,

        fecha: dia.fecha,

        orden: dia.orden,

        actividades: dia.actividades.map((actividad) => ({
          clientId: `actividad-${actividad.id}`,

          id: actividad.id,

          titulo: actividad.titulo,

          descripcion: actividad.descripcion,

          orden: actividad.orden,

          horaInicio: actividad.horaInicio,

          horaFin: actividad.horaFin,

          acompaniante: actividad.acompaniante,

          conceptoVisual: actividad.conceptoVisual,

          pictograma: actividad.pictograma
            ? {
                ...actividad.pictograma,
              }
            : null,
        })),
      })),
    };

    for (const dia of draft.dias) {
      dia.actividades = this.sortActivitiesByStartTime(dia.actividades);
    }

    this.agendaSignal.set(draft);

    this.dirtySignal.set(false);
  }

  createEmptyDraft(): void {
    const draft: AgendaDraft = {
      id: null,

      titulo: '',

      tipoAgenda: 'DIARIA',

      fechaInicio: '',

      fechaFin: null,

      dias: [
        {
          clientId: this.createClientId('dia'),

          id: null,

          fecha: '',

          orden: 1,

          actividades: [],
        },
      ],
    };

    this.agendaSignal.set(draft);

    this.dirtySignal.set(false);
  }

  applyGeneratedAgenda(request: AgendaRequest): void {
    const currentAgenda = this.agendaSignal();

    const usedActivityClientIds = new Set<string>();

    const draft: AgendaDraft = {
      id: currentAgenda?.id ?? null,

      titulo: request.titulo,

      tipoAgenda: request.tipoAgenda,

      fechaInicio: request.fechaInicio,

      fechaFin: request.fechaFin ?? null,

      dias: request.dias.map((dia) => {
        const currentDay = this.findMatchingDay(currentAgenda, dia);

        return {
          clientId: currentDay?.clientId ?? this.createClientId('dia'),

          id: currentDay?.id ?? dia.id ?? null,

          fecha: dia.fecha,

          orden: dia.orden,

          actividades: dia.actividades.map((actividad) => {
            const currentActivity = this.findMatchingActivity(
              currentAgenda,
              currentDay,
              dia,
              actividad,
              usedActivityClientIds,
            );

            if (currentActivity) {
              usedActivityClientIds.add(currentActivity.clientId);
            }

            const sameTitle =
              currentActivity !== null &&
              this.normalizeMatchText(currentActivity.titulo) ===
                this.normalizeMatchText(actividad.titulo);

            return {
              clientId: currentActivity?.clientId ?? this.createClientId('actividad'),

              id: currentActivity?.id ?? actividad.id ?? null,

              titulo: actividad.titulo,

              descripcion: actividad.descripcion ?? null,

              orden: actividad.orden,

              horaInicio: actividad.horaInicio ?? null,

              horaFin: actividad.horaFin ?? null,

              acompaniante: actividad.acompaniante ?? null,

              /*
               * Si es la misma actividad,
               * conservamos su concepto visual.
               *
               * Si cambió el título/concepto,
               * usamos el nuevo.
               */
              conceptoVisual:
                sameTitle && currentActivity
                  ? currentActivity.conceptoVisual
                  : actividad.conceptoVisual,

              /*
               * La IA nunca decide pictogramas.
               *
               * Si reconocemos la misma actividad,
               * preservamos el pictograma elegido.
               *
               * Si es una actividad nueva o cambió
               * su concepto, queda sin pictograma.
               */
              pictograma:
                sameTitle && currentActivity?.pictograma
                  ? {
                      ...currentActivity.pictograma,
                    }
                  : null,
            };
          }),
        };
      }),
    };

    for (const dia of draft.dias) {
      dia.actividades = this.sortActivitiesByStartTime(dia.actividades);
    }

    this.agendaSignal.set(draft);

    this.dirtySignal.set(true);
  }

  private findMatchingDay(
    currentAgenda: AgendaDraft | null,
    generatedDay: AgendaRequest['dias'][number],
  ): DiaAgendaDraft | null {
    if (!currentAgenda) {
      return null;
    }

    if (generatedDay.id !== null && generatedDay.id !== undefined) {
      const byId = currentAgenda.dias.find((dia) => dia.id === generatedDay.id);

      if (byId) {
        return byId;
      }
    }

    return currentAgenda.dias.find((dia) => dia.fecha === generatedDay.fecha) ?? null;
  }

  private findMatchingActivity(
    currentAgenda: AgendaDraft | null,
    currentDay: DiaAgendaDraft | null,
    generatedDay: AgendaRequest['dias'][number],
    generatedActivity: AgendaRequest['dias'][number]['actividades'][number],
    usedClientIds: Set<string>,
  ): ActividadAgendaDraft | null {
    if (!currentAgenda) {
      return null;
    }

    /*
     * 1. Si existe ID, es la coincidencia
     * más segura.
     */
    if (generatedActivity.id !== null && generatedActivity.id !== undefined) {
      const byId = currentAgenda.dias
        .flatMap((dia) => dia.actividades)
        .find(
          (actividad) =>
            actividad.id === generatedActivity.id && !usedClientIds.has(actividad.clientId),
        );

      if (byId) {
        return byId;
      }
    }

    const generatedTitle = this.normalizeMatchText(generatedActivity.titulo);

    /*
     * 2. Buscamos mismo título en el mismo día.
     */
    const sameDayByTitle = currentDay?.actividades.find(
      (actividad) =>
        !usedClientIds.has(actividad.clientId) &&
        this.normalizeMatchText(actividad.titulo) === generatedTitle,
    );

    if (sameDayByTitle) {
      return sameDayByTitle;
    }

    /*
     * 3. Buscamos mismo título en otro día.
     * Esto permite preservar una actividad
     * cuando la IA la mueve.
     */
    const anywhereByTitle = currentAgenda.dias
      .flatMap((dia) => dia.actividades)
      .find(
        (actividad) =>
          !usedClientIds.has(actividad.clientId) &&
          this.normalizeMatchText(actividad.titulo) === generatedTitle,
      );

    if (anywhereByTitle) {
      return anywhereByTitle;
    }

    /*
     * 4. Si la cantidad de actividades del día
     * no cambió, permitimos coincidencia por
     * posición.
     *
     * Esto sirve para un cambio de título:
     *
     * Vestirse
     *      ↓
     * Cepillarse los dientes
     *
     * Conservamos la identidad de la actividad,
     * pero luego el pictograma se elimina porque
     * sameTitle será false.
     */
    if (currentDay && currentDay.actividades.length === generatedDay.actividades.length) {
      const byOrder = currentDay.actividades.find(
        (actividad) =>
          actividad.orden === generatedActivity.orden && !usedClientIds.has(actividad.clientId),
      );

      if (byOrder) {
        return byOrder;
      }
    }

    return null;
  }

  private normalizeMatchText(value: string): string {
    return value
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9ñ]+/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
  }

  updateAgendaSettings(changes: {
    titulo: string;
    tipoAgenda: 'DIARIA' | 'SEMANAL';
    fechaInicio: string;
    fechaFin: string | null;
  }): void {
    this.updateAgenda((agenda) => {
      const oldDays = agenda.dias;

      agenda.titulo = changes.titulo;

      agenda.tipoAgenda = changes.tipoAgenda;

      agenda.fechaInicio = changes.fechaInicio;

      agenda.fechaFin = changes.tipoAgenda === 'SEMANAL' ? changes.fechaFin : null;

      /*
       * Agenda diaria:
       *
       * Conservamos únicamente el día que
       * corresponda exactamente a fechaInicio.
       *
       * No trasladamos actividades desde
       * otros días.
       */
      if (changes.tipoAgenda === 'DIARIA') {
        const existingDay = oldDays.find((dia) => dia.fecha === changes.fechaInicio);

        agenda.dias = [
          {
            clientId: existingDay?.clientId ?? this.createClientId('dia'),

            id: existingDay?.id ?? null,

            fecha: changes.fechaInicio,

            orden: 1,

            actividades: existingDay
              ? existingDay.actividades.map((actividad, index) => ({
                  ...actividad,
                  orden: index + 1,
                }))
              : [],
          },
        ];

        return agenda;
      }

      /*
       * Para una agenda semanal necesitamos
       * fechaFin para reconstruir el rango.
       */
      if (!changes.fechaFin) {
        return agenda;
      }

      const fechas = this.buildDateRange(changes.fechaInicio, changes.fechaFin);

      /*
       * La reconciliación se hace por FECHA,
       * no por posición.
       *
       * - Si la fecha ya existía, conservamos
       *   ese día y sus actividades.
       *
       * - Si la fecha es nueva, creamos un día
       *   vacío.
       *
       * - Los días que quedan fuera del nuevo
       *   rango simplemente desaparecen.
       */
      agenda.dias = fechas.map((fecha, index) => {
        const existingDay = oldDays.find((dia) => dia.fecha === fecha);

        return {
          clientId: existingDay?.clientId ?? this.createClientId('dia'),

          id: existingDay?.id ?? null,

          fecha,

          orden: index + 1,

          actividades: existingDay
            ? existingDay.actividades.map((actividad, activityIndex) => ({
                ...actividad,
                orden: activityIndex + 1,
              }))
            : [],
        };
      });

      return agenda;
    });
  }

  addActivity(dayClientId: string): void {
    this.updateAgenda((agenda) => {
      const dia = agenda.dias.find((item) => item.clientId === dayClientId);

      if (!dia) {
        return agenda;
      }

      const nuevaActividad: ActividadAgendaDraft = {
        clientId: this.createClientId('actividad'),

        id: null,

        titulo: 'Nueva actividad',

        descripcion: null,

        orden: dia.actividades.length + 1,

        horaInicio: null,

        horaFin: null,

        acompaniante: null,

        conceptoVisual: 'nueva actividad',

        pictograma: null,
      };

      dia.actividades.push(nuevaActividad);

      return agenda;
    });
  }

  deleteActivity(dayClientId: string, activityClientId: string): void {
    this.updateAgenda((agenda) => {
      const dia = agenda.dias.find((item) => item.clientId === dayClientId);

      if (!dia) {
        return agenda;
      }

      dia.actividades = dia.actividades
        .filter((actividad) => actividad.clientId !== activityClientId)
        .map((actividad, index) => ({
          ...actividad,
          orden: index + 1,
        }));

      return agenda;
    });
  }

  moveActivity(dayClientId: string, activityClientId: string, direction: 'up' | 'down'): void {
    this.updateAgenda((agenda) => {
      const dia = agenda.dias.find((item) => item.clientId === dayClientId);

      if (!dia) {
        return agenda;
      }

      const currentIndex = dia.actividades.findIndex(
        (actividad) => actividad.clientId === activityClientId,
      );

      if (currentIndex === -1) {
        return agenda;
      }

      const targetIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;

      if (targetIndex < 0 || targetIndex >= dia.actividades.length) {
        return agenda;
      }

      const currentActivity = dia.actividades[currentIndex];

      const targetActivity = dia.actividades[targetIndex];

      dia.actividades[currentIndex] = targetActivity;

      dia.actividades[targetIndex] = currentActivity;

      dia.actividades = dia.actividades.map((actividad, index) => ({
        ...actividad,
        orden: index + 1,
      }));

      return agenda;
    });
  }

  moveActivityToDay(
    sourceDayClientId: string,
    activityClientId: string,
    targetDayClientId: string,
  ): void {
    if (sourceDayClientId === targetDayClientId) {
      return;
    }

    this.updateAgenda((agenda) => {
      const sourceDay = agenda.dias.find((dia) => dia.clientId === sourceDayClientId);

      const targetDay = agenda.dias.find((dia) => dia.clientId === targetDayClientId);

      if (!sourceDay || !targetDay) {
        return agenda;
      }

      const activityIndex = sourceDay.actividades.findIndex(
        (actividad) => actividad.clientId === activityClientId,
      );

      if (activityIndex === -1) {
        return agenda;
      }

      const [movedActivity] = sourceDay.actividades.splice(activityIndex, 1);

      sourceDay.actividades = sourceDay.actividades.map((actividad, index) => ({
        ...actividad,
        orden: index + 1,
      }));

      movedActivity.orden = targetDay.actividades.length + 1;

      targetDay.actividades.push(movedActivity);

      targetDay.actividades = this.sortActivitiesByStartTime(targetDay.actividades);

      return agenda;
    });
  }

  updateActivity(
    dayClientId: string,
    activityClientId: string,
    changes: {
      titulo: string;
      descripcion: string | null;
      horaInicio: string | null;
      horaFin: string | null;
      acompaniante: string | null;
    },
  ): void {
    this.updateAgenda((agenda) => {
      const dia = agenda.dias.find((item) => item.clientId === dayClientId);

      if (!dia) {
        return agenda;
      }

      const actividad = dia.actividades.find((item) => item.clientId === activityClientId);

      if (!actividad) {
        return agenda;
      }

      const previousTitle = this.normalizeMatchText(actividad.titulo);

      const newTitle = this.normalizeMatchText(changes.titulo);

      const titleChanged = previousTitle !== newTitle;

      actividad.titulo = changes.titulo;

      actividad.descripcion = changes.descripcion;

      actividad.horaInicio = changes.horaInicio;

      actividad.horaFin = changes.horaFin;

      actividad.acompaniante = changes.acompaniante;

      if (titleChanged) {
        actividad.conceptoVisual = changes.titulo.trim().toLowerCase();

        actividad.pictograma = null;
      }

      dia.actividades = this.sortActivitiesByStartTime(dia.actividades);

      return agenda;
    });
  }

  assignPictogram(activityClientId: string, pictogram: Pictograma): void {
    this.updateAgenda((agenda) => {
      for (const dia of agenda.dias) {
        const actividad = dia.actividades.find((item) => item.clientId === activityClientId);

        if (actividad) {
          actividad.pictograma = {
            ...pictogram,
          };

          return agenda;
        }
      }

      return agenda;
    });
  }

  validateForSave(): string | null {
    const agenda = this.agendaSignal();

    if (!agenda) {
      return 'No hay una agenda para guardar.';
    }

    if (!agenda.titulo.trim()) {
      return 'Ingresá un título para la agenda.';
    }

    if (!agenda.fechaInicio) {
      return 'Ingresá la fecha de inicio.';
    }

    if (agenda.tipoAgenda === 'DIARIA') {
      if (agenda.dias.length !== 1) {
        return 'Una agenda diaria debe contener exactamente un día.';
      }
    }

    if (agenda.tipoAgenda === 'SEMANAL') {
      if (!agenda.fechaFin) {
        return 'Ingresá la fecha de fin para la agenda semanal.';
      }

      if (agenda.dias.length < 2) {
        return 'Una agenda semanal debe contener al menos dos días.';
      }
    }

    const totalActivities = agenda.dias.reduce((total, dia) => total + dia.actividades.length, 0);

    if (totalActivities === 0) {
      return 'La agenda debe contener al menos una actividad.';
    }

    return null;
  }

  toRequest(): AgendaRequest | null {
    const agenda = this.agendaSignal();

    if (!agenda) {
      return null;
    }

    return {
      titulo: agenda.titulo.trim(),

      tipoAgenda: agenda.tipoAgenda,

      fechaInicio: agenda.fechaInicio,

      fechaFin: agenda.tipoAgenda === 'SEMANAL' ? agenda.fechaFin : null,

      dias: agenda.dias.map((dia) => ({
        id: dia.id,

        fecha: dia.fecha,

        orden: dia.orden,

        actividades: dia.actividades.map((actividad) => ({
          id: actividad.id,

          titulo: actividad.titulo,

          descripcion: actividad.descripcion,

          orden: actividad.orden,

          horaInicio: actividad.horaInicio,

          horaFin: actividad.horaFin,

          acompaniante: actividad.acompaniante,

          conceptoVisual: actividad.conceptoVisual,

          pictograma: actividad.pictograma
            ? {
                ...actividad.pictograma,
              }
            : null,
        })),
      })),
    };
  }

  updateAgenda(updater: (agenda: AgendaDraft) => AgendaDraft): void {
    const current = this.agendaSignal();

    if (!current) {
      return;
    }

    const copy = structuredClone(current);

    const updated = updater(copy);

    this.agendaSignal.set(updated);

    this.dirtySignal.set(true);
  }

  markDirty(): void {
    this.dirtySignal.set(true);
  }

  markClean(): void {
    this.dirtySignal.set(false);
  }

  clear(): void {
    this.agendaSignal.set(null);

    this.dirtySignal.set(false);
  }

  createClientId(prefix: string): string {
    return `${prefix}-${crypto.randomUUID()}`;
  }

  private sortActivitiesByStartTime(activities: ActividadAgendaDraft[]): ActividadAgendaDraft[] {
    return activities
      .map((actividad, originalIndex) => ({
        actividad,
        originalIndex,
      }))
      .sort((left, right) => {
        const leftTime = this.timeToMinutes(left.actividad.horaInicio);

        const rightTime = this.timeToMinutes(right.actividad.horaInicio);

        if (leftTime === null && rightTime === null) {
          return left.originalIndex - right.originalIndex;
        }

        if (leftTime === null) {
          return 1;
        }

        if (rightTime === null) {
          return -1;
        }

        if (leftTime !== rightTime) {
          return leftTime - rightTime;
        }

        return left.originalIndex - right.originalIndex;
      })
      .map(({ actividad }, index) => ({
        ...actividad,
        orden: index + 1,
      }));
  }

  private timeToMinutes(value: string | null): number | null {
    if (!value) {
      return null;
    }

    const [hours, minutes] = value.split(':').map(Number);

    if (Number.isNaN(hours) || Number.isNaN(minutes)) {
      return null;
    }

    return hours * 60 + minutes;
  }

  private buildDateRange(start: string, end: string): string[] {
    const result: string[] = [];

    const [startYear, startMonth, startDay] = start.split('-').map(Number);

    const [endYear, endMonth, endDay] = end.split('-').map(Number);

    const current = new Date(Date.UTC(startYear, startMonth - 1, startDay));

    const last = new Date(Date.UTC(endYear, endMonth - 1, endDay));

    while (current <= last) {
      result.push(current.toISOString().substring(0, 10));

      current.setUTCDate(current.getUTCDate() + 1);
    }

    return result;
  }
}
