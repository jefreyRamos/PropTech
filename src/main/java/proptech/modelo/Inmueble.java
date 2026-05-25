package proptech.modelo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Inmueble {

    public enum Tipo     { APARTAMENTO, CASA, LOCAL_COMERCIAL, OFICINA, LOTE, BODEGA }
    public enum Finalidad{ VENTA, ARRIENDO }
    public enum Estado   { DISPONIBLE, RESERVADO, ARRENDADO, VENDIDO }

    private final IntegerProperty         id               = new SimpleIntegerProperty();
    private final StringProperty          codigo           = new SimpleStringProperty();
    private final StringProperty          direccion        = new SimpleStringProperty();
    private final StringProperty          ciudad           = new SimpleStringProperty();
    private final StringProperty          barrio           = new SimpleStringProperty();
    private final ObjectProperty<Tipo>    tipo             = new SimpleObjectProperty<>();
    private final ObjectProperty<Finalidad> finalidad      = new SimpleObjectProperty<>();
    private final DoubleProperty          precio           = new SimpleDoubleProperty();
    private final DoubleProperty          area             = new SimpleDoubleProperty();
    private final IntegerProperty         habitaciones     = new SimpleIntegerProperty();
    private final IntegerProperty         banos            = new SimpleIntegerProperty();
    private final ObjectProperty<Estado>  estado           = new SimpleObjectProperty<>();
    private final BooleanProperty         disponible       = new SimpleBooleanProperty(true);
    private final StringProperty          codigoAsesor     = new SimpleStringProperty();
    private final IntegerProperty         contadorVisitas  = new SimpleIntegerProperty(0);

    public Inmueble() {}

    public Inmueble(String codigo, String direccion, String ciudad, String barrio,
                    Tipo tipo, Finalidad finalidad, double precio, double area,
                    int habitaciones, int banos, Estado estado, String codigoAsesor) {
        setCodigo(codigo);         setDireccion(direccion);   setCiudad(ciudad);
        setBarrio(barrio);         setTipo(tipo);             setFinalidad(finalidad);
        setPrecio(precio);         setArea(area);             setHabitaciones(habitaciones);
        setBanos(banos);           setEstado(estado);         setCodigoAsesor(codigoAsesor);
        setDisponible(estado == Estado.DISPONIBLE);
    }

    // ── id ────────────────────────────────────────────────────────────────
    public int     getId()            { return id.get(); }
    public void    setId(int v)       { id.set(v); }
    public IntegerProperty idProperty(){ return id; }

    // ── codigo ─────────────────────────────────────────────────────────────
    public String  getCodigo()          { return codigo.get(); }
    public void    setCodigo(String v)  { codigo.set(v); }
    public StringProperty codigoProperty(){ return codigo; }

    // ── direccion ──────────────────────────────────────────────────────────
    public String  getDireccion()         { return direccion.get(); }
    public void    setDireccion(String v) { direccion.set(v); }
    public StringProperty direccionProperty(){ return direccion; }

    // ── ciudad ─────────────────────────────────────────────────────────────
    public String  getCiudad()          { return ciudad.get(); }
    public void    setCiudad(String v)  { ciudad.set(v); }
    public StringProperty ciudadProperty(){ return ciudad; }

    // ── barrio ─────────────────────────────────────────────────────────────
    public String  getBarrio()          { return barrio.get(); }
    public void    setBarrio(String v)  { barrio.set(v); }
    public StringProperty barrioProperty(){ return barrio; }

    // ── tipo ───────────────────────────────────────────────────────────────
    public Tipo    getTipo()            { return tipo.get(); }
    public void    setTipo(Tipo v)      { tipo.set(v); }
    public ObjectProperty<Tipo> tipoProperty(){ return tipo; }

    // ── finalidad ──────────────────────────────────────────────────────────
    public Finalidad getFinalidad()         { return finalidad.get(); }
    public void      setFinalidad(Finalidad v){ finalidad.set(v); }
    public ObjectProperty<Finalidad> finalidadProperty(){ return finalidad; }

    // ── precio ─────────────────────────────────────────────────────────────
    public double  getPrecio()          { return precio.get(); }
    public void    setPrecio(double v)  { precio.set(v); }
    public DoubleProperty precioProperty(){ return precio; }

    // ── area ───────────────────────────────────────────────────────────────
    public double  getArea()            { return area.get(); }
    public void    setArea(double v)    { area.set(v); }
    public DoubleProperty areaProperty(){ return area; }

    // ── habitaciones ───────────────────────────────────────────────────────
    public int     getHabitaciones()        { return habitaciones.get(); }
    public void    setHabitaciones(int v)   { habitaciones.set(v); }
    public IntegerProperty habitacionesProperty(){ return habitaciones; }

    // ── banos ──────────────────────────────────────────────────────────────
    public int     getBanos()           { return banos.get(); }
    public void    setBanos(int v)      { banos.set(v); }
    public IntegerProperty banosProperty(){ return banos; }

    // ── estado ─────────────────────────────────────────────────────────────
    public Estado  getEstado()          { return estado.get(); }
    public void    setEstado(Estado v)  { estado.set(v); if (v != null) setDisponible(v == Estado.DISPONIBLE); }
    public ObjectProperty<Estado> estadoProperty(){ return estado; }

    // ── disponible ─────────────────────────────────────────────────────────
    public boolean isDisponible()         { return disponible.get(); }
    public void    setDisponible(boolean v){ disponible.set(v); }
    public BooleanProperty disponibleProperty(){ return disponible; }

    // ── codigoAsesor ───────────────────────────────────────────────────────
    public String  getCodigoAsesor()          { return codigoAsesor.get(); }
    public void    setCodigoAsesor(String v)  { codigoAsesor.set(v); }
    public StringProperty codigoAsesorProperty(){ return codigoAsesor; }

    // ── contadorVisitas ────────────────────────────────────────────────────
    public int     getContadorVisitas()       { return contadorVisitas.get(); }
    public void    setContadorVisitas(int v)  { contadorVisitas.set(v); }
    public IntegerProperty contadorVisitasProperty(){ return contadorVisitas; }

    @Override
    public String toString() {
        return String.format("[%s] %s — %s, %s | %s | $%,.0f",
                getCodigo(), getTipo(), getBarrio(), getCiudad(), getFinalidad(), getPrecio());
    }
}