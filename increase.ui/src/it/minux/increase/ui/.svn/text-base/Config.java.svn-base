package it.minux.increase.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Config {

	private static final Log LOG = LogFactory.getLog(Config.class);
	public static final Config INSTANCE = new Config();
	public static final String DEFAULT_FILE = "increase.properties";
	
	public static final String HEATMAP_LATRES = "heatmap.latres";
	public static final String HEATMAP_LONRES = "heatmap.lonres";
	public static final String SECTOR_MINLAT = "sector.minlat";
	public static final String SECTOR_MAXLAT = "sector.maxlat";
	public static final String SECTOR_MINLON = "sector.minlon";
	public static final String SECTOR_MAXLON = "sector.maxlon";
	public static final String TOWERS_LABELS_SHOW = "towers.labelsshow";
	public static final String TOWERS_LABELS_MAXALT = "towers.labelsmaxalt";
	public static final String TOWERS_SKIP_NO_PANELS = "towers.skipnopanels";
	public static final String COVERAGE_MONOCHROME = "coverage.monochrome";
	public static final String COVERAGE_MONOCHROME_R = "coverage.monochrome.r";
	public static final String COVERAGE_MONOCHROME_G = "coverage.monochrome.g";
	public static final String COVERAGE_MONOCHROME_B = "coverage.monochrome.b";
	public static final String COVERAGE_MONOCHROME_A = "coverage.monochrome.a";
	public static final String COVERAGE_FILE = "coverage.file";
	public static final String COVERAGE_DTM_LEVEL = "coverage.dtm.level";
	public static final String USERSLOCATIONS_XMLFILENAME = "userslocations.file";
	public static final String SUPPORTREQUESTS_XMLFILENAME = "supportrequests.xmlfilename";
	public static final String INSTALLATIONFAILURES_XMLFILENAME = "installationfailures.xmlfilename";
	public static final String TOWERS_XMLFILENAME = "towers.xmlfilename";
	public static final String TOPOLOGY_XMLFILENAME = "topology.xmlfilename";
	public static final String COVERAGEREQUESTS_XMLFILENAME = "coveragerequests.xmlfilename";
	
	public static final String COMBINED_WEIGHT_SUPPORT = "combined.weight.support";
	public static final String COMBINED_WEIGHT_INSTALL = "combined.weight.install";
	public static final String COMBINED_WEIGHT_COVERAGE = "combined.weight.coverage";
	
	public static final String PTP_HEIGHT = "PTP_HEIGHT";
	public static final String MAX_PTP_DISTANCE = "MAX_PTP_DISTANCE";
	public static final String MAX_PMP_DISTANCE = "MAX_PMP_DISTANCE";
	public static final String LOOKAHEAD_STEPS = "LOOKAHEAD_STEPS";
	
	public static final String C = "C";
	
	public static String ENABLE_VISIBILITY_GRAPH_LAYER = "graph.layer"; // enable debug visibility graph layer
	
	private final Properties properties = new Properties();
	
	private Config() {		
	}
	
	/**
	 * Read configuration from file
	 * @throws IOException 
	 */
	public void init(File file) throws IOException {
		
		FileInputStream fis = new FileInputStream(file);
		try {
			properties.clear();
			properties.load(fis);
		} finally {
			fis.close();
		}
	}
	
	public String getValue(String key) {
		return properties.getProperty(key);
 	}
	
	public String getValue(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
 	}


	public int getIntValue(String key, int defaultValue) {
		String str = getValue(key);
		if (str == null) {
			LOG.warn(key + " property not found, default is " + defaultValue);
			return defaultValue;
		}
		
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			LOG.error("Invalid " + key + " property value: " + str);
			return defaultValue;
		}
	}
	
	public float getFloatValue(String key, float defaultValue) {
		String str = getValue(key);
		if (str == null) {
			LOG.warn(key + " property not found, default is " + defaultValue);
			return defaultValue;
		}
		
		try {
			return Float.parseFloat(str);
		} catch (Exception e) {
			LOG.error("Invalid " + key + " property value: " + str);
			return defaultValue;
		}
	}
	
	public boolean getBooleanValue(String key, boolean defaultValue) {
		String str = getValue(key);
		if (str == null) {
			LOG.warn(key + " property not found, default is " + defaultValue);
			return defaultValue;
		}
		
		try {
			return Boolean.parseBoolean(str);
		} catch (Exception e) {
			LOG.error("Invalid " + key + " property value: " + str);
			return defaultValue;
		}
	}
}
