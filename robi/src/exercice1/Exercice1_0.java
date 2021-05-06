package exercice1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

//import graphicLayer.GBounded;
import graphicLayer.GRect;
import graphicLayer.GSpace;

public class Exercice1_0 {
	GSpace space = new GSpace("Exercice 1", new Dimension(200, 150));
	GRect robi = new GRect();

	public Exercice1_0() {
		space.addElement(robi);
		space.open();
		Point gap = new Point(0,0);
		//bord droit
		while (true) {
			robi.setColor(new Color((int) (Math.random() * 0x1000000)));
			for (int i = 0; i < space.getWidth()-20 ; i++) {
				gap = new Point(0,0);
				gap.translate(1,0);
				robi.translate(gap);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//bord bas
			robi.setColor(new Color((int) (Math.random() * 0x1000000)));
			for (int i = 0; i < space.getHeight()-20 ; i++) {
				gap = new Point(0,0);
				gap.translate(0,1);
				robi.translate(gap);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//bord gauche
			robi.setColor(new Color((int) (Math.random() * 0x1000000)));
			for (int i = 0; i < space.getWidth()-20 ; i++) {
				gap = new Point(0,0);
				gap.translate(-1,0);
				robi.translate(gap);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//bord haut
			robi.setColor(new Color((int) (Math.random() * 0x1000000)));
			for (int i = 0; i < space.getHeight()-20 ; i++) {
				gap = new Point(0,0);
				gap.translate(0,-1);
				robi.translate(gap);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		new Exercice1_0();
	}

}