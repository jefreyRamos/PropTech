package proptech.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import proptech.modelo.Cliente;

public class ClienteRepository {

    private final Connection conn;
    public ClienteRepository() { this.conn = ConexionDB.getInstance().getConexion(); }

    public boolean insertar(Cliente c) {
        String sql = """
            INSERT INTO clientes
              (identificacion,nombre,correo,telefono,tipo,presupuesto,
               zona_interes,tipo_inmueble_deseado,min_habitaciones)
            VALUES (?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getIdentificacion()); ps.setString(2, c.getNombre());
            ps.setString(3, c.getCorreo());         ps.setString(4, c.getTelefono());
            ps.setString(5, c.getTipo().name());    ps.setDouble(6, c.getPresupuesto());
            ps.setString(7, c.getZonaInteres());    ps.setString(8, c.getTipoInmuebleDeseado());
            ps.setInt(9, c.getMinHabitaciones());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys(); if (rs.next()) c.setId(rs.getInt(1));
            return true;
        } catch (SQLException e) { System.err.println("[DB] insertar cliente: " + e.getMessage()); return false; }
    }

    public boolean actualizar(Cliente c) {
        String sql = """
            UPDATE clientes SET
              nombre=?,correo=?,telefono=?,tipo=?,presupuesto=?,zona_interes=?,
              tipo_inmueble_deseado=?,min_habitaciones=?,estado_busqueda=?
            WHERE identificacion=?""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());   ps.setString(2, c.getCorreo());
            ps.setString(3, c.getTelefono()); ps.setString(4, c.getTipo().name());
            ps.setDouble(5, c.getPresupuesto()); ps.setString(6, c.getZonaInteres());
            ps.setString(7, c.getTipoInmuebleDeseado()); ps.setInt(8, c.getMinHabitaciones());
            ps.setString(9, c.getEstadoBusqueda().name()); ps.setString(10, c.getIdentificacion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] actualizar cliente: " + e.getMessage()); return false; }
    }

    public boolean eliminar(String id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM clientes WHERE identificacion=?")) {
            ps.setString(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] eliminar cliente: " + e.getMessage()); return false; }
    }

    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery("SELECT * FROM clientes ORDER BY nombre")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("[DB] listar clientes: " + e.getMessage()); }
        return lista;
    }

    public Cliente buscarPorId(String id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM clientes WHERE identificacion=?")) {
            ps.setString(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) { System.err.println("[DB] buscar cliente: " + e.getMessage()); }
        return null;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id")); c.setIdentificacion(rs.getString("identificacion"));
        c.setNombre(rs.getString("nombre")); c.setCorreo(rs.getString("correo"));
        c.setTelefono(rs.getString("telefono")); c.setTipo(Cliente.Tipo.valueOf(rs.getString("tipo")));
        c.setPresupuesto(rs.getDouble("presupuesto")); c.setZonaInteres(rs.getString("zona_interes"));
        c.setTipoInmuebleDeseado(rs.getString("tipo_inmueble_deseado"));
        c.setMinHabitaciones(rs.getInt("min_habitaciones"));
        c.setEstadoBusqueda(Cliente.Estado.valueOf(rs.getString("estado_busqueda")));
        return c;
    }
}