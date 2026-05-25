package proptech.vista;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import proptech.vista.controllers.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * VISTA PRINCIPAL — Sidebar + TopBar + Área de contenido.
 * Patrón: Navigation Drawer con paneles intercambiables (sin abrir nuevas ventanas).
 */
public class MainView {

    private final Stage stage;
    private Label       topbarTitle;
    private StackPane   contentArea;
    private final Map<String, Button> navBtns = new LinkedHashMap<>();

    // Paneles (lazy)
    private Pane dashboardPanel;
    private Pane inmueblesPanel;
    private Pane clientesPanel;
    private Pane visitasPanel;
    private Pane asesoresPanel;

    public MainView(Stage stage) { this.stage = stage; }

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());

        VBox right = new VBox();
        HBox topbar = buildTopBar();
        StackPane content = buildContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        right.getChildren().addAll(topbar, content);
        root.setCenter(right);

        navigate("Dashboard");

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(
                getClass().getResource("/proptech/vista/proptech.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("[CSS] No se pudo cargar proptech.css: " + e.getMessage());
        }
        return scene;
    }

    // ── SIDEBAR ────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox();
        sb.getStyleClass().add("sidebar");
        sb.setPrefWidth(220);

        // Logo
        VBox logo = new VBox(3);
        logo.setPadding(new Insets(24, 16, 12, 20));
        Label l1 = new Label("🏢 PropTech");
        l1.setStyle("-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;");
        Label l2 = new Label("Sistema Inmobiliario");
        l2.setStyle("-fx-text-fill:#64b5f6;-fx-font-size:11px;");
        logo.getChildren().addAll(l1, l2);

        Region sep = new Region();
        sep.setStyle("-fx-background-color:#2d3f55;-fx-pref-height:1;");
        VBox.setMargin(sep, new Insets(0, 0, 8, 0));

        VBox nav = new VBox(2);
        nav.setPadding(new Insets(8, 0, 0, 0));
        String[][] items = {
            {"🏠","Dashboard"}, {"🏘","Inmuebles"}, {"👤","Clientes"},
            {"📅","Visitas"},   {"👔","Asesores"}
        };
        for (String[] it : items) {
            Button btn = new Button(it[0] + "  " + it[1]);
            btn.getStyleClass().add("nav-btn");
            btn.setPrefWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> navigate(it[1]));
            navBtns.put(it[1], btn);
            nav.getChildren().add(btn);
        }

        Region sp = new Region(); VBox.setVgrow(sp, Priority.ALWAYS);
        Label ver = new Label("v1.0 — Estructuras de Datos");
        ver.setStyle("-fx-text-fill:#455a78;-fx-font-size:10px;-fx-padding:0 0 14 20;");

        sb.getChildren().addAll(logo, sep, nav, sp, ver);
        return sb;
    }

    // ── TOPBAR ─────────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(12);
        bar.setStyle("-fx-background-color:white;-fx-border-color:transparent transparent #e2e8f0 transparent;-fx-border-width:0 0 1 0;-fx-padding:14 24;");

        topbarTitle = new Label("Dashboard");
        topbarTitle.getStyleClass().add("topbar-title");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label user = new Label("👤 Administrador");
        user.setStyle("-fx-text-fill:#718096;-fx-font-size:13px;");

        bar.getChildren().addAll(topbarTitle, sp, user);
        return bar;
    }

    // ── CONTENT AREA ───────────────────────────────────────────────────────
    private StackPane buildContent() {
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color:#f0f4f8;");
        contentArea.setPadding(new Insets(20));
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        return contentArea;
    }

    // ── NAVEGACIÓN ─────────────────────────────────────────────────────────
    public void navigate(String target) {
        navBtns.forEach((k, btn) -> {
            btn.getStyleClass().remove("nav-btn-active");
            if (!btn.getStyleClass().contains("nav-btn")) btn.getStyleClass().add("nav-btn");
        });
        Button active = navBtns.get(target);
        if (active != null) {
            active.getStyleClass().remove("nav-btn");
            active.getStyleClass().add("nav-btn-active");
        }
        topbarTitle.setText(target);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getPanel(target));
    }

    private Pane getPanel(String name) {
        return switch (name) {
            case "Inmuebles" -> {
                if (inmueblesPanel == null) inmueblesPanel = new InmueblesController().build();
                yield inmueblesPanel;
            }
            case "Clientes" -> {
                if (clientesPanel == null) clientesPanel = new ClientesController().build();
                yield clientesPanel;
            }
            case "Visitas" -> {
                if (visitasPanel == null) visitasPanel = new VisitasController().build();
                yield visitasPanel;
            }
            case "Asesores" -> {
                if (asesoresPanel == null) asesoresPanel = new AsesoresController().build();
                yield asesoresPanel;
            }
            default -> {
                if (dashboardPanel == null) dashboardPanel = new DashboardController().build();
                yield dashboardPanel;
            }
        };
    }
}