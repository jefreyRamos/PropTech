package main.java.proptech.servicios;

import main.java.proptech.estructuras.*;
import main.java.proptech.modelo.*;
import java.util.*;

/**
 * SERVICIO: Sistema de Alertas y Detección de Comportamiento Inusual
 * Usa ColaPrioridad para procesar alertas por nivel crítico,
 * ListaEnlazada para registro histórico de alertas,
 * Cola para alertas pendientes de revisión.
 */
public class SistemaAlertas {

    // Cola de prioridad: alertas críticas primero
    private final ColaPrioridad<Alerta> colaAlertas;

    // Historial completo (ListaEnlazada)
    private final ListaEnlazada<Alerta> historialAlertas;

    // Cola de alertas pendientes de revisión (FIFO)
    private final Cola<Alerta> pendientesRevision;

    private final GestorInmuebles gestorInmuebles;
    private final GestorClientes gestorClientes;
    private final GestorVisitas gestorVisitas;
    private final GestorOperaciones gestorOperaciones;
    private final GestorAsesores gestorAsesores;

    private int contadorAlerta = 1;

    public SistemaAlertas(GestorInmuebles gi, GestorClientes gc, GestorVisitas gv,
                        GestorOperaciones go, GestorAsesores ga) {
        this.gestorInmuebles = gi;
        this.gestorClientes = gc;
        this.gestorVisitas = gv;
        this.gestorOperaciones = go;
        this.gestorAsesores = ga;
        this.colaAlertas = new ColaPrioridad<>();
        this.historialAlertas = new ListaEnlazada<>();
        this.pendientesRevision = new Cola<>();
    }

    private String generarId() { return String.format("ALT-%04d", contadorAlerta++); }

    /** Genera y encola una alerta. */
    public Alerta generarAlerta(Alerta.TipoAlerta tipo, String descripcion,
                                Alerta.NivelAlerta nivel, String entidad) {
        Alerta alerta = new Alerta(generarId(), tipo, descripcion, nivel, entidad);
        colaAlertas.insertar(alerta);
        historialAlertas.agregarAlFinal(alerta);
        pendientesRevision.encolar(alerta);
        System.out.println("  " + alerta);
        return alerta;
    }

