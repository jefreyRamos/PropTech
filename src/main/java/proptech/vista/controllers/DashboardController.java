package proptech.vista.controllers;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import proptech.modelo.Inmueble;
import proptech.servicios.PropTechService;
import proptech.vista.MainView;

public class DashboardController {

    private final PropTechService svc;
    private final MainView nav;

    public DashboardController(MainView nav) {
        this.svc = PropTechService.getInstance();
        this.nav = nav;
    }

    public Pane build() {
        // ScrollPane ocupa todo el espacio
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:#f0f4f8; -fx-background:#f0f4f8;" +
                        "-fx-border-color:transparent;");

        VBox body = new VBox(18);
        body.setPadding(new Insets(22, 26, 22, 26));
        body.setFillWidth(true);
        body.setMaxWidth(Double.MAX_VALUE);

        body.getChildren().addAll(
            buildKpis(),
            buildMidRow(),
            buildEstructuras()
        );

        scroll.setContent(body);

        // El scroll llena todo BorderPane.center
        StackPane root = new StackPane(scroll);
        root.setAlignment(Pos.TOP_LEFT);
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    // ── KPIs ───────────────────────────────────────────────────────────────
    private HBox buildKpis() {
        HBox row = new HBox(16);
        row.setFillHeight(true);
        row.setMaxWidth(Double.MAX_VALUE);

        VBox c1 = kpi("🏘", svc.countInmuebles(),        "Inmuebles",     "card-blue",   "#1a73e8", "Inmuebles");
        VBox c2 = kpi("👥", svc.countClientes(),          "Clientes",      "card-green",  "#16a34a", "Clientes");
        VBox c3 = kpi("📅", svc.countVisitasPendientes(), "Visitas Pend.", "card-orange", "#ea580c", "Visitas");
        VBox c4 = kpi("👔", svc.countAsesores(),          "Asesores",      "card-teal",   "#0891b2", "Asesores");

        for (VBox c : List.of(c1,c2,c3,c4)) HBox.setHgrow(c, Priority.ALWAYS);
        row.getChildren().addAll(c1,c2,c3,c4);
        return row;
    }

    private VBox kpi(String icon, int num, String label, String css, String color, String destino) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("stat-card", css);
        card.setOnMouseClicked(e -> nav.navigate(destino));

        Label ico  = new Label(icon);
        ico.setStyle("-fx-font-size:30px;");

        Label n    = new Label(String.valueOf(num));
        n.setStyle("-fx-font-size:38px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");

        Label l    = new Label(label);
        l.getStyleClass().add("stat-label");
        l.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");

