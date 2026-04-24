package main.java.proptech.estructuras;

import java.util.ArrayList;
import java.util.List;
 
/**
 * ESTRUCTURA: TABLA HASH (Hash Map con encadenamiento)
 * Implementación propia con arreglo de listas enlazadas.
 * Uso en PropTech:
 *   - Buscar clientes por ID en O(1)
 *   - Acceder a inmuebles por código en O(1)
 *   - Localizar asesores rápidamente
 *   - Contar frecuencia de visitas por inmueble o zona
 *
 * Complejidad promedio: insertar O(1), buscar O(1), eliminar O(1)
 */
public class TablaHash<K, V> {
 
    private static final int CAPACIDAD_INICIAL = 64;
    private static final double FACTOR_CARGA_MAX = 0.75;
 
    private static class Entrada<K, V> {
        K clave;
        V valor;
        Entrada<K, V> siguiente;
        Entrada(K clave, V valor) { this.clave = clave; this.valor = valor; }
    }
 
    @SuppressWarnings("unchecked")
    private Entrada<K, V>[] tabla;
    private int tamano;
    private int capacidad;
 
    @SuppressWarnings("unchecked")
    public TablaHash() {
        this.capacidad = CAPACIDAD_INICIAL;
        this.tabla = new Entrada[capacidad];
        this.tamano = 0;
    }
 
    private int indice(K clave) {
        int hash = clave.hashCode() % capacidad;
        return hash < 0 ? hash + capacidad : hash;
    }
 
    /** Inserta o actualiza un par clave-valor. O(1) promedio */
    public void insertar(K clave, V valor) {
        if ((double) tamano / capacidad >= FACTOR_CARGA_MAX) rehash();
        int idx = indice(clave);
        Entrada<K, V> actual = tabla[idx];
        while (actual != null) {
            if (actual.clave.equals(clave)) { actual.valor = valor; return; }
            actual = actual.siguiente;
        }
        Entrada<K, V> nueva = new Entrada<>(clave, valor);
        nueva.siguiente = tabla[idx];
        tabla[idx] = nueva;
        tamano++;
    }
 
    /** Busca un valor por clave. O(1) promedio */
    public V buscar(K clave) {
        int idx = indice(clave);
        Entrada<K, V> actual = tabla[idx];
        while (actual != null) {
            if (actual.clave.equals(clave)) return actual.valor;
            actual = actual.siguiente;
        }
        return null;
    }
 
    /** Elimina una entrada por clave. O(1) promedio */
    public boolean eliminar(K clave) {
        int idx = indice(clave);
        Entrada<K, V> actual = tabla[idx];
        Entrada<K, V> anterior = null;
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) tabla[idx] = actual.siguiente;
                else anterior.siguiente = actual.siguiente;
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }
 
    public boolean contiene(K clave) { return buscar(clave) != null; }
    public int getTamano() { return tamano; }
    public boolean estaVacia() { return tamano == 0; }
 
    /** Retorna todos los valores almacenados. O(n) */
    public List<V> valores() {
        List<V> lista = new ArrayList<>();
        for (Entrada<K, V> entrada : tabla) {
            Entrada<K, V> actual = entrada;
            while (actual != null) { lista.add(actual.valor); actual = actual.siguiente; }
        }
        return lista;
    }
 
    /** Retorna todas las claves. O(n) */
    public List<K> claves() {
        List<K> lista = new ArrayList<>();
        for (Entrada<K, V> entrada : tabla) {
            Entrada<K, V> actual = entrada;
            while (actual != null) { lista.add(actual.clave); actual = actual.siguiente; }
        }
        return lista;
    }
 
    @SuppressWarnings("unchecked")
    private void rehash() {
        capacidad *= 2;
        Entrada<K, V>[] nuevaTabla = new Entrada[capacidad];
        for (Entrada<K, V> entrada : tabla) {
            Entrada<K, V> actual = entrada;
            while (actual != null) {
                int idx = Math.abs(actual.clave.hashCode() % capacidad);
                Entrada<K, V> nueva = new Entrada<>(actual.clave, actual.valor);
                nueva.siguiente = nuevaTabla[idx];
                nuevaTabla[idx] = nueva;
                actual = actual.siguiente;
            }
        }
        tabla = nuevaTabla;
    }
 
    @Override
    public String toString() {
        return "TablaHash{tamano=" + tamano + ", capacidad=" + capacidad + "}";
    }
}
