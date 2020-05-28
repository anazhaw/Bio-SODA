package ch.ethz.html5.tree;

import java.io.BufferedWriter;
import java.io.FileWriter;

import junit.framework.TestCase;
import ch.ethz.html5.Canvas;
import ch.ethz.html5.Context2D;
import ch.ethz.html5.tools.HtmlHeaderTools;

/**
 * Testing a tree using the generic node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class GenericNodeTest extends TestCase {

	public static void main(String[] args) throws Exception {
		new GenericNodeTest().testTree();
	}

	public void testTree() throws Exception {

		// create tree
		GenericNode tree = new GenericNode("root");
		GenericNode child1 = tree.addChild("1st child");
		tree.addChild("2nd child");
		child1.addChild("1.1st child");

		// width and height
		TreeSpacing spacing = new TreeSpacing(40);
		int width = tree.getWidth(spacing);
		int height = tree.getHeight(spacing);

		// render
		StringBuffer html = new StringBuffer();
		Canvas canvas = new Canvas(html, "tree", width, height);
		canvas.open();
		Context2D context = canvas.getContext2D();
		context.translate(width / 2.0, 0);
		tree.draw(context, spacing);
		context.translate(width / 2.0, 0);
		canvas.close();

		// write to file
		BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/generic-tree.html"));
		writer.write(HtmlHeaderTools.getXHtmlStrictHeader("en").toString());
		writer.write("<body>\n\n");
		writer.write(html.toString());
		writer.write("</body>\n");
		writer.write("</html>");
		writer.close();
	}

}
