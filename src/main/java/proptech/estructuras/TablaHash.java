package proptech.estructuras;

import java.util.ArrayList;
import java.util.List;

/**
 * TABLA HASH con encadenamiento por separado.
 * Acceso O(1) promedio — usada como caché de Inmuebles, Clientes y Asesores.
 */
public class TablaHash<K, V> {

    private static final int    CAP_INI  = 64;
    private static final double MAX_LOAD = 0.75;

    private static class Entrada<K, V> {
        K clave; V valor; Entrada<K, V> sig;
        Entrada(K c, V v) { clave = c; valor = v; }
    }

    @SuppressWarnings("unchecked")
    private Entrada<K, V>[] tabla = new Entrada[CAP_INI];
    private int capacidad = CAP_INI;
    private int tamano    = 0;

    private int idx(K k) {
        int h = k.hashCode() % capacidad;
        return h < 0 ? h + capacidad : h;
    }

    /** Inserta o actualiza. O(1) promedio */
    public void put(K clave, V valor) {
        if ((double) tamano / capacidad >= MAX_LOAD) rehash();
        int i = idx(clave);
        for (Entrada<K, V> n = tabla[i]; n != null; n = n.sig)
            if (n.clave.equals(clave)) { n.valor = valor; return; }
        Entrada<K, V> e = new Entrada<>(clave, valor);
        e.sig = tabla[i]; tabla[i] = e; tamano++;
    }

    /** Busca por clave. O(1) promedio */
    public V get(K clave) {
        for (Entrada<K, V> n = tabla[idx(clave)]; n != null; n = n.sig)
            if (n.clave.equals(clave)) return n.valor;
        return null;
    }

    /** Elimina. O(1) promedio */
    public boolean remove(K clave) {
        int i = idx(clave);
        Entrada<K, V> n = tabla[i], prev = null;
        while (n != null) {
            if (n.clave.equals(clave)) {
                if (prev == null) tabla[i] = n.sig; else prev.sig = n.sig;
                tamano--; return true;
            }
            prev = n; n = n.sig;
        }
        return false;
    }

    public boolean containsKey(K k) { return get(k) != null; }
    public int     size()            { return tamano; }
    public boolean isEmpty()         { return tamano == 0; }

    public List<V> values() {
        List<V> r = new ArrayList<>();
        for (Entrada<K, V> e : tabla)
            for (Entrada<K, V> n = e; n != null; n = n.sig) r.add(n.valor);
        return r;
    }

    public List<K> keys() {
        List<K> r = new ArrayList<>();
        for (Entrada<K, V> e : tabla)
            for (Entrada<K, V> n = e; n != null; n = n.sig) r.add(n.clave);
        return r;
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        capacidad *= 2;
        Entrada<K, V>[] nueva = new Entrada[capacidad];
        for (Entrada<K, V> e : tabla)
            for (Entrada<K, V> n = e; n != null; n = n.sig) {
                int i = Math.abs(n.clave.hashCode() % capacidad);
                Entrada<K, V> t = new Entrada<>(n.clave, n.valor);
                t.sig = nueva[i]; nueva[i] = t;
            }
        tabla = nueva;
    }

    @Override public String toString() {
        return "TablaHash{tamano=" + tamano + ", capacidad=" + capacidad + "}";
    }
}