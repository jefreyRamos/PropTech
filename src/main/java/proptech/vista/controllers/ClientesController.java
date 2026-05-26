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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
import proptech.modelo.Cliente;
import proptech.servicios.PropTechService;

public class ClientesController {

    private final PropTechService svc = PropTechService.getInstance();
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();
    private TableView<Cliente> tabla;
    private TextField txtBusqueda;

    public Pane build() {
        VBox root = new VBox(12);
        root.getStyleClass().add("modulo-root");
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);

        tabla = buildTabla();          // construir primero para poder hacer VGrow
        VBox.setVgrow(tabla, Priority.ALWAYS);

        root.getChildren().addAll(
            buildHeader(),
            tabla,
            buildAccionesBar()
        );
        cargarDatos();
        return root;
    }

    private HBox buildHeader() {
        HBox hb = new HBox(12);
        hb.getStyleClass().add("module-header");
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("👤  Gestión de Clientes");
        title.getStyleClass().add("module-title");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("🔍  Buscar nombre o identificación...");
        txtBusqueda.getStyleClass().add("search-field");
        txtBusqueda.setPrefWidth(280);
        txtBusqueda.textProperty().addListener((o,a,n) -> buscarLocal(n));

        Button btnNuevo = new Button("＋  Nuevo Cliente");
        btnNuevo.getStyleClass().add("btn-primary");
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, sp, txtBusqueda, btnNuevo);
        return hb;
    }

    @SuppressWarnings("unchecked")
    private TableView<Cliente> buildTabla() {
        TableView<Cliente> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setMaxWidth(Double.MAX_VALUE);
        t.setMaxHeight(Double.MAX_VALUE);
        t.setPlaceholder(new Label("Sin clientes registrados.  Haz clic en '＋ Nuevo Cliente'."));

        TableColumn<Cliente,String> cId   = sc("Identificación","identificacion",120);
        TableColumn<Cliente,String> cNom  = sc("Nombre",        "nombre",        200);
        TableColumn<Cliente,String> cCor  = sc("Correo",        "correo",        190);
        TableColumn<Cliente,String> cTel  = sc("Teléfono",      "telefono",      120);

        TableColumn<Cliente,Object> cTipo = new TableColumn<>("Tipo");
        cTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        cTipo.setMinWidth(110);
        cTipo.setCellFactory(tc -> badge(v -> "VIP".equals(v) ? "badge-purple" : "badge-blue"));

        TableColumn<Cliente,Number> cPres = new TableColumn<>("Presupuesto");
        cPres.setCellValueFactory(cd -> cd.getValue().presupuestoProperty());
        cPres.setMinWidth(140);
        cPres.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v,empty);
                if (empty||v==null) { setText(null); return; }
                setText("$" + String.format("%,.0f", v.doubleValue()));
                setStyle("-fx-font-weight:bold; -fx-text-fill:#1e293b;");
            }
        });

        TableColumn<Cliente,String> cZona = sc("Zona Interés",     "zonaInteres",         130);
        TableColumn<Cliente,String> cInm  = sc("Inmueble Deseado", "tipoInmuebleDeseado", 140);

        TableColumn<Cliente,Object> cEst = new TableColumn<>("Estado");
        cEst.setCellValueFactory(new PropertyValueFactory<>("estadoBusqueda"));
        cEst.setMinWidth(120);
        cEst.setCellFactory(tc -> badge(v -> switch(v) {
            case "ACTIVO"         -> "badge-green";
            case "EN_NEGOCIACION" -> "badge-orange";
            case "CERRADO"        -> "badge-blue";
            default               -> "badge-red";
        }));

        TableColumn<Cliente,Void> cAcc = accionesCol(
            i -> abrirFormulario(datos.get(i)),
            i -> confirmarEliminar(datos.get(i))
        );

        t.getColumns().addAll(cId,cNom,cCor,cTel,cTipo,cPres,cZona,cInm,cEst,cAcc);
        t.setItems(datos);
        tabla = t;
        return t;
    }

    private HBox buildAccionesBar() {
        HBox bar = new HBox(10);
        bar.getStyleClass().add("acciones-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setMaxWidth(Double.MAX_VALUE);

        Label lbl = new Label("Herramientas:");
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8; -fx-font-weight:bold;");

        Button btnOrden = new Button("⬆  Ordenar por Presupuesto");
        btnOrden.getStyleClass().add("btn-secondary");
        btnOrden.setOnAction(e -> {
            java.util.List<Cliente> lista = svc.getTodosClientes();
            lista.sort(java.util.Comparator.comparingDouble(Cliente::getPresupuesto));
            datos.setAll(lista);
        });

        Button btnReload = new Button("↺");
        btnReload.getStyleClass().add("btn-icon");
        btnReload.setTooltip(new Tooltip("Recargar"));
        btnReload.setOnAction(e -> cargarDatos());

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        bar.getChildren().addAll(lbl, sp, btnOrden, btnReload);
        return bar;
    }

    private void cargarDatos() { datos.setAll(svc.getTodosClientes()); }

    private void buscarLocal(String q) {
        if (q==null||q.isBlank()) { cargarDatos(); return; }
        String lq = q.toLowerCase();
        datos.setAll(svc.getTodosClientes().stream().filter(c ->
            safe(c.getNombre()).contains(lq) || safe(c.getIdentificacion()).contains(lq) ||
            safe(c.getCorreo()).contains(lq)).toList());
    }

    private void abrirFormulario(Cliente cliente) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(cliente==null ? "➕ Nuevo Cliente" : "✏ Editar Cliente");
        win.setWidth(500); win.setHeight(560); win.setResizable(false);

        GridPane gp = formGrid();

        TextField tId   = f(cliente!=null ? cliente.getIdentificacion()    : "");
        TextField tNom  = f(cliente!=null ? cliente.getNombre()            : "");
        TextField tCor  = f(cliente!=null ? cliente.getCorreo()            : "");
        TextField tTel  = f(cliente!=null ? cliente.getTelefono()          : "");
        TextField tPres = f(cliente!=null ? String.format("%.0f", cliente.getPresupuesto()) : "");
        TextField tZona = f(cliente!=null ? cliente.getZonaInteres()       : "");
        TextField tHab  = f(cliente!=null ? String.valueOf(cliente.getMinHabitaciones()) : "0");

        ComboBox<String> cTipo = cmb("COMPRADOR","ARRENDATARIO","INVERSOR","VIP");
        cTipo.setValue(cliente!=null ? cliente.getTipo().name() : "COMPRADOR");

        ComboBox<String> cInm = cmb("APARTAMENTO","CASA","LOCAL_COMERCIAL","OFICINA","LOTE","BODEGA");
        cInm.setValue(cliente!=null && cliente.getTipoInmuebleDeseado()!=null
                      ? cliente.getTipoInmuebleDeseado() : "APARTAMENTO");

        ComboBox<String> cEst = cmb("ACTIVO","EN_NEGOCIACION","PAUSADO","CERRADO");
        cEst.setValue(cliente!=null ? cliente.getEstadoBusqueda().name() : "ACTIVO");

        if (cliente!=null) tId.setDisable(true);

        int r=0;
        row(gp,r++,"Identificación *", tId);
        row(gp,r++,"Nombre *",         tNom);
        row(gp,r++,"Correo",           tCor);
        row(gp,r++,"Teléfono",         tTel);
        row(gp,r++,"Tipo",             cTipo);
        row(gp,r++,"Presupuesto *",    tPres);
        row(gp,r++,"Zona de Interés",  tZona);
        row(gp,r++,"Inmueble Deseado", cInm);
        row(gp,r++,"Mín. Habitaciones",tHab);
        row(gp,r++,"Estado Búsqueda",  cEst);

        Button btnG = new Button(cliente==null ? "✔  Registrar" : "✔  Actualizar");
        btnG.getStyleClass().add("btn-primary");
        Button btnC = new Button("Cancelar"); btnC.getStyleClass().add("btn-icon");
        btnC.setOnAction(e -> win.close());

        btnG.setOnAction(e -> {
            try {
                if (tId.getText().isBlank()||tNom.getText().isBlank()||tPres.getText().isBlank())
                    throw new IllegalArgumentException("Identificación, nombre y presupuesto son obligatorios.");
                Cliente obj = cliente!=null ? cliente : new Cliente();
                if (cliente==null) obj.setIdentificacion(tId.getText().trim());
                obj.setNombre(tNom.getText().trim()); obj.setCorreo(tCor.getText().trim());
                obj.setTelefono(tTel.getText().trim()); obj.setTipo(Cliente.Tipo.valueOf(cTipo.getValue()));
                obj.setPresupuesto(pd(tPres.getText())); obj.setZonaInteres(tZona.getText().trim());
                obj.setTipoInmuebleDeseado(cInm.getValue()); obj.setMinHabitaciones((int)pd(tHab.getText()));
                obj.setEstadoBusqueda(Cliente.Estado.valueOf(cEst.getValue()));
                boolean ok = cliente==null ? svc.registrarCliente(obj) : svc.actualizarCliente(obj);
                if (!ok) throw new IllegalStateException("Ya existe un cliente con esa identificación.");
                cargarDatos(); win.close();
            } catch (Exception ex) { err(ex.getMessage()); }
        });

        win.setScene(new Scene(dlgLayout(gp, btnC, btnG)));
        win.show();
    }

    private void confirmarEliminar(Cliente c) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar cliente " + c.getNombre() + " (" + c.getIdentificacion() + ")?",
            ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmar eliminación");
        a.showAndWait().ifPresent(r -> { if (r==ButtonType.YES) { svc.eliminarCliente(c.getIdentificacion()); cargarDatos(); } });
    }

    // ── helpers compartidos ────────────────────────────────────────────────
    private TableColumn<Cliente,String> sc(String n, String f, int w) {
        TableColumn<Cliente,String> c = new TableColumn<>(n);
        c.setCellValueFactory(new PropertyValueFactory<>(f)); c.setMinWidth(w); return c;
    }
    private <T> TableCell<Cliente,T> badge(java.util.function.Function<String,String> cssFn) {
        return new TableCell<>() {
            @Override protected void updateItem(T v, boolean e) {
                super.updateItem(v,e); setText(null); setGraphic(null);
                if (!e && v!=null) { Label l=new Label(v.toString()); l.getStyleClass().add(cssFn.apply(v.toString())); setGraphic(l); }
            }
        };
    }
    private TableColumn<Cliente,Void> accionesCol(java.util.function.Consumer<Integer> onEdit,
                                                   java.util.function.Consumer<Integer> onDel) {
        TableColumn<Cliente,Void> c = new TableColumn<>("Acciones");
        c.setMinWidth(95); c.setMaxWidth(95);
        c.setCellFactory(tc -> new TableCell<>() {
            final Button bE=new Button("✏"), bD=new Button("🗑");
            final HBox box=new HBox(4,bE,bD);
            { bE.getStyleClass().add("btn-icon"); bD.getStyleClass().add("btn-icon"); box.setAlignment(Pos.CENTER);
              bE.setOnAction(e->onEdit.accept(getIndex())); bD.setOnAction(e->onDel.accept(getIndex())); }
            @Override protected void updateItem(Void v,boolean e){ super.updateItem(v,e); setGraphic(e?null:box); }
        });
        return c;
    }
    private GridPane formGrid() {
        GridPane gp=new GridPane(); gp.setHgap(12); gp.setVgap(10); gp.setPadding(new Insets(20));
        gp.setStyle("-fx-background-color:white;");
        gp.getColumnConstraints().addAll(cc(125), cc2());
        return gp;
    }
    private void row(GridPane gp, int r, String lbl, javafx.scene.Node ctrl) {
        Label l=new Label(lbl); l.getStyleClass().add("field-label");
        gp.add(l,0,r); gp.add(ctrl,1,r);
        if (ctrl instanceof Region rg) { rg.setMaxWidth(Double.MAX_VALUE); GridPane.setFillWidth(ctrl,true); }
    }
    private VBox dlgLayout(GridPane gp, Button btnC, Button btnG) {
        ScrollPane sp=new ScrollPane(gp); sp.setFitToWidth(true); sp.setStyle("-fx-background:white; -fx-background-color:white;");
        HBox btns=new HBox(10,btnC,btnG); btns.setAlignment(Pos.CENTER_RIGHT);
        btns.setPadding(new Insets(10,20,16,20));
        btns.setStyle("-fx-background-color:white; -fx-border-color:#e8edf2 transparent transparent transparent; -fx-border-width:1 0 0 0;");
        VBox v=new VBox(sp,btns); v.setStyle("-fx-background-color:white;"); VBox.setVgrow(sp,Priority.ALWAYS); return v;
    }
    private TextField f(String v) { TextField t=new TextField(v); t.setMaxWidth(Double.MAX_VALUE); return t; }
    private ComboBox<String> cmb(String... items) { ComboBox<String> c=new ComboBox<>(FXCollections.observableArrayList(items)); c.setMaxWidth(Double.MAX_VALUE); return c; }
    private ColumnConstraints cc(int w) { ColumnConstraints c=new ColumnConstraints(w); return c; }
    private ColumnConstraints cc2() { ColumnConstraints c=new ColumnConstraints(); c.setHgrow(Priority.ALWAYS); c.setFillWidth(true); return c; }
    private double pd(String s) { try { return Double.parseDouble(s.replace(",","").replace(".","").trim()); } catch (Exception e) { return 0; } }
    private String safe(String s) { return s==null?"":s.toLowerCase(); }
    private void err(String m) { new Alert(Alert.AlertType.ERROR,m,ButtonType.OK).showAndWait(); }
}