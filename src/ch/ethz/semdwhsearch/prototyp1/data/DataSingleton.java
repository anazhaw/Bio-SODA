package ch.ethz.semdwhsearch.prototyp1.data;

/**
 * A singleton for the data.
 * <p>
 * To avoid having to load the data upon each request.
 * <p>
 * All this singleton does is holding a reference to a Data instance.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DataSingleton {

	private static final DataSingleton instance = new DataSingleton();

	private Data data;

	private DataSingleton() {
		this.data = null;
	}

	public static DataSingleton getInstance() {
		return instance;
	}

	// ---------------------------------------------------- getters and setters

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

}
