package ch.ethz.semdwhsearch.prototyp1.actions;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.actions.results.Failure;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Result;
import ch.ethz.semdwhsearch.prototyp1.actions.results.Success;
import ch.ethz.semdwhsearch.prototyp1.classification.Classification;
import ch.ethz.semdwhsearch.prototyp1.classification.ClassificationSingleton;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.data.Data;
import ch.ethz.semdwhsearch.prototyp1.data.DataSingleton;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionaries;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;
import ch.ethz.semdwhsearch.prototyp1.tools.request.PostRequest;

/**
 * An action to reload a classification index.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ReloadClassificationIndexAction implements Action {

	public static final String NAME = "reloadClassificationIndex";

	public Result execute(HttpServletRequest req) {

		// parse POST request
		PostRequest postReq = new PostRequest();
		try {
			postReq.parse(req, null, false);
		} catch (Exception e) {
			return new Failure("Could not parse post request!");
		}

		// get parameters
		String metadataIndexStr = postReq.getFormField(Constants.P_METADATA_INDEX);

		// use metadata index
		boolean metadataIndex = metadataIndexStr == null ? false : true;

		// reload
		Metadata metadata = MetadataSingleton.getInstance().getMetadata();
		Data data = DataSingleton.getInstance().getData();
		Classification classification = ClassificationSingleton.getInstance().getClassification();
		classification.reloadIndex(metadataIndex, metadata, data, false);

		// result
		Dictionary dict = Dictionaries.getDictionaryFromSession(req);
		String msg = null;
		if (metadataIndex) {
			msg = dict.reloadMetadataClassificationIndex();
		} else {
			msg = dict.reloadBaseDataClassificationIndex();
		}
		return new Success(msg);
	}

}
