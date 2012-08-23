package particlecollisionstest;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class QuadTree<T extends QuadTree.QuadTreeObject> {

	private boolean debugInsert = false;
	private boolean debugDelete = false;
	private boolean debugUpdate = false;
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

	private void logInsert (String message) {
		if (debugInsert) {
			System.out.println ("[Insert] " + message);
		}
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

	private boolean childrenHasAnyObjects (QuadTreeNode node) {
		if (node.childNodes != null) {
			return node.childNodes[0].hasObjectsWithin || node.childNodes[1].hasObjectsWithin || node.childNodes[2].hasObjectsWithin || node.childNodes[3].hasObjectsWithin;
		}
		return false;
	}

	/*Inserts object into a child
	 * return true when successfull insertion to child
	 * return false when cant insert into any child
	 */
	private boolean insertToAChild (T object, QuadTreeNode node) {
		if (node.childNodes == null) {
			subdivideNode (node);
			logInsert ("Subdividing");
		}

		for (int i = 0; i < node.childNodes.length; i++) {
			logInsert ("Inserting " + object + " into child " + i);
			if (insert (object, node.childNodes[i])) {
				logInsert ("Successful insertion of object " + object + " to child " + i);
				return true;
			}
			else {
				logInsert ("Failed insertion of object " + object + " to child " + i);
			}
		}

		return false;
	}

	/*Main insertion function (object being as deep as possible)
	 * Used to combine all other insertion functions recursively
	 * Flow:
	 * 1)Insert object as deep as possible, regardless of existing objects
	 * 2)Traverse up from deepest spot till an empty space is available
	 */
	private void insertToDeepestEmptyNode (T object, QuadTreeNode startNode) {
		//Find deepest node
		if (!insertToAChild (object, startNode)) {
			logInsert ("Object " + object + " cannot fit into smaller quads, put back at parent node");
			QuadTreeNode testingNode = startNode;

			//Find nearest empty node
			while (testingNode != null && testingNode.containedObject != null) {
				logInsert ("Traversing up tree for empty spot");
				testingNode = testingNode.parentNode;
			}

			if (testingNode != null) {
				//trim unused leafs
				if (!childrenHasAnyObjects (testingNode)) {
					testingNode.childNodes = null;
				}

				testingNode.setContainedObject (object);
			}
		}

		startNode.hasObjectsWithin = true;
	}

	/*Starts insertion from a node, checking for bounds
	 * returns true on successful insert
	 * returns false when out of bounds
	 */
	private boolean insert (T object, QuadTreeNode node) {
		logInsert ("Inserting " + object);
		if (object.completelyInBoundry (node)) {
			logInsert (object + " in bounds");

			insertToDeepestEmptyNode (object, node);
			return true;
		}
		else {
			logInsert (object + " not in bounds");

			//Not in bounds
			return false;
		}
	}

	/*
	 * Insertion from top of tree
	 */
	public void insert (T object) {
		insert (object, treeHead);
	}

	private void logDelete (String message) {
		if (debugDelete) {
			System.out.println ("[Delete] " + message);
		}
	}

	/*
	 * Preserves the tree so a deeper search can be carried out from the current node
	 */
	public void deleteDown (T object) {
		logDelete ("Before delete: " + this.toString ());
		QuadTreeNode node = object.getNode ();

		//Remove from tree
		node.containedObject = null;

		//Only delete down
		if (!childrenHasAnyObjects (node)) {
			logDelete ("No objects below");
			node.hasObjectsWithin = false;
			node.childNodes = null;
		}
		else {
			logDelete ("Possibly still have objects below");
		}
	}

	/*
	 * Cleans up and collapses nodes that are no longer useful after a deleteDown (during reallocation)
	 */
	public QuadTreeNode deleteUp (T object) {
		QuadTreeNode node = object.getNode ();

		while (node.parentNode != null && !childrenHasAnyObjects (node.parentNode)) {
			logDelete ("Collapsed to parent node");
			node = node.parentNode;
			node.hasObjectsWithin = false;
			node.childNodes = null;
		}
		
		return node;
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

			//Only delete down so insert can use existing subdivisions to check more efficiently
			deleteDown (object);
			logUpdate ("Before insert: " + this.toString ());
			if (insert (object, node)) {
				logUpdate (object + " no reallocation needed, putting at deepest");
			}
			else {
				//Clean up unused subdivisions, then update node with next available one
				node = deleteUp (object);
				logUpdate ("After failed insert: " + this.toString ());
				logUpdate (object + " reallocating...");

				//Find nearest parent where it fits, then fit to deepest
				do {
					node = node.parentNode;
				}
				while (node != null && !insert (object, node));

				//Insert from smallest node that it fits in
				if (node != null) {
					logUpdate ("Found node for " + object);
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
			//Most probably particles stuck at boundry or "oh no"
			logUpdate ("Inserting " + object + " from top of tree");
			insert (object);
		}

		logUpdate ("After update: " + this.toString ());
	}
}
