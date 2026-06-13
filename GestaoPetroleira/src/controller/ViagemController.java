package controller;

import model.Navio;
import model.Porto;
import model.Viagem;
import service.NavioService;
import service.ViagemService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

public class ViagemController {

    @FXML private TableView<Viagem> tabelaViagens;
    @FXML private TableColumn<Viagem, Integer> colId;
    @FXML private TableColumn<Viagem, String> colNavio;
    @FXML private TableColumn<Viagem, String> colOrigem;
    @FXML private TableColumn<Viagem, String> colDestino;
    @FXML private TableColumn<Viagem, String> colPartida;
    @FXML private TableColumn<Viagem, String> colEstado;

    @FXML private ComboBox<Navio> cmbNavio;
    @FXML private ComboBox<Porto> cmbOrigem;
    @FXML private ComboBox<Porto> cmbDestino;
    @FXML private DatePicker dtPartida;

    private final ViagemService viagemService = new ViagemService();
    private final NavioService navioService = new NavioService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNavio.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNavio().getNome()));
        colOrigem.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPortoOrigem().getNome()));
        colDestino.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPortoDestino().getNome()));
        colPartida.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataPartida().toString()));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstado().name()));

        cmbNavio.setItems(FXCollections.observableArrayList(navioService.listarNavios()));
        cmbOrigem.setItems(FXCollections.observableArrayList(viagemService.listarPortos()));
        cmbDestino.setItems(FXCollections.observableArrayList(viagemService.listarPortos()));

        carregarViagens();
    }

    private void carregarViagens() {
        tabelaViagens.setItems(FXCollections.observableArrayList(viagemService.listarViagens()));
    }

    @FXML
    private void onCriar() {
        try {
            viagemService.criarViagem(cmbNavio.getValue(), cmbOrigem.getValue(),
                    cmbDestino.getValue(), dtPartida.getValue());
            limparFormulario();
            carregarViagens();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML private void onIniciar()  { mudarEstado("iniciar"); }
    @FXML private void onConcluir() { mudarEstado("concluir"); }
    @FXML private void onCancelar() { mudarEstado("cancelar"); }

    private void mudarEstado(String acao) {
        Viagem v = tabelaViagens.getSelectionModel().getSelectedItem();
        if (v == null) { mostrarErro("Selecione uma viagem."); return; }
        try {
            switch (acao) {
                case "iniciar"  -> viagemService.iniciarViagem(v);
                case "concluir" -> viagemService.concluirViagem(v);
                case "cancelar" -> viagemService.cancelarViagem(v);
            }
            carregarViagens();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    // --- Associacoes que dependem de outros slices (Cargas / Tripulacao) ---
    @FXML
    private void onAssociarCargas() {
        Viagem v = tabelaViagens.getSelectionModel().getSelectedItem();
        if (v == null) { mostrarErro("Selecione uma viagem."); return; }
        // TODO: abrir sub-ecra do slice Cargas e gravar em dias.VIAGEM_CARGA
        mostrarInfo("Associacao de cargas: depende do slice Cargas (tabela VIAGEM_CARGA).");
    }

    @FXML
    private void onAssociarTripulacao() {
        Viagem v = tabelaViagens.getSelectionModel().getSelectedItem();
        if (v == null) { mostrarErro("Selecione uma viagem."); return; }
        // TODO: abrir sub-ecra do slice Tripulacao e gravar em dias.VIAGEM_TRIPULACAO
        mostrarInfo("Associacao de tripulacao: depende do slice Tripulacao (tabela VIAGEM_TRIPULACAO).");
    }

    private void limparFormulario() {
        cmbNavio.setValue(null);
        cmbOrigem.setValue(null);
        cmbDestino.setValue(null);
        dtPartida.setValue(null);
        tabelaViagens.getSelectionModel().clearSelection();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacao");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
