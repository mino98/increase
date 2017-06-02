package it.minux.increase.ui;

import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.io.IOException;

public class IncreaseConfig {
	public static final IncreaseConfig INSTANCE = new IncreaseConfig();
	
	private IncreaseConfig() {
	}
	
	/**
	 * Read configuration from file
	 * @throws IOException 
	 */
	public void init(File file) throws IOException {
		Config.INSTANCE.init(file);
	}
	
	public Sector getSectorOfInteres() {
		float minLat = Config.INSTANCE.getFloatValue(Config.SECTOR_MINLAT, 43);
		float maxLat = Config.INSTANCE.getFloatValue(Config.SECTOR_MAXLAT, 47);
		float minLon = Config.INSTANCE.getFloatValue(Config.SECTOR_MINLON, 8);
		float maxLon = Config.INSTANCE.getFloatValue(Config.SECTOR_MAXLON, 12);
		
		Sector sector = Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
		return sector;
	}
	
	public File getCoverageFile() {
		String path = Config.INSTANCE.getValue(Config.COVERAGE_FILE, "coverage.png");
		return new File(path);
	}

	public double getPtpHeight() {
		return Config.INSTANCE.getFloatValue(Config.PTP_HEIGHT, 30.0f);
	}

	public double getMaxPtpDistance() {
		return Config.INSTANCE.getFloatValue(Config.MAX_PTP_DISTANCE, 50.0f) * 1000.0;
	}
	
	public double getMaxPmpDistance() {
		return Config.INSTANCE.getFloatValue(Config.MAX_PMP_DISTANCE, 20.0f) * 1000.0;
	}

	public double getC() {
		return Config.INSTANCE.getFloatValue(Config.C, 0.05f);
	}
}
