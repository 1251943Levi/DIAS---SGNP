import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        TabPane abas = new TabPane();
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        abas.getTabs().addAll(
                criarAba("Navios",     "/resources/view/NaviosView.fxml"),
                criarAba("Viagens",    "/resources/view/ViagemView.fxml"),
                criarAba("Cargas",     "/resources/view/CargasView.fxml"),
                criarAba("Tripulação", "/resources/view/TripulacaoView.fxml")
        );

        // Ao mudar de aba, refresca os dados se o controller suportar (interface Atualizavel)
        abas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, selecionada) -> {
            try {
                if (selecionada != null && selecionada.getUserData() instanceof controller.Atualizavel a)
                    a.atualizar();
            } catch (Exception e) {
                Alert al = new Alert(Alert.AlertType.ERROR);
                al.setTitle("Erro"); al.setHeaderText(null);
                al.setContentText("Não foi possível atualizar os dados: " + e.getMessage());
                al.showAndWait();
            }
        });

        Scene scene = new Scene(abas, 1400, 850);
        stage.setTitle("SGNP — Sistema de Gestão de Navios Petroleiros");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    private Tab criarAba(String titulo, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent conteudo = loader.load();
            Tab tab = new Tab(titulo, conteudo);
            tab.setUserData(loader.getController());   // guarda o controller para refrescar ao abrir a aba
            return tab;
        } catch (Exception e) {
            // Se a aba falhar a carregar (ex.: base de dados indisponível), mostra a mensagem
            // em vez de a aplicação rebentar no arranque.
            Label erro = new Label("Não foi possível carregar este separador:\n" + e.getMessage());
            erro.setStyle("-fx-padding:20;");
            return new Tab(titulo, erro);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
