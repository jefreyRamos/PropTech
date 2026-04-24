package main.java.proptech.estructuras;

import java.util.*;
 
/**
 * ESTRUCTURA: GRAFO (no dirigido, con pesos) - Lista de adyacencia
 * Uso en PropTech:
 *   - Relaciones entre clientes e inmuebles visitados
 *   - Conexiones entre zonas y operaciones
 *   - Detección de propiedades similares consultadas por múltiples clientes
 *   - Análisis de patrones de movilidad comercial
 *
 * Complejidad: agregar vértice O(1), agregar arista O(1), BFS/DFS O(V+E)
 */
public class Grafo {
 
    public static class Arista {
        String destino;
        double peso;
        String tipo; // "visito", "es_favorito", "negocio", "similar"
        Arista(String destino, double peso, String tipo) {
            this.destino = destino;
            this.peso = peso;
            this.tipo = tipo;
        }
        @Override public String toString() {
            return String.format("->%s(%.0f,%s)", destino, peso, tipo);
        }
    }
 
    private final Map<String, List<Arista>> adyacencia;
    private final Set<String> vertices;
 
    public Grafo() {
        adyacencia = new HashMap<>();
        vertices = new HashSet<>();
    }
 
    /** Agrega un vértice (cliente, inmueble, zona). O(1) */
    public void agregarVertice(String id) {
        vertices.add(id);
        adyacencia.putIfAbsent(id, new ArrayList<>());
    }
 
    /** Agrega arista no dirigida entre dos nodos. O(1) */
    public void agregarArista(String origen, String destino, double peso, String tipo) {
        agregarVertice(origen);
        agregarVertice(destino);
        adyacencia.get(origen).add(new Arista(destino, peso, tipo));
        adyacencia.get(destino).add(new Arista(origen, peso, tipo));
    }
 
    /** Agrega arista dirigida. O(1) */
    public void agregarAristaDirigida(String origen, String destino, double peso, String tipo) {
        agregarVertice(origen);
        agregarVertice(destino);
        adyacencia.get(origen).add(new Arista(destino, peso, tipo));
    }
 
    /** Vecinos de un nodo. O(1) */
    public List<Arista> vecinos(String id) {
        return adyacencia.getOrDefault(id, Collections.emptyList());
    }
 
    /**
     * BFS: encuentra todos los nodos alcanzables desde origen.
     * Uso: encontrar inmuebles relacionados a un cliente.
     */
    public List<String> bfs(String origen) {
        if (!vertices.contains(origen)) return Collections.emptyList();
        List<String> visitados = new ArrayList<>();
        Set<String> visto = new HashSet<>();
        Queue<String> cola = new LinkedList<>();
        cola.add(origen);
        visto.add(origen);
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            visitados.add(actual);
            for (Arista arista : adyacencia.getOrDefault(actual, Collections.emptyList())) {
                if (!visto.contains(arista.destino)) {
                    visto.add(arista.destino);
                    cola.add(arista.destino);
                }
            }
        }
        return visitados;
    }
 
    /**
     * DFS: recorrido en profundidad.
     * Uso: explorar cadenas de relaciones en la red inmobiliaria.
     */
    public List<String> dfs(String origen) {
        if (!vertices.contains(origen)) return Collections.emptyList();
        List<String> visitados = new ArrayList<>();
        dfsRec(origen, new HashSet<>(), visitados);
        return visitados;
    }
 
    private void dfsRec(String actual, Set<String> visto, List<String> visitados) {
        visto.add(actual);
        visitados.add(actual);
        for (Arista arista : adyacencia.getOrDefault(actual, Collections.emptyList())) {
            if (!visto.contains(arista.destino)) dfsRec(arista.destino, visto, visitados);
        }
    }
 
    /**
     * Detecta clientes que comparten inmuebles visitados (nodos comunes).
     * Retorna pares de clientes con número de inmuebles en común.
     */
    public Map<String, Integer> clientesConInmueblesComunes(String idCliente) {
        Map<String, Integer> comunes = new HashMap<>();
        Set<String> inmueblesCliente = new HashSet<>();
        for (Arista a : adyacencia.getOrDefault(idCliente, Collections.emptyList())) {
            inmueblesCliente.add(a.destino);
        }
        // Para cada inmueble visitado, ver qué otros clientes también lo visitaron
        for (String inmueble : inmueblesCliente) {
            for (Arista a : adyacencia.getOrDefault(inmueble, Collections.emptyList())) {
                if (!a.destino.equals(idCliente) && a.destino.startsWith("CLI-")) {
                    comunes.merge(a.destino, 1, Integer::sum);
                }
            }
        }
        return comunes;
    }
 
    /**
     * Calcula el grado de un nodo (número de conexiones).
     * Uso: medir popularidad de un inmueble o actividad de un cliente.
     */
    public int grado(String id) {
        return adyacencia.getOrDefault(id, Collections.emptyList()).size();
    }
 
    /**
     * Ranking de nodos más conectados (mayor grado primero).
     * Uso: inmuebles más visitados, zonas más activas.
     */
    public List<Map.Entry<String, Integer>> rankingPorConexiones() {
        List<Map.Entry<String, Integer>> ranking = new ArrayList<>();
        for (String v : vertices) {
            ranking.add(Map.entry(v, grado(v)));
        }
        ranking.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return ranking;
    }
 
    /**
     * Detecta componentes conexas usando BFS iterativo.
     * Uso: identificar grupos aislados o clusters de actividad.
     */
    public List<List<String>> componentesConexas() {
        Set<String> visitados = new HashSet<>();
        List<List<String>> componentes = new ArrayList<>();
        for (String v : vertices) {
            if (!visitados.contains(v)) {
                List<String> componente = bfs(v);
                visitados.addAll(componente);
                componentes.add(componente);
            }
        }
        return componentes;
    }
 
    public Set<String> getVertices() { return vertices; }
    public int numVertices() { return vertices.size(); }
    public int numAristas() {
        int total = adyacencia.values().stream().mapToInt(List::size).sum();
        return total / 2; // no dirigido
    }
 
    @Override
    public String toString() {
        return String.format("Grafo{vértices=%d, aristas=%d}", numVertices(), numAristas());
    }
 
    public void imprimirAdyacencia() {
        System.out.println("=== Lista de Adyacencia del Grafo ===");
        for (Map.Entry<String, List<Arista>> entrada : adyacencia.entrySet()) {
            System.out.println(entrada.getKey() + ": " + entrada.getValue());
        }
    }
}