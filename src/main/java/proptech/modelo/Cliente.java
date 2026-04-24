package main.java.proptech.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
public class Cliente {
    private String id;
    private String nombre;
    private String correo;
    private String telefono;
    private TipoCliente tipo;
    private double presupuesto;
    private List<String> zonasInteres;
    private Inmueble.TipoInmueble tipoInmuebleDeseado;
    private int minHabitaciones;
    private EstadoBusqueda estadoBusqueda;
    private LocalDate fechaRegistro;
    private LocalDate ultimoSeguimiento;
 
    public enum TipoCliente { COMPRADOR, ARRENDATARIO, INVERSOR, VIP }
    public enum EstadoBusqueda { ACTIVO, EN_NEGOCIACION, PAUSADO, CERRADO }
 
    public Cliente(String id, String nombre, String correo, String telefono,
                   TipoCliente tipo, double presupuesto,
                   Inmueble.TipoInmueble tipoInmuebleDeseado, int minHabitaciones) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.tipo = tipo;
        this.presupuesto = presupuesto;
        this.tipoInmuebleDeseado = tipoInmuebleDeseado;
        this.minHabitaciones = minHabitaciones;
        this.zonasInteres = new ArrayList<>();
        this.estadoBusqueda = EstadoBusqueda.ACTIVO;
        this.fechaRegistro = LocalDate.now();
        this.ultimoSeguimiento = LocalDate.now();
    }
 
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getCorreo() { return correo; }
    public void setCorreo(String c) { this.correo = c; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String t) { this.telefono = t; }
    public TipoCliente getTipo() { return tipo; }
    public void setTipo(TipoCliente t) { this.tipo = t; }
    public double getPresupuesto() { return presupuesto; }
    public void setPresupuesto(double p) { this.presupuesto = p; }
    public List<String> getZonasInteres() { return zonasInteres; }
    public void agregarZona(String zona) { this.zonasInteres.add(zona); }
    public Inmueble.TipoInmueble getTipoInmuebleDeseado() { return tipoInmuebleDeseado; }
    public void setTipoInmuebleDeseado(Inmueble.TipoInmueble t) { this.tipoInmuebleDeseado = t; }
    public int getMinHabitaciones() { return minHabitaciones; }
    public void setMinHabitaciones(int m) { this.minHabitaciones = m; }
    public EstadoBusqueda getEstadoBusqueda() { return estadoBusqueda; }
    public void setEstadoBusqueda(EstadoBusqueda e) { this.estadoBusqueda = e; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public LocalDate getUltimoSeguimiento() { return ultimoSeguimiento; }
    public void actualizarSeguimiento() { this.ultimoSeguimiento = LocalDate.now(); }
 
    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Presupuesto: $%.0f | Busca: %s | Estado: %s",
                id, nombre, tipo, presupuesto, tipoInmuebleDeseado, estadoBusqueda);
    }
}
