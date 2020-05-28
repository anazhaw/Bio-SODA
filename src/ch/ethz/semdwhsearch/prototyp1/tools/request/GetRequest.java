package ch.ethz.semdwhsearch.prototyp1.tools.request;

import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * Tools to help with a GET request.
 * 
 * @author Lukas Blunschi
 * 
 */
public class GetRequest implements Request {

	private Map<String, String> parameterMap;

	public GetRequest() {
		this.parameterMap = new HashMap<String, String>();
	}

	public GetRequest(HttpServletRequest req) {
		this();
		parse(req);
	}

	public String getParameter(String name) {
		return parameterMap.get(name);
	}

	// --------------------------------------------------------- helper methods

	public void parse(HttpServletRequest req) {
		parameterMap = GetRequest.getParameterMap(req);
	}

	/**
	 * Create map containing all GET parameters of the given request.
	 * 
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getParameterMap(HttpServletRequest req) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> enumNames = req.getParameterNames();
		while (enumNames.hasMoreElements()) {
			String name = enumNames.nextElement();
			String value = req.getParameter(name);
			map.put(name, value);
		}
		return map;
	}

	/**
	 * Reconstruct parameter string for given GET request.
	 * 
	 * @param req
	 * @param msg
	 *            message parameter to append (not added if null or
	 *            zero-length).
	 * @return parameter string, e.g. '?a=2'
	 */
	public static String reconstructParameters(HttpServletRequest req, String msg) {

		// get parameters
		Map<String, String> map = getParameterMap(req);

		// add message
		if (msg != null && msg.trim().length() > 0) {
			String prevMsg = map.get(Constants.P_MSG);
			if (prevMsg == null) {
				map.put(Constants.P_MSG, msg);
			} else {
				map.put(Constants.P_MSG, prevMsg + "_" + msg);
			}
		}

		// remove action and language parameters
		map.remove(Constants.P_ACTION);
		map.remove(Constants.A_LANG_CODE);

		// recreate parameters string
		StringBuffer parameters = new StringBuffer();
		try {
			boolean first = true;
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (first) {
					first = false;
					parameters.append("?");
				} else {
					parameters.append("&");
				}
				String encKey = URLEncoder.encode(entry.getKey(), "utf-8");
				String encValue = URLEncoder.encode(entry.getValue(), "utf-8");
				parameters.append(encKey).append("=").append(encValue);
			}
		} catch (Exception e) {
			// should never happen since utf-8 is known
		}
		return parameters.toString();
	}

}
