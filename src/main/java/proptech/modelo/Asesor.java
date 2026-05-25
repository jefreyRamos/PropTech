package proptech.modelo;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Asesor {

    private final IntegerProperty  id                 = new SimpleIntegerProperty();
    private final StringProperty   codigo             = new SimpleStringProperty();
    private final StringProperty   nombre             = new SimpleStringProperty();
    private final StringProperty   contacto           = new SimpleStringProperty();
    private final StringProperty   especialidad       = new SimpleStringProperty();
    private final StringProperty   zonaAsignada       = new SimpleStringProperty();
    private final IntegerProperty  cierresRealizados  = new SimpleIntegerProperty(0);
    private final DoubleProperty   comisionTotal      = new SimpleDoubleProperty(0);

    public Asesor() {}

    public Asesor(String codigo, String nombre, String contacto,
                  String especialidad, String zonaAsignada) {
        setCodigo(codigo); setNombre(nombre);       setContacto(contacto);
        setEspecialidad(especialidad);              setZonaAsignada(zonaAsignada);
    }

    public int    getId()              { return id.get(); }
    public void   setId(int v)         { id.set(v); }
    public IntegerProperty idProperty(){ return id; }

    public String getCodigo()              { return codigo.get(); }
    public void   setCodigo(String v)      { codigo.set(v); }
    public StringProperty codigoProperty() { return codigo; }

    public String getNombre()              { return nombre.get(); }
    public void   setNombre(String v)      { nombre.set(v); }
    public StringProperty nombreProperty() { return nombre; }

    public String getContacto()              { return contacto.get(); }
    public void   setContacto(String v)      { contacto.set(v); }
    public StringProperty contactoProperty() { return contacto; }

    public String getEspecialidad()              { return especialidad.get(); }
    public void   setEspecialidad(String v)      { especialidad.set(v); }
    public StringProperty especialidadProperty() { return especialidad; }

    public String getZonaAsignada()              { return zonaAsignada.get(); }
    public void   setZonaAsignada(String v)      { zonaAsignada.set(v); }
    public StringProperty zonaAsignadaProperty() { return zonaAsignada; }

    public int    getCierresRealizados()           { return cierresRealizados.get(); }
    public void   setCierresRealizados(int v)      { cierresRealizados.set(v); }
    public IntegerProperty cierresRealizadosProperty(){ return cierresRealizados; }

    public double getComisionTotal()            { return comisionTotal.get(); }
    public void   setComisionTotal(double v)    { comisionTotal.set(v); }
    public DoubleProperty comisionTotalProperty(){ return comisionTotal; }

    @Override
    public String toString() { return getNombre() + " (" + getZonaAsignada() + ")"; }
}