package controller;

import dao.PortoDAO;
import dao.TipoNavioDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.*;
import service.NavioService;

import java.util.List;

public class NavioController {

    @FXML private TableView<Navio>             tabelaNavios;
    @FXML private TableColumn<Navio, Integer>  colId;
    @FXML private TableColumn<Navio, String>   colNome;
    @FXML private TableColumn<Navio, String>   colImo;
    @FXML private TableColumn<Navio, String>   colTipo;
    @FXML private TableColumn<Navio, Double>   colCapacidade;
    @FXML private TableColumn<Navio, Integer>  colCompartimentos;
    @FXML private TableColumn<Navio, String>   colBandeira;
    @FXML private TableColumn<Navio, Integer>  colAno;
    @FXML private TableColumn<Navio, String>   colEstado;

    @FXML private TextField          txtNome;
    @FXML private TextField          txtImo;
    @FXML private ComboBox<TipoNavio> cmbTipo;
    @FXML private TextField          txtCapacidade;
    @FXML private TextField          txtCompartimentos;
    @FXML private TextField          txtBandeira;
    @FXML private TextField          txtAno;

    private final NavioService  navioService  = new NavioService();
    private final TipoNavioDAO  tipoNavioDAO  = new TipoNavioDAO();
    private final PortoDAO      portoDAO      = new PortoDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colImo.setCellValueFactory(new PropertyValueFactory<>("codigoImo"));
        colTipo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTipoNavio() != null ? d.getValue().getTipoNavio().getNome() : "—"));
        colCapacidade.setCellValueFactory(new PropertyValueFactory<>("capacidadeMaxima"));
        colCompartimentos.setCellValueFactory(new PropertyValueFactory<>("numCompartimentos"));
        colBandeira.setCellValueFactory(new PropertyValueFactory<>("bandeira"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("anoFabrico"));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstadoOperacional().name()));

        cmbTipo.setItems(FXCollections.observableArrayList(tipoNavioDAO.listarTodos()));
        carregarNavios();

        // Código IMO: prefixo "IMO" fixo + 7 dígitos digitados à mão
        txtImo.setTextFormatter(new javafx.scene.control.TextFormatter<>(change ->
                change.getControlNewText().matches("IMO\\d{0,7}") ? change : null));
        txtImo.setText("IMO");
    }

    @FXML
    private void onAdicionar() {
        try {
            Navio n = navioDoFormulario(0);
            navioService.adicionarNavio(n);
            limparFormulario();
            carregarNavios();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onAtualizar() {
        Navio sel = tabelaNavios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um navio."); return; }
        try {
            Navio n = navioDoFormulario(sel.getId());
            n.setEstadoOperacional(sel.getEstadoOperacional());
            navioService.atualizarNavio(n);
            limparFormulario();
            carregarNavios();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onEliminar() {
        Navio sel = tabelaNavios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um navio."); return; }
        navioService.eliminarNavio(sel.getId());
        carregarNavios();
    }

    @FXML
    private void onSelecionar() {
        Navio sel = tabelaNavios.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        txtNome.setText(sel.getNome());
        txtImo.setText(sel.getCodigoImo());
        cmbTipo.setValue(sel.getTipoNavio());
        txtCapacidade.setText(String.valueOf(sel.getCapacidadeMaxima()));
        txtCompartimentos.setText(String.valueOf(sel.getNumCompartimentos()));
        txtBandeira.setText(sel.getBandeira());
        txtAno.setText(String.valueOf(sel.getAnoFabrico()));
    }

    @FXML
    private void onManutencao() {
        Navio sel = tabelaNavios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um navio."); return; }
        try {
            // CORRIGIDO: caminho correto do FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/view/ManutencoesView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Manutenções — " + sel.getNome());
            ManutencaoController ctrl = loader.getController();
            ctrl.setNavio(sel);
            stage.show();
        } catch (Exception e) { mostrarErro("Erro ao abrir manutenções: " + e.getMessage()); }
    }

    private Navio navioDoFormulario(int id) throws Exception {
        String nome = txtNome.getText().trim();
        String imo  = txtImo.getText().trim();
        if (nome.isEmpty()) throw new Exception("Insira o nome do navio.");
        if (!imo.matches("IMO\\d{7}")) throw new Exception("O código IMO deve ser \"IMO\" seguido de 7 dígitos (ex.: IMO1234567).");
        if (cmbTipo.getValue() == null) throw new Exception("Selecione o tipo de navio.");
        double cap;
        try { cap = Double.parseDouble(txtCapacidade.getText().trim()); }
        catch (NumberFormatException ex) { throw new Exception("A capacidade máxima deve ser um número válido (ex.: 75000)."); }
        int comp;
        try { comp = Integer.parseInt(txtCompartimentos.getText().trim()); }
        catch (NumberFormatException ex) { throw new Exception("O número de compartimentos deve ser um número inteiro válido (ex.: 4)."); }
        String band = txtBandeira.getText().trim();
        int ano;
        try { ano = Integer.parseInt(txtAno.getText().trim()); }
        catch (NumberFormatException ex) { throw new Exception("O ano de fabrico deve ser um número inteiro válido (ex.: 2020)."); }
        if (ano <= 1950) throw new Exception("O ano de fabrico deve ser superior a 1950.");
        if (band.isEmpty()) throw new Exception("Insira a bandeira.");
        return new Navio(id, nome, imo, cmbTipo.getValue(), cap, comp, band, ano, EstadoOperacional.ATIVO, null);
    }

    private void carregarNavios() {
        tabelaNavios.setItems(FXCollections.observableArrayList(navioService.listarNavios()));
    }

    private void limparFormulario() {
        txtNome.clear(); txtImo.setText("IMO"); cmbTipo.setValue(null);
        txtCapacidade.clear(); txtCompartimentos.clear(); txtBandeira.clear(); txtAno.clear();
        tabelaNavios.getSelectionModel().clearSelection();
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
