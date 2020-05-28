package ch.ethz.html5.tools;

/**
 * Tools to write an HTML header.
 * 
 * @author Lukas Blunschi
 * 
 */
public class HtmlHeaderTools {

	/**
	 * Get HTML 5 header.
	 * 
	 * @param langCode
	 *            language code, e.g. en or de.
	 * @param title
	 *            website title.
	 * @param ctxPath
	 *            context path of this webapp, e.g. /mywebapp
	 * @return
	 */
	public static StringBuffer getHtml5Header(String langCode, String title, String ctxPath) {
		StringBuffer html = new StringBuffer();

		html.append("<!DOCTYPE html>\n");

		html.append("<html dir='ltr' lang='" + langCode + "'>\n\n");

		html.append("<head>\n");
		html.append("  <meta charset='UTF-8' />\n");
		html.append("  <title>" + title + "</title>\n");
		html.append("  <link rel='stylesheet' type='text/css' href='" + ctxPath + "/css/semdwhsearch.css' />\n");
		html.append("  <link rel='stylesheet' type='text/css' href='" + ctxPath + "/css/elements.css' />\n");
		html.append("  <link rel='stylesheet' type='text/css' href='" + ctxPath + "/css/dagBrowser.css' />\n");
		html.append("  <link rel='icon' type='image/gif' href='" + ctxPath + "/images/semdwhsearch-icon.gif' />\n");
		html.append("  <script type='text/javascript' src='" + ctxPath + "/js/pageSubmission.js'></script>\n");
		html.append("  <script type='text/javascript' src='" + ctxPath + "/js/toggle.js'></script>\n");
		html.append("  <script type='text/javascript' src='" + ctxPath + "/js/tooltips.js'></script>\n");
		html.append("  <script type='text/javascript' src='" + ctxPath + "/js/xml.js'></script>\n");
		html.append("  <script type='text/javascript' src='" + ctxPath + "/js/dagBrowser.js'></script>\n");
		html.append("  <script type='text/javascript' src='" + ctxPath + "/js/libs/d3.v2.min.js'></script>\n");
		html.append("  <script type='text/javascript' src='" + ctxPath + "/js/libs/jquery-1.7.2.min.js'></script>\n");
		html.append("</head>\n\n");

		return html;
	}

	public static StringBuffer getXHtmlStrictHeader(String langCode) {
		StringBuffer html = new StringBuffer();

		html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
		html.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
		html.append("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='" + langCode + "' lang='" + langCode + "'>\n");

		html.append("<head>\n");
		html.append("   <meta http-equiv='pragma' content='no-cache' />\n");
		html.append("   <meta http-equiv='expires' content='tue, 04 dec 1993 21:29:02 gmt' />\n");
		html.append("   <meta http-equiv='content-type' content='text/html; charset=utf-8' />\n");
		html.append("   <title>Semantic DWH Search</title>\n");
		html.append("   <link rel='stylesheet' type='text/css' href='css/semdwhsearch.css' />\n");
		html.append("   <link rel='stylesheet' type='text/css' href='css/elements.css' />\n");
		html.append("   <link rel='stylesheet' type='text/css' href='css/dagBrowser.css' />\n");
		html.append("   <link rel='icon' type='image/gif' href='images/semdwhsearch-icon.gif' />\n");
		html.append("   <script type='text/javascript' src='js/pageSubmission.js'></script>\n");
		html.append("   <script type='text/javascript' src='js/toggle.js'></script>\n");
		html.append("   <script type='text/javascript' src='js/tooltips.js'></script>\n");
		html.append("   <script type='text/javascript' src='js/dagBrowser.js'></script>\n");
		html.append("   <script type='text/javascript' src='js/libs/d3.v2.min.js'></script>\n");
		html.append("   <script type='text/javascript' src='js/libs/jquery-1.7.2.min.js'></script>\n");
		html.append("</head>\n");

		return html;
	}

}
