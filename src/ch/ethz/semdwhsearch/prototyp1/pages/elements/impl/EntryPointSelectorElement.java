package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Entry Point selector element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class EntryPointSelectorElement implements Element {

	public static final String P_URI_SELECT = "uriSelect";

	public static final String P_URI_INPUT = "uriInput";

	private final String pagename;

	private final String selUri;

	private final Html5DagGenericNode dag;

	public EntryPointSelectorElement(String pagename, String selUri, Html5DagGenericNode dag) {
		this.pagename = pagename;
		this.selUri = selUri;
		this.dag = dag;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {

		// onchange
		String onchangeSubmit = "document.getElementById(\"entry_point_selector_form\").submit();";
		String onchangeClearInput = "document.getElementById(\"entry_point_selector_input\").value=\"\";";

		// entry node IDs
		Set<String> idSet = dag.getEntryNodes();
		List<String> ids = new ArrayList<String>(idSet);
		Collections.sort(ids);

		// safe selected URI
		final String selUriSafe = selUri == null ? "" : selUri.trim();

		// flag entry node selected
		boolean entryNodeSelected = selUriSafe.length() > 0 && idSet.contains(selUri);

		// open form
		html.append("<!-- entry point selector -->\n");
		html.append("<form id='entry_point_selector_form' action='?' method='get'>\n");
		html.append("<div class='content'>\n");
		html.append("<input type='hidden' name='page' value='" + pagename + "' />\n");

		// select box of all entry nodes
		html.append("<select name='" + P_URI_SELECT + "' size='1' onchange='javascript:" + onchangeClearInput
				+ onchangeSubmit + "'>\n");
		if (entryNodeSelected) {
			html.append("<option value=''>...</option>\n");
		} else {
			html.append("<option selected='selected'>...</option>\n");
		}
		for (String id : ids) {
			String selected = selUriSafe.length() == 0 ? "" : (id.equals(selUriSafe) ? " selected='selected'" : "");
			html.append("<option" + selected + ">" + id + "</option>\n");
		}
		html.append("</select>\n");

		// input box for manual entry
		String value = entryNodeSelected ? "" : selUriSafe;
		html.append("<input id='entry_point_selector_input' type='text' name='" + P_URI_INPUT + "' onchange='javascript:"
				+ onchangeSubmit + "' value='" + value + "' size='60' />\n");

		// close form
		html.append("</div>\n");
		html.append("</form>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
