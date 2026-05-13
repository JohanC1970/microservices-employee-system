/**
 * Pruebas unitarias para el servicio de notificaciones.
 * Testea los endpoints HTTP reales de src/index.js con dependencias mockeadas.
 */

// ─── Mocks DEBEN declararse ANTES de cualquier require ───────────────────────

jest.mock('amqplib', () => ({
  connect: jest.fn().mockRejectedValue(new Error('RabbitMQ no disponible en tests'))
}));

jest.mock('winston', () => ({
  createLogger: jest.fn().mockReturnValue({
    info: jest.fn(),
    error: jest.fn(),
    warn: jest.fn()
  }),
  format: {
    combine: jest.fn().mockReturnValue({}),
    timestamp: jest.fn().mockReturnValue({}),
    errors: jest.fn().mockReturnValue({}),
    json: jest.fn().mockReturnValue({}),
    colorize: jest.fn().mockReturnValue({}),
    printf: jest.fn().mockReturnValue({})
  },
  transports: { Console: jest.fn() }
}));

jest.mock('swagger-jsdoc', () => jest.fn().mockReturnValue({}));
jest.mock('swagger-ui-express', () => ({
  serve: [],
  setup: jest.fn().mockReturnValue((req, res, next) => next())
}));

// Mock de pg — query configurable por test
jest.mock('pg', () => {
  const mockFn = jest.fn().mockResolvedValue({ rows: [] });
  const MockPool = jest.fn().mockReturnValue({ query: mockFn });
  MockPool.__query = mockFn;
  return { Pool: MockPool };
});

// ─── Imports ─────────────────────────────────────────────────────────────────

const request = require('supertest');
const { Pool } = require('pg');
const mockQuery = Pool.__query;
const app = require('../src/index');

// ─── Tests ───────────────────────────────────────────────────────────────────

describe('Notificaciones Service — API Tests', () => {

  beforeEach(() => {
    jest.clearAllMocks();
    mockQuery.mockResolvedValue({ rows: [] });
  });

  // ─── Health ────────────────────────────────────────────────────────────────

  describe('GET /health', () => {
    it('debe retornar estado healthy', async () => {
      const res = await request(app).get('/health');
      expect(res.status).toBe(200);
      expect(res.body.status).toBe('healthy');
      expect(res.body.service).toBe('notificaciones-service');
    });

    it('debe incluir versión del servicio', async () => {
      const res = await request(app).get('/health');
      expect(res.body).toHaveProperty('version');
    });
  });

  // ─── GET /notificaciones ───────────────────────────────────────────────────

  describe('GET /notificaciones', () => {
    it('debe retornar lista vacía cuando no hay notificaciones', async () => {
      mockQuery.mockResolvedValueOnce({ rows: [] });
      const res = await request(app).get('/notificaciones');
      expect(res.status).toBe(200);
      expect(Array.isArray(res.body)).toBe(true);
      expect(res.body).toHaveLength(0);
    });

    it('debe retornar lista de notificaciones correctamente mapeadas', async () => {
      const fecha = new Date().toISOString();
      mockQuery.mockResolvedValueOnce({
        rows: [
          {
            id: 'uuid-1',
            tipo: 'BIENVENIDA',
            destinatario: 'juan@empresa.com',
            mensaje: 'Bienvenido al equipo',
            fecha_envio: fecha,
            empleado_id: 'EMP-001'
          }
        ]
      });

      const res = await request(app).get('/notificaciones');
      expect(res.status).toBe(200);
      expect(res.body).toHaveLength(1);
      expect(res.body[0].id).toBe('uuid-1');
      expect(res.body[0].tipo).toBe('BIENVENIDA');
      expect(res.body[0].destinatario).toBe('juan@empresa.com');
      expect(res.body[0].mensaje).toBe('Bienvenido al equipo');
      expect(res.body[0].empleadoId).toBe('EMP-001');
    });

    it('debe retornar múltiples notificaciones', async () => {
      mockQuery.mockResolvedValueOnce({
        rows: [
          { id: 'u1', tipo: 'BIENVENIDA', destinatario: 'a@b.com', mensaje: 'msg1', fecha_envio: new Date(), empleado_id: 'E1' },
          { id: 'u2', tipo: 'SEGURIDAD', destinatario: 'b@b.com', mensaje: 'msg2', fecha_envio: new Date(), empleado_id: 'E2' },
        ]
      });

      const res = await request(app).get('/notificaciones');
      expect(res.status).toBe(200);
      expect(res.body).toHaveLength(2);
    });

    it('debe retornar 500 si la base de datos falla', async () => {
      mockQuery.mockRejectedValueOnce(new Error('Connection refused'));
      const res = await request(app).get('/notificaciones');
      expect(res.status).toBe(500);
      expect(res.body).toHaveProperty('error');
    });
  });

  // ─── GET /notificaciones/:empleadoId ───────────────────────────────────────

  describe('GET /notificaciones/:empleadoId', () => {
    it('debe retornar notificaciones de un empleado específico', async () => {
      const fecha = new Date().toISOString();
      mockQuery.mockResolvedValueOnce({
        rows: [
          {
            id: 'uuid-3',
            tipo: 'ACTUALIZACION',
            destinatario: 'emp@empresa.com',
            mensaje: 'Datos actualizados',
            fecha_envio: fecha,
            empleado_id: 'EMP-042'
          }
        ]
      });

      const res = await request(app).get('/notificaciones/EMP-042');
      expect(res.status).toBe(200);
      expect(res.body).toHaveLength(1);
      expect(res.body[0].empleadoId).toBe('EMP-042');
      expect(res.body[0].tipo).toBe('ACTUALIZACION');
    });

    it('debe retornar lista vacía si el empleado no tiene notificaciones', async () => {
      mockQuery.mockResolvedValueOnce({ rows: [] });
      const res = await request(app).get('/notificaciones/EMP-NO-EXISTE');
      expect(res.status).toBe(200);
      expect(Array.isArray(res.body)).toBe(true);
      expect(res.body).toHaveLength(0);
    });

    it('debe retornar 500 si la base de datos falla al buscar por empleado', async () => {
      mockQuery.mockRejectedValueOnce(new Error('DB timeout'));
      const res = await request(app).get('/notificaciones/EMP-001');
      expect(res.status).toBe(500);
      expect(res.body).toHaveProperty('error');
    });

    it('debe consultar la BD con el empleadoId correcto', async () => {
      mockQuery.mockResolvedValueOnce({ rows: [] });
      await request(app).get('/notificaciones/EMP-XYZ');
      expect(mockQuery).toHaveBeenCalledWith(
        expect.stringContaining('WHERE empleado_id'),
        ['EMP-XYZ']
      );
    });
  });
});
