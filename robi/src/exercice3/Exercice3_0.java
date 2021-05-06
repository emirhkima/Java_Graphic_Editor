package exercice3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import graphicLayer.GRect;
import graphicLayer.GSpace;
import stree.parser.SNode;
import stree.parser.SParser;

public class Exercice3_0 {
	GSpace space = new GSpace("Exercice 3", new Dimension(200, 100));
	GRect robi = new GRect();
	String script = "" +
	"   (space setColor black) " +
	"   (robi setColor yellow)" +
	"   (space sleep 1000)" +
	"   (space setColor white)\n" + 
	"   (space sleep 1000)" +
	"	(robi setColor red) \n" + 
	"   (space sleep 1000)" +
	"	(robi translate 100 0)\n" + 
	"	(space sleep 1000)\n" + 
	"	(robi translate 0 50)\n" + 
	"	(space sleep 1000)\n" + 
	"	(robi translate -100 0)\n" + 
	"	(space sleep 1000)\n" + 
	"	(robi translate 0 -40)";

	public Exercice3_0() {
		space.addElement(robi);
		space.open();
		this.runScript();
	}

	private void runScript() {
		SParser<SNode> parser = new SParser<>();
		List<SNode> rootNodes = null;
		try {
			rootNodes = parser.parse(script);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterator<SNode> itor = rootNodes.iterator();
		while (itor.hasNext()) {
			this.run(itor.next());
		}
	}

	private void run(SNode expr) {
		Command cmd = getCommandFromExpr(expr);
		if (cmd == null)
			throw new Error("unable to get command for: " + expr);
		cmd.run();
	}

	Command getCommandFromExpr(SNode expr) {
		Command cmd;
		
		if (expr.get(0).contents().equals("space")){ // pour SPACE
			if (expr.get(1).contents().equals("setColor")){
				if(expr.get(2).contents().equals("black")){
					Color couleur = Color.black; 
					cmd = new SpaceChangeColor(couleur);
					return cmd; 
				}
				if(expr.get(2).contents().equals("white")){
					Color couleur = Color.white; 
					cmd = new SpaceChangeColor(couleur);
					return cmd; 
				}	
			} else if (expr.get(1).contents().equals("sleep")){
					Integer time  = Integer.parseInt(expr.get(2).contents());
					cmd = new SpaceSleep(time);
					return cmd;
			}
			
		}else if (expr.get(0).contents().equals("robi")){ // pour ROBI
			if (expr.get(1).contents().equals("setColor")) {
				if (expr.get(2).contents().equals("yellow")) {
					Color couleur = Color.yellow; 
					cmd = new RobiChangeColor(couleur);
					return cmd; 
				} else if (expr.get(2).contents().equals("red")) {
					Color couleur = Color.red; 
					cmd = new RobiChangeColor(couleur);
					return cmd; 
				}
			} else if (expr.get(1).contents().equals("translate")){
				Integer x = Integer.parseInt(expr.get(2).contents());
				Integer y = Integer.parseInt(expr.get(3).contents());
				cmd = new RobiTranslate(x,y);
				return cmd;
			}
		}
		return null;
		
	}

	public static void main(String[] args) {
		new Exercice3_0();
	}

	public interface Command {
		abstract public void run();
	}

	/* Commandes pour SPACE */
	/* Change la couleur */
	public class SpaceChangeColor implements Command {
		Color newColor;

		public SpaceChangeColor(Color newColor) {
			this.newColor = newColor;
		}

		@Override
		public void run() {
			space.setColor(newColor);
		}
	}
	/* Fait une pause */
	public class SpaceSleep implements Command {
		int time;

		public SpaceSleep(int time) {
			this.time = time;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/* Commandes pour ROBI */
	public class RobiChangeColor implements Command {
		Color newColor;

		public RobiChangeColor(Color newColor) {
			this.newColor = newColor;
		}

		@Override
		public void run() {
			robi.setColor(newColor);
		}
	}
	public class RobiTranslate implements Command {
		int arg1;
		int arg2;

		public RobiTranslate(int arg1, int arg2) {
			this.arg1 = arg1;
			this.arg2 = arg2;
		}

		@Override
		public void run() {
			Point gap = new Point(0,0);
			gap.translate(arg1,arg2);
			robi.translate(gap);
		}
	}
}