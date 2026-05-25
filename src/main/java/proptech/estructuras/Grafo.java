package proptech.estructuras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * GRAFO con lista de adyacencia.
 * Modela relaciones cliente ↔ inmueble (visitas, favoritos).
 * BFS para encontrar inmuebles relacionados y hacer recomendaciones.
 */
public class Grafo {

    public static class Arista {
        public final String destino;
        public final double peso;
        public final String tipo;
        public Arista(String d, double p, String t) { destino = d; peso = p; tipo = t; }
        @Override public String toString() {
            return String.format("→%s(%.1f,%s)", destino, peso, tipo);
        }
    }

    private final Map<String, List<Arista>> ady      = new HashMap<>();
    private final Set<String>               vertices = new HashSet<>();

    public void agregarVertice(String id) {
        vertices.add(id); ady.putIfAbsent(id, new ArrayList<>());
    }

    /** Arista dirigida origen → destino. O(1) */
    public void agregarArista(String origen, String destino, double peso, String tipo) {
        agregarVertice(origen); agregarVertice(destino);
        ady.get(origen).add(new Arista(destino, peso, tipo));
    }

    /** Arista no dirigida. O(1) */
    public void agregarAristaBi(String a, String b, double peso, String tipo) {
        agregarArista(a, b, peso, tipo); agregarArista(b, a, peso, tipo);
    }

    public List<Arista> vecinos(String id) {
        return ady.getOrDefault(id, Collections.emptyList());
    }

    /**
     * BFS desde un nodo. O(V + E)
     * Retorna todos los nodos alcanzables en orden de distancia.
     */
    public List<String> bfs(String origen) {
        if (!vertices.contains(origen)) return Collections.emptyList();
        List<String>  vis  = new ArrayList<>();
        Set<String>   seen = new HashSet<>();
        Queue<String> cola = new LinkedList<>();
        cola.add(origen); seen.add(origen);
        while (!cola.isEmpty()) {
            String cur = cola.poll(); vis.add(cur);
            for (Arista a : ady.getOrDefault(cur, List.of()))
                if (seen.add(a.destino)) cola.add(a.destino);
        }
        return vis;
    }

    /** Ranking de nodos por número de conexiones. */
    public List<Map.Entry<String, Integer>> rankingConexiones() {
        List<Map.Entry<String, Integer>> r = new ArrayList<>();
        for (String v : vertices) r.add(Map.entry(v, ady.getOrDefault(v, List.of()).size()));
        r.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return r;
    }

    public int            grado(String id) { return ady.getOrDefault(id, List.of()).size(); }
    public Set<String>    getVertices()    { return Collections.unmodifiableSet(vertices); }
    public int            numVertices()    { return vertices.size(); }
    public int            numAristas()     { return ady.values().stream().mapToInt(List::size).sum(); }

    @Override public String toString() {
        return String.format("Grafo{vertices=%d, aristas=%d}", numVertices(), numAristas());
    }
}