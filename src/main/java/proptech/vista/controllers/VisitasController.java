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
import proptech.modelo.Visita;
import proptech.servicios.PropTechService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VisitasController {

    private final PropTechService svc = PropTechService.getInstance();
    private final ObservableList<Visita> datos = FXCollections.observableArrayList();
    private TableView<Visita> tabla;
    private ComboBox<String> cmbFiltroEstado;

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
        HBox hb = new HBox(12); hb.setAlignment(Pos.CENTER_LEFT);
        hb.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:16 20;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        Label title = new Label("📅 Gestión de Visitas");
        title.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#1a202c;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblF = new Label("Filtrar:");
        lblF.setStyle("-fx-font-size:12px;-fx-text-fill:#718096;-fx-font-weight:bold;");

        cmbFiltroEstado = new ComboBox<>(FXCollections.observableArrayList(
            "Todos","PENDIENTE","CONFIRMADA","REALIZADA","CANCELADA","REPROGRAMADA"));
        cmbFiltroEstado.setValue("Todos");
        cmbFiltroEstado.setOnAction(e -> filtrarPorEstado());

        Button btnNueva = new Button("＋  Agendar Visita");
        btnNueva.getStyleClass().add("btn-primary");
        btnNueva.setOnAction(e -> abrirFormulario(null));

        hb.getChildren().addAll(title, sp, lblF, cmbFiltroEstado, btnNueva);
        return hb;
    }

    @SuppressWarnings("unchecked")
    private TableView<Visita> buildTabla() {
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(Double.MAX_VALUE);
        tabla.setPlaceholder(new Label("Sin visitas registradas."));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Visita,Number> cId    = new TableColumn<>("#");
        cId.setCellValueFactory(new PropertyValueFactory<>("id")); cId.setMinWidth(45); cId.setMaxWidth(55);

        TableColumn<Visita,String> cCli   = strCol("Cliente",          "nombreCliente",     160);
        TableColumn<Visita,String> cInm   = strCol("Inmueble",         "direccionInmueble", 180);
        TableColumn<Visita,String> cFecha = strCol("Fecha y Hora",     "fechaHora",         130);
        TableColumn<Visita,String> cAs    = strCol("Asesor",           "nombreAsesor",      130);

        TableColumn<Visita,Object> cPrior = new TableColumn<>("Prioridad");
        cPrior.setCellValueFactory(new PropertyValueFactory<>("prioridad"));
        cPrior.setMinWidth(90);
        cPrior.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Object v, boolean empty) {
                super.updateItem(v,empty); setText(null); setGraphic(null);
                if (!empty && v != null) {
                    Label l = new Label(v.toString());
                    l.getStyleClass().add(switch(v.toString()) {
                        case "VIP"   -> "badge-purple";
                        case "ALTA"  -> "badge-orange";
                        default      -> "badge-blue";
                    });
                    setGraphic(l);
                }
            }
        });

        TableColumn<Visita,Object> cEst = new TableColumn<>("Estado");
        cEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cEst.setMinWidth(110);
        cEst.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Object v, boolean empty) {
                super.updateItem(v,empty); setText(null); setGraphic(null);
                if (!empty && v != null) {
                    Label l = new Label(v.toString());
                    l.getStyleClass().add(switch(v.toString()) {
                        case "CONFIRMADA"   -> "badge-green";
                        case "REALIZADA"    -> "badge-blue";
                        case "CANCELADA"    -> "badge-red";
                        case "REPROGRAMADA" -> "badge-orange";
                        default             -> "badge-orange";
                    });
                    setGraphic(l);
                }
            }
        });

        TableColumn<Visita,String> cObs = strCol("Observaciones","observaciones",150);

        TableColumn<Visita,Void> cAcc = new TableColumn<>("Acciones");
        cAcc.setMinWidth(130); cAcc.setMaxWidth(140);
        cAcc.setCellFactory(tc -> new TableCell<>() {
            final Button bEdit   = new Button("✏");
            final Button bConf   = new Button("✔");
            final Button bCancel = new Button("✕");
            final HBox box = new HBox(3, bEdit, bConf, bCancel);
            {
                bEdit.getStyleClass().add("btn-icon");
                bConf.getStyleClass().add("btn-icon");
                bCancel.getStyleClass().add("btn-icon");
                box.setAlignment(Pos.CENTER);
                bEdit.setOnAction(e -> abrirFormulario(getTableView().getItems().get(getIndex())));
                bConf.setOnAction(e -> cambiarEstado(getTableView().getItems().get(getIndex()), Visita.Estado.CONFIRMADA));
                bCancel.setOnAction(e -> cambiarEstado(getTableView().getItems().get(getIndex()), Visita.Estado.CANCELADA));
            }
            @Override protected void updateItem(Void v, boolean empty){ super.updateItem(v,empty); setGraphic(empty?null:box); }
        });

        tabla.getColumns().addAll(cId,cCli,cInm,cFecha,cAs,cPrior,cEst,cObs,cAcc);
        tabla.setItems(datos);
        return tabla;
    }

    private HBox buildPie() {
        HBox hb = new HBox(10); hb.setAlignment(Pos.CENTER_RIGHT);

        Label info = new Label("💡 Las visitas VIP se procesan primero (Cola de Prioridad)");
        info.setStyle("-fx-font-size:11px;-fx-text-fill:#718096;-fx-font-style:italic;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnProcesar = new Button("⚡ Procesar Siguiente Urgente (Cola Prioridad)");
        btnProcesar.getStyleClass().add("btn-secondary");
        btnProcesar.setOnAction(e -> {
            PropTechService.VisitaPriorizada vp = svc.procesarSiguiente();
            if (vp == null) { mostrarInfo("No hay visitas urgentes en cola."); return; }
            mostrarInfo("Procesando: " + vp.toString());
        });

        hb.getChildren().addAll(info, sp, btnProcesar);
        return hb;
    }

    private void cargarDatos() { datos.setAll(svc.getTodosVisitas()); }

    private void filtrarPorEstado() {
        String f = cmbFiltroEstado.getValue();
        if ("Todos".equals(f)) { cargarDatos(); return; }
        datos.setAll(svc.getTodosVisitas().stream()
            .filter(v -> v.getEstado().name().equals(f)).toList());
    }

    private void cambiarEstado(Visita v, Visita.Estado nuevoEstado) {
        v.setEstado(nuevoEstado);
        svc.actualizarVisita(v);
        cargarDatos();
    }

    private void abrirFormulario(Visita visita) {
        Stage win = new Stage();
        win.initModality(Modality.APPLICATION_MODAL);
        win.setTitle(visita == null ? "Agendar Visita" : "Editar Visita");
        win.setWidth(480); win.setHeight(420);

        GridPane gp = new GridPane();
        gp.setHgap(12); gp.setVgap(10); gp.setPadding(new Insets(20));
        gp.setStyle("-fx-background-color:white;");

        String now = LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TextField tCli   = field(visita != null ? visita.getIdCliente()      : "");
        TextField tInm   = field(visita != null ? visita.getCodigoInmueble() : "");
        TextField tFecha = field(visita != null ? visita.getFechaHora()      : now);
        TextField tObs   = field(visita != null ? visita.getObservaciones()  : "");

        List<Asesor> asesores = svc.getTodosAsesores();
        ComboBox<String> cAs = new ComboBox<>();
        asesores.forEach(a -> cAs.getItems().add(a.getCodigo() + " — " + a.getNombre()));
        if (visita != null && visita.getIdAsesor() != null)
            cAs.getItems().stream().filter(s -> s.startsWith(visita.getIdAsesor()))
               .findFirst().ifPresent(cAs::setValue);
        else if (!cAs.getItems().isEmpty()) cAs.setValue(cAs.getItems().get(0));

        ComboBox<String> cPrior = new ComboBox<>(FXCollections.observableArrayList("NORMAL","ALTA","VIP"));
        cPrior.setValue(visita != null ? visita.getPrioridad().name() : "NORMAL");

        ComboBox<String> cEst = new ComboBox<>(FXCollections.observableArrayList(
            "PENDIENTE","CONFIRMADA","REALIZADA","CANCELADA","REPROGRAMADA"));
        cEst.setValue(visita != null ? visita.getEstado().name() : "PENDIENTE");

        int r = 0;
        gp.add(lbl("ID Cliente *"),0,r);          gp.add(tCli,1,r++);
        gp.add(lbl("Código Inmueble *"),0,r);     gp.add(tInm,1,r++);
        gp.add(lbl("Fecha (yyyy-MM-dd HH:mm)"),0,r); gp.add(tFecha,1,r++);
        gp.add(lbl("Asesor"),0,r);                gp.add(cAs,1,r++);
        gp.add(lbl("Prioridad"),0,r);             gp.add(cPrior,1,r++);
        gp.add(lbl("Estado"),0,r);                gp.add(cEst,1,r++);
        gp.add(lbl("Observaciones"),0,r);         gp.add(tObs,1,r++);

        gp.getChildren().forEach(n -> {
            if (n instanceof ComboBox<?> cb) cb.setPrefWidth(250);
            if (n instanceof TextField tf)  tf.setPrefWidth(250);
        });

        Button btnG = new Button(visita == null ? "Agendar" : "Actualizar");
        btnG.getStyleClass().add("btn-primary");
        Button btnC = new Button("Cancelar"); btnC.getStyleClass().add("btn-icon");
        btnC.setOnAction(e -> win.close());

        btnG.setOnAction(e -> {
            try {
                if (tCli.getText().isBlank() || tInm.getText().isBlank() || tFecha.getText().isBlank())
                    throw new IllegalArgumentException("Cliente, inmueble y fecha son obligatorios.");
                String codAs = cAs.getValue() != null ? cAs.getValue().split(" — ")[0] : null;

                if (visita == null) {
                    Visita v = new Visita(
                        tCli.getText().trim(), tInm.getText().trim(),
                        tFecha.getText().trim(), codAs,
                        Visita.Prioridad.valueOf(cPrior.getValue()));
                    v.setObservaciones(tObs.getText().trim());
                    boolean ok = svc.agendarVisita(v);
                    if (!ok) throw new IllegalStateException("No se pudo registrar la visita.");
                } else {
                    visita.setFechaHora(tFecha.getText().trim());
                    visita.setIdAsesor(codAs);
                    visita.setPrioridad(Visita.Prioridad.valueOf(cPrior.getValue()));
                    visita.setEstado(Visita.Estado.valueOf(cEst.getValue()));
                    visita.setObservaciones(tObs.getText().trim());
                    svc.actualizarVisita(visita);
                }
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

    private TableColumn<Visita,String> strCol(String name, String field, int w) {
        TableColumn<Visita,String> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(field)); c.setMinWidth(w); return c;
    }
    private TextField field(String v) { TextField t = new TextField(v); t.getStyleClass().add("text-field"); return t; }
    private Label     lbl(String txt) { Label l = new Label(txt); l.getStyleClass().add("field-label"); l.setMinWidth(130); return l; }
    private void mostrarError(String m){ Alert a=new Alert(Alert.AlertType.ERROR,m); a.setTitle("Error"); a.showAndWait(); }
    private void mostrarInfo(String m) { Alert a=new Alert(Alert.AlertType.INFORMATION,m); a.setTitle("Info"); a.showAndWait(); }
}