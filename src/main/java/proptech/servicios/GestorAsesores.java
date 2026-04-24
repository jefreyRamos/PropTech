package main.java.proptech.servicios;

import main.java.proptech.estructuras.*;
import main.java.proptech.modelo.*;
import java.util.*;
 
/**
 * SERVICIO: Gestión de Asesores
 * Usa TablaHash para acceso O(1),
 * ArbolBST para ranking por efectividad y cierres.
 */
public class GestorAsesores {
 
    private final TablaHash<String, Asesor> tablaAsesores;
    private final ArbolBST<Asesor> arbolPorCierres;
 
    public GestorAsesores() {
        tablaAsesores = new TablaHash<>();
        arbolPorCierres = new ArbolBST<>();
    }
 
    public boolean registrar(Asesor asesor) {
        if (tablaAsesores.contiene(asesor.getId())) return false;
        tablaAsesores.insertar(asesor.getId(), asesor);
        arbolPorCierres.insertar(asesor.getCierresRealizados(), asesor);
        System.out.println("  [OK] Asesor registrado: " + asesor.getId() + " - " + asesor.getNombre());
        return true;
    }
 
    public Asesor buscarPorId(String id) { return tablaAsesores.buscar(id); }
 
    /** Registra un cierre y actualiza el árbol de ranking. */
    public void registrarCierre(String idAsesor, double comision) {
        Asesor a = tablaAsesores.buscar(idAsesor);
        if (a == null) return;
        arbolPorCierres.eliminar(a.getCierresRealizados());
        a.registrarCierre(comision);
        arbolPorCierres.insertar(a.getCierresRealizados(), a);
        System.out.println("  [OK] Cierre registrado para asesor " + idAsesor + ". Total: " + a.getCierresRealizados());
    }
 
    public void asignarInmueble(String idAsesor, String codigoInmueble) {
        Asesor a = tablaAsesores.buscar(idAsesor);
        if (a != null) a.asignarInmueble(codigoInmueble);
    }
 
    public void registrarVisitaAsignada(String idAsesor) {
        Asesor a = tablaAsesores.buscar(idAsesor);
        if (a != null) a.incrementarVisitas();
    }
 
    /** Ranking de asesores por cierres. Usa ÁRBOL BST inOrden. */
    public List<Asesor> rankingPorCierres() {
        List<Asesor> lista = arbolPorCierres.inOrden();
        Collections.reverse(lista); // Mayor primero
        return lista;
    }
 
    /** Detecta asesores con sobrecarga (muchas visitas asignadas). */
    public List<Asesor> asesoresConSobrecarga(int umbral) {
        List<Asesor> resultado = new ArrayList<>();
        for (Asesor a : tablaAsesores.valores()) {
            if (a.getVisitasAgendadas() >= umbral) resultado.add(a);
        }
        resultado.sort(Comparator.comparingInt(Asesor::getVisitasAgendadas).reversed());
        return resultado;
    }
 
    public List<Asesor> getTodos() { return tablaAsesores.valores(); }
    public TablaHash<String, Asesor> getTabla() { return tablaAsesores; }
}
