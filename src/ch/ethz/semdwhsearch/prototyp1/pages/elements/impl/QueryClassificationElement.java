package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.util.List;

import ch.ethz.semdwhsearch.prototyp1.classification.Match;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Query classification element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class QueryClassificationElement implements Element {

	private final List<Match> matches;

	private final String query;

	private final String pagename;

	private final String ctxPath;

	public QueryClassificationElement(List<Match> matches, String query, String pagename, String ctxPath) {
		this.matches = matches;
		this.query = query;
		this.pagename = pagename;
		this.ctxPath = ctxPath;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- query classification -->\n");
		html.append("<div class='content'>\n");

		html.append("<table class='query'>\n");
		html.append("<tr>\n");
		
		for (Match match : matches) {
			if(match.getTerms().size() == 0)
				continue;
			html.append("<th>");
			Term t = match.getTerms().get(0);
			html.append(t.key);
			//special display for numbers detected in question
			if(t.key.matches("-?\\d+(\\.\\d+)?"))
				html.append("<i>   (filter on property)</i>");
			html.append("</th>");
		}
		html.append("</tr>\n");
		html.append("<tr>\n");
		for (Match match: matches) {
			if(match != null) {
				html.append("<td>");
				html.append(match.toHtml());
				html.append("</td>\n");
			}
		}

		html.append("</tr>\n");
		html.append("</table>\n");
		// legend

		// origins
		final int displayCount = Constants.DISPLAY_COUNT;
		html.append("<ul>\n");
		for (Match match : matches) {
			if(match.getTerms().size() == 0)
                                continue;
			html.append("      <li>");
			Term t = match.getTerms().get(0);
			html.append("<b>"+ t.key + "</b>");
                        //special display for numbers detected in question
                        if(t.key.matches("-?\\d+(\\.\\d+)?"))
				html.append("<i> (numerical filter on property) </i>");
			html.append("      </li>");
			final int termCount = match.getTerms().size();
			for (int j = 0; j < termCount && j < displayCount; j++) {
				Term term = match.getTerms().get(j);
				if(term != null) {
					html.append("      <li>");
					html.append(term.toHtml() + ": " + term.originName + ", " + term.origin);
					html.append("</li>\n");
				}
			}
			if (termCount > displayCount) {
				html.append("<li>... " + (termCount - displayCount) + " more result</li>");
			}
		}
		html.append("</ul>\n");

		html.append("</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
