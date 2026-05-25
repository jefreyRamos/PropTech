package proptech.modelo;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cliente {

    public enum Tipo   { COMPRADOR, ARRENDATARIO, INVERSOR, VIP }
    public enum Estado { ACTIVO, EN_NEGOCIACION, PAUSADO, CERRADO }

    private final IntegerProperty          id                    = new SimpleIntegerProperty();
    private final StringProperty           identificacion        = new SimpleStringProperty();
    private final StringProperty           nombre                = new SimpleStringProperty();
    private final StringProperty           correo                = new SimpleStringProperty();
    private final StringProperty           telefono              = new SimpleStringProperty();
    private final ObjectProperty<Tipo>     tipo                  = new SimpleObjectProperty<>();
    private final DoubleProperty           presupuesto           = new SimpleDoubleProperty();
    private final StringProperty           zonaInteres           = new SimpleStringProperty();
    private final StringProperty           tipoInmuebleDeseado   = new SimpleStringProperty();
    private final IntegerProperty          minHabitaciones       = new SimpleIntegerProperty();
    private final ObjectProperty<Estado>   estadoBusqueda        = new SimpleObjectProperty<>(Estado.ACTIVO);

    public Cliente() {}

    public Cliente(String identificacion, String nombre, String correo, String telefono,
                   Tipo tipo, double presupuesto, String zonaInteres,
                   String tipoInmuebleDeseado, int minHabitaciones) {
        setIdentificacion(identificacion); setNombre(nombre);       setCorreo(correo);
        setTelefono(telefono);             setTipo(tipo);           setPresupuesto(presupuesto);
        setZonaInteres(zonaInteres);       setTipoInmuebleDeseado(tipoInmuebleDeseado);
        setMinHabitaciones(minHabitaciones);
    }

    public int     getId()                { return id.get(); }
    public void    setId(int v)           { id.set(v); }
    public IntegerProperty idProperty()   { return id; }

    public String  getIdentificacion()              { return identificacion.get(); }
    public void    setIdentificacion(String v)      { identificacion.set(v); }
    public StringProperty identificacionProperty()  { return identificacion; }

    public String  getNombre()              { return nombre.get(); }
    public void    setNombre(String v)      { nombre.set(v); }
    public StringProperty nombreProperty()  { return nombre; }

    public String  getCorreo()              { return correo.get(); }
    public void    setCorreo(String v)      { correo.set(v); }
    public StringProperty correoProperty()  { return correo; }

    public String  getTelefono()              { return telefono.get(); }
    public void    setTelefono(String v)      { telefono.set(v); }
    public StringProperty telefonoProperty()  { return telefono; }

    public Tipo    getTipo()                { return tipo.get(); }
    public void    setTipo(Tipo v)          { tipo.set(v); }
    public ObjectProperty<Tipo> tipoProperty(){ return tipo; }

    public double  getPresupuesto()           { return presupuesto.get(); }
    public void    setPresupuesto(double v)   { presupuesto.set(v); }
    public DoubleProperty presupuestoProperty(){ return presupuesto; }

    public String  getZonaInteres()             { return zonaInteres.get(); }
    public void    setZonaInteres(String v)     { zonaInteres.set(v); }
    public StringProperty zonaInteresProperty() { return zonaInteres; }

    public String  getTipoInmuebleDeseado()              { return tipoInmuebleDeseado.get(); }
    public void    setTipoInmuebleDeseado(String v)      { tipoInmuebleDeseado.set(v); }
    public StringProperty tipoInmuebleDeseadoProperty()  { return tipoInmuebleDeseado; }

    public int     getMinHabitaciones()           { return minHabitaciones.get(); }
    public void    setMinHabitaciones(int v)      { minHabitaciones.set(v); }
    public IntegerProperty minHabitacionesProperty(){ return minHabitaciones; }

    public Estado  getEstadoBusqueda()                  { return estadoBusqueda.get(); }
    public void    setEstadoBusqueda(Estado v)          { estadoBusqueda.set(v); }
    public ObjectProperty<Estado> estadoBusquedaProperty(){ return estadoBusqueda; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | $%,.0f",
                getIdentificacion(), getNombre(), getTipo(), getPresupuesto());
    }
}