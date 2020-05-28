package ch.ethz.semdwhsearch.prototyp1.classification.index;

import java.sql.SQLException;
import java.util.List;

import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.zhaw.nlp.TokenList;

/**
 * Our index interface.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Index {

	// ------------------------------------------------------------------- read

	/**
	 * Lookup terms for given key.
	 * 
	 * @param key
	 *            key to lookup terms (simplified version of value).
	 * @return all terms stored for given key.
	 */
	List<Term> lookup(TokenList tokens);

	/**
	 * Lookup terms for given key prefix.
	 * 
	 * @param keyPrefix
	 *            prefix of key to lookup terms for (the key is a simplified
	 *            version of the value).
	 * @return all terms stored for matching keys.
	 */
	List<Term> lookupPrefix(TokenList tokens);

	/**
	 * Lookup terms for given substring of key.
	 * 
	 * @param keySubstring
	 *            substring of key to lookup terms for (the key is a simplified
	 *            version of the value).
	 * @return all terms stored for matching keys.
	 */
	List<Term> lookupSubstring(TokenList tokens);

	int size();

	// ------------------------------------------------------------------ write

	/**
	 * Add given term for given key.
	 * 
	 * @param key
	 *            key to store term under (simplified version of value).
	 * @param term
	 *            term to store.
	 */
	void add(String key, Term term);

	/**
	 * Add given term for given key (optimization method to avoid having to
	 * create a Term object for each call).
	 * 
	 * @param key
	 *            key to store term under (simplified version of value).
	 * @param term
	 *            term to store.
	 */
	void add(String key, TermType type, String value, TermOrigin origin, String originName);

	/**
	 * Remove given term for given key.
	 * 
	 * @param key
	 *            key to store term under (simplified version of value).
	 * @param term
	 *            term to remove.
	 */
	void remove(String key, Term term);

	// ------------------------------------------------------------------- fast

	void beginFast() throws SQLException;

	void fastAdd(String key, TermType type, String value, TermOrigin origin, String originName) throws SQLException;

	void fastExecuteBatch() throws SQLException;

	void fastCommit() throws SQLException;

	void endFast() throws SQLException;

}
