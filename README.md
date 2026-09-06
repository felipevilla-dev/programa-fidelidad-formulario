# Programa de Fidelidad — Formulario de Inscripción

Aplicación web para inscribir clientes a un programa de fidelidad compartido por
varias marcas de un mismo grupo. El usuario completa un formulario con sus datos
personales y de ubicación, y elige la marca a la que quiere vincularse.

**Marcas participantes:** Americanino, American Eagle, Chevignon, Esprit, Naf Naf y Rifle.

**Campos del formulario:**

| Campo | Tipo |
|---|---|
| Tipo de identificación | Lista desplegable (desde la base de datos) |
| Número de identificación | Texto |
| Nombres | Texto |
| Apellidos | Texto |
| Fecha de nacimiento | Fecha |
| Dirección | Texto |
| Ciudad | Lista desplegable (desde la base de datos) |
| Departamento | Lista desplegable (desde la base de datos) |
| País | Lista desplegable (desde la base de datos) |
| Marca | Lista desplegable (desde la base de datos) |

> Proyecto académico construido por etapas. Las seis etapas están completas: estructura del
> proyecto, modelo de datos, API REST, formulario en React y documentación de entrega.

**La API se puede explorar y probar desde el navegador.** Una vez levantado el backend,
<http://localhost:8080/swagger-ui.html> muestra todos los endpoints con sus datos de entrada,
sus respuestas y un botón para ejecutarlos — sin `curl` ni Postman. Los detalles están en
[Documentación interactiva (Swagger UI)](#documentación-interactiva-swagger-ui); cómo levantar
el backend, en [Puesta en marcha](#puesta-en-marcha).

El backend trae **23 pruebas automatizadas** que corren sobre una base en memoria, así que
`mvn test` funciona sin necesidad de instalar PostgreSQL: ver
[Pruebas automatizadas](#pruebas-automatizadas).

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3.5.16 · Maven |
| Persistencia | Spring Data JPA (Hibernate) · PostgreSQL |
| Utilidades backend | Bean Validation · Lombok · Spring Boot DevTools |
| Documentación de la API | springdoc-openapi (Swagger UI) |
| Pruebas | JUnit 5 · Mockito · H2 en memoria |
| Frontend | React 19 · Vite |

---

## Estructura del repositorio

```
.
├── README.md
├── .gitignore
├── database/
│   └── dump.sql                          # Estructura + datos semilla
├── backend/                              # API REST (Spring Boot)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/fidelidad/programa/
│       │   ├── ProgramaFidelidadApplication.java
│       │   ├── model/                    # Entidades JPA
│       │   ├── repository/               # Interfaces JpaRepository
│       │   ├── dto/                      # Objetos de transferencia de datos
│       │   ├── service/                  # Lógica de negocio
│       │   ├── validation/               # Validaciones y manejo de excepciones
│       │   ├── controller/               # Endpoints REST (uno por recurso)
│       │   └── config/                   # Infraestructura (CORS y OpenAPI)
│       └── resources/
│           ├── application.properties            # Configuración real
│           ├── application-example.properties    # Plantilla de referencia
│           └── data.sql                          # Datos semilla al arrancar
└── frontend/                             # Interfaz de usuario (React + Vite)
    ├── .env.example                      # Plantilla; el .env no se versiona
    └── src/
        ├── api/                          # Cliente axios y llamadas al backend
        ├── components/                   # FormularioInscripcion
        ├── utils/                        # Validación de los campos
        ├── pages/                        # Vistas completas (aún sin usar)
        └── styles.css                    # Hoja de estilos única
```

La explicación de cada capa está en [Arquitectura del backend](#arquitectura-del-backend).

---

## Arquitectura del backend

El backend está organizado en **capas**, cada una con una única responsabilidad. Una petición
las atraviesa siempre en el mismo orden, y cada capa solo habla con la siguiente:

```
   HTTP
    │
    ▼
┌─────────────┐   recibe la petición, valida el formato del cuerpo
│ controller  │   y devuelve el código HTTP. Sin lógica de negocio.
└──────┬──────┘
       │ DTO
       ▼
┌─────────────┐   reglas del programa: ¿existe esa ciudad?, ¿ya está
│  service    │   inscrito en esa marca? Abre la transacción.
└──────┬──────┘
       │ entidades
       ▼
┌─────────────┐   consultas a la base. Spring Data las implementa
│ repository  │   derivándolas del nombre del método.
└──────┬──────┘
       │
       ▼
┌─────────────┐   entidades JPA: el mapeo a las tablas de PostgreSQL.
│   model     │
└─────────────┘

   dto ......... objetos que viajan por la API, en los dos sentidos
   validation .. reglas de validación y traducción de errores a HTTP
   config ...... infraestructura (CORS y documentación OpenAPI)
```

| Capa | Qué contiene | Por qué existe |
|---|---|---|
| `model` | `Pais`, `Departamento`, `Ciudad`, `TipoIdentificacion`, `Marca`, `Cliente` | Entidades JPA. Definen las tablas y sus relaciones |
| `repository` | Seis interfaces `JpaRepository` | Acceso a datos sin escribir SQL: Spring Data deriva la consulta del nombre del método |
| `dto` | `ClienteRegistroDTO`, `ClienteResponseDTO` y los cinco DTO de catálogo | Separan el contrato de la API del esquema de la base. Sin ellos, cambiar una columna rompería a los clientes de la API |
| `service` | `ClienteService` y los cinco servicios de catálogo | La lógica de negocio, y la transacción dentro de la cual se navegan las relaciones perezosas |
| `validation` | Excepciones propias, `ErrorResponseDTO` y `GlobalExceptionHandler` | Reúne en un solo sitio la traducción de excepción a código HTTP |
| `controller` | Siete controladores, uno por recurso | Traducen HTTP a llamadas de servicio. Nada más |
| `config` | `CorsConfig`, `OpenApiConfig` | Infraestructura: ni negocio ni endpoints |

### Por qué las entidades no se exponen directamente

Sería más corto devolver la entidad `Cliente` desde el controlador, pero traería tres
problemas: expondría el esquema de la base en la API, arrastraría las relaciones perezosas de
JPA (que fallan al serializarse fuera de la transacción), y cualquier cambio en una columna
rompería a quien consume la API. Los DTO cortan esa dependencia.

---

## Modelo de datos

```
Pais 1 ──< Departamento 1 ──< Ciudad 1 ──┐
                                          │
TipoIdentificacion 1 ─────────────────────┤
                                          ├──< Cliente
Marca 1 ──────────────────────────────────┘
```

| Entidad | Campos | Restricción de unicidad |
|---|---|---|
| `Pais` | `id`, `nombre` | `nombre` |
| `Departamento` | `id`, `nombre`, `pais` | `(nombre, pais_id)` |
| `Ciudad` | `id`, `nombre`, `departamento` | `(nombre, departamento_id)` |
| `TipoIdentificacion` | `id`, `codigo`, `nombre` | `codigo` |
| `Marca` | `id`, `nombre` | `nombre` |
| `Cliente` | `tipoIdentificacion`, `numeroIdentificacion`, `nombres`, `apellidos`, `fechaNacimiento`, `direccion`, `ciudad`, `marca`, `fechaRegistro` | `(tipo_identificacion_id, numero_identificacion, marca_id)` |

### Por qué `Cliente` solo guarda la ciudad

El formulario pide ciudad, departamento y país, pero los tres no son datos
independientes: cada ciudad pertenece a un único departamento y cada departamento a un
único país. Esa jerarquía ya está en el modelo.

Si `Cliente` guardara también `departamento_id` y `pais_id`, la base podría terminar con
un cliente cuya ciudad es Medellín y cuyo departamento es Valle del Cauca, sin forma de
saber cuál de los dos es correcto. Guardando solo `ciudad_id`, ese estado inconsistente
es **imposible de representar**; el resto se deriva navegando
`cliente.getCiudad().getDepartamento().getPais()`.

### Datos semilla

`backend/src/main/resources/data.sql` puebla los catálogos en cada arranque:

| Tabla | Filas |
|---|---|
| `pais` | 1 (Colombia) |
| `departamento` | 32 |
| `ciudad` | 76 (capital de cada departamento + principales ciudades) |
| `tipo_identificacion` | 5 (CC, CE, TI, PA, NIT) |
| `marca` | 6 |

Dos detalles del script:

- Usa `INSERT ... ON CONFLICT DO NOTHING`, porque se ejecuta en **cada** arranque y no
  debe duplicar filas ni fallar por clave repetida.
- Requiere `spring.jpa.defer-datasource-initialization=true`. Desde Spring Boot 2.5,
  `data.sql` se ejecuta *antes* del DDL de Hibernate, así que sin esta propiedad fallaría
  con `relation does not exist`.

> Bogotá D.C. está registrada como ciudad de Cundinamarca. Administrativamente es un
> Distrito Capital independiente; se modela así para mantener uniforme la jerarquía
> País → Departamento → Ciudad que usan los desplegables en cascada.

---

## Requisitos previos

| Herramienta | Versión mínima | Cómo verificar |
|---|---|---|
| JDK | 17 (probado con 21) | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| PostgreSQL | 14+ (probado con 18) | `psql --version` |
| Node.js | 18+ (probado con 24) | `node -v` |

---

## Puesta en marcha

### 1. Base de datos

Primero se crea la base, que debe existir antes de arrancar el backend:

```bash
psql -U postgres -c "CREATE DATABASE fidelidad_db;"
```

A partir de ahí hay **dos formas** de tener las tablas y los catálogos. Cualquiera sirve.

#### Opción A — cargar el dump (recomendada)

Deja la base lista de una vez, con estructura y datos semilla:

```bash
psql -U postgres -d fidelidad_db -f database/dump.sql
```

Comprobar que quedó bien:

```bash
psql -U postgres -d fidelidad_db -c "SELECT (SELECT COUNT(*) FROM departamento) departamentos, (SELECT COUNT(*) FROM ciudad) ciudades, (SELECT COUNT(*) FROM marca) marcas;"
# esperado: 32 | 76 | 6
```

#### Opción B — dejar que lo haga el backend

Si se omite el paso anterior no pasa nada: al arrancar, Hibernate crea las tablas a partir de
las entidades (`ddl-auto=update`) y `data.sql` inserta los catálogos.

> Las dos opciones conviven sin chocar. Sobre una base ya creada por el dump, el `update` de
> Hibernate no encuentra nada que cambiar, y los `INSERT ... ON CONFLICT DO NOTHING` de
> `data.sql` no duplican filas.

**El dump contiene** la estructura completa (6 tablas, claves primarias y foráneas, y las
restricciones de unicidad) más 1 país, 32 departamentos, 76 ciudades, 5 tipos de identificación
y las 6 marcas. La tabla `cliente` va vacía: los clientes son datos de prueba, no semilla.

### 2. Backend

La conexión se configura con **variables de entorno**, para no guardar credenciales
en el repositorio. Si una variable no está definida, se usa el valor por defecto:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `fidelidad_db` | Nombre de la base de datos |
| `DB_USER` | `postgres` | Usuario de PostgreSQL |
| `DB_PASSWORD` | `postgres` | Contraseña del usuario |
| `SERVER_PORT` | `8080` | Puerto donde escucha la API |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Orígenes autorizados a llamar a la API desde un navegador |

Levantar el servidor:

```bash
cd backend
mvn spring-boot:run
```

Si la contraseña de tu usuario de PostgreSQL no es `postgres`, defínela antes de arrancar:

```powershell
# Windows (PowerShell)
$env:DB_PASSWORD = "tu_password"
mvn spring-boot:run
```

```bash
# Linux / macOS
DB_PASSWORD=tu_password mvn spring-boot:run
```

#### Alternativa para desarrollo local: el perfil `local`

Exportar la variable en cada terminal nueva es incómodo. Por eso `application.properties`
deja activo un perfil llamado `local`:

```properties
spring.profiles.active=local
```

Con ese perfil activo basta con crear el archivo
`backend/src/main/resources/application-local.properties` y escribir ahí tus valores. Spring
Boot lo carga solo, y ya no hay que exportar nada:

```properties
spring.datasource.password=tu_password
```

Ese archivo **está en `.gitignore`**: se queda en tu máquina y nunca viaja al repositorio. Es
el sitio correcto para la contraseña real.

> **Ojo con la precedencia.** Un archivo de perfil pesa **más** que `application.properties`.
> Si `application-local.properties` define `spring.datasource.password`, la variable
> `DB_PASSWORD` deja de surtir efecto: lo que queda sobrescrito es precisamente la línea
> `${DB_PASSWORD:postgres}` que la lee. Para volver a mandar con variables de entorno, borra
> esa línea del archivo local.

Si el archivo no existe —el caso de quien clona el repositorio—, no pasa nada: el perfil queda
activo pero vacío y la configuración vuelve a salir de las variables de entorno.

Verificar que responde:

```bash
curl http://localhost:8080/api/health
# {"status":"UP"}
```

### 3. Frontend

```bash
cd frontend
cp .env.example .env      # define VITE_API_URL
npm install
npm run dev
```

La aplicación queda disponible en <http://localhost:5173>. El backend debe estar corriendo:
el formulario carga los desplegables desde la API al abrirse.

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080/api` | URL base de la API |

Vite solo expone al navegador las variables con prefijo `VITE_`. El `.env` no se versiona;
`.env.example` sirve de plantilla.

#### Cómo funciona el formulario

- **Desplegables en cascada**: al elegir país se cargan sus departamentos, y al elegir
  departamento sus ciudades. Cambiar una selección limpia las de abajo, para que no quede
  seleccionada una ciudad que ya no pertenece a lo que se ve en pantalla.
- **Qué se envía**: solo los ocho campos de `ClienteRegistroDTO`. `paisId` y
  `departamentoId` se quedan en el frontend — el backend los deduce desde la ciudad.
- **Validación en dos capas**: el navegador valida primero para dar respuesta inmediata, pero
  el backend sigue siendo la autoridad. Ambas usan la misma forma de error (`{ campo: mensaje }`),
  así que los mensajes del 400 se pintan bajo el campo correspondiente sin traducción.
- **Errores**: 409 muestra el mensaje de cliente duplicado; un fallo de red o del servidor
  muestra un mensaje genérico sin romper la aplicación.

### 4. Probar el formulario de punta a punta

Con el backend en `localhost:8080` y el frontend en `localhost:5173`, abre
<http://localhost:5173> y sigue este guion. Cada paso dice qué se debe ver.

| # | Acción | Resultado esperado |
|---|---|---|
| 1 | Abrir la página | Los desplegables muestran `Cargando…` un instante y luego se pueblan: 5 tipos de documento y 6 marcas. Departamento y ciudad salen deshabilitados |
| 2 | Elegir **País → Colombia** | Departamento se habilita con 32 opciones |
| 3 | Elegir **Departamento → Antioquia** | Ciudad se habilita con 7 opciones (Apartadó, Bello, Envigado, Itagüí, Medellín, Rionegro, Turbo) |
| 4 | Elegir **Ciudad → Medellín** y luego cambiar el país | Departamento y ciudad se **vacían**: no puede quedar una ciudad que no corresponda a la selección actual |
| 5 | Pulsar **Inscribirme** con el formulario vacío | Un mensaje de error bajo cada campo. **No se llama al servidor** |
| 6 | En el número de documento, con tipo **CC**, escribir `12ab34` | En el campo queda `1234`: las letras se descartan al escribir y al pegar |
| 7 | Cambiar el tipo a **PA** (Pasaporte) y escribir `AR123456` | Se acepta tal cual: el pasaporte sí admite letras |
| 8 | Completar todo y enviar | Mensaje verde de confirmación con el nombre y la marca, y el formulario se limpia |
| 9 | Repetir el envío con **el mismo documento y la misma marca** | Mensaje de conflicto bajo el campo del documento: *ya está registrado en esa marca* |
| 10 | Cambiar solo la marca y enviar de nuevo | Se registra: una persona puede estar en varias marcas, pero no dos veces en la misma |
| 11 | Detener el backend y enviar | Mensaje genérico de conexión. La aplicación no se rompe |

Comprobar en la base de datos lo que se registró:

```bash
psql -U postgres -d fidelidad_db -c "SELECT cl.id, ti.codigo, cl.numero_identificacion, cl.nombres, c.nombre AS ciudad, m.nombre AS marca FROM cliente cl JOIN tipo_identificacion ti ON ti.id = cl.tipo_identificacion_id JOIN ciudad c ON c.id = cl.ciudad_id JOIN marca m ON m.id = cl.marca_id;"
```

Para dejar la base como estaba:

```bash
psql -U postgres -d fidelidad_db -c "DELETE FROM cliente;"
```

---

## API REST

Todos los endpoints cuelgan de `http://localhost:8080/api`.

### Documentación interactiva (Swagger UI)

Con el backend levantado, la API se puede explorar y **probar desde el navegador**, sin `curl`
ni Postman:

<http://localhost:8080/swagger-ui.html>

Cada endpoint trae su descripción, el esquema de los datos que recibe y devuelve, y los códigos
de estado posibles. El botón **Try it out** ejecuta la petición de verdad contra el backend y
muestra la respuesta, además del comando `curl` equivalente.

La especificación en bruto, por si se quiere usar con otra herramienta:

```
http://localhost:8080/v3/api-docs
```

> La documentación **se genera sola** a partir de los controladores y los DTO cada vez que la
> aplicación arranca. No es un documento aparte que haya que mantener al día: si cambia un
> endpoint o se añade un campo, la página cambia con él y no puede quedar desactualizada.

### Catálogos (alimentan los desplegables)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/paises` | Países |
| `GET` | `/api/departamentos?paisId=1` | Departamentos; sin el parámetro devuelve todos |
| `GET` | `/api/ciudades?departamentoId=2` | Ciudades; sin el parámetro devuelve todas |
| `GET` | `/api/tipos-identificacion` | CC, CE, TI, PA, NIT |
| `GET` | `/api/marcas` | Las 6 marcas del programa |
| `GET` | `/api/health` | Diagnóstico: `{"status":"UP"}` |

Los desplegables van **en cascada**: al elegir un país se piden sus departamentos, y al
elegir un departamento sus ciudades.

### Clientes

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/clientes` | Registra un cliente. Devuelve `201` con la cabecera `Location` |
| `GET` | `/api/clientes/{id}` | Consulta un cliente |

**Petición de registro:**

```json
{
  "tipoIdentificacionId": 1,
  "numeroIdentificacion": "1098765432",
  "nombres": "Andrés Felipe",
  "apellidos": "Villa Gómez",
  "fechaNacimiento": "1998-03-15",
  "direccion": "Carrera 43A # 5-15, Apto 802",
  "ciudadId": 2,
  "marcaId": 3
}
```

**Respuesta `201 Created`** — fíjate en que `departamento` y `pais` vienen resueltos aunque
no sean campos de la tabla `cliente`: se obtienen navegando desde la ciudad.

```json
{
  "id": 4,
  "tipoIdentificacion": { "id": 1, "codigo": "CC", "nombre": "Cédula de Ciudadanía" },
  "numeroIdentificacion": "1098765432",
  "nombres": "Andrés Felipe",
  "apellidos": "Villa Gómez",
  "fechaNacimiento": "1998-03-15",
  "direccion": "Carrera 43A # 5-15, Apto 802",
  "ciudad": { "id": 2, "nombre": "Medellín" },
  "departamento": { "id": 2, "nombre": "Antioquia" },
  "pais": { "id": 1, "nombre": "Colombia" },
  "marca": { "id": 3, "nombre": "Chevignon" },
  "fechaRegistro": "2026-09-05T23:55:53.500322"
}
```

### Formato de los errores

Todos los errores comparten la misma forma, definida en `ErrorResponseDTO` y producida por
`GlobalExceptionHandler`. El campo `errores` solo aparece en los fallos de validación.

| Código | Cuándo ocurre |
|---|---|
| `400 Bad Request` | Falla alguna validación del formulario |
| `404 Not Found` | Un `tipoIdentificacionId`, `ciudadId`, `marcaId` o cliente no existe |
| `409 Conflict` | Ese documento ya está inscrito **en la marca elegida** |
| `500 Internal Server Error` | Error inesperado; la traza va al log, nunca a la respuesta |

**`400` con detalle por campo:**

```json
{
  "timestamp": "2026-09-05T23:56:11.4071262",
  "status": 400,
  "error": "Bad Request",
  "message": "Hay errores de validación en los datos enviados",
  "errores": {
    "nombres": "Los nombres son obligatorios",
    "tipoIdentificacionId": "El tipo de identificación es obligatorio",
    "fechaNacimiento": "La fecha de nacimiento debe ser anterior a hoy"
  }
}
```

**`409` por documento ya inscrito en esa marca** — incluye `errores` igual que un 400, para
que el frontend pueda pintar el mensaje bajo el campo del documento:

```json
{
  "timestamp": "2026-09-06T01:12:06.7005255",
  "status": 409,
  "error": "Conflict",
  "message": "El documento CC 1122334455 ya está registrado en Chevignon",
  "errores": {
    "numeroIdentificacion": "El documento CC 1122334455 ya está registrado en Chevignon"
  }
}
```

### Regla de unicidad: una inscripción por marca

Una misma persona **puede** inscribirse en varias marcas del grupo, pero **no dos veces en la
misma marca**. Por eso la restricción es sobre los tres campos
`(tipo_identificacion_id, numero_identificacion, marca_id)` y no solo sobre el documento.

El tipo entra en la combinación porque un mismo número puede repetirse entre tipos distintos:
una CC y un NIT pueden coincidir en dígitos y pertenecer a titulares diferentes.

> Si vienes de una versión anterior del proyecto, `data.sql` borra la restricción antigua
> `uk_cliente_identificacion` al arrancar. Hibernate con `ddl-auto=update` crea la nueva pero
> nunca elimina la vieja, y esta habría seguido bloqueando el registro en una segunda marca.
> En un proyecto real ese cambio viviría en una migración de Flyway, no en el archivo de datos.

### Formato del número de identificación

Qué caracteres admite el número depende del **tipo de documento**:

| Tipo | Formato | Ejemplo válido |
|---|---|---|
| `CC` Cédula de Ciudadanía | Solo dígitos | `1098765432` |
| `CE` Cédula de Extranjería | Solo dígitos | `456789` |
| `TI` Tarjeta de Identidad | Solo dígitos | `1012345678` |
| `NIT` | Solo dígitos | `900123456` |
| `PA` Pasaporte | Letras y dígitos | `AR123456` |

Ningún tipo admite espacios ni símbolos: ni puntos, ni guiones, ni barras. Un `79.123.456-X`
pegado en el campo se convierte en `79123456`.

#### Por qué esta regla no es una anotación del DTO

Las anotaciones de Bean Validation solo pueden mirar **el campo que decoran**, y aquí el
formato válido depende de *otro* campo: el tipo elegido. Además `ClienteRegistroDTO` recibe el
**id** del tipo, no su código, así que hay que resolverlo contra la base de datos antes de
saber si es un pasaporte.

Por eso la validación quedó repartida en dos sitios:

| Regla | Dónde vive | Motivo |
|---|---|---|
| Sin espacios ni símbolos | `@Pattern` en `ClienteRegistroDTO` | Vale para todos los tipos; se decide mirando solo ese campo |
| Solo dígitos salvo pasaporte | `ClienteService.validarFormatoDelNumero` | Depende del tipo, que hay que consultar en la base |

Para que el frontend no tenga que distinguir de dónde vino cada error, el servicio lanza
`DatoInvalidoException`, que lleva el nombre del campo culpable. El `GlobalExceptionHandler` la
traduce a un **400 con el mismo formato** que un fallo de las anotaciones —incluido el mapa
`errores`—, así que el formulario pinta el mensaje bajo el campo exactamente igual:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Hay errores de validación en los datos enviados",
  "errores": {
    "numeroIdentificacion": "El número de CC solo puede contener dígitos"
  }
}
```

#### En el formulario

El campo filtra el valor mientras se escribe **y al pegar**, de modo que el usuario nunca llega
a ver un carácter que no se acepta. Se hace en el manejador del cambio y no con un atributo
`pattern` del HTML, porque `pattern` no impide pegar: solo marcaría el campo como inválido
después.

Al cambiar el tipo de documento, el número ya escrito se vuelve a filtrar. Si alguien teclea un
pasaporte `AR123456` y luego pasa a Cédula, el campo queda en `123456`: esas letras dejaron de
ser válidas y, de conservarse, el servidor rechazaría el envío por un dato que el formulario
mostraba como correcto.

> El código `PA` está escrito como constante en dos sitios, `ClienteService` y `validacion.js`.
> Es el precio de tener la misma regla en las dos capas, inevitable sin compartir código entre
> Java y JavaScript: al añadir otro tipo de documento con letras hay que tocar los dos.

### Organización de los controladores

Hay **un controlador por recurso**, cada uno con su prefijo declarado a nivel de clase:

| Clase | Prefijo |
|---|---|
| `PaisController` | `/api/paises` |
| `DepartamentoController` | `/api/departamentos` |
| `CiudadController` | `/api/ciudades` |
| `TipoIdentificacionController` | `/api/tipos-identificacion` |
| `MarcaController` | `/api/marcas` |
| `ClienteController` | `/api/clientes` |
| `HealthController` | `/api/health` |

Ninguno lleva `try/catch`: el `GlobalExceptionHandler` de la capa `validation` traduce las
excepciones al código HTTP correspondiente para toda la API de una sola vez.

### CORS

El navegador bloquea las peticiones entre orígenes distintos, y `http://localhost:5173`
(React con Vite) y `http://localhost:8080` (la API) lo son. `config/CorsConfig` autoriza el
origen del frontend sobre `/api/**`.

Se comprueba con un *preflight*:

```bash
# Origen autorizado -> 200 con las cabeceras Access-Control-*
curl -i -X OPTIONS http://localhost:8080/api/clientes \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST"

# Origen no autorizado -> 403 "Invalid CORS request"
curl -i -X OPTIONS http://localhost:8080/api/clientes \
  -H "Origin: http://otro-sitio.example" \
  -H "Access-Control-Request-Method: POST"
```

Para autorizar otro origen (por ejemplo al desplegar el frontend), se define la variable de
entorno `CORS_ALLOWED_ORIGINS`; admite varios separados por coma.

> Con `curl` las llamadas funcionan aunque CORS no esté configurado: la restricción la impone
> el navegador, no el servidor.

### Arquitectura de la petición

```
HTTP  ->  controller  ->  service  ->  repository  ->  PostgreSQL
             │              │
             │              └─ reglas de negocio (existe?, duplicado?) -> 404 / 409
             └─ @Valid sobre el DTO (formato de los campos)            -> 400
                        ▲
          GlobalExceptionHandler (@RestControllerAdvice) traduce
          cada excepción al código HTTP, una sola vez para toda la API
```

---

## Pruebas automatizadas

```bash
cd backend
mvn test
```

**No hace falta PostgreSQL para ejecutarlas.** Las pruebas corren sobre el perfil `test`, que
sustituye la base por **H2 en memoria**: se crea un esquema limpio al empezar y se destruye al
terminar. Por eso `mvn package` funciona en una máquina recién clonada, sin configurar nada.

Son 23 pruebas repartidas en tres niveles, cada uno con un propósito distinto:

| Clase | Tipo | Qué comprueba |
|---|---|---|
| `ClienteServiceTest` | Unitaria, con Mockito | Las reglas de negocio, sin base de datos |
| `ClienteControllerTest` | `@WebMvcTest` | La traducción entre HTTP y el servicio: 201, 400, 404, 409 |
| `ClienteRepositoryTest` | `@DataJpaTest` sobre H2 | Las consultas derivadas y la restricción de unicidad |
| `ProgramaFidelidadApplicationTests` | `@SpringBootTest` | Que el contexto de Spring arranca completo |

### Qué reglas quedan protegidas

Las pruebas cubren las decisiones de negocio que no son evidentes leyendo el código:

- Un documento **no puede repetirse dentro de la misma marca**, pero **sí puede aparecer en
  varias marcas** del grupo. Esta es la razón de que la restricción sea
  `(tipo, número, marca)` y no solo `(tipo, número)`.
- El **pasaporte admite letras**; la cédula y el NIT solo dígitos. El mensaje de error usa el
  código corto del documento (`NIT`), no su nombre completo.
- La restricción de unicidad **existe en la base**, no solo en el servicio: es la última
  defensa si dos peticiones simultáneas superan a la vez la comprobación previa.
- `findByTipoIdentificacionAndNumeroIdentificacion` devuelve `List` y no `Optional`,
  precisamente porque una persona puede estar inscrita en varias marcas.

---

## Comandos útiles

| Acción | Comando |
|---|---|
| Compilar y empaquetar el backend | `cd backend && mvn clean package` |
| Empaquetar sin ejecutar los tests | `cd backend && mvn clean package -DskipTests` |
| Ejecutar los tests del backend | `cd backend && mvn test` |
| Build de producción del frontend | `cd frontend && npm run build` |

> El test `contextLoads` abre una conexión real a PostgreSQL, así que necesita la base
> de datos creada y las credenciales correctas.

---

## Publicar en GitHub

El proyecto ya está publicado en
<https://github.com/felipevilla-dev/programa-fidelidad-formulario>, sobre la rama `main`.

Los pasos quedan documentados por si hay que repetir la publicación en otro entorno o partiendo
de un repositorio nuevo.

**1. Crear el repositorio remoto** en <https://github.com/new>, **vacío**: sin README, sin
`.gitignore` y sin licencia. Si GitHub crea archivos, el primer `push` chocará con el historial
local.

**2. Conectarlo y subir** (sustituye `TU-USUARIO` y el nombre del repositorio):

```bash
git remote add origin https://github.com/TU-USUARIO/programa-fidelidad.git
git push -u origin main
```

Con la CLI de GitHub, los dos pasos en uno:

```bash
gh repo create programa-fidelidad --private --source=. --remote=origin --push
```

**3. Comprobar** que subió lo que debía:

```bash
git remote -v          # el remoto apunta a tu repositorio
git log --oneline -5   # los commits que quedaron arriba
```

### Qué no se sube

El `.gitignore` deja fuera lo que no debe viajar en el repositorio:

| Excluido | Motivo |
|---|---|
| `backend/target/` | Artefactos de compilación; se regeneran con `mvn package` |
| `frontend/node_modules/` | Dependencias; se reinstalan con `npm install` |
| `frontend/dist/` | Build de producción; se regenera con `npm run build` |
| `.env` | Puede contener configuración local. Se versiona `.env.example` como plantilla |
| `.idea/`, `.vscode/`, `*.iml` | Configuración personal del editor |

**Sobre las credenciales:** ninguna contraseña real está en el repositorio. `application.properties`
toma todos los valores de variables de entorno (`${VARIABLE:valor_por_defecto}`), y los valores
por defecto son los convencionales de desarrollo local, no secretos. Se puede comprobar en
cualquier momento:

```bash
git grep -n "password" -- backend/src/main/resources/
```

---

## Estado del proyecto

- [x] **Etapa 1** — Setup: repositorio Git, backend Spring Boot por capas, conexión a
      PostgreSQL, endpoint `/api/health` y frontend React con Vite.
- [x] **Etapa 2** — Entidades JPA, repositorios JpaRepository y datos semilla de los
      catálogos (países, departamentos, ciudades, tipos de identificación y marcas).
- [x] **Etapa 3** — DTOs, servicios, validaciones de campo y de negocio, manejo global de
      errores y endpoints REST del formulario.
- [x] **Etapa 4** — Un controlador REST por recurso y configuración de CORS para el frontend.
- [x] **Etapa 5** — Formulario en React consumiendo la API, con desplegables en cascada,
      validación en el cliente y mapeo de los errores del backend.
- [x] **Etapa 6** — Estados de carga, dump de la base de datos, plantilla de configuración,
      README con las instrucciones de instalación y publicación del repositorio en GitHub.
