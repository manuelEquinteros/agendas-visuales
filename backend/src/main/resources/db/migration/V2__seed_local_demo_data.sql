-- =========================================================
-- Dataset local de demostracion
-- =========================================================

-- Limpieza preventiva por si quedo informacion de una
-- ejecucion manual previa.
DELETE FROM agenda_visual
WHERE titulo IN (
    'Rutina de mañana',
    'Semana escolar'
);


-- =========================================================
-- Agenda diaria
-- =========================================================

INSERT INTO agenda_visual (
    titulo,
    tipo_agenda,
    fecha_inicio,
    fecha_fin,
    fecha_creacion,
    fecha_modificacion
)
VALUES (
    'Rutina de mañana',
    'DIARIA',
    DATE '2026-10-05',
    DATE '2026-10-05',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


INSERT INTO dia_agenda (
    id_agenda,
    fecha,
    orden
)
SELECT
    id_agenda,
    DATE '2026-10-05',
    1
FROM agenda_visual
WHERE titulo = 'Rutina de mañana';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Desayunar',
    NULL,
    1,
    TIME '07:30',
    NULL::TIME,
    NULL,
    'desayunar',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Rutina de mañana';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Vestirse',
    NULL,
    2,
    TIME '08:00',
    NULL::TIME,
    'Mamá',
    'vestirse',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Rutina de mañana';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Ir a la escuela',
    NULL,
    3,
    TIME '08:30',
    NULL::TIME,
    'Mamá',
    'escuela',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Rutina de mañana';


-- =========================================================
-- Agenda semanal
-- =========================================================

INSERT INTO agenda_visual (
    titulo,
    tipo_agenda,
    fecha_inicio,
    fecha_fin,
    fecha_creacion,
    fecha_modificacion
)
VALUES (
    'Semana escolar',
    'SEMANAL',
    DATE '2026-10-05',
    DATE '2026-10-09',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


INSERT INTO dia_agenda (
    id_agenda,
    fecha,
    orden
)
SELECT id_agenda, DATE '2026-10-05', 1
FROM agenda_visual
WHERE titulo = 'Semana escolar';


INSERT INTO dia_agenda (
    id_agenda,
    fecha,
    orden
)
SELECT id_agenda, DATE '2026-10-06', 2
FROM agenda_visual
WHERE titulo = 'Semana escolar';


INSERT INTO dia_agenda (
    id_agenda,
    fecha,
    orden
)
SELECT id_agenda, DATE '2026-10-07', 3
FROM agenda_visual
WHERE titulo = 'Semana escolar';


INSERT INTO dia_agenda (
    id_agenda,
    fecha,
    orden
)
SELECT id_agenda, DATE '2026-10-08', 4
FROM agenda_visual
WHERE titulo = 'Semana escolar';


INSERT INTO dia_agenda (
    id_agenda,
    fecha,
    orden
)
SELECT id_agenda, DATE '2026-10-09', 5
FROM agenda_visual
WHERE titulo = 'Semana escolar';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Escuela',
    NULL,
    1,
    TIME '08:00',
    TIME '12:30',
    'Docente',
    'escuela',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Semana escolar'
  AND d.fecha = DATE '2026-10-05';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Fonoaudiología',
    NULL,
    1,
    TIME '16:00',
    TIME '17:00',
    'Mamá',
    'fonoaudiologia',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Semana escolar'
  AND d.fecha = DATE '2026-10-06';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Natación',
    NULL,
    1,
    TIME '17:00',
    TIME '18:00',
    'Papá',
    'natacion',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Semana escolar'
  AND d.fecha = DATE '2026-10-07';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Hacer la tarea',
    NULL,
    1,
    NULL::TIME,
    NULL::TIME,
    'Mamá',
    'hacer tarea escolar',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Semana escolar'
  AND d.fecha = DATE '2026-10-08';


INSERT INTO actividad_agenda (
    id_dia,
    titulo,
    descripcion,
    orden,
    hora_inicio,
    hora_fin,
    acompaniante,
    concepto_visual,
    id_arasaac
)
SELECT
    d.id_dia,
    'Escuela',
    NULL,
    1,
    TIME '08:00',
    TIME '12:30',
    'Docente',
    'escuela',
    NULL
FROM dia_agenda d
JOIN agenda_visual a
    ON a.id_agenda = d.id_agenda
WHERE a.titulo = 'Semana escolar'
  AND d.fecha = DATE '2026-10-09';