package proptech.servicios;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import proptech.estructuras.ArbolBST;
import proptech.estructuras.ColaPrioridad;
import proptech.estructuras.Grafo;
import proptech.estructuras.TablaHash;
import proptech.modelo.Asesor;
import proptech.modelo.Cliente;
import proptech.modelo.Inmueble;
import proptech.modelo.Visita;
import proptech.repositorio.AsesorRepository;
import proptech.repositorio.ClienteRepository;
import proptech.repositorio.InmuebleRepository;
import proptech.repositorio.VisitaRepository;

/**
 * SERVICIO CENTRAL — lógica de negocio.
 *
 * Integración BD ↔ Estructuras de datos:
 *   BD SQLite → fuente de verdad (persistencia)
 *   TablaHash → caché O(1) en memoria para Inmueble / Cliente / Asesor
 *   ArbolBST  → inmuebles ordenados por precio (inOrden, rango)
 *   ColaPrioridad → visitas VIP/ALTA se atienden primero
 *   Grafo     → relaciones cliente→inmueble para recomendaciones
 *   Pila (Deque) → historial de acciones para deshacer
 */
public class PropTechService {

    // ── Repositorios ───────────────────────────────────────────────────────
    private final InmuebleRepository repoInmueble = new InmuebleRepository();
    private final ClienteRepository  repoCliente  = new ClienteRepository();
    private final AsesorRepository   repoAsesor   = new AsesorRepository();
    private final VisitaRepository   repoVisita   = new VisitaRepository();

    // ── Estructuras de datos en memoria ───────────────────────────────────
    private final TablaHash<String, Inmueble>       cacheInmuebles = new TablaHash<>();
    private final TablaHash<String, Cliente>         cacheClientes  = new TablaHash<>();
    private final TablaHash<String, Asesor>          cacheAsesores  = new TablaHash<>();
    private final ArbolBST<Inmueble>                 arbolPrecio    = new ArbolBST<>();
    private final ColaPrioridad<VisitaPriorizada>    colaVisitas    = new ColaPrioridad<>();
    private final Grafo                              grafo          = new Grafo();
    private final Deque<String>                      pilaDeshacer   = new ArrayDeque<>();

    // ── Singleton ──────────────────────────────────────────────────────────
    private static PropTechService instancia;
    public static PropTechService getInstance() {
        if (instancia == null) instancia = new PropTechService();
        return instancia;
    }
    private PropTechService() { cargarCache(); }

    private void cargarCache() {
        repoInmueble.listarTodos().forEach(i -> {
            cacheInmuebles.put(i.getCodigo(), i);
            arbolPrecio.insertar(i.getPrecio(), i);
            grafo.agregarVertice("INM:" + i.getCodigo());
        });
        repoCliente.listarTodos().forEach(c -> {
            cacheClientes.put(c.getIdentificacion(), c);
            grafo.agregarVertice("CLI:" + c.getIdentificacion());
        });
        repoAsesor.listarTodos().forEach(a -> cacheAsesores.put(a.getCodigo(), a));
        System.out.printf("[Service] Caché → %d inmuebles | %d clientes | %d asesores%n",
                cacheInmuebles.size(), cacheClientes.size(), cacheAsesores.size());
    }

    // ══════════════════════════════════════════════════════════════════════
    // INMUEBLES
    // ══════════════════════════════════════════════════════════════════════

    public boolean registrarInmueble(Inmueble i) {
        if (cacheInmuebles.containsKey(i.getCodigo())) return false;
        if (!repoInmueble.insertar(i)) return false;
        cacheInmuebles.put(i.getCodigo(), i);
        arbolPrecio.insertar(i.getPrecio(), i);
        grafo.agregarVertice("INM:" + i.getCodigo());
        pilaDeshacer.push("CREAR_INM:" + i.getCodigo());
        return true;
    }

