package proptech.vista.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import proptech.modelo.Cliente;
import proptech.servicios.PropTechService;

public class ClientesController {

    private final PropTechService svc = PropTechService.getInstance();
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();
    private TableView<Cliente> tabla;
    private TextField txtBusqueda;

    public Pane build() {
        VBox root = new VBox(14);
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setPadding(new Insets(4));
        root.getChildren().addAll(buildHeader(), buildTabla(), buildPie());
        VBox.setVgrow(buildTabla(), Priority.ALWAYS);
        cargarDatos();
        return root;
    }

    private HBox buildHeader() {
        HBox hb = new HBox(12);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:16 20;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        Label title = new Label("👤 Gestión de Clientes");
        title.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#1a202c;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("🔍  Buscar por nombre o identificación...");
        txtBusqueda.getStyleClass().add("search-field");
        txtBusqueda.setPrefWidth(270);
        txtBusqueda.textProperty().addListener((o, a, n) -> buscarLocal(n));

        Button btnNuevo = new Button("＋  Nuevo Cliente");
        btnNuevo.getStyleClass().add("btn-primary");
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, sp, txtBusqueda, btnNuevo);
        return hb;
    }

    @SuppressWarnings("unchecked")
    private TableView<Cliente> buildTabla() {
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(Double.MAX_VALUE);
        tabla.setPlaceholder(new Label("Sin clientes registrados."));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Cliente,String> cId   = strCol("Identificación","identificacion",110);
        TableColumn<Cliente,String> cNom  = strCol("Nombre",        "nombre",        160);
        TableColumn<Cliente,String> cCor  = strCol("Correo",        "correo",        170);
        TableColumn<Cliente,String> cTel  = strCol("Teléfono",      "telefono",      110);

        TableColumn<Cliente,Object> cTipo = new TableColumn<>("Tipo");
        cTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        cTipo.setMinWidth(100);
        cTipo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Object v, boolean empty) {
                super.updateItem(v,empty); setText(null); setGraphic(null);
                if (!empty && v != null) {
                    Label l = new Label(v.toString());
                    l.getStyleClass().add("VIP".equals(v.toString()) ? "badge-purple" : "badge-blue");
                    setGraphic(l);
                }
            }
        });

        TableColumn<Cliente,Number> cPres = new TableColumn<>("Presupuesto");
        cPres.setCellValueFactory(cd -> cd.getValue().presupuestoProperty());
        cPres.setMinWidth(130);
        cPres.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v,empty);
                setText(empty||v==null ? null : "$"+String.format("%,.0f",v.doubleValue()));
            }
        });

        TableColumn<Cliente,String> cZona = strCol("Zona Interés",      "zonaInteres",          110);
        TableColumn<Cliente,String> cInm  = strCol("Inmueble Deseado",  "tipoInmuebleDeseado",  130);

        TableColumn<Cliente,Object> cEst = new TableColumn<>("Estado");
        cEst.setCellValueFactory(new PropertyValueFactory<>("estadoBusqueda"));
        cEst.setMinWidth(110);
        cEst.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Object v, boolean empty) {
                super.updateItem(v,empty); setText(null); setGraphic(null);
                if (!empty && v != null) {
                    Label l = new Label(v.toString());
                    l.getStyleClass().add(switch(v.toString()) {
                        case "ACTIVO"         -> "badge-green";
                        case "EN_NEGOCIACION" -> "badge-orange";
                        case "CERRADO"        -> "badge-blue";
                        default               -> "badge-red";
                    });
                    setGraphic(l);
                }
            }
        });

        TableColumn<Cliente,Void> cAcc = new TableColumn<>("Acciones");
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

        tabla.getColumns().addAll(cId,cNom,cCor,cTel,cTipo,cPres,cZona,cInm,cEst,cAcc);
        tabla.setItems(datos);
        return tabla;
    }

    private HBox buildPie() {
        HBox hb = new HBox(10); hb.setAlignment(Pos.CENTER_RIGHT);
        Button btnOrden = new Button("⬆ Ordenar por Presupuesto");
        btnOrden.getStyleClass().add("btn-secondary");
        btnOrden.setOnAction(e -> {
            // Usa el servicio que internamente usa el ArbolBST
            java.util.List<Cliente> todos = svc.getTodosClientes();
            todos.sort(java.util.Comparator.comparingDouble(Cliente::getPresupuesto));
            datos.setAll(todos);
        });
        hb.getChildren().add(btnOrden);
        return hb;
    }

    private void cargarDatos() { datos.setAll(svc.getTodosClientes()); }

    private void buscarLocal(String q) {
        if (q == null || q.isBlank()) { cargarDatos(); return; }
        String lq = q.toLowerCase();
        datos.setAll(svc.getTodosClientes().stream().filter(c ->
            c.getNombre().toLowerCase().contains(lq) ||
            c.getIdentificacion().toLowerCase().contains(lq) ||
            (c.getCorreo() != null && c.getCorreo().toLowerCase().contains(lq))).toList());
    }

    private void abrirFormulario(Cliente cliente) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(cliente == null ? "Nuevo Cliente" : "Editar Cliente");
        win.setWidth(500); win.setHeight(560);

        GridPane gp = new GridPane();
        gp.setHgap(12); gp.setVgap(10); gp.setPadding(new Insets(20));
        gp.setStyle("-fx-background-color:white;");

        TextField tId   = field(cliente != null ? cliente.getIdentificacion() : "");
        TextField tNom  = field(cliente != null ? cliente.getNombre()          : "");
        TextField tCor  = field(cliente != null ? cliente.getCorreo()          : "");
        TextField tTel  = field(cliente != null ? cliente.getTelefono()        : "");
        TextField tPres = field(cliente != null ? String.valueOf((long)cliente.getPresupuesto()) : "");
        TextField tZona = field(cliente != null ? cliente.getZonaInteres()     : "");
        TextField tHab  = field(cliente != null ? String.valueOf(cliente.getMinHabitaciones())   : "0");

        ComboBox<String> cTipo = new ComboBox<>(FXCollections.observableArrayList(
            "COMPRADOR","ARRENDATARIO","INVERSOR","VIP"));
        cTipo.setValue(cliente != null ? cliente.getTipo().name() : "COMPRADOR");

        ComboBox<String> cInm = new ComboBox<>(FXCollections.observableArrayList(
            "APARTAMENTO","CASA","LOCAL_COMERCIAL","OFICINA","LOTE","BODEGA",""));
        cInm.setValue(cliente != null && cliente.getTipoInmuebleDeseado() != null
                      ? cliente.getTipoInmuebleDeseado() : "APARTAMENTO");

        ComboBox<String> cEst = new ComboBox<>(FXCollections.observableArrayList(
            "ACTIVO","EN_NEGOCIACION","PAUSADO","CERRADO"));
        cEst.setValue(cliente != null ? cliente.getEstadoBusqueda().name() : "ACTIVO");

        if (cliente != null) tId.setDisable(true);

        int r = 0;
        gp.add(lbl("Identificación *"),0,r); gp.add(tId,1,r++);
        gp.add(lbl("Nombre *"),0,r);         gp.add(tNom,1,r++);
        gp.add(lbl("Correo"),0,r);           gp.add(tCor,1,r++);
        gp.add(lbl("Teléfono"),0,r);         gp.add(tTel,1,r++);
        gp.add(lbl("Tipo"),0,r);             gp.add(cTipo,1,r++);
        gp.add(lbl("Presupuesto *"),0,r);    gp.add(tPres,1,r++);
        gp.add(lbl("Zona de Interés"),0,r);  gp.add(tZona,1,r++);
        gp.add(lbl("Inmueble Deseado"),0,r); gp.add(cInm,1,r++);
        gp.add(lbl("Mín. Habitaciones"),0,r);gp.add(tHab,1,r++);
        gp.add(lbl("Estado Búsqueda"),0,r);  gp.add(cEst,1,r++);

        gp.getChildren().forEach(n -> {
            if (n instanceof ComboBox<?> cb) cb.setPrefWidth(250);
            if (n instanceof TextField tf)  tf.setPrefWidth(250);
        });

        Button btnG = new Button(cliente == null ? "Registrar" : "Actualizar");
        btnG.getStyleClass().add("btn-primary");
        Button btnC = new Button("Cancelar"); btnC.getStyleClass().add("btn-icon");
        btnC.setOnAction(e -> win.close());

        btnG.setOnAction(e -> {
            try {
                if (tId.getText().isBlank() || tNom.getText().isBlank() || tPres.getText().isBlank())
                    throw new IllegalArgumentException("Identificación, nombre y presupuesto son obligatorios.");
                Cliente obj = cliente != null ? cliente : new Cliente();
                if (cliente == null) obj.setIdentificacion(tId.getText().trim());
                obj.setNombre(tNom.getText().trim());
                obj.setCorreo(tCor.getText().trim());
                obj.setTelefono(tTel.getText().trim());
                obj.setTipo(Cliente.Tipo.valueOf(cTipo.getValue()));
                obj.setPresupuesto(Double.parseDouble(tPres.getText().replace(",","").replace(".","").trim()));
                obj.setZonaInteres(tZona.getText().trim());
                obj.setTipoInmuebleDeseado(cInm.getValue());
                obj.setMinHabitaciones(tHab.getText().isBlank() ? 0 : Integer.parseInt(tHab.getText().trim()));
                obj.setEstadoBusqueda(Cliente.Estado.valueOf(cEst.getValue()));

                boolean ok = cliente == null ? svc.registrarCliente(obj) : svc.actualizarCliente(obj);
                if (!ok) throw new IllegalStateException("Ya existe un cliente con esa identificación.");
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

    private void confirmarEliminar(Cliente c) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar cliente " + c.getNombre() + " (" + c.getIdentificacion() + ")?",
            ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmar eliminación");
        a.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) { svc.eliminarCliente(c.getIdentificacion()); cargarDatos(); } });
    }

    private TableColumn<Cliente,String> strCol(String name, String field, int w) {
        TableColumn<Cliente,String> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(field)); c.setMinWidth(w); return c;
    }
    private TextField field(String v) { TextField t = new TextField(v); t.getStyleClass().add("text-field"); return t; }
    private Label     lbl(String txt) { Label l = new Label(txt); l.getStyleClass().add("field-label"); l.setMinWidth(120); return l; }
    private void mostrarError(String m){ Alert a=new Alert(Alert.AlertType.ERROR,m); a.setTitle("Error"); a.showAndWait(); }
}