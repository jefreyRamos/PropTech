package proptech.vista.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import proptech.modelo.Asesor;
import proptech.modelo.Visita;
import proptech.servicios.PropTechService;

public class VisitasController {

    private final PropTechService svc = PropTechService.getInstance();
    private final ObservableList<Visita> datos = FXCollections.observableArrayList();
    private TableView<Visita> tabla;
    private ComboBox<String> cmbFiltro;

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

    private HBox buildHeader() {
        HBox hb = new HBox(12);
        hb.getStyleClass().add("module-header");
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("📅  Gestión de Visitas");
        title.getStyleClass().add("module-title");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblF = new Label("Estado:");
        lblF.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b; -fx-font-weight:bold;");

        cmbFiltro = new ComboBox<>(FXCollections.observableArrayList(
            "Todos","PENDIENTE","CONFIRMADA","REALIZADA","CANCELADA","REPROGRAMADA"));
        cmbFiltro.setValue("Todos");
        cmbFiltro.setPrefWidth(150);
        cmbFiltro.setOnAction(e -> filtrar());

        Button btnNueva = new Button("＋  Agendar Visita");
        btnNueva.getStyleClass().add("btn-primary");
        btnNueva.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, sp, lblF, cmbFiltro, btnNueva);
        return hb;
    }

    @SuppressWarnings("unchecked")
    private TableView<Visita> buildTabla() {
        TableView<Visita> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setMaxWidth(Double.MAX_VALUE);
        t.setMaxHeight(Double.MAX_VALUE);
        t.setPlaceholder(new Label("Sin visitas registradas.  Haz clic en '＋ Agendar Visita'."));

        TableColumn<Visita,Number> cId = new TableColumn<>("#");
        cId.setCellValueFactory(new PropertyValueFactory<>("id"));
        cId.setMinWidth(45); cId.setMaxWidth(55);

        TableColumn<Visita,String> cCli   = sc("Cliente",       "nombreCliente",      180);
        TableColumn<Visita,String> cInm   = sc("Inmueble",      "direccionInmueble",  200);
        TableColumn<Visita,String> cFecha = sc("Fecha / Hora",  "fechaHora",          140);
        TableColumn<Visita,String> cAs    = sc("Asesor",        "nombreAsesor",       150);

        TableColumn<Visita,Object> cPrior = new TableColumn<>("Prioridad");
        cPrior.setCellValueFactory(new PropertyValueFactory<>("prioridad"));
        cPrior.setMinWidth(95);
        cPrior.setCellFactory(tc -> badge(v -> switch(v) {
            case "VIP"  -> "badge-purple";
            case "ALTA" -> "badge-orange";
            default     -> "badge-blue";
        }));

        TableColumn<Visita,Object> cEst = new TableColumn<>("Estado");
        cEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cEst.setMinWidth(115);
        cEst.setCellFactory(tc -> badge(v -> switch(v) {
            case "CONFIRMADA"   -> "badge-green";
            case "REALIZADA"    -> "badge-blue";
            case "CANCELADA"    -> "badge-red";
            case "REPROGRAMADA" -> "badge-orange";
            default             -> "badge-gray";
        }));

        TableColumn<Visita,String> cObs = sc("Observaciones","observaciones",160);

        TableColumn<Visita,Void> cAcc = new TableColumn<>("Acciones");
        cAcc.setMinWidth(140); cAcc.setMaxWidth(145);
        cAcc.setCellFactory(tc -> new TableCell<>() {
            final Button bE=new Button("✏"), bC=new Button("✔"), bX=new Button("✕");
            final HBox box=new HBox(3,bE,bC,bX);
            {
                bE.getStyleClass().add("btn-icon"); bC.getStyleClass().add("btn-icon"); bX.getStyleClass().add("btn-icon");
                bC.setTooltip(new Tooltip("Confirmar")); bX.setTooltip(new Tooltip("Cancelar"));
                box.setAlignment(Pos.CENTER);
                bE.setOnAction(e -> abrirFormulario(datos.get(getIndex())));
                bC.setOnAction(e -> cambiarEstado(datos.get(getIndex()), Visita.Estado.CONFIRMADA));
                bX.setOnAction(e -> cambiarEstado(datos.get(getIndex()), Visita.Estado.CANCELADA));
            }
            @Override protected void updateItem(Void v, boolean e){ super.updateItem(v,e); setGraphic(e?null:box); }
        });

        t.getColumns().addAll(cId,cCli,cInm,cFecha,cAs,cPrior,cEst,cObs,cAcc);
        t.setItems(datos);
        tabla = t;
        return t;
    }

    private HBox buildAccionesBar() {
        HBox bar = new HBox(10);
        bar.getStyleClass().add("acciones-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setMaxWidth(Double.MAX_VALUE);

        Label info = new Label("💡  VIP y ALTA se procesan primero (Cola de Prioridad)");
        info.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8; -fx-font-style:italic;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnProc = new Button("⚡  Procesar Siguiente Urgente");
        btnProc.getStyleClass().add("btn-secondary");
        btnProc.setOnAction(e -> {
            PropTechService.VisitaPriorizada vp = svc.procesarSiguiente();
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                vp==null ? "No hay visitas urgentes en la cola."
                         : "Procesando: " + vp, ButtonType.OK);
            a.setTitle("Cola de Prioridad"); a.showAndWait();
        });

        Button btnReload = new Button("↺");
        btnReload.getStyleClass().add("btn-icon");
        btnReload.setTooltip(new Tooltip("Recargar"));
        btnReload.setOnAction(e -> cargarDatos());

        bar.getChildren().addAll(info, sp, btnProc, btnReload);
        return bar;
    }

    private void cargarDatos() { datos.setAll(svc.getTodosVisitas()); }

    private void filtrar() {
        String f = cmbFiltro.getValue();
        if ("Todos".equals(f)) { cargarDatos(); return; }
        datos.setAll(svc.getTodosVisitas().stream()
            .filter(v -> v.getEstado().name().equals(f)).toList());
    }

    private void cambiarEstado(Visita v, Visita.Estado s) {
        v.setEstado(s); svc.actualizarVisita(v); cargarDatos();
    }

    private void abrirFormulario(Visita visita) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(visita==null ? "➕ Agendar Visita" : "✏ Editar Visita");
        win.setWidth(480); win.setHeight(430); win.setResizable(false);

        GridPane gp = formGrid();

        String now = LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TextField tCli   = f(visita!=null ? visita.getIdCliente()      : "");
        TextField tInm   = f(visita!=null ? visita.getCodigoInmueble() : "");
        TextField tFecha = f(visita!=null ? visita.getFechaHora()      : now);
        TextField tObs   = f(visita!=null ? visita.getObservaciones()  : "");

        List<Asesor> asesores = svc.getTodosAsesores();
        ComboBox<String> cAs = new ComboBox<>();
        cAs.setMaxWidth(Double.MAX_VALUE);
        asesores.forEach(a -> cAs.getItems().add(a.getCodigo() + " — " + a.getNombre()));
        if (visita!=null && visita.getIdAsesor()!=null)
            cAs.getItems().stream().filter(s->s.startsWith(visita.getIdAsesor())).findFirst().ifPresent(cAs::setValue);
        else if (!cAs.getItems().isEmpty()) cAs.setValue(cAs.getItems().get(0));

        ComboBox<String> cPrior = cmb("NORMAL","ALTA","VIP");
        cPrior.setValue(visita!=null ? visita.getPrioridad().name() : "NORMAL");

        ComboBox<String> cEst = cmb("PENDIENTE","CONFIRMADA","REALIZADA","CANCELADA","REPROGRAMADA");
        cEst.setValue(visita!=null ? visita.getEstado().name() : "PENDIENTE");

        int r=0;
        row(gp,r++,"ID Cliente *",            tCli);
        row(gp,r++,"Código Inmueble *",        tInm);
        row(gp,r++,"Fecha (yyyy-MM-dd HH:mm)", tFecha);
        row(gp,r++,"Asesor",                   cAs);
        row(gp,r++,"Prioridad",                cPrior);
        row(gp,r++,"Estado",                   cEst);
        row(gp,r++,"Observaciones",            tObs);

        Button btnG = new Button(visita==null ? "✔  Agendar" : "✔  Actualizar");
        btnG.getStyleClass().add("btn-primary");
        Button btnC = new Button("Cancelar"); btnC.getStyleClass().add("btn-icon");
        btnC.setOnAction(e -> win.close());

        btnG.setOnAction(e -> {
            try {
                if (tCli.getText().isBlank()||tInm.getText().isBlank()||tFecha.getText().isBlank())
                    throw new IllegalArgumentException("Cliente, inmueble y fecha son obligatorios.");
                String codAs = cAs.getValue()!=null ? cAs.getValue().split(" — ")[0].trim() : "";
                if (visita==null) {
                    Visita v = new Visita(tCli.getText().trim(), tInm.getText().trim(),
                        tFecha.getText().trim(), codAs, Visita.Prioridad.valueOf(cPrior.getValue()));
                    v.setObservaciones(tObs.getText().trim());
                    if (!svc.agendarVisita(v)) throw new IllegalStateException("No se pudo registrar la visita.");
                } else {
                    visita.setFechaHora(tFecha.getText().trim()); visita.setIdAsesor(codAs);
                    visita.setPrioridad(Visita.Prioridad.valueOf(cPrior.getValue()));
                    visita.setEstado(Visita.Estado.valueOf(cEst.getValue()));
                    visita.setObservaciones(tObs.getText().trim());
                    svc.actualizarVisita(visita);
                }
                cargarDatos(); win.close();
            } catch (Exception ex) { err(ex.getMessage()); }
        });

        win.setScene(new Scene(dlgLayout(gp, btnC, btnG)));
        win.show();
    }

    // helpers
    private TableColumn<Visita,String> sc(String n, String f, int w) {
        TableColumn<Visita,String> c = new TableColumn<>(n);
        c.setCellValueFactory(new PropertyValueFactory<>(f)); c.setMinWidth(w); return c;
    }
    private <T> TableCell<Visita,T> badge(java.util.function.Function<String,String> fn) {
        return new TableCell<>() {
            @Override protected void updateItem(T v, boolean e) {
                super.updateItem(v,e); setText(null); setGraphic(null);
                if (!e && v!=null) { Label l=new Label(v.toString()); l.getStyleClass().add(fn.apply(v.toString())); setGraphic(l); }
            }
        };
    }
    private GridPane formGrid() {
        GridPane gp=new GridPane(); gp.setHgap(12); gp.setVgap(10); gp.setPadding(new Insets(20));
        gp.setStyle("-fx-background-color:white;");
        gp.getColumnConstraints().addAll(new ColumnConstraints(145), cc2());
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
    private ColumnConstraints cc2() { ColumnConstraints c=new ColumnConstraints(); c.setHgrow(Priority.ALWAYS); c.setFillWidth(true); return c; }
    private void err(String m) { new Alert(Alert.AlertType.ERROR,m,ButtonType.OK).showAndWait(); }
}