    public boolean actualizarInmueble(Inmueble i) {
        Inmueble anterior = cacheInmuebles.get(i.getCodigo());
        if (anterior == null) return false;
        pilaDeshacer.push("MOD_INM:" + i.getCodigo() + ":" + anterior.getPrecio());
        arbolPrecio.eliminar(anterior.getPrecio());
        if (!repoInmueble.actualizar(i)) return false;
        cacheInmuebles.put(i.getCodigo(), i);
        arbolPrecio.insertar(i.getPrecio(), i);
        return true;
    }

    public boolean eliminarInmueble(String codigo) {
        Inmueble i = cacheInmuebles.get(codigo);
        if (i == null || !repoInmueble.eliminar(codigo)) return false;
        arbolPrecio.eliminar(i.getPrecio());
        cacheInmuebles.remove(codigo);
        return true;
    }

    public Inmueble        buscarInmueble(String codigo) { return cacheInmuebles.get(codigo); } // O(1)
    public List<Inmueble>  getTodosInmuebles()           { return repoInmueble.listarTodos(); }
    public List<Inmueble>  filtrarInmuebles(String tipo, String fin, double maxPrecio, int minHab, String ciudad) {
        return repoInmueble.buscarConFiltros(tipo, fin, maxPrecio, minHab, ciudad);
    }
    public List<Inmueble>  inmueblesPorRango(double min, double max) { return arbolPrecio.rango(min, max); }
    public List<Inmueble>  inmueblesOrdenadosPorPrecio()             { return arbolPrecio.inOrden(); }

    // ══════════════════════════════════════════════════════════════════════
    // CLIENTES
    // ══════════════════════════════════════════════════════════════════════

    public boolean registrarCliente(Cliente c) {
        if (cacheClientes.containsKey(c.getIdentificacion())) return false;
        if (!repoCliente.insertar(c)) return false;
        cacheClientes.put(c.getIdentificacion(), c);
        grafo.agregarVertice("CLI:" + c.getIdentificacion());
        return true;
    }

    public boolean actualizarCliente(Cliente c) {
        if (!repoCliente.actualizar(c)) return false;
        cacheClientes.put(c.getIdentificacion(), c);
        return true;
    }

    public boolean eliminarCliente(String id) {
        if (!repoCliente.eliminar(id)) return false;
        cacheClientes.remove(id);
        return true;
    }

    public Cliente        buscarCliente(String id) { return cacheClientes.get(id); } // O(1)
    public List<Cliente>  getTodosClientes()        { return repoCliente.listarTodos(); }

    // ══════════════════════════════════════════════════════════════════════
    // ASESORES
    // ══════════════════════════════════════════════════════════════════════

    public boolean registrarAsesor(Asesor a) {
        if (cacheAsesores.containsKey(a.getCodigo())) return false;
        if (!repoAsesor.insertar(a)) return false;
        cacheAsesores.put(a.getCodigo(), a);
        return true;
    }

    public boolean actualizarAsesor(Asesor a) {
        if (!repoAsesor.actualizar(a)) return false;
        cacheAsesores.put(a.getCodigo(), a);
        return true;
    }

    public boolean eliminarAsesor(String codigo) {
        if (!repoAsesor.eliminar(codigo)) return false;
        cacheAsesores.remove(codigo);
        return true;
    }

    public Asesor        buscarAsesor(String codigo) { return cacheAsesores.get(codigo); }
    public List<Asesor>  getTodosAsesores()          { return repoAsesor.listarTodos(); }

    // ══════════════════════════════════════════════════════════════════════
    // VISITAS
    // ══════════════════════════════════════════════════════════════════════

    public boolean agendarVisita(Visita v) {
        if (!repoVisita.insertar(v)) return false;
        grafo.agregarArista("CLI:" + v.getIdCliente(), "INM:" + v.getCodigoInmueble(), 1.0, "visito");
        int p = switch (v.getPrioridad()) { case VIP -> 3; case ALTA -> 2; default -> 1; };
        colaVisitas.insertar(new VisitaPriorizada(v.getId(), p, v.getFechaHora()));
        repoInmueble.incrementarVisitas(v.getCodigoInmueble());
        Inmueble inm = cacheInmuebles.get(v.getCodigoInmueble());
        if (inm != null) inm.setContadorVisitas(inm.getContadorVisitas() + 1);
        return true;
    }

