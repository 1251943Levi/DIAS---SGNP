package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import service.TripulacaoService;

public class TripulacaoController {

    @FXML private TableView<Tripulante> tabelaTripulantes;
    @FXML private TableColumn<Tripulante, Integer> colId;
    @FXML private TableColumn<Tripulante, String>  colNome;
    @FXML private TableColumn<Tripulante, String>  colMatricula;
    @FXML private TableColumn<Tripulante, String>  colFuncao;
    @FXML private TableColumn<Tripulante, String>  colDisponivel;

    @FXML private TextField            txtNome;
    @FXML private TextField            txtMatricula;
    @FXML private ComboBox<Funcao>     cmbFuncao;
    @FXML private CheckBox             chkDisponivel;

    private final TripulacaoService tripulacaoService = new TripulacaoService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("numeroMatricula"));
        colFuncao.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFuncao() != null ? d.getValue().getFuncao().name() : "—"));
        colDisponivel.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isDisponivel() ? "Sim" : "Não"));

        cmbFuncao.setItems(FXCollections.observableArrayList(Funcao.values()));
        chkDisponivel.setSelected(true);

        carregarTripulantes();

        // Matrícula gerada automaticamente: MAT + próximo número
        txtMatricula.setEditable(false);
        txtMatricula.setText(proximaMatricula());
    }

    @FXML
    private void onAdicionar() {
        try {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) throw new Exception("Insira o nome do tripulante.");
            if (cmbFuncao.getValue() == null) throw new Exception("Selecione a função.");

            String mat = proximaMatricula();   // MAT + próximo número, automático
            Tripulante t = new Tripulante(0, nome, mat, cmbFuncao.getValue(), chkDisponivel.isSelected());
            tripulacaoService.adicionarTripulante(t);
            limparFormulario();
            carregarTripulantes();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onAtualizar() {
        Tripulante sel = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um tripulante."); return; }
        try {
            String nome = txtNome.getText().trim();
            String mat  = txtMatricula.getText().trim();
            if (nome.isEmpty()) throw new Exception("Insira o nome.");
            if (mat.isEmpty())  throw new Exception("Insira a matrícula.");
            if (cmbFuncao.getValue() == null) throw new Exception("Selecione a função.");

            sel.setNome(nome); sel.setNumeroMatricula(mat);
            sel.setFuncao(cmbFuncao.getValue()); sel.setDisponivel(chkDisponivel.isSelected());
            tripulacaoService.atualizarTripulante(sel);
            limparFormulario();
            carregarTripulantes();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Tripulante sel = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um tripulante."); return; }
        tripulacaoService.eliminarTripulante(sel.getId());
        carregarTripulantes();
    }

    @FXML
    private void onSelecionar() {
        Tripulante sel = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        txtNome.setText(sel.getNome());
        txtMatricula.setText(sel.getNumeroMatricula());
        cmbFuncao.setValue(sel.getFuncao());
        chkDisponivel.setSelected(sel.isDisponivel());
    }

    private void carregarTripulantes() {
        tabelaTripulantes.setItems(
                FXCollections.observableArrayList(tripulacaoService.listarTripulantes()));
    }

    private void limparFormulario() {
        txtNome.clear(); txtMatricula.setText(proximaMatricula());
        cmbFuncao.setValue(null); chkDisponivel.setSelected(true);
        tabelaTripulantes.getSelectionModel().clearSelection();
    }

    /** Gera a próxima matrícula no formato MAT + (maior número existente + 1). */
    private String proximaMatricula() {
        int max = 1000;
        for (Tripulante t : tripulacaoService.listarTripulantes()) {
            String m = t.getNumeroMatricula();
            if (m != null && m.matches("MAT\\d+")) {
                int n = Integer.parseInt(m.substring(3));
                if (n > max) max = n;
            }
        }
        return "MAT" + (max + 1);
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setContentText(msg); a.showAndWait();
    }
}
