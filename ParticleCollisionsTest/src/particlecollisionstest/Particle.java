package particlecollisionstest;

public class Particle extends QuadTree.QuadTreeObject {
	private float x;
	private float y;
	private Vector2D v;
	private Vector2D vBias;
	private boolean hasBias;
	private float r;
	
	public Particle (float x, float y, float dx, float dy, float r) {
		this.setPosition (x, y);
		this.setVelocity (dx, dy);
		this.setBias (new Vector2D (0, 0));
		this.hasBias = false;
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
	
	public final void setBias (Vector2D vBias) {
		this.hasBias = true;
		this.vBias = vBias;
	}
	
	public Vector2D getBias () {
		this.hasBias = false;
		return this.vBias;
	}
	
	public boolean hasBias () {
		return this.hasBias;
}
	
	@Override
	public String toString () {
		return "(x, y) = (" + getX() + ", " + getY() + ") r = " + getR();
	}
	
	@Override
	public boolean completelyInBoundry (QuadTreeNode node) {
		if ((getX() - getR() >= node.getX () && getX() + getR() <= node.getX () + node.getW ()) &&
			(getY() - getR() >= node.getY () && getY() + getR() <= node.getY () + node.getH ())) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public QuadTree.QuadTreeQuery getQuery () {
		return (new QuadTree.QuadTreeQuery (
			x + (v.dx < 0 ? v.dx : 0) - r, 
			y + (v.dy < 0 ? v.dy : 0) - r, 
			2 * r + (v.dx < 0 ? -v.dx : v.dx), 
			2 * r + (v.dy < 0 ? -v.dy : v.dy)));
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
	
	@Override
	public float getX () {return x;}
	
	@Override
	public float getY () {return y;}
	
	public float getR () {return r;}
}

