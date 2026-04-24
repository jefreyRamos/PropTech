package main.java.proptech.estructuras;

/**
 * ESTRUCTURA: PILA (Stack)
 * Implementación propia con nodos enlazados.
 * Uso en PropTech:
 *   - Deshacer cambios en publicaciones de inmuebles
 *   - Historial de acciones administrativas
 *   - Reversión de estados de propiedades
 *
 * Complejidad: push O(1), pop O(1), peek O(1)
 */
public class Pila<T> {
 
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;
        Nodo(T dato) { this.dato = dato; }
    }
 
    private Nodo<T> tope;
    private int tamano;
 
    public Pila() {
        tope = null;
        tamano = 0;
    }
 
    /** Apila un elemento en el tope. O(1) */
    public void push(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        nuevo.siguiente = tope;
        tope = nuevo;
        tamano++;
    }
 
    /** Desapila y retorna el elemento del tope. O(1) */
    public T pop() {
        if (estaVacia()) throw new RuntimeException("Pila vacía");
        T dato = tope.dato;
        tope = tope.siguiente;
        tamano--;
        return dato;
    }
 
    /** Consulta el tope sin eliminarlo. O(1) */
    public T peek() {
        if (estaVacia()) throw new RuntimeException("Pila vacía");
        return tope.dato;
    }
 
    public boolean estaVacia() { return tope == null; }
    public int getTamano() { return tamano; }
 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Pila[tope -> ");
        Nodo<T> actual = tope;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" | ");
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}
