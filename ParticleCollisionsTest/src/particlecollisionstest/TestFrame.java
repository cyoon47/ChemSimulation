package particlecollisionstest;

import java.util.Random;
import javax.swing.*;

public class TestFrame {
	private JFrame frame;
	private CanvasPanel canvas;
	
	public TestFrame () {
		int width = 1920;
		int height = 1080;
		frame = new JFrame ("Collision");
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setVisible (true);
		frame.createBufferStrategy (2);
		
		canvas = new CanvasPanel (width, height, frame.getBufferStrategy (), 60);
		frame.add (canvas);
		frame.pack ();
		
        /*canvas.addParticle (new Particle (90, 150, 1, 0, 10));
		canvas.addParticle (new Particle (20, 150, 2, 0, 20));
		canvas.addParticle (new Particle (200, 150, -2, 0, 40));
		canvas.addParticle (new Particle (380, 150, 4, 0, 20));
		canvas.addParticle (new Particle (20, 20, 5, 1, 20));*/
		
        Random rand = new Random ();
        for(int i = 0; i < 10000; i++){
			canvas.addParticle (new Particle (width * haltonSequence (i, 2), height * haltonSequence (i, 3), rand.nextInt (5) - 2, rand.nextInt (5) - 2, 2f));
		}

		canvas.startAnimations ();
	}
	
	private float haltonSequence (int index, int base) {
		float result = 0;
		float f = 1f / base;
		int i = index;
		
		while (i > 0) {
			result += f * (i % base);
			i = (int) Math.floor ((float) i / base);
			f = f / base;
		}
		
		return result;
	}
}

