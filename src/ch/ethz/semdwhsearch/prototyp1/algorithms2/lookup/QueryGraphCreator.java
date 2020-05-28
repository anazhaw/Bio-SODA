package ch.ethz.semdwhsearch.prototyp1.algorithms2.lookup;

import ch.ethz.rdf.dag.RdfDagNode;
import ch.ethz.semdwhsearch.prototyp1.algorithms.querygraphgeneration.TermCombinations;
import ch.ethz.semdwhsearch.prototyp1.classification.Match;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.querygraph.QueryGraph;
import ch.zhaw.nlp.TokenList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The query graph creator.
 * <p>
 * This class creates query graphs from the given matches.
 * 
 * @author Lukas Blunschi
 * 
 */
public class QueryGraphCreator {

	private final static Logger logger = LoggerFactory.getLogger(QueryGraphCreator.class);

	private final Metadata metadata;

	public QueryGraphCreator(Metadata metadata) {
		this.metadata = metadata;
	}

	public List<RdfDagNode> create(TokenList tokens, List<Match> matches) {

		// compute term combinations
		List<LinkedList<Term>> termCombinations = new TermCombinations().getTermCombinations(matches);

		// create one or several RDF dag per term combination
		List<RdfDagNode> queryGraphs = new ArrayList<RdfDagNode>();

		// loop over term combinations
		final int combinationsCount = termCombinations.size();
		final int maxCount = Constants.MAX_RESULT_COUNT;
		int size = 0;
		for (int i = 0; i < combinationsCount && size < maxCount; i++) {
			LinkedList<Term> termCombination = termCombinations.get(i); //(int)(Math.random() * combinationsCount )); //replace this with first of first term, then first of second etc.

			// compute query graphs and
			// add to final result
			List<QueryGraph> queryGraphsCur = new QueryGraphCreatorTerms(termCombination, metadata).addTerms(tokens);
			for (QueryGraph queryGraph : queryGraphsCur) {
				queryGraphs.add(queryGraph.getRdf());
			}
			
			size++;
		}

		// log warning
		if (combinationsCount > maxCount) {
			logger.warn("Combinations: "+ termCombinations);
			logger.warn("max result count reached (ignoring " + (combinationsCount - maxCount) + " term combinations.");
		}

		return queryGraphs;
	}

}