    /** Ejecuta el ciclo completo de detección automática. */
    public void ejecutarDeteccionAutomatica() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║     DETECCIÓN AUTOMÁTICA DE ALERTAS                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        detectarContratosVencer();
        detectarInmueblesSinVisitas();
        detectarAltaDemanda();
        detectarVisitasPendientes();
        detectarClientesSinSeguimiento();
        detectarComportamientoInusual();
    }

    /** Contratos próximos a vencer (15 días). */
    private void detectarContratosVencer() {
        List<Operacion> ops = gestorOperaciones.contratosProximosAVencer(15);
        for (Operacion op : ops) {
            String dias = op.getFechaVencimiento().toString();
            generarAlerta(Alerta.TipoAlerta.CONTRATO_POR_VENCER,
                    "Contrato " + op.getId() + " vence el " + dias,
                    Alerta.NivelAlerta.ALTA, op.getCodigoInmueble());
        }
        if (ops.isEmpty()) System.out.println("  ✓ Sin contratos próximos a vencer.");
    }

    /** Inmuebles publicados hace más de 30 días sin visitas. */
    private void detectarInmueblesSinVisitas() {
        List<Inmueble> sinVisitas = gestorInmuebles.inmueblesConPocoInteres(30);
        for (Inmueble i : sinVisitas) {
            generarAlerta(Alerta.TipoAlerta.INMUEBLE_SIN_VISITAS,
                    "Sin visitas en más de 30 días",
                    Alerta.NivelAlerta.MEDIA, i.getCodigo());
        }
        if (sinVisitas.isEmpty()) System.out.println("  ✓ Todos los inmuebles tienen actividad.");
    }

    /** Inmuebles con más de 5 visitas: alta demanda. */
    private void detectarAltaDemanda() {
        List<Inmueble> demandados = gestorInmuebles.inmueblesAltaDemanda(5);
        for (Inmueble i : demandados) {
            generarAlerta(Alerta.TipoAlerta.ALTA_DEMANDA,
                    "Alta demanda: " + i.getContadorVisitas() + " visitas",
                    Alerta.NivelAlerta.BAJA, i.getCodigo());
        }
    }

    /** Visitas pendientes de confirmar. */
    private void detectarVisitasPendientes() {
        List<Visita> pendientes = gestorVisitas.visitasPendientes();
        for (Visita v : pendientes) {
            generarAlerta(Alerta.TipoAlerta.VISITA_PENDIENTE,
                    "Visita " + v.getId() + " sin confirmar",
                    Alerta.NivelAlerta.MEDIA, v.getCodigoInmueble());
        }
        if (pendientes.isEmpty()) System.out.println("  ✓ Sin visitas pendientes de confirmar.");
    }

    /** Clientes activos sin seguimiento en 7 días. */
    private void detectarClientesSinSeguimiento() {
        List<Cliente> clientes = gestorClientes.clientesSinSeguimiento(7);
        for (Cliente c : clientes) {
            generarAlerta(Alerta.TipoAlerta.CLIENTE_SIN_SEGUIMIENTO,
                    "Sin contacto en más de 7 días",
                    Alerta.NivelAlerta.MEDIA, c.getId());
        }
        if (clientes.isEmpty()) System.out.println("  ✓ Todos los clientes con seguimiento reciente.");
    }

    /** Detecta comportamientos inusuales. */
    private void detectarComportamientoInusual() {
        System.out.println("\n  --- Análisis de Comportamiento Inusual ---");

        // Inmuebles con muchas visitas sin cierre
        List<String> sospechososInmuebles = gestorVisitas.inmueblesConMuchasVisitasSinCierre(4);
        for (String desc : sospechososInmuebles) {
            generarAlerta(Alerta.TipoAlerta.COMPORTAMIENTO_INUSUAL,
                    "Inmueble con alto nro de visitas sin cierre: " + desc,
                    Alerta.NivelAlerta.ALTA, desc.split(" ")[0]);
        }

        // Clientes con visitas excesivas
        List<String> sospechososClientes = gestorVisitas.clientesConVisitasExcesivas(4, 7);
        for (String desc : sospechososClientes) {
            generarAlerta(Alerta.TipoAlerta.COMPORTAMIENTO_INUSUAL,
                    "Cliente con visitas excesivas en poco tiempo: " + desc,
                    Alerta.NivelAlerta.ALTA, desc.split(" ")[0]);
        }

        // Asesores con sobrecarga
        List<Asesor> sobrecargados = gestorAsesores.asesoresConSobrecarga(8);
        for (Asesor a : sobrecargados) {
            generarAlerta(Alerta.TipoAlerta.COMPORTAMIENTO_INUSUAL,
                    "Asesor con sobrecarga: " + a.getVisitasAgendadas() + " visitas activas",
                    Alerta.NivelAlerta.CRITICA, a.getId());
        }

        if (sospechososInmuebles.isEmpty() && sospechososClientes.isEmpty() && sobrecargados.isEmpty()) {
            System.out.println("  ✓ Sin comportamientos comerciales inusuales detectados.");
        }
    }

    /** Procesa la alerta más crítica. Usa COLA DE PRIORIDAD. */
    public Alerta procesarAlertaMasCritica() {
        if (colaAlertas.estaVacia()) return null;
        Alerta a = colaAlertas.extraerMaximo();
        a.marcarRevisada();
        System.out.println("  [PROCESADA] " + a);
        return a;
    }

    /** Lista alertas no revisadas ordenadas por criticidad. */
    public List<Alerta> alertasPendientes() {
        return colaAlertas.listarOrdenado();
    }

    /** Historial completo de alertas. */
    public List<Alerta> getHistorial() {
        return historialAlertas.aLista();
    }

    /** Reporta resumen de alertas. */
    public void imprimirResumen() {
        System.out.println("\n=== RESUMEN DE ALERTAS ===");
        System.out.println("  Total generadas : " + historialAlertas.getTamano());
        System.out.println("  Pendientes      : " + colaAlertas.getTamano());
        Map<Alerta.NivelAlerta, Long> conteo = new HashMap<>();
        for (Alerta a : historialAlertas.aLista()) {
            conteo.merge(a.getNivel(), 1L, Long::sum);
        }
        conteo.forEach((nivel, cnt) -> System.out.println("  " + nivel + ": " + cnt));
    }
}
