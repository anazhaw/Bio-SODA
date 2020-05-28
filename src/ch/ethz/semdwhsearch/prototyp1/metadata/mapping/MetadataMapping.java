package ch.ethz.semdwhsearch.prototyp1.metadata.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * A metadata mapping defines the mappings from property names in the given data
 * to property names understood by this application.
 * <p>
 * The mapping is loaded from a file when the metadata is loaded.
 * 
 * @author Lukas Blunschi
 * 
 */
public class MetadataMapping {

	private static final Logger logger = LoggerFactory.getLogger(MetadataMapping.class);

	private Map<String, String> mapping;

	public MetadataMapping(File dataDir) {
		mapping = new HashMap<String, String>();
		try {
			File mappingFile = new File(dataDir, Constants.FN_METADATA_MAPPING);
			BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// ignore comments and empty lines
				if (line.length() == 0 || line.startsWith("#")) {
					continue;
				}

				// store key value mapping
				String[] keyValueParts = line.split("=");
				if (keyValueParts.length != 2) {
					continue;
				}
				mapping.put(keyValueParts[0].trim(), keyValueParts[1].trim());
			}
			reader.close();
		} catch (IOException ioe) {
			logger.warn("Exception while loading metadata mapping: " + ioe.getMessage());
			mapping.clear();
		}
	}

	// ---------------------------------------------------------------- general

	public String getGeneralPropNamesCaption() {
		return mapping.get("general.propnames.caption");
	}

	public Set<String> getGeneralPropNamesCaptionSet() {
		String pnsCaption = getGeneralPropNamesCaption();
		if (pnsCaption == null || pnsCaption.isEmpty()) {
			return new HashSet<String>();
		}
		pnsCaption = pnsCaption.toLowerCase();

		String[] pnsCaptionArray = pnsCaption.split(",");
		Set<String> pnsCaptionSet = new HashSet<String>();
		for (String pngCaption : pnsCaptionArray) {
			pngCaption = pngCaption.trim().toLowerCase();
			if (pngCaption.length() > 0) {
				pnsCaptionSet.add(pngCaption);
			}
		}
		return pnsCaptionSet;
	}

	// -------------------------------------------------------------- base data

	public String getBasedataBasePath() {
		return mapping.get("basedata.basePath");
	}

	public String getBasedataBaseUrl() {
		return mapping.get("basedata.baseUrl");
	}

	// ----------------------------------------------------------------- schema

	public String getSchemaBasePath() {
		return mapping.get("schema.basePath");
	}

	public String getSchemaBaseUrl() {
		return mapping.get("schema.baseUrl");
	}

	public String getSchemaPropNamePhysicalTableName() {
		return mapping.get("schema.propname.physicalTableName");
	}

	public String getSchemaPropNamePhysicalColumnName() {
		return mapping.get("schema.propname.physicalColumnName");
	}

	public String getSchemaPropNameLogicalAttr() {
		return mapping.get("schema.propname.logicalAttr");
	}

	public String getSchemaTypeKeyAttr() {
		return mapping.get("schema.type.keyAttr");
	}

	public String getSchemaPropNameKeyAttr() {
		return mapping.get("schema.propname.keyAttr");
	}

	public String getSchemaPropNameForeignKey() {
		return mapping.get("schema.propname.foreignKey");
	}

	public String getSchemaTypeJoinNode() {
		return mapping.get("schema.type.joinNode");
	}

	public String getSchemaTypeJoinAttr() {
		return mapping.get("schema.type.joinAttr");
	}

	public String getSchemaPropNameJoinAttr1() {
		return mapping.get("schema.propname.joinAttr1");
	}

	public String getSchemaPropNameJoinAttr2() {
		return mapping.get("schema.propname.joinAttr2");
	}

	public String getSchemaPropNamePhysicalColumn() {
		return mapping.get("schema.propname.physicalColumn");
	}

	public String getSchemaTypePhysicalTable() {
		return mapping.get("schema.type.physicalTable");
	}

	public String getSchemaTypePhysicalColumn() {
		return mapping.get("schema.type.physicalColumn");
	}

	public String getSchemaTypeInheritanceNode() {
		return mapping.get("schema.type.inheritanceNode");
	}

	public String getSchemaPropNameInheritanceChild() {
		return mapping.get("schema.propname.inheritanceChild");
	}

	public String getSchemaPropNameInheritanceParent() {
		return mapping.get("schema.propname.inheritanceParent");
	}

	// ----------------------------------------------------- schema description

	public String getSchemaDescBaseUrl() {
		return mapping.get("schemaDesc.baseUrl");
	}

	public String getSchemaDescPropNameIsLogicalEntity() {
		return mapping.get("schemaDesc.propname.isLogicalEntity");
	}

	public String getSchemaDescPropNameIsLogicalAttr() {
		return mapping.get("schemaDesc.propname.isLogicalAttr");
	}

	public String getSchemaDescUriLogicalEntity() {
		return mapping.get("schemaDesc.uri.logicalEntity");
	}

	public String getSchemaDescUriLogicalAttr() {
		return mapping.get("schemaDesc.uri.logicalAttr");
	}

	// ------------------------------------------------------ domain ontologies

	public String getDosBasePath() {
		return mapping.get("dos.basePath");
	}

	public String getDosBaseUrl() {
		return mapping.get("dos.baseUrl");
	}

	public String getDosPropNameValue() {
		return mapping.get("dos.propname.value");
	}

	public String getDosPropNameType() {
		return mapping.get("dos.propname.type");
	}

	// ---------------------------------------------------------------- DBPedia

	public String getDBPediaResourceBasePath() {
		return mapping.get("dbPedia.resource.basePath");
	}

	public String getDBPediaPropertyWikilink() {
		return mapping.get("dbPedia.property.wikilink");
	}

	public String getDBPediaPropNameWikilink() {
		return mapping.get("dbPedia.propname.wikilink");
	}

	// [mscmike] ------------------------------------------------ User Feedback

	public String getNewNodeUFBasePath() {
		return mapping.get("userfeedback.newNode.basePath");
	}
}
