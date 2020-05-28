package ch.zhaw.nlp;

/**
 * Tokenizer.
 *
 * @author Lukas Blunschi
 *
 */
public class Tokenizer {

	private static final String WORD_SEPARATOR = " ";

	private static final String TERM_SPLITTER = "\\s+";

	public Tokenizer() {
	}

	public String[] split(String input) {
		return input.split(TERM_SPLITTER);
	}

	public String[] splitToTokens(String input) {
		return input.toLowerCase().split(TERM_SPLITTER);
	}

	public String splitToKey(String input) {
		String[] tokens = splitToTokens(input);
		return concat(tokens, tokens.length, 0);
	}

	public String[] splitToTokensAndKey(String input) {
		String[] tokens = splitToTokens(input);
		if (tokens.length > 1) {
			String[] tokensAndKey = new String[tokens.length + 1];
			System.arraycopy(tokens, 0, tokensAndKey, 0, tokens.length);
			tokensAndKey[tokens.length] = concat(tokens, tokens.length, 0);
			return tokensAndKey;
		} else {
			return tokens;
		}
	}

	public String concat(String[] words, int length, int pos) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				buf.append(WORD_SEPARATOR);
			}
			buf.append(words[pos + i]);
		}
		return buf.toString();
	}

}
