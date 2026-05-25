package proptech.estructuras;

import java.util.ArrayList;
import java.util.List;

/**
 * ÁRBOL BINARIO DE BÚSQUEDA genérico.
 * Indexa por clave double (precio, presupuesto).
 * InOrden → ordenado menor a mayor.  Rango → O(log n + k).
 */
public class ArbolBST<T> {

    private static class Nodo<T> {
        double clave; T dato; Nodo<T> izq, der;
        Nodo(double c, T d) { clave = c; dato = d; }
    }

    private Nodo<T> raiz;
    private int     tamano;

    /** O(log n) promedio */
    public void insertar(double clave, T dato) {
        raiz = ins(raiz, clave, dato); tamano++;
    }
    private Nodo<T> ins(Nodo<T> n, double k, T d) {
        if (n == null)    return new Nodo<>(k, d);
        if (k < n.clave)  n.izq = ins(n.izq, k, d);
        else if (k > n.clave) n.der = ins(n.der, k, d);
        else n.dato = d;
        return n;
    }

    /** O(log n) promedio */
    public void eliminar(double clave) {
        raiz = del(raiz, clave);
        if (tamano > 0) tamano--;
    }
    private Nodo<T> del(Nodo<T> n, double k) {
        if (n == null) return null;
        if      (k < n.clave) n.izq = del(n.izq, k);
        else if (k > n.clave) n.der = del(n.der, k);
        else {
            if (n.izq == null) return n.der;
            if (n.der == null) return n.izq;
            Nodo<T> m = n.der; while (m.izq != null) m = m.izq;
            n.clave = m.clave; n.dato = m.dato;
            n.der   = del(n.der, m.clave);
        }
        return n;
    }

    /** Recorrido InOrden: menor → mayor. O(n) */
    public List<T> inOrden() { List<T> r = new ArrayList<>(); inOrd(raiz, r); return r; }
    private void inOrd(Nodo<T> n, List<T> r) {
        if (n == null) return; inOrd(n.izq, r); r.add(n.dato); inOrd(n.der, r);
    }

    /** Elementos con clave en [min, max]. O(log n + k) */
    public List<T> rango(double min, double max) {
        List<T> r = new ArrayList<>(); rang(raiz, min, max, r); return r;
    }
    private void rang(Nodo<T> n, double min, double max, List<T> r) {
        if (n == null) return;
        if (n.clave > min) rang(n.izq, min, max, r);
        if (n.clave >= min && n.clave <= max) r.add(n.dato);
        if (n.clave < max) rang(n.der, min, max, r);
    }

    public int     size()    { return tamano; }
    public boolean isEmpty() { return raiz == null; }

    @Override public String toString() { return "ArbolBST{tamano=" + tamano + "}"; }
}