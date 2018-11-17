package graph;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import element.Magique;
import element.Monstre;
import element.Mur;
import element.Tresor;

public class Dessin extends JPanel implements KeyListener, Runnable {

	private ArrayList<Mur> lMur=new ArrayList<Mur>();
	protected element.Heros perso;
	private ArrayList<Monstre> lMonstre = new ArrayList<Monstre>();
	private Tresor arivee;
	private ArrayList<Magique> lMagique = new ArrayList<Magique>();
	private ArrayList<Magique> lMagiqueUsed = new ArrayList<Magique>();
	private long temps=0;

	@Override
	public void run(){
		System.out.println("Execution");
		while (Visuel.partieencours){
			if (arivee.pietinee(perso))
				Visuel.partieencours=false;
			for (Magique m:lMagique){
				if (m.pietinee(perso)){
					m.appeffect(perso);
					if (m.getEffect().equals("tuerUnMonstre")){
						lMonstre.remove(0);
					}
					lMagiqueUsed.add(m);
				}
			}
			if  (perso.peutAvancer( lMur )){
				perso.setX(perso.getX()+perso.getDirectionX()*perso.getFacteurdevitesse());
				perso.setY(perso.getY()+perso.getDirectionY()*perso.getFacteurdevitesse());
			}
			for (Magique m :lMagiqueUsed)lMagique.remove(m);
			
			ArrayList<Monstre> monstresupprim = new ArrayList<Monstre>();
			for (Monstre m:lMonstre){
				//ici on fera bouger les monstres patrouilleurs...s ils survivent
				if ((perso.getAttaqueX()!=0 || perso.getAttaqueY()!=0) && perso.monstredroite2points(m) < m.getRayon())m.perdPV(1);

				if(m.getPointdevie()<=0)monstresupprim.add(m);
				else{
					int newX=m.getX()+m.getDirectionX()*m.getFacteurdevitesse();
					int newY=m.getY()+m.getDirectionY()*m.getFacteurdevitesse();
					if (m.peutAvancer(lMur)
							&& Math.abs(newX-m.getPoint()[0])<=Math.abs(m.getDistance()[0])
							&& Math.abs(newY-m.getPoint()[1])<=Math.abs(m.getDistance()[1])){
						m.setX(newX);
						m.setY(newY);

					}
					else{
						m.setDirectionX(-1*m.getDirectionX());
						m.setDirectionY(-1*m.getDirectionY());
					}
				}

			}
			for (Monstre m:monstresupprim)lMonstre.remove(m);
			this.repaint();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Dessin(generation_lab.Lecture_lab lab){
		//On prend les info du lab, extraites elels meme du fichier auparavant
		setBackground(Color.WHITE);
		this.lMur = lab.getListMur();
		this.perso = lab.getHeros();
		this.lMonstre = lab.getListMonstre();

		this.lMagique=lab.getlMagique();
		this.arivee=lab.getArrivee();

	}

	public Dessin(ArrayList<Mur> lMur,	element.Heros perso){ //utile pour des tests,
		setFocusable(true);
		setBackground(Color.WHITE);
		this.lMur=lMur;
		this.perso=perso;
	}

	public Dessin(element.Heros perso){  //idem
		//sa c'est le dessin sans donnee
		setFocusable(true);
		setBackground(Color.WHITE);
		Mur m = new Mur(100,100,10,50);
		this.lMur.add(m);
		this.perso = perso;
	}

	public void paintComponent(Graphics g)  { //appele lors du repaint(), on dessine le panneau
		//System.out.println("repaint"); //vu la frequence , sa devient trop le flood sur la console

		super.paintComponent(g);
		
		if (!Visuel.partieencours) {
			g.setColor(Color.blue);
			if (perso.getPointdevie()!=0)
				g.drawString("VICTOIRE", 200, 200);

			else g.drawString("DEFAITE", 200, 200);
		}
		else{
			setBackground(Color.WHITE);
			
			long t = System.currentTimeMillis();
			

			g.setColor(Color.BLACK);
			if (t-temps<=100)g.drawLine(perso.getX(), perso.getY(), perso.getX()+perso.getAttaqueX()*perso.getPortee(), perso.getY()+perso.getAttaqueY()*perso.getPortee());
			for (Mur m : lMur){
				g.fillRect(m.getPosx(), m.getPosy(), m.getLongx(), m.getLongy());
			}
			g.setColor(Color.RED);
			for (Monstre monstre : lMonstre){
				g.fillOval(monstre.getX()-monstre.getRayon(), monstre.getY()-monstre.getRayon(), 2*monstre.getRayon(), 2*monstre.getRayon());;
			}
			g.setColor(Color.BLUE);
			g.fillOval(perso.getX()-perso.getRayon(), perso.getY()-perso.getRayon(), 2*perso.getRayon(), 2*perso.getRayon());

			g.setColor(Color.YELLOW);
			g.fillRect(arivee.getX(), arivee.getY(), arivee.getLongX(), arivee.getLongY());

			g.setColor(Color.GREEN);
			for(Magique m:lMagique)
				g.fillRect(m.getX(), m.getY(), m.getLongX(), m.getLongY());
		}
	}

	@Override
	public void keyPressed(KeyEvent e) { //lorsque une touche est presse on ...
		// TODO Auto-generated method stub
		System.out.println("clefpress");
		//...selon les touches appuyes, on indique une direction;

		//Avantage majeur : permet deplacement diagonal
		//et en + , plus pratique pour eventuel deplacement tout les 10 ms ( pas reussi pour l'instant)
		if(e.getKeyCode()==KeyEvent.VK_Q)
			perso.setDirectionX(-1);

		if(e.getKeyCode()==KeyEvent.VK_D)
			perso.setDirectionX(1);

		if(e.getKeyCode()==KeyEvent.VK_S)
			perso.setDirectionY(1);

		if(e.getKeyCode()==KeyEvent.VK_Z)
			perso.setDirectionY(-1);

		if (e.getKeyCode()==KeyEvent.VK_LEFT){perso.setAttaqueX(-1);temps=System.currentTimeMillis();}
		if(e.getKeyCode()==KeyEvent.VK_RIGHT){perso.setAttaqueX(1);temps=System.currentTimeMillis();}
		if(e.getKeyCode()==KeyEvent.VK_DOWN){perso.setAttaqueY(1);temps=System.currentTimeMillis();}
		if(e.getKeyCode()==KeyEvent.VK_UP){perso.setAttaqueY(-1);temps=System.currentTimeMillis();}
	}

	@Override
	public void keyReleased(KeyEvent e) { // lorsque on relache la touche , faut pas continuer a avancer
		// TODO Auto-generated method stub
		System.out.println("clefrelach");
		if(e.getKeyCode()==KeyEvent.VK_Q || e.getKeyCode()==KeyEvent.VK_D)
			perso.setDirectionX(0);

		if(e.getKeyCode()==KeyEvent.VK_S || e.getKeyCode()==KeyEvent.VK_Z)
			perso.setDirectionY(0);

		if (e.getKeyCode()==KeyEvent.VK_LEFT || e.getKeyCode()==KeyEvent.VK_RIGHT)
			perso.setAttaqueX(0);

		if(e.getKeyCode()==KeyEvent.VK_DOWN || e.getKeyCode()==KeyEvent.VK_UP)
			perso.setAttaqueY(0);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("cleftap");
		
		

	}

}
