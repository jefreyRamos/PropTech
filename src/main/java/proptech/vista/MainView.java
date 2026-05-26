package proptech.vista;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import proptech.vista.controllers.AsesoresController;
import proptech.vista.controllers.ClientesController;
import proptech.vista.controllers.DashboardController;
import proptech.vista.controllers.InmueblesController;
import proptech.vista.controllers.VisitasController;

/**
 * VISTA PRINCIPAL — Layout raíz que llena toda la ventana.
 * Corrección: usa BorderPane para garantizar que el contenido
 * siempre ocupe el 100% del espacio disponible.
 */
public class MainView {

    private final Stage stage;
    private Label     topbarTitle;
    private Pane      contentArea;
    private final Map<String, Button> navBtns = new LinkedHashMap<>();

    public MainView(Stage stage) { this.stage = stage; }

    public Scene buildScene() {
        // BorderPane garantiza que center siempre ocupe todo el espacio restante
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f0f4f8;");

        // Sidebar: lado izquierdo fijo
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // Columna derecha: topbar arriba + contenido abajo
        VBox rightCol = new VBox();
        rightCol.setFillWidth(true);
        rightCol.setStyle("-fx-background-color:#f0f4f8;");

        HBox topbar = buildTopBar();

        // contentWrapper: el área que cambia con la navegación
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color:#f0f4f8;");
        ((StackPane) contentArea).setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        rightCol.getChildren().addAll(topbar, contentArea);
        root.setCenter(rightCol);

        // Escena al 100% — Stage.setMaximized hace el resto
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(
                getClass().getResource("/proptech/vista/proptech.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("[CSS] " + e.getMessage());
        }

        navigate("Dashboard");
        return scene;
    }

    // ── SIDEBAR ────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox();
        sb.getStyleClass().add("sidebar");
        sb.setPrefWidth(220);
        sb.setMinWidth(220);
        sb.setMaxWidth(220);

        // Logo
        VBox logoBox = new VBox(4);
        logoBox.setPadding(new Insets(24, 16, 16, 20));
        logoBox.setStyle("-fx-border-color:transparent transparent #243044 transparent;" +
                         "-fx-border-width:0 0 1 0;");
        Label logo = new Label("🏢  PropTech");
        logo.setStyle("-fx-text-fill:white;-fx-font-size:18px;-fx-font-weight:bold;");
        Label sub  = new Label("Sistema Inmobiliario");
        sub.setStyle("-fx-text-fill:#4d6b8a;-fx-font-size:11px;");
        logoBox.getChildren().addAll(logo, sub);

        // Menú de navegación
        VBox nav = new VBox(3);
        nav.setPadding(new Insets(14, 0, 0, 0));

        String[][] items = {
            {"🏠", "Dashboard"},
            {"🏘", "Inmuebles"},
            {"👤", "Clientes"},
            {"📅", "Visitas"},
            {"👔", "Asesores"}
        };

        for (String[] it : items) {
            Button btn = new Button(it[0] + "   " + it[1]);
            btn.getStyleClass().add("nav-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(44);
            btn.setOnAction(e -> navigate(it[1]));
            navBtns.put(it[1], btn);
            nav.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label ver = new Label("v2.0  ·  Estructuras de Datos");
        ver.setStyle("-fx-text-fill:#2d4a66;-fx-font-size:10px;-fx-padding:0 0 16 20;");

        sb.getChildren().addAll(logoBox, nav, spacer, ver);
        return sb;
    }

    // ── TOPBAR ─────────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(12);
        bar.setMaxWidth(Double.MAX_VALUE);

        topbarTitle = new Label("Dashboard");
        topbarTitle.getStyleClass().add("topbar-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label user = new Label("👤  Administrador");
        user.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:13px;");

        bar.getChildren().addAll(topbarTitle, sp, user);
        return bar;
    }

    // ── NAVEGACIÓN ─────────────────────────────────────────────────────────
    public void navigate(String target) {
        // Actualizar estilos del menú
        navBtns.forEach((k, btn) -> {
            btn.getStyleClass().removeAll("nav-btn-active", "nav-btn");
            btn.getStyleClass().add("nav-btn");
        });
        Button active = navBtns.get(target);
        if (active != null) {
            active.getStyleClass().remove("nav-btn");
            active.getStyleClass().add("nav-btn-active");
        }

        topbarTitle.setText(target);

        // Reemplazar panel — siempre se reconstruye para mostrar datos frescos
        ((StackPane) contentArea).getChildren().clear();

        Pane panel = buildPanel(target);

        // El panel debe llenar todo el espacio disponible
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
        if (panel instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            r.setMaxHeight(Double.MAX_VALUE);
        }
        ((StackPane) contentArea).getChildren().add(panel);
    }

    private Pane buildPanel(String name) {
        return switch (name) {
            case "Inmuebles" -> new InmueblesController().build();
            case "Clientes"  -> new ClientesController().build();
            case "Visitas"   -> new VisitasController().build();
            case "Asesores"  -> new AsesoresController().build();
            default          -> new DashboardController(this).build();
        };
    }
}