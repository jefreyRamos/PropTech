package main.java.proptech.modelo;

import java.time.LocalDateTime;
 
public class Alerta implements Comparable<Alerta> {
    private String id;
    private TipoAlerta tipo;
    private String descripcion;
    private NivelAlerta nivel;
    private String entidadRelacionada; // código inmueble, id cliente, etc.
    private LocalDateTime fechaGeneracion;
    private boolean revisada;
 
    public enum TipoAlerta {
        CONTRATO_POR_VENCER,
        INMUEBLE_SIN_VISITAS,
        ALTA_DEMANDA,
        VISITA_PENDIENTE,
        RESERVA_EXTENSA,
        CLIENTE_SIN_SEGUIMIENTO,
        COMPORTAMIENTO_INUSUAL
    }
 
    public enum NivelAlerta { BAJA, MEDIA, ALTA, CRITICA }
 
    public Alerta(String id, TipoAlerta tipo, String descripcion, NivelAlerta nivel, String entidadRelacionada) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.entidadRelacionada = entidadRelacionada;
        this.fechaGeneracion = LocalDateTime.now();
        this.revisada = false;
    }
 
    public String getId() { return id; }
    public TipoAlerta getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public NivelAlerta getNivel() { return nivel; }
    public String getEntidadRelacionada() { return entidadRelacionada; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public boolean isRevisada() { return revisada; }
    public void marcarRevisada() { this.revisada = true; }
 
    @Override
    public int compareTo(Alerta otra) {
        // Mayor nivel primero
        return Integer.compare(otra.nivel.ordinal(), this.nivel.ordinal());
    }
 
    @Override
    public String toString() {
        String icono = switch (nivel) {
            case CRITICA -> "🔴";
            case ALTA    -> "🟠";
            case MEDIA   -> "🟡";
            case BAJA    -> "🟢";
        };
        return String.format("%s [%s] %s | Entidad:%s | %s%s",
                icono, nivel, tipo, entidadRelacionada, descripcion,
                revisada ? " [REVISADA]" : "");
    }
}