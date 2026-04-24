package main.java.proptech.modelo;

import java.time.LocalDateTime;
 
public class Visita implements Comparable<Visita> {
    private String id;
    private String idCliente;
    private String codigoInmueble;
    private LocalDateTime fechaHora;
    private String idAsesor;
    private EstadoVisita estado;
    private String observaciones;
    private int prioridad; // 1=normal, 2=alta, 3=VIP urgente
 
    public enum EstadoVisita { PENDIENTE, CONFIRMADA, REALIZADA, CANCELADA, REPROGRAMADA }
 
    public Visita(String id, String idCliente, String codigoInmueble,
                  LocalDateTime fechaHora, String idAsesor, int prioridad) {
        this.id = id;
        this.idCliente = idCliente;
        this.codigoInmueble = codigoInmueble;
        this.fechaHora = fechaHora;
        this.idAsesor = idAsesor;
        this.estado = EstadoVisita.PENDIENTE;
        this.prioridad = prioridad;
        this.observaciones = "";
    }
 
    public String getId() { return id; }
    public String getIdCliente() { return idCliente; }
    public String getCodigoInmueble() { return codigoInmueble; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime f) { this.fechaHora = f; }
    public String getIdAsesor() { return idAsesor; }
    public void setIdAsesor(String a) { this.idAsesor = a; }
    public EstadoVisita getEstado() { return estado; }
    public void setEstado(EstadoVisita e) { this.estado = e; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String o) { this.observaciones = o; }
    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int p) { this.prioridad = p; }
 
    @Override
    public int compareTo(Visita otra) {
        // Mayor prioridad primero; si igual, la más próxima primero
        if (this.prioridad != otra.prioridad) return Integer.compare(otra.prioridad, this.prioridad);
        return this.fechaHora.compareTo(otra.fechaHora);
    }
 
    @Override
    public String toString() {
        return String.format("[%s] Cliente:%s -> Inmueble:%s | %s | Asesor:%s | Estado:%s | Prioridad:%d",
                id, idCliente, codigoInmueble, fechaHora, idAsesor, estado, prioridad);
    }
}
