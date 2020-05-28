package ch.ethz.semdwhsearch.prototyp1.localization;

public class EnglishDatabase implements DictionaryDatabase {

	// ----------------------------------------------------------------- common

	public String id() {
		return "Id";
	}

	public String name() {
		return "Name";
	}

	// ---------------------------------------------------------------- options

	public String company() {
		return "Company";
	}

	public String type() {
		return "Type";
	}

	// ------------------------------------------------------------ derivatives

	public String shortName() {
		return "Short Name";
	}

}
