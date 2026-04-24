package main.java.proptech.servicios;

import main.java.proptech.estructuras.*;
import main.java.proptech.modelo.*;
import java.time.LocalDate;
import java.util.*;
 
/**
 * SERVICIO: Gestión de Operaciones (arriendos, ventas, renovaciones)
 * Usa ListaEnlazada para el registro histórico,
 * TablaHash para acceso rápido,
 * ArbolBST para ranking de asesores por cierres.
 */
public class GestorOperaciones {
 
    private final TablaHash<String, Operacion> tablaOperaciones;
    private final ListaEnlazada<Operacion> historialGlobal;
    private final TablaHash<String, List<Operacion>> porAsesor;
    private final TablaHash<String, List<Operacion>> porInmueble;
    private int contadorId = 1;
 
    public GestorOperaciones() {
        tablaOperaciones = new TablaHash<>();
        historialGlobal = new ListaEnlazada<>();
        porAsesor = new TablaHash<>();
        porInmueble = new TablaHash<>();
    }
 
    private String generarId() { return String.format("OP-%04d", contadorId++); }
 
    /** Registra una operación de negocio. */
    public Operacion registrar(String codigoInmueble, String idCliente, String idAsesor,
                                Operacion.TipoOperacion tipo, double valor, double comision) {
        String id = generarId();
        Operacion op = new Operacion(id, codigoInmueble, idCliente, idAsesor, tipo, valor, comision);
        tablaOperaciones.insertar(id, op);
        historialGlobal.agregarAlFinal(op);
 
        List<Operacion> listaAsesor = porAsesor.buscar(idAsesor);
        if (listaAsesor == null) { listaAsesor = new ArrayList<>(); porAsesor.insertar(idAsesor, listaAsesor); }
        listaAsesor.add(op);
 
        List<Operacion> listaInmueble = porInmueble.buscar(codigoInmueble);
        if (listaInmueble == null) { listaInmueble = new ArrayList<>(); porInmueble.insertar(codigoInmueble, listaInmueble); }
        listaInmueble.add(op);
 
        System.out.println("  [OK] Operación registrada: " + id + " | " + tipo + " | $" + valor);
        return op;
    }
 
    /** Establece fecha de vencimiento para arriendos. */
    public void setFechaVencimiento(String idOperacion, LocalDate fecha) {
        Operacion op = tablaOperaciones.buscar(idOperacion);
        if (op != null) op.setFechaVencimiento(fecha);
    }
 
    /** Contratos que vencen en los próximos N días. */
    public List<Operacion> contratosProximosAVencer(int dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaOperaciones.valores()) {
            if (op.getTipo() == Operacion.TipoOperacion.ARRIENDO &&
                op.getEstado() == Operacion.EstadoOperacion.EN_PROCESO &&
                op.getFechaVencimiento() != null &&
                !op.getFechaVencimiento().isAfter(limite)) {
                resultado.add(op);
            }
        }
        resultado.sort(Comparator.comparing(Operacion::getFechaVencimiento));
        return resultado;
    }
 
    /** Completa una operación. */
    public boolean completar(String idOperacion) {
        Operacion op = tablaOperaciones.buscar(idOperacion);
        if (op == null) return false;
        op.setEstado(Operacion.EstadoOperacion.COMPLETADA);
        System.out.println("  [OK] Operación completada: " + idOperacion);
        return true;
    }
 
    public Operacion buscarPorId(String id) { return tablaOperaciones.buscar(id); }
    public List<Operacion> getTodas() { return historialGlobal.aLista(); }
    public List<Operacion> getPorAsesor(String idAsesor) {
        List<Operacion> r = porAsesor.buscar(idAsesor);
        return r == null ? Collections.emptyList() : r;
    }
    public List<Operacion> getPorInmueble(String codigo) {
        List<Operacion> r = porInmueble.buscar(codigo);
        return r == null ? Collections.emptyList() : r;
    }
}
