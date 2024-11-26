package explorer.view;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ExplorerView {
    private final BorderPane rootPane;
    private final ListView<String> fileListView;
    private final Label currentPathLabel;
    private final Button backButton;

    public ExplorerView(Stage primaryStage) {
        rootPane = new BorderPane();
        fileListView = new ListView<>();
        currentPathLabel = new Label();
        backButton = new Button("Back");

        VBox topPanel = new VBox();
        topPanel.getChildren().addAll(currentPathLabel, backButton);

        rootPane.setTop(topPanel);
        rootPane.setCenter(fileListView);

        Scene scene = new Scene(rootPane, 600, 400);
        primaryStage.setTitle("Explorer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public BorderPane getRootPane() {
        return rootPane;
    }

    public ListView<String> getFileListView() {
        return fileListView;
    }

    public Label getCurrentPathLabel() {
        return currentPathLabel;
    }

    public Button getBackButton() {
        return backButton;
    }
}
