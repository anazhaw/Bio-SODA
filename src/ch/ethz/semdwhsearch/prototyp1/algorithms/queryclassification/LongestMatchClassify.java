package ch.ethz.semdwhsearch.prototyp1.algorithms.queryclassification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.classification.Classification;
import ch.ethz.semdwhsearch.prototyp1.classification.Match;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.tools.ArrayListPool;
import ch.ethz.semdwhsearch.prototyp1.tools.Tokenizer;
import ch.zhaw.biosoda.SPARQLUtilsRemote;
import ch.zhaw.nlp.NlpPipeline;
import ch.zhaw.nlp.Token;
import ch.zhaw.nlp.TokenList;

/**
 * A classify algorithm which performs a longest match classification.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public class LongestMatchClassify {

	private final static Logger logger =
			LoggerFactory.getLogger(LongestMatchClassify.class);

	private final Classification classification;

	private final Tokenizer tokenizer;

	private final ArrayListPool<Match> arrayListPool;

	private final boolean substringMatching;

	private final boolean prefixMatching;

	private final Config config;

	public LongestMatchClassify(Classification classification, Config config) {
		this.classification = classification;
		this.tokenizer = classification.getTokenizer();
		this.arrayListPool = new ArrayListPool<Match>();
		this.substringMatching = config.isAlgoLookupSubstringMatching(false);
		this.prefixMatching = config.isAlgoLookupPrefixMatching(true);
		this.config = config;
	}

	// ------------------------------------------- classify algorithm interface

	public List<Match> classify(TokenList tokens) {

		// classify
		List<Match> matches = classifyBreadthFirst(tokens, 0, tokens.size());
		// remove duplicates
		for (Match match : matches) {
			match.removeDuplicates();

			HashSet<Term> toRemoveMatches = new HashSet<Term>();

			for(Term term: match.getTerms()) {
				if((term.filteredClass == null || term.filteredClass.equals("null")) && (!config.isExcludedFromLookup(term.originName)) ) {
					String className = SPARQLUtilsRemote.getTypeOfResource(Constants.REMOTE_REPO, term.originName);
					if(className != null) {
						term.filteredClass = className;
						if(className.contains("Property"))
							term.pageRank = Constants.PROPERTY_PAGERANK;
					}
				}

				if(term.pageRank <= Constants.DEFAULT_PAGERANK || config.isExcludedFromLookup(term.originName)){ //TODO: add FIX FOR PROPERTIES
					toRemoveMatches.add(term);
					continue;
				}
			}

			match.getTerms().removeAll(toRemoveMatches);
			match.limitResults();
			match.sortByBestMatch();
		}

		//FINAL STEP: remove matches that DO not have good similarity scores

		NlpPipeline nlpPipeline = classification.getNlpPipeline();


		for(Match match: matches) {
			HashSet<Term> toRemoveMatches = new HashSet<Term>();

			if(Constants.useWordEmbeddings) {
				for(Term term: match.getTerms()) {

					Set<String> keys = nlpPipeline.getLookupKeys(term.key);
					/*if(keys.size() > 1) { //this can happen e.g. for plurals: genes -> gene, genes
					logger.info("Skipping multi-word key " + keys);
					continue;
					}*/

					String key = null;
					boolean spuriousMatch = false;

					for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
						key  = it.next().toLowerCase();

						if(!nlpPipeline.getWordVectorModel().vocab().containsWord(key)){
							//NOTE: here we used to have "continue". but actually it's better to just move on to next term
							// e.g. for penicillin g, considering only "g"  as a key is a bad idea
							logger.info("Pipeline does not contain " + key + " breaking from loop on " + keys);
							break;
						}
						for(String word : nlpPipeline.getLookupKeys(term.value)) {
							logger.info("Lookup key from value: " + word);
							if(word.split(" ").length > 1)
								word = word.split(" ")[0];
							if(!nlpPipeline.getWordVectorModel().vocab().containsWord(word)){
								logger.info("Pipeline does not contain " + word);
								//NOTE: here we used to have "continue". but actually it's better to just move on to next term
								// e.g. for penicillin g, considering only "g"  as a key is a bad idea
								break;
							}
							//logger.info("Looking for similarity: " + word + "  and " + key);	
							if(word.toLowerCase().contains(key)) {
								double similarity = nlpPipeline.getWordVectorModel().similarity(key, word);
								if(Double.isNaN(similarity) ||((!Double.isNaN(similarity)) && (Math.abs(similarity) > Constants.thresholdWord2VecSimilarity))){
									spuriousMatch = false;
									break;
								}
								else
									spuriousMatch = true;
								logger.info("SIMILARITY "+ term.key + " and "+ term.value + " based on "+  word + " is "+
										similarity + " spurious? "+ spuriousMatch);
							}
						}}

					if(spuriousMatch){
						logger.info("Removing spurious: " + term);
						toRemoveMatches.add(term);
					}
				}
			}
			match.getTerms().removeAll(toRemoveMatches);

		}


		logger.info("\n\n\n####MATCHES : "+ matches +  "\n\n\n\n\n\n");

		return matches;
	}

	private ArrayList<Match> classifyBreadthFirst(TokenList tokens, int from, final int to) {
		ArrayList<Match> matches = arrayListPool.getArrayList();

		// loop over decreasing number of words
		final int wordCount = to - from;
		String number = null;

		for(Token tok : tokens.getTokens())
			if(tok.getText().matches("-?\\d+(\\.\\d+)?"))
				number = tok.getText();

		for (int numWords = wordCount; numWords > 0; numWords--) {

			// loop over all possible positions
			for (int pos = from; pos < from + wordCount - numWords + 1; pos++) {

				// create lookup tokens
				TokenList keyTokens = tokens.subList(pos, pos + numWords);
				if (!keyTokens.isKeyForLookup()) {
					continue;
				}


				boolean negateNext = false;
				if(keyTokens.size() > 0){
					//special handling of numbers
					if(keyTokens.getToken(0).getText().matches("-?\\d+(\\.\\d+)?")) {
						keyTokens = keyTokens.subList(1, keyTokens.size());
					}
					//special handling of negations
					else if(keyTokens.getToken(0).getText().equals("no") || keyTokens.getToken(0).getText().equals("not") || keyTokens.getToken(0).getText().equals("without")) {
						negateNext = true;
						keyTokens = keyTokens.subList(1, keyTokens.size());
						from += 1;
					}
				}

				if(keyTokens.size() == 0)
					continue;

				List<Term> termsDict = null;
				if (substringMatching) {
					termsDict = classification.lookupSubstring(keyTokens);
				} else if (prefixMatching) {
					termsDict = classification.lookupPrefix(keyTokens);
				} else {
					termsDict = classification.lookup(keyTokens);
				}

				if (termsDict.size() > 0) {
					// left side
					ArrayList<Match> left = classifyBreadthFirst(tokens, from, pos);
					matches.addAll(left);
					arrayListPool.returnArrayList(left);

					// middle
					Match match = new Match(termsDict);
					if(negateNext) {
						for(Term term : match.getTerms()){
							term.setNegated();
							logger.info("Setting negated on term: " +  term);
						}
					}

					matches.add(match);

					Term numericalMatch = null;
					Term numericalProp = null;
					LinkedList<Term> numericalTerms = new LinkedList<Term>();
					if(number != null) {
						for(Term term : match.getTerms()) {
							if(term.value.matches("-?\\d+(\\.\\d+)?"))
								continue;
							numericalProp = null;
							if(term.filteredClass != null && (!term.filteredClass.equals("null")) && term.filteredClass.contains("Property")){
								HashSet<String> rangeClasses = SPARQLUtilsRemote.getRangeOfPropertyRemote("<"+term.originName+">", Constants.REMOTE_REPO);
								for(String rangeClass : rangeClasses){
									if(rangeClass.contains("integer") || rangeClass.contains("decimal") || rangeClass.contains("double")) {
										numericalProp = term;
									}
								}
								/* unfortunately not all schemas are nicely defined... */
								if(rangeClasses.size() == 0)
									numericalProp = term;
								boolean matchingTermFound = false;
								if(numericalProp != null)
									for(int i = pos + numWords ; i < tokens.size(); i++) {
										Token tok = tokens.getToken(i);
										if(matchingTermFound)
											break;
										if(tok.getText().matches("-?\\d+(\\.\\d+)?")) {
											number = tok.getText();
											logger.info("Adding numerical match on " + numericalProp + " number: "+ number);
											numericalMatch = new Term(numericalProp.type, number, number, numericalProp.origin, numericalProp.originName, numericalProp.pageRank);
											numericalMatch.setClassProp(numericalProp.filteredClass, numericalProp.filteredProp);
											numericalTerms.add(numericalMatch);
											matchingTermFound = true;
											break;
										}
									}

							}}}

					if(numericalTerms.size() > 0)
						matches.add(new Match(numericalTerms));

					// right side
					ArrayList<Match> right = classifyBreadthFirst(tokens, pos + numWords, to);
					matches.addAll(right);
					arrayListPool.returnArrayList(right);

					return matches;
				}

			}
		}

		// handle unknown words
		if (wordCount > 0) {
			for (int pos = from; pos < from + wordCount; pos++) {
				if(pos == tokens.size())
					break;
				Token token = tokens.getToken(pos);
				if (!token.isStopword()) {
					Term term = new Term(TermType.UNKNOWN, token.getText(), TermOrigin.UNKNOWN, null);
					Match match = new Match(term);
					matches.add(match);
				}
			}
		}
		return matches;
	}

}
