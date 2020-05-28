package ch.ethz.semdwhsearch.prototyp1.pages.impl;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.init.ConfigAction;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.LogoElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.TitleElement;

/**
 * Configuration page.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ConfigPage extends AbstractPage {

	public static final String NAME = "config";

	public String getInitJs() {
		return "document.getElementById(\"dbvendor\").focus();";
	}

	public String getHtml(HttpServletRequest req, Dictionary dict) {
		StringBuffer html = new StringBuffer();

		// title
		new TitleElement(dict.config()).appendHtml(html, dict);

		// logo
		new LogoElement().appendHtml(html, dict);

		// form
		String action = "?action=" + ConfigAction.NAME;
		html.append("<!-- form and table -->\n");
		html.append("<form id='query_form' enctype='multipart/form-data' action='" + action + "' method='post'>\n");

		// database connection
		html.append("<table class='tablecontent'>\n");
		html.append("<tr>");
		html.append("<td colspan='2'><h3>").append(dict.dbconn()).append(":</h3></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.dbvendor()).append("</td>");
		html.append("<td><input id='dbvendor' type='text' name='" + Params.DBVENDOR + "' value='' /></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.schema()).append("</td>");
		html.append("<td><input type='text' name='" + Params.SCHEMA + "' value='' /></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.username()).append("</td>");
		html.append("<td><input type='text' name='" + Params.USERNAME + "' value='' /></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.password()).append("</td>");
		html.append("<td><input type='password' name='" + Params.PASSWORD + "' value='' /></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.dbname()).append("</td>");
		html.append("<td><input type='text' name='" + Params.DBNAME + "' value='' /></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.hostname()).append("</td>");
		html.append("<td><input type='text' name='" + Params.HOSTNAME + "' value='' /></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.port()).append("</td>");
		html.append("<td><input type='text' name='" + Params.PORT + "' value='' /></td>");
		html.append("</tr>\n");
		html.append("</table>\n");

		// data directory
		html.append("<table class='tablecontent'>\n");
		html.append("<tr>");
		html.append("<td colspan='2'><h3>").append("Configuration and RDF Data Diretories").append(":</h3></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.configDirectory()).append("</td>");
		html.append("<td><input type='text' name='" + Params.CONFIGDIR + "' value='config/' /></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.dataDirectory()).append("</td>");
		html.append("<td><input type='text' name='" + Params.DATADIR + "' value='sample_data/' /></td>");
		html.append("</tr>\n");
		html.append("</table>\n");

		// inverted index
		// TODO move index rebuild to admin page
		html.append("<table class='tablecontent'>\n");
		html.append("<tr>");
		html.append("<td colspan='2'><h3>").append(dict.invertedIndex()).append(":</h3></td>");
		html.append("</tr>\n");
		html.append("<tr>");
		html.append("<td>").append(dict.reload()).append("</td>");
		html.append("<td><input type='checkbox' name='" + Params.INVERTEDINDEX + "' /></td>");
		html.append("</tr>\n");
		html.append("</table>\n");
		

 		html.append("<table class='tablecontent'>\n");
 		html.append("<tr>");
 		html.append("<td colspan='2'><h3>").append(dict.appendToIndex()).append(":</h3></td>");
 		html.append("</tr>\n");
 		html.append("<tr>");
 		html.append("<td>").append(dict.append()).append("</td>");
 		html.append("<td><input type='checkbox' name='" + Params.APPENDTOINDEX + "' /></td>");
 		html.append("</tr>\n");
 		html.append("</table>\n");


		// submit button
		html.append("<div class='content'>\n");
		html.append("<input type='submit' value='" + dict.go() + "' />\n");
		html.append("</div>\n");

		html.append("</form>\n\n");

		return html.toString();
	}

}
