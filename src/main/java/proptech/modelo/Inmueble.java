package main.java.proptech.modelo;

import java.time.LocalDate;
 
public class Inmueble {
    private String codigo;
    private String direccion;
    private String ciudad;
    private String barrio;
    private TipoInmueble tipo;
    private Finalidad finalidad;
    private double precio;
    private double area;
    private int habitaciones;
    private int banos;
    private EstadoInmueble estado;
    private boolean disponible;
    private String codigoAsesor;
    private LocalDate fechaPublicacion;
    private int contadorVisitas;
    private int contadorFavoritos;
    private LocalDate ultimaModificacion;
 
    public enum TipoInmueble { APARTAMENTO, CASA, LOCAL_COMERCIAL, OFICINA, LOTE, BODEGA }
    public enum Finalidad { VENTA, ARRIENDO }
    public enum EstadoInmueble { DISPONIBLE, RESERVADO, ARRENDADO, VENDIDO, EN_MANTENIMIENTO }
 
    public Inmueble(String codigo, String direccion, String ciudad, String barrio,
                    TipoInmueble tipo, Finalidad finalidad, double precio,
                    double area, int habitaciones, int banos, String codigoAsesor) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.barrio = barrio;
        this.tipo = tipo;
        this.finalidad = finalidad;
        this.precio = precio;
        this.area = area;
        this.habitaciones = habitaciones;
        this.banos = banos;
        this.codigoAsesor = codigoAsesor;
        this.estado = EstadoInmueble.DISPONIBLE;
        this.disponible = true;
        this.fechaPublicacion = LocalDate.now();
        this.ultimaModificacion = LocalDate.now();
        this.contadorVisitas = 0;
        this.contadorFavoritos = 0;
    }
 
    // Getters y Setters
    public String getCodigo() { return codigo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String d) { this.direccion = d; this.ultimaModificacion = LocalDate.now(); }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String c) { this.ciudad = c; }
    public String getBarrio() { return barrio; }
    public void setBarrio(String b) { this.barrio = b; }
    public TipoInmueble getTipo() { return tipo; }
    public void setTipo(TipoInmueble t) { this.tipo = t; }
    public Finalidad getFinalidad() { return finalidad; }
    public void setFinalidad(Finalidad f) { this.finalidad = f; }
    public double getPrecio() { return precio; }
    public void setPrecio(double p) { this.precio = p; this.ultimaModificacion = LocalDate.now(); }
    public double getArea() { return area; }
    public void setArea(double a) { this.area = a; }
    public int getHabitaciones() { return habitaciones; }
    public void setHabitaciones(int h) { this.habitaciones = h; }
    public int getBanos() { return banos; }
    public void setBanos(int b) { this.banos = b; }
    public EstadoInmueble getEstado() { return estado; }
    public void setEstado(EstadoInmueble e) { this.estado = e; this.ultimaModificacion = LocalDate.now(); }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean d) { this.disponible = d; }
    public String getCodigoAsesor() { return codigoAsesor; }
    public void setCodigoAsesor(String a) { this.codigoAsesor = a; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public int getContadorVisitas() { return contadorVisitas; }
    public void incrementarVisitas() { this.contadorVisitas++; }
    public int getContadorFavoritos() { return contadorFavoritos; }
    public void incrementarFavoritos() { this.contadorFavoritos++; }
    public void decrementarFavoritos() { if (this.contadorFavoritos > 0) this.contadorFavoritos--; }
    public LocalDate getUltimaModificacion() { return ultimaModificacion; }
 
    @Override
    public String toString() {
        return String.format("[%s] %s - %s, %s | %s | %s | $%.0f | %.0fm² | %d hab | %d baños | %s",
                codigo, tipo, barrio, ciudad, finalidad, estado, precio, area, habitaciones, banos,
                disponible ? "DISPONIBLE" : "NO DISPONIBLE");
    }
}