package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.util.Map;
import java.util.TreeMap;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * SPARQL executor.
 * 
 * @author Ana Sima
 * 
 */
public class AsyncDisplayOfSparqlQueries implements Element {

	private final TreeMap<String, String> sparqlStmtsMap;

	private final String ctxPath;

	public AsyncDisplayOfSparqlQueries(TreeMap<String, String> sparqlStmtsMap, String ctxPath) {
		this.sparqlStmtsMap = sparqlStmtsMap;
		this.ctxPath = ctxPath;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		String url = ctxPath + "/sparql/";
		html.append("<!-- javascript to execute SPARQL statements asynchronously -->\n");
		html.append("<script type='text/javascript'>\n");
		html.append("<!--\n");
		for (Map.Entry<String, String> entry : sparqlStmtsMap.entrySet()) {
			String resultDivId = entry.getKey();
			String sparqlStmt = entry.getValue();
			sparqlStmt = sparqlStmt.replace("\n", " ");
			html.append("executeSparqlQuery(\"" + url + "\",\"" + sparqlStmt.replace("\"","\\\"") + "\",\"" + resultDivId + "\");\n");
		}
		html.append("//-->\n");
		html.append("</script>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {

	}

}

