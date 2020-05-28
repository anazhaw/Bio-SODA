package ch.ethz.html5;

/**
 * An HTML5 CanvasRenderingContext2D element.
 * 
 * @author Lukas Blunschi
 * @see http://dev.w3.org/html5/2dcontext/
 * 
 */
public class Context2D {

	// ---------------------------------------------------------------- members

	private final StringBuffer html;

	private int fontSizePx;

	private String fontFamily;

	/* color used for text */
	private String color;

	// --------------------------------------------- members from specification

	private String strokeStyle;

	private String fillStyle;

	private String globalAlpha;

	private String lineWidth;

	private String lineCap;

	private String lineJoin;

	private String miterLimit;

	private String shadowOffsetX;

	private String shadowOffsetY;

	private String shadowBlur;

	private String shadowColor;

	private String globalCompositeOperation;

	private String font;

	private String textAlign;

	private String textBaseline;

	/**
	 * Constructor.
	 * 
	 * @param html
	 */
	public Context2D(StringBuffer html) {
		this.html = html;
		this.fontSizePx = 15;
		this.fontFamily = "sans-serif";
		setColor("black");
		setStrokeStyle("#666666");
		setFillStyle("#cccccc");
		setGlobalAlpha("");
		setLineWidth("1px");
		setLineCap("");
		setLineJoin("");
		setMiterLimit("");
		setShadowOffsetX("");
		setShadowOffsetY("");
		setShadowBlur("");
		setShadowColor("");
		setGlobalCompositeOperation("");
		setFont("");
		setTextAlign("center");
		setTextBaseline("");
	}

	// -------------------------------------------------------- transformations

	public void scale(double x, double y) {
		html.append("context.scale(" + x + "," + y + ");\n");
	}

	public void rotate(double angle) {
		html.append("context.rotate(" + angle + ");\n");
	}

	public void translate(double x, double y) {
		html.append("context.translate(" + x + "," + y + ");\n");
	}

	// -------------------------------------------- special getters and setters

	public String getColor() {
		return color;
	}

	public void setColor(String cssColor) {
		this.color = cssColor;
	}

	// --------------------------------- getters and setters from specification

	public String getStrokeStyle() {
		return strokeStyle;
	}

	public void setStrokeStyle(String cssColor) {
		this.strokeStyle = cssColor;
		html.append("context.strokeStyle = '" + cssColor + "';\n");
	}

	public String getFillStyle() {
		return fillStyle;
	}

	public void setFillStyle(String cssColor) {
		this.fillStyle = cssColor;
		html.append("context.fillStyle = '" + cssColor + "';\n");
	}

	public String getGlobalAlpha() {
		return globalAlpha;
	}

	public void setGlobalAlpha(String globalAlpha) {
		this.globalAlpha = globalAlpha;
	}

	public String getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(String lineWidth) {
		this.lineWidth = lineWidth;
	}

	public String getLineCap() {
		return lineCap;
	}

	public void setLineCap(String lineCap) {
		this.lineCap = lineCap;
	}

	public String getLineJoin() {
		return lineJoin;
	}

	public void setLineJoin(String lineJoin) {
		this.lineJoin = lineJoin;
	}

	public String getMiterLimit() {
		return miterLimit;
	}

	public void setMiterLimit(String miterLimit) {
		this.miterLimit = miterLimit;
	}

	public String getShadowOffsetX() {
		return shadowOffsetX;
	}

	public void setShadowOffsetX(String shadowOffsetX) {
		this.shadowOffsetX = shadowOffsetX;
	}

	public String getShadowOffsetY() {
		return shadowOffsetY;
	}

	public void setShadowOffsetY(String shadowOffsetY) {
		this.shadowOffsetY = shadowOffsetY;
	}

	public String getShadowBlur() {
		return shadowBlur;
	}

	public void setShadowBlur(String shadowBlur) {
		this.shadowBlur = shadowBlur;
	}

	public String getShadowColor() {
		return shadowColor;
	}

	public void setShadowColor(String shadowColor) {
		this.shadowColor = shadowColor;
	}

	public String getGlobalCompositeOperation() {
		return globalCompositeOperation;
	}

