package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Manutencao;
import model.Navio;
import service.NavioService;

import java.util.List;

public class ManutencaoController {

    @FXML private TableView<Manutencao>            tabelaManutencoes;
    @FXML private TableColumn<Manutencao, Integer> colId;
    @FXML private TableColumn<Manutencao, String>  colNavio;
    @FXML private TableColumn<Manutencao, String>  colInicio;
    @FXML private TableColumn<Manutencao, String>  colFim;
    @FXML private TableColumn<Manutencao, String>  colDescricao;

    @FXML private ComboBox<Navio> cmbNavio;
    @FXML private TextField       txtDescricao;

    private final NavioService navioService = new NavioService();
    private Navio navioAtual;

    public void setNavio(Navio navio) {
        this.navioAtual = navio;
        if (cmbNavio != null) cmbNavio.setValue(navio);
        carregarManutencoes();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNavio.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNavio().getNome()));
        colInicio.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataInicio().toString()));
        colFim.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataFim() != null ? d.getValue().getDataFim().toString() : "Em curso"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        if (cmbNavio != null)
            cmbNavio.setItems(FXCollections.observableArrayList(navioService.listarNavios()));
    }

    @FXML
    private void onRegistar() {
        Navio navio = navioAtual != null ? navioAtual :
                (cmbNavio != null ? cmbNavio.getValue() : null);
        if (navio == null) { mostrarErro("Selecione um navio."); return; }
        String desc = txtDescricao.getText().trim();
        if (desc.isEmpty()) { mostrarErro("Insira uma descrição."); return; }
        try {
            navioService.registarManutencao(navio, desc);
            txtDescricao.clear();
            carregarManutencoes();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onConcluir() {
        Manutencao sel = tabelaManutencoes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione uma manutenção."); return; }
        try {
            navioService.concluirManutencao(sel);
            carregarManutencoes();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onEliminar() {
        Manutencao sel = tabelaManutencoes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione uma manutenção."); return; }
        try {
            navioService.eliminarManutencao(sel.getId());
            carregarManutencoes();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    private void carregarManutencoes() {
        List<Manutencao> lista;
        if (navioAtual != null) {
            lista = navioService.listarManutencoes(navioAtual.getId());
        } else {
            // se não há navio fixo, mostra manutenções de todos
            lista = new java.util.ArrayList<>();
            for (Navio n : navioService.listarNavios())
                lista.addAll(navioService.listarManutencoes(n.getId()));
        }
        tabelaManutencoes.setItems(FXCollections.observableArrayList(lista));
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setContentText(msg); a.showAndWait();
    }
}
