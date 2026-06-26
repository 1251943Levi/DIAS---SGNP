package controller;

import dao.PortoDAO;
import dao.TipoNavioDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    @FXML private ComboBox<EstadoOperacional> cmbEstado;

    @FXML private TextField          txtPesquisa;

    private final ObservableList<Navio> dadosNavios = FXCollections.observableArrayList();

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

        // Estado: permite Ativar / Desativar o navio
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoOperacional.ATIVO, EstadoOperacional.INATIVO));
        cmbEstado.setValue(EstadoOperacional.ATIVO);

        // Pesquisa/filtro sobre a tabela de navios
        FilteredList<Navio> filtrados = new FilteredList<>(dadosNavios, n -> true);
        txtPesquisa.textProperty().addListener((obs, anterior, texto) ->
                filtrados.setPredicate(navioCorresponde(texto)));
        tabelaNavios.setItems(filtrados);

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
            // Navio em manutenção mantém-se EM_MANUTENCAO; o estado só muda ao concluir a manutenção
            if (sel.getEstadoOperacional() == EstadoOperacional.EM_MANUTENCAO)
                n.setEstadoOperacional(EstadoOperacional.EM_MANUTENCAO);
            navioService.atualizarNavio(n);
            limparFormulario();
            carregarNavios();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    private void onEliminar() {
        Navio sel = tabelaNavios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um navio."); return; }
        try {
            navioService.eliminarNavio(sel.getId());
            carregarNavios();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
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
        cmbEstado.setValue(sel.getEstadoOperacional());
        // Em manutenção: não permitir mudar o estado (só ao concluir a manutenção)
        cmbEstado.setDisable(sel.getEstadoOperacional() == EstadoOperacional.EM_MANUTENCAO);
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
            // Janela modal: bloqueia a janela principal enquanto esta aberta,
            // garantindo uma so janela ativa de cada vez (evita sobrecarregar a BD).
            stage.initOwner(tabelaNavios.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            // Ao fechar a janela de manutenções, recarrega a tabela para refletir o estado atualizado
            stage.setOnHidden(e -> carregarNavios());
            stage.showAndWait();
        } catch (Exception e) { mostrarErro("Erro ao abrir manutenções: " + e.getMessage()); }
    }

    @FXML
    private void onNovoTipo() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Novo Tipo de Navio");
        dlg.setHeaderText("Insira os dados do novo tipo de navio");
        ButtonType btnOk = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        TextField tfNome = new TextField(); tfNome.setPromptText("Nome (ex.: Aframax)");
        TextField tfCap  = new TextField(); tfCap.setPromptText("Capacidade máxima (t)");
        TextField tfMax  = new TextField(); tfMax.setPromptText("Nº máximo de cargas");

        javafx.scene.layout.GridPane gp = new javafx.scene.layout.GridPane();
        gp.setHgap(8); gp.setVgap(8);
        gp.add(new Label("Nome:"), 0, 0);                gp.add(tfNome, 1, 0);
        gp.add(new Label("Capacidade máx (t):"), 0, 1);  gp.add(tfCap, 1, 1);
        gp.add(new Label("Nº máx. cargas:"), 0, 2);      gp.add(tfMax, 1, 2);
        dlg.getDialogPane().setContent(gp);

        dlg.showAndWait().ifPresent(resp -> {
            if (resp != btnOk) return;
            try {
                String nome = tfNome.getText().trim();
                if (nome.isEmpty()) throw new Exception("Insira o nome do tipo de navio.");
                double cap;
                try { cap = Double.parseDouble(tfCap.getText().trim()); }
                catch (NumberFormatException ex) { throw new Exception("A capacidade máxima deve ser um número válido (ex.: 75000)."); }
                int maxc;
                try { maxc = Integer.parseInt(tfMax.getText().trim()); }
                catch (NumberFormatException ex) { throw new Exception("O número máximo de cargas deve ser um número inteiro válido (ex.: 4)."); }

                TipoNavio novo = new TipoNavio(0, nome, cap, maxc);
                tipoNavioDAO.inserir(novo);
                cmbTipo.setItems(FXCollections.observableArrayList(tipoNavioDAO.listarTodos()));
                cmbTipo.setValue(novo);   // já fica selecionado para o navio
            } catch (Exception e) {
                mostrarErro(e.getMessage());
            }
        });
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
        if (ano > java.time.Year.now().getValue()) throw new Exception("O ano de fabrico não pode ser superior ao ano atual (" + java.time.Year.now().getValue() + ").");
        if (band.isEmpty()) throw new Exception("Insira a bandeira.");
        if (!band.matches("[\\p{L} ]+")) throw new Exception("A bandeira só pode conter letras.");
        EstadoOperacional estado = cmbEstado.getValue() != null ? cmbEstado.getValue() : EstadoOperacional.ATIVO;
        return new Navio(id, nome, imo, cmbTipo.getValue(), cap, comp, band, ano, estado, null);
    }

    private void carregarNavios() {
        dadosNavios.setAll(navioService.listarNavios());
    }

    /** Pesquisa, sem distinguir maiúsculas, em nome, IMO, tipo, bandeira e estado. */
    private java.util.function.Predicate<Navio> navioCorresponde(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        return n -> {
            if (q.isEmpty()) return true;
            String tipo = n.getTipoNavio() != null ? n.getTipoNavio().getNome() : "";
            return n.getNome().toLowerCase().contains(q)
                    || n.getCodigoImo().toLowerCase().contains(q)
                    || tipo.toLowerCase().contains(q)
                    || n.getBandeira().toLowerCase().contains(q)
                    || n.getEstadoOperacional().name().toLowerCase().contains(q);
        };
    }

    private void limparFormulario() {
        txtNome.clear(); txtImo.setText("IMO"); cmbTipo.setValue(null);
        txtCapacidade.clear(); txtCompartimentos.clear(); txtBandeira.clear(); txtAno.clear();
        cmbEstado.setValue(EstadoOperacional.ATIVO);
        cmbEstado.setDisable(false);
        tabelaNavios.getSelectionModel().clearSelection();
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
