package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Show given execution times.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ExecutionTimes implements Element {

	private final long t0;

	private final long t1;

	private final long t2;

	private final long t3;

	private final long t4;

	private final long t5;

	private final long t6;

	public ExecutionTimes(long t0, long t1, long t2, long t3, long t4, long t5, long t6) {
		this.t0 = t0;
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.t4 = t4;
		this.t5 = t5;
		this.t6 = t6;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- execution times -->\n");
		html.append("<table class='grid content'>\n");
		html.append("<tr><td>Lookup: </td><td>" + (((int) (((t1 - t0) / 1E9) * 100)) / 100.0) + "s.</td></tr>\n");
		html.append("<tr><td>Rank:   </td><td>" + (((int) (((t2 - t1) / 1E9) * 100)) / 100.0) + "s.</td></tr>\n");
		html.append("<tr><td>Tables: </td><td>" + (((int) (((t3 - t2) / 1E9) * 100)) / 100.0) + "s.</td></tr>\n");
		html.append("<tr><td>Filter: </td><td>" + (((int) (((t4 - t3) / 1E9) * 100)) / 100.0) + "s.</td></tr>\n");
		html.append("<tr><td>SQL:    </td><td>" + (((int) (((t5 - t4) / 1E9) * 100)) / 100.0) + "s.</td></tr>\n");
		html.append("<tr><td>Group:  </td><td>" + (((int) (((t6 - t5) / 1E9) * 100)) / 100.0) + "s.</td></tr>\n");
		html.append("</table>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
