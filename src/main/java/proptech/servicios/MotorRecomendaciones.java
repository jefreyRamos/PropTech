package main.java.proptech.servicios;

import main.java.proptech.estructuras.*;
import main.java.proptech.modelo.*;
import java.util.*;
 
/**
 * SERVICIO: Motor de Recomendaciones e Inteligencia Comercial
 * Usa Grafo para relaciones cliente-inmueble,
 * ColaPrioridad para priorizar recomendaciones,
 * TablaHash para scores de similitud.
 */
public class MotorRecomendaciones {
 
    // Grafo principal: relaciona clientes con inmuebles
    private final Grafo grafo;
    private final GestorInmuebles gestorInmuebles;
    private final GestorClientes gestorClientes;
 
    public MotorRecomendaciones(GestorInmuebles gestorInmuebles, GestorClientes gestorClientes) {
        this.grafo = new Grafo();
        this.gestorInmuebles = gestorInmuebles;
        this.gestorClientes = gestorClientes;
    }
 
    /** Registra que un cliente visitó un inmueble en el grafo. */
    public void registrarVisitaEnGrafo(String idCliente, String codigoInmueble) {
        grafo.agregarAristaDirigida("CLI-" + idCliente, "INM-" + codigoInmueble, 1.0, "visito");
    }
 
    /** Registra que un cliente marcó un inmueble como favorito. */
    public void registrarFavoritoEnGrafo(String idCliente, String codigoInmueble) {
        grafo.agregarAristaDirigida("CLI-" + idCliente, "INM-" + codigoInmueble, 2.0, "es_favorito");
    }
 
    /** Registra similitud entre dos inmuebles. */
    public void registrarSimilitud(String cod1, String cod2, double peso) {
        grafo.agregarArista("INM-" + cod1, "INM-" + cod2, peso, "similar");
    }
 
    /**
     * Recomienda inmuebles a un cliente usando:
     * 1. Filtro por preferencias (presupuesto, tipo, habitaciones, zona)
     * 2. Score de similitud con lo ya visitado
     * 3. Popularidad (favoritos + visitas)
     * Resultado ordenado por ColaPrioridad.
     */
    public List<Inmueble> recomendar(String idCliente, int cantidad) {
        Cliente cliente = gestorClientes.buscarPorId(idCliente);
        if (cliente == null) return Collections.emptyList();
 
        List<String> yaVisitados = gestorClientes.getHistorialConsultas(idCliente);
        List<String> yaDescartados = gestorClientes.getDescartados(idCliente);
 
        // Candidatos: todos los disponibles que no ha visitado ni descartado
        List<Inmueble> candidatos = gestorInmuebles.buscarConFiltros(
                cliente.getTipoInmuebleDeseado(),
                null,
                cliente.getPresupuesto(),
                cliente.getMinHabitaciones(),
                null);
 
        // Cola de prioridad para puntuar candidatos
        ColaPrioridad<InmueblePuntuado> cola = new ColaPrioridad<>();
 
        for (Inmueble i : candidatos) {
            if (yaVisitados.contains(i.getCodigo()) || yaDescartados.contains(i.getCodigo())) continue;
 
            double puntaje = calcularPuntaje(i, cliente);
            // Bonus por zona de interés
            if (cliente.getZonasInteres().contains(i.getBarrio())) puntaje += 20;
            // Bonus por popularidad
            puntaje += i.getContadorVisitas() * 0.5 + i.getContadorFavoritos() * 1.0;
 
            cola.insertar(new InmueblePuntuado(i, puntaje));
        }
 
        // Extraer los top N
        List<Inmueble> resultado = new ArrayList<>();
        int extraidos = 0;
        while (!cola.estaVacia() && extraidos < cantidad) {
            resultado.add(cola.extraerMaximo().inmueble);
            extraidos++;
        }
        return resultado;
    }
 
    private double calcularPuntaje(Inmueble i, Cliente c) {
        double puntaje = 0;
        // Entre más cercano al presupuesto (pero sin superar), mayor puntaje
        double ratio = i.getPrecio() / c.getPresupuesto();
        if (ratio <= 1.0) puntaje += (1 - ratio) > 0.5 ? 10 : 30; // Preferir cercanos al límite
        // Match de tipo
        if (i.getTipo().name().equalsIgnoreCase(c.getTipoInmuebleDeseado().name())) puntaje += 25;
        // Habitaciones
        if (i.getHabitaciones() >= c.getMinHabitaciones()) puntaje += 15;
        return puntaje;
    }
 
    /**
     * Recomendación de inmuebles similares a uno dado.
     * Usa BFS en el grafo para encontrar inmuebles conectados por similitud.
     */
    public List<Inmueble> inmueblesSimulares(String codigoInmueble, int cantidad) {
        List<String> bfsResult = grafo.bfs("INM-" + codigoInmueble);
        List<Inmueble> similares = new ArrayList<>();
        for (String nodo : bfsResult) {
            if (nodo.startsWith("INM-") && !nodo.equals("INM-" + codigoInmueble)) {
                String codigo = nodo.substring(4);
                Inmueble i = gestorInmuebles.buscarPorCodigo(codigo);
                if (i != null && i.isDisponible()) similares.add(i);
                if (similares.size() >= cantidad) break;
            }
        }
        return similares;
    }
 
    /**
     * Detecta clientes con intereses similares (visitaron inmuebles comunes).
     * Usa el análisis de vecindad en el grafo.
     */
    public Map<String, Integer> clientesSimilares(String idCliente) {
        return grafo.clientesConInmueblesComunes("CLI-" + idCliente);
    }
 
    /**
     * Ranking de inmuebles más populares en el grafo (mayor grado).
     */
    public List<Map.Entry<String, Integer>> rankingPopularidadGrafo() {
        List<Map.Entry<String, Integer>> ranking = grafo.rankingPorConexiones();
        ranking.removeIf(e -> !e.getKey().startsWith("INM-"));
        return ranking;
    }
 
    /** Componentes del grafo: clusters de actividad. */
    public List<List<String>> analisisComponentes() {
        return grafo.componentesConexas();
    }
 
    public Grafo getGrafo() { return grafo; }
 
    /** Clase auxiliar para la cola de prioridad de recomendaciones. */
    private static class InmueblePuntuado implements Comparable<InmueblePuntuado> {
        Inmueble inmueble;
        double puntaje;
        InmueblePuntuado(Inmueble i, double p) { this.inmueble = i; this.puntaje = p; }
        @Override public int compareTo(InmueblePuntuado otro) {
            return Double.compare(this.puntaje, otro.puntaje);
        }
        @Override public String toString() {
            return inmueble.getCodigo() + "(score=" + String.format("%.1f", puntaje) + ")";
        }
    }
}
