package robic;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Base64;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Control {

    public Button b1;
    public TextArea textArea1;
    public Label label1;
    public ImageView image1;

    public void b1_exec(ActionEvent event) throws Exception {
    	
    	try {
	    	label1.setText("> ");
	    	
	    	System.out.println("b1_exec");
	    	String txt = textArea1.getText();
	    	System.out.println("> textArea1 = "+txt);
	    	
	    	//traitement de la chaine de caract�res
	    	txt = txt.replace("\n", "");
	    	txt = txt.replace("\"", "'");
	    	
	    	//creation d'un Message conversion de l'objet java Message en String json
	    	Message m = new Message("sexpr",txt);
	    	//String json = (new ObjectMapper()).writeValueAsString(m);
	    	String json = Message.toJson(m);
    	
	    	//creation socket
	    	Socket s = new Socket("localhost", 2121);
	    	System.out.println("Socket cr��e.");
		
			//InputStream
			BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//OutputStream
			PrintStream printer = new PrintStream(s.getOutputStream());
			
			//envoi de la commande
	        printer.println(json);
	        System.out.println("Commande envoy�e.");
	        
			String code = "ko";
			while (!code.equals("ok")) {
	        
		        //reception trace d'execution
		        json = reader.readLine();
		        
		        //conversion vers objet Java Message
				//m = (new ObjectMapper()).readValue(json, Message.class); //� utiliser si les librairies json fonctionnent
				m = Message.fromJson(json);
				
				//si on a re�u une image
				if (m.getType().equals("image")) {
					
					//conversion et �criture de l'image en local
	                code = m.getMess();
	                byte[] decodedBytes = Base64.getDecoder().decode(code);
	                FileOutputStream fos = new FileOutputStream("D:\screen.jpg");
	                fos.write(decodedBytes, 0, decodedBytes.length); //ecriture des donn�es
	                fos.close();
	                
	                //mise � jour de l'interface graphique
	                Image im = new Image("file:"+"D:\screen.jpg");
	                image1.setImage(im);
	            }
		        
				//si on a bien re�u un texte...
		        if (m.getType().equals("text")) {
					 code = m.getMess();
				} 
		        
		        //affichage de messages l'interface graphique
		        System.out.println("Message re�u : "+code);
		        if (code.equals("ok")) {
		        	label1.setText(label1.getText()+"> Done.\n");
		        } else {
		        	if (!m.getType().equals("image")) {
		        		label1.setText(label1.getText()+"> "+code+"\n");
		        	} else {
		        		label1.setText(label1.getText()+"> Image re�ue.\n");
		        	}
		        }
			}
			
			s.close();
			
    	} catch (Exception e) {
    		
    		//�chec de connection
    		System.out.println("Tentative de connexion echou�e, le serveur n'est pas disponible.");
  	      	label1.setText(label1.getText()+"Tentative de connexion echou�e, le serveur n'est pas disponible.");
    	}
    }
}