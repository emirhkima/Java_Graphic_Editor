package robic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Robic extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
    	
    	//charge l'interface graphique
        FXMLLoader loader = new FXMLLoader(getClass().getResource("modele.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Projet L3");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
	public static void main(String[] args) {
		launch(args);
	}
}