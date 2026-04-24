package main.java.proptech.util;

import main.java.proptech.modelo.*;
import main.java.proptech.servicios.*;
import java.time.LocalDateTime;
import java.util.*;
 
/**
 * Interfaz de consola interactiva para la plataforma PropTech.
 */
public class Consola {
 
    private final GestorInmuebles gi;
    private final GestorClientes gc;
    private final GestorAsesores ga;
    private final GestorVisitas gv;
    private final GestorOperaciones go;
    private final SistemaAlertas sa;
    private final MotorRecomendaciones mr;
    private final Scanner scanner = new Scanner(System.in);
 
    public Consola(GestorInmuebles gi, GestorClientes gc, GestorAsesores ga,
                   GestorVisitas gv, GestorOperaciones go, SistemaAlertas sa, MotorRecomendaciones mr) {
        this.gi = gi; this.gc = gc; this.ga = ga;
        this.gv = gv; this.go = go; this.sa = sa; this.mr = mr;
    }
 
    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            menuPrincipal();
            int op = leerInt();
            switch (op) {
                case 1  -> menuInmuebles();
                case 2  -> menuClientes();
                case 3  -> menuVisitas();
                case 4  -> menuOperaciones();
                case 5  -> menuAlertas();
                case 6  -> menuRecomendaciones();
                case 7  -> menuReportes();
                case 8  -> menuGrafo();
                case 0  -> salir = true;
                default -> System.out.println("  Opción inválida.");
            }
        }
        System.out.println("\n  ¡Hasta luego! PropTech cerrado.");
    }
 
    // ──────────────────────────────────────────────────
    private void menuPrincipal() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         PROPTECH - PLATAFORMA INMOBILIARIA          ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  1. Gestión de Inmuebles                            ║");
        System.out.println("║  2. Gestión de Clientes                             ║");
        System.out.println("║  3. Visitas                                         ║");
        System.out.println("║  4. Operaciones (arriendos/ventas)                  ║");
        System.out.println("║  5. Alertas automáticas                             ║");
        System.out.println("║  6. Recomendaciones                                 ║");
        System.out.println("║  7. Reportes y estadísticas                         ║");
        System.out.println("║  8. Análisis de Grafo                               ║");
        System.out.println("║  0. Salir                                           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.print("  Seleccione: ");
    }
 
    // ──────────────────────────────────────────────────
    private void menuInmuebles() {
        System.out.println("\n─── INMUEBLES ───");
        System.out.println("  1. Listar todos");
        System.out.println("  2. Buscar por código");
        System.out.println("  3. Buscar por rango de precio (BST)");
        System.out.println("  4. Filtro combinado");
        System.out.println("  5. Ordenados por precio (BST InOrden)");
        System.out.println("  6. Modificar precio (con Pila de deshacer)");
        System.out.println("  7. Deshacer último cambio (Pila)");
        System.out.println("  8. Ranking de zonas");
        System.out.print("  Opción: ");
        int op = leerInt();
        switch (op) {
            case 1 -> gi.getTodos().forEach(System.out::println);
            case 2 -> {
                System.out.print("  Código: ");
                Inmueble i = gi.buscarPorCodigo(scanner.nextLine().trim());
                System.out.println(i == null ? "  No encontrado." : i);
            }
            case 3 -> {
                System.out.print("  Precio mínimo: ");  double min = leerDouble();
                System.out.print("  Precio máximo: ");  double max = leerDouble();
                List<Inmueble> r = gi.buscarPorRangoPrecio(min, max);
                System.out.println("  Encontrados: " + r.size());
                r.forEach(System.out::println);
            }
            case 4 -> {
                System.out.print("  Tipo (APARTAMENTO/CASA/LOCAL_COMERCIAL/OFICINA/LOTE/BODEGA, vacío=todos): ");
                String t = scanner.nextLine().trim();
                System.out.print("  Finalidad (VENTA/ARRIENDO, vacío=todos): ");
                String f = scanner.nextLine().trim();
                System.out.print("  Presupuesto máximo (0=sin límite): ");  double p = leerDouble();
                System.out.print("  Mínimo habitaciones (0=sin límite): "); int h = leerInt();
                System.out.print("  Ciudad (vacío=todas): ");               String c = scanner.nextLine().trim();
                Inmueble.TipoInmueble tipo = t.isBlank() ? null : Inmueble.TipoInmueble.valueOf(t);
                Inmueble.Finalidad fin = f.isBlank() ? null : Inmueble.Finalidad.valueOf(f);
                gi.buscarConFiltros(tipo, fin, p, h, c).forEach(System.out::println);
            }
            case 5 -> {
                System.out.println("  (BST InOrden - menor a mayor precio):");
                gi.ordenadosPorPrecio().forEach(i -> System.out.printf("  $%,.0f - %s%n", i.getPrecio(), i.getCodigo()));
            }
            case 6 -> {
                System.out.print("  Código inmueble: ");   String cod = scanner.nextLine().trim();
                System.out.print("  Nuevo precio: ");       double np = leerDouble();
                gi.modificarPrecio(cod, np);
            }
            case 7 -> gi.deshacerUltimoCambio();
            case 8 -> {
                System.out.println("  Ranking de zonas (TablaHash de frecuencias):");
                gi.rankingZonas().forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue() + " visitas"));
            }
        }
    }
 
    // ──────────────────────────────────────────────────
    private void menuClientes() {
        System.out.println("\n─── CLIENTES ───");
        System.out.println("  1. Listar todos");
        System.out.println("  2. Buscar por ID (TablaHash O(1))");
        System.out.println("  3. Ver historial de consultas");
        System.out.println("  4. Ver favoritos");
        System.out.println("  5. Agregar favorito");
        System.out.println("  6. Ordenar por presupuesto (BST)");
        System.out.println("  7. Clientes con alta probabilidad de cierre");
        System.out.println("  8. Encolar cliente para atención (Cola FIFO)");
        System.out.println("  9. Atender siguiente cliente");
        System.out.print("  Opción: ");
        int op = leerInt();
        switch (op) {
            case 1 -> gc.getTodos().forEach(System.out::println);
            case 2 -> {
                System.out.print("  ID: ");
                Cliente c = gc.buscarPorId(scanner.nextLine().trim());
                System.out.println(c == null ? "  No encontrado." : c);
            }
            case 3 -> {
                System.out.print("  ID Cliente: ");
                gc.getHistorialConsultas(scanner.nextLine().trim())
                  .forEach(cod -> System.out.println("  -> " + cod));
            }
            case 4 -> {
                System.out.print("  ID Cliente: ");
                gc.getFavoritos(scanner.nextLine().trim())
                  .forEach(cod -> System.out.println("  ★ " + cod));
            }
            case 5 -> {
                System.out.print("  ID Cliente: ");  String cli = scanner.nextLine().trim();
                System.out.print("  Código inmueble: "); String inm = scanner.nextLine().trim();
                gc.agregarFavorito(cli, inm);
                mr.registrarFavoritoEnGrafo(cli, inm);
            }
            case 6 -> {
                System.out.println("  (BST InOrden - menor a mayor presupuesto):");
                gc.ordenadosPorPresupuesto()
                  .forEach(c -> System.out.printf("  $%,.0f - %s%n", c.getPresupuesto(), c.getNombre()));
            }
            case 7 -> gc.clientesConAltaProbabilidadCierre().forEach(System.out::println);
            case 8 -> {
                System.out.print("  ID Cliente: ");
                gc.encolarAtencion(scanner.nextLine().trim());
            }
            case 9 -> {
                String id = gc.atenderSiguiente();
                if (id == null) System.out.println("  Cola de atención vacía.");
                else { Cliente c = gc.buscarPorId(id); System.out.println("  Atendiendo: " + c); }
            }
        }
    }
 
    // ──────────────────────────────────────────────────
    private void menuVisitas() {
        System.out.println("\n─── VISITAS ───");
        System.out.println("  1. Programar visita");
        System.out.println("  2. Procesar siguiente urgente (ColaPrioridad)");
        System.out.println("  3. Procesar siguiente estándar (Cola FIFO)");
        System.out.println("  4. Ver historial de cliente");
        System.out.println("  5. Ver visitas por inmueble");
        System.out.println("  6. Actualizar estado de visita");
        System.out.println("  7. Cancelar visita");
        System.out.print("  Opción: ");
        int op = leerInt();
        switch (op) {
            case 1 -> {
                System.out.print("  ID Cliente: ");     String cli = scanner.nextLine().trim();
                System.out.print("  Código inmueble: "); String inm = scanner.nextLine().trim();
                System.out.print("  Asesor ID: ");       String as = scanner.nextLine().trim();
                System.out.print("  Prioridad (1=normal, 2=VIP, 3=urgente): "); int pr = leerInt();
                Visita v = gv.programarVisita(cli, inm,
                        LocalDateTime.now().plusDays(2), as, pr);
                gi.registrarVisitaEnInmueble(inm);
                mr.registrarVisitaEnGrafo(cli, inm);
                ga.registrarVisitaAsignada(as);
            }
            case 2 -> {
                Visita v = gv.procesarSiguienteUrgente();
                System.out.println(v == null ? "  No hay visitas urgentes." : "  Procesando: " + v);
            }
            case 3 -> {
                Visita v = gv.procesarSiguienteEstandar();
                System.out.println(v == null ? "  No hay visitas estándar." : "  Procesando: " + v);
            }
            case 4 -> {
                System.out.print("  ID Cliente: ");
                gv.getHistorialCliente(scanner.nextLine().trim()).forEach(System.out::println);
            }
            case 5 -> {
                System.out.print("  Código inmueble: ");
                gv.getVisitasPorInmueble(scanner.nextLine().trim()).forEach(System.out::println);
            }
            case 6 -> {
                System.out.print("  ID Visita: ");   String id = scanner.nextLine().trim();
                System.out.print("  Estado (PENDIENTE/CONFIRMADA/REALIZADA/CANCELADA/REPROGRAMADA): ");
                String est = scanner.nextLine().trim();
                System.out.print("  Observaciones: "); String obs = scanner.nextLine();
                gv.actualizarEstado(id, Visita.EstadoVisita.valueOf(est), obs);
            }
            case 7 -> {
                System.out.print("  ID Visita: ");  String id = scanner.nextLine().trim();
                System.out.print("  Motivo: ");     String m = scanner.nextLine();
                gv.cancelar(id, m);
            }
        }
    }
 
    // ──────────────────────────────────────────────────
    private void menuOperaciones() {
        System.out.println("\n─── OPERACIONES ───");
        System.out.println("  1. Registrar operación");
        System.out.println("  2. Ver contratos próximos a vencer");
        System.out.println("  3. Listar todas las operaciones");
        System.out.print("  Opción: ");
        int op = leerInt();
        switch (op) {
            case 1 -> {
                System.out.print("  Inmueble: ");   String inm = scanner.nextLine().trim();
                System.out.print("  Cliente: ");    String cli = scanner.nextLine().trim();
                System.out.print("  Asesor: ");     String as  = scanner.nextLine().trim();
                System.out.print("  Tipo (ARRIENDO/VENTA/RENOVACION_CONTRATO): "); String t = scanner.nextLine().trim();
                System.out.print("  Valor: ");      double val = leerDouble();
                System.out.print("  Comisión: ");   double com = leerDouble();
                go.registrar(inm, cli, as, Operacion.TipoOperacion.valueOf(t), val, com);
            }
            case 2 -> {
                System.out.println("  Contratos vencen en los próximos 30 días:");
                go.contratosProximosAVencer(30).forEach(System.out::println);
            }
            case 3 -> go.getTodas().forEach(System.out::println);
        }
    }
 
    // ──────────────────────────────────────────────────
    private void menuAlertas() {
        sa.ejecutarDeteccionAutomatica();
        sa.imprimirResumen();
        System.out.println("\n  1. Procesar alerta más crítica (ColaPrioridad)");
        System.out.println("  2. Ver historial de alertas");
        System.out.print("  Opción (Enter para salir): ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op)) sa.procesarAlertaMasCritica();
        else if ("2".equals(op)) sa.getHistorial().forEach(System.out::println);
    }
 
    // ──────────────────────────────────────────────────
    private void menuRecomendaciones() {
        System.out.println("\n─── RECOMENDACIONES ───");
        System.out.println("  1. Recomendar inmuebles para un cliente");
        System.out.println("  2. Inmuebles similares a uno dado");
        System.out.println("  3. Clientes con intereses similares");
        System.out.print("  Opción: ");
        int op = leerInt();
        switch (op) {
            case 1 -> {
                System.out.print("  ID Cliente: ");
                String id = scanner.nextLine().trim();
                Cliente c = gc.buscarPorId(id);
                if (c == null) { System.out.println("  No encontrado."); return; }
                System.out.println("  Recomendaciones para " + c.getNombre() + " (presupuesto $" + c.getPresupuesto() + "):");
                List<Inmueble> recs = mr.recomendar(id, 5);
                if (recs.isEmpty()) System.out.println("  Sin recomendaciones por ahora.");
                else recs.forEach(i -> System.out.println("  ▶ " + i));
            }
            case 2 -> {
                System.out.print("  Código inmueble: ");
                String cod = scanner.nextLine().trim();
                mr.inmueblesSimulares(cod, 5).forEach(i -> System.out.println("  ≈ " + i));
            }
            case 3 -> {
                System.out.print("  ID Cliente: ");
                String id = scanner.nextLine().trim();
                Map<String, Integer> sim = mr.clientesSimilares(id);
                if (sim.isEmpty()) System.out.println("  Sin clientes con intereses comunes en el grafo.");
                else sim.forEach((cid, cnt) -> System.out.println("  " + cid + " -> " + cnt + " inmueble(s) en común"));
            }
        }
    }
 
    // ──────────────────────────────────────────────────
    private void menuReportes() {
        System.out.println("\n─── REPORTES ───");
        System.out.println("  1. Ranking de asesores por cierres (BST)");
        System.out.println("  2. Inmuebles más demandados");
        System.out.println("  3. Ranking de zonas (TablaHash)");
        System.out.println("  4. Clientes con alta probabilidad de cierre");
        System.out.println("  5. Asesores con sobrecarga");
        int op = leerInt();
        switch (op) {
            case 1 -> {
                System.out.println("  Ranking asesores:");
                int[] pos = {1};
                ga.rankingPorCierres().forEach(a -> System.out.printf("  %d. %s%n", pos[0]++, a));
            }
            case 2 -> {
                System.out.println("  Inmuebles con mayor demanda:");
                gi.inmueblesAltaDemanda(1).forEach(i ->
                        System.out.printf("  %s - %d visitas%n", i.getCodigo(), i.getContadorVisitas()));
            }
            case 3 -> gi.rankingZonas().forEach(e ->
                    System.out.println("  " + e.getKey() + ": " + e.getValue() + " visitas"));
            case 4 -> gc.clientesConAltaProbabilidadCierre().forEach(System.out::println);
            case 5 -> ga.asesoresConSobrecarga(1).forEach(System.out::println);
        }
    }
 
    // ──────────────────────────────────────────────────
    private void menuGrafo() {
        System.out.println("\n─── ANÁLISIS DE GRAFO ───");
        System.out.println("  Grafo de relaciones: " + mr.getGrafo());
        System.out.println("\n  1. Ver lista de adyacencia");
        System.out.println("  2. BFS desde un nodo");
        System.out.println("  3. Ranking de popularidad en el grafo");
        System.out.println("  4. Componentes conexas");
        int op = leerInt();
        switch (op) {
            case 1 -> mr.getGrafo().imprimirAdyacencia();
            case 2 -> {
                System.out.print("  Nodo (ej: CLI-CLI-001 o INM-INM-005): ");
                String nodo = scanner.nextLine().trim();
                System.out.println("  BFS: " + mr.getGrafo().bfs(nodo));
            }
            case 3 -> mr.rankingPopularidadGrafo()
                        .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue() + " conexiones"));
            case 4 -> {
                List<List<String>> comps = mr.analisisComponentes();
                System.out.println("  Componentes conexas: " + comps.size());
                comps.forEach(c -> System.out.println("  " + c));
            }
        }
    }
 
    // ──────────────────────────────────────────────────
    private int leerInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (Exception e) { return -1; }
    }
    private double leerDouble() {
        try { return Double.parseDouble(scanner.nextLine().trim()); }
        catch (Exception e) { return 0; }
    }
}
