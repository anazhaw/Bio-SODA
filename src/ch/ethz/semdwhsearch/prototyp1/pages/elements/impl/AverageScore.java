package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * An average score.
 * 
 * @author Lukas Blunschi
 * 
 */
public class AverageScore implements Element {

	private final double avgScore;

	public AverageScore(double avgScore) {
		this.avgScore = avgScore;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- average score -->\n");
		html.append("<div class='content'>" + dict.averageScore() + ": " + avgScore + "</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {

	}

}
