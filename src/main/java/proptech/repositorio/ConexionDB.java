package proptech.repositorio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SINGLETON — Conexión SQLite vía JDBC.
 * CORRECCIÓN: se usa ruta absoluta al directorio de trabajo del usuario
 * para garantizar que proptech.db siempre se encuentre sin importar
 * desde dónde se lanza la aplicación en VS Code / terminal.
 */
public class ConexionDB {

    private static final String DB_URL;

    static {
        // Guarda la BD junto al proyecto, en la carpeta de trabajo actual
        String dir = System.getProperty("user.dir");
        DB_URL = "jdbc:sqlite:" + dir + "/proptech.db";
        System.out.println("[DB] Ruta BD → " + dir + "/proptech.db");
    }

    private static ConexionDB instancia;
    private Connection conn;

    private ConexionDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            // Mejora rendimiento y garantiza integridad referencial
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
                st.execute("PRAGMA journal_mode = WAL");
                st.execute("PRAGMA synchronous = NORMAL");
            }
            crearEsquema();
            System.out.println("[DB] Conectado correctamente a SQLite");
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar SQLite: " + e.getMessage(), e);
        }
    }

    public static ConexionDB getInstance() {
        if (instancia == null) instancia = new ConexionDB();
        return instancia;
    }

    public Connection getConexion() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al recuperar conexión: " + e.getMessage(), e);
        }
        return conn;
    }

    private void crearEsquema() throws SQLException {
        Statement st = conn.createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS asesores (
                id                 INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo             TEXT    NOT NULL UNIQUE,
                nombre             TEXT    NOT NULL,
                contacto           TEXT    DEFAULT '',
                especialidad       TEXT    DEFAULT '',
                zona_asignada      TEXT    DEFAULT '',
                cierres_realizados INTEGER DEFAULT 0,
                comision_total     REAL    DEFAULT 0
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS inmuebles (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo           TEXT    NOT NULL UNIQUE,
                direccion        TEXT    NOT NULL,
                ciudad           TEXT    NOT NULL DEFAULT '',
                barrio           TEXT    DEFAULT '',
                tipo             TEXT    NOT NULL DEFAULT 'APARTAMENTO',
                finalidad        TEXT    NOT NULL DEFAULT 'ARRIENDO',
                precio           REAL    NOT NULL DEFAULT 0,
                area             REAL    DEFAULT 0,
                habitaciones     INTEGER DEFAULT 0,
                banos            INTEGER DEFAULT 0,
                estado           TEXT    DEFAULT 'DISPONIBLE',
                disponible       INTEGER DEFAULT 1,
                codigo_asesor    TEXT    DEFAULT '',
                contador_visitas INTEGER DEFAULT 0
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS clientes (
                id                    INTEGER PRIMARY KEY AUTOINCREMENT,
                identificacion        TEXT    NOT NULL UNIQUE,
                nombre                TEXT    NOT NULL,
                correo                TEXT    DEFAULT '',
                telefono              TEXT    DEFAULT '',
                tipo                  TEXT    NOT NULL DEFAULT 'COMPRADOR',
                presupuesto           REAL    NOT NULL DEFAULT 0,
                zona_interes          TEXT    DEFAULT '',
                tipo_inmueble_deseado TEXT    DEFAULT '',
                min_habitaciones      INTEGER DEFAULT 0,
                estado_busqueda       TEXT    DEFAULT 'ACTIVO'
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS visitas (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                id_cliente      TEXT    NOT NULL DEFAULT '',
                codigo_inmueble TEXT    NOT NULL DEFAULT '',
                fecha_hora      TEXT    NOT NULL DEFAULT '',
                id_asesor       TEXT    DEFAULT '',
                estado          TEXT    DEFAULT 'PENDIENTE',
                prioridad       TEXT    DEFAULT 'NORMAL',
                observaciones   TEXT    DEFAULT ''
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS favoritos (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                id_cliente      TEXT NOT NULL,
                codigo_inmueble TEXT NOT NULL,
                UNIQUE(id_cliente, codigo_inmueble)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS operaciones (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo_inmueble  TEXT NOT NULL DEFAULT '',
                id_cliente       TEXT NOT NULL DEFAULT '',
                id_asesor        TEXT DEFAULT '',
                tipo             TEXT NOT NULL DEFAULT 'VENTA',
                valor_acordado   REAL NOT NULL DEFAULT 0,
                comision         REAL DEFAULT 0,
                estado           TEXT DEFAULT 'EN_PROCESO',
                fecha            TEXT DEFAULT (datetime('now')),
                fecha_vencimiento TEXT DEFAULT ''
            )""");

        st.close();
        System.out.println("[DB] Esquema verificado/creado correctamente");
    }

    public void cerrar() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("[DB] Conexión cerrada.");
            }
        } catch (SQLException ignored) {}
    }
}