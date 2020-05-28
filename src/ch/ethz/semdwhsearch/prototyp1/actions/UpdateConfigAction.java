package ch.ethz.semdwhsearch.prototyp1.actions;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.actions.results.Failure;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Result;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Success;
import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.config.ConfigSingleton;
import ch.ethz.semdwhsearch.prototyp1.tools.request.PostRequest;

/**
 * An action to update the configuration of the system.
 * 
 * @author Lukas Blunschi
 * 
 */
public class UpdateConfigAction implements Action {

	public static final String NAME = "updateConfig";

	public Result execute(HttpServletRequest req) {

		// parse POST request
		PostRequest postReq = new PostRequest();
		try {
			postReq.parse(req, null, false);
		} catch (Exception e) {
			return new Failure("Could not parse post request!");
		}

		// config
		Config config = ConfigSingleton.getInstance().getConfig();
		Map<String, String> props = config.getAllProperties();

		// loop over all properties and check if update required
		boolean changed = false;
		for (Map.Entry<String, String> entry : props.entrySet()) {
			String propName = entry.getKey();
			String propValue = entry.getValue();

			// check given values
			String valueGiven = postReq.getParameter(propName);
			if (valueGiven != null && !propValue.equals(valueGiven)) {
				config.setProperty(propName, valueGiven);
				changed = true;
			}
		}

		// persist if needed
		if (changed) {
			config.persist();
		}

		return new Success();
	}

}
