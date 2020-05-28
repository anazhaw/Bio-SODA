package ch.ethz.html5;

import java.util.ArrayList;
import java.util.List;

/**
 * Style of a node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Style {

	private List<StyleKeyValue> keysAndValues;

	/**
	 * Default constructor.
	 */
	public Style() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param style
	 *            initial style information. (maybe an empty string or null).
	 */
	public Style(String style) {
		this.keysAndValues = parse(style);
	}

	// ---------------------------------------------------- getters and setters

	public void setStyle(String style) {
		this.keysAndValues = parse(style);
	}

	public String getStyle() {
		return format(keysAndValues);
	}

	public void addKeyValue(String key, String value) {
		keysAndValues.add(new StyleKeyValue(key, value));
	}

	public List<StyleKeyValue> getKeysAndValues() {
		return keysAndValues;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return format(keysAndValues);
	}

	// -------------------------------------------------------- private methods

	private static List<StyleKeyValue> parse(String style) {
		List<StyleKeyValue> result = new ArrayList<StyleKeyValue>();
		if (style != null) {
			String[] styleInfos = style.split(";");
			for (String styleInfo : styleInfos) {
				String keyAndValue[] = styleInfo.split(":");
				if (keyAndValue.length == 2) {
					String key = keyAndValue[0].trim();
					String value = keyAndValue[1].trim();
					result.add(new StyleKeyValue(key, value));
				}
			}
		}
		return result;
	}

	private static String format(List<StyleKeyValue> keysAndValues) {
		StringBuffer css = new StringBuffer();
		for (StyleKeyValue info : keysAndValues) {
			css.append(info.key + ": " + info.value + "; ");
		}
		return css.toString();
	}

}
