
Installation de Java FX
-----------------------

Nécessaire à partir de Java 9

Téléchargement JavaFX
---------------------

https://openjfx.io/
https://gluonhq.com/products/javafx/

Configuration Eclipse, accès aux jars JavaFX
--------------------------------------------

clic droit sur le projet puis
Java Build path / Libraries / Modulepath / Add external jars

Configuration Eclipse, exécution du programme
---------------------------------------------

clic droit sur le programme à exécuter puis
Run As / Run configurations
Arguments / VM arguments
--module-path "/.../javafx-sdk-11.0.2/lib" --add-modules=javafx.controls,javafx.fxml
