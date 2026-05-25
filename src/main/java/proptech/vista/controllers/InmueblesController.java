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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import proptech.modelo.Asesor;
import proptech.modelo.Inmueble;
import proptech.servicios.PropTechService;

public class InmueblesController {

    private final PropTechService svc = PropTechService.getInstance();
    private final ObservableList<Inmueble> datos = FXCollections.observableArrayList();
    private TableView<Inmueble> tabla;

    // Filtros
    private ComboBox<String> cmbTipo, cmbFinalidad;
    private TextField txtPrecioMax, txtCiudad, txtBusqueda;

    public Pane build() {
        VBox root = new VBox(14);
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setPadding(new Insets(4));

        root.getChildren().addAll(
            buildHeader(),
            buildFiltros(),
            buildTabla(),
            buildPiePagina()
        );
        VBox.setVgrow(buildTabla(), Priority.ALWAYS);
        cargarDatos();
        return root;
    }

    // ── Encabezado ─────────────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox hb = new HBox(12);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:16 20;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        Label title = new Label("🏘 Gestión de Inmuebles");
        title.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#1a202c;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("🔍  Buscar código, dirección, barrio...");
        txtBusqueda.getStyleClass().add("search-field");
        txtBusqueda.setPrefWidth(270);
        txtBusqueda.textProperty().addListener((o, a, n) -> buscarLocal(n));

