package main.java.proptech.util;

import main.java.proptech.modelo.*;
import main.java.proptech.servicios.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utilidad: carga datos de demostración en todos los gestores.
 */
public class CargaDatos {

        public static void cargar(GestorInmuebles gi, GestorClientes gc,
                                GestorAsesores ga, GestorVisitas gv,
                                GestorOperaciones go, MotorRecomendaciones mr) {

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         CARGANDO DATOS DE DEMOSTRACIÓN              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        // ── ASESORES ────────────────────────────────────────────────────────
        System.out.println("\n── Asesores ──");
        Asesor a1 = new Asesor("AS-001", "Laura Gómez",   "310-111-2222", "Residencial", "Norte");
        Asesor a2 = new Asesor("AS-002", "Carlos Rueda",  "320-333-4444", "Comercial",   "Centro");
        Asesor a3 = new Asesor("AS-003", "Sofía Vargas",  "315-555-6666", "Premium",     "Occidente");
        ga.registrar(a1); ga.registrar(a2); ga.registrar(a3);
 
        // ── INMUEBLES ────────────────────────────────────────────────────────
        System.out.println("\n── Inmuebles ──");
        Inmueble[] inmuebles = {
            new Inmueble("INM-001","Cra 15 #45-20","Bogotá","Chapinero",
                    Inmueble.TipoInmueble.APARTAMENTO, Inmueble.Finalidad.ARRIENDO,
                    1_800_000, 65, 2, 1, "AS-001"),
            new Inmueble("INM-002","Cl 80 #12-10","Bogotá","Suba",
                    Inmueble.TipoInmueble.CASA, Inmueble.Finalidad.VENTA,
                    350_000_000, 120, 3, 2, "AS-001"),
            new Inmueble("INM-003","Av 68 #25-30","Bogotá","Engativá",
                    Inmueble.TipoInmueble.APARTAMENTO, Inmueble.Finalidad.ARRIENDO,
                    1_200_000, 55, 2, 1, "AS-001"),
            new Inmueble("INM-004","Cl 19 #5-40","Bogotá","La Candelaria",
                    Inmueble.TipoInmueble.LOCAL_COMERCIAL, Inmueble.Finalidad.ARRIENDO,
                    2_500_000, 80, 0, 1, "AS-002"),
            new Inmueble("INM-005","Cra 7 #100-15","Bogotá","Usaquén",
                    Inmueble.TipoInmueble.APARTAMENTO, Inmueble.Finalidad.VENTA,
                    420_000_000, 95, 3, 2, "AS-003"),
            new Inmueble("INM-006","Cl 50 #30-10","Medellín","El Poblado",
                    Inmueble.TipoInmueble.APARTAMENTO, Inmueble.Finalidad.ARRIENDO,
                    2_200_000, 75, 2, 2, "AS-003"),
            new Inmueble("INM-007","Cra 43 #18-50","Medellín","Laureles",
                    Inmueble.TipoInmueble.CASA, Inmueble.Finalidad.VENTA,
                    280_000_000, 145, 4, 3, "AS-002"),
            new Inmueble("INM-008","Cl 10 #85-20","Cali","Granada",
                    Inmueble.TipoInmueble.OFICINA, Inmueble.Finalidad.ARRIENDO,
                    1_900_000, 60, 0, 1, "AS-002"),
            new Inmueble("INM-009","Av 9N #20-10","Cali","San Fernando",
                    Inmueble.TipoInmueble.APARTAMENTO, Inmueble.Finalidad.VENTA,
                    195_000_000, 70, 2, 2, "AS-001"),
            new Inmueble("INM-010","Cl 72 #50-30","Bogotá","Barrios Unidos",
                    Inmueble.TipoInmueble.BODEGA, Inmueble.Finalidad.ARRIENDO,
                    3_500_000, 200, 0, 1, "AS-002"),
        };
        for (Inmueble i : inmuebles) gi.registrar(i);
 
        // Asignar a asesores
        ga.asignarInmueble("AS-001","INM-001"); ga.asignarInmueble("AS-001","INM-002");
        ga.asignarInmueble("AS-001","INM-003"); ga.asignarInmueble("AS-002","INM-004");
        ga.asignarInmueble("AS-003","INM-005"); ga.asignarInmueble("AS-003","INM-006");
        ga.asignarInmueble("AS-002","INM-007"); ga.asignarInmueble("AS-002","INM-008");
 
        // ── CLIENTES ─────────────────────────────────────────────────────────
        System.out.println("\n── Clientes ──");
        Cliente[] clientes = {
            new Cliente("CLI-001","Andrés Morales","andres@email.com","315-100-2000",
                    Cliente.TipoCliente.ARRENDATARIO, 2_000_000,
                    Inmueble.TipoInmueble.APARTAMENTO, 2),
            new Cliente("CLI-002","María Rodríguez","maria@email.com","317-200-3000",
                    Cliente.TipoCliente.COMPRADOR, 400_000_000,
                    Inmueble.TipoInmueble.CASA, 3),
            new Cliente("CLI-003","Juan Pérez","juan@email.com","318-400-5000",
                    Cliente.TipoCliente.INVERSOR, 500_000_000,
                    Inmueble.TipoInmueble.APARTAMENTO, 2),
            new Cliente("CLI-004","Paula Sánchez","paula@email.com","314-600-7000",
                    Cliente.TipoCliente.VIP, 450_000_000,
                    Inmueble.TipoInmueble.APARTAMENTO, 3),
            new Cliente("CLI-005","Ricardo Torres","ricardo@email.com","312-800-9000",
                    Cliente.TipoCliente.COMPRADOR, 300_000_000,
                    Inmueble.TipoInmueble.CASA, 3),
        };
        for (Cliente c : clientes) {
            gc.registrar(c);
        }
        clientes[0].agregarZona("Chapinero"); clientes[0].agregarZona("Engativá");
        clientes[1].agregarZona("Suba");       clientes[1].agregarZona("Usaquén");
        clientes[2].agregarZona("El Poblado"); clientes[2].agregarZona("Chapinero");
        clientes[3].agregarZona("Usaquén");
        clientes[4].agregarZona("Laureles");   clientes[4].agregarZona("Suba");
 
        // ── CONSULTAS E HISTORIAL ────────────────────────────────────────────
        System.out.println("\n── Consultas e historial ──");
        gc.registrarConsulta("CLI-001","INM-001");
        gc.registrarConsulta("CLI-001","INM-003");
        gc.registrarConsulta("CLI-002","INM-002");
        gc.registrarConsulta("CLI-002","INM-005");
        gc.registrarConsulta("CLI-003","INM-005");
        gc.registrarConsulta("CLI-003","INM-006");
        gc.registrarConsulta("CLI-004","INM-005");
        gc.registrarConsulta("CLI-004","INM-001");
        gc.registrarConsulta("CLI-005","INM-007");
 
        gc.agregarFavorito("CLI-001","INM-001");
        gc.agregarFavorito("CLI-002","INM-002");
        gc.agregarFavorito("CLI-003","INM-005");
        gc.agregarFavorito("CLI-004","INM-005");
        gc.agregarFavorito("CLI-005","INM-007");
 
        // ── GRAFO DE RELACIONES ──────────────────────────────────────────────
        mr.registrarVisitaEnGrafo("CLI-001","INM-001");
        mr.registrarVisitaEnGrafo("CLI-001","INM-003");
        mr.registrarVisitaEnGrafo("CLI-002","INM-002");
        mr.registrarVisitaEnGrafo("CLI-002","INM-005");
        mr.registrarVisitaEnGrafo("CLI-003","INM-005");
        mr.registrarVisitaEnGrafo("CLI-003","INM-006");
        mr.registrarVisitaEnGrafo("CLI-004","INM-005");
        mr.registrarVisitaEnGrafo("CLI-004","INM-001");
        mr.registrarFavoritoEnGrafo("CLI-001","INM-001");
        mr.registrarFavoritoEnGrafo("CLI-003","INM-005");
        mr.registrarSimilitud("INM-001","INM-003", 0.85);
        mr.registrarSimilitud("INM-002","INM-007", 0.90);
        mr.registrarSimilitud("INM-005","INM-006", 0.80);
 
        // ── VISITAS ──────────────────────────────────────────────────────────
        System.out.println("\n── Visitas ──");
        LocalDateTime base = LocalDateTime.now().minusDays(5);
        gv.programarVisita("CLI-001","INM-001", base.plusDays(1),"AS-001", 1);
        gv.programarVisita("CLI-002","INM-002", base.plusDays(2),"AS-001", 2); // VIP
        gv.programarVisita("CLI-003","INM-005", base.plusDays(1),"AS-003", 3); // Urgente
        gv.programarVisita("CLI-004","INM-005", base.plusDays(3),"AS-003", 2); // VIP
        gv.programarVisita("CLI-005","INM-007", base.plusDays(2),"AS-002", 1);
        gv.programarVisita("CLI-001","INM-003", base.plusDays(4),"AS-001", 1);
 
        // Registrar visitas en contadores de inmuebles
        gi.registrarVisitaEnInmueble("INM-001"); gi.registrarVisitaEnInmueble("INM-001");
        gi.registrarVisitaEnInmueble("INM-002"); gi.registrarVisitaEnInmueble("INM-003");
        gi.registrarVisitaEnInmueble("INM-005"); gi.registrarVisitaEnInmueble("INM-005");
        gi.registrarVisitaEnInmueble("INM-005"); gi.registrarVisitaEnInmueble("INM-005");
        gi.registrarVisitaEnInmueble("INM-005"); gi.registrarVisitaEnInmueble("INM-006");
        gi.registrarVisitaEnInmueble("INM-007"); gi.registrarVisitaEnInmueble("INM-007");
 
        // Actualizar carga de asesores
        ga.registrarVisitaAsignada("AS-001"); ga.registrarVisitaAsignada("AS-001");
        ga.registrarVisitaAsignada("AS-001"); ga.registrarVisitaAsignada("AS-002");
        ga.registrarVisitaAsignada("AS-003"); ga.registrarVisitaAsignada("AS-003");
 
        // ── OPERACIONES ──────────────────────────────────────────────────────
        System.out.println("\n── Operaciones ──");
        Operacion op1 = go.registrar("INM-001","CLI-001","AS-001",
                Operacion.TipoOperacion.ARRIENDO, 1_800_000, 90_000);
        op1.setFechaVencimiento(LocalDate.now().plusDays(10)); // Por vencer
        go.registrar("INM-007","CLI-005","AS-002",
                Operacion.TipoOperacion.VENTA, 280_000_000, 8_400_000);
        Operacion op3 = go.registrar("INM-006","CLI-003","AS-003",
                Operacion.TipoOperacion.ARRIENDO, 2_200_000, 110_000);
        op3.setFechaVencimiento(LocalDate.now().plusDays(60));
 
        // Registrar cierres en asesores
        ga.registrarCierre("AS-001", 90_000);
        ga.registrarCierre("AS-002", 8_400_000);
 
        System.out.println("\n  [OK] Datos de demostración cargados exitosamente.");
    }
}
