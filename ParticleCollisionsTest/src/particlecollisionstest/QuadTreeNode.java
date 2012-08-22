package particlecollisionstest;

import java.util.ArrayList;

public class QuadTreeNode<T extends QuadTree.QuadTreeObject> {

	private float x;
	private float y;
	private float w;
	private float h;
	public boolean hasObjectsWithin;
	public QuadTreeNode parentNode;
	public QuadTreeNode[] childNodes;
	public T containedObject;

	public QuadTreeNode (float x, float y, float w, float h, QuadTreeNode parentNode) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.hasObjectsWithin = false;
		this.parentNode = parentNode;
	}
	
	@Override
	public String toString () {
		return this.toString ("");
	}
	
	public String toString (String prefix) {
		String buffer = prefix + "|->[hasObjectsWithin: " + hasObjectsWithin + " Object: " + (containedObject == null ? "NULL" : containedObject.toString ()) + "]\n";
		if (this.childNodes != null) {
			for (QuadTreeNode child : this.childNodes) {
				if (child != null) {
					buffer += prefix + "|\t" + child.toString (prefix + "|\t");
				}
			}
		}
		
		return buffer;
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	public float getW () {
		return w;
	}

	public float getH () {
		return h;
	}

	public void setContainedObject (T object) {
		containedObject = object;
		containedObject.setNode (this);
	}

	public T removeContainedObject () {
		T object = containedObject;
		containedObject = null;
		return object;
	}

	public ArrayList<T> getAllObjectsUnderNode () {
		ArrayList<T> objects = new ArrayList<> ();

		if (this.containedObject != null) {
			objects.add ((T) this.containedObject);
		}
		
		if (this.childNodes != null) {
			for (QuadTreeNode child : this.childNodes) {
				for (QuadTree.QuadTreeObject o : (ArrayList<T>) child.getAllObjectsUnderNode ()) {
					objects.add ((T) o);
				}
			}
		}

		return objects;
	}
}
