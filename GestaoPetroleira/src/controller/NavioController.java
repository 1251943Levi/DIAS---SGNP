package controller;

import dao.TipoNavioDAO;
import model.EstadoOperacional;
import model.Navio;
import model.TipoNavio;
import service.NavioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;

import java.util.List;

public class NavioController {

    @FXML private TableView<Navio> tabelaNavios;
    @FXML private TableColumn<Navio, Integer> colId;
    @FXML private TableColumn<Navio, String> colNome;
    @FXML private TableColumn<Navio, String> colImo;
    @FXML private TableColumn<Navio, String> colTipo;
    @FXML private TableColumn<Navio, String> colEstado;

    @FXML private TextField txtNome;
    @FXML private TextField txtImo;
    @FXML private ComboBox<TipoNavio> cmbTipo;
    @FXML private ComboBox<EstadoOperacional> cmbEstado;

    private final NavioService navioService = new NavioService();
    private final TipoNavioDAO tipoNavioDAO = new TipoNavioDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colImo.setCellValueFactory(new PropertyValueFactory<>("codigoImo"));
        colTipo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTipoNavio() != null
                        ? data.getValue().getTipoNavio().getNome() : ""));
        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstadoOperacional().name()));

        cmbTipo.setItems(FXCollections.observableArrayList(tipoNavioDAO.listarTodos()));
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoOperacional.values()));

        tabelaNavios.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, selecionado) -> preencherFormulario(selecionado));

        carregarNavios();
    }

    private void carregarNavios() {
        tabelaNavios.setItems(FXCollections.observableArrayList(navioService.listarNavios()));
    }

    private void preencherFormulario(Navio navio) {
        if (navio == null) return;
        txtNome.setText(navio.getNome());
        txtImo.setText(navio.getCodigoImo());
        cmbTipo.setValue(navio.getTipoNavio());
        cmbEstado.setValue(navio.getEstadoOperacional());
    }

    @FXML
    private void onAdicionar() {
        try {
            Navio navio = new Navio();
            navio.setNome(txtNome.getText());
            navio.setCodigoImo(txtImo.getText());
            navio.setTipoNavio(cmbTipo.getValue());
            navioService.adicionarNavio(navio);
            limparFormulario();
            carregarNavios();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onEditar() {
        Navio s = tabelaNavios.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarErro("Selecione um navio para editar."); return; }
        try {
            s.setNome(txtNome.getText());
            s.setCodigoImo(txtImo.getText());
            s.setTipoNavio(cmbTipo.getValue());
            s.setEstadoOperacional(cmbEstado.getValue());
            navioService.atualizarNavio(s);
            limparFormulario();
            carregarNavios();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Navio s = tabelaNavios.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarErro("Selecione um navio para eliminar."); return; }
        navioService.eliminarNavio(s.getId());
        limparFormulario();
        carregarNavios();
    }

    @FXML
    private void onManutencoes() {
        Navio s = tabelaNavios.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarErro("Selecione um navio para ver as manutenções."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/manutencoes-view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Manutenções — " + s.getNome());
            ManutencaoController controller = loader.getController();
            controller.setNavio(s);
            stage.show();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    private void limparFormulario() {
        txtNome.clear();
        txtImo.clear();
        cmbTipo.setValue(null);
        cmbEstado.setValue(null);
        tabelaNavios.getSelectionModel().clearSelection();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}