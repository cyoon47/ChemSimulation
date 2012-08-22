package particlecollisionstest;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class QuadTree<T extends QuadTree.QuadTreeObject> {

	private boolean debugInsert = false;
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

	private void logInsert (String message) {
		if (debugInsert) {
			System.out.println ("[Insert] " + message);
		}
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
		//Not in tree yet
		if (node == null) {
			object.setNode (treeHead);
			node = object.getNode ();
		}

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

			//Nearest node is deepest node, trim unused leafs
			if (!childrenHasAnyObjects (startNode)) {
				startNode.childNodes = null;
			}

			if (testingNode != null) {
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
	
	private void deleteNode (QuadTreeNode node) {
		if (node != null && node.containedObject == null) {
			if (!childrenHasAnyObjects (node)) {
				node.childNodes = null;
				//node.hasObjectsWithin = false;
				deleteNode (node.parentNode);
			}
		}
	}

	public void delete (T object) {
		QuadTreeNode node = object.getNode ();
		//Remove from tree
		node.containedObject = null;
		if (node.childNodes == null) {
			node.hasObjectsWithin = false;
		}
		
		deleteNode (node);
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
			
			delete (object);

			if (insert (object, node)) {
				logUpdate (object + " no reallocation needed, putting at deepest");
			}
			else {
				logUpdate (object + " reallocating...");

				//Find nearest parent where it fits
				do {
					node = node.parentNode;
				}
				while (node != null && !object.completelyInBoundry (node));
				
				//Insert from smallest node that it fits in
				if (node != null) {
					logUpdate ("Found possible node for " + object);
					insertToDeepestEmptyNode (object, node);
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

		//logUpdate (this.toString ());
	}
}
