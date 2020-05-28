package ch.ethz.semdwhsearch.prototyp1.algorithms2.lookup;

import java.util.List;

import ch.ethz.rdf.dag.RdfDagNode;
import ch.zhaw.nlp.TokenList;

/**
 * The Lookup interface.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface LookupInterface {

	List<RdfDagNode> lookup(TokenList tokens);

}