	public void setGlobalCompositeOperation(String globalCompositeOperation) {
		this.globalCompositeOperation = globalCompositeOperation;
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public String getTextAlign() {
		return textAlign;
	}

	/**
	 * 
	 * @param textAlign
	 *            possible values: start, end, left, right, and center.
	 */
	public void setTextAlign(String textAlign) {
		this.textAlign = textAlign;
		html.append("context.textAlign = '" + textAlign + "';\n");
	}

	public String getTextBaseline() {
		return textBaseline;
	}

	public void setTextBaseline(String textBaseline) {
		this.textBaseline = textBaseline;
	}

	/**
	 * Apply style.
	 * 
	 * @param style
	 * @return previous style information (only changed information).
	 */
	public Style applyStyle(Style style) {
		Style result = new Style();
		for (StyleKeyValue info : style.getKeysAndValues()) {
			if (info.key.equals("border")) {
				result.addKeyValue(info.key, getLineWidth() + " solid " + getStrokeStyle());
				String[] parts = info.value.split(" ");
				if (parts.length == 3) {
					setLineWidth(parts[0].trim());
					setStrokeStyle(parts[2].trim());
				} else {
					System.err.println("Ignoring wrong border spec: " + info.value);
				}
			} else if (info.key.equals("background-color")) {
				result.addKeyValue(info.key, getFillStyle());
				setFillStyle(info.value);
			} else if (info.key.equals("color")) {
				result.addKeyValue(info.key, getColor());
				setColor(info.value);
			}
		}
		return result;
	}

	// ------------------------------------------------------- TODO line styles

	// ----------------------------------------------------------- TODO shadows

	// ---------------------------------------------- simple shapes: rectangles

	public void clearRect(double x, double y, double w, double h) {
		// TODO
	}

	public void fillRect(double x, double y, double w, double h) {
		// TODO
	}

	public void strokeRect(double x, double y, double w, double h) {
		// TODO
	}

	// -------------------------------------------------- complex shapes: paths

	public void beginPath() {

	}

	public void moveTo(int x, int y) {
		html.append("context.moveTo(" + x + ", " + y + ");\n");
	}

	public void closePath() {

	}

	public void lineTo(int x, int y) {
		html.append("context.beginPath();\n");
		html.append("context.lineTo(0,0);\n");
		html.append("context.lineTo(" + x + ", " + y + ");\n");
		html.append("context.stroke();\n");
	}

	public void quadraticCurveTo(double cpx, double cpy, double cp2x, double cp2y, int x, int y) {

	}

	public void bezierCurveTo(double cp1x, double cp1y, double xp2x, double cp2y, int x, int y) {

	}

	public void arcTo(int x1, int y1, int x2, int y2, double radius) {

	}

	public void arc(int x, int y, int radius, double startAngle, double endAngle, boolean antiClockWise) {

	}

	public void rect(int x, int y, int w, int h) {

	}

	public void fill() {

	}

	public void stroke() {

	}

	public void clip() {

	}

	public void isPointInPath(int x, int y) {

	}

	// ------------------------------------------------------------------- text

	/**
	 * 
	 * @param fontFamily
	 *            font family, e.g. sans-serif.
	 */
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
		html.append("context.font = '" + fontSizePx + ", " + fontFamily + "';\n");
	}

	/**
	 * 
	 * @param fontSizePx
	 *            font size in pixels, e.g. 12 for 12px.
	 */
	public void setFontSize(int fontSizePx) {
		this.fontSizePx = fontSizePx;
		html.append("context.font = '" + fontSizePx + ", " + fontFamily + "';\n");
	}

	// TODO more text

	// ------------------------------------------------------- extended methods

	public void lineWithText(double x, double y, String text) {
		html.append("context.beginPath();\n");
		html.append("context.moveTo(0,0);\n");
		html.append("context.lineTo(" + x + ", " + y + ");\n");
		html.append("context.stroke();\n");
	}
	
	public void line(double x, double y) {
		html.append("context.beginPath();\n");
		html.append("context.moveTo(0,0);\n");
		html.append("context.lineTo(" + x + ", " + y + ");\n");
		html.append("context.stroke();\n");
	}

	public void circle(double radius) {
		// arc:
		// x, y, radius, startAng, endAng, anti-clockwise
		html.append("context.beginPath();\n");
		html.append("context.arc(0,0," + radius + ",0,6.2832,false);\n");
		html.append("context.stroke();\n");
		html.append("context.fill();\n");
	}

	public void textSplitLines(String text) {
		html.append("context.fillStyle = '" + color + "';\n");
		int lineHeiht = fontSizePx;
		int textHeight = fontSizePx * 3 / 4;
		String[] parts = text.trim().split(" ");
		if (parts.length > 1) {
			// two lines
			int split = parts.length / 2;
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < split; i++) {
				if (i > 0) {
					buf.append(" ");
				}
				buf.append(parts[i]);
			}
			String line1 = buf.toString();
			buf = new StringBuffer();
			for (int i = split; i < parts.length; i++) {
				if (i > split) {
					buf.append(" ");
				}
				buf.append(parts[i]);
			}
			String line2 = buf.toString();
			html.append("context.fillText('" + line1 + "',0," + (-lineHeiht / 2 + textHeight / 2) + ");\n");
			html.append("context.fillText('" + line2 + "',0," + (lineHeiht / 2 + textHeight / 2) + ");\n");
		} else {
			html.append("context.fillText('" + text + "',0," + (textHeight / 2) + ");\n");
		}
	}

	public void textOneLine(String text, int maxWidth) {
		text = text.replaceAll("\\(", " ");
		text = text.replaceAll("\\)", " ");
		text = text.replaceAll("'", " ");
		text = text.replaceAll("\\\n", " ");
		if (text.length() > 30) {
			text = text.substring(0, 30);
		}
		html.append("context.fillStyle = '" + color + "';\n");
		int dy = fontSizePx / 2;
		html.append("context.fillText('" + text + "',0," + dy + ");\n");
	}
	
	public void text(String text, int maxWidth, int maxHeight) {
		text = text.replaceAll("\\(", " ");
		text = text.replaceAll("\\)", " ");
		text = text.replaceAll("'", " ");
		text = text.replaceAll("\\\n", " ");
		/*if (text.length() > 30) {
			text = text.substring(0, 30);
		}*/
		
		html.append("context.fillStyle = '" + color + "';\n");
		html.append("context.fillText('" + text + "',"+ maxWidth / 2 +"," + fontSizePx + ");\n");
	}

}
