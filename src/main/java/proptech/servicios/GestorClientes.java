package main.java.proptech.servicios;

import java.util.*;
import main.java.proptech.estructuras.*;
import main.java.proptech.modelo.*;
 
/**
 * SERVICIO: Gestión de Clientes
 * Usa TablaHash para acceso O(1),
 * ListaEnlazada para historial de interacciones y favoritos,
 * ArbolBST para clasificar clientes por presupuesto.
 */
public class GestorClientes {
 
    // TablaHash: búsqueda por ID en O(1)
    private final TablaHash<String, Cliente> tablaClientes;
 
    // Historial de interacción por cliente: ListaEnlazada<String> (códigos de inmueble)
    private final TablaHash<String, ListaEnlazada<String>> historialesConsulta;
 
    // Favoritos por cliente: ListaEnlazada<String>
    private final TablaHash<String, ListaEnlazada<String>> favoritos;
 
    // Inmuebles descartados por cliente
    private final TablaHash<String, ListaEnlazada<String>> descartados;
 
    // ArbolBST: clasificar clientes por presupuesto
    private final ArbolBST<Cliente> arbolPorPresupuesto;
 
    // Cola de atención: FIFO
    private final Cola<String> colaAtencion;
 
    public GestorClientes() {
        tablaClientes = new TablaHash<>();
        historialesConsulta = new TablaHash<>();
        favoritos = new TablaHash<>();
        descartados = new TablaHash<>();
        arbolPorPresupuesto = new ArbolBST<>();
        colaAtencion = new Cola<>();
    }
 
    /** Registra un nuevo cliente. */
    public boolean registrar(Cliente cliente) {
        if (tablaClientes.contiene(cliente.getId())) {
            System.out.println("  [!] Ya existe cliente con ID: " + cliente.getId());
            return false;
        }
        tablaClientes.insertar(cliente.getId(), cliente);
        historialesConsulta.insertar(cliente.getId(), new ListaEnlazada<>());
        favoritos.insertar(cliente.getId(), new ListaEnlazada<>());
        descartados.insertar(cliente.getId(), new ListaEnlazada<>());
        arbolPorPresupuesto.insertar(cliente.getPresupuesto(), cliente);
        System.out.println("  [OK] Cliente registrado: " + cliente.getId() + " - " + cliente.getNombre());
        return true;
    }
 
    /** Busca cliente por ID en O(1). */
    public Cliente buscarPorId(String id) {
        return tablaClientes.buscar(id);
    }
 
    /** Registra consulta de inmueble en el historial (ListaEnlazada). */
    public void registrarConsulta(String idCliente, String codigoInmueble) {
        ListaEnlazada<String> historial = historialesConsulta.buscar(idCliente);
        if (historial != null) {
            historial.agregarAlFinal(codigoInmueble);
            Cliente c = tablaClientes.buscar(idCliente);
            if (c != null) c.actualizarSeguimiento();
        }
    }
 
    /** Agrega a favoritos. */
    public boolean agregarFavorito(String idCliente, String codigoInmueble) {
        ListaEnlazada<String> favList = favoritos.buscar(idCliente);
        if (favList == null) return false;
        // No duplicar
        if (favList.buscar(c -> c.equals(codigoInmueble)) != null) {
            System.out.println("  [!] Ya está en favoritos.");
            return false;
        }
        favList.agregarAlFinal(codigoInmueble);
        System.out.println("  [OK] Favorito agregado: " + codigoInmueble + " para " + idCliente);
        return true;
    }
 
    /** Elimina de favoritos. */
    public boolean eliminarFavorito(String idCliente, String codigoInmueble) {
        ListaEnlazada<String> favList = favoritos.buscar(idCliente);
        if (favList == null) return false;
        return favList.eliminar(c -> c.equals(codigoInmueble));
    }
 
    /** Descarta un inmueble. */
    public void descartarInmueble(String idCliente, String codigoInmueble) {
        ListaEnlazada<String> desc = descartados.buscar(idCliente);
        if (desc != null) desc.agregarAlFinal(codigoInmueble);
        // Remover de favoritos si estaba
        eliminarFavorito(idCliente, codigoInmueble);
    }
 
