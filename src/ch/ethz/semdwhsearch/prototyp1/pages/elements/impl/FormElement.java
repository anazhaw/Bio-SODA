package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * The form element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class FormElement implements Element {

	private final String pagename;

	private String id;

	private String actionName;

	public FormElement(String pagename) {
		this.pagename = pagename;
		this.id = null;
		this.actionName = null;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		String idStr = id == null ? "" : " id='" + id + "'";
		String enctypeStr = " enctype='multipart/form-data'";
		String actionStr = " action='?page=" + pagename + (actionName == null ? "" : "&amp;action=" + actionName) + "'";
		String methodStr = " method='post'";
		html.append("<!-- form -->\n");
		html.append("<form" + idStr + enctypeStr + actionStr + methodStr + ">\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
		html.append("<!-- close form -->\n");
		html.append("</form>\n\n");
	}

}
