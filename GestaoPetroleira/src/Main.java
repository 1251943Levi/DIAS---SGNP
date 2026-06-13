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
                criarAba("Navios",  "/resources/view/NaviosView.fxml"),
                criarAba("Viagens", "/resources/view/ViagensView.fxml")
        );

        Scene scene = new Scene(abas, 900, 600);
        stage.setTitle("DIAS — Gestão de Navios e Viagens");
        stage.setScene(scene);
        stage.show();
    }

    /** Carrega um FXML e devolve-o como conteúdo de um separador. */
    private Tab criarAba(String titulo, String fxml) throws Exception {
        Parent conteudo = FXMLLoader.load(getClass().getResource(fxml));
        return new Tab(titulo, conteudo);
    }

    public static void main(String[] args) {
        launch(args);
    }
}