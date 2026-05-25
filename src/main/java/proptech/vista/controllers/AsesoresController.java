package proptech.vista.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
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
        VBox root = new VBox(14);
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setPadding(new Insets(4));
        root.getChildren().addAll(buildHeader(), buildTabla());
        VBox.setVgrow(buildTabla(), Priority.ALWAYS);
        cargarDatos();
        return root;
    }

    private HBox buildHeader() {
        HBox hb = new HBox(12); hb.setAlignment(Pos.CENTER_LEFT);
        hb.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:16 20;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        Label title = new Label("👔 Gestión de Asesores");
        title.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#1a202c;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("🔍  Buscar por nombre o zona...");
        txtBusqueda.getStyleClass().add("search-field");
        txtBusqueda.setPrefWidth(240);
        txtBusqueda.textProperty().addListener((o, a, n) -> buscarLocal(n));

        Button btnNuevo = new Button("＋  Nuevo Asesor");
        btnNuevo.getStyleClass().add("btn-primary");
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, sp, txtBusqueda, btnNuevo);
        return hb;
    }

    @SuppressWarnings("unchecked")
    private TableView<Asesor> buildTabla() {
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(Double.MAX_VALUE);
        tabla.setPlaceholder(new Label("Sin asesores registrados."));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Asesor,String> cCod  = strCol("Código",       "codigo",            80);
        TableColumn<Asesor,String> cNom  = strCol("Nombre",       "nombre",           170);
        TableColumn<Asesor,String> cCont = strCol("Contacto",     "contacto",         130);
        TableColumn<Asesor,String> cEsp  = strCol("Especialidad", "especialidad",     130);
        TableColumn<Asesor,String> cZona = strCol("Zona Asignada","zonaAsignada",     120);

        TableColumn<Asesor,Number> cCier = new TableColumn<>("Cierres");
        cCier.setCellValueFactory(new PropertyValueFactory<>("cierresRealizados"));
        cCier.setMinWidth(75);

        TableColumn<Asesor,Number> cCom = new TableColumn<>("Comisión Total");
        cCom.setCellValueFactory(cd -> cd.getValue().comisionTotalProperty());
        cCom.setMinWidth(130);
        cCom.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v,empty);
                setText(empty||v==null ? null : "$"+String.format("%,.0f",v.doubleValue()));
            }
        });

        // Columna de efectividad calculada
        TableColumn<Asesor,String> cEfec = new TableColumn<>("Efectividad");
        cEfec.setMinWidth(100);
        cEfec.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); setGraphic(null); setText(null);
                if (!empty) {
                    Asesor a = getTableView().getItems().get(getIndex());
                    int cierres = a.getCierresRealizados();
                    Label l = new Label(cierres == 0 ? "Sin cierres" : cierres + " cierres");
                    l.getStyleClass().add(cierres >= 3 ? "badge-green" : cierres >= 1 ? "badge-orange" : "badge-red");
                    setGraphic(l);
                }
            }
        });

        TableColumn<Asesor,Void> cAcc = new TableColumn<>("Acciones");
        cAcc.setMinWidth(110); cAcc.setMaxWidth(110);
        cAcc.setCellFactory(tc -> new TableCell<>() {
            final Button bEdit = new Button("✏"); final Button bDel = new Button("🗑");
            final HBox box = new HBox(4, bEdit, bDel);
            { bEdit.getStyleClass().add("btn-icon"); bDel.getStyleClass().add("btn-icon");
              box.setAlignment(Pos.CENTER);
              bEdit.setOnAction(e -> abrirFormulario(getTableView().getItems().get(getIndex())));
              bDel.setOnAction(e  -> confirmarEliminar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty){ super.updateItem(v,empty); setGraphic(empty?null:box); }
        });

        tabla.getColumns().addAll(cCod,cNom,cCont,cEsp,cZona,cCier,cCom,cEfec,cAcc);
        tabla.setItems(datos);
        return tabla;
    }

    private void cargarDatos() { datos.setAll(svc.getTodosAsesores()); }

    private void buscarLocal(String q) {
        if (q == null || q.isBlank()) { cargarDatos(); return; }
        String lq = q.toLowerCase();
        datos.setAll(svc.getTodosAsesores().stream().filter(a ->
            a.getNombre().toLowerCase().contains(lq) ||
            a.getCodigo().toLowerCase().contains(lq) ||
            (a.getZonaAsignada() != null && a.getZonaAsignada().toLowerCase().contains(lq))).toList());
    }

    private void abrirFormulario(Asesor asesor) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(asesor == null ? "Nuevo Asesor" : "Editar Asesor");
        win.setWidth(450); win.setHeight(360);

        GridPane gp = new GridPane();
        gp.setHgap(12); gp.setVgap(10); gp.setPadding(new Insets(20));
        gp.setStyle("-fx-background-color:white;");

        TextField tCod  = field(asesor != null ? asesor.getCodigo()       : "");
        TextField tNom  = field(asesor != null ? asesor.getNombre()       : "");
        TextField tCont = field(asesor != null ? asesor.getContacto()     : "");
        TextField tEsp  = field(asesor != null ? asesor.getEspecialidad() : "");
        TextField tZona = field(asesor != null ? asesor.getZonaAsignada() : "");

        if (asesor != null) tCod.setDisable(true);

        int r = 0;
        gp.add(lbl("Código *"),      0,r); gp.add(tCod,1,r++);
        gp.add(lbl("Nombre *"),      0,r); gp.add(tNom,1,r++);
        gp.add(lbl("Contacto"),      0,r); gp.add(tCont,1,r++);
        gp.add(lbl("Especialidad"),  0,r); gp.add(tEsp,1,r++);
        gp.add(lbl("Zona Asignada"), 0,r); gp.add(tZona,1,r++);

        gp.getChildren().forEach(n -> { if (n instanceof TextField tf) tf.setPrefWidth(240); });

        Button btnG = new Button(asesor == null ? "Registrar" : "Actualizar");
        btnG.getStyleClass().add("btn-primary");
        Button btnC = new Button("Cancelar"); btnC.getStyleClass().add("btn-icon");
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
            } catch (Exception ex) { mostrarError(ex.getMessage()); }
        });

        HBox botones = new HBox(10, btnC, btnG);
        botones.setAlignment(Pos.CENTER_RIGHT); botones.setPadding(new Insets(10,20,16,20));

        VBox layout = new VBox(gp, botones);
        layout.setStyle("-fx-background-color:white;");
        win.setScene(new Scene(layout));
        win.show();
    }

    private void confirmarEliminar(Asesor a) {
        Alert al = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar asesor " + a.getNombre() + " (" + a.getCodigo() + ")?",
            ButtonType.YES, ButtonType.NO);
        al.setTitle("Confirmar eliminación");
        al.showAndWait().ifPresent(r -> { if (r==ButtonType.YES){ svc.eliminarAsesor(a.getCodigo()); cargarDatos(); } });
    }

    private TableColumn<Asesor,String> strCol(String name, String field, int w) {
        TableColumn<Asesor,String> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(field)); c.setMinWidth(w); return c;
    }
    private TextField field(String v) { TextField t = new TextField(v); t.getStyleClass().add("text-field"); return t; }
    private Label     lbl(String txt) { Label l = new Label(txt); l.getStyleClass().add("field-label"); l.setMinWidth(110); return l; }
    private void mostrarError(String m){ Alert a=new Alert(Alert.AlertType.ERROR,m); a.setTitle("Error"); a.showAndWait(); }
}