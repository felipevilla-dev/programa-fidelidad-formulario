-- ===========================================================================
-- Datos semilla de las tablas de referencia (catálogos).
--
-- Este script se ejecuta en CADA arranque de la aplicación, después de que
-- Hibernate crea o actualiza las tablas (ver spring.jpa.defer-datasource-
-- initialization en application.properties).
--
-- Por eso cada INSERT lleva "ON CONFLICT DO NOTHING": si la fila ya existe,
-- PostgreSQL la ignora en vez de fallar por clave duplicada. El script es
-- idempotente, es decir, ejecutarlo muchas veces deja el mismo resultado que
-- ejecutarlo una sola vez.
--
-- Los id van explícitos para que las claves foráneas de este mismo archivo
-- sean estables entre ejecuciones.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- Limpieza de una restricción de una versión anterior del esquema.
--
-- La regla de negocio cambió: antes un documento se podía inscribir una sola vez
-- en todo el programa, y ahora se permite una inscripción por marca. Hibernate,
-- con ddl-auto=update, CREA la restricción nueva pero nunca BORRA la antigua, así
-- que en una base ya existente uk_cliente_identificacion seguiría bloqueando el
-- registro en una segunda marca.
--
-- IF EXISTS lo hace inofensivo en una base recién creada, donde esa restricción
-- nunca llegó a existir.
--
-- Esto es, en realidad, una migración de esquema metida en el archivo de datos:
-- es la consecuencia de usar ddl-auto en vez de una herramienta de migraciones
-- como Flyway, que versionaría este cambio en su propio archivo.
-- ---------------------------------------------------------------------------
ALTER TABLE cliente DROP CONSTRAINT IF EXISTS uk_cliente_identificacion;

-- ---------------------------------------------------------------------------
-- País
-- ---------------------------------------------------------------------------
INSERT INTO pais (id, nombre) VALUES (1, 'Colombia') ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Departamentos: los 32 departamentos oficiales de Colombia.
--
-- Nota: Bogotá es constitucionalmente un Distrito Capital y no pertenece a
-- Cundinamarca desde 1954. Aquí se registra como ciudad de Cundinamarca por
-- decisión de diseño del proyecto, para mantener uniforme la jerarquía
-- País -> Departamento -> Ciudad que necesitan los desplegables en cascada.
-- ---------------------------------------------------------------------------
INSERT INTO departamento (id, nombre, pais_id) VALUES
    (1,  'Amazonas',                  1),
    (2,  'Antioquia',                 1),
    (3,  'Arauca',                    1),
    (4,  'Atlántico',                 1),
    (5,  'Bolívar',                   1),
    (6,  'Boyacá',                    1),
    (7,  'Caldas',                    1),
    (8,  'Caquetá',                   1),
    (9,  'Casanare',                  1),
    (10, 'Cauca',                     1),
    (11, 'Cesar',                     1),
    (12, 'Chocó',                     1),
    (13, 'Córdoba',                   1),
    (14, 'Cundinamarca',              1),
    (15, 'Guainía',                   1),
    (16, 'Guaviare',                  1),
    (17, 'Huila',                     1),
    (18, 'La Guajira',                1),
    (19, 'Magdalena',                 1),
    (20, 'Meta',                      1),
    (21, 'Nariño',                    1),
    (22, 'Norte de Santander',        1),
    (23, 'Putumayo',                  1),
    (24, 'Quindío',                   1),
    (25, 'Risaralda',                 1),
    (26, 'San Andrés y Providencia',  1),
    (27, 'Santander',                 1),
    (28, 'Sucre',                     1),
    (29, 'Tolima',                    1),
    (30, 'Valle del Cauca',           1),
    (31, 'Vaupés',                    1),
    (32, 'Vichada',                   1)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Ciudades: la capital de cada uno de los 32 departamentos (id 1 a 32) y las
