package ch.ethz.html5.dag;

import java.io.BufferedWriter;
import java.io.FileWriter;

import junit.framework.TestCase;
import ch.ethz.html5.Canvas;
import ch.ethz.html5.Context2D;
import ch.ethz.html5.tools.HtmlHeaderTools;

/**
 * Testing Html5DagGenericNode.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Html5DagGenericNodeTest extends TestCase {

	public static void main(String[] args) throws Exception {
		Html5DagGenericNodeTest test = new Html5DagGenericNodeTest();
		test.testBoundaryMap();
		test.testDraw();
	}

	public void testBoundaryMap() {
		System.out.println("Boundary map (1): " + getExampleDag1().getBoundaryMap());
		System.out.println("Boundary map (2): " + getExampleDag2().getBoundaryMap());
	}

	public void testDraw() throws Exception {
		System.out.println("DAG simple:");
		writeToFile("dagSimple.html", getSimpleDag());
		System.out.println("DAG 1:");
		writeToFile("dag1.html", getExampleDag1());
		System.out.println("DAG 2:");
		writeToFile("dag2.html", getExampleDag2());
	}

	private void writeToFile(String filename, Html5DagGenericNode dag) throws Exception {

		// width and height
		DagState state = dag.getBounds(40, 1);
		int width = state.width;
		int height = state.height;

		// render
		StringBuffer html = new StringBuffer();
		Canvas canvas = new Canvas(html, "dag" + filename, width, height);
		canvas.open();
		Context2D context = canvas.getContext2D();
		dag.draw(context, state, true);
		canvas.close();

		// write to file
		BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/" + filename));
		writer.write(HtmlHeaderTools.getXHtmlStrictHeader("en").toString());
		writer.write("<body>\n\n");

		writer.write(html.toString());

		writer.write("</body>\n");
		writer.write("</html>");
		writer.close();
	}

	private Html5DagGenericNode getSimpleDag() {
		Html5DagGenericNode node1 = new Html5DagGenericNode("1");
		node1.addOutput("11");
		node1.addOutput("12");
		return node1;
	}

	public Html5DagGenericNode getExampleDag1() {
		// http://en.wikipedia.org/wiki/Directed_acyclic_graph
		Html5DagGenericNode node5 = new Html5DagGenericNode("5");
		Html5DagGenericNode node11 = node5.addOutput("11");
		Html5DagGenericNode node2 = node11.addOutput("2");
		Html5DagGenericNode node9 = node11.addOutput("9");
		Html5DagGenericNode node10 = node11.addOutput("10");
		Html5DagGenericNode node7 = node11.addInput("7");
		Html5DagGenericNode node8 = node7.addOutput("8");
		node8.addEdge("8", "9");
		Html5DagGenericNode node3 = node8.addInput("3");
		node3.addEdge("3", "10");

		// set types
		node5.setType(1);
		node7.setType(1);

		node11.setType(2);
		node8.setType(2);
		node3.setType(2);
		node2.setType(2);

		node9.setType(3);
		node10.setType(3);

		node3.styleByType(1, "background-color: yellow;");
		node3.styleByType(2, "background-color: blue;");
		node3.styleByType(3, "background-color: red;");

		return node3;
	}

	public Html5DagGenericNode getExampleDag2() {
		// http://en.wikipedia.org/wiki/Directed_acyclic_graph
		Html5DagGenericNode node5 = new Html5DagGenericNode("5");
		Html5DagGenericNode node11 = node5.addOutput("11");
		Html5DagGenericNode node2 = node11.addOutput("2");
		Html5DagGenericNode node9 = node11.addOutput("9");
		Html5DagGenericNode node10 = node11.addOutput("10");
		Html5DagGenericNode node7 = node11.addInput("7");
		Html5DagGenericNode node8 = node7.addOutput("8");
		node8.addEdge("8", "9");
		Html5DagGenericNode node3 = node8.addInput("3");
		node3.addEdge("3", "10");

		// set types
		node7.setType(1);

		node5.setType(2);
		node11.setType(2);

		node2.setType(3);
		node3.setType(3);
		node8.setType(3);

		node9.setType(4);
		node10.setType(4);

		node3.styleByType(1, "background-color: yellow;");
		node3.styleByType(2, "background-color: blue;");
		node3.styleByType(3, "background-color: red;");
		node3.styleByType(4, "background-color: green;");

		return node3;
	}

}
