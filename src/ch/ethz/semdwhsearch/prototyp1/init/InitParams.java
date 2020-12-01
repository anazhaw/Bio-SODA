package ch.ethz.semdwhsearch.prototyp1.init;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * Initialization parameters.
 * 
 * @author Lukas Blunschi
 * 
 */
public class InitParams {

	private boolean initialized;

	// -------------------------------------------------------- init parameters

	private String dbvendor;

	private String schema;

	private String username;

	private String password;

	private String dbname;

	private String hostname;

	private String port;

	private String sid;

	private String configDirPath;

	private String dataDirPath;

	private String reloadIdx;

	private String appendIdx;
	// ----------------------------------------------------------- construction

	public InitParams() {
	}

	public boolean tryInitFile() {

		// try
		try {

			// test if init file can be read
			File initFile = new File(Constants.FN_AUTO_INIT);
			if (initFile.canRead()) {

				// load properties
				Properties props = new Properties();
				props.load(new InputStreamReader(new FileInputStream(initFile), "utf-8"));

				// set parameters
				dbvendor = props.getProperty("db.vendor");
				schema = props.getProperty("db.schema");
				username = props.getProperty("db.username");
				password = props.getProperty("db.password");
				dbname = props.getProperty("db.dbname");
				hostname = props.getProperty("db.hostname");
				port = props.getProperty("db.port");
				sid = props.getProperty("db.sid");
				dataDirPath = props.getProperty("data.dir");
				configDirPath = props.getProperty("config.dir");
				reloadIdx = props.getProperty("index.reload");
				appendIdx = props.getProperty("index.append");

				if (isValid()) {
					initialized = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return initialized;
	}

	public boolean tryConsole() {

		// try
		try {

			// get console
			Console console = System.console();
			if (console != null) {

				// set parameters
				dbvendor = console.readLine("DB vendor: ");
				schema = console.readLine("Schema: ");
				username = console.readLine("Username: ");
				password = String.valueOf(console.readPassword("Password (hidden input): "));
				dbname = console.readLine("Database name: ");
				hostname = console.readLine("Hostname: ");
				port = console.readLine("Port: ");
				sid = console.readLine("SID: ");
				dataDirPath = console.readLine("Data directory path: ");
				reloadIdx = console.readLine("Reload index? [true/false]: ");
				appendIdx = console.readLine("Append to index? [true/false]: ");

				// test
				if (isValid()) {
					initialized = true;
				}
			} else {
				System.err.println("No console available! Aborting...");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return initialized;
	}

	public boolean tryArgs(String[] args) {

		// try
		try {

			// print args
			System.out.println("Args: " + new ArrayList<String>(Arrays.asList(args)));

			// checks args length
			if (args.length == 11) {

				// set parameters
				dbvendor = args[0].equals("-") ? null : args[0];
				schema = args[1].equals("-") ? null : args[1];
				username = args[2].equals("-") ? null : args[2];
				password = args[3].equals("-") ? null : args[3];
				dbname = args[4].equals("-") ? null : args[4];
				hostname = args[5].equals("-") ? null : args[5];
				port = args[6].equals("-") ? null : args[6];
				sid = args[7].equals("-") ? null : args[7];
				dataDirPath = args[8].equals("-") ? null : args[8];
				reloadIdx = args[9].equals("-") ? null : args[9];
				appendIdx = args[10].equals("-") ? null : args[10];

				// test
				if (isValid()) {
					initialized = true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return initialized;
	}

	private boolean isValid() {
		boolean valid = true;

		// check input
		if (dataDirPath == null || dataDirPath.trim().length() == 0) {
			dataDirPath = "./";
		}
		if (!dataDirPath.endsWith("/")) {
			dataDirPath += "/";
		}

		if (configDirPath == null || configDirPath.trim().length() == 0) {
			configDirPath = "./";
		}
		if (!configDirPath.endsWith("/")) {
			configDirPath += "/";
		}

		return valid;
	}

	// ---------------------------------------------------------------- getters

	public boolean isInitialized() {
		return initialized;
	}

	public String getDbvendor() {
		return dbvendor;
	}

	public String getSchema() {
		return schema;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDbname() {
		return dbname;
	}

	public String getHostname() {
		return hostname;
	}

	public String getPort() {
		return port;
	}

	public String getSid() {
		return sid;
	}

	public String getConfigDirPath() {
		return configDirPath;
	}


	public String getDataDirPath() {
		return dataDirPath;
	}

	public boolean isReloadIdx() {
		return reloadIdx == null ? false : reloadIdx.equals("true");
	}

	public void setReloadIdx(String reloadIdx) {
		this.reloadIdx = reloadIdx;
	}
	
	public boolean isAppendIdx() {
		return appendIdx == null ? false : appendIdx.equals("true");
	}

	public void setAppendIdx(String appendIdx) {
		this.appendIdx = reloadIdx;
	}

}
