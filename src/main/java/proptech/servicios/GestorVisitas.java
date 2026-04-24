package main.java.proptech.servicios;

import main.java.proptech.estructuras.*;
import main.java.proptech.modelo.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
 
/**
 * SERVICIO: Gestión de Visitas
 * Usa ColaPrioridad para visitas urgentes/VIP,
 * Cola FIFO para visitas normales,
 * TablaHash para acceso rápido a visita por ID,
 * ListaEnlazada para historial por cliente.
 */
public class GestorVisitas {
 
    // TablaHash: acceso rápido a cualquier visita O(1)
    private final TablaHash<String, Visita> tablaVisitas;
 
    // ColaPrioridad: visitas ordenadas por urgencia (VIP primero)
    private final ColaPrioridad<Visita> colaPrioridad;
 
    // Cola normal FIFO para visitas estándar pendientes
    private final Cola<String> colaEstandar;
 
    // ListaEnlazada: historial de visitas por cliente
    private final TablaHash<String, ListaEnlazada<Visita>> historialPorCliente;
 
    // TablaHash: visitas por inmueble (para detectar alta demanda)
    private final TablaHash<String, List<Visita>> visitasPorInmueble;
 
    // Contador para IDs
    private int contadorId = 1;
 
    public GestorVisitas() {
        tablaVisitas = new TablaHash<>();
        colaPrioridad = new ColaPrioridad<>();
        colaEstandar = new Cola<>();
        historialPorCliente = new TablaHash<>();
        visitasPorInmueble = new TablaHash<>();
    }
 
    /** Genera ID único de visita. */
    private String generarId() {
        return String.format("VIS-%04d", contadorId++);
    }
 
    /** Programa una nueva visita. */
    public Visita programarVisita(String idCliente, String codigoInmueble,
                                   LocalDateTime fechaHora, String idAsesor, int prioridad) {
        String id = generarId();
        Visita v = new Visita(id, idCliente, codigoInmueble, fechaHora, idAsesor, prioridad);
        tablaVisitas.insertar(id, v);
 
        // Encolamientos según prioridad
        if (prioridad >= 2) {
            colaPrioridad.insertar(v); // VIP/urgente: cola de prioridad
        } else {
            colaEstandar.encolar(id); // Normal: FIFO
        }
 
        // Historial por cliente
        ListaEnlazada<Visita> historial = historialPorCliente.buscar(idCliente);
        if (historial == null) {
            historial = new ListaEnlazada<>();
            historialPorCliente.insertar(idCliente, historial);
        }
        historial.agregarAlFinal(v);
 
        // Índice por inmueble
        List<Visita> xInmueble = visitasPorInmueble.buscar(codigoInmueble);
        if (xInmueble == null) {
            xInmueble = new ArrayList<>();
            visitasPorInmueble.insertar(codigoInmueble, xInmueble);
        }
        xInmueble.add(v);
 
        System.out.println("  [OK] Visita programada: " + id + " | Prioridad:" + prioridad);
        return v;
    }
 
    /** Procesa la siguiente visita más urgente. Usa COLA DE PRIORIDAD. */
    public Visita procesarSiguienteUrgente() {
        if (colaPrioridad.estaVacia()) return null;
        return colaPrioridad.extraerMaximo();
    }
 
    /** Procesa la siguiente visita estándar. Usa COLA FIFO. */
    public Visita procesarSiguienteEstandar() {
        if (colaEstandar.estaVacia()) return null;
        String id = colaEstandar.desencolar();
        return tablaVisitas.buscar(id);
    }
 
    /** Actualiza el estado de una visita. */
    public boolean actualizarEstado(String idVisita, Visita.EstadoVisita nuevoEstado, String obs) {
        Visita v = tablaVisitas.buscar(idVisita);
        if (v == null) return false;
        v.setEstado(nuevoEstado);
        if (obs != null && !obs.isBlank()) v.setObservaciones(obs);
        System.out.println("  [OK] Visita " + idVisita + " -> " + nuevoEstado);
        return true;
    }
 
    /** Reprograma una visita. */
    public boolean reprogramar(String idVisita, LocalDateTime nuevaFecha) {
        Visita v = tablaVisitas.buscar(idVisita);
        if (v == null) return false;
        v.setFechaHora(nuevaFecha);
        v.setEstado(Visita.EstadoVisita.REPROGRAMADA);
        System.out.println("  [OK] Visita reprogramada: " + idVisita + " -> " + nuevaFecha);
        return true;
    }
 
    /** Cancela una visita. */
    public boolean cancelar(String idVisita, String motivo) {
        return actualizarEstado(idVisita, Visita.EstadoVisita.CANCELADA, motivo);
    }
 
    /** Historial de visitas de un cliente. Usa LISTA ENLAZADA. */
    public List<Visita> getHistorialCliente(String idCliente) {
        ListaEnlazada<Visita> h = historialPorCliente.buscar(idCliente);
        return h == null ? Collections.emptyList() : h.aLista();
    }
 
    /** Todas las visitas de un inmueble. */
    public List<Visita> getVisitasPorInmueble(String codigoInmueble) {
        List<Visita> v = visitasPorInmueble.buscar(codigoInmueble);
        return v == null ? Collections.emptyList() : v;
    }
 
    /** Visitas pendientes o confirmadas de un asesor. */
    public List<Visita> getVisitasPorAsesor(String idAsesor) {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaVisitas.valores()) {
            if (v.getIdAsesor().equals(idAsesor) &&
               (v.getEstado() == Visita.EstadoVisita.PENDIENTE ||
                v.getEstado() == Visita.EstadoVisita.CONFIRMADA)) {
                resultado.add(v);
            }
        }
        return resultado;
    }
 
    /** Inmuebles con visitas sin cierre en los últimos N días (comportamiento inusual). */
    public List<String> inmueblesConMuchasVisitasSinCierre(int umbral) {
        List<String> sospechosos = new ArrayList<>();
        for (String codigo : visitasPorInmueble.claves()) {
            List<Visita> visitas = visitasPorInmueble.buscar(codigo);
            long realizadas = visitas.stream()
                .filter(v -> v.getEstado() == Visita.EstadoVisita.REALIZADA).count();
            if (realizadas >= umbral) sospechosos.add(codigo + " (" + realizadas + " visitas sin cierre)");
        }
        return sospechosos;
    }
 
    /** Clientes con múltiples visitas en poco tiempo (comportamiento inusual). */
    public List<String> clientesConVisitasExcesivas(int umbral, int diasVentana) {
        List<String> sospechosos = new ArrayList<>();
        LocalDate desde = LocalDate.now().minusDays(diasVentana);
        for (String idCliente : historialPorCliente.claves()) {
            ListaEnlazada<Visita> h = historialPorCliente.buscar(idCliente);
            long cuenta = h.aLista().stream()
                .filter(v -> !v.getFechaHora().toLocalDate().isBefore(desde)).count();
            if (cuenta >= umbral) sospechosos.add(idCliente + " (" + cuenta + " visitas en " + diasVentana + " días)");
        }
        return sospechosos;
    }
 
    /** Lista todas las visitas pendientes de confirmar. */
    public List<Visita> visitasPendientes() {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaVisitas.valores()) {
            if (v.getEstado() == Visita.EstadoVisita.PENDIENTE) resultado.add(v);
        }
        return resultado;
    }
 
    public Visita buscarPorId(String id) { return tablaVisitas.buscar(id); }
    public List<Visita> getTodas() { return tablaVisitas.valores(); }
    public ColaPrioridad<Visita> getColaPrioridad() { return colaPrioridad; }
}
