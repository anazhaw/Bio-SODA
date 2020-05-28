package ch.ethz.html5.dag;

import java.io.BufferedWriter;
import java.io.FileWriter;

import ch.ethz.html5.Canvas;
import ch.ethz.html5.Context2D;
import ch.ethz.html5.tools.HtmlHeaderTools;

/**
 * A HTML5 DAG writer.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Html5DagWriter {

	public void writeToFile(String filename, Html5DagGenericNode dag, int radius) throws Exception {

		// width and height
		DagState state = dag.getBounds(radius, 2);
		int width = state.width;
		int height = state.height;

		// render
		StringBuffer html = new StringBuffer();
		Canvas canvas = new Canvas(html, "dag" + filename, width, height);
		canvas.open();
		Context2D context = canvas.getContext2D();
		dag.draw(context, state, false);
		canvas.close();

		// write to file
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write(HtmlHeaderTools.getXHtmlStrictHeader("en").toString());
		writer.write("<body>\n\n");

		writer.write(html.toString());

		writer.write("</body>\n");
		writer.write("</html>");
		writer.close();
	}

}
