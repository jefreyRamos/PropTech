package proptech.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import proptech.modelo.Inmueble;

public class InmuebleRepository {

    private final Connection conn;
    public InmuebleRepository() { this.conn = ConexionDB.getInstance().getConexion(); }

    // ── INSERT ─────────────────────────────────────────────────────────────
    public boolean insertar(Inmueble i) {
        String sql = """
            INSERT INTO inmuebles
              (codigo,direccion,ciudad,barrio,tipo,finalidad,precio,area,
               habitaciones,banos,estado,disponible,codigo_asesor)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,  i.getCodigo());
            ps.setString(2,  i.getDireccion());
            ps.setString(3,  i.getCiudad());
            ps.setString(4,  i.getBarrio());
            ps.setString(5,  i.getTipo().name());
            ps.setString(6,  i.getFinalidad().name());
            ps.setDouble(7,  i.getPrecio());
            ps.setDouble(8,  i.getArea());
            ps.setInt(9,     i.getHabitaciones());
            ps.setInt(10,    i.getBanos());
            ps.setString(11, i.getEstado().name());
            ps.setInt(12,    i.isDisponible() ? 1 : 0);
            ps.setString(13, i.getCodigoAsesor());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) i.setId(rs.getInt(1));
            return true;
        } catch (SQLException e) { System.err.println("[DB] insertar inmueble: " + e.getMessage()); return false; }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    public boolean actualizar(Inmueble i) {
        String sql = """
            UPDATE inmuebles SET
              direccion=?,ciudad=?,barrio=?,tipo=?,finalidad=?,precio=?,area=?,
              habitaciones=?,banos=?,estado=?,disponible=?,codigo_asesor=?,contador_visitas=?
            WHERE codigo=?""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  i.getDireccion());
            ps.setString(2,  i.getCiudad());
            ps.setString(3,  i.getBarrio());
            ps.setString(4,  i.getTipo().name());
            ps.setString(5,  i.getFinalidad().name());
            ps.setDouble(6,  i.getPrecio());
            ps.setDouble(7,  i.getArea());
            ps.setInt(8,     i.getHabitaciones());
            ps.setInt(9,     i.getBanos());
            ps.setString(10, i.getEstado().name());
            ps.setInt(11,    i.isDisponible() ? 1 : 0);
            ps.setString(12, i.getCodigoAsesor());
            ps.setInt(13,    i.getContadorVisitas());
            ps.setString(14, i.getCodigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] actualizar inmueble: " + e.getMessage()); return false; }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    public boolean eliminar(String codigo) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM inmuebles WHERE codigo=?")) {
            ps.setString(1, codigo); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] eliminar inmueble: " + e.getMessage()); return false; }
    }

    // ── SELECT ALL ─────────────────────────────────────────────────────────
    public List<Inmueble> listarTodos() {
        List<Inmueble> lista = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery("SELECT * FROM inmuebles ORDER BY precio ASC")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("[DB] listar inmuebles: " + e.getMessage()); }
        return lista;
    }

    // ── SELECT BY CÓDIGO ───────────────────────────────────────────────────
    public Inmueble buscarPorCodigo(String codigo) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM inmuebles WHERE codigo=?")) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) { System.err.println("[DB] buscar inmueble: " + e.getMessage()); }
        return null;
    }

    // ── FILTRO COMBINADO ───────────────────────────────────────────────────
    public List<Inmueble> buscarConFiltros(String tipo, String finalidad,
                                            double precioMax, int minHab, String ciudad) {
        List<Inmueble> lista   = new ArrayList<>();
        StringBuilder  sql    = new StringBuilder("SELECT * FROM inmuebles WHERE 1=1");
        List<Object>   params = new ArrayList<>();

        if (tipo     != null && !tipo.isBlank())     { sql.append(" AND tipo=?");                        params.add(tipo); }
        if (finalidad!= null && !finalidad.isBlank()){ sql.append(" AND finalidad=?");                   params.add(finalidad); }
        if (precioMax > 0)                           { sql.append(" AND precio<=?");                     params.add(precioMax); }
        if (minHab   > 0)                            { sql.append(" AND habitaciones>=?");               params.add(minHab); }
        if (ciudad   != null && !ciudad.isBlank())   { sql.append(" AND LOWER(ciudad) LIKE ?");          params.add("%" + ciudad.toLowerCase() + "%"); }
        sql.append(" ORDER BY precio ASC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int k = 0; k < params.size(); k++) {
                Object p = params.get(k);
                if      (p instanceof String  s) ps.setString(k+1, s);
                else if (p instanceof Double  d) ps.setDouble(k+1, d);
                else if (p instanceof Integer n) ps.setInt(k+1, n);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("[DB] filtrar inmuebles: " + e.getMessage()); }
        return lista;
    }

    // ── DISPONIBLES (para recomendaciones) ────────────────────────────────
    public List<Inmueble> listarDisponibles() {
        List<Inmueble> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM inmuebles WHERE disponible=1 ORDER BY contador_visitas DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("[DB] listar disponibles: " + e.getMessage()); }
        return lista;
    }

    // ── INCREMENTAR VISITAS ────────────────────────────────────────────────
    public void incrementarVisitas(String codigo) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE inmuebles SET contador_visitas = contador_visitas + 1 WHERE codigo=?")) {
            ps.setString(1, codigo); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] incrementar visitas: " + e.getMessage()); }
    }

    // ── MAPEAR ResultSet → Inmueble ────────────────────────────────────────
    private Inmueble mapear(ResultSet rs) throws SQLException {
        Inmueble i = new Inmueble();
        i.setId(rs.getInt("id"));
        i.setCodigo(rs.getString("codigo"));
        i.setDireccion(rs.getString("direccion"));
        i.setCiudad(rs.getString("ciudad"));
        i.setBarrio(rs.getString("barrio"));
        i.setTipo(Inmueble.Tipo.valueOf(rs.getString("tipo")));
        i.setFinalidad(Inmueble.Finalidad.valueOf(rs.getString("finalidad")));
        i.setPrecio(rs.getDouble("precio"));
        i.setArea(rs.getDouble("area"));
        i.setHabitaciones(rs.getInt("habitaciones"));
        i.setBanos(rs.getInt("banos"));
        i.setEstado(Inmueble.Estado.valueOf(rs.getString("estado")));
        i.setDisponible(rs.getInt("disponible") == 1);
        i.setCodigoAsesor(rs.getString("codigo_asesor"));
        i.setContadorVisitas(rs.getInt("contador_visitas"));
        return i;
    }
}