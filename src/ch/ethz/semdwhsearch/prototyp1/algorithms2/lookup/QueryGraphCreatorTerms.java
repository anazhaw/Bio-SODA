package ch.ethz.semdwhsearch.prototyp1.algorithms2.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.classification.TermLookup;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.querygraph.QueryGraph;
import ch.zhaw.nlp.TokenList;

/**
 * Query graph creator which takes into account how many times a URI is found
 * for a given term.
 *
 * @author Lukas Blunschi
 */
public class QueryGraphCreatorTerms {

    private final static Logger logger = LoggerFactory.getLogger(QueryGraphCreatorTerms.class);

    private final LinkedList<Term> termCombination;

    private final Metadata metadata;

    public QueryGraphCreatorTerms(LinkedList<Term> termCombination, Metadata metadata) {
        this.termCombination = termCombination;
        this.metadata = metadata;
    }

    public List<QueryGraph> addTerms(TokenList tokens) {
        List<QueryGraph> result = new ArrayList<QueryGraph>();

        // BOs and VALs term lookup map
        Map<Term, Set<String>> termLookupMap = new HashMap<Term, Set<String>>();

        // 1. compute number of query graphs
        // - operators and unknown both produce 1 option only
        final int numTerms = termCombination.size();
        int[] options = new int[numTerms];
        int countOptions = 1;
        for (int ti = 0; ti < numTerms; ti++) {
            Term term = termCombination.get(ti);
            if (term.type == TermType.UNKNOWN) {
                options[ti] = 1;
                countOptions *= 1;
            } else {
                Set<String> uris = termLookupMap.get(term);
                if (uris == null) {
                    List<String> uriList = new TermLookup(metadata, term).getUri();
                    uris = new TreeSet<String>(uriList);
                    termLookupMap.put(term, uris);
                }
                if (uris.size() == 0) {
                    logger.warn("term lookup failed: " + term);
                }
                options[ti] = uris.size();
                countOptions *= uris.size();
            }
        }
        final int numQueryGraphs = countOptions;
        logger.info("Term combination produces " + numQueryGraphs + " query graphs.");

        // produce each query graph after each other
        for (int i = 0; i < numQueryGraphs; i++) {

            // create query graph
            QueryGraph.resetCounters();
            QueryGraph queryGraph = new QueryGraph();

            // remember original query
            queryGraph.setQuery(tokens.getQuery());

            // create one node per term
            for (int ti = 0; ti < numTerms; ti++) {
                Term term = termCombination.get(ti);

                // switch on term type
                if (term.type == TermType.UNKNOWN) {
                    queryGraph.addUnknown(term.value);
                } else {

                    // find uri
                    String uri = null;
                    Set<String> uris = termLookupMap.get(term);
                    int divisor = 1;
                    for (int j = numTerms - 1; j > ti; j--) {
                        divisor *= options[j];
                    }
                    int index = (i / divisor) % options[ti];
                    Iterator<String> iter = uris.iterator();
                    for (int m = 0; m < index; m++) {
                        iter.next();
                    }
                    uri = iter.next();

                    // add BO or VAL
                    if (term.type == TermType.RDF_MATCH) {
                        queryGraph.addBusinessObject(term.key, term.value, term.originName, term.filteredClass, term.filteredProp, term.pageRank, term.isNegated(), term.operator);
                    } else if (term.type == TermType.VALUE) {
                        queryGraph.addValue(term.key, term.value, uri);
                    } else {
                        logger.warn("Unknown term type!");
                    }
                }
            }

            // ensure at least one BO or VAL
            boolean hasBo = queryGraph.getBusinessObjects().size() > 0;
            boolean hasVal = queryGraph.getValues().size() > 0;
            if (hasBo || hasVal) {

                // add to result
                result.add(queryGraph);
            }
        }

        return result;
    }

}
