package particlecollisionstest;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class QuadTree<T extends QuadTree.QuadTreeObject> {

	private boolean debugInsert = true;
	private boolean debugUpdate = true;
	private float w;
	private float h;
	private QuadTreeNode treeHead;

	public static abstract class QuadTreeObject {

		private QuadTreeNode associatedLeaf;

		public QuadTreeObject () {
		}

		public abstract float getX ();

		public abstract float getY ();

		public abstract boolean completelyInBoundry (QuadTreeNode node);

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

	private void handleAffectedObjectAt (QuadTreeNode node) {
		if (node.containedObject != null) {
			QuadTreeObject affectedObject = node.removeContainedObject ();
			logInsert ("Reallocating affected object " + affectedObject + " into children");
			if (!insert ((T) affectedObject, node)) {
				logInsert ("Failed to reallocate " + affectedObject + " into children of " + node);
			}
		}
	}

	private boolean insert (T object, QuadTreeNode node) {

		if (object.completelyInBoundry (node)) {
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
				boolean newlyDivided = node.childNodes == null;
				if (newlyDivided) {
					subdivideNode (node);
					logInsert ("Subdividing");
				}

				//Handle affected object at node (if any)
				handleAffectedObjectAt (node);

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
					if (newlyDivided) {
						//Cleanup unused subdivision
						node.childNodes = null;
					}
					
					logInsert ("Object " + object + " cannot fit into smaller quads, put back at original node");
					if (node.containedObject == null) {
						node.setContainedObject (object);
					}
					else {
						logInsert ("Conflict!");
						if (node != null && node.parentNode != null) {
							logInsert ("Parent node conatins object ? " + (node.parentNode.containedObject != null));
						}
						do {
							node = node.parentNode;

						}
						while (node != null && node.containedObject != null);

						if (node != null) {
							logInsert ("Conflict resolved!");
							node.setContainedObject (object);
						}
					}

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

	private void logUpdate (String message) {
		if (debugUpdate) {
			System.out.println ("[Update] " + message);
		}
	}

	public void update (T object) {
		logUpdate ("Updating " + object);
		QuadTreeNode node = object.getNode ();

		if (node != null) {
			logUpdate (object + " Exists in tree");

			//Remove from tree
			node.containedObject = null;
			if (node.childNodes != null) {
				boolean hasObjects = false;
				for (QuadTreeNode child : node.childNodes) {
					hasObjects = hasObjects || child.hasObjectsWithin;
				}

				if (!hasObjects) {
					node.childNodes = null;
					node.hasObjectsWithin = false;
				}
			}
			else {
				node.hasObjectsWithin = false;
			}

			if (insert(object, node)/*object.completelyInBoundry (node)*/) {
				logUpdate (object + " no update needed");
				node.containedObject = object;
				node.hasObjectsWithin = true;
			}
			else {
				logUpdate (object + " reallocating...");
				do {
					node = node.parentNode;
				}
				while (node != null && !object.completelyInBoundry (node));

				if (node != null) {//Insert from smallest node that it fits in
					logUpdate ("Found possible node for " + object);
					insert (object, node);
				}
				else {
					//Shouldnt reach here
					logUpdate ("No node found! Inserting from top again....");
					insert (object);
				}
			}
		}
		else {
			//Insert to tree
			logUpdate ("Inserting " + object + " from top of tree");
			insert (object);
		}

		System.out.println (this);
	}
}
