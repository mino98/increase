package it.minux.increase.data;

import it.minux.increase.xml.InstallationFailureType;
import it.minux.increase.xml.InstallationFailures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InstallationFailuresLoader 
	extends XMLLoader
{
//	private static final Log LOG = LogFactory.getLog(InstallationFailuresLoader.class);
	public static final InstallationFailuresLoader INSTANCE = new InstallationFailuresLoader();
	
	private InstallationFailuresLoader() {
	}
	
	public List<InstallationFailure> loadFromXML(File file) throws DataLoadException {
		try {
			InstallationFailures xml = unmarshallFile(InstallationFailures.class, file);
			List<InstallationFailure> result = new ArrayList<InstallationFailure>(xml.getInstallationFailure().size());
			for (InstallationFailureType failXML : xml.getInstallationFailure()) {
				result.add(new InstallationFailure(failXML));
			}
			
			return result;
		} catch (Exception e) {
			throw new DataLoadException("Failed to load xml file " + file, e);
		}
	}
}
