package ch.ethz.semdwhsearch.prototyp1.config;

/**
 * A singleton for the configuration.
 * <p>
 * To avoid having to load the configuration upon each request.
 * <p>
 * All this singleton does is holding a reference to a Config instance.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ConfigSingleton {

	private static final ConfigSingleton instance = new ConfigSingleton();

	private Config config;

	private ConfigSingleton() {
		this.config = null;
	}

	public static ConfigSingleton getInstance() {
		return instance;
	}

	// ---------------------------------------------------- getters and setters

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

}
