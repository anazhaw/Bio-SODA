package ch.ethz.rdf.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A little tool to extract attribute names from RDF files (works for XML in
 * general).
 * 
 * @author Lukas Blunschi
 * 
 */
public class RdfAttrNameExtractor extends DefaultHandler {

	private Set<String> localNames = new HashSet<String>();

	private Set<String> qNames = new HashSet<String>();

	/**
	 * Standalone app entry point.
	 * 
	 * @param args
	 *            filename
	 */
	public static void main(String[] args) throws Exception {

		// check arguments
		if (args.length != 1) {
			System.err.println("Usage: app <filename>");
			return;
		}

		// get input file
		File file = new File(args[0]);

		// Use an instance of ourselves as the SAX event handler
		RdfAttrNameExtractor handler = new RdfAttrNameExtractor();

		// Parse the input with the default (non-validating) parser
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(file, handler);

		// print result
		handler.printLocalNames();
	}

	public void printLocalNames() {
		List<String> localNamesList = new ArrayList<String>(localNames);
		Collections.sort(localNamesList);
		System.out.println("\nLocal Names:\n");
		for (String localName : localNamesList) {
			System.out.println(localName);
		}
		List<String> qNamesList = new ArrayList<String>(qNames);
		Collections.sort(qNamesList);
		System.out.println("\nQualified Names:\n");
		for (String qName : qNamesList) {
			System.out.println(qName);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		// collect local names and q names
		localNames.add(localName);
		qNames.add(qName);
	}

}
