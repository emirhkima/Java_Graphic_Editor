package exercice6;

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
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import graphicLayer.GContainer;
import graphicLayer.GElement;
import graphicLayer.GImage;
import graphicLayer.GOval;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import graphicLayer.GString;
import stree.parser.SNode;
import stree.parser.SParser;

public class Exercice6 {
	// Une seule variable d'instance
	Environment environment = new Environment();
	GSpace space = new GSpace("Exercice 6", new Dimension(200, 100));
	
	BufferedReader br;
	PrintStream ps;
	
	Message m;
	String json;
		
	public Exercice6() {
		// ouverture de la fenetre
		space.open();

		// création de réferences pour la fenetre et les différents types d'éléments qu"elle contient
		Reference spaceRef = new Reference(space);
		Reference rectClassRef = new Reference(GRect.class);
		Reference ovalClassRef = new Reference(GOval.class);
		Reference imageClassRef = new Reference(GImage.class);
		Reference stringClassRef = new Reference(GString.class);

		// Ajout de commande pour la fenêtre
		spaceRef.addCommand("setColor", new SetColor()); //change de couleur
		spaceRef.addCommand("sleep", new Sleep());  // fait une pause (dort)
		spaceRef.addCommand("setDim", new SetDim()); // change de taille

		spaceRef.addCommand("add", new AddElement()); // ajoute un element 
		spaceRef.addCommand("del", new DelElement()); // supprime un element
		spaceRef.addCommand("addScript", new AddScript()); // ajoute un nouveau script
		
		// Ajout de la commande de création d'un nouvel élement dans leurs références
		rectClassRef.addCommand("new", new NewElement()); // 
		ovalClassRef.addCommand("new", new NewElement());
		imageClassRef.addCommand("new", new NewImage());
		stringClassRef.addCommand("new", new NewString());

		// Ajout des réferences dans le dictionnaire d'un environnement
		environment.addReference("space", spaceRef);
		environment.addReference("Rect", rectClassRef);
		environment.addReference("Oval", ovalClassRef);
		environment.addReference("Image", imageClassRef);
		environment.addReference("Label", stringClassRef);
		
		//appel du mainLoop
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
				
				Thread.sleep(500);
				
				// On prend une capture d'ecran
				Robot robot = new Robot();
                BufferedImage image = robot.createScreenCapture(new Rectangle(8, 30, 200, 100));
                ImageIO.write(image, "jpg", new File("screen.jpg"));

                // Envoi d'image
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

                        //envoi de l'image au client
                        m = new Message("image", encodeBytes);
                        json = Message.toJson(m);
                        ps.println(json);
                        fis.close();
                    }
                }
             
				//envoi confirmation au client
				//creation d'un Message et conversion de l'objet java Message en String json
		    	m = new Message("text","ok");
		    	//json = (new ObjectMapper()).writeValueAsString(m);  //à utiliser si les librairies json fonctionnent
		    	json = Message.toJson(m);
		        ps.println(json);
			}
		} catch (Exception e) {}			
	}
	
	public class Interpreter {
		// la méthode compute interprete la réference d'une expression
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
		Map<String, SNode> script;
		
		// la class reference prend un objet en parametre et contient des dictionnaires
		public Reference(Object receiver) {
			this.receiver = receiver;
			primitives = new HashMap<String, Command>(); // pour les commandes
			script = new HashMap<String, SNode>(); // pour les noeuds des commandes créées par des scripts
		}
		
		public Reference run(SNode nds) {
			// recuperation du nom de la commande
			String cmd_str = nds.get(1).contents(); 
			// recherche de la commande 
			Command cmd = primitives.get(cmd_str);
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
		
		// methode pour ajouter une commande et son nom au dictionnaire de cette reference
		public void addCommand(String name, Command Cmd) {
			primitives.put(name,Cmd); 
		}
		// methode pour recuperer l'objet de la reference 
		public Object getReceiver() {
			return this.receiver;
		}
		
		// methode pour ajouter un noeud (un script) et son nom de commande dans le dictionnaire de script
		public void addNode(String name, SNode nds) {
			script.put(name, nds);
		}
		
		// methode pour recuperer un noeud(un script) d'une commande créées avec son nom (dans le dictionnaire script)
		public SNode getNodebyName(String name) {
			return script.get(name);
		}
		
	}
	
	public class Environment {
		HashMap<String, Reference> variables;

		// la class environnement contient un dictionnaire de reference
		public Environment() {
			variables = new HashMap<String, Reference>();
		} 
		
		// methode pour ajouter une reference et son nom au dictionnaire 
		public void addReference(String name, Reference Ref) {
			variables.put(name,Ref); 
		}
		
		// methode pour recuperer une reference dans le dictionnaire a partir de son nom
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
	/* Nouvel Element (Rect, Oval)*/
	class NewElement implements Command {
		public Reference run(Reference reference, SNode method) {
			try {
				@SuppressWarnings("unchecked")
				GElement e = ((Class<GElement>) reference.getReceiver()).getDeclaredConstructor().newInstance();
				Reference ref = new Reference(e);
				// ajout des commandes pour l'element créé
				ref.addCommand("setColor", new SetColor()); 
				ref.addCommand("translate", new Translate());
				ref.addCommand("setDim", new SetDim());
				ref.addCommand("add", new AddElement());
				ref.addCommand("del", new DelElement());
				ref.addCommand("addScript", new AddScript());
				// retourne sa reference
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
				// nouveau fichier a partir du nom de l'image
				File path = new File(method.get(2).contents());
				BufferedImage rawImage = null;
				try {
					rawImage = ImageIO.read(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// création de l'element de l'image 
				GImage e = new GImage(rawImage);
				// creation de sa reference
				Reference ref = new Reference(e);
				// ajout des commandes pour l'image
				ref.addCommand("translate", new Translate());
				return ref; // retourne sa reference
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
				// Recuperation du texte a afficher
				str = method.get(2).contents();
				
				// Boucle pour mettre le texte sans guillemet dans une variable temporaire
				for (int i=0 ; i<str.length()-2; i++) {
					tmp = tmp + str.charAt(i+1);
				}
				// création de l'element du texte
				GString e = new GString();
				e.setString(tmp);
				// creation de sa reference
				Reference ref = new Reference(e);
				// ajout des commandes pour l'image
				ref.addCommand("translate", new Translate());
				ref.addCommand("setColor", new SetColor());
				// retourne sa reference
				return ref;
			} catch (Throwable e){
				e.printStackTrace();
			}
			return null;
		}
	}
	
	/* ajoute un element (image, label, rectangle, oval) */
	class AddElement implements Command{
		public Reference run(Reference ref, SNode method) {
			// recuperation du noeud avec la commande du nouvel element
			SNode met=method.get(3);
			// recuperation de la reference du type d'element 
			Reference reference = environment.getReferenceByName(method.get(3).get(0).contents()).run(met);
			// ajout de la reference de l'element dans le dictionnaire de reference (de l'environnement)
			environment.addReference(method.get(0).contents()+"."+method.get(2).contents(), reference);
			// ajout de l'element dans son conteneur et actualisation du conteneur 
			((GContainer) ref.receiver).addElement((GElement) reference.getReceiver());
			((GContainer) ref.receiver).repaint();
			// si l'element est un label, le format du label est change
			if (method.get(3).get(0).contents().equals("Label") ) {
				((GString) reference.getReceiver()).setFont(new Font("Arial", Font.CENTER_BASELINE, 13));
			}
			return null;
		}
	}
	/* supprime element */
	class DelElement implements Command{
		public Reference run(Reference ref, SNode method) {
			// recuperation de la reference donnée avec son no
			Reference newref = environment.getReferenceByName(method.get(2).contents());
			// suppression de l'element (et ce qu'il contient) de son conteneur et actualisation du conteneur
			((GContainer) ref.receiver).removeElement((GElement) newref.getReceiver());
			((GContainer) ref.receiver).repaint();
			return null;
		}
	}
	
	/* creer une fonction a partir d'un script */
	class AddScript implements Command {
		public Reference run (Reference ref, SNode nds) {
			// ajout de la commande créée et son nom dans le dictionnaire de commande de la reference
			ref.addCommand(nds.get(2).contents(), new ExeScript());
			// ajout du noeud et du nom de la commande dans le dictionnaire de script de la reference
			ref.addNode(nds.get(2).contents(), nds);
			return null;
		}
	}
	/* execution d'une commande créée par un script */
	class ExeScript implements Command {
		@Override
		public Reference run(Reference ref, SNode method) {
			// recuperation du noeud (script qui defini la commande) avec le nom de la commande appelee
			SNode nds = ref.getNodebyName(method.get(1).contents());
			
			int largmet = method.size()-2; //nombre d'argument recu
			int largnds = nds.get(3).get(0).size()-1; // nombre d'argement demande
			// SI le nombre d'argument recu et demmande son les memes ALORS on continue SINON message d'erreur
			if (largmet==largnds) {
				// appel de la methode de parcours des noeuds pour remplacer self par le nom de l'objet de reference
				parcours (nds.get(3), method.get(0).contents(),nds.get(3).get(0).get(0).contents(), nds.get(0).contents());
				// boucle pour appeler la methode de parcours des noeuds pour remplacer chaque argument par sa valeur 
				for (int i=0; i<largmet;i++) {
					parcours (nds.get(3), method.get(i+2).contents(),nds.get(3).get(0).get(i+1).contents(), nds.get(0).contents());
				}
			}else {
				System.out.println("Nombre d'argument pour "+method.get(1).contents()+" est incorrect !");
				m = new Message("text","Nombre d'argument pour "+method.get(1).contents()+" est incorrect !");
		    	json = Message.toJson(m);
		        ps.println(json);
			}
			
			/* ON INTERPRETE */
			int lndsf = nds.get(3).size(); // nombre de noeud a interpreter
			// boucle poru interpreter chaque noeud de la commande du script
			for (int i=1; i<lndsf;i++) {
				new Interpreter().compute(environment, nds.get(3).get(i));
			}
			return null;
		}
		
		/* methode de parcours des noeuds pour remplacer chaque argument par sa valeur 
		   (prend en parametre le noeud a parcourir, la valeur de l'argument, le nom de l'argument et le conteneur)*/
		public void parcours (SNode nds, String arg, String arg_nom, String cont) {
			
			int x = nds.size(); // nombre de d'element dans le noeud
			// boucle pour parcourir chaque element du noeud
			for (int i = 0; i<x; i++) {
				// SI l'element est un noeud alors on le parcours 
				if (nds.get(i).hasChildren()) {
					parcours(nds.get(i), arg, arg_nom, cont);
					
				//SINON on verifie si ce n'est pas l'argument
				}else {
					int cpt=0;
					String[] str = new String[4];
					// separation d'une chaine qui contient un point en plusieurs token
					StringTokenizer token = new StringTokenizer(nds.get(i).contents(), ".");
					// SI l'element du noeud est un argument ALORS on le replace par sa valeur 
					if (nds.get(i).contents().equals(arg_nom)) {
						nds.get(i).setContents(arg);
					// SINON que la chaine ne contenait pas un point (avec le nombre de token)
					}else if (token.countTokens()>1) {
						// on met les tokens dans un tableau de string (déclaré plus haut)
						while(token.hasMoreTokens()) {
							str[cpt]= token.nextToken();
							cpt++;
						}
						// SI l'element vaut self.argnom ALORS on remplace self par le conteneur et l'argument par sa valeur
						if (str[1].equals(arg_nom)&&str[0].equals("self")) { 
							System.out.println(cont+"."+arg);
							String concat = new String(cont+"."+arg);
							nds.get(i).setContents(concat);
						}
					}
				}
			}
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
		/* la classe SetColor modifie la couleur des elements et/ou du space 
		   (avec la couleur recuperee dans le noeud) */
		public Reference run(Reference ref, SNode nds) {
			Color color = toColor(nds.get(2).contents());
			if (SPACE.isInstance(ref.receiver))
				((GSpace) ref.receiver).setColor(color);
			else if (RECT.isInstance(ref.receiver) || OVAL.isInstance(ref.receiver) ||STRING.isInstance(ref.receiver))
				((GElement) ref.receiver).setColor(color);
			return null;
		}
	}
	/* Fait une pause */
	public class Sleep implements Command {
		// la class Sleep arrete la fenetre pendant un temps (recupere dans le noeud)
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
		// la class Translate deplace les elements de x et y (recuperes dans le noeud)
		@Override
		public Reference run(Reference ref, SNode nds) {
			Integer x = Integer.parseInt(nds.get(2).contents());
			Integer y = Integer.parseInt(nds.get(3).contents());
			Point gap = new Point(0,0);
			gap.translate(x,y);
			((GElement) ref.receiver).translate(gap);
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
		/* la classe SetDim modifie la taille des elements et/ou du space 
		   (avec les x et y recuperes dans le noeud) */
		public Reference run(Reference ref, SNode nds) {
			Integer x = Integer.parseInt(nds.get(2).contents());
			Integer y = Integer.parseInt(nds.get(3).contents());
			if (RECT.isInstance(ref.receiver) || OVAL.isInstance(ref.receiver))
				((GRect) ref.receiver).setDimension(new Dimension(x, y));
			else if (OVAL.isInstance(ref.receiver))
				((GOval) ref.receiver).setDimension(new Dimension(x, y));
			else if (SPACE.isInstance(ref.receiver))
				((GSpace) ref.receiver).changeWindowSize(new Dimension(x, y));
			return null;
		}
	}
	
	
	public static void main(String[] args) {
		new Exercice6();
	}
}