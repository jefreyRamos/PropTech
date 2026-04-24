package main.java.proptech.estructuras;

/**
 * ESTRUCTURA: COLA (Queue) - FIFO
 * Implementación propia con nodos enlazados.
 * Uso en PropTech:
 *   - Solicitudes de atención de clientes
 *   - Visitas pendientes por procesar
 *   - Seguimiento de tareas administrativas
 *
 * Complejidad: encolar O(1), desencolar O(1)
 */
public class Cola<T> {
 
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;
        Nodo(T dato) { this.dato = dato; }
    }
 
    private Nodo<T> frente;
    private Nodo<T> fondo;
    private int tamano;
 
    public Cola() {
        frente = fondo = null;
        tamano = 0;
    }
 
    /** Encola un elemento al fondo. O(1) */
    public void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (estaVacia()) {
            frente = fondo = nuevo;
        } else {
            fondo.siguiente = nuevo;
            fondo = nuevo;
        }
        tamano++;
    }
 
    /** Desencola y retorna el elemento del frente. O(1) */
    public T desencolar() {
        if (estaVacia()) throw new RuntimeException("Cola vacía");
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fondo = null;
        tamano--;
        return dato;
    }
 
    /** Consulta el frente sin eliminar. O(1) */
    public T verFrente() {
        if (estaVacia()) throw new RuntimeException("Cola vacía");
        return frente.dato;
    }
 
    public boolean estaVacia() { return frente == null; }
    public int getTamano() { return tamano; }
 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cola[frente -> ");
        Nodo<T> actual = frente;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" -> ");
            actual = actual.siguiente;
        }
        sb.append(" <- fondo]");
        return sb.toString();
    }
}
