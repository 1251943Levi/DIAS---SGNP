package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import service.CargaService;
import service.NavioService;
import service.TripulacaoService;
import service.ViagemService;

import java.util.List;

public class ViagemController implements Atualizavel {

    // ── tabela viagens ─────────────────────────────────────────────────────────
    @FXML private TableView<Viagem>             tabelaViagens;
    @FXML private TableColumn<Viagem, Integer>  colId;
    @FXML private TableColumn<Viagem, String>   colNavio;
    @FXML private TableColumn<Viagem, String>   colOrigem;
    @FXML private TableColumn<Viagem, String>   colDestino;
    @FXML private TableColumn<Viagem, String>   colPartida;
    @FXML private TableColumn<Viagem, String>   colChegada;
    @FXML private TableColumn<Viagem, String>   colEstado;

    // ── formulário nova viagem ─────────────────────────────────────────────────
    @FXML private ComboBox<Navio>  cmbNavio;
    @FXML private ComboBox<Porto>  cmbOrigem;
    @FXML private ComboBox<Porto>  cmbDestino;
    @FXML private DatePicker       dtPartida;
    @FXML private DatePicker       dtChegada;

    @FXML private TextField        txtPesquisa;

    private final ObservableList<Viagem> dadosViagens = FXCollections.observableArrayList();

    // ── painel associar cargas ─────────────────────────────────────────────────
    @FXML private TableView<Carga>            tabelaCargasViagem;
    @FXML private TableColumn<Carga, String>  colCargaTanque;
    @FXML private TableColumn<Carga, String>  colCargaNome;
    @FXML private TableColumn<Carga, String>  colCargaTipo;
    @FXML private TableColumn<Carga, Double>  colCargaPeso;

    @FXML private ComboBox<Carga>   cmbCarga;
    @FXML private ComboBox<Integer> cmbTanque;
    @FXML private Button            btnAssociarCarga;
    @FXML private Button            btnDesassociarCarga;

    // ── painel associar tripulação ─────────────────────────────────────────────
    @FXML private TableView<TripulacaoViagem>           tabelaTripulacaoViagem;
    @FXML private TableColumn<TripulacaoViagem, String> colTripNome;
    @FXML private TableColumn<TripulacaoViagem, String> colTripFuncao;

    @FXML private ComboBox<Tripulante> cmbTripulante;
    @FXML private ComboBox<Funcao>     cmbFuncaoTrip;

