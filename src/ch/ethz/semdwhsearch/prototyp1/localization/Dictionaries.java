package ch.ethz.semdwhsearch.prototyp1.localization;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * Tools to access dictionaries.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Dictionaries {

	private static final Map<String, Dictionary> dictionaryMap;

	static {
		dictionaryMap = new TreeMap<String, Dictionary>();
		dictionaryMap.put(English.LANGCODE, new English());
	}

	public static Collection<String> getLanguageCodes() {
		return dictionaryMap.keySet();
	}

	public static Dictionary getDictionary(String langcode) {
		return dictionaryMap.get(langcode);
	}

	public static Dictionary getDictionaryFromSession(HttpServletRequest req) {
		String langCodeStr = (String) req.getSession().getAttribute(Constants.A_LANG_CODE);
		if (langCodeStr == null) {
			// english is default
			return getDictionary(English.LANGCODE);
		} else {
			return getDictionary(langCodeStr);
		}
	}
}
