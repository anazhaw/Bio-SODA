package ch.zhaw.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * A token consists of {@link CoreLabel} and additional information like {@code isStopword}.
 *
 * @author Katrin Affolter
 */
public class Token {
    private int index;
    private CoreLabel token;
    private boolean isStopword;

    public Token(int index, CoreLabel token) {
        this.index = index;
        this.token = token;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return token.get(CoreAnnotations.OriginalTextAnnotation.class);
    }

    public String getLemma() {
        return token.get(CoreAnnotations.LemmaAnnotation.class);
    }

    public void setText(String text) {
    		token.setOriginalText(text);
    		token.setLemma(text);
    }

    public String getPOS() {
        return token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
    }

    public boolean isStopword() {
        return isStopword;
    }

    public void setStopword(boolean stopword) {
        isStopword = stopword;
    }

    public boolean isFirstOfSentence() {
        return token.get(CoreAnnotations.IndexAnnotation.class) == 1;
    }

    public int getOffsetBegin() {
        return token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
    }

    public int getOffsetEnd() {
        return token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
    }

    @Override
    public String toString() {
        return index + "\t" + getText() + "\t" + getLemma() + "\t" + getPOS() + "\t" + isStopword();
    }
}
