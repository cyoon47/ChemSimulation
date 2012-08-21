package particlecollisionstest;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class QuadTree<T extends QuadTree.QuadTreeObject> {

	private boolean debugInsert = false;
	private float w;
	private float h;
	private QuadTreeNode treeHead;

	public static abstract class QuadTreeObject {

		private QuadTreeNode associatedLeaf;

		public QuadTreeObject () {
		}

		public abstract float getX ();

		public abstract float getY ();

		public abstract boolean completelyInBoundry (float x, float y, float w, float h);

		public void setNode (QuadTreeNode node) {
			associatedLeaf = node;
		}

		public QuadTreeNode getNode () {
			if (associatedLeaf == null) {
				System.out.println ("Not in tree!");
			}
			return associatedLeaf;
		}

		public void updateAssociatedLeaf () {
		}
	}

	public QuadTree (float w, float h) {
		this.w = w;
		this.h = h;
		createTree ();
	}

	public final void createTree () {
		treeHead = new QuadTreeNode<> (0f, 0f, w, h, null);
	}

	@Override
	public String toString () {
		return treeHead.toString ();
	}

	private void subdivideNode (QuadTreeNode node) {
		node.childNodes = new QuadTreeNode[4];
		float newW = node.getW () / 2;
		float newH = node.getH () / 2;

		node.childNodes[0] = new QuadTreeNode (node.getX (), node.getY (), newW, newH, node);
		node.childNodes[1] = new QuadTreeNode (node.getX () + newW, node.getY (), newW, newH, node);
		node.childNodes[2] = new QuadTreeNode (node.getX (), node.getY () + newH, newW, newH, node);
		node.childNodes[3] = new QuadTreeNode (node.getX () + newW, node.getY () + newH, newW, newH, node);
	}

	public ArrayList<Shape> getLines () {
		return getLines (treeHead);
	}
	
	public ArrayList<Shape> getLines (QuadTreeNode node) {
		ArrayList<Shape> lines = new ArrayList<> ();

		if (node.childNodes != null) {
			lines.add (new Line2D.Float (node.getX (), (2 * node.getY () + node.getH ()) / 2, node.getX () + node.getW (), (2 * node.getY () + node.getH ()) / 2));
			lines.add (new Line2D.Float ((2 * node.getX () + node.getW ()) / 2, node.getY (), (2 * node.getX () + node.getW ()) / 2, node.getY () + node.getH ()));
			
			for (int i = 0; i < node.childNodes.length; i++) {
				lines.addAll (getLines (node.childNodes[i]));
			}

		}
		
		return lines;
	}

	public void insert (T object) {
		insert (object, treeHead);
	}

	private void logInsert (String message) {
		if (debugInsert) {
			System.out.println ("[Insert] " + message);
		}
	}

	private boolean insert (T object, QuadTreeNode node) {

		if (object.completelyInBoundry (node.getX (), node.getY (), node.getW (), node.getH ())) {
			logInsert (object + " Insert Attempt");
			if (!node.hasObjectsWithin) {
				node.setContainedObject (object);

				//Successful insert
				node.hasObjectsWithin = true;
				return true;
			}
			else {
				logInsert ("Objects present in branch");

				//Subdivided?
				if (node.childNodes == null) {
					subdivideNode (node);
					logInsert ("Subdividing");
				}

				//Handle affected object at node (if any)
				if (node.containedObject != null) {
					QuadTreeObject affectedObject = node.removeContainedObject ();
					logInsert ("Reallocating affected object " + affectedObject + " into children");
					if (!insert ((T) affectedObject, node)) {
						logInsert ("Failed to reallocate " + affectedObject + " into children of " + node);
					}
				}

				//Insert object into a child (if possible)
				boolean reallocated = false;
				for (int i = 0; i < node.childNodes.length; i++) {
					logInsert ("Inserting " + object + " into child " + i);
					reallocated = reallocated || insert (object, node.childNodes[i]);
					if (reallocated) {
						logInsert ("Successful insertion of object " + object + " to child " + i);
						break;
					}
					else {
						logInsert ("Failed insertion of object " + object + " to child " + i);
					}
				}

				//Not possible to put object into a child
				if (!reallocated) {
					logInsert ("Object " + object + " cannot fit into smaller quads, put back at original node");
					node.setContainedObject (object);

					//Affected object handled
					return true;
				}

				//Everything handled
				return true;
			}
		}

		//Not in bounds
		return false;
	}
	
	public void deleteBranch (QuadTreeNode node) {
		if (node.childNodes != null) {
			for (int i = 0; i < node.childNodes.length; i++) {
				if (node.childNodes[i] != null) {
					deleteBranch (node.childNodes[i]);
					node.childNodes[i] = null;
				}
			}
			node.childNodes = null;
		}
	}

	public void delete (T object) {
		QuadTreeNode node = object.getNode ();
		
		//Not in tree
		if (node == null) {
			return;
		}
		
		node.containedObject = null;

		//Check if all children are empty
		if (node.childNodes != null) {
			
			boolean empty = false;
			for (QuadTreeNode child : node.childNodes) {
				empty = empty && !child.hasObjectsWithin;
			}
			
			if (empty) {
				
				node.hasObjectsWithin = false;
				deleteBranch (node);
			}
		}
		else {
			node.hasObjectsWithin = false;
		}
	}

	public void update (T object) {
		//System.out.println ("Before delete: " + this);
		delete (object);
		//System.out.println ("After delete: " + this);
		insert (object);
		System.out.println ("After update: " + this);
	}
}
