package main.java.proptech.modelo;

import java.util.ArrayList;
import java.util.List;
 
public class Asesor {
    private String id;
    private String nombre;
    private String contacto;
    private String especialidad;
    private String zonaAsignada;
    private List<String> inmueblesAsignados;
    private int visitasAgendadas;
    private int cierresRealizados;
    private double comisionTotal;
 
    public Asesor(String id, String nombre, String contacto, String especialidad, String zonaAsignada) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.especialidad = especialidad;
        this.zonaAsignada = zonaAsignada;
        this.inmueblesAsignados = new ArrayList<>();
        this.visitasAgendadas = 0;
        this.cierresRealizados = 0;
        this.comisionTotal = 0.0;
    }
 
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getContacto() { return contacto; }
    public void setContacto(String c) { this.contacto = c; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String e) { this.especialidad = e; }
    public String getZonaAsignada() { return zonaAsignada; }
    public void setZonaAsignada(String z) { this.zonaAsignada = z; }
    public List<String> getInmueblesAsignados() { return inmueblesAsignados; }
    public void asignarInmueble(String codigo) { inmueblesAsignados.add(codigo); }
    public void desasignarInmueble(String codigo) { inmueblesAsignados.remove(codigo); }
    public int getVisitasAgendadas() { return visitasAgendadas; }
    public void incrementarVisitas() { this.visitasAgendadas++; }
    public void decrementarVisitas() { if (this.visitasAgendadas > 0) this.visitasAgendadas--; }
    public int getCierresRealizados() { return cierresRealizados; }
    public void registrarCierre(double comision) { this.cierresRealizados++; this.comisionTotal += comision; }
    public double getComisionTotal() { return comisionTotal; }
    public double getEfectividad() {
        if (visitasAgendadas + cierresRealizados == 0) return 0;
        return (double) cierresRealizados / (visitasAgendadas + cierresRealizados) * 100;
    }
 
    @Override
    public String toString() {
        return String.format("[%s] %s | Zona: %s | Visitas: %d | Cierres: %d | Efectividad: %.1f%%",
                id, nombre, zonaAsignada, visitasAgendadas, cierresRealizados, getEfectividad());
    }
}
