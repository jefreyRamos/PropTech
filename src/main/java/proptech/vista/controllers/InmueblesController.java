package proptech.vista.controllers;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
import proptech.modelo.Inmueble;
import proptech.servicios.PropTechService;

public class InmueblesController {

    private final PropTechService svc = PropTechService.getInstance();
    private final ObservableList<Inmueble> datos = FXCollections.observableArrayList();
    private TableView<Inmueble> tabla;
    private ComboBox<String> cmbTipo, cmbFinalidad;
    private TextField txtPrecioMax, txtCiudad, txtBusqueda;
    private Label lblConteo;

    public Pane build() {
        VBox root = new VBox(12);
        root.getStyleClass().add("modulo-root");
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);

        tabla = buildTabla();
        VBox.setVgrow(tabla, Priority.ALWAYS);

        root.getChildren().addAll(
            buildHeader(),
            buildFiltros(),
            tabla,
            buildAccionesBar()
        );
        cargarDatos();
        return root;
    }

    // ── Header ─────────────────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox hb = new HBox(12);
        hb.getStyleClass().add("module-header");
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("🏘  Gestión de Inmuebles");
        title.getStyleClass().add("module-title");

        lblConteo = new Label();
        lblConteo.setStyle("-fx-font-size:12px; -fx-text-fill:#94a3b8; -fx-padding:0 0 0 10;");
        datos.addListener((javafx.collections.ListChangeListener<Inmueble>) c ->
            lblConteo.setText(datos.size() + " inmueble" + (datos.size()!=1?"s":"")));

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("🔍  Buscar código, dirección, barrio...");
        txtBusqueda.getStyleClass().add("search-field");
        txtBusqueda.setPrefWidth(260);
        txtBusqueda.textProperty().addListener((o,a,n) -> buscarLocal(n));

        Button btnNuevo = new Button("＋  Nuevo Inmueble");
        btnNuevo.getStyleClass().add("btn-primary");
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, lblConteo, sp, txtBusqueda, btnNuevo);
        return hb;
    }

    // ── Filtros ────────────────────────────────────────────────────────────
    private HBox buildFiltros() {
        HBox hb = new HBox(10);
        hb.getStyleClass().add("filtros-bar");
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setMaxWidth(Double.MAX_VALUE);

        Label lbl = new Label("FILTROS");
        lbl.getStyleClass().add("filtros-label");

        Separator s1 = new Separator(); s1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        s1.setPrefHeight(24);

        cmbTipo = new ComboBox<>(FXCollections.observableArrayList(
            "Todos","APARTAMENTO","CASA","LOCAL_COMERCIAL","OFICINA","LOTE","BODEGA"));
        cmbTipo.setValue("Todos"); cmbTipo.setPrefWidth(155);

        cmbFinalidad = new ComboBox<>(FXCollections.observableArrayList("Todos","VENTA","ARRIENDO"));
        cmbFinalidad.setValue("Todos"); cmbFinalidad.setPrefWidth(115);

        txtPrecioMax = new TextField();
        txtPrecioMax.setPromptText("Precio máximo");
        txtPrecioMax.setPrefWidth(130);

        txtCiudad = new TextField();
        txtCiudad.setPromptText("Ciudad");
        txtCiudad.setPrefWidth(115);

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.getStyleClass().add("btn-primary");
        btnFiltrar.setOnAction(e -> aplicarFiltros());

        Button btnLimpiar = new Button("✕  Limpiar");
        btnLimpiar.getStyleClass().add("btn-secondary");
        btnLimpiar.setOnAction(e -> limpiarFiltros());

        hb.getChildren().addAll(lbl, s1, cmbTipo, cmbFinalidad, txtPrecioMax, txtCiudad, btnFiltrar, btnLimpiar);
        return hb;
    }

    // ── Tabla ──────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Inmueble> buildTabla() {
        TableView<Inmueble> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setMaxWidth(Double.MAX_VALUE);
        t.setMaxHeight(Double.MAX_VALUE);
        t.setPlaceholder(new Label("Sin inmuebles.  Haz clic en '＋ Nuevo Inmueble' para agregar."));

        TableColumn<Inmueble,String> cCod  = sc("Código",    "codigo",    75);
        TableColumn<Inmueble,String> cDir  = sc("Dirección", "direccion", 170);
        TableColumn<Inmueble,String> cCiud = sc("Ciudad",    "ciudad",    85);
        TableColumn<Inmueble,String> cBarr = sc("Barrio",    "barrio",    100);

        TableColumn<Inmueble,Object> cTipo = new TableColumn<>("Tipo");
        cTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        cTipo.setMinWidth(120);
        cTipo.setCellFactory(tc -> badgeCell(v -> "badge-blue"));

        TableColumn<Inmueble,Object> cFin = new TableColumn<>("Finalidad");
        cFin.setCellValueFactory(new PropertyValueFactory<>("finalidad"));
        cFin.setMinWidth(90);
        cFin.setCellFactory(tc -> badgeCell(v -> "VENTA".equals(v) ? "badge-green" : "badge-teal"));

        TableColumn<Inmueble,Number> cPrecio = new TableColumn<>("Precio");
        cPrecio.setCellValueFactory(cd -> cd.getValue().precioProperty());
        cPrecio.setMinWidth(130);
        cPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean e) {
                super.updateItem(v,e);
                if (e||v==null) { setText(null); return; }
                setText("$" + String.format("%,.0f", v.doubleValue()));
                setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a;");
            }
        });

        TableColumn<Inmueble,Number> cArea = numCol("m²",    "area",          55);
        TableColumn<Inmueble,Number> cHab  = numCol("Hab.",  "habitaciones",  45);
        TableColumn<Inmueble,Number> cBan  = numCol("Baños", "banos",         55);

        TableColumn<Inmueble,Object> cEst = new TableColumn<>("Estado");
        cEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cEst.setMinWidth(108);
        cEst.setCellFactory(tc -> badgeCell(v -> switch(v) {
            case "DISPONIBLE" -> "badge-green";
            case "RESERVADO"  -> "badge-orange";
            case "ARRENDADO"  -> "badge-blue";
            default           -> "badge-red";
        }));

        TableColumn<Inmueble,Number> cVis = new TableColumn<>("👁");
        cVis.setCellValueFactory(new PropertyValueFactory<>("contadorVisitas"));
        cVis.setMinWidth(45); cVis.setMaxWidth(55);

        TableColumn<Inmueble,Void> cAcc = new TableColumn<>("Acciones");
        cAcc.setMinWidth(135); cAcc.setMaxWidth(145);
        cAcc.setCellFactory(tc -> new TableCell<>() {
            final Button bEdit = btn("✏  Editar",  "btn-edit");
            final Button bDel  = btn("🗑  Borrar", "btn-delete");
            final HBox box     = new HBox(6, bEdit, bDel);
            {
                box.setAlignment(Pos.CENTER);
                bEdit.setOnAction(e -> abrirFormulario(datos.get(getIndex())));
                bDel.setOnAction(e  -> confirmarEliminar(datos.get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean e){ super.updateItem(v,e); setGraphic(e?null:box); }
        });

        t.getColumns().addAll(cCod,cDir,cCiud,cBarr,cTipo,cFin,cPrecio,cArea,cHab,cBan,cEst,cVis,cAcc);
        t.setItems(datos);
        return t;
    }

    // ── Acciones Bar ───────────────────────────────────────────────────────
    private HBox buildAccionesBar() {
        HBox bar = new HBox(10);
        bar.getStyleClass().add("acciones-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setMaxWidth(Double.MAX_VALUE);

        Label lbl = new Label("Herramientas:");
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8; -fx-font-weight:bold;");

        Button btnOrden = new Button("⬆  Ordenar por Precio (BST)");
        btnOrden.getStyleClass().add("btn-secondary");
        btnOrden.setOnAction(e -> datos.setAll(svc.inmueblesOrdenadosPorPrecio()));

        Button btnRango = new Button("💰  Rango de Precio");
        btnRango.getStyleClass().add("btn-secondary");
        btnRango.setOnAction(e -> buscarRango());

        Button btnRec = new Button("💡  Recomendar a Cliente");
        btnRec.getStyleClass().add("btn-secondary");
        btnRec.setOnAction(e -> mostrarRecomendaciones());

        Button btnReload = new Button("↺  Recargar");
        btnReload.getStyleClass().add("btn-secondary");
        btnReload.setOnAction(e -> cargarDatos());

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        bar.getChildren().addAll(lbl, sp, btnOrden, btnRango, btnRec, btnReload);
        return bar;
    }

    // ── Lógica ─────────────────────────────────────────────────────────────
    private void cargarDatos()  { datos.setAll(svc.getTodosInmuebles()); }

    private void buscarLocal(String q) {
        if (q == null || q.isBlank()) { cargarDatos(); return; }
        String lq = q.toLowerCase();
        datos.setAll(svc.getTodosInmuebles().stream().filter(i ->
            safe(i.getCodigo()).contains(lq) || safe(i.getDireccion()).contains(lq) ||
            safe(i.getBarrio()).contains(lq) || safe(i.getCiudad()).contains(lq)).toList());
    }

    private void aplicarFiltros() {
        String tipo = "Todos".equals(cmbTipo.getValue())      ? null : cmbTipo.getValue();
        String fin  = "Todos".equals(cmbFinalidad.getValue()) ? null : cmbFinalidad.getValue();
        datos.setAll(svc.filtrarInmuebles(tipo, fin, pd(txtPrecioMax.getText()), 0, txtCiudad.getText().trim()));
    }

    private void limpiarFiltros() {
        cmbTipo.setValue("Todos"); cmbFinalidad.setValue("Todos");
        txtPrecioMax.clear(); txtCiudad.clear(); txtBusqueda.clear();
        cargarDatos();
    }

    private void buscarRango() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Buscar por Rango — ArbolBST");
        dlg.setHeaderText("Rango de precio");
        GridPane gp = new GridPane(); gp.setHgap(12); gp.setVgap(10); gp.setPadding(new Insets(16));
        TextField tMin = new TextField(); tMin.setPromptText("Mínimo");
        TextField tMax = new TextField(); tMax.setPromptText("Máximo");
        gp.add(new Label("Precio mín:"),0,0); gp.add(tMin,1,0);
        gp.add(new Label("Precio máx:"),0,1); gp.add(tMax,1,1);
        dlg.getDialogPane().setContent(gp);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                double min = pd(tMin.getText()), max = pd(tMax.getText());
                if (max == 0) max = Double.MAX_VALUE;
                List<Inmueble> r = svc.inmueblesPorRango(min, max);
                datos.setAll(r);
                if (r.isEmpty()) info("El ArbolBST no encontró inmuebles en ese rango.");
            }
        });
    }

    private void mostrarRecomendaciones() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Recomendaciones — Grafo + TablaHash");
        dlg.setHeaderText("ID del cliente");
        dlg.setContentText("Identificación:");
        dlg.showAndWait().ifPresent(id -> {
            List<Inmueble> recs = svc.recomendar(id.trim());
            if (recs.isEmpty()) { info("Sin recomendaciones para ese cliente."); return; }
            StringBuilder sb = new StringBuilder();
            recs.forEach(i -> sb.append("• ").append(i.getCodigo()).append(" — ")
                .append(i.getDireccion()).append("  |  $").append(String.format("%,.0f", i.getPrecio())).append("\n"));
            Alert a = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
            a.setTitle("Top " + recs.size() + " recomendaciones para " + id); a.showAndWait();
        });
    }

    // ── Formulario modal de inmueble ────────────────────────────────────────
    private void abrirFormulario(Inmueble inmueble) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(inmueble == null ? "➕ Nuevo Inmueble" : "✏ Editar Inmueble — " + inmueble.getCodigo());
        win.setWidth(540); win.setHeight(620); win.setResizable(false);

        // Header oscuro
        VBox header = new VBox(4);
        header.getStyleClass().add("form-dialog-header");
        Label hTitle = new Label(inmueble == null ? "Nuevo Inmueble" : "Editar Inmueble");
        hTitle.getStyleClass().add("form-dialog-title");
        Label hSub = new Label(inmueble == null ? "Complete los datos del nuevo inmueble"
                                                : "Modifique los datos del inmueble " + inmueble.getCodigo());
        hSub.getStyleClass().add("form-dialog-subtitle");
        header.getChildren().addAll(hTitle, hSub);

        // Formulario
        GridPane gp = new GridPane();
        gp.getStyleClass().add("form-dialog-body");
        gp.setHgap(14); gp.setVgap(11);
        gp.getColumnConstraints().addAll(cc(120), cc2());

        TextField tCod  = f(inmueble != null ? inmueble.getCodigo()    : "");
        TextField tDir  = f(inmueble != null ? inmueble.getDireccion() : "");
        TextField tCiud = f(inmueble != null ? inmueble.getCiudad()    : "");
        TextField tBarr = f(inmueble != null ? inmueble.getBarrio()    : "");
        TextField tPrec = f(inmueble != null ? String.format("%.0f", inmueble.getPrecio()) : "");
        TextField tArea = f(inmueble != null ? String.format("%.0f", inmueble.getArea())   : "");
        TextField tHab  = f(inmueble != null ? String.valueOf(inmueble.getHabitaciones())  : "0");
        TextField tBan  = f(inmueble != null ? String.valueOf(inmueble.getBanos())          : "0");

        ComboBox<String> cTipo = cmb("APARTAMENTO","CASA","LOCAL_COMERCIAL","OFICINA","LOTE","BODEGA");
        cTipo.setValue(inmueble != null ? inmueble.getTipo().name() : "APARTAMENTO");

        ComboBox<String> cFin = cmb("VENTA","ARRIENDO");
        cFin.setValue(inmueble != null ? inmueble.getFinalidad().name() : "ARRIENDO");

        ComboBox<String> cEst = cmb("DISPONIBLE","RESERVADO","ARRENDADO","VENDIDO");
        cEst.setValue(inmueble != null ? inmueble.getEstado().name() : "DISPONIBLE");

        ComboBox<String> cAs = new ComboBox<>();
        cAs.setMaxWidth(Double.MAX_VALUE);
        svc.getTodosAsesores().forEach(a -> cAs.getItems().add(a.getCodigo() + " — " + a.getNombre()));
        if (inmueble != null && inmueble.getCodigoAsesor() != null)
            cAs.getItems().stream().filter(s -> s.startsWith(inmueble.getCodigoAsesor())).findFirst().ifPresent(cAs::setValue);
        else if (!cAs.getItems().isEmpty()) cAs.setValue(cAs.getItems().get(0));

        if (inmueble != null) { tCod.setDisable(true); tCod.setStyle("-fx-opacity:0.6;"); }

        int r = 0;
        row(gp,r++,"Código *",      tCod);
        row(gp,r++,"Dirección *",   tDir);
        row(gp,r++,"Ciudad *",      tCiud);
        row(gp,r++,"Barrio / Zona", tBarr);
        row(gp,r++,"Tipo",          cTipo);
        row(gp,r++,"Finalidad",     cFin);
        row(gp,r++,"Precio *",      tPrec);
        row(gp,r++,"Área m²",       tArea);
        row(gp,r++,"Habitaciones",  tHab);
        row(gp,r++,"Baños",         tBan);
        row(gp,r++,"Estado",        cEst);
        row(gp,r++,"Asesor",        cAs);

        // Footer
        Button btnG = new Button(inmueble == null ? "✔  Registrar Inmueble" : "✔  Guardar Cambios");
        btnG.getStyleClass().add("btn-primary");
        Button btnC = new Button("Cancelar"); btnC.getStyleClass().add("btn-secondary");
        btnC.setOnAction(e -> win.close());

        btnG.setOnAction(e -> {
            try {
                if (tCod.getText().isBlank() || tDir.getText().isBlank()
                        || tCiud.getText().isBlank() || tPrec.getText().isBlank())
                    throw new IllegalArgumentException("Código, dirección, ciudad y precio son obligatorios.");
                String codAs = cAs.getValue() != null ? cAs.getValue().split(" — ")[0].trim() : "";
                Inmueble obj = inmueble != null ? inmueble : new Inmueble();
                if (inmueble == null) obj.setCodigo(tCod.getText().trim());
                obj.setDireccion(tDir.getText().trim()); obj.setCiudad(tCiud.getText().trim());
                obj.setBarrio(tBarr.getText().trim());   obj.setTipo(Inmueble.Tipo.valueOf(cTipo.getValue()));
                obj.setFinalidad(Inmueble.Finalidad.valueOf(cFin.getValue()));
                obj.setPrecio(pd(tPrec.getText()));      obj.setArea(pd(tArea.getText()));
                obj.setHabitaciones((int)pd(tHab.getText())); obj.setBanos((int)pd(tBan.getText()));
                obj.setEstado(Inmueble.Estado.valueOf(cEst.getValue())); obj.setCodigoAsesor(codAs);
                boolean ok = inmueble == null ? svc.registrarInmueble(obj) : svc.actualizarInmueble(obj);
                if (!ok) throw new IllegalStateException("Ya existe un inmueble con ese código.");
                cargarDatos(); win.close();
            } catch (Exception ex) { err(ex.getMessage()); }
        });

        HBox footer = new HBox(10, btnC, btnG);
        footer.getStyleClass().add("form-dialog-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        ScrollPane sp = new ScrollPane(gp);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:white; -fx-background-color:white;");
        VBox layout = new VBox(header, sp, footer);
        layout.getStyleClass().add("form-dialog");
        VBox.setVgrow(sp, Priority.ALWAYS);
        win.setScene(new Scene(layout));
        win.show();
    }

    private void confirmarEliminar(Inmueble i) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar el inmueble " + i.getCodigo() + "?\n" + i.getDireccion() + "\n\nEsta acción no se puede deshacer.",
            ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmar eliminación"); a.setHeaderText("Eliminar Inmueble");
        a.showAndWait().ifPresent(r -> { if (r==ButtonType.YES) { svc.eliminarInmueble(i.getCodigo()); cargarDatos(); } });
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private TableColumn<Inmueble,String> sc(String n, String f, int w) {
        TableColumn<Inmueble,String> c = new TableColumn<>(n);
        c.setCellValueFactory(new PropertyValueFactory<>(f)); c.setMinWidth(w); return c;
    }
    private TableColumn<Inmueble,Number> numCol(String n, String f, int w) {
        TableColumn<Inmueble,Number> c = new TableColumn<>(n);
        c.setCellValueFactory(new PropertyValueFactory<>(f)); c.setMinWidth(w); c.setMaxWidth(w+20); return c;
    }
    private <T> TableCell<Inmueble,T> badgeCell(java.util.function.Function<String,String> fn) {
        return new TableCell<>() {
            @Override protected void updateItem(T v, boolean e) {
                super.updateItem(v,e); setText(null); setGraphic(null);
                if (!e && v!=null) { Label l=new Label(v.toString().replace("_"," ")); l.getStyleClass().add(fn.apply(v.toString())); setGraphic(l); }
            }
        };
    }
    private void row(GridPane gp, int r, String lbl, javafx.scene.Node ctrl) {
        Label l = new Label(lbl); l.getStyleClass().add("field-label");
        gp.add(l,0,r); gp.add(ctrl,1,r);
        if (ctrl instanceof Region rg) { rg.setMaxWidth(Double.MAX_VALUE); GridPane.setFillWidth(ctrl,true); }
    }
    private TextField f(String v) { TextField t=new TextField(v); t.setMaxWidth(Double.MAX_VALUE); return t; }
    private ComboBox<String> cmb(String... items) { ComboBox<String> c=new ComboBox<>(FXCollections.observableArrayList(items)); c.setMaxWidth(Double.MAX_VALUE); return c; }
    private ColumnConstraints cc(int w) { return new ColumnConstraints(w); }
    private ColumnConstraints cc2() { ColumnConstraints c=new ColumnConstraints(); c.setHgrow(Priority.ALWAYS); c.setFillWidth(true); return c; }
    private double pd(String s) { try { return Double.parseDouble(s.replace(",","").replace(".","").trim()); } catch (Exception e) { return 0; } }
    private String safe(String s) { return s==null?"":s.toLowerCase(); }
    private Button btn(String txt, String css) { Button b=new Button(txt); b.getStyleClass().add(css); return b; }
    private void err(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
}