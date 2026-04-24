package main.java.proptech;

import main.java.proptech.servicios.*;
import main.java.proptech.util.*;
 
/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║           PROPTECH - Plataforma Inmobiliaria Inteligente     ║
 * ║                  Proyecto Final - Estructuras de Datos       ║
 * ╚═══════════════════════════════════════════════════════════════╝
 *
 * ESTRUCTURAS UTILIZADAS:
 *  ✦ Lista Enlazada  → Historial de visitas, consultas, favoritos
 *  ✦ Pila            → Deshacer cambios en inmuebles
 *  ✦ Cola FIFO       → Atención de clientes, visitas estándar
 *  ✦ Cola Prioridad  → Visitas VIP/urgentes, alertas críticas
 *  ✦ Tabla Hash      → Búsqueda O(1) de clientes, inmuebles, asesores
 *  ✦ Árbol BST       → Ordenamiento y rangos por precio/presupuesto/cierres
 *  ✦ Grafo           → Relaciones cliente-inmueble, similitud, BFS/DFS
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   PROPTECH - Sistema de Gestión Inmobiliaria        ║");
        System.out.println("║               Iniciando plataforma...               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        // ── Instanciar gestores (inyección de dependencias manual) ──────────
        GestorInmuebles gestorInmuebles = new GestorInmuebles();
        GestorClientes  gestorClientes  = new GestorClientes();
        GestorAsesores  gestorAsesores  = new GestorAsesores();
        GestorVisitas   gestorVisitas   = new GestorVisitas();
        GestorOperaciones gestorOps     = new GestorOperaciones();

        // Motor de recomendaciones necesita referencias a los gestores
        MotorRecomendaciones motor = new MotorRecomendaciones(gestorInmuebles, gestorClientes);

        // Sistema de alertas necesita acceso a todos los gestores
        SistemaAlertas alertas = new SistemaAlertas(
                gestorInmuebles, gestorClientes, gestorVisitas, gestorOps, gestorAsesores);

        // ── Cargar datos de demostración ─────────────────────────────────────
        CargaDatos.cargar(gestorInmuebles, gestorClientes, gestorAsesores,
                        gestorVisitas, gestorOps, motor);

        // ── Demostración automática de estructuras de datos ──────────────────
        //DemoEstructuras.ejecutar(gestorInmuebles, gestorClientes, gestorAsesores,
                                //500_000_000gestorVisitas, gestorOps, alertas, motor);

        // ── Iniciar consola interactiva ───────────────────────────────────────
        Consola consola = new Consola(
                gestorInmuebles, gestorClientes, gestorAsesores,
                gestorVisitas, gestorOps, alertas, motor);
        consola.iniciar();
    }
}
