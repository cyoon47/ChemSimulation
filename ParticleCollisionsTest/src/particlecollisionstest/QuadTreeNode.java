package particlecollisionstest;

public class QuadTreeNode <T extends QuadTree.QuadTreeObject> {
	private float x;
	private float y;
	private float w;
	private float h;
	
	public QuadTreeNode parentNode;
	public QuadTreeNode[] childNodes;
	public T containedObject; 
	
	public QuadTreeNode (float x, float y, float w, float h, QuadTreeNode parentNode) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.parentNode = parentNode;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getW() {
		return w;
	}
	
	public float getH() {
		return h;
	}
}

