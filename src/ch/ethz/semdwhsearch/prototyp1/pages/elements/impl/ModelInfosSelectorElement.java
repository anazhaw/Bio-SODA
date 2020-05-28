package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.util.Collection;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.ModelInfo;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Model infos selector element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ModelInfosSelectorElement implements Element {

	public static final String P_MODELNAME = "modelName";

	public static final String V_ALL = "all";

	private final String pagename;

	private final String selModelName;

	private final Metadata metadata;

	public ModelInfosSelectorElement(String pagename, String selModelName, Metadata metadata) {
		this.pagename = pagename;
		this.selModelName = selModelName;
		this.metadata = metadata;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {

		// onchange
		String onchange = "javascript:document.getElementById(\"model_infos_selector_form\").submit();";

		// model infos
		Collection<ModelInfo> modelInfos = metadata.getModelInfos();

		html.append("<!-- model infos selector -->\n");
		html.append("<form id='model_infos_selector_form' action='?' method='get'>\n");
		html.append("<div class='content'>\n");
		html.append("<input type='hidden' name='page' value='" + pagename + "' />\n");
		html.append("<select name='" + P_MODELNAME + "' size='1' onchange='" + onchange + "'>\n");
		if (selModelName == null || selModelName.equals(V_ALL)) {
			html.append("<option selected='selected'>...</option>\n");
		} else {
			html.append("<option value='" + V_ALL + "'>...</option>\n");
		}
		for (ModelInfo curModelInfo : modelInfos) {
			String curModelName = curModelInfo.getModelName();
			String selected = selModelName == null ? "" : (curModelName.equals(selModelName) ? " selected='selected'" : "");
			html.append("<option" + selected + ">" + curModelName + "</option>\n");
		}
		html.append("</select>\n");
		html.append("</div>\n");
		html.append("</form>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