    public boolean            actualizarVisita(Visita v) { return repoVisita.actualizar(v); }
    public boolean            eliminarVisita(int id)     { return repoVisita.eliminar(id); }
    public List<Visita>       getTodosVisitas()          { return repoVisita.listarTodas(); }
    public List<Visita>       getVisitasPendientes()     { return repoVisita.listarPendientes(); }
    public VisitaPriorizada   procesarSiguiente()        { return colaVisitas.isEmpty() ? null : colaVisitas.extraer(); }

    // ══════════════════════════════════════════════════════════════════════
    // RECOMENDACIONES (Grafo + TablaHash + ArbolBST)
    // ══════════════════════════════════════════════════════════════════════

    public List<Inmueble> recomendar(String idCliente) {
        Cliente c = cacheClientes.get(idCliente);
        if (c == null) return Collections.emptyList();
        Set<String> yaVistos = new HashSet<>();
        for (Grafo.Arista a : grafo.vecinos("CLI:" + idCliente)) yaVistos.add(a.destino);
        return repoInmueble.listarDisponibles().stream()
                .filter(i -> !yaVistos.contains("INM:" + i.getCodigo()))
                .filter(i -> i.getPrecio() <= c.getPresupuesto())
                .filter(i -> c.getTipoInmuebleDeseado() == null || c.getTipoInmuebleDeseado().isBlank()
                             || i.getTipo().name().equals(c.getTipoInmuebleDeseado()))
                .filter(i -> i.getHabitaciones() >= c.getMinHabitaciones())
                .sorted(Comparator.comparingInt(Inmueble::getContadorVisitas).reversed())
                .limit(5).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════
    // ALERTAS
    // ══════════════════════════════════════════════════════════════════════

    public List<Inmueble> inmueblesConPocoInteres() {
        return cacheInmuebles.values().stream()
                .filter(i -> i.isDisponible() && i.getContadorVisitas() == 0)
                .collect(Collectors.toList());
    }

    public List<Inmueble> inmueblesAltaDemanda() {
        return cacheInmuebles.values().stream()
                .sorted(Comparator.comparingInt(Inmueble::getContadorVisitas).reversed())
                .limit(5).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════════════════

    public int    countInmuebles()         { return cacheInmuebles.size(); }
    public int    countClientes()          { return cacheClientes.size(); }
    public int    countAsesores()          { return cacheAsesores.size(); }
    public int    countVisitasPendientes() { return repoVisita.contarPendientes(); }
    public Grafo  getGrafo()              { return grafo; }

    public String getInfoEstructuras() {
        return String.format(
            "TablaHash inmuebles  : %d entradas\n" +
            "TablaHash clientes   : %d entradas\n" +
            "TablaHash asesores   : %d entradas\n" +
            "ArbolBST precio      : %d nodos\n" +
            "Grafo                : %s\n" +
            "Cola prioridad       : %d visitas urgentes\n" +
            "Pila deshacer        : %d acciones",
            cacheInmuebles.size(), cacheClientes.size(), cacheAsesores.size(),
            arbolPrecio.size(), grafo, colaVisitas.size(), pilaDeshacer.size());
    }

    // ── Clase auxiliar para ColaPrioridad ─────────────────────────────────
    public record VisitaPriorizada(int idVisita, int prioridad, String fechaHora)
            implements Comparable<VisitaPriorizada> {
        @Override public int compareTo(VisitaPriorizada o) {
            if (this.prioridad != o.prioridad) return Integer.compare(this.prioridad, o.prioridad);
            return this.fechaHora.compareTo(o.fechaHora);
        }
        @Override public String toString() {
            return String.format("Visita#%d [prior=%d, %s]", idVisita, prioridad, fechaHora);
        }
    }
}