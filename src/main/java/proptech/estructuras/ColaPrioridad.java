package main.java.proptech.estructuras;

import java.util.ArrayList;
import java.util.List;
 
/**
 * ESTRUCTURA: COLA DE PRIORIDAD (Max-Heap)
 * Implementación propia con arreglo dinámico.
 * Uso en PropTech:
 *   - Visitas urgentes o VIP primero
 *   - Alertas por nivel de criticidad
 *   - Clientes con alta intención de cierre
 *   - Inmuebles con mayor demanda
 *
 * Complejidad: insertar O(log n), extraer máximo O(log n), ver máximo O(1)
 */
public class ColaPrioridad<T extends Comparable<T>> {
 
    private final List<T> heap;
 
    public ColaPrioridad() {
        this.heap = new ArrayList<>();
    }
 
    /** Inserta manteniendo la propiedad de max-heap. O(log n) */
    public void insertar(T elemento) {
        heap.add(elemento);
        subirBurbuja(heap.size() - 1);
    }
 
    /** Extrae el elemento de mayor prioridad. O(log n) */
    public T extraerMaximo() {
        if (estaVacia()) throw new RuntimeException("Cola de prioridad vacía");
        T max = heap.get(0);
        T ultimo = heap.remove(heap.size() - 1);
        if (!estaVacia()) {
            heap.set(0, ultimo);
            bajarBurbuja(0);
        }
        return max;
    }
 
    /** Ve el elemento de mayor prioridad sin extraerlo. O(1) */
    public T verMaximo() {
        if (estaVacia()) throw new RuntimeException("Cola de prioridad vacía");
        return heap.get(0);
    }
 
    private void subirBurbuja(int idx) {
        while (idx > 0) {
            int padre = (idx - 1) / 2;
            if (heap.get(idx).compareTo(heap.get(padre)) > 0) {
                intercambiar(idx, padre);
                idx = padre;
            } else break;
        }
    }
 
    private void bajarBurbuja(int idx) {
        int n = heap.size();
        while (true) {
            int mayor = idx;
            int izq = 2 * idx + 1;
            int der = 2 * idx + 2;
            if (izq < n && heap.get(izq).compareTo(heap.get(mayor)) > 0) mayor = izq;
            if (der < n && heap.get(der).compareTo(heap.get(mayor)) > 0) mayor = der;
            if (mayor != idx) { intercambiar(idx, mayor); idx = mayor; }
            else break;
        }
    }
 
    private void intercambiar(int i, int j) {
        T tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }
 
    public boolean estaVacia() { return heap.isEmpty(); }
    public int getTamano() { return heap.size(); }
 
    /** Lista todos los elementos en orden descendente (no destructivo). */
    public List<T> listarOrdenado() {
        ColaPrioridad<T> copia = new ColaPrioridad<>();
        for (T e : heap) copia.insertar(e);
        List<T> resultado = new ArrayList<>();
        while (!copia.estaVacia()) resultado.add(copia.extraerMaximo());
        return resultado;
    }
 
    @Override
    public String toString() {
        return "ColaPrioridad(max=" + (estaVacia() ? "vacía" : verMaximo()) + ", tam=" + heap.size() + ")";
    }
}
