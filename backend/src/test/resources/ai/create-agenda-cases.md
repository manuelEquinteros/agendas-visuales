# Casos de evaluación — Interpretación de agendas

Modelo de referencia:
`gemini-3.5-flash-lite`

Estos casos validan el comportamiento funcional del intérprete real.

No se evalúa el texto exacto de `assistantMessage`.
Se evalúan estado, estructura y datos relevantes.

---

## AI-01 — Agenda diaria completa

### Mensaje

Creá una agenda diaria para mañana con desayunar a las 8:00 y vestirse a las 8:30.

### Esperado

- status: COMPLETE
- tipoAgenda: DIARIA
- fechaInicio: fecha actual + 1 día
- fechaFin: null
- exactamente un día
- actividades:
  1. Desayunar
     - horaInicio: 08:00
     - horaFin: null
  2. Vestirse
     - horaInicio: 08:30
     - horaFin: null
- no inventar acompañante
- no inventar descripción
- pictograma: null

---

## AI-02 — Agenda sin fecha

### Mensaje

Creá una agenda diaria con desayunar y vestirse.

### Esperado

- status: INCOMPLETE
- missingFields contiene únicamente fechaInicio
- fechaInicio: null
- conserva las actividades interpretadas
- no solicita horarios
- horarios: null

---

## AI-03 — Agenda diaria sin horarios

### Mensaje

Para mañana quiero una agenda con desayunar, vestirse e ir a la escuela.

### Esperado

- status: COMPLETE
- tipoAgenda: DIARIA
- fechaInicio: fecha actual + 1 día
- actividades en el mismo orden:
  1. Desayunar
  2. Vestirse
  3. Ir a la escuela
- todos los horarios: null
- no inventar horarios

---

## AI-04 — Agenda semanal

### Mensaje

Creá una agenda semanal desde el próximo lunes.
El lunes tiene escuela y natación.
El martes tiene terapia.
El miércoles tiene escuela.

### Esperado

- status: COMPLETE
- tipoAgenda: SEMANAL
- fechaInicio corresponde al próximo lunes
- días correctamente fechados
- actividades asociadas al día indicado
- orden de días consecutivo
- orden de actividades consecutivo
- no inventar horarios

---

## AI-05 — Fecha explícita

### Mensaje

Creá una agenda diaria para el 15/10/2026 con almorzar y hacer la tarea.

### Esperado

- status: COMPLETE
- tipoAgenda: DIARIA
- fechaInicio: 2026-10-15
- fechaFin: null
- un único día con fecha 2026-10-15
- Almorzar antes de Hacer la tarea

---

## AI-06 — Fuera de alcance

### Mensaje

¿Cuál es la capital de Francia?

### Esperado

- status: OUT_OF_SCOPE
- agenda: null
- missingFields: []
- assistantMessage indica brevemente que el asistente trabaja con agendas

---

## AI-07 — Mensaje vacío funcionalmente

### Mensaje

Quiero hacer una agenda.

### Esperado

- status: INCOMPLETE
- no debe inventar actividades
- debe solicitar únicamente información indispensable para poder construir la agenda
- no debe devolver COMPLETE con una agenda inventada

---

## AI-08 — Hora de inicio sin hora final

### Mensaje

Creá una agenda para mañana con natación a las 17:00.

### Esperado

- status: COMPLETE
- Natación:
  - horaInicio: 17:00
  - horaFin: null
- no inferir duración de la actividad

---

## AI-09 — Acompañante explícito

### Mensaje

Mañana a las 10:00 tiene fonoaudiología con mamá.

### Esperado

- status: COMPLETE
- actividad: Fonoaudiología
- horaInicio: 10:00
- acompaniante: mamá
- horaFin: null
- no inventar descripción

---

## Criterio general de aprobación

Un caso se considera aprobado cuando:

- el status es semánticamente correcto;
- no se inventan datos relevantes;
- las fechas relativas son correctas;
- los datos opcionales ausentes permanecen null;
- se conserva el orden indicado por el usuario;
- la estructura resultante supera la validación Java;
- no se generan pictogramas ni datos ARASAAC;
- no se genera información fuera del alcance del schema.