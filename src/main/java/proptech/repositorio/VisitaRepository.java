package proptech.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import proptech.modelo.Visita;

public class VisitaRepository {

    private final Connection conn;
    public VisitaRepository() { this.conn = ConexionDB.getInstance().getConexion(); }

    public boolean insertar(Visita v) {
        String sql = """
            INSERT INTO visitas (id_cliente,codigo_inmueble,fecha_hora,id_asesor,estado,prioridad,observaciones)
            VALUES (?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,v.getIdCliente()); ps.setString(2,v.getCodigoInmueble());
            ps.setString(3,v.getFechaHora()); ps.setString(4,v.getIdAsesor());
            ps.setString(5,v.getEstado().name()); ps.setString(6,v.getPrioridad().name());
            ps.setString(7,v.getObservaciones());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys(); if (rs.next()) v.setId(rs.getInt(1));
            return true;
        } catch (SQLException e) { System.err.println("[DB] insertar visita: "+e.getMessage()); return false; }
    }

    public boolean actualizar(Visita v) {
        String sql = """
            UPDATE visitas SET estado=?,prioridad=?,observaciones=?,fecha_hora=?,id_asesor=?
            WHERE id=?""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,v.getEstado().name()); ps.setString(2,v.getPrioridad().name());
            ps.setString(3,v.getObservaciones()); ps.setString(4,v.getFechaHora());
            ps.setString(5,v.getIdAsesor()); ps.setInt(6,v.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] actualizar visita: "+e.getMessage()); return false; }
    }

    public boolean eliminar(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM visitas WHERE id=?")) {
            ps.setInt(1,id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] eliminar visita: "+e.getMessage()); return false; }
    }

    /** Lista todas con JOIN para mostrar nombres en la tabla. */
    public List<Visita> listarTodas() {
        List<Visita> lista = new ArrayList<>();
        String sql = """
            SELECT v.*,
                   c.nombre    AS nombre_cliente,
                   i.direccion AS dir_inmueble,
                   a.nombre    AS nombre_asesor
            FROM visitas v
            LEFT JOIN clientes  c ON v.id_cliente      = c.identificacion
            LEFT JOIN inmuebles i ON v.codigo_inmueble  = i.codigo
            LEFT JOIN asesores  a ON v.id_asesor        = a.codigo
            ORDER BY v.fecha_hora DESC""";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs, true));
        } catch (SQLException e) { System.err.println("[DB] listar visitas: "+e.getMessage()); }
        return lista;
    }

    public List<Visita> listarPendientes() {
        List<Visita> lista = new ArrayList<>();
        String sql = """
            SELECT v.*,
                   c.nombre    AS nombre_cliente,
                   i.direccion AS dir_inmueble,
                   a.nombre    AS nombre_asesor
            FROM visitas v
            LEFT JOIN clientes  c ON v.id_cliente      = c.identificacion
            LEFT JOIN inmuebles i ON v.codigo_inmueble  = i.codigo
            LEFT JOIN asesores  a ON v.id_asesor        = a.codigo
            WHERE v.estado='PENDIENTE'
            ORDER BY v.prioridad DESC, v.fecha_hora ASC""";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs, true));
        } catch (SQLException e) { System.err.println("[DB] listar pendientes: "+e.getMessage()); }
        return lista;
    }

    public int contarPendientes() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM visitas WHERE estado='PENDIENTE'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[DB] contar pendientes: "+e.getMessage()); }
        return 0;
    }

    private Visita mapear(ResultSet rs, boolean conJoin) throws SQLException {
        Visita v = new Visita();
        v.setId(rs.getInt("id")); v.setIdCliente(rs.getString("id_cliente"));
        v.setCodigoInmueble(rs.getString("codigo_inmueble")); v.setFechaHora(rs.getString("fecha_hora"));
        v.setIdAsesor(rs.getString("id_asesor")); v.setEstado(Visita.Estado.valueOf(rs.getString("estado")));
        v.setPrioridad(Visita.Prioridad.valueOf(rs.getString("prioridad"))); v.setObservaciones(rs.getString("observaciones"));
        if (conJoin) {
            String nc = rs.getString("nombre_cliente"); v.setNombreCliente(nc != null ? nc : v.getIdCliente());
            String di = rs.getString("dir_inmueble");   v.setDireccionInmueble(di != null ? di : v.getCodigoInmueble());
            String na = rs.getString("nombre_asesor");  v.setNombreAsesor(na != null ? na : (v.getIdAsesor() != null ? v.getIdAsesor() : "—"));
        }
        return v;
    }
}