package main.java.proptech.modelo;

import java.time.LocalDate;
 
public class Operacion {
    private String id;
    private String codigoInmueble;
    private String idCliente;
    private String idAsesor;
    private LocalDate fecha;
    private TipoOperacion tipo;
    private double valorAcordado;
    private double comision;
    private EstadoOperacion estado;
    private LocalDate fechaVencimiento; // para arriendos
 
    public enum TipoOperacion { ARRIENDO, VENTA, RENOVACION_CONTRATO, CANCELACION }
    public enum EstadoOperacion { EN_PROCESO, COMPLETADA, CANCELADA }
 
    public Operacion(String id, String codigoInmueble, String idCliente, String idAsesor,
                     TipoOperacion tipo, double valorAcordado, double comision) {
        this.id = id;
        this.codigoInmueble = codigoInmueble;
        this.idCliente = idCliente;
        this.idAsesor = idAsesor;
        this.tipo = tipo;
        this.valorAcordado = valorAcordado;
        this.comision = comision;
        this.estado = EstadoOperacion.EN_PROCESO;
        this.fecha = LocalDate.now();
    }
 
    public String getId() { return id; }
    public String getCodigoInmueble() { return codigoInmueble; }
    public String getIdCliente() { return idCliente; }
    public String getIdAsesor() { return idAsesor; }
    public LocalDate getFecha() { return fecha; }
    public TipoOperacion getTipo() { return tipo; }
    public double getValorAcordado() { return valorAcordado; }
    public double getComision() { return comision; }
    public EstadoOperacion getEstado() { return estado; }
    public void setEstado(EstadoOperacion e) { this.estado = e; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
 
    @Override
    public String toString() {
        return String.format("[%s] %s | Inmueble:%s | Cliente:%s | $%.0f | Comisión:$%.0f | %s",
                id, tipo, codigoInmueble, idCliente, valorAcordado, comision, estado);
    }
}
