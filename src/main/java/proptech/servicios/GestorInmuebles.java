package main.java.proptech.servicios;

import main.java.proptech.estructuras.*;
import main.java.proptech.modelo.*;
import java.time.LocalDate;
import java.util.*;
 
/**
 * SERVICIO: Gestión de Inmuebles
 * Usa TablaHash para acceso O(1), ArbolBST para orden por precio,
 * y Pila para historial de cambios (deshacer).
 */
public class GestorInmuebles {
 
    // TablaHash: acceso rápido por código O(1)
    private final TablaHash<String, Inmueble> tablaInmuebles;
 
    // ArbolBST: ordenamiento y búsqueda por precio O(log n)
    private final ArbolBST<Inmueble> arbolPorPrecio;
 
    // TablaHash: agrupar por ciudad para análisis de zona
    private final TablaHash<String, List<Inmueble>> porCiudad;
    private final TablaHash<String, List<Inmueble>> porBarrio;
    private final TablaHash<String, Integer> frecuenciaVisitasPorZona;
 
    // Pila: historial de cambios para deshacer
    private final Pila<String> historialCambios; // "código|campo|valorAnterior"
 
    private int contadorCodigo = 1;
 
    public GestorInmuebles() {
        tablaInmuebles = new TablaHash<>();
        arbolPorPrecio = new ArbolBST<>();
        porCiudad = new TablaHash<>();
        porBarrio = new TablaHash<>();
        frecuenciaVisitasPorZona = new TablaHash<>();
        historialCambios = new Pila<>();
    }
 
    /** Registra un nuevo inmueble. */
    public boolean registrar(Inmueble inmueble) {
        if (tablaInmuebles.contiene(inmueble.getCodigo())) {
            System.out.println("  [!] Ya existe un inmueble con código: " + inmueble.getCodigo());
            return false;
        }
        tablaInmuebles.insertar(inmueble.getCodigo(), inmueble);
        arbolPorPrecio.insertar(inmueble.getPrecio(), inmueble);
        agruparPorCiudad(inmueble);
        agruparPorBarrio(inmueble);
        System.out.println("  [OK] Inmueble registrado: " + inmueble.getCodigo());
        return true;
    }
 
    private void agruparPorCiudad(Inmueble i) {
        List<Inmueble> lista = porCiudad.buscar(i.getCiudad());
        if (lista == null) { lista = new ArrayList<>(); porCiudad.insertar(i.getCiudad(), lista); }
        lista.add(i);
    }
 
    private void agruparPorBarrio(Inmueble i) {
        List<Inmueble> lista = porBarrio.buscar(i.getBarrio());
        if (lista == null) { lista = new ArrayList<>(); porBarrio.insertar(i.getBarrio(), lista); }
        lista.add(i);
    }
 
    /** Busca por código en O(1). */
    public Inmueble buscarPorCodigo(String codigo) {
        return tablaInmuebles.buscar(codigo);
    }
 
    /** Modifica el precio con registro en la pila de deshacer. */
    public boolean modificarPrecio(String codigo, double nuevoPrecio) {
        Inmueble i = tablaInmuebles.buscar(codigo);
        if (i == null) return false;
        historialCambios.push(codigo + "|precio|" + i.getPrecio());
        arbolPorPrecio.eliminar(i.getPrecio());
        i.setPrecio(nuevoPrecio);
        arbolPorPrecio.insertar(nuevoPrecio, i);
        System.out.println("  [OK] Precio actualizado: " + codigo + " -> $" + nuevoPrecio);
        return true;
    }
 
    /** Modifica el estado con registro en la pila de deshacer. */
    public boolean modificarEstado(String codigo, Inmueble.EstadoInmueble nuevoEstado) {
        Inmueble i = tablaInmuebles.buscar(codigo);
        if (i == null) return false;
        historialCambios.push(codigo + "|estado|" + i.getEstado());
        i.setEstado(nuevoEstado);
        i.setDisponible(nuevoEstado == Inmueble.EstadoInmueble.DISPONIBLE);
        System.out.println("  [OK] Estado actualizado: " + codigo + " -> " + nuevoEstado);
        return true;
    }
 
