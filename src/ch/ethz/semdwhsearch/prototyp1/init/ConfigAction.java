package ch.ethz.semdwhsearch.prototyp1.init;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.actions.Action;
import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Failure;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Result;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Success;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionaries;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.tools.request.PostRequest;

/**
 * An action to configure the system.
 * 
 * @author Lukas Blunschi
 */
public class ConfigAction implements Action {

	public static final String NAME = "config";

	public Result execute(HttpServletRequest req) {

		// parse POST request
		PostRequest postReq = new PostRequest();
		try {
			postReq.parse(req, null, false);
		} catch (Exception e) {
			return new Failure("Could not parse post request!");
		}

		// get parameters
		String dbvendor = postReq.getFormField(Params.DBVENDOR);
		String schema = postReq.getFormField(Params.SCHEMA);
		String username = postReq.getFormField(Params.USERNAME);
		String password = postReq.getFormField(Params.PASSWORD);
		String dbname = postReq.getFormField(Params.DBNAME);
		String hostname = postReq.getFormField(Params.HOSTNAME);
		String port = postReq.getFormField(Params.PORT);
		String sid = postReq.getFormField(Params.SID);
		String dataDirPath = postReq.getFormField(Params.DATADIR);
		String configDirPath = postReq.getFormField(Params.CONFIGDIR);
		String invertedIndexStr = postReq.getFormField(Params.INVERTEDINDEX);
		String appendToIndexStr = postReq.getFormField(Params.APPENDTOINDEX);

		// get docroot (e.g. /path/to/root/)
		String docroot = req.getSession().getServletContext().getRealPath("/");
		if (docroot == null) {
			docroot = "./";
		}
		docroot = docroot.trim();
		if (!docroot.endsWith("/")) {
			docroot = docroot + "/";
		}

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

		// reload index flag
		boolean reloadIdx = invertedIndexStr == null ? false : true;
		
		// append to index flag
		boolean appendIdx = appendToIndexStr == null ? false : true;

		// init application
		Init.go(dbvendor, schema, username, password, dbname, hostname, port, sid, configDirPath, dataDirPath, reloadIdx, appendIdx);

		// dictionary
		Dictionary dict = Dictionaries.getDictionaryFromSession(req);
		return new Success(dict.dataLoadingCompleted());

	}

}
