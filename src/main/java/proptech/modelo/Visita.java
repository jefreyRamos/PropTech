package proptech.modelo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Visita {

    public enum Estado   { PENDIENTE, CONFIRMADA, REALIZADA, CANCELADA, REPROGRAMADA }
    public enum Prioridad{ NORMAL, ALTA, VIP }

    private final IntegerProperty           id                 = new SimpleIntegerProperty();
    private final StringProperty            idCliente          = new SimpleStringProperty();
    private final StringProperty            codigoInmueble     = new SimpleStringProperty();
    private final StringProperty            fechaHora          = new SimpleStringProperty();
    private final StringProperty            idAsesor           = new SimpleStringProperty();
    private final ObjectProperty<Estado>    estado             = new SimpleObjectProperty<>(Estado.PENDIENTE);
    private final ObjectProperty<Prioridad> prioridad          = new SimpleObjectProperty<>(Prioridad.NORMAL);
    private final StringProperty            observaciones      = new SimpleStringProperty("");
    // Campos de display (JOIN desde BD)
    private final StringProperty            nombreCliente      = new SimpleStringProperty("");
    private final StringProperty            direccionInmueble  = new SimpleStringProperty("");
    private final StringProperty            nombreAsesor       = new SimpleStringProperty("");

    public Visita() {}

    public Visita(String idCliente, String codigoInmueble, String fechaHora,
                  String idAsesor, Prioridad prioridad) {
        setIdCliente(idCliente); setCodigoInmueble(codigoInmueble);
        setFechaHora(fechaHora); setIdAsesor(idAsesor); setPrioridad(prioridad);
    }

    public int    getId()              { return id.get(); }
    public void   setId(int v)         { id.set(v); }
    public IntegerProperty idProperty(){ return id; }

    public String getIdCliente()              { return idCliente.get(); }
    public void   setIdCliente(String v)      { idCliente.set(v); }
    public StringProperty idClienteProperty() { return idCliente; }

    public String getCodigoInmueble()              { return codigoInmueble.get(); }
    public void   setCodigoInmueble(String v)      { codigoInmueble.set(v); }
    public StringProperty codigoInmuebleProperty() { return codigoInmueble; }

    public String getFechaHora()              { return fechaHora.get(); }
    public void   setFechaHora(String v)      { fechaHora.set(v); }
    public StringProperty fechaHoraProperty() { return fechaHora; }

    public String getIdAsesor()              { return idAsesor.get(); }
    public void   setIdAsesor(String v)      { idAsesor.set(v); }
    public StringProperty idAsesorProperty() { return idAsesor; }

    public Estado  getEstado()                  { return estado.get(); }
    public void    setEstado(Estado v)          { estado.set(v); }
    public ObjectProperty<Estado> estadoProperty(){ return estado; }

    public Prioridad getPrioridad()                    { return prioridad.get(); }
    public void      setPrioridad(Prioridad v)         { prioridad.set(v); }
    public ObjectProperty<Prioridad> prioridadProperty(){ return prioridad; }

    public String getObservaciones()              { return observaciones.get(); }
    public void   setObservaciones(String v)      { observaciones.set(v != null ? v : ""); }
    public StringProperty observacionesProperty() { return observaciones; }

    public String getNombreCliente()              { return nombreCliente.get(); }
    public void   setNombreCliente(String v)      { nombreCliente.set(v != null ? v : ""); }
    public StringProperty nombreClienteProperty() { return nombreCliente; }

    public String getDireccionInmueble()              { return direccionInmueble.get(); }
    public void   setDireccionInmueble(String v)      { direccionInmueble.set(v != null ? v : ""); }
    public StringProperty direccionInmuebleProperty() { return direccionInmueble; }

    public String getNombreAsesor()              { return nombreAsesor.get(); }
    public void   setNombreAsesor(String v)      { nombreAsesor.set(v != null ? v : ""); }
    public StringProperty nombreAsesorProperty() { return nombreAsesor; }
}