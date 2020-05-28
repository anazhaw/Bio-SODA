package ch.ethz.semdwhsearch.prototyp1.pages.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.constants.PageNames;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionaries;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.localization.English;
import ch.ethz.semdwhsearch.prototyp1.pages.Page;
import ch.ethz.semdwhsearch.prototyp1.tools.request.GetRequest;

/**
 * Base class for all pages.
 * 
 * @author Lukas Blunschi
 * 
 */
public abstract class AbstractPage implements Page {

	public String getInitJs() {
		return null;
	}

	public final String getContent(HttpServletRequest req, Dictionary dict) {
		StringBuffer html = new StringBuffer();

		// init script (if any)
		String initJs = getInitJs();
		if (initJs != null) {
			html.append("<!-- init script -->\n");
			html.append("<script type='text/javascript'>function init() {");
			html.append(initJs);
			html.append("}</script>\n\n");
		}

		// header (welcome and language selector)
		String langCodeStr = (String) req.getSession().getAttribute(Constants.A_LANG_CODE);
		if (langCodeStr == null) {
			langCodeStr = String.valueOf(English.LANGCODE);
		}
		final String selValue = langCodeStr;
		List<String> texts = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		for (String langCode : Dictionaries.getLanguageCodes()) {
			values.add(langCode);
			texts.add(Dictionaries.getDictionary(langCode).langName());
		}
		Map<String, String> paramMap = GetRequest.getParameterMap(req);
		paramMap.remove(Constants.A_LANG_CODE);
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			html.append("<input type='hidden' name='" + name + "' value='" + value + "' />\n");
		}
		html.append(dict.welcome()).append("! ");
		
		html.append("</select>\n");
		html.append("</div>\n");
		html.append("</form>\n\n");

		// title
		html.append("<!-- title -->\n");
		html.append("<div id='title'>");
		html.append("<h1>" + dict.mainTitle().replaceAll("\\s", "&#160;") + "</h1>");
		html.append("</div>\n\n");

		// horizontal bar
		html.append("<!-- horizontal bar -->\n");
		html.append("<div id='horizontal-bar'>&#160;</div>\n\n");

		// real html content
		html.append(getHtml(req, dict));

		// footer
		// - contributors
		// - actions
		html.append("<!-- footer -->\n");
		html.append("<div class='footer content'>\n");
		// contributors
		html.append("<div style='float:right'>\n");
		// html.append(Constants.CONTRIBUTORS);
		html.append("</div>\n");
		// actions
		html.append("<div>\n");
		html.append("</div>\n");
		html.append("</div>\n\n");

		return html.toString();
	}

}
