package ch.zhaw.nlp;

import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.*;
import java.util.*;

/**
 * natural language processing pipeline based on Stanford CoreNLP.
 *
 * @author Katrin Affolter, Ana Sima
 */
public class NlpPipeline {
    final private StanfordCoreNLP pipeline;

    private final Config config;

    private Set<String> stopwords = new HashSet<String>();

    final private Set<String> punctuations = new HashSet<String>(Arrays.asList("#", "$", ",", ".", ":", "``", "''", "-LRB-", "-RRB-"));

    private Word2Vec wordVectorModel = null;
    
    public NlpPipeline(Config config) {
        this.config = config;
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse");
        pipeline = new StanfordCoreNLP(props);
        loadStopwords();
        if(Constants.useWordEmbeddings)
        		wordVectorModel = WordVectorSerializer.readWord2VecModel(Constants.word2VecPath);
    }

    public Word2Vec getWordVectorModel() {
    		return this.wordVectorModel;
    }

    private void loadStopwords() {
        InputStream inputStream = NlpPipeline.class.getClassLoader().getResourceAsStream("stopwords.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){ 
            for (String line; (line = br.readLine()) != null; ) {
                stopwords.add(line.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TokenList annotate(String query) {
        query = query == null ? "" : query;
        //gerunzium...
        query = query.replaceAll("'", " ");
        //query = query.replaceAll("-", " ");
        TokenList tokens = runCoreNLP(query);
        identifyStopwords(tokens);
        return tokens;
    }

    private TokenList runCoreNLP(String query) {
        Annotation annotation = new Annotation(query);
        pipeline.annotate(annotation);
        return getTokenListOfAnnotation(annotation);
    }

    private TokenList getTokenListOfAnnotation(Annotation annotation) {
        TokenList tokenList = new TokenList();
        int index = 0;
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            tokenList.addDependencyTree(sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class));

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
			// we need special handling of percentage sign since Stanford NLP tokeniser creates a separate token for this, so things like 50% will appear disconnected
			// here we concatenate them back
			if(token.originalText().equals("%") && tokenList.size() > 0) {
            			Token lastToken = tokenList.getToken(tokenList.size() - 1);
            			lastToken.setText(lastToken.getText() + "%");
            			continue;
            		}
            if(token.originalText().length() == 1 &&
					(token.originalText().equals(">") 
					|| token.originalText().equals("<")
					|| token.originalText().equals("="))) {
				tokenList.addToken(new Token(++index, token));
			}
			if(token.originalText().length() > 1)
            			tokenList.addToken(new Token(++index, token));
            }
        }
        return tokenList;
    }

    private void identifyStopwords(TokenList tokens) {
        if (stopwords.size() == 0) {
            loadStopwords();
        }

        for (Token token : tokens.getTokens()) {
            token.setStopword(isStopword(token));
        }
    }
    
    public boolean isStopword(String word) {
        return stopwords.contains(word.toLowerCase());// || punctuations.contains(token.getPOS());
    }

    private boolean isStopword(Token token) {
        return stopwords.contains(token.getLemma().toLowerCase()) || punctuations.contains(token.getPOS());
    }

    public Set<String> getLookupKeys(String str) {
        boolean addUniGrams = config.isAlgoLookupSubstringMatching(false);
	return annotate(str).getLookupKeys(addUniGrams);
    }
}
