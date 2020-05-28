package ch.ethz.semdwhsearch.prototyp1.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.ethz.html5.tools.HtmlHeaderTools;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.data.Data;
import ch.ethz.semdwhsearch.prototyp1.data.DataSingleton;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionaries;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.tools.IOUtils;
import ch.ethz.semdwhsearch.prototyp1.tools.ServletResponseTools;
import ch.ethz.semdwhsearch.prototyp1.tools.request.GetRequest;
import ch.ethz.semdwhsearch.prototyp1.tools.request.PostRequest;
import ch.ethz.semdwhsearch.prototyp1.tools.request.Request;

/**
 * The SPARQL servlet.
 * 
 * @author Ana Sima
 * 
 */
public class SparqlServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			// parse request
			ServletInputStream in = req.getInputStream();
			String sparql = IOUtils.readToString(in, "utf-8");

			// create XML/HTML
			StringBuffer xml = new StringBuffer();

			// check input
			if (sparql == null || sparql.trim().length() == 0) {
				xml.append("<p class='error content'>SPARQL parameter missing.</p>");
			} else {
				sparql = sparql.trim();
				if (!sparql.startsWith("<sparql>") || !sparql.endsWith("</sparql>")) {
					xml.append("<p class='error content'>SPARQL parameter has invalid format: " + sparql + "</p>");
				} else {
					Data data = DataSingleton.getInstance().getData();
					if (data == null) {
						xml.append("<p class='error content'>Data source not connected.</p>");
					} else {

						// execute SPARQL
						sparql = sparql.substring(8, sparql.length() - 9);
						xml.append(executeSparql(sparql, data));
					}
				}
			}

			// result
			ServletResponseTools.streamStringBuffer(xml, "text/xml", "UTF-8", 0.0, req, resp);

		} catch (Exception e) {
			ServletResponseTools.sendInternalServerError(req, resp, e.getMessage());
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			// parse request
			Request request = req.getMethod().equals("GET") ? new GetRequest(req) : new PostRequest(req);

			// get sparql parameter
			String sparql = request.getParameter(Constants.P_SPARQL);

			// create html
			StringBuffer html = new StringBuffer();

			// check input
			if (sparql == null || sparql.trim().length() == 0) {
				html.append("<p class='error content'>SPARQL parameter missing.</p>\n\n");
			} else {
				Data data = DataSingleton.getInstance().getData();
				if (data == null) {
					html.append("<p class='error content'>Data source not initialized.</p>\n\n");
				} else {

					// execute SQL
					html.append(executeSparql(sparql, data));
				}
			}

			// dictionary
			Dictionary dict = Dictionaries.getDictionaryFromSession(req);

			// result
			StringBuffer out = new StringBuffer();
			out.append(HtmlHeaderTools.getHtml5Header(dict.langCode(), dict.mainTitle(), req.getContextPath()));
			out.append("<body>\n\n");
			out.append(html);
			out.append("</body>\n");
			out.append("</html>\n");
			ServletResponseTools.streamStringBuffer(out, "text/html", "UTF-8", 0.0, req, resp);

		} catch (Exception e) {
			ServletResponseTools.sendInternalServerError(req, resp, e.getMessage());
		}
	}

	private StringBuffer executeSparql(String sparql, Data data) {
		StringBuffer html = new StringBuffer();
		StringBuffer buf = new StringBuffer();
		data.appendSPARQLResultTable(buf, sparql, 20);
		html.append(buf);
		return html;
	}

}

