package ch.ethz.semdwhsearch.prototyp1.tools;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.rdf.dag.RdfDagNode;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * A collection of tools to work with NTriples.
 * 
 * @author Lukas Blunschi
 * 
 */
public class NTriples {

	private static final Logger logger = LoggerFactory.getLogger(NTriples.class);

	/**
	 * Concatenate the given list of N-Triples.
	 * 
	 * @param nTriplesList
	 * @return single string which contains all given N-Triples strings
	 *         separated by 3 empty lines.
	 */
	public static String concat(List<String> nTriplesList) {
		StringBuffer buf = new StringBuffer();
		final int size = nTriplesList.size();
		final int maxCount = Constants.MAX_RESULT_COUNT;
		for (int i = 0; i < size && i < maxCount; i++) {
			buf.append(nTriplesList.get(i)).append("\n\n\n");
		}
		if (size > maxCount) {
			logger.warn("max result count reached (ignoring " + (size - maxCount) + " results).");
		}
		return buf.toString();
	}

	/**
	 * Concatenate the given list of RDF-DAGs by first converting them to
	 * N-Triples. Use the first 'count' DAGs only.
	 * 
	 * @param dagsList
	 * @param count
	 *            number of DAGs to concatenate.
	 * @return single string which contains all given N-Triples strings
	 *         separated by 3 empty lines.
	 */
	public static String concatDags(List<RdfDagNode> dagsList, int count) {
		List<RdfDagNode> dagsListCounted = null;
		if (dagsList.size() > count) {
			dagsListCounted = dagsList.subList(0, count);
		} else {
			dagsListCounted = dagsList;
		}
		return concatDags(dagsListCounted);
	}

	/**
	 * Concatenate the given list of RDF-DAGs by first converting them to
	 * N-Triples.
	 * 
	 * @param dagsList
	 * @return single string which contains all given N-Triples strings
	 *         separated by 3 empty lines.
	 */
	public static String concatDags(List<RdfDagNode> dagsList) {
		StringBuffer buf = new StringBuffer();
		final int size = dagsList.size();
		final int maxCount = Constants.MAX_RESULT_COUNT;
		for (int i = 0; i < size && i < maxCount; i++) {
			buf.append(dagsList.get(i).toNTriples()).append("\n\n\n");
		}
		if (size > maxCount) {
			logger.warn("max result count reached (ignoring " + (size - maxCount) + " results).");
		}
		return buf.toString();
	}

}
