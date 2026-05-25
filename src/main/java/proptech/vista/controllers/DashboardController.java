package proptech.vista.controllers;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import proptech.modelo.Inmueble;
import proptech.servicios.PropTechService;

public class DashboardController {

    private final PropTechService svc = PropTechService.getInstance();

    public Pane build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setPannable(true);

        VBox root = new VBox(18);
        root.setPadding(new Insets(2));
        root.getChildren().addAll(buildKpis(), buildFila2(), buildEstructuras());

        scroll.setContent(root);
        StackPane sp = new StackPane(scroll);
        sp.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return sp;
    }

    // ── KPI cards ──────────────────────────────────────────────────────────
    private HBox buildKpis() {
        HBox row = new HBox(14);
        row.getChildren().addAll(
            kpi("🏘", String.valueOf(svc.countInmuebles()),         "Inmuebles",      "card-blue"),
            kpi("👥", String.valueOf(svc.countClientes()),           "Clientes",       "card-green"),
            kpi("📅", String.valueOf(svc.countVisitasPendientes()),  "Visitas Pend.",  "card-orange"),
            kpi("👔", String.valueOf(svc.countAsesores()),           "Asesores",       "card-teal")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private VBox kpi(String icon, String num, String label, String css) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", css);
        card.setPadding(new Insets(18));

        Label ico = new Label(icon); ico.setStyle("-fx-font-size:26px;");
        Label n   = new Label(num);  n.getStyleClass().add("stat-number");
        Label l   = new Label(label); l.getStyleClass().add("stat-label");

        HBox top = new HBox(12, ico, new VBox(2, n, l));
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        return card;
    }

    // ── Segunda fila: top inmuebles + alertas ──────────────────────────────
    private HBox buildFila2() {
        HBox row = new HBox(14);
        VBox topInm = buildTopInmuebles(); HBox.setHgrow(topInm, Priority.ALWAYS);
        VBox alertas = buildAlertas();     alertas.setPrefWidth(270); alertas.setMinWidth(250);
        row.getChildren().addAll(topInm, alertas);
        return row;
    }

    private VBox buildTopInmuebles() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("content-panel");

        Label title = new Label("🔥 Inmuebles con Mayor Demanda");
        title.getStyleClass().add("panel-title");
        panel.getChildren().add(title);

        List<Inmueble> top = svc.inmueblesAltaDemanda();
        if (top.isEmpty()) {
            panel.getChildren().add(new Label("Sin visitas registradas aún."));
        } else {
            for (Inmueble i : top) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(7, 0, 7, 0));
                row.setStyle("-fx-border-color:transparent transparent #f0f4f8 transparent;-fx-border-width:0 0 1 0;");

                Label tipo = new Label(i.getTipo().name().replace("_"," "));
                tipo.getStyleClass().add("badge-blue");

                VBox info = new VBox(1);
                Label dir = new Label(i.getDireccion() + " — " + i.getBarrio());
                dir.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a202c;");
                Label det = new Label(i.getCiudad() + "  |  $" + String.format("%,.0f", i.getPrecio()));
                det.setStyle("-fx-font-size:11px;-fx-text-fill:#718096;");
                info.getChildren().addAll(dir, det);

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label vis = new Label("👁 " + i.getContadorVisitas());
                vis.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a73e8;");

                row.getChildren().addAll(tipo, info, sp, vis);
                panel.getChildren().add(row);
            }
        }
        return panel;
    }

    private VBox buildAlertas() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("content-panel");

        Label title = new Label("⚠ Alertas del Sistema");
        title.getStyleClass().add("panel-title");
        panel.getChildren().add(title);

        List<Inmueble> sin = svc.inmueblesConPocoInteres();
        if (sin.isEmpty()) {
            Label ok = new Label("✅ Sin alertas activas");
            ok.setStyle("-fx-text-fill:#2e7d32;-fx-font-size:13px;");
            panel.getChildren().add(ok);
        } else {
            Label at = new Label("Inmuebles sin visitas (" + sin.size() + ")");
            at.setStyle("-fx-font-size:12px;-fx-text-fill:#e65100;-fx-font-weight:bold;");
            panel.getChildren().add(at);
            sin.stream().limit(5).forEach(i -> {
                VBox item = new VBox(2);
                item.getStyleClass().add("alerta-item");
                Label nm = new Label("⚠ " + i.getCodigo() + " — " + i.getBarrio());
                nm.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#e65100;");
                Label dt = new Label("$" + String.format("%,.0f",i.getPrecio()) + " | " + i.getTipo());
                dt.setStyle("-fx-font-size:11px;-fx-text-fill:#718096;");
                item.getChildren().addAll(nm, dt);
                panel.getChildren().add(item);
            });
        }

        Separator sep = new Separator(); sep.setPadding(new Insets(4,0,0,0));
        panel.getChildren().add(sep);
        Label cola = new Label("🔔 Visitas urgentes en cola:\n" + svc.countVisitasPendientes() + " pendientes");
        cola.setStyle("-fx-font-size:12px;-fx-text-fill:#1a73e8;-fx-wrap-text:true;");
        panel.getChildren().add(cola);
        return panel;
    }

    // ── Estado de estructuras ──────────────────────────────────────────────
    private VBox buildEstructuras() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("content-panel");
        Label title = new Label("📊 Estado de Estructuras de Datos en Memoria");
        title.getStyleClass().add("panel-title");
        Label info = new Label(svc.getInfoEstructuras());
        info.setStyle("-fx-font-size:12px;-fx-text-fill:#4a5568;-fx-font-family:'Courier New',monospace;");
        info.setWrapText(true);
        panel.getChildren().addAll(title, info);
        return panel;
    }
}