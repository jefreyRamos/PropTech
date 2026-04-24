package main.java.proptech.estructuras;

import java.util.ArrayList;
import java.util.List;
 
/**
 * ESTRUCTURA: ÁRBOL BINARIO DE BÚSQUEDA (BST)
 * Indexa inmuebles por precio para búsquedas y rangos eficientes.
 * Uso en PropTech:
 *   - Ordenar inmuebles por precio
 *   - Consultar propiedades en rango de precio
 *   - Clasificar clientes por presupuesto
 *   - Ordenar asesores por cierres o efectividad
 *
 * Complejidad promedio: insertar O(log n), buscar O(log n), rango O(log n + k)
 */
public class ArbolBST<T> {
 
    private static class Nodo<T> {
        double clave;   // precio o valor numérico de ordenamiento
        T dato;
        Nodo<T> izq, der;
        Nodo(double clave, T dato) { this.clave = clave; this.dato = dato; }
    }
 
    private Nodo<T> raiz;
    private int tamano;
 
    public ArbolBST() { raiz = null; tamano = 0; }
 
    /** Inserta un elemento con su clave numérica. O(log n) promedio */
    public void insertar(double clave, T dato) {
        raiz = insertarRec(raiz, clave, dato);
        tamano++;
    }
 
    private Nodo<T> insertarRec(Nodo<T> nodo, double clave, T dato) {
        if (nodo == null) return new Nodo<>(clave, dato);
        if (clave < nodo.clave)       nodo.izq = insertarRec(nodo.izq, clave, dato);
        else if (clave > nodo.clave)  nodo.der = insertarRec(nodo.der, clave, dato);
        else nodo.dato = dato; // actualizar si misma clave
        return nodo;
    }
 
    /** Busca exactamente por clave. O(log n) */
    public T buscar(double clave) {
        Nodo<T> nodo = buscarNodo(raiz, clave);
        return nodo == null ? null : nodo.dato;
    }
 
    private Nodo<T> buscarNodo(Nodo<T> nodo, double clave) {
        if (nodo == null || nodo.clave == clave) return nodo;
        return clave < nodo.clave ? buscarNodo(nodo.izq, clave) : buscarNodo(nodo.der, clave);
    }
 
    /** Retorna todos los elementos en un rango [min, max]. O(log n + k) */
    public List<T> buscarEnRango(double min, double max) {
        List<T> resultado = new ArrayList<>();
        buscarRangoRec(raiz, min, max, resultado);
        return resultado;
    }
 
    private void buscarRangoRec(Nodo<T> nodo, double min, double max, List<T> resultado) {
        if (nodo == null) return;
        if (nodo.clave > min) buscarRangoRec(nodo.izq, min, max, resultado);
        if (nodo.clave >= min && nodo.clave <= max) resultado.add(nodo.dato);
        if (nodo.clave < max) buscarRangoRec(nodo.der, min, max, resultado);
    }
 
    /** Recorrido InOrden: retorna elementos ordenados de menor a mayor. O(n) */
    public List<T> inOrden() {
        List<T> lista = new ArrayList<>();
        inOrdenRec(raiz, lista);
        return lista;
    }
 
    private void inOrdenRec(Nodo<T> nodo, List<T> lista) {
        if (nodo == null) return;
        inOrdenRec(nodo.izq, lista);
        lista.add(nodo.dato);
        inOrdenRec(nodo.der, lista);
    }
 
    /** Retorna los N elementos más baratos. */
    public List<T> nMasBaratos(int n) {
        List<T> todos = inOrden();
        return todos.subList(0, Math.min(n, todos.size()));
    }
 
    /** Retorna los N elementos más caros. */
    public List<T> nMasCaros(int n) {
        List<T> todos = inOrden();
        int desde = Math.max(0, todos.size() - n);
        return todos.subList(desde, todos.size());
    }
 
    /** Elimina un nodo por clave. O(log n) */
    public void eliminar(double clave) {
        raiz = eliminarRec(raiz, clave);
        tamano--;
    }
 
    private Nodo<T> eliminarRec(Nodo<T> nodo, double clave) {
        if (nodo == null) return null;
        if (clave < nodo.clave) nodo.izq = eliminarRec(nodo.izq, clave);
        else if (clave > nodo.clave) nodo.der = eliminarRec(nodo.der, clave);
        else {
            if (nodo.izq == null) return nodo.der;
            if (nodo.der == null) return nodo.izq;
            // Sucesor inorden (mínimo del subárbol derecho)
            Nodo<T> sucesor = minimo(nodo.der);
            nodo.clave = sucesor.clave;
            nodo.dato = sucesor.dato;
            nodo.der = eliminarRec(nodo.der, sucesor.clave);
        }
        return nodo;
    }
 
    private Nodo<T> minimo(Nodo<T> nodo) {
        while (nodo.izq != null) nodo = nodo.izq;
        return nodo;
    }
 
    public int getTamano() { return tamano; }
    public boolean estaVacio() { return raiz == null; }
 
    @Override
    public String toString() {
        return "ArbolBST{tamano=" + tamano + ", raiz.clave=" + (raiz == null ? "nulo" : raiz.clave) + "}";
    }
}
