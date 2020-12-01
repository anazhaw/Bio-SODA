package ch.ethz.semdwhsearch.prototyp1.pages.impl.phases;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.classification.Classification;
import ch.ethz.semdwhsearch.prototyp1.constants.PageNames;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.FormElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.QueryElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.TitleElement;
import ch.ethz.semdwhsearch.prototyp1.pages.impl.AbstractPage;
import ch.ethz.semdwhsearch.prototyp1.tools.ErrorTools;
import ch.ethz.semdwhsearch.prototyp1.tools.Escape;
import ch.ethz.semdwhsearch.prototyp1.tools.request.PostRequest;

/**
 * A page for demo queries based on the SDSS dataset
 * 
 * TODO: update demo queries once ontology allows for nicer names
 * @author Ana Sima
 * 
 */
public class DemoPageSDSS extends AbstractPage {
	private static final Logger logger = LoggerFactory.getLogger(DemoPageSDSS.class);

	HashMap<String, String> classMap = new HashMap<String, String>();

	public String getInitJs() {
		return "document.getElementById(\"demo\").focus();";
	}
	
	public ArrayList<String> getQuestions() {
		String Q1 = "show all spec galaxies with ascension < 130 declination > 5";
		String Q2 = "show all photo galaxies with magnitude_g <= 23 magnitude_g >= 21 ascension < 130 declination > 5";
		String Q3 = "show all photo asteroids with mode of photo observation 1";
		String Q4 = "show white dwarfs with redshift > 0";
		String Q5 = "show all hot massive blue stars ";
		String Q6 = "show all spec stars with plate number 1760";
		String Q7 = "show all spec stars with the subclass WDhotter";
		String Q8 = "redshift of spectroscopic objects whose class is QSO";
		String Q9 = "show all quasars with ascension > 120 and declination > 5.2";
		String Q10 = "show all star burst galaxies with velocity dispersion > 800";
		
		
		ArrayList<String> results = new ArrayList<String>();
		results.add(Q1);results.add(Q2);results.add(Q3);results.add(Q4);results.add(Q5);
		results.add(Q6);results.add(Q7);results.add(Q8);results.add(Q9);results.add(Q10);
		
		return results;
	}

	public String getHtml(HttpServletRequest req, Dictionary dict) {
		String qStr = null;
		if (req.getMethod().equals("GET")) {
			qStr = req.getParameter(Params.Q);
		} else if (req.getMethod().equals("POST")) {
			PostRequest postReq = new PostRequest();
			try {
				postReq.parse(req, null, false);
			} catch (Exception e) {
				return ErrorTools.toDivWarn("Could not parse post request!");
			}
			qStr = postReq.getFormField(Params.Q);
		}

		StringBuffer html = new StringBuffer();
		new TitleElement("SDSS Demo Page").appendHtml(html, dict);
		
		// form - use the main search page to answer free text queries
		FormElement form = new FormElement(PageNames.PN_SGRAPH_FEDERATED);
		form.appendHtml(html, dict);

		// query
		new QueryElement(qStr).appendHtml(html, dict);

		// close form
		form.appendHtmlClose(html, dict);

		html.append("<div class='content'>\n");

                html.append("<table class='questions'>\n");
                html.append("<tr>\n");
                html.append("<th>");
                html.append("ID");
                html.append("</th>");

                html.append("<th>");
                html.append("Question");
                html.append("</th>");
               
                ArrayList<String> questions = getQuestions();
               
                for (int i = 0; i < questions.size(); i++) {                              
                        html.append("</tr>\n");
                        html.append("<tr>\n");
                        html.append("<td>"+ (i+1) + "</td>");
                        html.append("<td>");
                        html.append("<a href=\"" + Escape.safeXml("http://biosoda-prod.cloudlab.zhaw.ch:8091/biosoda/?page=biosoda&q=") + Escape.safeXml(questions.get(i)) + "\">");
                        html.append(questions.get(i));
                        html.append("</a>");
                        html.append("</td>\n");
                }

                html.append("</tr>\n");
                html.append("</table>\n");
                
                html.append("<ul>\n");
                html.append("<li>To start, click on one of the above questions (expected response time generally under 10 seconds)</li>");
                html.append("<li>...or use the input field above to simply type in your question and then hit the button \"Go\"</li>");
                html.append("</ul>\n");
		
		logger.info("Final time: "+ new Timestamp(System.currentTimeMillis()));

		return html.toString();
	}
}
