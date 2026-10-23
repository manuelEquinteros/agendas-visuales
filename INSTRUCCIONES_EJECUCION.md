# Guía de instalación, configuración y ejecución local

## Agendas Visuales — Seminario Final de Informática

Este documento describe los pasos necesarios para ejecutar localmente el MVP de **Agendas Visuales** en una computadora distinta de la utilizada durante el desarrollo.

La aplicación está compuesta por:

- un backend REST desarrollado con Java y Spring Boot;
- un frontend desarrollado con Angular;
- una base de datos PostgreSQL;
- integración con Google Gemini para interpretación de lenguaje natural;
- integración con ARASAAC para búsqueda de pictogramas.

> **Importante:** las credenciales y claves privadas no se incluyen en el proyecto. Cada persona que ejecute la aplicación debe configurar sus propios valores mediante variables de entorno.

---

## 1. Versiones de referencia

El proyecto fue desarrollado y probado con las siguientes versiones:

| Componente | Versión de referencia |
| --- | --- |
| Java | 25 LTS |
| Spring Boot | 4.1.1 |
| Spring Framework | 7.x |
| Spring AI | 2.0.1 |
| Node.js | 24 LTS |
| Angular | 22 |
| TypeScript | 6.x |
| PostgreSQL | 18 |

Se recomienda utilizar estas versiones o versiones compatibles de la misma línea estable.

---

## 2. Requisitos previos

Antes de iniciar la aplicación deben estar instalados:

1. **Java 25 LTS**.
2. **Node.js 24 LTS**, incluyendo `npm`.
3. **PostgreSQL 18**.
4. Un navegador web moderno.
5. Conexión a Internet para acceder a:
   - Google Gemini Developer API;
   - ARASAAC.

No es necesario instalar Maven globalmente porque el backend incluye **Maven Wrapper**.

Tampoco es necesario instalar Angular CLI globalmente para ejecutar el proyecto mediante los scripts de `npm`.

---

## 3. Verificar las herramientas instaladas

Abrir PowerShell y ejecutar:

```powershell
java --version
node --version
npm --version
psql --version
```

Como referencia, deberían corresponder aproximadamente a:

```text
Java 25
Node.js 24
PostgreSQL 18
```

Si `psql` no es reconocido pero PostgreSQL está instalado, puede utilizarse **SQL Shell (psql)** incluido con PostgreSQL o agregar la carpeta `bin` de PostgreSQL al `PATH`.

---

## 4. Ubicar el proyecto

Descomprimir o copiar el proyecto en una carpeta local.

La estructura principal debe ser similar a:

```text
Seminario-final/
├── backend/
├── frontend/
├── .gitignore
└── README.md
```

En los ejemplos siguientes se asume que PowerShell se abre dentro de la carpeta raíz `Seminario-final`.

La ubicación física puede ser cualquiera; **no es necesario utilizar `E:\Seminario-final`**.

---

## 5. Preparar PostgreSQL

La configuración predeterminada del proyecto utiliza:

```text
Host: localhost
Puerto: 5432
Base de datos: agendasvisuales
Usuario: agendasvisuales_app
```

La contraseña no está incluida en el código.

### 5.1 Iniciar PostgreSQL

Verificar que el servicio de PostgreSQL esté iniciado.

En Windows también puede comprobarse desde:

```text
Servicios > PostgreSQL
```

o utilizando las herramientas instaladas junto con PostgreSQL.

### 5.2 Crear usuario y base de datos

Abrir **SQL Shell (psql)** o una sesión de `psql` con un usuario administrador, normalmente `postgres`.

Ejecutar:

```sql
CREATE ROLE agendasvisuales_app
WITH LOGIN
PASSWORD 'REEMPLAZAR_POR_UNA_PASSWORD_LOCAL';

CREATE DATABASE agendasvisuales
OWNER agendasvisuales_app;
```

La contraseña elegida deberá coincidir luego con la variable `DB_PASSWORD`.

Si el usuario o la base ya existen, no es necesario crearlos nuevamente.

### 5.3 Configuración alternativa

No es obligatorio utilizar esos nombres.

El backend permite indicar otra URL y otro usuario mediante:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Por ejemplo:

```text
DB_URL=jdbc:postgresql://localhost:5432/otra_base
DB_USERNAME=otro_usuario
DB_PASSWORD=otra_password
```

El usuario configurado debe tener permisos suficientes para que Flyway cree y actualice los objetos del esquema.

---

## 6. Obtener una API key de Gemini

La funcionalidad de inteligencia artificial requiere una API key válida de **Google Gemini Developer API**.

La clave no se distribuye junto con el proyecto.

