package proptech.repositorio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SINGLETON — Conexión SQLite vía JDBC.
 * Crea el esquema completo la primera vez que se ejecuta.
 * El archivo proptech.db se genera en el directorio de trabajo.
 */
public class ConexionDB {

    private static final String URL = "jdbc:sqlite:proptech.db";
    private static ConexionDB instancia;
    private Connection conn;

    private ConexionDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
            crearEsquema();
            System.out.println("[DB] Conectado → proptech.db");
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
            if (conn == null || conn.isClosed())
                conn = DriverManager.getConnection(URL);
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
                codigo             TEXT NOT NULL UNIQUE,
                nombre             TEXT NOT NULL,
                contacto           TEXT,
                especialidad       TEXT,
                zona_asignada      TEXT,
                cierres_realizados INTEGER DEFAULT 0,
                comision_total     REAL    DEFAULT 0
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS inmuebles (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo           TEXT    NOT NULL UNIQUE,
                direccion        TEXT    NOT NULL,
                ciudad           TEXT    NOT NULL,
                barrio           TEXT,
                tipo             TEXT    NOT NULL,
                finalidad        TEXT    NOT NULL,
                precio           REAL    NOT NULL,
                area             REAL    DEFAULT 0,
                habitaciones     INTEGER DEFAULT 0,
                banos            INTEGER DEFAULT 0,
                estado           TEXT    DEFAULT 'DISPONIBLE',
                disponible       INTEGER DEFAULT 1,
                codigo_asesor    TEXT,
                contador_visitas INTEGER DEFAULT 0
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS clientes (
                id                    INTEGER PRIMARY KEY AUTOINCREMENT,
                identificacion        TEXT NOT NULL UNIQUE,
                nombre                TEXT NOT NULL,
                correo                TEXT,
                telefono              TEXT,
                tipo                  TEXT NOT NULL,
                presupuesto           REAL NOT NULL,
                zona_interes          TEXT,
                tipo_inmueble_deseado TEXT,
                min_habitaciones      INTEGER DEFAULT 0,
                estado_busqueda       TEXT    DEFAULT 'ACTIVO'
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS visitas (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                id_cliente      TEXT    NOT NULL,
                codigo_inmueble TEXT    NOT NULL,
                fecha_hora      TEXT    NOT NULL,
                id_asesor       TEXT,
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
                codigo_inmueble  TEXT NOT NULL,
                id_cliente       TEXT NOT NULL,
                id_asesor        TEXT,
                tipo             TEXT NOT NULL,
                valor_acordado   REAL NOT NULL,
                comision         REAL DEFAULT 0,
                estado           TEXT DEFAULT 'EN_PROCESO',
                fecha            TEXT DEFAULT (datetime('now')),
                fecha_vencimiento TEXT
            )""");

        st.close();
    }

    public void cerrar() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}