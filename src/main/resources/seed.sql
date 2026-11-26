-- =====================================================================
-- Datos de prueba para la demo. Se cargan justo despues de schema.sql.
--
-- Las tarjetas guardan el SHA-256 del UID normalizado (mayusculas, sin ':').
-- UIDs "impresos" en las tarjetas de demo (los usa SimuladorLectorRFID):
--   04:A3:1F:7B -> Carlos Perez    (activa)
--   04:B7:22:9C -> Ana Torres      (activa)
--   04:C1:5E:03 -> Luis Mendoza    (activa)
--   04:D9:8A:44 -> Sofia Ramirez   (activa)
--   04:E2:10:F5 -> Jorge Castillo  (BLOQUEADA, reportada como perdida)
-- =====================================================================

INSERT INTO trabajador (cedula, nombre, rol, nivel_acceso) VALUES
    ('0901234567', 'Carlos Perez',   'OBRERO',              'BASICO'),
    ('0912345678', 'Ana Torres',     'OPERADOR_MAQUINARIA', 'RESTRINGIDO'),
    ('0923456789', 'Luis Mendoza',   'ELECTRICISTA',        'CRITICO'),
    ('0934567890', 'Sofia Ramirez',  'SUPERVISOR',          'CRITICO'),
    ('0945678901', 'Jorge Castillo', 'VISITANTE',           'BASICO');

INSERT INTO zona (codigo, nombre, nivel_requerido) VALUES
    ('Z-ING', 'Ingreso principal',    'BASICO'),
    ('Z-BOD', 'Bodega de materiales', 'RESTRINGIDO'),
    ('Z-GRU', 'Area de grua torre',   'CRITICO'),
    ('Z-ELE', 'Cuarto electrico',     'CRITICO');

INSERT INTO tarjeta_rfid (uid_hash, trabajador_id, activa, fecha_emision) VALUES
    ('1fc3e9644fe319e8984c96a8bb5d03beb7afcec8ef65b79a12ffa7b582213973', (SELECT id FROM trabajador WHERE cedula = '0901234567'), TRUE,  DATE '2026-01-15'),
    ('410e3d2bde376813545b9e84b7dfd40117b6544541dc6ec052878d825b72740d', (SELECT id FROM trabajador WHERE cedula = '0912345678'), TRUE,  DATE '2026-02-01'),
    ('293347bc4f1ac2ffd3f2268feff49f687ef51fc6168039bda04d869c1b8c930d', (SELECT id FROM trabajador WHERE cedula = '0923456789'), TRUE,  DATE '2026-02-10'),
    ('3811d0b7c7aa0cefd58904ceee1c005090000575b7a057a86d4fa485daac9b29', (SELECT id FROM trabajador WHERE cedula = '0934567890'), TRUE,  DATE '2026-03-05'),
    ('9acc12d80f305abe14d6123eb8b6c8f9f208819f54997a277bf728d12cdb9b27', (SELECT id FROM trabajador WHERE cedula = '0945678901'), FALSE, DATE '2026-04-20');
