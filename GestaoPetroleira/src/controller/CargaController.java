package controller;

import dao.TipoNavioDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import service.CargaService;

import java.util.List;

public class CargaController implements Atualizavel {

    // ── TIPO_CARGA tab ─────────────────────────────────────────────────────────
    @FXML private TableView<TipoCarga> tabelaTiposCarga;
    @FXML private TableColumn<TipoCarga, Integer> colTcId;
    @FXML private TableColumn<TipoCarga, String>  colTcNome;
    @FXML private TableColumn<TipoCarga, String>  colTcProps;

    @FXML private TextField txtTcNome;
    @FXML private CheckBox  chkInflamavel;
    @FXML private CheckBox  chkCorrosiva;
    @FXML private CheckBox  chkToxica;

    // ── CARGA tab ─────────────────────────────────────────────────────────────
    @FXML private TableView<Carga> tabelaCargas;
    @FXML private TableColumn<Carga, Integer> colCId;
    @FXML private TableColumn<Carga, String>  colCDesig;
    @FXML private TableColumn<Carga, String>  colCTipo;
    @FXML private TableColumn<Carga, Double>  colCPeso;
    @FXML private TableColumn<Carga, Double>  colCVolume;

    @FXML private TextField          txtDesignacao;
    @FXML private ComboBox<TipoCarga> cmbTipoCarga;
    @FXML private TextField          txtPeso;
    @FXML private TextField          txtVolume;
    @FXML private TextField          txtPesquisaCarga;

    private final ObservableList<Carga> dadosCargas = FXCollections.observableArrayList();

    // ── COMPATIBILIDADE tab ────────────────────────────────────────────────────
    @FXML private TableView<Compatibilidade> tabelaCompat;
    @FXML private TableColumn<Compatibilidade, String> colCompNavio;
    @FXML private TableColumn<Compatibilidade, String> colCompCarga;

    @FXML private ComboBox<TipoNavio> cmbTipoNavio;
    @FXML private ComboBox<TipoCarga> cmbTipoCargaCompat;

    private final CargaService cargaService = new CargaService();
    private final TipoNavioDAO tipoNavioDAO = new TipoNavioDAO();

