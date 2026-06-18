import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        Scene scene = new Scene(abas, 1100, 700);
        stage.setTitle("SGNP — Sistema de Gestão de Navios Petroleiros");
        stage.setScene(scene);
        stage.show();
    }

    private Tab criarAba(String titulo, String fxml) throws Exception {
        Parent conteudo = FXMLLoader.load(getClass().getResource(fxml));
        return new Tab(titulo, conteudo);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
