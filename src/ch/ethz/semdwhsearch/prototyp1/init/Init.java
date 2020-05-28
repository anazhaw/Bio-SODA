package ch.ethz.semdwhsearch.prototyp1.init;

import javax.sql.DataSource;

import ch.ethz.semdwhsearch.prototyp1.classification.Classification;
import ch.ethz.semdwhsearch.prototyp1.classification.ClassificationSingleton;
import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.config.ConfigSingleton;
import ch.ethz.semdwhsearch.prototyp1.data.Data;
import ch.ethz.semdwhsearch.prototyp1.data.DataSingleton;
import ch.ethz.semdwhsearch.prototyp1.data.DataSourceSetup;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;
//import ch.ethz.semdwhsearch.prototyp1.metadatastructure.MetadataStructure;
//import ch.ethz.semdwhsearch.prototyp1.metadatastructure.MetadataStructureSingleton;

/**
 * Initialize application.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Init {

	/**
	 * Do init now
	 * 
	 * @param params
	 */
	public static void go(InitParams params) {
		go(params.getDbvendor(), params.getSchema(), params.getUsername(), params.getPassword(), params.getDbname(),
				params.getHostname(), params.getPort(), params.getSid(), params.getConfigDirPath(), params.getDataDirPath(), params.isReloadIdx(), params.isAppendIdx());
	}

	/**
	 * Do init now.
	 * 
	 * @param dbvendor
	 * @param schema
	 * @param username
	 * @param password
	 * @param dbname
	 * @param hostname
	 * @param port
	 * @param sid
	 * @param dataDirPath
	 * @param reloadIdx
	 */
	public static void go(String dbvendor, String schema, String username, String password, String dbname, String hostname,
			String port, String sid, String configDirPath, String dataDirPath, boolean reloadIdx, boolean appendIdx) {

		// default values
		if (dbvendor != null && dbvendor.trim().length() == 0) {
			dbvendor = null;
		}
		if (hostname == null || hostname.trim().length() == 0) {
			hostname = "localhost";
		}
		if (schema != null && schema.trim().length() == 0) {
			schema = null;
		}

		// init config
		// - this loads the config properties (semdwhsearch.properties)
		// - and stores a reference in the singleton
		Config config = new Config(configDirPath);
		ConfigSingleton.getInstance().setConfig(config);

		// init data
		// - this creates a data source (a connection factory)
		// - then creates a data reference
		// - and stores a reference in the singleton
		DataSource ds = DataSourceSetup.getDs(dbvendor, username, password, dbname, hostname, port, sid);
		Data data = new Data(ds, dbvendor, schema, username, config);
		DataSingleton.getInstance().setData(data);

		// init metadata
		Metadata metadata = new Metadata(configDirPath, dataDirPath, config, reloadIdx, appendIdx);
		MetadataSingleton.getInstance().setMetadata(metadata);
		
		// init classification
		Classification cls = new Classification(metadata, data, config, reloadIdx, appendIdx);
		ClassificationSingleton.getInstance().setClassification(cls);

	}

	/**
	 * TODO is this needed?
	 * 
	 * @return true if properly initialized.
	 */
	public static boolean isInitialized() {
		boolean hasConfig = ConfigSingleton.getInstance().getConfig() != null;
		boolean hasData = DataSingleton.getInstance().getData() != null;
		boolean hasMetadata = MetadataSingleton.getInstance().getMetadata() != null;
		boolean hasClassification = ClassificationSingleton.getInstance().getClassification() != null;
		return hasConfig && hasData && hasMetadata && hasClassification;
	}

}
