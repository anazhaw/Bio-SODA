package ch.ethz.semdwhsearch.prototyp1.classification.index.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.classification.index.Index;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.zhaw.nlp.TokenList;

/**
 * An index based on a hash map.
 * 
 * @author Lukas Blunschi
 * 
 */
public class HashMapIndex implements Index {

	private static final Logger logger = LoggerFactory.getLogger(HashMapIndex.class);

	private final Map<String, List<Term>> dictionary;

	public HashMapIndex() {
		this.dictionary = new HashMap<String, List<Term>>();
	}

	// -------------------------------------------------------- index interface

	 public List<Term> lookup(TokenList tokens) {
	        return dictionary.get(tokens.getNonStopwordText());
	    }

	    public List<Term> lookupPrefix(TokenList tokens) {
	        String keyPrefix = tokens.getNonStopwordText();
	        // not a very good implementation :-)
	        List<Term> result = new ArrayList<Term>();
	        for (Map.Entry<String, List<Term>> entry : dictionary.entrySet()) {
	            if (entry.getKey().startsWith(keyPrefix)) {

	                // create new terms having key prefix as key
	                // see impl in DbTableIndex!
	                List<Term> storedTerms = entry.getValue();
	                for (Term t : storedTerms) {
	                    result.add(new Term(t.type, keyPrefix, t.value, t.origin, t.originName));
	                }
	            }
	        }
	        return result;
	    }

	    public List<Term> lookupSubstring(TokenList tokens) {
	        String keySubstring = tokens.getNonStopwordText();
	        // not a very good implementation :-)
	        List<Term> result = new ArrayList<Term>();
	        for (Map.Entry<String, List<Term>> entry : dictionary.entrySet()) {
	            if (entry.getKey().indexOf(keySubstring) >= 0) {

	                // create new terms having key substring as key
	                // see impl in DbTableIndex!
	                List<Term> storedTerms = entry.getValue();
	                for (Term t : storedTerms) {
	                    result.add(new Term(t.type, keySubstring, t.value, t.origin, t.originName));
	                }
	            }
	        }
	        return result;
	    }

	public int size() {
		return dictionary.size();
	}

	public void add(String key, Term term) {
		List<Term> terms = dictionary.get(key);
		if (terms == null) {
			terms = new ArrayList<Term>();
			dictionary.put(key, terms);
		}
		// TODO check if term already exists
		terms.add(term);
	}

	public void add(String key, TermType type, String value, TermOrigin origin, String originName) {
		add(key, new Term(type, key, value, origin, originName));
	}

	public void remove(String key, Term term) {

		// lookup mapping
		List<Term> terms = dictionary.get(key);
		if (terms == null) {
			logger.warn("No match in dictionary");
			return;
		}

		// collect terms to remove
		List<Term> termsToRemove = new ArrayList<Term>();
		for (Term termCur : terms) {
			if (termCur.value.equalsIgnoreCase(term.value) && termCur.type.equals(term.type)
					&& termCur.origin.equals(term.origin)) {
				termsToRemove.add(termCur);
			}
		}

		// check for duplicates
		if (termsToRemove.size() > 1) {
			logger.warn("Classification contains duplicate term = " + termsToRemove.get(0).value);
		}

		// check if term was found
		if (termsToRemove.size() == 0) {
			logger.warn("No matching term in Classification found!");
			return;
		}

		// remove term
		terms.remove(termsToRemove.get(0));

		// remove term list from dictionary if empty
		if (terms.size() == 0) {
			dictionary.remove(key);
		}
	}

	public void beginFast() {
		// nothing to do
	}

	public void fastAdd(String key, TermType type, String value, TermOrigin origin, String originName) {
		add(key, type, value, origin, originName);
	}

	public void fastExecuteBatch() throws SQLException {
		// nothing to do
	}

	public void fastCommit() throws SQLException {
		// nothing to do
	}

	public void endFast() {
		// nothing to do
	}

}
