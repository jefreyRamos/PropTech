package main.java.proptech.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
 
/**
 * ESTRUCTURA: LISTA ENLAZADA (doblemente enlazada)
 * Uso en PropTech:
 *   - Historial de visitas por cliente
 *   - Historial de inmuebles consultados
 *   - Favoritos por cliente
 *   - Inmuebles asignados a un asesor
 *   - Contratos y operaciones
 *
 * Complejidad: insertar al inicio/final O(1), buscar O(n), eliminar O(n)
 */
public class ListaEnlazada<T> {
 
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;
        Nodo<T> anterior;
        Nodo(T dato) { this.dato = dato; }
    }
 
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;
 
    public ListaEnlazada() { cabeza = cola = null; tamano = 0; }
 
    /** Agrega al final. O(1) */
    public void agregarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) { cabeza = cola = nuevo; }
        else {
            nuevo.anterior = cola;
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamano++;
    }
 
    /** Agrega al inicio. O(1) */
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) { cabeza = cola = nuevo; }
        else {
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
            cabeza = nuevo;
        }
        tamano++;
    }
 
    /** Elimina el primer elemento que cumpla el predicado. O(n) */
    public boolean eliminar(Predicate<T> condicion) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                if (actual.anterior != null) actual.anterior.siguiente = actual.siguiente;
                else cabeza = actual.siguiente;
                if (actual.siguiente != null) actual.siguiente.anterior = actual.anterior;
                else cola = actual.anterior;
                tamano--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }
 
    /** Busca el primero que cumpla el predicado. O(n) */
    public T buscar(Predicate<T> condicion) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (condicion.test(actual.dato)) return actual.dato;
            actual = actual.siguiente;
        }
        return null;
    }
 
    /** Retorna todos los que cumplen el predicado. O(n) */
    public List<T> filtrar(Predicate<T> condicion) {
        List<T> resultado = new ArrayList<>();
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (condicion.test(actual.dato)) resultado.add(actual.dato);
            actual = actual.siguiente;
        }
        return resultado;
    }
 
    /** Retorna todos los elementos como lista. O(n) */
    public List<T> aLista() {
        List<T> lista = new ArrayList<>();
        Nodo<T> actual = cabeza;
        while (actual != null) { lista.add(actual.dato); actual = actual.siguiente; }
        return lista;
    }
 
    /** Retorna el primer elemento. O(1) */
    public T primero() {
        if (estaVacia()) return null;
        return cabeza.dato;
    }
 
    /** Retorna el último elemento. O(1) */
    public T ultimo() {
        if (estaVacia()) return null;
        return cola.dato;
    }
 
    public boolean estaVacia() { return cabeza == null; }
    public int getTamano() { return tamano; }
 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Lista[");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" <-> ");
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}
