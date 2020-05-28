package ch.ethz.rdf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.tools.StopWatch;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Tools to work with several models.
 * 
 * @author Lukas Blunschi
 * 
 */
public class EModels {

	private static final Logger logger = LoggerFactory.getLogger(EModels.class);

	/**
	 * Load all models in the given directory.
	 * <p>
	 * This creates one model per .rdf/.nt file that exists in the given
	 * directory.
	 * 
	 * @param dir
	 * @return
	 */
	public static List<EModel> getFromDir(File dir) {
		List<EModel> models = new ArrayList<EModel>();
		try {

			// ignore non-existing or empty directory
			if (!dir.exists() || dir.listFiles() == null) {
				return models;
			}

			// loop over all .rdf, .nt and .ttl files in the given directory
			StopWatch watch = new StopWatch("~");
			File[] files = dir.listFiles();
			for (File file : files) {
				String filename = file.getName();
				if (file.canRead() && file.isFile()) {

					// load model from RDF or N-Triples
					InputStream in = new BufferedInputStream(new FileInputStream(file));
					Model model = ModelFactory.createDefaultModel();
					if (filename.endsWith(".rdf")) {
						model.read(in, null);
						models.add(new EModel(model, filename));
					} else if (filename.endsWith(".nt")) {
						model.read(in, null, "N-TRIPLE");
						models.add(new EModel(model, filename));
					} else if (filename.endsWith(".ttl")) {
						model.read(in, null,"TTL");
						models.add(new EModel(model, filename));
					}else if (filename.endsWith(".owl")) {
						model.read(in, null);
						models.add(new EModel(model, filename));
					}
				}
			}

			// report time to load models
			watch.stopAndReport("Loading RDF models");

		} catch (IOException ioe) {
			logger.warn("Exception while loading models from directory: " + dir.getAbsolutePath());
			models.clear();
		}
		return models;
	}

}
