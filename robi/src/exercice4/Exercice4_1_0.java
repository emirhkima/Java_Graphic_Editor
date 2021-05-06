package exercice4;

import java.awt.Color;

// 
//	(space setColor black)  
//	(robi setColor yellow) 
//	(space sleep 2000) 
//	(space setColor white)  
//	(space sleep 1000) 	
//	(robi setColor red)		  
//	(space sleep 1000)
//	(robi translate 100 0)
//	(space sleep 1000)
//	(robi translate 0 50)
//	(space sleep 1000)
//	(robi translate -100 0)
//	(space sleep 1000)
//	(robi translate 0 -40) ) 
//

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import graphicLayer.GRect;
import graphicLayer.GSpace;
import stree.parser.SNode;
import stree.parser.SParser;
import tools.Tools;

public class Exercice4_1_0 {
	// Une seule variable d'instance
	Environment environment = new Environment();

	public Exercice4_1_0() {
		// space et robi sont temporaires ici
		
		GSpace space = new GSpace("Exercice 4", new Dimension(200, 100));
		GRect robi = new GRect();

		space.addElement(robi);
		space.open();

		Reference spaceRef = new Reference(space);
		Reference robiRef = new Reference(robi);

		// Initialisation des references : on leur ajoute les primitives qu'elles comprenent
		// TODO <A VOUS DE CODER>
			spaceRef.addCommand("setColor", new SetColor());
			spaceRef.addCommand("sleep", new Sleep());
			robiRef.addCommand("setColor", new SetColor());
			robiRef.addCommand("translate", new Translate());
		//

		// Enrigestrement des references dans l'environement par leur nom
		environment.addReference("space", spaceRef);
		environment.addReference("robi", robiRef);

		this.mainLoop();
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
		
		public void run(SNode nds) {
			String cmd_str = nds.get(1).contents(); 
			System.out.println(cmd_str);
			Command cmd = primitives.get(cmd_str);
			System.out.println(cmd);
			cmd.run(this ,nds);
		}
		
		public void addCommand(String name, Command Cmd) {
			primitives.put(name,Cmd); 
		}
	}
	
	public class Environment {
		HashMap<String, Reference> variables;
		public Environment() {
			variables = new HashMap<String, Reference>();
		} // TODO ...
		public void addReference(String name, Reference Ref) {
			variables.put(name,Ref); 
		}
		
		public Reference getReferenceByName (String Ref) {
					return variables.get(Ref);
		}
		public void run() {
			//TODO 
			
		}
	}
	
	private void mainLoop() {
		while (true) {
			// prompt
			System.out.print("> ");
			// lecture d'une serie de s-expressions au clavier (return = fin de la serie)
			String input = Tools.readKeyboard();
			// creation du parser
			SParser<SNode> parser = new SParser<>();
			// compilation
			List<SNode> compiled = null;
			try {
				compiled = parser.parse(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// execution des s-expressions compilees
			Iterator<SNode> itor = compiled.iterator();
			while (itor.hasNext()) {
				this.run((SNode) itor.next());
			}
		}
	}

	private void run(SNode expr) {
		// quel est le nom du receiver
		String receiverName = expr.get(0).contents();
		// quel est le receiver
		Reference receiver = environment.getReferenceByName(receiverName);
		// demande au receiver d'executer la s-expression compilee
		receiver.run(expr);
	}
	
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
	/* Commandes pour SPACE */
	/* Change la couleur */
	public class SetColor implements Command {
		Color newColor;
		@SuppressWarnings("rawtypes")
		Class SPACE = GSpace.class;
		@SuppressWarnings("rawtypes")
		Class RECT = GRect.class;
		@Override
		public Reference run(Reference ref, SNode nds) {
			Color color = toColor(nds.get(2).contents());
			if (SPACE.isInstance(ref.receiver))
				((GSpace) ref.receiver).setColor(color);
			else if (RECT.isInstance(ref.receiver))
				((GRect) ref.receiver).setColor(color);
			
			
			return ref;
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
			return ref;
		}
	}
	
	
	/* Commandes pour ROBI */
	public class Translate implements Command {
		
		@Override
		public Reference run(Reference ref, SNode nds) {
			Integer x = Integer.parseInt(nds.get(2).contents());
			Integer y = Integer.parseInt(nds.get(2).contents());
			Point gap = new Point(0,0);
			gap.translate(x,y);
			((GRect) ref.receiver).translate(gap);
			return ref;
		}
	}
	
	public static void main(String[] args) {
		new Exercice4_1_0();
	}
	
	

}