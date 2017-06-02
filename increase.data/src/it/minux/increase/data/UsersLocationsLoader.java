package it.minux.increase.data;

import it.minux.increase.xml.UserLocationType;
import it.minux.increase.xml.UsersLocations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UsersLocationsLoader 
	extends XMLLoader
{
//	private static final Log LOG = LogFactory.getLog(InstallationFailuresLoader.class);
	public static final UsersLocationsLoader INSTANCE = new UsersLocationsLoader();
	
	private UsersLocationsLoader() {
	}
	
	public List<UserLocation> loadFromXML(File file) throws DataLoadException {
		try {
			UsersLocations xml = unmarshallFile(UsersLocations.class, file);
			List<UserLocation> result = new ArrayList<UserLocation>(xml.getUserLocation().size());
			for (UserLocationType userXML : xml.getUserLocation()) {
				result.add(new UserLocation(userXML));
			}
			
			return result;
		} catch (Exception e) {
			throw new DataLoadException("Failed to load xml file " + file, e);
		}
	}
}
