package particlecollisionstest;

public class Particle {
	private float x;
	private float y;
	private Vector2D v;
	private float r;
	
	public Particle (float x, float y, float dx, float dy, float r) {
		this.setPosition (x, y);
		this.setVelocity (dx, dy);
		this.r = r;
	}
	
	public boolean collidesWith (Particle p) {
		float deltaX = this.getX () - p.getX ();
		float deltaY = this.getY () - p.getY ();
		float minDist = this.getR () + p.getR ();
		float distance = deltaX * deltaX + deltaY * deltaY;
		
		if (distance <= (minDist * minDist)) {
			return true;
		}
		
		return false;
	}
	
	public final void setPosition (float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public final void setVelocity (float dx, float dy) {
		this.v = new Vector2D (dx, dy);
	}
	
	public final void setVelocity (Vector2D v) {
		this.v = v;
	}
	
	public Vector2D getVelocity () {
		return this.v;
	}
	
	public float getNewX () {
		return getX () + v.dx;
	}
	
	public float getNewY () {
		return getY () + v.dy;
	}
	
	public float getX () {return x;}
	
	public float getY () {return y;}
	
	public float getR () {return r;}
}

