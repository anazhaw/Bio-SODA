package ch.ethz.html5;

/**
 * An HTML5 Canvas element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Canvas {

	private final String id;

	private final StringBuffer html;

	private final int width;

	private final int height;

	public Canvas(StringBuffer html, String id, int width, int height) {
		this.id = id;
		this.html = html;
		this.width = width;
		this.height = height;
	}

	public void open() {

		// add canvas
		html.append("<!-- canvas -->\n");
		html.append("<canvas id='" + id + "'></canvas>\n\n");

		// graph
		html.append("<!-- graph -->\n");
		html.append("<script type='text/javascript'>\n");
		html.append("<!--\n");
		html.append("var canvas = document.getElementById(\"" + id + "\");\n");
		html.append("canvas.setAttribute('width', '" + width + "');\n");
		html.append("canvas.setAttribute('height', '" + height + "');\n");
		html.append("var context = canvas.getContext('2d');\n");

	}

	public Context2D getContext2D() {
		return new Context2D(html);
	}

	public void close() {
		html.append("\n-->\n");
		html.append("</script>\n\n");
	}

}
