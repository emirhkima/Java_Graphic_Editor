package exercice5.examples;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import exercice6.Message;
import graphicLayer.GContainer;
import graphicLayer.GElement;
import graphicLayer.GImage;
import graphicLayer.GOval;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import graphicLayer.GString;
import stree.parser.SNode;
import stree.parser.SParser;

public class Exercice5 {
	// Une seule variable d'instance
	Environment environment = new Environment();
	GSpace space = new GSpace("Exercice 5", new Dimension(200, 100));
	
	BufferedReader br;
	PrintStream ps;
	
	Message m;
	String json;
		
	public Exercice5() {
		space.open();

		Reference spaceRef = new Reference(space);
		Reference rectClassRef = new Reference(GRect.class);
		Reference ovalClassRef = new Reference(GOval.class);
		Reference imageClassRef = new Reference(GImage.class);
		Reference stringClassRef = new Reference(GString.class);

		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("sleep", new Sleep());
		spaceRef.addCommand("setDim", new SetDim());

		spaceRef.addCommand("add", new AddElement());
		spaceRef.addCommand("del", new DelElement());
		
		rectClassRef.addCommand("new", new NewElement());
		ovalClassRef.addCommand("new", new NewElement());
		imageClassRef.addCommand("new", new NewImage());
		stringClassRef.addCommand("new", new NewString());

		environment.addReference("space", spaceRef);
		environment.addReference("Rect", rectClassRef);
		environment.addReference("Oval", ovalClassRef);
		environment.addReference("Image", imageClassRef);
		environment.addReference("Label", stringClassRef);
		
		this.mainLoop();
	}
	