-- principales ciudades de los departamentos más poblados (id 33 a 76).
-- ---------------------------------------------------------------------------
INSERT INTO ciudad (id, nombre, departamento_id) VALUES
    -- Capitales departamentales
    (1,  'Leticia',                1),
    (2,  'Medellín',               2),
    (3,  'Arauca',                 3),
    (4,  'Barranquilla',           4),
    (5,  'Cartagena',              5),
    (6,  'Tunja',                  6),
    (7,  'Manizales',              7),
    (8,  'Florencia',              8),
    (9,  'Yopal',                  9),
    (10, 'Popayán',               10),
    (11, 'Valledupar',            11),
    (12, 'Quibdó',                12),
    (13, 'Montería',              13),
    (14, 'Bogotá D.C.',           14),
    (15, 'Inírida',               15),
    (16, 'San José del Guaviare', 16),
    (17, 'Neiva',                 17),
    (18, 'Riohacha',              18),
    (19, 'Santa Marta',           19),
    (20, 'Villavicencio',         20),
    (21, 'Pasto',                 21),
    (22, 'Cúcuta',                22),
    (23, 'Mocoa',                 23),
    (24, 'Armenia',               24),
    (25, 'Pereira',               25),
    (26, 'San Andrés',            26),
    (27, 'Bucaramanga',           27),
    (28, 'Sincelejo',             28),
    (29, 'Ibagué',                29),
    (30, 'Cali',                  30),
    (31, 'Mitú',                  31),
    (32, 'Puerto Carreño',        32),
    -- Antioquia
    (33, 'Bello',                  2),
    (34, 'Itagüí',                 2),
    (35, 'Envigado',               2),
    (36, 'Rionegro',               2),
    (37, 'Apartadó',               2),
    (38, 'Turbo',                  2),
    -- Valle del Cauca
    (39, 'Palmira',               30),
    (40, 'Buenaventura',          30),
    (41, 'Tuluá',                 30),
    (42, 'Cartago',               30),
    (43, 'Buga',                  30),
    (44, 'Jamundí',               30),
    -- Cundinamarca
    (45, 'Soacha',                14),
    (46, 'Zipaquirá',             14),
    (47, 'Facatativá',            14),
    (48, 'Chía',                  14),
    (49, 'Fusagasugá',            14),
    (50, 'Girardot',              14),
    -- Atlántico
    (51, 'Soledad',                4),
    (52, 'Malambo',                4),
    (53, 'Sabanalarga',            4),
    -- Santander
    (54, 'Floridablanca',         27),
    (55, 'Girón',                 27),
    (56, 'Piedecuesta',           27),
    (57, 'Barrancabermeja',       27),
    -- Bolívar
    (58, 'Magangué',               5),
    (59, 'Turbaco',                5),
    -- Norte de Santander
    (60, 'Ocaña',                 22),
    (61, 'Villa del Rosario',     22),
    (62, 'Los Patios',            22),
    -- Tolima
    (63, 'Espinal',               29),
    (64, 'Melgar',                29),
    -- Nariño
    (65, 'Ipiales',               21),
    (66, 'Tumaco',                21),
    -- Córdoba
    (67, 'Lorica',                13),
    (68, 'Sahagún',               13),
    -- Caldas
    (69, 'La Dorada',              7),
    (70, 'Chinchiná',              7),
    -- Risaralda
    (71, 'Dosquebradas',          25),
    (72, 'Santa Rosa de Cabal',   25),
    -- Huila
    (73, 'Pitalito',              17),
    (74, 'Garzón',                17),
    -- Meta
    (75, 'Acacías',               20),
    (76, 'Granada',               20)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Tipos de identificación
-- ---------------------------------------------------------------------------
INSERT INTO tipo_identificacion (id, codigo, nombre) VALUES
    (1, 'CC',  'Cédula de Ciudadanía'),
    (2, 'CE',  'Cédula de Extranjería'),
    (3, 'TI',  'Tarjeta de Identidad'),
    (4, 'PA',  'Pasaporte'),
    (5, 'NIT', 'Número de Identificación Tributaria')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Marcas del programa de fidelidad
-- ---------------------------------------------------------------------------
INSERT INTO marca (id, nombre) VALUES
    (1, 'Americanino'),
    (2, 'American Eagle'),
    (3, 'Chevignon'),
    (4, 'Esprit'),
    (5, 'Naf Naf'),
    (6, 'Rifle')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Sincronización de las secuencias.
--
-- Al insertar los id a mano, el contador interno de cada columna identidad se
-- queda en 1. Si más adelante la aplicación inserta una fila sin id, PostgreSQL
-- intentaría usar el 1 y chocaría con una fila existente. Estas líneas dejan
-- cada secuencia apuntando al mayor id ya utilizado.
-- ---------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('pais', 'id'),                (SELECT COALESCE(MAX(id), 1) FROM pais));
SELECT setval(pg_get_serial_sequence('departamento', 'id'),        (SELECT COALESCE(MAX(id), 1) FROM departamento));
SELECT setval(pg_get_serial_sequence('ciudad', 'id'),              (SELECT COALESCE(MAX(id), 1) FROM ciudad));
SELECT setval(pg_get_serial_sequence('tipo_identificacion', 'id'), (SELECT COALESCE(MAX(id), 1) FROM tipo_identificacion));
SELECT setval(pg_get_serial_sequence('marca', 'id'),               (SELECT COALESCE(MAX(id), 1) FROM marca));