    /** Deshace el último cambio registrado. Usa la PILA. */
    public boolean deshacerUltimoCambio() {
        if (historialCambios.estaVacia()) {
            System.out.println("  [!] No hay cambios para deshacer.");
            return false;
        }
        String registro = historialCambios.pop();
        String[] partes = registro.split("\\|");
        String codigo = partes[0], campo = partes[1], valorAnterior = partes[2];
        Inmueble i = tablaInmuebles.buscar(codigo);
        if (i == null) return false;
        switch (campo) {
            case "precio" -> {
                arbolPorPrecio.eliminar(i.getPrecio());
                i.setPrecio(Double.parseDouble(valorAnterior));
                arbolPorPrecio.insertar(i.getPrecio(), i);
            }
            case "estado" -> {
                i.setEstado(Inmueble.EstadoInmueble.valueOf(valorAnterior));
                i.setDisponible(i.getEstado() == Inmueble.EstadoInmueble.DISPONIBLE);
            }
        }
        System.out.println("  [OK] Deshacer: " + codigo + "." + campo + " restaurado a " + valorAnterior);
        return true;
    }
 
    /** Elimina un inmueble del sistema. */
    public boolean eliminar(String codigo) {
        Inmueble i = tablaInmuebles.buscar(codigo);
        if (i == null) return false;
        tablaInmuebles.eliminar(codigo);
        arbolPorPrecio.eliminar(i.getPrecio());
        System.out.println("  [OK] Inmueble eliminado: " + codigo);
        return true;
    }
 
    /** Busca inmuebles en rango de precio. Usa el ÁRBOL BST. */
    public List<Inmueble> buscarPorRangoPrecio(double min, double max) {
        return arbolPorPrecio.buscarEnRango(min, max);
    }
 
    /** Retorna inmuebles ordenados por precio (InOrden BST). */
    public List<Inmueble> ordenadosPorPrecio() {
        return arbolPorPrecio.inOrden();
    }
 
    /** Filtro combinado multi-criterio. */
    public List<Inmueble> buscarConFiltros(Inmueble.TipoInmueble tipo,
                                            Inmueble.Finalidad finalidad,
                                            double presupuestoMax,
                                            int minHabitaciones,
                                            String ciudad) {
        List<Inmueble> todos = tablaInmuebles.valores();
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : todos) {
            if (!i.isDisponible()) continue;
            if (tipo != null && i.getTipo() != tipo) continue;
            if (finalidad != null && i.getFinalidad() != finalidad) continue;
            if (presupuestoMax > 0 && i.getPrecio() > presupuestoMax) continue;
            if (minHabitaciones > 0 && i.getHabitaciones() < minHabitaciones) continue;
            if (ciudad != null && !ciudad.isBlank() && !i.getCiudad().equalsIgnoreCase(ciudad)) continue;
            resultado.add(i);
        }
        resultado.sort(Comparator.comparingDouble(Inmueble::getPrecio));
        return resultado;
    }
 
    /** Registra una visita en el contador del inmueble y la zona. */
    public void registrarVisitaEnInmueble(String codigoInmueble) {
        Inmueble i = tablaInmuebles.buscar(codigoInmueble);
        if (i != null) {
            i.incrementarVisitas();
            Integer actual = frecuenciaVisitasPorZona.buscar(i.getBarrio());
            frecuenciaVisitasPorZona.insertar(i.getBarrio(), (actual == null ? 0 : actual) + 1);
        }
    }
 
    /** Ranking de zonas por visitas. Usa la TablaHash de frecuencias. */
    public List<Map.Entry<String, Integer>> rankingZonas() {
        List<Map.Entry<String, Integer>> lista = new ArrayList<>();
        for (String zona : frecuenciaVisitasPorZona.claves()) {
            lista.add(Map.entry(zona, frecuenciaVisitasPorZona.buscar(zona)));
        }
        lista.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return lista;
    }
 
    /** Inmuebles sin visitas recientes (para alertas). */
    public List<Inmueble> inmueblesConPocoInteres(int umbralDias) {
        LocalDate limite = LocalDate.now().minusDays(umbralDias);
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaInmuebles.valores()) {
            if (i.isDisponible() && i.getFechaPublicacion().isBefore(limite) && i.getContadorVisitas() == 0) {
                resultado.add(i);
            }
        }
        return resultado;
    }
 
    /** Inmuebles con alta demanda. */
    public List<Inmueble> inmueblesAltaDemanda(int umbralVisitas) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaInmuebles.valores()) {
            if (i.getContadorVisitas() >= umbralVisitas) resultado.add(i);
        }
        resultado.sort(Comparator.comparingInt(Inmueble::getContadorVisitas).reversed());
        return resultado;
    }
 
    public List<Inmueble> getTodos() { return tablaInmuebles.valores(); }
    public List<Inmueble> getPorCiudad(String ciudad) {
        List<Inmueble> r = porCiudad.buscar(ciudad);
        return r == null ? Collections.emptyList() : r;
    }
    public TablaHash<String, Inmueble> getTabla() { return tablaInmuebles; }
}
