package ch.ethz.semdwhsearch.prototyp1.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.rdf.EModel;
import ch.ethz.rdf.EModels;
import ch.ethz.rdf.EResource;
import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.metadata.mapping.MetadataMapping;

/**
 * Metadata - is great! :-)
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public class Metadata {

	private static final Logger logger = LoggerFactory.getLogger(Metadata.class);

	// -------------------------------------------------------------- constants
	public static final int TYPE_OP = 1; //Operator
	public static final int TYPE_DO = 2; //Domain Ontology
	public static final int TYPE_CS = 3; //Conceptual Schema
	public static final int TYPE_LS = 4; //Logical Schema
	public static final int TYPE_BD = 5; //Base Data
	public static final int TYPE_SD = 6; //

	// ------------------------------------------------------------------------

	public static final String STYLE_DB = "color: black; background-color: #ccffff; border: 1px solid #999999";
	public static final String STYLE_UK = "color: black; background-color: #ffffff; border: 1px solid #000000";
	public static final String STYLE_OP = "color: black; background-color: #aaffaa; border: 1px solid #66ff66";
	public static final String STYLE_DO = "color: black; background-color: #ddaaff; border: 1px solid #9900ff";
	public static final String STYLE_CS = "color: black; background-color: #aaddff; border: 1px solid #0099ff";
	public static final String STYLE_LS = "color: black; background-color: #aabbff; border: 1px solid #0033ff";
	public static final String STYLE_BD = "color: black; background-color: #ffddaa; border: 1px solid #ffcc00";
	public static final String STYLE_SD = "color: black; background-color: #cccccc; border: 1px solid #999999";
	public static final String STYLE_JO = "color: black; background-color: #ffccff; border: 1px solid #aa33aa";
	public static final String STYLE_SELECTED = "color: black; background-color: #00ff00; border: 1px solid #009933";
	public static final String STYLE_MATCH = "color: black; background-color: #E74C3C; border: 1px solid #641E16";
	public static final String STYLE_FINAL_MATCH = "color: black; background-color: #C70039; border: 1px solid #641E16";

	// ---------------------------------- add user feedback node type [mscmike]

	public static final String STYLE_UF = "color: black; background-color: #ff0033; border: 1px solid #999999";

	// ------------------------------------------------------------------------

	public static final String getStyleByType(int type) {
		if (type == TYPE_OP) {
			return STYLE_OP;
		} else if (type == TYPE_DO) {
			return STYLE_DO;
		} else {
			return STYLE_SD;
		}
	}

	// ---------------------------------------------------------------- members

	private final String dataDirPath;

	private final String configDirPath;
	
	private final Config config;

	private Html5DagGenericNode dag;

	private Collection<ModelInfo> doInfos;

	private MetadataMapping mapping;

	// ----------------------------------------------------------- construction

	public Metadata(String configDirPath, String dataDirPath, Config config, boolean reloadIdx, boolean appendIdx) {
		this.configDirPath = configDirPath.endsWith("/") ? configDirPath : configDirPath + "/";
		this.dataDirPath = dataDirPath.endsWith("/") ? dataDirPath : dataDirPath + "/";
		this.config = config;

		// init members
		dag = null;
		doInfos = new ArrayList<ModelInfo>();
		mapping = null;

		// load mapping and metadata
		File configDir = new File(configDirPath);
		File dataDir = new File(dataDirPath);
		logger.info("Loading metadata from " + dataDir.getName() + "...");
		mapping = new MetadataMapping(configDir);
		load(dataDir, reloadIdx, appendIdx);
		logger.info("Metadata loaded from " + dataDir.getName() + ".");
	}

	/**
	 * @return data dir path (ending with a slash).
	 */
	public String getDataDirPath() {
		return dataDirPath;
	}

	public Html5DagGenericNode getDag() {
		return dag;
	}

	public MetadataMapping getMapping() {
		return mapping;
	}

	public Collection<ModelInfo> getModelInfos() {
		List<ModelInfo> modelInfos = new ArrayList<ModelInfo>();
		modelInfos.addAll(doInfos);
		return modelInfos;
	}

	public Collection<ModelInfo> getModelInfos(int type) {
		if (type == TYPE_DO) {
			return doInfos;
		} else {
			throw new RuntimeException("Unsupported input source type: " + type);
		}
	}

	public ModelInfo getModelInfo(int type, String modelName) {
		for (ModelInfo info : getModelInfos(type)) {
			if (info.getModelName().equals(modelName)) {
				return info;
			}
		}
		return null;
	}

	public ModelInfo getModelInfo(String modelName) {
		for (ModelInfo info : getModelInfos()) {
			if (info.getModelName().equals(modelName)) {
				return info;
			}
		}
		return null;
	}


	/**
	 * Our metadata contains a single DAG (in the dag member variable). This
	 * method tests if the variable was already initialized and otherwise
	 * creates the DAG from the first entry point in finds in the first model
	 * given.
	 * 
	 * @param models
	 */
	private void createDagIfNeeded(List<EModel> models) {

		// only create DAG if needed and possible
		if (dag == null && models.size() > 0) {
			EModel model = models.get(0);

			// create DAG from first entry point
			List<EResource> entryPoints = model.getEntryPoints();
			if (entryPoints.size() > 0) {
				EResource entryPoint = entryPoints.get(0);
				dag = new Html5DagGenericNode(entryPoint.getURI());
			}
		}
	}

	public void load(File dataDir, boolean reloadIdx, boolean appendIdx) {

		List<EModel> modelsDos = new ArrayList<EModel>();
		createDagIfNeeded(modelsDos);
		
		// SPARQL data
		// use the file manager to read an RDF document into the model

		// ********* Federated *********** ///
		Model allModels = ModelFactory.createDefaultModel();
		if(appendIdx || reloadIdx) {
			if(Constants.ONDISK_MODEL){
				Dataset ds = TDBFactory.createDataset(Constants.TBD_DATA_DIR);
				allModels = ds.getDefaultModel();
			}
			else{
				modelsDos = EModels.getFromDir(new File(getDataDirPath()));
				logger.info("GOT FROM DIR MODELS "+ modelsDos.size());
	
				//TODO: use TDB-backed model for LARGE RDF 
				createDagIfNeeded(modelsDos);
				
				for (EModel model : modelsDos) {
					logger.info("ADDING MODEL "+ model.getFilename() + " statements: "+ model.getAllUris().size());
					allModels.add(model.getModel());
				}
			}
		}
		ModelInfo infoSparql = new ModelInfo(allModels, Constants.MODEL_NAME, TYPE_DO, mapping, false);
		doInfos.add(infoSparql);

		logger.info("RDF dataset added.");
	}
}
