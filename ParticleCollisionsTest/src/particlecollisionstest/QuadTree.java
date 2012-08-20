package particlecollisionstest;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class QuadTree<T extends QuadTree.QuadTreeObject> {

	private float w;
	private float h;
	private QuadTreeNode treeHead;
	public ArrayList <Shape> lines;
	
	public static abstract class QuadTreeObject {

		protected QuadTreeNode associatedLeaf;

		public QuadTreeObject () {
		}

		public QuadTreeNode getNode () {
			return associatedLeaf;
		}

		public abstract float getX ();

		public abstract float getY ();

		public abstract boolean completelyInBoundry (float x, float y, float w, float h);
	}

	public QuadTree (float w, float h) {
		this.w = w;
		this.h = h;
		lines = new ArrayList <> ();
	}

	public void createTree (ArrayList<T> objects) {
		treeHead = new QuadTreeNode<> (0f, 0f, w, h, null);
		lines.clear ();
		populateNode (treeHead, objects, 1);
	}
	
	public ArrayList <Shape> getGraphicalView () {
		return lines;
	}

	private void populateNode (QuadTreeNode node, ArrayList<T> objects, int maxN) {
		ArrayList<T> withinNode = new ArrayList<> ();

		for (T o : objects) {
			if (o.completelyInBoundry (node.getX (), node.getY (), node.getW (), node.getH ())) {
				withinNode.add (o);
			}
		}
		
		//System.out.println (withinNode.size ());

		if (!withinNode.isEmpty ()) {
			if (withinNode.size () <= maxN) {
				node.containedObject = withinNode.get (0);
			}
			else {
				//System.out.println ("Split again!");
				System.out.println (node.getY () / 2);
				lines.add (new Line2D.Float (node.getX (), (2 * node.getY () + node.getH ()) / 2, node.getX () + node.getW (), (2 * node.getY () + node.getH ()) / 2));
				lines.add (new Line2D.Float ((2 * node.getX () + node.getW ()) / 2, node.getY (), (2 * node.getX () + node.getW ()) / 2, node.getY () + node.getH ()));
				float newW = node.getW () / 2;
				float newH = node.getH () / 2;
				
				node.childNodes = new QuadTreeNode [4];
				node.childNodes[0] = new QuadTreeNode <> (node.getX (), node.getY (), newW, newH, node);
				node.childNodes[1] = new QuadTreeNode <> (node.getX () + newW, node.getY (), newW, newH, node);
				node.childNodes[2] = new QuadTreeNode <> (node.getX (), node.getY () + newH, newW, newH, node);
				node.childNodes[3] = new QuadTreeNode <> (node.getX () + newW, node.getY () + newH, newW, newH, node);
				
				for (int i = 0; i < node.childNodes.length; i++) {
					populateNode (node.childNodes[i], withinNode, maxN);
				}
			}
		}
	}
}
