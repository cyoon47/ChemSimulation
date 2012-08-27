package particlecollisionstest;

public class Vector2D {
	public float dx;
	public float dy;
	private boolean calcMagnitude;
	private float magnitude;
	private boolean calcAngle;
	private float angle;
	
	public Vector2D (float dx, float dy) {
		this.dx = dx;
		this.dy = dy;
		this.calcMagnitude = true;
		this.calcAngle = true;
	}
	
	public void add (Vector2D o) {
		this.dx += o.dx;
		this.dy += o.dy;
	}
	
	public void subtract (Vector2D o) {
		this.dx -= o.dx;
		this.dy -= o.dy;
	}
	
	public float getMagnitude () {
		if (this.calcMagnitude) {
			this.magnitude = (float) Math.sqrt (this.dx * this.dx + this.dy * this.dy);
			calcMagnitude = false;
		}
		
		return this.magnitude;
	}
	
	public float getAngle () {
		if (this.calcAngle) {
			this.angle = (float) Math.atan2 (dy, dx);
			this.calcAngle = false;
		}
		
		return this.angle;
	}
}

