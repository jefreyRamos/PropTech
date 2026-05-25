package proptech.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import proptech.modelo.Asesor;

public class AsesorRepository {

    private final Connection conn;
    public AsesorRepository() { this.conn = ConexionDB.getInstance().getConexion(); }

    public boolean insertar(Asesor a) {
        String sql = "INSERT INTO asesores (codigo,nombre,contacto,especialidad,zona_asignada) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,a.getCodigo()); ps.setString(2,a.getNombre());
            ps.setString(3,a.getContacto()); ps.setString(4,a.getEspecialidad());
            ps.setString(5,a.getZonaAsignada());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys(); if (rs.next()) a.setId(rs.getInt(1));
            return true;
        } catch (SQLException e) { System.err.println("[DB] insertar asesor: "+e.getMessage()); return false; }
    }

    public boolean actualizar(Asesor a) {
        String sql = """
            UPDATE asesores SET
              nombre=?,contacto=?,especialidad=?,zona_asignada=?,
              cierres_realizados=?,comision_total=?
            WHERE codigo=?""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,a.getNombre()); ps.setString(2,a.getContacto());
            ps.setString(3,a.getEspecialidad()); ps.setString(4,a.getZonaAsignada());
            ps.setInt(5,a.getCierresRealizados()); ps.setDouble(6,a.getComisionTotal());
            ps.setString(7,a.getCodigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] actualizar asesor: "+e.getMessage()); return false; }
    }

    public boolean eliminar(String codigo) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM asesores WHERE codigo=?")) {
            ps.setString(1,codigo); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] eliminar asesor: "+e.getMessage()); return false; }
    }

    public List<Asesor> listarTodos() {
        List<Asesor> lista = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery("SELECT * FROM asesores ORDER BY nombre")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("[DB] listar asesores: "+e.getMessage()); }
        return lista;
    }

    public Asesor buscarPorCodigo(String codigo) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM asesores WHERE codigo=?")) {
            ps.setString(1,codigo); ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) { System.err.println("[DB] buscar asesor: "+e.getMessage()); }
        return null;
    }

    private Asesor mapear(ResultSet rs) throws SQLException {
        Asesor a = new Asesor();
        a.setId(rs.getInt("id")); a.setCodigo(rs.getString("codigo"));
        a.setNombre(rs.getString("nombre")); a.setContacto(rs.getString("contacto"));
        a.setEspecialidad(rs.getString("especialidad")); a.setZonaAsignada(rs.getString("zona_asignada"));
        a.setCierresRealizados(rs.getInt("cierres_realizados")); a.setComisionTotal(rs.getDouble("comision_total"));
        return a;
    }
}