package explorer;

import explorer.controller.ExplorerController;
import explorer.model.ExplorerModel;
import explorer.view.ExplorerView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        ExplorerModel model = new ExplorerModel();
        ExplorerView view = new ExplorerView(primaryStage);
        new ExplorerController(model, view);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