Cada persona que ejecute la aplicación debe utilizar una API key propia y habilitada para el servicio correspondiente.

> Nunca escribir una API key real dentro de `application.yml`, `application-local.yml`, archivos Markdown o archivos que puedan versionarse.

---

## 7. Configurar variables de entorno

### 7.1 Abrir la terminal del backend

Abrir una ventana nueva de PowerShell y ubicarse en la raíz del proyecto.

Las variables configuradas con `$env:` tienen alcance sobre **esa sesión de PowerShell**. Por ese motivo, deben configurarse en la misma terminal desde la que luego se inicia el backend.

### 7.2 Variables de PostgreSQL

Si se utilizan los nombres predeterminados de base y usuario:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/agendasvisuales"
$env:DB_USERNAME="agendasvisuales_app"
$env:DB_PASSWORD="<PASSWORD_ELEGIDA_EN_POSTGRESQL>"
```

### 7.3 Variables de Gemini

Configurar:

```powershell
$env:GEMINI_API_KEY="<API_KEY_PROPIA>"
$env:GEMINI_MODEL="gemini-3.5-flash-lite"
$env:AI_PROVIDER="gemini"
$env:SPRING_AI_CHAT_MODEL="google-genai"
```

### 7.4 Perfil local

Para hacer explícito el perfil utilizado durante la ejecución de la demo:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
```

### 7.5 Verificar sin mostrar secretos

Pueden verificarse las variables no sensibles con:

```powershell
$env:DB_URL
$env:DB_USERNAME
$env:GEMINI_MODEL
$env:AI_PROVIDER
$env:SPRING_AI_CHAT_MODEL
$env:SPRING_PROFILES_ACTIVE
```

Para comprobar únicamente que las variables sensibles existen, sin imprimir su contenido:

```powershell
if ($env:DB_PASSWORD) { "DB_PASSWORD configurada" } else { "DB_PASSWORD NO configurada" }

if ($env:GEMINI_API_KEY) { "GEMINI_API_KEY configurada" } else { "GEMINI_API_KEY NO configurada" }
```

---

## 8. Configuración del backend

El archivo:

```text
backend/src/main/resources/application-local.yml
```

permite configurar la conexión sin modificar el código.

La configuración esperada es equivalente a:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/agendasvisuales}
    username: ${DB_USERNAME:agendasvisuales_app}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  flyway:
    enabled: true

  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY:}
        chat:
          model: ${GEMINI_MODEL:gemini-3.5-flash-lite}
          temperature: 0.2
```

No debe ser necesario editar este archivo para una ejecución normal.

---

## 9. Ejecutar las migraciones y levantar el backend

Desde la misma PowerShell en la que se configuraron las variables:

```powershell
cd .\backend
.\mvnw.cmd spring-boot:run
```

Durante el inicio:

1. Spring Boot se conecta a PostgreSQL.
2. Flyway valida y aplica automáticamente las migraciones pendientes.
3. Hibernate valida el esquema.
4. El servidor HTTP inicia en el puerto `8080`.

Cuando el arranque sea correcto, el log debe indicar que Tomcat se inició en el puerto 8080 y que la aplicación quedó levantada.

El backend queda disponible en:

```text
http://localhost:8080
```

> Mantener esta terminal abierta mientras se utiliza la aplicación.

---

## 10. Instalar dependencias del frontend

Abrir **otra ventana de PowerShell**.

Desde la raíz del proyecto:

```powershell
cd .\frontend
npm install
```

Este paso es necesario la primera vez que se prepara el proyecto en una computadora.

También debe repetirse si posteriormente cambian las dependencias de `package.json`.

---

## 11. Levantar el frontend

En la terminal del frontend ejecutar:

```powershell
npm start
```

La aplicación queda disponible normalmente en:

```text
http://localhost:4200
```

Abrir esa dirección en el navegador.

> Mantener también esta terminal abierta mientras se utiliza la aplicación.

---

## 12. Verificación inicial

Con PostgreSQL, backend y frontend activos:

1. Abrir `http://localhost:4200`.
2. Verificar que se muestre la pantalla **Mis Agendas Visuales**.
3. Abrir una agenda existente, si hay datos precargados.
4. Verificar que el editor cargue correctamente.
5. Volver al listado.
6. Crear una agenda nueva.

Si estos pasos funcionan, la comunicación básica entre frontend, backend y PostgreSQL está operativa.

---

## 13. Verificar la integración con ARASAAC

Dentro de una agenda:

1. Crear o seleccionar una actividad.
2. Abrir la opción para elegir un pictograma.
3. Realizar una búsqueda, por ejemplo:

```text
escuela
```