    /** Retorna historial de consultas de un cliente. */
    public List<String> getHistorialConsultas(String idCliente) {
        ListaEnlazada<String> h = historialesConsulta.buscar(idCliente);
        return h == null ? Collections.emptyList() : h.aLista();
    }
 
    /** Retorna favoritos del cliente. */
    public List<String> getFavoritos(String idCliente) {
        ListaEnlazada<String> f = favoritos.buscar(idCliente);
        return f == null ? Collections.emptyList() : f.aLista();
    }
 
    /** Retorna descartados del cliente. */
    public List<String> getDescartados(String idCliente) {
        ListaEnlazada<String> d = descartados.buscar(idCliente);
        return d == null ? Collections.emptyList() : d.aLista();
    }
 
    /** Clientes ordenados por presupuesto. Usa ÁRBOL BST. */
    public List<Cliente> ordenadosPorPresupuesto() {
        return arbolPorPresupuesto.inOrden();
    }
 
    /** Clientes en rango de presupuesto. Usa ÁRBOL BST. */
    public List<Cliente> clientesPorRangoPresupuesto(double min, double max) {
        return arbolPorPresupuesto.buscarEnRango(min, max);
    }
 
    /** Encola cliente para atención. Usa COLA FIFO. */
    public void encolarAtencion(String idCliente) {
        colaAtencion.encolar(idCliente);
        System.out.println("  [OK] Cliente encolado para atención: " + idCliente);
    }
 
    /** Atiende al siguiente cliente. */
    public String atenderSiguiente() {
        if (colaAtencion.estaVacia()) return null;
        String id = colaAtencion.desencolar();
        System.out.println("  [OK] Atendiendo cliente: " + id);
        return id;
    }
 
    /** Modifica datos del cliente con registro en pila. */
    public boolean modificar(String id, String campo, String nuevoValor) {
        Cliente c = tablaClientes.buscar(id);
        if (c == null) return false;
        switch (campo.toLowerCase()) {
            case "nombre"   -> c.setNombre(nuevoValor);
            case "correo"   -> c.setCorreo(nuevoValor);
            case "telefono" -> c.setTelefono(nuevoValor);
            default -> { System.out.println("  [!] Campo no reconocido."); return false; }
        }
        return true;
    }
 
    /** Elimina cliente. */
    public boolean eliminar(String id) {
        if (!tablaClientes.contiene(id)) return false;
        tablaClientes.eliminar(id);
        historialesConsulta.eliminar(id);
        favoritos.eliminar(id);
        descartados.eliminar(id);
        System.out.println("  [OK] Cliente eliminado: " + id);
        return true;
    }
 
    /**
     * Detecta clientes con alta probabilidad de cierre:
     * - Tiene favoritos
     * - Estado ACTIVO o EN_NEGOCIACION
     * - Presupuesto suficiente
     */
    public List<Cliente> clientesConAltaProbabilidadCierre() {
        List<Cliente> candidatos = new ArrayList<>();
        for (Cliente c : tablaClientes.valores()) {
            if (c.getEstadoBusqueda() == Cliente.EstadoBusqueda.CERRADO) continue;
            List<String> favs = getFavoritos(c.getId());
            List<String> hist = getHistorialConsultas(c.getId());
            if (!favs.isEmpty() || hist.size() >= 3) {
                candidatos.add(c);
            }
        }
        // Ordenar: mayor presupuesto primero
        candidatos.sort(Comparator.comparingDouble(Cliente::getPresupuesto).reversed());
        return candidatos;
    }
 
    /** Clientes sin seguimiento reciente (N días). */
    public List<Cliente> clientesSinSeguimiento(int dias) {
        java.time.LocalDate limite = java.time.LocalDate.now().minusDays(dias);
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : tablaClientes.valores()) {
            if (c.getEstadoBusqueda() == Cliente.EstadoBusqueda.ACTIVO &&
                c.getUltimoSeguimiento().isBefore(limite)) {
                resultado.add(c);
            }
        }
        return resultado;
    }
 
    public List<Cliente> getTodos() { return tablaClientes.valores(); }
    public TablaHash<String, Cliente> getTabla() { return tablaClientes; }
    public Cola<String> getColaAtencion() { return colaAtencion; }
}