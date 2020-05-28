package ch.ethz.semdwhsearch.prototyp1.algorithms2.lookup;

import ch.ethz.rdf.dag.RdfDagNode;
import ch.ethz.semdwhsearch.prototyp1.algorithms.queryclassification.LongestMatchClassify;
import ch.ethz.semdwhsearch.prototyp1.classification.Classification;
import ch.ethz.semdwhsearch.prototyp1.classification.Match;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.config.ConfigSingleton;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.zhaw.nlp.TokenList;

import java.util.ArrayList;
import java.util.List;

/**
 * A lookup implementation which parses the operators in the query string and
 * passes the remaining parts on to the next implementation.
 *
 * @author Lukas Blunschi
 */
public class OperatorsParsing implements LookupInterface {

    private final Classification classification;

    private final Metadata metadata;

    private List<Match> matches;

    public OperatorsParsing(Classification classification, Metadata metadata) {
        this.classification = classification;
        this.metadata = metadata;
        this.matches = null;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public List<RdfDagNode> lookup(TokenList tokens) {

        // config
        Config config = ConfigSingleton.getInstance().getConfig();

        // classify whole string
        matches = new LongestMatchClassify(classification, config).classify(tokens);

        // create query graphs and return
        QueryGraphCreator lookup = new QueryGraphCreator(metadata);
        return lookup.create(tokens, matches);
    }

}