4. Verificar que aparezcan candidatos de ARASAAC.
5. Seleccionar un pictograma.
6. Volver al editor.
7. Guardar la agenda.
8. Volver a abrirla y comprobar que el pictograma permanezca asociado.

ARASAAC no requiere una API key propia para este proyecto, pero sí requiere conexión a Internet.

---

## 14. Verificar la integración con Gemini

Abrir una agenda o iniciar una nueva y utilizar el asistente.

Ejemplo de creación:

```text
Quiero una agenda diaria para el 25 de octubre de 2026.
A las 8 tiene desayuno y a las 10 terapia.
```

Verificar que:

1. el asistente procese la solicitud;
2. se genere la estructura de agenda;
3. las actividades aparezcan en el editor;
4. la aplicación pueda sugerir pictogramas independientemente mediante ARASAAC.

### Prueba de continuidad conversacional

Puede probarse:

```text
Agregá teatro a las 12 hs.
```

y luego:

```text
Cambiá eso a las 13 hs.
```

El segundo mensaje debe interpretarse utilizando el turno conversacional anterior y el estado actual de la agenda.

### Fechas fuera del período actual

Si se solicita una actividad para una fecha que no pertenece al período de la agenda, el asistente puede pedir una aclaración en lugar de modificar automáticamente las fechas de la agenda.

Este comportamiento es intencional para evitar cambios ambiguos.

---

## 15. Guardar y recuperar una agenda

Después de realizar cambios:

1. presionar **Guardar Agenda**;
2. esperar la confirmación de guardado;
3. volver a **Mis Agendas**;
4. abrir nuevamente la agenda.

Los datos deben recuperarse desde PostgreSQL, incluyendo las actividades y los pictogramas guardados.

---

## 16. Recorrido recomendado para la demostración

Para una demostración breve y representativa se recomienda el siguiente recorrido:

### A. Mis Agendas

- mostrar el listado de agendas;
- abrir una agenda existente;
- volver al listado.

### B. Edición manual

- crear una agenda;
- configurar título, tipo y fecha;
- agregar una actividad manualmente;
- modificar horario o acompañante;
- guardar.

### C. Pictogramas

- abrir el selector de pictogramas;
- buscar una alternativa en ARASAAC;
- elegirla;
- volver al editor;
- guardar y comprobar persistencia.

### D. Creación mediante IA

Ingresar una instrucción completa en lenguaje natural y mostrar cómo el asistente genera la estructura de agenda.

### E. Aclaración conversacional

Realizar una modificación que dependa del turno anterior, por ejemplo:

```text
Agregá teatro a las 12 hs.
```

seguido de:

```text
Cambiá eso a las 13 hs.
```

### F. Adaptación de una agenda existente

Pedir mediante lenguaje natural agregar, modificar o mover una actividad y verificar que las actividades no afectadas y sus pictogramas se conserven.

### G. Persistencia

Guardar, volver al listado y reabrir la agenda para mostrar que el resultado permanece almacenado.

---

## 17. Ejecutar los tests del backend

Con PostgreSQL disponible y las variables necesarias configuradas:

```powershell
cd .\backend
.\mvnw.cmd clean test
```

La ejecución correcta debe finalizar con:

```text
BUILD SUCCESS
```

La suite incluye pruebas de persistencia, validación de respuestas de IA y payloads inválidos, entre otras verificaciones del backend.

---

## 18. Compilar el frontend

Desde otra terminal:

```powershell
cd .\frontend
npm run build
```

La compilación debe finalizar sin errores.

Este paso permite verificar que el frontend completo compila correctamente antes de realizar la demostración.

---

## 19. Detener la aplicación

Para detener frontend o backend:

```text
Ctrl + C
```

en la terminal correspondiente.

Detener la aplicación no elimina la información almacenada en PostgreSQL.

---

## 20. Problemas frecuentes

### El backend no puede conectarse a PostgreSQL

Síntomas habituales:

```text
Connection refused
```

o errores de autenticación.

Verificar:

- que PostgreSQL esté iniciado;
- que `DB_URL` sea correcta;
- que `DB_USERNAME` exista;
- que `DB_PASSWORD` coincida con la contraseña configurada en PostgreSQL;
- que el puerto configurado sea el correcto.

### La variable existe en una terminal pero no en otra

Las asignaciones:

```powershell
$env:NOMBRE="valor"
```

sólo afectan la sesión actual de PowerShell.

Si se abre una terminal nueva para ejecutar el backend, las variables deben volver a configurarse allí.

### Gemini responde 401 o 403

Revisar:

- que `GEMINI_API_KEY` esté configurada;
- que la clave sea válida;
- que corresponda al servicio de Gemini utilizado por el proyecto.