	public void mainLoop() {
		
		try {
			//création du serveur
			ServerSocket serveurFTP = new ServerSocket(2121);
			
			while (true) {
				
				Socket socket = serveurFTP.accept();
				
				//InputStream
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						
				//OutputStream
				ps = new PrintStream(socket.getOutputStream());
				
				//reception String json
				json = br.readLine();
				System.out.println("json recu = "+json);
				
				//conversion vers objet Java Message
				//Message m = (new ObjectMapper()).readValue(json, Message.class);
				m = Message.fromJson(json);
				System.out.println("Type = "+m.getType()+" message = "+m.getMess());
				
				//on récupère le script
				String input = "";
				if (m.getType().equals("sexpr")) {
                    input = m.getMess();
                    input = input.replace("'", "\"");
                    //fermeture du serveur à distance
                    if (input.toLowerCase().equals("quit")) {
                        System.out.println("Le client ferme tout.");
                        m = new Message("text","ok");
                        //json = (new ObjectMapper()).writeValueAsString(m);
                        json = Message.toJson(m);
                        ps.println(json);
                        socket.close();
                        serveurFTP.close();
                        System.exit(0);
                    }
				}
				
				// creation du parser
				SParser<SNode> parser = new SParser<>();
				// compilation
				List<SNode> compiled = null;
				try {
					compiled = parser.parse(input);
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
				// execution des s-expressions compilees
				Iterator<SNode> itor = compiled.iterator();
				while (itor.hasNext()) {
					new Interpreter().compute(environment, itor.next());
				}
				
				Robot robot = new Robot(); // on prend une capture d'ecran
                BufferedImage image = robot.createScreenCapture(new Rectangle(8, 30, 200, 100));
                ImageIO.write(image, "jpg", new File("screen.jpg"));

                // envoi d'image
                String NomF2;
                List<File> files = Files.list(Paths.get(""))
                        .map(Path::toFile)            //on stocke la liste des fichiers du repertoire dans une liste
                        .collect(Collectors.toList());
                for(File f : files) {
                    NomF2 = f.getAbsoluteFile().getName();    //on verifie si le nom du screen est présent dans la liste
                    if(NomF2.equals("screen.jpg")) {
                        FileInputStream fis = new FileInputStream("screen.jpg");
                        byte[] bytes = new byte[(int)f.length()];
                        fis.read(bytes);
                        String encodeBytes = new String(Base64.getEncoder().encodeToString(bytes));

                        m = new Message("image", encodeBytes);
                        json = Message.toJson(m);
                        ps.println(json);
                        fis.close();
                    }
                }
                
				//envoi confirmation au client
				//creation d'un Message et conversion de l'objet java Message en String json
		    	m = new Message("text","ok");
		    	//json = (new ObjectMapper()).writeValueAsString(m);
		    	json = Message.toJson(m);
		        ps.println(json);
			}
		} catch (Exception e) {}			
	}
	
	public class Interpreter {
		public void compute (Environment environment, SNode expr) {
			// quel est le nom du receiver
			String receiverName = expr.get(0).contents();
			// quel est le receiver
			Reference receiver = environment.getReferenceByName(receiverName);
			// le receiver existe-t-il
			if (receiver==null) {
				System.out.println(receiverName+" Pas de référence !"); // NON, message d'erreur
				//envoi de l'erreur au client
				m = new Message("text",receiverName+" : Pas de référence !");
		    	json = Message.toJson(m);
		        ps.println(json);
			} else {
				receiver.run(expr); // OUI, demande au receiver d'executer la s-expression compilee
				//envoi au client
				m = new Message("text",receiverName+" : Reference trouvée.");
		    	json = Message.toJson(m);
		        ps.println(json);
			}
		}
	}
	
	public interface Command {
		// le receiver est l'objet qui va executer method
		// method est la s-expression resultat de la compilation
		 // du code source a executer
		// exemple de code source : "(space setColor black)"
		abstract public Reference run(Reference receiver, SNode method);
	}
	
	public class Reference {
		Object receiver;
		Map<String, Command> primitives;
		
		public Reference(Object receiver) {
			this.receiver = receiver;
			primitives = new HashMap<String, Command>();
		}
		
		public Reference run(SNode nds) {
			String cmd_str = nds.get(1).contents(); 
			//System.out.println(cmd_str);
			Command cmd = primitives.get(cmd_str);
			//System.out.println(cmd);
			// la commmande existe-t-elle 
			if (cmd==null) { // NON, message d'erreur et retour d'un reference null;
				System.out.println("Pas de commande "+cmd_str+" pour "+nds.get(0).contents());
				m = new Message("text","Pas de commande "+cmd_str+" pour "+nds.get(0).contents());
		    	json = Message.toJson(m);
		        ps.println(json);
				return null; 
			} else { // OUI, execution de la commande pour cette reference, et retourne la nouvelle reference 
				Reference ref = cmd.run(this ,nds);
				m = new Message("text","Commande "+cmd_str+" pour "+nds.get(0).contents()+" en cours d'execution");
		    	json = Message.toJson(m);
		        ps.println(json);
				return ref; 
			}
		}
		
		public void addCommand(String name, Command Cmd) {
			primitives.put(name,Cmd); 
		}
		
		public Object getReceiver() {
			return this.receiver;
		}
	}
	
	public class Environment {
		HashMap<String, Reference> variables;
		public Environment() {
			variables = new HashMap<String, Reference>();
		}
		public void addReference(String name, Reference Ref) {
			variables.put(name,Ref); 
		}
		
		public Reference getReferenceByName (String Ref) {
					return variables.get(Ref);
		}
	}
	
	/* string to color*/
	public Color toColor (String color) {
		Color c; 
		if (color.equals("red")) {
			c = Color.red;
		}else if (color.equals("yellow")) {
			c = Color.yellow;
		}else if (color.equals("green")) {
			c = Color.green;
		}else if (color.equals("blue")) {
			c = Color.blue;
		}else if (color.equals("black")) {
			c = Color.black;
		}else if (color.equals("white")) {
			c = Color.white;
		}else {
			return null;
		}
		
		return c;
	}
	
	/**********************************COMMANDE***************************************************/
	/* Nouvel Element */
	class NewElement implements Command {
		public Reference run(Reference reference, SNode method) {
			try {
				@SuppressWarnings("unchecked")
				GElement e = ((Class<GElement>) reference.getReceiver()).getDeclaredConstructor().newInstance();
				Reference ref = new Reference(e);
				ref.addCommand("setColor", new SetColor());
				ref.addCommand("translate", new Translate());
				ref.addCommand("setDim", new SetDim());
				ref.addCommand("add", new AddElement());
				ref.addCommand("del", new DelElement());
				return ref;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	/* Nouvelle Image */
	class NewImage implements Command {
		public Reference run(Reference reference, SNode method) {
			try {
				System.out.println(method.get(2).contents());
				File path = new File(method.get(2).contents());
				BufferedImage rawImage = null;
				try {
					rawImage = ImageIO.read(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
				GImage e = new GImage(rawImage);
				Reference ref = new Reference(e);
				ref.addCommand("translate", new Translate());
				return ref;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	/*nouveau texte */
	class NewString implements Command {
		public Reference run(Reference reference, SNode method) {
			try {
				String tmp = new String();
				String str = new String();
				str = method.get(2).contents();
				
				for (int i=0 ; i<str.length()-2; i++) {
					tmp = tmp + str.charAt(i+1);
				}
				GString e = new GString();
				e.setString(tmp);
				Reference ref = new Reference(e);
				ref.addCommand("translate", new Translate());
				ref.addCommand("setColor", new SetColor());
				
				return ref;
			} catch (Throwable e){
				e.printStackTrace();
			}
			return null;
		}
	}
	/* ajoute un element */
	class AddElement implements Command{
		public Reference run(Reference ref, SNode method) {
			SNode met=method.get(3);
			Reference reference = environment.getReferenceByName(method.get(3).get(0).contents()).run(met);
			environment.addReference(method.get(0).contents()+"."+method.get(2).contents(), reference);
			((GContainer) ref.receiver).addElement((GElement) reference.getReceiver());
			((GContainer) ref.receiver).repaint();
			if (method.get(3).get(0).contents().equals("Label") ) {
				((GString) reference.getReceiver()).setFont(new Font("Arial", Font.CENTER_BASELINE, 13));
			}
			return null;
		}
	}
	/* supprime element */
	class DelElement implements Command{
		public Reference run(Reference ref, SNode method) {
			Reference newref = environment.getReferenceByName(method.get(2).contents());
			System.out.println(newref);
			((GContainer) ref.receiver).removeElement((GElement) newref.getReceiver());
			((GContainer) ref.receiver).repaint();
			return null;
		}
	}
	

	/* Change la couleur */
	public class SetColor implements Command {
		Color newColor;
		@SuppressWarnings("rawtypes")
		Class SPACE = GSpace.class;
		@SuppressWarnings("rawtypes")
		Class RECT = GRect.class;
		@SuppressWarnings("rawtypes")
		Class OVAL = GOval.class;
		@SuppressWarnings("rawtypes")
		Class STRING = GString.class;
		@Override
		public Reference run(Reference ref, SNode nds) {
			Color color = toColor(nds.get(2).contents());
			if (SPACE.isInstance(ref.receiver))
				((GSpace) ref.receiver).setColor(color);
			else if (RECT.isInstance(ref.receiver))
				((GRect) ref.receiver).setColor(color);
			else if (OVAL.isInstance(ref.receiver))
				((GOval) ref.receiver).setColor(color);
			else if (STRING.isInstance(ref.receiver))
				((GString) ref.receiver).setColor(color);
			
			return null;
		}
	}
	/* Fait une pause */
	public class Sleep implements Command {
	
		@Override
		public Reference run(Reference ref, SNode nds) {
			String t = nds.get(2).contents();
			Integer time = Integer.parseInt(t);
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	/* Se deplace */
	public class Translate implements Command {	
		@Override
		public Reference run(Reference ref, SNode nds) {
			@SuppressWarnings("rawtypes")
			Class RECT = GRect.class;
			@SuppressWarnings("rawtypes")
			Class OVAL = GOval.class;
			@SuppressWarnings("rawtypes")
			Class IMAGE = GImage.class;
			@SuppressWarnings("rawtypes")
			Class STRING = GString.class;
			Integer x = Integer.parseInt(nds.get(2).contents());
			Integer y = Integer.parseInt(nds.get(3).contents());
			Point gap = new Point(0,0);
			gap.translate(x,y);
			if (RECT.isInstance(ref.receiver))
				((GRect) ref.receiver).translate(gap);
			else if (OVAL.isInstance(ref.receiver))
				((GOval) ref.receiver).translate(gap);
			else if (IMAGE.isInstance(ref.receiver))
				((GImage) ref.receiver).translate(gap);
			else if (STRING.isInstance(ref.receiver))
				((GString) ref.receiver).translate(gap);
			return null;
		}
	}
	/* Dimension */
	public class SetDim implements Command {
		@SuppressWarnings("rawtypes")
		Class RECT = GRect.class;
		@SuppressWarnings("rawtypes")
		Class OVAL = GOval.class;
		@SuppressWarnings("rawtypes")
		Class SPACE = GSpace.class;
		@Override
		public Reference run(Reference ref, SNode nds) {
			Integer x = Integer.parseInt(nds.get(2).contents());
			Integer y = Integer.parseInt(nds.get(3).contents());
			if (RECT.isInstance(ref.receiver))
				((GRect) ref.receiver).setDimension(new Dimension(x, y));
			else if (OVAL.isInstance(ref.receiver))
				((GOval) ref.receiver).setDimension(new Dimension(x, y));
			else if (SPACE.isInstance(ref.receiver))
				((GSpace) ref.receiver).changeWindowSize(new Dimension(x, y));
			return null;
		}
	}
	
	
	public static void main(String[] args) {
		new Exercice5();
	}

}