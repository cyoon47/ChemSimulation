package particlecollisionstest;

public class Vector2D {
	public float dx;
	public float dy;
	public float magnitude;
	public float angle;
	
	public Vector2D (float dx, float dy) {
		this.dx = dx;
		this.dy = dy;
		this.magnitude = (float) Math.sqrt (this.dx * this.dx + this.dy * this.dy);
		this.angle = (float) Math.atan2 (dy, dx);
	}
}