    @FXML
    public void initialize() {
        // Tipos de carga
        colTcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTcNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTcProps.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPropriedades()));

        // Cargas
        colCId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCDesig.setCellValueFactory(new PropertyValueFactory<>("designacao"));
        colCTipo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTipoCarga() != null ? d.getValue().getTipoCarga().getNome() : "—"));
        colCPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colCVolume.setCellValueFactory(new PropertyValueFactory<>("volume"));

        // Compatibilidade
        colCompNavio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoNavio().getNome()));
        colCompCarga.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoCarga().getNome()));

        // Combos
        cmbTipoNavio.setItems(FXCollections.observableArrayList(tipoNavioDAO.listarTodos()));

        // Pesquisa/filtro sobre a tabela de cargas
        FilteredList<Carga> filtradas = new FilteredList<>(dadosCargas, c -> true);
        txtPesquisaCarga.textProperty().addListener((obs, anterior, texto) ->
                filtradas.setPredicate(cargaCorresponde(texto)));
        tabelaCargas.setItems(filtradas);

        carregarTiposCarga();
        carregarCargas();
        carregarCompatibilidades();
    }

    /** Pesquisa, sem distinguir maiúsculas, em designação e tipo de carga. */
    private java.util.function.Predicate<Carga> cargaCorresponde(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        return c -> {
            if (q.isEmpty()) return true;
            String tipo = c.getTipoCarga() != null ? c.getTipoCarga().getNome() : "";
            return c.getDesignacao().toLowerCase().contains(q)
                    || tipo.toLowerCase().contains(q);
        };
    }

    // ── TIPO_CARGA actions ────────────────────────────────────────────────────
    @FXML
    private void onAdicionarTipoCarga() {
        String nome = txtTcNome.getText().trim();
        if (nome.isEmpty()) { mostrarErro("Insira o nome do tipo de carga."); return; }
        try {
            TipoCarga tc = new TipoCarga(0, nome, chkInflamavel.isSelected(),
                    chkCorrosiva.isSelected(), chkToxica.isSelected());
            cargaService.adicionarTipoCarga(tc);
            txtTcNome.clear(); chkInflamavel.setSelected(false);
            chkCorrosiva.setSelected(false); chkToxica.setSelected(false);
            carregarTiposCarga();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onEliminarTipoCarga() {
        TipoCarga sel = tabelaTiposCarga.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um tipo de carga."); return; }
        try {
            cargaService.eliminarTipoCarga(sel.getId());
            carregarTiposCarga();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    // ── CARGA actions ──────────────────────────────────────────────────────────
    @FXML
    private void onAdicionarCarga() {
        try {
            String desig = txtDesignacao.getText().trim();
            if (desig.isEmpty()) throw new Exception("Insira a designação da carga.");
            if (cmbTipoCarga.getValue() == null) throw new Exception("Selecione o tipo de carga.");
            double peso   = Double.parseDouble(txtPeso.getText().trim());
            double volume = Double.parseDouble(txtVolume.getText().trim());

            // Template sem portos: os portos são herdados da viagem ao associar.
            Carga c = new Carga(0, desig, cmbTipoCarga.getValue(), volume, peso, null, null);
            cargaService.adicionarCarga(c);
            limparFormCarga();
            carregarCargas();
        } catch (NumberFormatException e) {
            mostrarErro("Peso e Volume devem ser números.");
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onEliminarCarga() {
        Carga sel = tabelaCargas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione uma carga."); return; }
        try {
            cargaService.eliminarCarga(sel.getId());
            carregarCargas();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    /** Preenche o formulário com a carga selecionada na tabela (para editar, ex.: pôr o peso). */
    @FXML
    private void onSelecionarCarga() {
        Carga sel = tabelaCargas.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        txtDesignacao.setText(sel.getDesignacao());
        cmbTipoCarga.setValue(sel.getTipoCarga());
        txtPeso.setText(String.valueOf(sel.getPeso()));
        txtVolume.setText(String.valueOf(sel.getVolume()));
    }

    /** Atualiza a carga selecionada (usado para preencher o peso de uma carga pendente). */
    @FXML
    private void onAtualizarCarga() {
        Carga sel = tabelaCargas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione uma carga."); return; }
        try {
            String desig = txtDesignacao.getText().trim();
            if (desig.isEmpty()) throw new Exception("Insira a designação da carga.");
            if (cmbTipoCarga.getValue() == null) throw new Exception("Selecione o tipo de carga.");
            double peso   = Double.parseDouble(txtPeso.getText().trim());
            double volume = Double.parseDouble(txtVolume.getText().trim());

            sel.setDesignacao(desig);
            sel.setTipoCarga(cmbTipoCarga.getValue());
            sel.setPeso(peso);
            sel.setVolume(volume);
            cargaService.atualizarCarga(sel);
            limparFormCarga();
            carregarCargas();
        } catch (NumberFormatException e) {
            mostrarErro("Peso e Volume devem ser números.");
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    // ── COMPATIBILIDADE actions ────────────────────────────────────────────────
    @FXML
    private void onAdicionarCompatibilidade() {
        if (cmbTipoNavio.getValue() == null || cmbTipoCargaCompat.getValue() == null) {
            mostrarErro("Selecione tipo de navio e tipo de carga."); return;
        }
        try {
            Compatibilidade comp = new Compatibilidade(0, cmbTipoNavio.getValue(), cmbTipoCargaCompat.getValue());
            cargaService.adicionarCompatibilidade(comp);
            carregarCompatibilidades();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onEliminarCompatibilidade() {
        Compatibilidade sel = tabelaCompat.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione uma compatibilidade."); return; }
        try {
            cargaService.eliminarCompatibilidade(sel.getTipoNavio().getId(), sel.getTipoCarga().getId());
            carregarCompatibilidades();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    // ── helpers ────────────────────────────────────────────────────────────────
    private void carregarTiposCarga() {
        List<TipoCarga> tipos = cargaService.listarTiposCarga();
        tabelaTiposCarga.setItems(FXCollections.observableArrayList(tipos));
        cmbTipoCarga.setItems(FXCollections.observableArrayList(tipos));
        cmbTipoCargaCompat.setItems(FXCollections.observableArrayList(tipos));
    }

    private void carregarCargas() {
        // Catálogo = cargas "template" (ainda sem viagem). As cópias por viagem veem-se nas Viagens.
        dadosCargas.setAll(cargaService.listarCargasLivres());
    }

    private void carregarCompatibilidades() {
        tabelaCompat.setItems(FXCollections.observableArrayList(cargaService.listarCompatibilidades()));
    }

    /** Limpa o formulário e a seleção — para começar uma carga nova. */
    @FXML
    private void onLimparCarga() {
        limparFormCarga();
        tabelaCargas.getSelectionModel().clearSelection();
    }

    private void limparFormCarga() {
        txtDesignacao.clear(); txtPeso.clear(); txtVolume.clear();
        cmbTipoCarga.setValue(null);
    }

    /** Chamado pelo Main ao abrir a aba Cargas: recarrega tudo (apanha cargas pendentes novas). */
    @Override
    public void atualizar() {
        cmbTipoNavio.setItems(FXCollections.observableArrayList(tipoNavioDAO.listarTodos()));
        carregarTiposCarga();
        carregarCargas();
        carregarCompatibilidades();
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setContentText(msg); a.showAndWait();
    }
}