        Button btnNuevo = new Button("＋  Nuevo Inmueble");
        btnNuevo.getStyleClass().add("btn-primary");
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, sp, txtBusqueda, btnNuevo);
        return hb;
    }

    // ── Filtros ────────────────────────────────────────────────────────────
    private HBox buildFiltros() {
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-padding:10 16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),6,0,0,1);");

        Label lbl = new Label("Filtros:");
        lbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#718096;");

        cmbTipo = new ComboBox<>(FXCollections.observableArrayList(
            "Todos","APARTAMENTO","CASA","LOCAL_COMERCIAL","OFICINA","LOTE","BODEGA"));
        cmbTipo.setValue("Todos"); cmbTipo.setPrefWidth(165);

        cmbFinalidad = new ComboBox<>(FXCollections.observableArrayList("Todos","VENTA","ARRIENDO"));
        cmbFinalidad.setValue("Todos"); cmbFinalidad.setPrefWidth(120);

        txtPrecioMax = new TextField(); txtPrecioMax.setPromptText("Precio máx."); txtPrecioMax.setPrefWidth(130);
        txtCiudad    = new TextField(); txtCiudad.setPromptText("Ciudad");         txtCiudad.setPrefWidth(120);

        Button btnFiltrar = new Button("Filtrar");   btnFiltrar.getStyleClass().add("btn-secondary");
        Button btnLimpiar = new Button("✕ Limpiar"); btnLimpiar.getStyleClass().add("btn-icon");

        btnFiltrar.setOnAction(e -> aplicarFiltros());
        btnLimpiar.setOnAction(e -> limpiarFiltros());

        hb.getChildren().addAll(lbl, cmbTipo, cmbFinalidad, txtPrecioMax, txtCiudad, btnFiltrar, btnLimpiar);
        return hb;
    }

    // ── Tabla ──────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Inmueble> buildTabla() {
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(Double.MAX_VALUE);
        tabla.setPlaceholder(new Label("Sin inmuebles registrados."));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Inmueble,String> cCod  = strCol("Código",    "codigo",    80);
        TableColumn<Inmueble,String> cDir  = strCol("Dirección", "direccion", 170);
        TableColumn<Inmueble,String> cCiud = strCol("Ciudad",    "ciudad",    90);
        TableColumn<Inmueble,String> cBarr = strCol("Barrio",    "barrio",    100);

        TableColumn<Inmueble,Object> cTipo = new TableColumn<>("Tipo");
        cTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        cTipo.setMinWidth(120);
        cTipo.setCellFactory(tc -> badgeCell("badge-blue"));

        TableColumn<Inmueble,Object> cFin = new TableColumn<>("Finalidad");
        cFin.setCellValueFactory(new PropertyValueFactory<>("finalidad"));
        cFin.setMinWidth(90);
        cFin.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Object v, boolean empty) {
                super.updateItem(v,empty); setText(null); setGraphic(null);
                if (!empty && v != null) {
                    Label l = new Label(v.toString());
                    l.getStyleClass().add("VENTA".equals(v.toString()) ? "badge-green" : "badge-orange");
                    setGraphic(l);
                }
            }
        });

        TableColumn<Inmueble,Number> cPrecio = new TableColumn<>("Precio");
        cPrecio.setCellValueFactory(cd -> cd.getValue().precioProperty());
        cPrecio.setMinWidth(130);
        cPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v,empty);
                setText(empty||v==null ? null : "$"+String.format("%,.0f",v.doubleValue()));
            }
        });

        TableColumn<Inmueble,Number> cArea = numCol("m²",   "area",         65);
        TableColumn<Inmueble,Number> cHab  = numCol("Hab.", "habitaciones", 50);
        TableColumn<Inmueble,Number> cBan  = numCol("Baños","banos",        55);

        TableColumn<Inmueble,Object> cEst = new TableColumn<>("Estado");
        cEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cEst.setMinWidth(110);
        cEst.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Object v, boolean empty) {
                super.updateItem(v,empty); setText(null); setGraphic(null);
                if (!empty && v != null) {
                    Label l = new Label(v.toString());
                    l.getStyleClass().add(switch(v.toString()) {
                        case "DISPONIBLE" -> "badge-green";
                        case "RESERVADO"  -> "badge-orange";
                        default           -> "badge-red";
                    });
                    setGraphic(l);
                }
            }
        });

        TableColumn<Inmueble,Number> cVis = numCol("Visitas","contadorVisitas",60);

        TableColumn<Inmueble,Void> cAcc = new TableColumn<>("Acciones");
        cAcc.setMinWidth(110); cAcc.setMaxWidth(110);
        cAcc.setCellFactory(tc -> new TableCell<>() {
            final Button bEdit = new Button("✏"); final Button bDel = new Button("🗑");
            final HBox box = new HBox(4, bEdit, bDel);
            { bEdit.getStyleClass().add("btn-icon"); bDel.getStyleClass().add("btn-icon");
              box.setAlignment(Pos.CENTER);
              bEdit.setOnAction(e -> abrirFormulario(getTableView().getItems().get(getIndex())));
              bDel.setOnAction(e  -> confirmarEliminar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v,boolean empty){ super.updateItem(v,empty); setGraphic(empty?null:box); }
        });

        tabla.getColumns().addAll(cCod,cDir,cCiud,cBarr,cTipo,cFin,cPrecio,cArea,cHab,cBan,cEst,cVis,cAcc);
        tabla.setItems(datos);
        return tabla;
    }

    // ── Pie de página ──────────────────────────────────────────────────────
    private HBox buildPiePagina() {
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER_RIGHT);

        Button btnOrden = new Button("⬆ Ordenar por Precio (BST)");
        btnOrden.getStyleClass().add("btn-secondary");
        btnOrden.setOnAction(e -> datos.setAll(svc.inmueblesOrdenadosPorPrecio()));

        Button btnRango = new Button("💰 Buscar por Rango de Precio");
        btnRango.getStyleClass().add("btn-secondary");
        btnRango.setOnAction(e -> buscarRango());

        Button btnRec = new Button("💡 Recomendar a Cliente");
        btnRec.getStyleClass().add("btn-secondary");
        btnRec.setOnAction(e -> mostrarRecomendaciones());

        hb.getChildren().addAll(btnOrden, btnRango, btnRec);
        return hb;
    }

    // ── Lógica ─────────────────────────────────────────────────────────────
    private void cargarDatos()  { datos.setAll(svc.getTodosInmuebles()); }

    private void buscarLocal(String q) {
        if (q == null || q.isBlank()) { cargarDatos(); return; }
        String lq = q.toLowerCase();
        datos.setAll(svc.getTodosInmuebles().stream().filter(i ->
            i.getCodigo().toLowerCase().contains(lq) ||
            i.getDireccion().toLowerCase().contains(lq) ||
            i.getBarrio().toLowerCase().contains(lq) ||
            i.getCiudad().toLowerCase().contains(lq)).toList());
    }

    private void aplicarFiltros() {
        String tipo = "Todos".equals(cmbTipo.getValue())     ? null : cmbTipo.getValue();
        String fin  = "Todos".equals(cmbFinalidad.getValue())? null : cmbFinalidad.getValue();
        double max  = 0;
        try { max = Double.parseDouble(txtPrecioMax.getText().replace(",","").replace(".","").trim()); }
        catch (Exception ignored) {}
        datos.setAll(svc.filtrarInmuebles(tipo, fin, max, 0, txtCiudad.getText().trim()));
    }

    private void limpiarFiltros() {
        cmbTipo.setValue("Todos"); cmbFinalidad.setValue("Todos");
        txtPrecioMax.clear(); txtCiudad.clear(); txtBusqueda.clear();
        cargarDatos();
    }

    private void buscarRango() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Buscar por rango de precio (ArbolBST)");
        dlg.setHeaderText("Ingrese el rango de precio:");
        TextField tMin = new TextField(); tMin.setPromptText("Mínimo");
        TextField tMax = new TextField(); tMax.setPromptText("Máximo");
        GridPane gp = new GridPane(); gp.setHgap(10); gp.setVgap(8); gp.setPadding(new Insets(12));
        gp.add(new Label("Precio mínimo:"),0,0); gp.add(tMin,1,0);
        gp.add(new Label("Precio máximo:"),0,1); gp.add(tMax,1,1);
        dlg.getDialogPane().setContent(gp);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    double min = Double.parseDouble(tMin.getText().replace(",","").trim());
                    double max = Double.parseDouble(tMax.getText().replace(",","").trim());
                    List<Inmueble> r = svc.inmueblesPorRango(min, max);
                    datos.setAll(r);
                    mostrarInfo("ArbolBST encontró " + r.size() + " inmuebles en ese rango.");
                } catch (NumberFormatException e) {
                    mostrarError("Ingrese valores numéricos válidos.");
                }
            }
        });
    }

    private void mostrarRecomendaciones() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Recomendaciones (Grafo + TablaHash)");
        dlg.setHeaderText("Ingrese la identificación del cliente:");
        dlg.setContentText("ID Cliente:");
        dlg.showAndWait().ifPresent(id -> {
            List<Inmueble> recs = svc.recomendar(id.trim());
            if (recs.isEmpty()) { mostrarInfo("Sin recomendaciones para ese cliente."); return; }
            StringBuilder sb = new StringBuilder();
            recs.forEach(i -> sb.append("• ").append(i).append("\n"));
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Recomendaciones"); a.setHeaderText("Top " + recs.size() + " para " + id);
            a.setContentText(sb.toString()); a.showAndWait();
        });
    }

    private void abrirFormulario(Inmueble inmueble) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(inmueble == null ? "Nuevo Inmueble" : "Editar Inmueble");
        win.setWidth(520); win.setHeight(580);

        GridPane gp = new GridPane();
        gp.setHgap(12); gp.setVgap(10); gp.setPadding(new Insets(20));
        gp.setStyle("-fx-background-color:white;");

        TextField tCod  = field(inmueble != null ? inmueble.getCodigo()    : "");
        TextField tDir  = field(inmueble != null ? inmueble.getDireccion() : "");
        TextField tCiud = field(inmueble != null ? inmueble.getCiudad()    : "");
        TextField tBarr = field(inmueble != null ? inmueble.getBarrio()    : "");
        TextField tPrec = field(inmueble != null ? String.valueOf((long)inmueble.getPrecio()) : "");
        TextField tArea = field(inmueble != null ? String.valueOf((long)inmueble.getArea())   : "");
        TextField tHab  = field(inmueble != null ? String.valueOf(inmueble.getHabitaciones()) : "0");
        TextField tBan  = field(inmueble != null ? String.valueOf(inmueble.getBanos())        : "0");

        ComboBox<String> cTipo = new ComboBox<>(FXCollections.observableArrayList(
            "APARTAMENTO","CASA","LOCAL_COMERCIAL","OFICINA","LOTE","BODEGA"));
        cTipo.setValue(inmueble != null ? inmueble.getTipo().name() : "APARTAMENTO");

        ComboBox<String> cFin = new ComboBox<>(FXCollections.observableArrayList("VENTA","ARRIENDO"));
        cFin.setValue(inmueble != null ? inmueble.getFinalidad().name() : "ARRIENDO");

        ComboBox<String> cEst = new ComboBox<>(FXCollections.observableArrayList(
            "DISPONIBLE","RESERVADO","ARRENDADO","VENDIDO"));
        cEst.setValue(inmueble != null ? inmueble.getEstado().name() : "DISPONIBLE");

        List<Asesor> asesores = svc.getTodosAsesores();
        ComboBox<String> cAsesor = new ComboBox<>();
        asesores.forEach(a -> cAsesor.getItems().add(a.getCodigo() + " — " + a.getNombre()));
        if (inmueble != null && inmueble.getCodigoAsesor() != null)
            cAsesor.getItems().stream()
                .filter(s -> s.startsWith(inmueble.getCodigoAsesor()))
                .findFirst().ifPresent(cAsesor::setValue);
        else if (!cAsesor.getItems().isEmpty()) cAsesor.setValue(cAsesor.getItems().get(0));

        if (inmueble != null) tCod.setDisable(true);

        int r = 0;
        gp.add(lbl("Código *"),0,r); gp.add(tCod,1,r++);
        gp.add(lbl("Dirección *"),0,r); gp.add(tDir,1,r++);
        gp.add(lbl("Ciudad *"),0,r); gp.add(tCiud,1,r++);
        gp.add(lbl("Barrio / Zona"),0,r); gp.add(tBarr,1,r++);
        gp.add(lbl("Tipo"),0,r); gp.add(cTipo,1,r++);
        gp.add(lbl("Finalidad"),0,r); gp.add(cFin,1,r++);
        gp.add(lbl("Precio *"),0,r); gp.add(tPrec,1,r++);
        gp.add(lbl("Área m²"),0,r); gp.add(tArea,1,r++);
        gp.add(lbl("Habitaciones"),0,r); gp.add(tHab,1,r++);
        gp.add(lbl("Baños"),0,r); gp.add(tBan,1,r++);
        gp.add(lbl("Estado"),0,r); gp.add(cEst,1,r++);
        gp.add(lbl("Asesor"),0,r); gp.add(cAsesor,1,r++);

        for (int i = 0; i < gp.getChildren().size(); i++) {
            javafx.scene.Node n = gp.getChildren().get(i);
            if (n instanceof ComboBox<?> cb) cb.setPrefWidth(260);
            if (n instanceof TextField tf && !(tf == tCod && inmueble != null)) tf.setPrefWidth(260);
        }

        Button btnGuardar = new Button(inmueble == null ? "Registrar" : "Actualizar");
        btnGuardar.getStyleClass().add("btn-primary");
        Button btnCancelar = new Button("Cancelar"); btnCancelar.getStyleClass().add("btn-icon");
        btnCancelar.setOnAction(e -> win.close());

        HBox botones = new HBox(10, btnCancelar, btnGuardar);
        botones.setAlignment(Pos.CENTER_RIGHT); botones.setPadding(new Insets(10, 20, 16, 20));

        btnGuardar.setOnAction(e -> {
            try {
                if (tCod.getText().isBlank() || tDir.getText().isBlank() || tCiud.getText().isBlank() || tPrec.getText().isBlank())
                    throw new IllegalArgumentException("Código, dirección, ciudad y precio son obligatorios.");

                String codAsesor = cAsesor.getValue() != null ? cAsesor.getValue().split(" — ")[0] : null;
                Inmueble obj = inmueble != null ? inmueble : new Inmueble();
                if (inmueble == null) obj.setCodigo(tCod.getText().trim());
                obj.setDireccion(tDir.getText().trim());
                obj.setCiudad(tCiud.getText().trim());
                obj.setBarrio(tBarr.getText().trim());
                obj.setTipo(Inmueble.Tipo.valueOf(cTipo.getValue()));
                obj.setFinalidad(Inmueble.Finalidad.valueOf(cFin.getValue()));
                obj.setPrecio(Double.parseDouble(tPrec.getText().replace(",","").replace(".","").trim()));
                obj.setArea(tArea.getText().isBlank() ? 0 : Double.parseDouble(tArea.getText().trim()));
                obj.setHabitaciones(tHab.getText().isBlank() ? 0 : Integer.parseInt(tHab.getText().trim()));
                obj.setBanos(tBan.getText().isBlank() ? 0 : Integer.parseInt(tBan.getText().trim()));
                obj.setEstado(Inmueble.Estado.valueOf(cEst.getValue()));
                obj.setCodigoAsesor(codAsesor);

                boolean ok = inmueble == null ? svc.registrarInmueble(obj) : svc.actualizarInmueble(obj);
                if (!ok) throw new IllegalStateException("Ya existe un inmueble con ese código.");
                cargarDatos(); win.close();
            } catch (Exception ex) { mostrarError(ex.getMessage()); }
        });

        VBox layout = new VBox(gp, botones);
        layout.setStyle("-fx-background-color:white;");
        win.setScene(new Scene(layout));
        win.show();
    }

    private void confirmarEliminar(Inmueble i) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar inmueble " + i.getCodigo() + " — " + i.getDireccion() + "?",
            ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmar eliminación");
        a.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) { svc.eliminarInmueble(i.getCodigo()); cargarDatos(); } });
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private TableColumn<Inmueble,String> strCol(String name, String field, int w) {
        TableColumn<Inmueble,String> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(field)); c.setMinWidth(w); return c;
    }
    private TableColumn<Inmueble,Number> numCol(String name, String field, int w) {
        TableColumn<Inmueble,Number> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(field)); c.setMinWidth(w); return c;
    }
    private <T> TableCell<Inmueble,T> badgeCell(String css) {
        return new TableCell<>() {
            @Override protected void updateItem(T v, boolean empty) {
                super.updateItem(v,empty); setText(null); setGraphic(null);
                if (!empty && v != null) { Label l = new Label(v.toString()); l.getStyleClass().add(css); setGraphic(l); }
            }
        };
    }
    private TextField field(String val) { TextField t = new TextField(val); t.getStyleClass().add("text-field"); return t; }
    private Label     lbl(String txt)   { Label l = new Label(txt); l.getStyleClass().add("field-label"); l.setMinWidth(110); return l; }
    private void mostrarError(String msg){ Alert a=new Alert(Alert.AlertType.ERROR,msg); a.setTitle("Error"); a.showAndWait(); }
    private void mostrarInfo(String msg) { Alert a=new Alert(Alert.AlertType.INFORMATION,msg); a.setTitle("Info"); a.showAndWait(); }
}