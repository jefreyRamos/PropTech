package proptech.vista.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import proptech.modelo.Asesor;
import proptech.servicios.PropTechService;

public class AsesoresController {

    private final PropTechService svc = PropTechService.getInstance();
    private final ObservableList<Asesor> datos = FXCollections.observableArrayList();
    private TableView<Asesor> tabla;
    private TextField txtBusqueda;

    public Pane build() {
        VBox root = new VBox(12);
        root.getStyleClass().add("modulo-root");
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);

        tabla = buildTabla();
        VBox.setVgrow(tabla, Priority.ALWAYS);

        root.getChildren().addAll(buildHeader(), tabla, buildAccionesBar());
        cargarDatos();
        return root;
    }

    // ── Header ─────────────────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox hb = new HBox(12);
        hb.getStyleClass().add("module-header");
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("👔  Gestión de Asesores");
        title.getStyleClass().add("module-title");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("🔍  Buscar nombre, código o zona...");
        txtBusqueda.getStyleClass().add("search-field");
        txtBusqueda.setPrefWidth(260);
        txtBusqueda.textProperty().addListener((o, a, n) -> buscarLocal(n));

        Button btnNuevo = new Button("＋  Nuevo Asesor");
        btnNuevo.getStyleClass().add("btn-primary");
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, sp, txtBusqueda, btnNuevo);
        return hb;
    }

    // ── Tabla ──────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Asesor> buildTabla() {
        TableView<Asesor> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setMaxWidth(Double.MAX_VALUE);
        t.setMaxHeight(Double.MAX_VALUE);
        t.setPlaceholder(new Label("Sin asesores registrados.  Haz clic en '＋ Nuevo Asesor'."));

        TableColumn<Asesor,String> cCod  = sc("Código",        "codigo",        80);
        TableColumn<Asesor,String> cNom  = sc("Nombre",        "nombre",        190);
        TableColumn<Asesor,String> cCont = sc("Contacto",      "contacto",      140);
        TableColumn<Asesor,String> cEsp  = sc("Especialidad",  "especialidad",  140);
        TableColumn<Asesor,String> cZona = sc("Zona Asignada", "zonaAsignada",  140);

        TableColumn<Asesor,Number> cCier = new TableColumn<>("Cierres");
        cCier.setCellValueFactory(new PropertyValueFactory<>("cierresRealizados"));
        cCier.setMinWidth(75);
        cCier.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setText(null); setGraphic(null); return; }
                Label l = new Label(v.toString());
                l.getStyleClass().add(v.intValue() >= 3 ? "badge-green"
                                    : v.intValue() >= 1 ? "badge-orange" : "badge-gray");
                setGraphic(l); setText(null);
            }
        });

        TableColumn<Asesor,Number> cCom = new TableColumn<>("Comisión Total");
        cCom.setCellValueFactory(cd -> cd.getValue().comisionTotalProperty());
        cCom.setMinWidth(140);
        cCom.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setText(null); return; }
                setText("$" + String.format("%,.0f", v.doubleValue()));
                setStyle("-fx-font-weight:bold; -fx-text-fill:#15803d;");
            }
        });

        TableColumn<Asesor, Void> cAcc = new TableColumn<>("Acciones");
        cAcc.setMinWidth(130); cAcc.setMaxWidth(140);
        cAcc.setCellFactory(tc -> new TableCell<>() {
            final Button bEdit = btn("✏  Editar", "btn-edit");
            final Button bDel  = btn("🗑  Borrar",  "btn-delete");
            final HBox box     = new HBox(6, bEdit, bDel);
            {
                box.setAlignment(Pos.CENTER);
                bEdit.setOnAction(e -> abrirFormulario(datos.get(getIndex())));
                bDel.setOnAction(e  -> confirmarEliminar(datos.get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e); setGraphic(e ? null : box);
            }
        });

        t.getColumns().addAll(cCod, cNom, cCont, cEsp, cZona, cCier, cCom, cAcc);
        t.setItems(datos);
        return t;
    }

    // ── Acciones bar ───────────────────────────────────────────────────────
    private HBox buildAccionesBar() {
        HBox bar = new HBox(10);
        bar.getStyleClass().add("acciones-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setMaxWidth(Double.MAX_VALUE);

        Label info = new Label("Gestione sus asesores inmobiliarios — edite directamente desde la tabla");
        info.setStyle("-fx-font-size:11.5px; -fx-text-fill:#94a3b8; -fx-font-style:italic;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnReload = new Button("↺  Recargar");
        btnReload.getStyleClass().add("btn-secondary");
        btnReload.setOnAction(e -> cargarDatos());

        bar.getChildren().addAll(info, sp, btnReload);
        return bar;
    }

    // ── Lógica ─────────────────────────────────────────────────────────────
    private void cargarDatos() {
        datos.setAll(svc.getTodosAsesores());
    }

    private void buscarLocal(String q) {
        if (q == null || q.isBlank()) { cargarDatos(); return; }
        String lq = q.toLowerCase();
        datos.setAll(svc.getTodosAsesores().stream().filter(a ->
            safe(a.getNombre()).contains(lq) ||
            safe(a.getCodigo()).contains(lq) ||
            safe(a.getZonaAsignada()).contains(lq) ||
            safe(a.getEspecialidad()).contains(lq)).toList());
    }

    // ── Formulario modal ───────────────────────────────────────────────────
    private void abrirFormulario(Asesor asesor) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(asesor == null ? "➕ Nuevo Asesor" : "✏ Editar Asesor — " + asesor.getNombre());
        win.setWidth(480); win.setHeight(420); win.setResizable(false);

        // Header oscuro
        VBox header = new VBox(4);
        header.getStyleClass().add("form-dialog-header");
        Label hTitle = new Label(asesor == null ? "Nuevo Asesor" : "Editar Asesor");
        hTitle.getStyleClass().add("form-dialog-title");
        Label hSub = new Label(asesor == null ? "Complete los datos del nuevo asesor"
                                              : "Modifique los datos del asesor");
        hSub.getStyleClass().add("form-dialog-subtitle");
        header.getChildren().addAll(hTitle, hSub);

        // Body con formulario
        GridPane gp = new GridPane();
        gp.getStyleClass().add("form-dialog-body");
        gp.setHgap(14); gp.setVgap(12);
        gp.getColumnConstraints().addAll(cc(120), cc2());

        TextField tCod  = f(asesor != null ? asesor.getCodigo()       : "");
        TextField tNom  = f(asesor != null ? asesor.getNombre()       : "");
        TextField tCont = f(asesor != null ? asesor.getContacto()     : "");
        TextField tEsp  = f(asesor != null ? asesor.getEspecialidad() : "");
        TextField tZona = f(asesor != null ? asesor.getZonaAsignada() : "");

        if (asesor != null) { tCod.setDisable(true); tCod.setStyle("-fx-opacity:0.6;"); }

        int r = 0;
        row(gp, r++, "Código *",      tCod);
        row(gp, r++, "Nombre *",      tNom);
        row(gp, r++, "Contacto",      tCont);
        row(gp, r++, "Especialidad",  tEsp);
        row(gp, r++, "Zona Asignada", tZona);

        // Footer con botones
        Button btnG = new Button(asesor == null ? "✔  Registrar Asesor" : "✔  Guardar Cambios");
        btnG.getStyleClass().add("btn-primary");
        Button btnC = new Button("Cancelar");
        btnC.getStyleClass().add("btn-secondary");
        btnC.setOnAction(e -> win.close());

        btnG.setOnAction(e -> {
            try {
                if (tCod.getText().isBlank() || tNom.getText().isBlank())
                    throw new IllegalArgumentException("Código y nombre son obligatorios.");
                Asesor obj = asesor != null ? asesor : new Asesor();
                if (asesor == null) obj.setCodigo(tCod.getText().trim());
                obj.setNombre(tNom.getText().trim());
                obj.setContacto(tCont.getText().trim());
                obj.setEspecialidad(tEsp.getText().trim());
                obj.setZonaAsignada(tZona.getText().trim());
                boolean ok = asesor == null ? svc.registrarAsesor(obj) : svc.actualizarAsesor(obj);
                if (!ok) throw new IllegalStateException("Ya existe un asesor con ese código.");
                cargarDatos(); win.close();
            } catch (Exception ex) { err(ex.getMessage()); }
        });

        HBox footer = new HBox(10, btnC, btnG);
        footer.getStyleClass().add("form-dialog-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(header, gp, footer);
        layout.getStyleClass().add("form-dialog");
        VBox.setVgrow(gp, Priority.ALWAYS);
        win.setScene(new Scene(layout));
        win.show();
    }

    private void confirmarEliminar(Asesor a) {
        Alert al = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar al asesor " + a.getNombre() + "?\nEsta acción no se puede deshacer.",
            ButtonType.YES, ButtonType.NO);
        al.setTitle("Confirmar eliminación");
        al.setHeaderText("Eliminar Asesor");
        al.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { svc.eliminarAsesor(a.getCodigo()); cargarDatos(); }
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private TableColumn<Asesor,String> sc(String n, String f, int w) {
        TableColumn<Asesor,String> c = new TableColumn<>(n);
        c.setCellValueFactory(new PropertyValueFactory<>(f));
        c.setMinWidth(w);
        return c;
    }
    private void row(GridPane gp, int r, String lbl, javafx.scene.Node ctrl) {
        Label l = new Label(lbl); l.getStyleClass().add("field-label");
        gp.add(l, 0, r); gp.add(ctrl, 1, r);
        if (ctrl instanceof Region rg) { rg.setMaxWidth(Double.MAX_VALUE); GridPane.setFillWidth(ctrl, true); }
    }
    private TextField f(String v)   { TextField t = new TextField(v); t.setMaxWidth(Double.MAX_VALUE); return t; }
    private ColumnConstraints cc(int w)  { return new ColumnConstraints(w); }
    private ColumnConstraints cc2() { ColumnConstraints c = new ColumnConstraints(); c.setHgrow(Priority.ALWAYS); c.setFillWidth(true); return c; }
    private String safe(String s)   { return s == null ? "" : s.toLowerCase(); }
    private Button btn(String txt, String css) { Button b = new Button(txt); b.getStyleClass().add(css); return b; }
    private void err(String m) { Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK); a.setTitle("Error"); a.showAndWait(); }
}