    private final ViagemService     viagemService     = new ViagemService();
    private final NavioService      navioService      = new NavioService();
    private final CargaService      cargaService      = new CargaService();
    private final TripulacaoService tripulacaoService = new TripulacaoService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNavio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNavio().getNome()));
        colOrigem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPortoOrigem().getNome()));
        colDestino.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPortoDestino().getNome()));
        colPartida.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataPartida().toString()));
        colChegada.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDataChegada() != null ? d.getValue().getDataChegada().toString() : "—"));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));

        if (colCargaTanque != null) colCargaTanque.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumeroTanque() != null ? "Tanque " + d.getValue().getNumeroTanque() : "—"));
        if (colCargaNome  != null) colCargaNome .setCellValueFactory(new PropertyValueFactory<>("designacao"));
        if (colCargaTipo  != null) colCargaTipo .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTipoCarga() != null ? d.getValue().getTipoCarga().getNome() : "—"));
        if (colCargaPeso  != null) colCargaPeso .setCellValueFactory(new PropertyValueFactory<>("peso"));
        if (colTripNome   != null) colTripNome  .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTripulante().getNome()));
        if (colTripFuncao != null) colTripFuncao.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFuncao().name()));

        cmbNavio.setItems(FXCollections.observableArrayList(navioService.listarNaviosAtivos()));
        cmbOrigem.setItems(FXCollections.observableArrayList(viagemService.listarPortos()));
        cmbDestino.setItems(FXCollections.observableArrayList(viagemService.listarPortos()));

        if (cmbFuncaoTrip != null)
            cmbFuncaoTrip.setItems(FXCollections.observableArrayList(Funcao.values()));

        tabelaViagens.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> onViagemSelecionada(sel));

        // Pesquisa/filtro sobre a tabela de viagens
        FilteredList<Viagem> filtradas = new FilteredList<>(dadosViagens, v -> true);
        txtPesquisa.textProperty().addListener((obs, anterior, texto) ->
                filtradas.setPredicate(viagemCorresponde(texto)));
        tabelaViagens.setItems(filtradas);

        carregarViagens();
    }

    /** Pesquisa, sem distinguir maiúsculas, em navio, portos e estado da viagem. */
    private java.util.function.Predicate<Viagem> viagemCorresponde(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        return v -> {
            if (q.isEmpty()) return true;
            return v.getNavio().getNome().toLowerCase().contains(q)
                    || v.getPortoOrigem().getNome().toLowerCase().contains(q)
                    || v.getPortoDestino().getNome().toLowerCase().contains(q)
                    || v.getEstado().name().toLowerCase().contains(q);
        };
    }

    private void onViagemSelecionada(Viagem v) {
        if (v == null) return;
        try {
            if (tabelaCargasViagem != null)
                tabelaCargasViagem.setItems(FXCollections.observableArrayList(
                        cargaService.listarCargasDaViagem(v.getId())));
            if (cmbCarga != null)
                cmbCarga.setItems(FXCollections.observableArrayList(
                        cargaService.listarCargasLivresCompativeis(v.getNavio().getTipoNavio().getId())));
            if (cmbTanque != null)
                cmbTanque.setItems(FXCollections.observableArrayList(cargaService.tanquesLivres(v)));
            if (tabelaTripulacaoViagem != null)
                tabelaTripulacaoViagem.setItems(FXCollections.observableArrayList(
                        tripulacaoService.listarTripulacaoDaViagem(v.getId())));
            if (cmbTripulante != null)
                cmbTripulante.setItems(FXCollections.observableArrayList(
                        tripulacaoService.listarTripulantesDisponiveis()));

            // Só se mexe nas cargas enquanto a viagem está PLANEADA — desativa os controlos caso contrário.
            boolean planeada = v.getEstado() == EstadoViagem.PLANEADA;
            if (cmbCarga != null)           cmbCarga.setDisable(!planeada);
            if (cmbTanque != null)          cmbTanque.setDisable(!planeada);
            if (btnAssociarCarga != null)   btnAssociarCarga.setDisable(!planeada);
            if (btnDesassociarCarga != null) btnDesassociarCarga.setDisable(!planeada);
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    /** Chamado pelo Main ao abrir a aba Viagens: recarrega navios, portos e viagens
     *  (apanha navios/portos criados noutras abas). */
    @Override
    public void atualizar() {
        Viagem selecionada = tabelaViagens.getSelectionModel().getSelectedItem();
        int idSelecionado = selecionada != null ? selecionada.getId() : -1;

        cmbNavio.setItems(FXCollections.observableArrayList(navioService.listarNaviosAtivos()));
        cmbOrigem.setItems(FXCollections.observableArrayList(viagemService.listarPortos()));
        cmbDestino.setItems(FXCollections.observableArrayList(viagemService.listarPortos()));
        carregarViagens();

        // Repõe a seleção da viagem (pelo id) para o painel de cargas/tripulação
        // recarregar com os dados atuais (ex.: tipo de carga alterado noutra aba).
        if (idSelecionado != -1)
            for (Viagem v : dadosViagens)
                if (v.getId() == idSelecionado) { tabelaViagens.getSelectionModel().select(v); break; }
    }

    private void carregarViagens() {
        dadosViagens.setAll(viagemService.listarViagens());
    }

    @FXML
    private void onCriar() {
        try {
            viagemService.criarViagem(cmbNavio.getValue(), cmbOrigem.getValue(),
                    cmbDestino.getValue(), dtPartida.getValue(), dtChegada.getValue());
            limparFormulario();
            carregarViagens();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
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
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    // ── Associar Cargas ────────────────────────────────────────────────────────
    @FXML
    private void onAssociarCarga() {
        Viagem v = tabelaViagens.getSelectionModel().getSelectedItem();
        Carga c  = cmbCarga != null ? cmbCarga.getValue() : null;
        Integer tanque = cmbTanque != null ? cmbTanque.getValue() : null;
        if (v == null) { mostrarErro("Selecione uma viagem."); return; }
        if (c == null) { mostrarErro("Selecione uma carga."); return; }
        if (tanque == null) { mostrarErro("Selecione o compartimento (tanque)."); return; }
        try {
            cargaService.associarCargaAViagem(v, c, tanque);
            onViagemSelecionada(v);
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onDesassociarCarga() {
        Viagem v = tabelaViagens.getSelectionModel().getSelectedItem();
        Carga c  = tabelaCargasViagem != null ? tabelaCargasViagem.getSelectionModel().getSelectedItem() : null;
        if (v == null || c == null) { mostrarErro("Selecione a viagem e a carga."); return; }
        try {
            cargaService.desassociarCargaDaViagem(v.getId(), c.getId());
            onViagemSelecionada(v);
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    // ── Associar Tripulação ────────────────────────────────────────────────────
    @FXML
    private void onAssociarTripulante() {
        Viagem v     = tabelaViagens.getSelectionModel().getSelectedItem();
        Tripulante t = cmbTripulante != null ? cmbTripulante.getValue() : null;
        if (v == null) { mostrarErro("Selecione uma viagem."); return; }
        if (t == null) { mostrarErro("Selecione um tripulante."); return; }
        Funcao f = t.getFuncao();   // função automática: a do próprio tripulante
        if (f == null) { mostrarErro("O tripulante selecionado não tem função definida."); return; }
        try {
            tripulacaoService.associarTripulanteAViagem(v, t, f);
            onViagemSelecionada(v);
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onDesassociarTripulante() {
        Viagem v = tabelaViagens.getSelectionModel().getSelectedItem();
        TripulacaoViagem tv = tabelaTripulacaoViagem != null ?
                tabelaTripulacaoViagem.getSelectionModel().getSelectedItem() : null;
        if (v == null || tv == null) { mostrarErro("Selecione a viagem e o tripulante."); return; }
        try {
            tripulacaoService.desassociarTripulante(v.getId(), tv.getTripulante());
            onViagemSelecionada(v);
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    // ── helpers ────────────────────────────────────────────────────────────────
    private void limparFormulario() {
        cmbNavio.setValue(null); cmbOrigem.setValue(null);
        cmbDestino.setValue(null); dtPartida.setValue(null); dtChegada.setValue(null);
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setContentText(msg); a.showAndWait();
    }
}
