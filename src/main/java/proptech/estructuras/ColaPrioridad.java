package proptech.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * COLA DE PRIORIDAD — Max-Heap binario propio.
 * El elemento con mayor prioridad se sirve primero.
 * Usada para visitas VIP/urgentes.
 * insertar O(log n) | extraer O(log n) | peek O(1)
 */
public class ColaPrioridad<T extends Comparable<T>> {

    private final List<T> heap = new ArrayList<>();

    public void insertar(T e) {
        heap.add(e); subir(heap.size() - 1);
    }

    public T extraer() {
        if (heap.isEmpty()) throw new NoSuchElementException("Cola vacía");
        T top  = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) { heap.set(0, last); bajar(0); }
        return top;
    }

    public T    peek()    { return heap.isEmpty() ? null : heap.get(0); }
    public boolean isEmpty() { return heap.isEmpty(); }
    public int  size()    { return heap.size(); }
    public List<T> toList(){ return new ArrayList<>(heap); }

    private void subir(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (heap.get(i).compareTo(heap.get(p)) > 0) { swap(i, p); i = p; } else break;
        }
    }
    private void bajar(int i) {
        int n = heap.size();
        while (true) {
            int m = i, l = 2*i+1, r = 2*i+2;
            if (l < n && heap.get(l).compareTo(heap.get(m)) > 0) m = l;
            if (r < n && heap.get(r).compareTo(heap.get(m)) > 0) m = r;
            if (m != i) { swap(i, m); i = m; } else break;
        }
    }
    private void swap(int a, int b) { T t = heap.get(a); heap.set(a, heap.get(b)); heap.set(b, t); }

    @Override public String toString() {
        return "ColaPrioridad{size=" + heap.size() + ", max=" + (heap.isEmpty() ? "vacía" : heap.get(0)) + "}";
    }
}