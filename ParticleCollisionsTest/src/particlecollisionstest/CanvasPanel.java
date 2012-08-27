package particlecollisionstest;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.Timer;

public class CanvasPanel extends JPanel {

	private boolean debugEnergy = false;
	private float width;
	private float height;
	private BufferStrategy bs;
	private Timer animator;
	private ArrayList<Particle> particles;
	private QuadTree qTree;

	public CanvasPanel (int width, int height, BufferStrategy bs, int fps) {
		this.width = (float) width;
		this.height = (float) height;
		this.setPreferredSize (new Dimension (width, height));
		this.bs = bs;
		
		qTree = new QuadTree (this.width, this.height);
		particles = new ArrayList<> ();
		animator = new Timer (1000 / fps, new ActionListener () {
			@Override
			public void actionPerformed (ActionEvent e) {
				for (Particle p : particles) {
					updateParticle (p);
				}
				repaint ();
				//draw ();
			}
		});
	}

	public void startAnimations () {
		animator.start ();
	}

	public void stopAnimations () {
		animator.stop ();
	}
	
	public void addParticle (Particle p) {
		particles.add (p);
		qTree.insert (p);
	}

	private float CalculateSystemEnergy () {
		float ke = 0;
		for (Particle tmp : particles) {
			float m = tmp.getR () * tmp.getR ();
			ke += 0.5 * m * tmp.getVelocity ().getMagnitude () * tmp.getVelocity ().getMagnitude ();
		}
		return ke;
	}

	private void logEnergyDebug (String msg) {
		if (debugEnergy) {
			System.out.println ("[Energy]" + msg + " = " + CalculateSystemEnergy ());
		}
	}
	
	private void updateNextPosition (Particle p) {
		p.setPosition (p.getNewX (), p.getNewY ());
	}
	
	private void reversePosition (Particle p) {
		p.setPosition (p.getX () - p.getVelocity ().dx, p.getY () - p.getVelocity ().dy);
	}
	
	private void updateBoundryCheck (Particle p) {
		if (p.getX () - p.getR () < 0 || p.getX () + p.getR () > width) {
			reversePosition (p);
			p.setVelocity (-p.getVelocity ().dx, p.getVelocity ().dy);
			updateNextPosition (p);
		}
		
		if (p.getY () - p.getR () < 0 || p.getY () + p.getR () > height) {
			reversePosition (p);
			p.setVelocity (p.getVelocity ().dx, -p.getVelocity ().dy);
			updateNextPosition (p);
		}
	}
	
	private void updateCollisionCheck (Particle p) {
		//Collision culling here
		ArrayList <Particle> possibleColliders = qTree.getObjectsWithinBound (p);
		//System.out.println ("Reduced collision checks to " +  ((double) possibleColliders.size () / (double) (particles.size () - 1) * 100) + "%");

		//Collision
		for (Particle o : possibleColliders) {
			if (p != o) {
				if (p.collidesWith (o)) {
					logEnergyDebug ("Initial");
					reversePosition (p);
					reversePosition (o);

					float pMass = p.getR () * p.getR ();
					float oMass = o.getR () * o.getR ();
					float po_dx = p.getX () - o.getX ();
					float po_dy = p.getY () - o.getY ();
					float sinPhi, cosPhi, distance;

					//Get trigo angle shifts needed for collision to be 1D
					distance = (float) Math.sqrt (po_dx * po_dx + po_dy * po_dy);
					sinPhi = po_dy / distance;
					cosPhi = po_dx / distance;
					//System.out.println ("phi = " + phi);

					//Transform velocities to rotated coordinate system
					Vector2D pVelRefInit = new Vector2D (
						(p.getVelocity ().dx * cosPhi + p.getVelocity ().dy * sinPhi),
						(p.getVelocity ().dx * -sinPhi + p.getVelocity ().dy * cosPhi));

					Vector2D oVelRefInit = new Vector2D (
						(o.getVelocity ().dx * cosPhi + o.getVelocity ().dy * sinPhi),
						(o.getVelocity ().dx * -sinPhi + o.getVelocity ().dy * cosPhi));

					//Calculate new velocities based on 1D collision
					Vector2D pVelRefFinal = new Vector2D (
						(pVelRefInit.dx * (pMass - oMass) + 2 * oMass * oVelRefInit.dx) / (pMass + oMass),
						pVelRefInit.dy);

					Vector2D oVelRefFinal = new Vector2D (
						(oVelRefInit.dx * (oMass - pMass) + 2 * pMass * pVelRefInit.dx) / (pMass + oMass),
						oVelRefInit.dy);

					//Transform velocities back to original system
					Vector2D pVelFinal = new Vector2D (
						(pVelRefFinal.dx * cosPhi - pVelRefFinal.dy * sinPhi),
						(pVelRefFinal.dx * sinPhi + pVelRefFinal.dy * cosPhi));

					Vector2D oVelFinal = new Vector2D (
						(oVelRefFinal.dx * cosPhi - oVelRefFinal.dy * sinPhi),
						(oVelRefFinal.dx * sinPhi + oVelRefFinal.dy * cosPhi));

					p.setVelocity (pVelFinal);
					o.setVelocity (oVelFinal);

					updateNextPosition (p);
					updateNextPosition (o);
					qTree.update (o);

					logEnergyDebug ("After Collision");

					/*if (p.collidesWith (o)) {
						System.out.println ("oh no");
					}*/
				}
			}
		}
	}

	private void updateParticle (Particle p) {
		updateNextPosition (p);
		updateBoundryCheck (p);
		qTree.update (p);
		updateCollisionCheck (p);
		//Done
		updateNextPosition (p);
		//System.out.println ("Objects in tree: " + qTree.treeHead.getAllObjectsUnderNode ().size () + " / " + particles.size ());
		//System.out.println (qTree);
	}
	
	private void draw () {
		Graphics2D g2 = (Graphics2D) bs.getDrawGraphics ();
		g2.clearRect (0, 0, getWidth(), getHeight ());
		
		/*ArrayList<Shape> lines = qTree.getLines ();
		for (int i = 0; i < lines.size (); i++) {
			g2.draw ((Shape) lines.get (i));
		}*/
		
		//Random rand = new Random ();
		if (particles.size () > 0) {
			for (int i = 0; i < particles.size (); i++) {
				Particle p = particles.get (i);
				//g2.setColor (new Color (rand.nextFloat (), rand.nextFloat (), rand.nextFloat ()));
				g2.fill (new Ellipse2D.Float (p.getX () - p.getR (), p.getY () - p.getR (), p.getR () * 2, p.getR () * 2));
			}
		}
		
		g2.dispose ();
		bs.show ();
	}

	@Override
	public void paintComponent (Graphics g) {
		super.paintComponent (g);
		Graphics2D g2 = (Graphics2D) g;
		
		/*ArrayList<Shape> lines = qTree.getLines ();
		for (int i = 0; i < lines.size (); i++) {
			g2.draw ((Shape) lines.get (i));
		}*/

		Random rand = new Random ();
		if (particles.size () > 0) {
			for (int i = 0; i < particles.size (); i++) {
				
				Particle p = particles.get (i);
				//QuadTree.QuadTreeQuery query = p.getQuery ();
				//g2.drawRect ((int) query.x, (int) query.y, (int) query.w, (int) query.h);
				//g2.setColor (new Color (rand.nextFloat (), rand.nextFloat (), rand.nextFloat ()));
				g2.fill (new Ellipse2D.Float (p.getX () - p.getR (), p.getY () - p.getR (), p.getR () * 2, p.getR () * 2));
			}
		}
	}
}
