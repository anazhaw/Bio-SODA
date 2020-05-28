package ch.ethz.rdf.tools;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class to extract text words from a given XML file.
 * 
 * @author Lukas Blunschi
 * 
 */
public class XmlTextExtractor extends DefaultHandler {

	private Set<String> words;

	private StringBuffer buf;

	public XmlTextExtractor() {
		this.words = new HashSet<String>();
		this.buf = new StringBuffer();
	}

	public Set<String> getWords(File file) throws Exception {

		// create handler
		XmlTextExtractor handler = new XmlTextExtractor();

		// Parse the input with the default (non-validating) parser
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(file, handler);

		// return words
		return words;
	}

	// ------------------------------------------------------ handler overrides

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		buf = new StringBuffer();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (buf.length() > 0) {
			String word = buf.toString().trim();
			words.add(word);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String tmp = String.valueOf(ch, start, length).trim();
		if (tmp.length() > 0) {
			buf.append(tmp);
		}
	}

}
