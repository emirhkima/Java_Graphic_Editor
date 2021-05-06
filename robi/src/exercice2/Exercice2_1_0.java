package exercice2;

import java.awt.Color;

//import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import graphicLayer.GRect;
import graphicLayer.GSpace;
import stree.parser.SNode;
import stree.parser.SParser;


public class Exercice2_1_0 {
	GSpace space = new GSpace("Exercice 2_1", new Dimension(200, 100));
	GRect robi = new GRect();
	String script = "(space setColor black) (robi setColor yellow) (space color white) (robi color red) (robi translate 10 0) (space sleep 100) (robi translate 0 10)(space sleep 100) (robi translate -10 0) (space sleep 100) (robi translate 0 -10)";

	public Exercice2_1_0() {
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
		// A compléter...

		Point gap = new Point(0,0);
		
		System.out.print(expr.get(0).contents());
		System.out.print(" ");
		System.out.print(expr.get(1).contents());
		System.out.print(" ");
		System.out.print(expr.get(2).contents());
		System.out.print(" ");
		if (expr.size()==4) {
			System.out.print(expr.get(3).contents());
		}
		System.out.println();
		
		String elmt1 = new String(expr.get(0).contents());
		String elmt2 = new String(expr.get(1).contents());
		String elmt3 = new String(expr.get(2).contents());
		String elmt4 = new String();
		if (expr.size()==4) {
			elmt4 = expr.get(3).contents();
		}
		
		if (elmt1.equals("space")){
				if (elmt2.equals("setColor") || elmt2.equals("color")){
					if(elmt3.equals("black")){
						space.setColor(Color.black);
					}
					if(elmt3.equals("white")){
						space.setColor(Color.white);
					}	
				}
				if (elmt2.equals("sleep")){
					if (elmt3.equals("1000")){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
		}
		if (elmt1.equals("robi")){
			if (elmt2.equals("setColor")|| elmt2.equals("color")) {
				if (elmt3.equals("yellow")) {
					robi.setColor(Color.yellow);
				}
				if (elmt3.equals("red")) {
					robi.setColor(Color.red);
				}
			}
			if (elmt2.equals("translate")){
				Integer x = Integer.parseInt(elmt3);
				Integer y = Integer.parseInt(elmt4);
				System.out.println("");
				gap = new Point(0,0);
				gap.translate(x,y);
				robi.translate(gap);
			}
			
		}
	}

	public static void main(String[] args) {
		new Exercice2_1_0();
	}

}