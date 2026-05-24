package EasyGui;

import Simulation.CreateLogFile;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SimulationMainView extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        //在下方添加工具栏
        ToolBar toolBar = new ToolBar();
        root.setBottom(toolBar);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("MainView");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO,"[SimulationMainView]:Create Window successfully!");
        launch(args);
    }
}
