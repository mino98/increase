package it.minux.increase.data;

import gov.nasa.worldwind.geom.LatLon;
import it.minux.increase.xml.CoverageRequestType;
import it.minux.increase.xml.CoverageRequests;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVReader;

public class CoverageRequestsLoader 
	extends XMLLoader
{
	
	private static final Log LOG = LogFactory.getLog(CoverageRequestsLoader.class);
	
	public static final CoverageRequestsLoader INSTANCE = new CoverageRequestsLoader();
	
	private CoverageRequestsLoader() {
	}
	
	
	public List<CoverageRequest> loadFromXML(File file) throws DataLoadException {
		try {
			CoverageRequests coverageXML = unmarshallFile(CoverageRequests.class, file);
			List<CoverageRequest> result = new ArrayList<CoverageRequest>(coverageXML.getCoverageRequest().size());
			for (CoverageRequestType reqXML : coverageXML.getCoverageRequest()) {
				result.add(new CoverageRequest(reqXML));
			}
			
			return result;
		} catch (Exception e) {
			throw new DataLoadException("Failed to load xml file " + file, e);
		}
	}
	
	/**
	 * Load from csv text file
	 * @param csvFile
	 * @return
	 * @throws DataLoadException 
	 */
	public List<CoverageRequest> loadFromCSV(File csvFile) throws DataLoadException {
		List<CoverageRequest> result = new LinkedList<CoverageRequest>();
		
		try {
			CSVReader csvReader = new CSVReader(new FileReader(csvFile), '\t');
			try {
				String [] csvLine;
				while ((csvLine = csvReader.readNext()) != null) {
				    if(csvLine.length != 2) {
				    	continue;
				    }
				    
				    // The format is: "lng	lat", separated by a tab
				    Double lon = Double.valueOf(csvLine[0].trim());
				    Double lat = Double.valueOf(csvLine[1].trim());
				    LatLon location = LatLon.fromDegrees(lat, lon);
				    
				    CoverageRequest request = new CoverageRequest(location, null); 
				    result.add(request);
				}
			} finally {
				csvReader.close();
			}
		} catch (Exception e) {
			LOG.error("Failed to load csv file " + csvFile, e);
			throw new DataLoadException("Failed to load csv file " + csvFile, e);
		} 
		
		return result;
	}
 	
}
