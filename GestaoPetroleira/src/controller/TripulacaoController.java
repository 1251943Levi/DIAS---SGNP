package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import service.TripulacaoService;

import java.util.List;

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
    @FXML private TextField            txtPesquisa;

    private final ObservableList<Tripulante> dadosTripulantes = FXCollections.observableArrayList();

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

        // Pesquisa/filtro sobre a tabela de tripulantes
        FilteredList<Tripulante> filtrados = new FilteredList<>(dadosTripulantes, t -> true);
        txtPesquisa.textProperty().addListener((obs, anterior, texto) ->
                filtrados.setPredicate(tripulanteCorresponde(texto)));
        tabelaTripulantes.setItems(filtrados);

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
        try {
            tripulacaoService.eliminarTripulante(sel.getId());
            carregarTripulantes();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
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
        dadosTripulantes.setAll(tripulacaoService.listarTripulantes());
    }

    /** Pesquisa, sem distinguir maiúsculas, em nome, matrícula, função e disponibilidade. */
    private java.util.function.Predicate<Tripulante> tripulanteCorresponde(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        return t -> {
            if (q.isEmpty()) return true;
            String funcao = t.getFuncao() != null ? t.getFuncao().name() : "";
            String disp = t.isDisponivel() ? "sim disponível" : "não indisponível";
            return t.getNome().toLowerCase().contains(q)
                    || t.getNumeroMatricula().toLowerCase().contains(q)
                    || funcao.toLowerCase().contains(q)
                    || disp.contains(q);
        };
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

    @FXML
    private void onHistorico() {
        Tripulante sel = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um tripulante."); return; }

        List<TripulacaoViagem> historico;
        try {
            historico = tripulacaoService.historicoDoTripulante(sel.getId());
        } catch (Exception e) { mostrarErro(e.getMessage()); return; }

        StringBuilder sb = new StringBuilder();
        if (historico.isEmpty()) {
            sb.append("Este tripulante ainda não participou em nenhuma viagem.");
        } else {
            for (TripulacaoViagem tv : historico) {
                sb.append(tv.getViagem()).append("  —  Função: ").append(tv.getFuncao()).append("\n");
            }
        }

        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(560, 240);

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Histórico de viagens");
        a.setHeaderText("Tripulante: " + sel.getNome() + " (" + sel.getNumeroMatricula() + ")");
        a.getDialogPane().setContent(area);
        a.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setContentText(msg); a.showAndWait();
    }
}