        VBox texts = new VBox(3, n, l);
        HBox top   = new HBox(14, ico, texts);
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        return card;
    }

    // ── Fila central ───────────────────────────────────────────────────────
    private HBox buildMidRow() {
        HBox row = new HBox(16);
        row.setFillHeight(true);
        row.setMaxWidth(Double.MAX_VALUE);

        VBox topInm  = buildTopInmuebles();
        VBox alertas = buildAlertas();

        HBox.setHgrow(topInm, Priority.ALWAYS);
        alertas.setPrefWidth(300);
        alertas.setMinWidth(260);
        alertas.setMaxWidth(340);

        row.getChildren().addAll(topInm, alertas);
        return row;
    }

    private VBox buildTopInmuebles() {
        VBox panel = new VBox();
        panel.getStyleClass().add("content-panel");
        panel.setMaxWidth(Double.MAX_VALUE);

        HBox hdr = sectionHeader("🔥  Inmuebles más visitados");
        VBox lista = new VBox();

        List<Inmueble> top = svc.inmueblesAltaDemanda();
        if (top.isEmpty()) {
            Label e = new Label("Aún no hay visitas registradas.");
            e.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13px; -fx-padding:18;");
            lista.getChildren().add(e);
        } else {
            for (int i = 0; i < top.size(); i++) {
                Inmueble inm = top.get(i);
                HBox fila = new HBox(12);
                fila.setPadding(new Insets(13, 18, 13, 18));
                fila.setAlignment(Pos.CENTER_LEFT);
                if (i < top.size()-1)
                    fila.setStyle("-fx-border-color:transparent transparent #f1f5f9 transparent;" +
                                  "-fx-border-width:0 0 1 0;");

                Label pos = new Label((i+1) + "");
                pos.setStyle("-fx-text-fill:#cbd5e0; -fx-font-size:13px; -fx-font-weight:bold; -fx-min-width:20;");

                Label tipo = new Label(inm.getTipo().name().replace("_"," "));
                tipo.getStyleClass().add("badge-blue");

                VBox info = new VBox(3);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label dir = new Label(inm.getDireccion());
                dir.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
                Label det = new Label(inm.getBarrio() + ", " + inm.getCiudad() +
                                      "   ·   $" + String.format("%,.0f", inm.getPrecio()));
                det.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");
                info.getChildren().addAll(dir, det);

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

                VBox visBox = new VBox(1);
                visBox.setAlignment(Pos.CENTER_RIGHT);
                Label vn = new Label(String.valueOf(inm.getContadorVisitas()));
                vn.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#1a73e8;");
                Label vl = new Label("visitas");
                vl.setStyle("-fx-font-size:10px; -fx-text-fill:#94a3b8;");
                visBox.getChildren().addAll(vn, vl);

                fila.getChildren().addAll(pos, tipo, info, sp, visBox);
                lista.getChildren().add(fila);
            }
        }
        panel.getChildren().addAll(hdr, lista);
        return panel;
    }

    private VBox buildAlertas() {
        VBox panel = new VBox();
        panel.getStyleClass().add("content-panel");

        HBox hdr = sectionHeader("⚠  Alertas");
        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 16));

        List<Inmueble> sinVisitas = svc.inmueblesConPocoInteres();
        if (sinVisitas.isEmpty()) {
            Label ok = new Label("✅  Sin alertas activas");
            ok.setStyle("-fx-text-fill:#16a34a; -fx-font-size:13px; -fx-font-weight:bold;");
            body.getChildren().add(ok);
        } else {
            Label lbl = new Label("Sin actividad (" + sinVisitas.size() + ")");
            lbl.setStyle("-fx-font-size:12px; -fx-text-fill:#c2410c; -fx-font-weight:bold;");
            body.getChildren().add(lbl);
            sinVisitas.stream().limit(4).forEach(i -> {
                VBox item = new VBox(3);
                item.getStyleClass().add("alerta-item");
                Label nm = new Label(i.getCodigo() + "  —  " + i.getBarrio());
                nm.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#92400e;");
                nm.setWrapText(true);
                Label dt = new Label("$" + String.format("%,.0f", i.getPrecio()));
                dt.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");
                item.getChildren().addAll(nm, dt);
                body.getChildren().add(item);
            });
        }

        body.getChildren().add(new Separator());
        int pend = svc.countVisitasPendientes();
        Label cola = new Label("🔔  " + pend + " visita" + (pend!=1?"s":"") + " pendiente" + (pend!=1?"s":""));
        cola.setStyle("-fx-font-size:13px; -fx-text-fill:#1a73e8; -fx-font-weight:bold;");
        body.getChildren().add(cola);

        panel.getChildren().addAll(hdr, body);
        return panel;
    }

    // ── Estructuras ────────────────────────────────────────────────────────
    private VBox buildEstructuras() {
        VBox panel = new VBox();
        panel.getStyleClass().add("content-panel");
        panel.setMaxWidth(Double.MAX_VALUE);

        HBox hdr = sectionHeader("📊  Estado de Estructuras de Datos en Memoria");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(16, 18, 16, 18));

        Object[][] cards = {
            {"🗂", "TablaHash  Inmuebles",  svc.countInmuebles() + " entradas",   "Acceso O(1) por código",            "badge-blue"},
            {"🗂", "TablaHash  Clientes",   svc.countClientes()  + " entradas",   "Acceso O(1) por identificación",    "badge-blue"},
            {"🌲", "Árbol BST  Precio",     svc.countInmuebles() + " nodos",      "Orden ascendente, rangos O(log n)", "badge-green"},
            {"🔗", "Grafo  Relaciones",     svc.getGrafo().numVertices() + " vértices", "BFS recomendaciones cliente→inmueble","badge-purple"},
            {"⬆",  "Cola  Prioridad",       svc.countVisitasPendientes() + " elem.", "VIP y ALTA se atienden primero",  "badge-orange"},
            {"📚", "Pila  Deshacer",        "activa",                              "Historial reversible de cambios",   "badge-gray"},
        };

        for (int i = 0; i < cards.length; i++) {
            VBox c = estructuraCard(
                (String) cards[i][0], (String) cards[i][1],
                (String) cards[i][2], (String) cards[i][3], (String) cards[i][4]);
            GridPane.setHgrow(c, Priority.ALWAYS);
            grid.add(c, i % 3, i / 3);
        }

        panel.getChildren().addAll(hdr, grid);
        return panel;
    }

    private VBox estructuraCard(String ico, String nombre, String valor, String desc, String badge) {
        VBox c = new VBox(6);
        c.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:10; -fx-padding:14 16;");
        c.setMaxWidth(Double.MAX_VALUE);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label i = new Label(ico); i.setStyle("-fx-font-size:18px;");
        Label n = new Label(nombre); n.setStyle("-fx-font-size:12.5px; -fx-font-weight:bold; -fx-text-fill:#334155;");
        top.getChildren().addAll(i, n);

        Label v = new Label(valor);
        v.getStyleClass().add(badge);

        Label d = new Label(desc);
        d.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");
        d.setWrapText(true);

        c.getChildren().addAll(top, v, d);
        return c;
    }

    // ── Utilidad ───────────────────────────────────────────────────────────
    private HBox sectionHeader(String title) {
        HBox hdr = new HBox();
        hdr.setPadding(new Insets(14, 18, 12, 18));
        hdr.setStyle("-fx-border-color:transparent transparent #f1f5f9 transparent;" +
                     "-fx-border-width:0 0 1 0;");
        Label l = new Label(title);
        l.getStyleClass().add("panel-title");
        hdr.getChildren().add(l);
        return hdr;
    }
}