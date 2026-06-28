package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.EntregaHistorico;
import service.CargaService;

/**
 * Aba "Histórico" — só de leitura. Lista as cargas entregues em viagens concluídas,
 * com o percurso (porto de carga -> porto de descarga) e as datas.
 */
public class HistoricoController implements Atualizavel {

    @FXML private TableView<EntregaHistorico>            tabela;
    @FXML private TableColumn<EntregaHistorico, Integer> colViagem;
    @FXML private TableColumn<EntregaHistorico, String>  colNavio;
    @FXML private TableColumn<EntregaHistorico, String>  colCarga;
    @FXML private TableColumn<EntregaHistorico, String>  colTipo;
    @FXML private TableColumn<EntregaHistorico, Double>  colPeso;
    @FXML private TableColumn<EntregaHistorico, String>  colTanque;
    @FXML private TableColumn<EntregaHistorico, String>  colPortoCarga;
    @FXML private TableColumn<EntregaHistorico, String>  colPortoDescarga;
    @FXML private TableColumn<EntregaHistorico, String>  colPartida;
    @FXML private TableColumn<EntregaHistorico, String>  colChegada;

    @FXML private TextField txtPesquisa;
    @FXML private Label      lblTotal;

    private final ObservableList<EntregaHistorico> dados = FXCollections.observableArrayList();
    private final CargaService cargaService = new CargaService();

    @FXML
    public void initialize() {
        colViagem.setCellValueFactory(new PropertyValueFactory<>("idViagem"));
        colNavio.setCellValueFactory(new PropertyValueFactory<>("navio"));
        colCarga.setCellValueFactory(new PropertyValueFactory<>("carga"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colTanque.setCellValueFactory(new PropertyValueFactory<>("tanque"));
        colPortoCarga.setCellValueFactory(new PropertyValueFactory<>("portoCarga"));
        colPortoDescarga.setCellValueFactory(new PropertyValueFactory<>("portoDescarga"));
        colPartida.setCellValueFactory(new PropertyValueFactory<>("partida"));
        colChegada.setCellValueFactory(new PropertyValueFactory<>("chegada"));

        FilteredList<EntregaHistorico> filtrados = new FilteredList<>(dados, e -> true);
        txtPesquisa.textProperty().addListener((obs, anterior, texto) -> {
            filtrados.setPredicate(corresponde(texto));
            atualizarTotal(filtrados.size());
        });
        tabela.setItems(filtrados);

        carregar();
    }

    private java.util.function.Predicate<EntregaHistorico> corresponde(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        return e -> {
            if (q.isEmpty()) return true;
            return e.getNavio().toLowerCase().contains(q)
                    || e.getCarga().toLowerCase().contains(q)
                    || e.getTipo().toLowerCase().contains(q)
                    || e.getPercurso().toLowerCase().contains(q)
                    || String.valueOf(e.getIdViagem()).contains(q);
        };
    }

    private void carregar() {
        try {
            dados.setAll(cargaService.listarHistoricoEntregas());
            atualizarTotal(dados.size());
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erro"); a.setHeaderText(null);
            a.setContentText("Não foi possível carregar o histórico: " + e.getMessage());
            a.showAndWait();
        }
    }

    private void atualizarTotal(int n) {
        if (lblTotal != null) lblTotal.setText(n + " entrega(s)");
    }

    /** Chamado pelo Main ao abrir a aba: recarrega o histórico. */
    @Override
    public void atualizar() {
        carregar();
    }
}
