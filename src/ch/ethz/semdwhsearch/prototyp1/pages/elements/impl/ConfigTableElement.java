package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.util.Map;
import java.util.Set;

import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * A configuration table.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ConfigTableElement implements Element {

	private final Set<String> propNames;

	private final Config config;

	public ConfigTableElement(Set<String> propNames, Config config) {
		this.propNames = propNames;
		this.config = config;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {

		// properties
		Map<String, String> props = config.getAllProperties();

		// comment
		html.append("<!-- config table -->\n");

		// open table
		html.append("<table class='config content'>\n");

		// header row
		html.append("<tr class='header'>");
		html.append("<td>" + dict.propertyName() + "</td>");
		html.append("<td>" + dict.storedValue() + "</td>");
		html.append("<td>" + dict.newValue() + "</td>");
		html.append("</tr>\n");

		// one row per property
		for (String propName : propNames) {
			String propValue = props.get(propName);
			html.append("<tr>");

			// name
			html.append("<td>" + propName + "</td>");

			// stored value
			html.append("<td class='stored'>" + propValue + "</td>");

			// input
			html.append("<td>");
			html.append("<input type='text' name='" + propName + "' value='" + propValue + "' />");
			html.append("</td>");

			html.append("</tr>\n");
		}

		// submit
		html.append("<tr>");
		html.append("<td colspan='2'>&#160;</td>");
		html.append("<td><input type='submit' value='" + dict.save() + "' /></td>");
		html.append("</tr>\n");

		// close table
		html.append("</table>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {

	}

}