Nunca copiar la clave a archivos del repositorio para solucionar este problema.

### Gemini responde 429

Normalmente indica un límite de cuota o tasa del proveedor.

Esperar y volver a intentar o revisar la cuota asociada a la API key.

La agenda existente no debe ser reemplazada por una respuesta fallida del servicio externo.

### ARASAAC no devuelve resultados

Puede significar:

- que no existen pictogramas para el término exacto;
- que existe un problema de conectividad;
- que el servicio externo no está disponible temporalmente.

Probar con un término más simple, por ejemplo:

```text
comer
escuela
dormir
```

Una falla de ARASAAC no debe impedir que la actividad exista.

### El puerto 8080 ya está ocupado

En PowerShell:

```powershell
netstat -ano | findstr :8080
```

Identificar el proceso que utiliza el puerto o cerrarlo antes de iniciar el backend.

### El puerto 4200 ya está ocupado

En PowerShell:

```powershell
netstat -ano | findstr :4200
```

Cerrar el proceso anterior de Angular o iniciar la aplicación en un puerto alternativo.

### `npm` no encuentra dependencias

Ejecutar nuevamente:

```powershell
cd .\frontend
npm install
```

y luego:

```powershell
npm start
```

### Error de Flyway

No modificar manualmente migraciones históricas ya aplicadas.

Verificar primero:

- conexión a la base;
- permisos del usuario;
- que se esté utilizando la base correcta;
- estado de las migraciones existentes.

Las migraciones del proyecto deben aplicarse automáticamente durante el inicio del backend.

---

## 21. Variables de entorno de referencia

| Variable | Obligatoria | Descripción | Valor de ejemplo |
| --- | --- | --- | --- |
| `DB_URL` | No, tiene valor por defecto | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/agendasvisuales` |
| `DB_USERNAME` | No, tiene valor por defecto | Usuario de PostgreSQL | `agendasvisuales_app` |
| `DB_PASSWORD` | Sí | Contraseña del usuario de PostgreSQL | No versionar |
| `GEMINI_API_KEY` | Sí para IA real | API key propia de Gemini | No versionar |
| `GEMINI_MODEL` | No | Modelo Gemini | `gemini-3.5-flash-lite` |
| `AI_PROVIDER` | Sí para usar Gemini | Proveedor de interpretación | `gemini` |
| `SPRING_AI_CHAT_MODEL` | Sí para proveedor real | Implementación de chat usada por Spring AI | `google-genai` |
| `SPRING_PROFILES_ACTIVE` | Recomendado | Perfil Spring para ejecución local | `local` |

---

## 22. Configuración rápida de una sesión PowerShell

Ejemplo completo antes de levantar el backend:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/agendasvisuales"
$env:DB_USERNAME="agendasvisuales_app"
$env:DB_PASSWORD="<PASSWORD_LOCAL>"

$env:GEMINI_API_KEY="<API_KEY_PROPIA>"
$env:GEMINI_MODEL="gemini-3.5-flash-lite"
$env:AI_PROVIDER="gemini"
$env:SPRING_AI_CHAT_MODEL="google-genai"

$env:SPRING_PROFILES_ACTIVE="local"

cd .\backend
.\mvnw.cmd spring-boot:run
```

En otra PowerShell:

```powershell
cd .\frontend
npm install
npm start
```

Luego abrir:

```text
http://localhost:4200
```

---

## 23. Orden completo de arranque

En una máquina ya preparada, el orden normal de ejecución es:

```text
1. Iniciar PostgreSQL
2. Abrir PowerShell del backend
3. Configurar variables de entorno
4. Iniciar Spring Boot
5. Abrir otra PowerShell
6. Iniciar Angular
7. Abrir http://localhost:4200
8. Ejecutar la demostración
```

---

## 24. Consideraciones de seguridad

No deben compartirse ni versionarse:

```text
DB_PASSWORD
GEMINI_API_KEY
```

El proyecto utiliza variables de entorno para mantener estos valores fuera del código fuente.

Antes de publicar o entregar el código, verificar que no existan claves reales en:

```text
*.yml
*.yaml
*.properties
*.md
.env
logs
capturas de pantalla
```

---

## 25. Alcance de la demo

El MVP está orientado a una demostración académica local.

El núcleo funcional incluye:

- listado de agendas;
- creación y edición manual;
- agendas diarias y semanales;
- persistencia PostgreSQL;
- interpretación de lenguaje natural mediante Gemini;
- adaptación de agendas existentes;
- búsqueda y selección de pictogramas ARASAAC;
- manejo controlado de errores externos.

No se requiere despliegue público para ejecutar la demostración local.
