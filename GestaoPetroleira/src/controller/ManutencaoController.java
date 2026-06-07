package controller;

import model.Manutencao;
import model.Navio;
import service.NavioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class ManutencaoController {

    @FXML private TableView<Manutencao> tabelaManutencoes;
    @FXML private TableColumn<Manutencao, Integer> colId;
    @FXML private TableColumn<Manutencao, String> colNavio;
    @FXML private TableColumn<Manutencao, String> colInicio;
    @FXML private TableColumn<Manutencao, String> colFim;
    @FXML private TableColumn<Manutencao, String> colDescricao;

    @FXML private TextField txtDescricao;
    @FXML private Label lblNavio;

    private final NavioService navioService = new NavioService();
    private Navio navioAtual;

    public void setNavio(Navio navio) {
        this.navioAtual = navio;
        lblNavio.setText("Navio: " + navio.getNome());
        carregarManutencoes();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNavio.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNavio().getNome()));
        colInicio.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDataInicio().toString()));
        colFim.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDataFim() != null
                        ? data.getValue().getDataFim().toString() : "Em curso"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
    }

    private void carregarManutencoes() {
        if (navioAtual == null) return;
        tabelaManutencoes.setItems(FXCollections.observableArrayList(
                navioService.listarManutencoes(navioAtual.getId())));
    }

    @FXML
    private void onRegistar() {
        if (navioAtual == null) return;
        try {
            navioService.registarManutencao(navioAtual, txtDescricao.getText());
            txtDescricao.clear();
            carregarManutencoes();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onConcluir() {
        Manutencao s = tabelaManutencoes.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarErro("Selecione uma manutenção para concluir."); return; }
        if (!s.emCurso()) { mostrarErro("Esta manutenção já foi concluída."); return; }
        navioService.concluirManutencao(s);
        carregarManutencoes();
    }

    @FXML
    private void onEliminar() {
        Manutencao s = tabelaManutencoes.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarErro("Selecione uma manutenção para eliminar."); return; }
        carregarManutencoes();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}