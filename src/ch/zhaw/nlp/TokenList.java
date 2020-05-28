package ch.zhaw.nlp;

import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A list of tokens, including the {@code SemanticGraph}.
 *
 * @author Katrin Affolter
 */
public class TokenList {
    final String delimiter = " ";
    private List<Token> tokens = new ArrayList<Token>();
    private List<SemanticGraph> dependencyTrees = new ArrayList<SemanticGraph>();
    private String query = "";

    public TokenList() {
        initQuery();
    }

    public TokenList(List<Token> tokens) {
        super();
        this.tokens = tokens;
    }

    public void initQuery() {
        StringBuilder query = new StringBuilder();
        Token prevToken = null;

        for (Token token : tokens) {
            if (prevToken != null) {
                if (token.getOffsetBegin() - prevToken.getOffsetEnd() > 0) {
                    query.append(" ");
                }
            }
            query.append(token.getText());
            prevToken = token;
        }
        this.query = query.toString();
    }

    public String getQuery() {
        return query;
    }

    public void addDependencyTree(SemanticGraph tree) {
        dependencyTrees.add(tree);
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public Token getToken(int index) {
        return tokens.get(index);
    }

    /**
     * remove token at {@code index} from the list
     * (does not affect dependency tree)
     *
     * @param index the index of the token to be removed
     */
    private void removeToken(int index) {
        tokens.remove(index);
    }

    /**
     * @return list of {@link Token}
     */
    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * @return list of all non stop word tokens
     */
    public List<Token> getNonStopwordTokens() {
        return tokens.stream()
                .filter(token -> !token.isStopword())
                .collect(Collectors.toList());
    }

    /**
     * @return set of all tokens (text)
     */
    public Set<String> getUniqueTokenTextSet() {
        return tokens.stream()
                .map(token -> token.getText().toLowerCase())
                .collect(Collectors.toSet());
    }

    /**
     * @return set of all lemmas
     */
    public Set<String> getUniqueTokenLemmaSet() {
        return tokens.stream()
                .map(token -> token.getLemma().toLowerCase())
                .collect(Collectors.toSet());
    }

    /**
     * @return tokenized text
     */
    public String getTokenizedText() {
        return tokens.stream()
                .map(Token::getText)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * @return tokenized lemmas
     */
    public String getLemmatizedText() {
        return tokens.stream()
                .map(Token::getLemma)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * @return set of all POS tags
     */
    public Set<String> getUniquePOSTags() {
        return tokens.stream()
                .map(token -> token.getPOS())
                .collect(Collectors.toSet());
    }

    /**
     * @return tokenized non stop word text
     */
    public String getNonStopwordText() {
        return tokens.stream()
                .filter(token -> !token.isStopword())
                .map(Token::getText)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * a token list is a key for lookup if it does not start or end with a stop word
     *
     * @return boolean
     */
    public boolean isKeyForLookup() {
        return !(getNonStopwordTokens().size() == 0 || getToken(0).isStopword() || getToken(tokens.size() - 1).isStopword());
    }

    /**
     * get all possible lookup keys of the token list.
     *
     * @param addUniGrams defines if uni grams are lookup keys
     * @return set of lookup key of the token list
     */
    public Set<String> getLookupKeys(boolean addUniGrams) {
        Set<String> lookupKeys = new HashSet<>();

        
        // add uni grams
        if (addUniGrams && size() > 1) {
            for (Token token : getNonStopwordTokens()) {

                //HERE ADD SPLITTING CAMEL CASE STRINGS for indexing!
            	
                lookupKeys.add(token.getText());
                if (!token.getLemma().equalsIgnoreCase(token.getText())) {
                    lookupKeys.add(token.getLemma());
                }
            }
        }

        // add lookup key of this token list
        TokenList lookupTokens = isKeyForLookup() ? new TokenList(new ArrayList<Token>(tokens)) : reduceToLookupKey();
        String tokenizedText = lookupTokens.getTokenizedText();
        String lemmatizedText = lookupTokens.getLemmatizedText();

        lookupKeys.add(tokenizedText);
        if (!lemmatizedText.equalsIgnoreCase(tokenizedText)) {
            lookupKeys.add(lemmatizedText);
        }

        return lookupKeys;
    }

    /**
     * returns a token list that can be used as lookup key
     *
     * @return token list that can be used as lookup key
     */
    private TokenList reduceToLookupKey() {
        TokenList reducedTokens = new TokenList(new ArrayList<Token>(tokens));
        while (reducedTokens.size() > 0 && reducedTokens.getToken(0).isStopword()) {
            reducedTokens.removeToken(0);
        }
        while (reducedTokens.size() > 0 && reducedTokens.getToken(reducedTokens.size() - 1).isStopword()) {
            reducedTokens.removeToken(reducedTokens.size() - 1);
        }

        return reducedTokens.size() > 0 ? reducedTokens : this;
    }

    /**
     * returns index of {@code nGram} in the token list or -1
     *
     * @param nGram list of string to search for
     * @return index of {@code nGram} or -1
     */
    public int indexOf(List<String> nGram) {
        int length = nGram.size();
        for (int i = 0; i < tokens.size() - (length - 1); i++) {
            boolean result = true;
            for (int j = 0; j < length; j++) {
                if (!tokens.get(i + j).getText().equalsIgnoreCase(nGram.get(j))) {
                    result = false;
                }
            }
            if (result) {
                return i;
            }

        }
        return -1;
    }

    /**
     * index of {@code text} in the token list
     *
     * @param text {@link String} to search for
     * @return index of {@code text} or -1
     */
    public int indexOfText(String text) {
        return indexOfText(text, 0);
    }

    /**
     * index of {@code text} in the token list starting by {@code start}
     *
     * @param text  {@link String} to search for
     * @param start index of starting point
     * @return index of {@code text} or -1
     */
    public int indexOfText(String text, int start) {
        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).getText().equalsIgnoreCase(text)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * return new {@link TokenList} from {@code fromIndex} to {@code toIndex}
     *
     * @param fromIndex index where the new {@link TokenList} starts
     * @param toIndex   index where the new {@link TokenList} ends
     * @return new {@link TokenList}
     */
    public TokenList subList(int fromIndex, int toIndex) {
        TokenList subList = new TokenList();
        for (int i = fromIndex; i < toIndex && i < tokens.size(); i++) {
            subList.addToken(tokens.get(i));
        }
        return subList;
    }

    /**
     * @return number of tokens
     */
    public int size() {
        return tokens.size();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Token token : tokens) {
            if (str.length() > 0) {
                str.append("\n");
                if (token.isFirstOfSentence()) {
                    str.append("\n");
                }
            }
            str.append(token.toString());
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TokenList) {
            TokenList tokenList = (TokenList) obj;
            return tokenList.getTokenizedText().equals(this.getTokenizedText());
        } else {
            return false;
        }
    }
}
