package particlecollisionstest;

import java.util.Random;
import javax.swing.*;

public class TestFrame {
	private JFrame frame;
	private CanvasPanel canvas;
	
	public TestFrame () {
		frame = new JFrame ("Collision");
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setVisible (true);
		
		canvas = new CanvasPanel (1000, 1000, 60);
		frame.add (canvas);
		frame.pack ();
		
        canvas.addParticle (new Particle (90, 150, 4, 0, 10));
		canvas.addParticle (new Particle (20, 150, 2, 0, 20));
		canvas.addParticle (new Particle (200, 150, -2, 0, 40));
		canvas.addParticle (new Particle (380, 150, 4, 0, 20));
		canvas.addParticle (new Particle (20, 20, 5, 1, 20));
		
        Random rand = new Random ();
        for(int i = 0; i < 1000; i++){
			canvas.addParticle (new Particle (rand.nextInt (998) + 1, rand.nextInt (998) + 1, rand.nextInt (5) - 2, rand.nextInt (5) - 2, rand.nextInt (7) + 1));
		}


		canvas.startAnimations ();
	}
}

