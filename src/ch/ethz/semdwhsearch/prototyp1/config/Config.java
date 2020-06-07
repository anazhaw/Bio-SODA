package ch.ethz.semdwhsearch.prototyp1.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * Our configuration.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public class Config {

	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	// ---------------------------------------------------------------- members

	private final String configFilePath;

	private long lastModified;

	private Properties props;

	public Config(String configDirPath) {
		this.configFilePath = (configDirPath.endsWith("/") ? configDirPath : configDirPath + "/") + Constants.FN_CONFIG;
		this.lastModified = 0L;
		this.props = new Properties();
		refreshIfNeeded();
		logger.info("Configuration from " + configFilePath + " loaded.");
	}

	private void refreshIfNeeded() {
		File configFile = new File(configFilePath);
		long lastModifiedNew = configFile.lastModified();
		if (lastModifiedNew != lastModified) {
			Properties propsTmp = new Properties();
			try {
				propsTmp.load(new FileInputStream(configFile));

				// assign if successfully refreshed
				props = propsTmp;
				lastModified = lastModifiedNew;
			} catch (IOException ioe) {
				logger.warn("Problem while refreshing configuration: " + ioe.getMessage());
			}
		}
	}

	public Map<String, String> getAllProperties() {
		return getAllProperties(null);
	}

	public Map<String, String> getAllProperties(String prefix) {

		// collect all propertis
		Map<String, String> mapAll = new TreeMap<String, String>();
		mapAll.put("ds.useDbIndex", String.valueOf(isDsUseDbIndex(true)));
		mapAll.put("ds.index.batchSize", String.valueOf(getDsIndexBatchSize(1000)));
		mapAll.put("ds.index.commitSize", String.valueOf(getDsIndexCommitSize(1000000)));
		mapAll.put("ds.index.purgeSize", String.valueOf(getDsIndexPurgeSize(1000000)));
		mapAll.put(ConfigPropertyNames.DS_INDEX_TOKENIZE_KEY, String.valueOf(isDsIndexTokenizeKey(false)));
		mapAll.put(ConfigPropertyNames.ALGO_LOOKUP_SUBSTRING_MATCHING, String.valueOf(isAlgoLookupSubstringMatching(false)));
		mapAll.put(ConfigPropertyNames.ALGO_LOOKUP_PREFIX_MATCHING, String.valueOf(isAlgoLookupPrefixMatching(true)));
		mapAll.put(ConfigPropertyNames.ALGO_SPARQL_SUBSTRING_MATCHING, String.valueOf(isAlgoSparqlSubstringMatching(true)));
		mapAll.put(ConfigPropertyNames.ALGO_SPARQL_PREFIX_MATCHING, String.valueOf(isAlgoSparqlPrefixMatching(false)));
		mapAll.put(ConfigPropertyNames.ALGO_LOOKUP_EXCLUDE, String.valueOf(getLookupExclusionList("")));

		// filter by prefix
		if (prefix == null) {
			return mapAll;
		} else {
			Map<String, String> mapFiltered = new TreeMap<String, String>();
			for (Map.Entry<String, String> entry : mapAll.entrySet()) {
				if (entry.getKey().startsWith(prefix)) {
					mapFiltered.put(entry.getKey(), entry.getValue());
				}
			}
			return mapFiltered;
		}
	}

	public boolean isExcludedFromLookup(String uri) {
		String exclusionList = getString(ConfigPropertyNames.ALGO_LOOKUP_EXCLUDE, "");
		if(exclusionList.isEmpty())
			return false;
		for(String excluded : exclusionList.split(",")){
			if(uri.contains(excluded))
				return true;
		}
		return false;
	}

	public String getProperty(String key) {
		return getAllProperties().get(key);
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	public void persist() {
		File curFile = new File(configFilePath);
		File newFile = new File(curFile + ".tmp");
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(curFile), "utf-8"));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "utf-8"));
			Map<String, String> map = getAllProperties();
			String line = null;
			while ((line = reader.readLine()) != null) {
				int pos = line.indexOf("=");
				if (!line.startsWith("#") && pos > 0) {
					String propName = line.substring(0, pos).trim();
					String propValue = map.get(propName);
					if (propValue != null) {
						line = propName + " = " + propValue;
					}
				}
				writer.write(line + "\n");
			}
			reader.close();
			writer.close();
			reader = null;
			writer = null;
			curFile.delete();
			newFile.renameTo(curFile);
			logger.info("Config file updated.");
		} catch (Exception e) {
			logger.warn("Failure persisting config file '" + Constants.FN_CONFIG + "': " + e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void print(PrintStream out) {

		// get all properties
		Map<String, String> propMap = getAllProperties();

		// find max key length
		int maxLength = 0;
		for (String key : propMap.keySet()) {
			if (key.length() > maxLength) {
				maxLength = key.length();
			}
		}

		// print
		for (Map.Entry<String, String> entry : propMap.entrySet()) {
			out.print("- " + entry.getKey() + ": ");
			for (int i = (maxLength - entry.getKey().length()); i > 0; i--) {
				out.print(" ");
			}
			out.println(entry.getValue());
		}
	}

	// ------------------------------------------------------------------ input

	// -------------------------------------------------------- data structures

	public boolean isDsUseDbIndex(boolean defaultValue) {
		return getBoolean("ds.useDbIndex", defaultValue);
	}

	public int getDsIndexBatchSize(int defaultValue) {
		return getInt("ds.index.batchSize", defaultValue);
	}

	public int getDsIndexCommitSize(int defaultValue) {
		return getInt("ds.index.commitSize", defaultValue);
	}

	public int getDsIndexPurgeSize(int defaultValue) {
		return getInt("ds.index.purgeSize", defaultValue);
	}

	public boolean isDsIndexTokenizeKey(boolean defaultValue) {
		return getBoolean("ds.index.tokenizeKey", defaultValue);
	}

	// -------------------------------------------------------------- algorithm

	public boolean isAlgoLookupSubstringMatching(boolean defaultValue) {
		return getBoolean(ConfigPropertyNames.ALGO_LOOKUP_SUBSTRING_MATCHING, defaultValue);
	}

	public boolean isAlgoLookupPrefixMatching(boolean defaultValue) {
		return getBoolean(ConfigPropertyNames.ALGO_LOOKUP_PREFIX_MATCHING, defaultValue);
	}

	public boolean isAlgoFiltersUseLookupKey(boolean defaultValue) {
		return getBoolean(ConfigPropertyNames.ALGO_FILTERS_USE_LOOKUP_KEY, defaultValue);
	}

	public boolean isAlgoSparqlSubstringMatching(boolean defaultValue) {
		return getBoolean(ConfigPropertyNames.ALGO_SPARQL_SUBSTRING_MATCHING, defaultValue);
	}

	public boolean isAlgoSparqlPrefixMatching(boolean defaultValue) {
		return getBoolean(ConfigPropertyNames.ALGO_SPARQL_PREFIX_MATCHING, defaultValue);
	}

	public String getLookupExclusionList(String defaultValue) {
		return getString(ConfigPropertyNames.ALGO_LOOKUP_EXCLUDE, defaultValue);
	}

	// ---------------------------------------------------------------- results
	
	public boolean isStanfordNLPEnable(boolean defaultValue) {
		return getBoolean("stanfordnlp", defaultValue);
	}



	// -------------------------------------------------------- private methods

	private boolean getBoolean(String propertyName, boolean defaultValue) {
		refreshIfNeeded();
		String value = props.getProperty(propertyName);
		if (value == null) {
			return defaultValue;
		} else {
			return Boolean.parseBoolean(value);
		}
	}

	private int getInt(String propertyName, int defaultValue) {
		refreshIfNeeded();
		String value = props.getProperty(propertyName);
		if (value == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(value);
		}
	}

	private String getString(String propertyName, String defaultValue) {
		refreshIfNeeded();
		String value = props.getProperty(propertyName);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

}
