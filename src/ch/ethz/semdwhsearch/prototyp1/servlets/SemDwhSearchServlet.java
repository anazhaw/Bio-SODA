package ch.ethz.semdwhsearch.prototyp1.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.html5.tools.HtmlHeaderTools;
import ch.ethz.semdwhsearch.prototyp1.actions.Action;
import ch.ethz.semdwhsearch.prototyp1.actions.ReloadClassificationIndexAction;
import ch.ethz.semdwhsearch.prototyp1.actions.UpdateConfigAction;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Failure;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Result;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.constants.PageNames;
import ch.ethz.semdwhsearch.prototyp1.data.Data;
import ch.ethz.semdwhsearch.prototyp1.data.DataSingleton;
import ch.ethz.semdwhsearch.prototyp1.init.ConfigAction;
import ch.ethz.semdwhsearch.prototyp1.init.Init;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionaries;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.Page;
import ch.ethz.semdwhsearch.prototyp1.pages.impl.ConfigPage;
import ch.ethz.semdwhsearch.prototyp1.pages.impl.phases.DemoPageQald4;
import ch.ethz.semdwhsearch.prototyp1.pages.impl.phases.FederatedSummaryGraphPage;
import ch.ethz.semdwhsearch.prototyp1.tools.ServletResponseTools;
import ch.ethz.semdwhsearch.prototyp1.tools.request.GetRequest;

/**
 * The SemDwhSearch servlet.
 * 
 * @author Lukas Blunschi
 * 
 */
public class SemDwhSearchServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(SemDwhSearchServlet.class);

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// create html
		StringBuffer html = new StringBuffer();

		// check for action
		String actionStr = req.getParameter("action");
		Action action = checkForAndReturnAction(actionStr);
		
		Result result = null;
		if (actionStr != null && action == null) {
			result = new Failure("Action unknown or insufficient rights.");
		}
		if (action != null) {
			executeAction(req, resp, action);
		}
		else {
			// check for automatic initialization
			Data data = DataSingleton.getInstance().getData();
			if (data == null) {
				data = initApplicationAndLoadData(req, data);
			}
	
			// find message (if any)
			String msg = req.getParameter("msg");
	
			// find page
			String pageStr = req.getParameter("page");
			Page page = getRequestedPage(data, pageStr);
			if (page == null) {
				result = new Failure("Unknown page.");
			}
	
			// dictionary
			Dictionary dict = Dictionaries.getDictionaryFromSession(req);
	
			// init script
			boolean hasInit = page.getInitJs() != null;
			String onload = hasInit ? " onload='init();'" : "";
	
			// html
			html.append(HtmlHeaderTools.getHtml5Header(dict.langCode(), dict.mainTitle(), req.getContextPath()));
			html.append("<body" + onload + ">\n\n");
			if (result != null) {
				html.append("<p class='error content'>" + result.message + "</p>\n\n");
			}
			if (msg != null) {
				html.append("<p class='success content'>" + msg + "</p>\n\n");
			}
			html.append(page.getContent(req, dict));
			html.append("</body>\n");
			html.append("</html>\n");
	
			ServletResponseTools.streamStringBuffer(html, "text/html", "UTF-8", 0.0, req, resp);
		}
	}

	private Page getRequestedPage(Data data, String pageStr) {
		Page page = null;
		if (pageStr == null) {
			pageStr = PageNames.PN_SGRAPH_FEDERATED;
		}
		if (pageStr.equals(ConfigPage.NAME)) {
			page = new ConfigPage();
		} else if (pageStr.equals(PageNames.PN_SGRAPH_FEDERATED)) {
			page = new FederatedSummaryGraphPage();
		} else if (pageStr.equals(PageNames.PN_SGRAPH_DEMO)) {
			page = new DemoPageQald4();
		}
		if (data == null) {
			page = new ConfigPage();
		}
		if (page == null) {
			page = new FederatedSummaryGraphPage();
		}
		return page;
	}

	private Data initApplicationAndLoadData(HttpServletRequest req, Data data) {
		// get docroot
		String docroot = req.getSession().getServletContext().getRealPath("/");
		if (docroot == null) {
			docroot = "./";
		}
		docroot = docroot.trim();
		if (!docroot.endsWith("/")) {
			docroot += "/";
		}

		// test two possible locations
		String webappDir = docroot + "WEB-INF/";
		String workDir = docroot;
		File autoInitFile = new File(Constants.FN_AUTO_INIT);
		if (!autoInitFile.exists()) {
			autoInitFile = new File(Constants.FN_AUTO_INIT);
		}
		if (autoInitFile.exists()) {
			try {

				// load properties
				Properties props = new Properties();
				props.load(new FileInputStream(autoInitFile));

				String dbvendor = props.getProperty("db.vendor");
				String schema = props.getProperty("db.schema");
				String username = props.getProperty("db.username");
				String password = props.getProperty("db.password");
				String dbname = props.getProperty("db.dbname");
				String hostname = props.getProperty("db.hostname");
				String port = props.getProperty("db.post");
				String sid = props.getProperty("db.sid");
				String dataDirPath = props.getProperty("data.dir");
				String configDirPath = props.getProperty("config.dir");

				// fix datadir path
				if (dataDirPath == null) {
					dataDirPath = "";
				}
				dataDirPath = dataDirPath.trim();
				if (dataDirPath.startsWith("/")) {
					dataDirPath = dataDirPath.substring(1);
				}

				// real datadir path
				dataDirPath = docroot + dataDirPath;
				
				// fix configdir path
				if (configDirPath == null) {
					configDirPath = "";
				}
				configDirPath = configDirPath.trim();
				if (configDirPath.startsWith("/")) {
					configDirPath = configDirPath.substring(1);
				}

				// real datadir path
				configDirPath = docroot + configDirPath;

				// init application
				Init.go(dbvendor, schema, username, password, dbname, hostname, port, sid, configDirPath, dataDirPath, false, false);

				// reset data variable
				data = DataSingleton.getInstance().getData();

			} catch (Exception e) {
				logger.error("auto-config could not be loaded: " + e.getMessage());
			}
		}
		return data;
	}

	private void executeAction(HttpServletRequest req, HttpServletResponse resp, Action action) {
		Result result;
		result = action.execute(req);
		// if everything was okay
		// send reload (retaining get parameters)
		if (result.success) {
			String parameters = GetRequest.reconstructParameters(req, result.message);
			String uri = req.getRequestURI();
			ServletResponseTools.sendReload(req, resp, uri + parameters);
		}
	}

	private Action checkForAndReturnAction(String actionStr) {
		Action action = null;
		if (actionStr != null) {
			if (actionStr.equals(ConfigAction.NAME)) {
				action = new ConfigAction();
			} else if (actionStr.equals(UpdateConfigAction.NAME)) {
				action = new UpdateConfigAction();
			} else if (actionStr.equals(ReloadClassificationIndexAction.NAME)) {
				action = new ReloadClassificationIndexAction();
			}
		}
		return action;
	}

}
