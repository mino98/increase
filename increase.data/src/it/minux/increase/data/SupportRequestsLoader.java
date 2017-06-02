package it.minux.increase.data;

import it.minux.increase.xml.SupportRequestType;
import it.minux.increase.xml.SupportRequests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SupportRequestsLoader 
	extends XMLLoader 
{
	public static final SupportRequestsLoader INSTANCE = new SupportRequestsLoader();
	
	private SupportRequestsLoader() {
	}
	
	public List<SupportRequest> loadFromXML(File file) throws DataLoadException {
		try {
			SupportRequests xml = unmarshallFile(SupportRequests.class, file);
			List<SupportRequest> result = new ArrayList<SupportRequest>(xml.getSupportRequest().size());
			for (SupportRequestType reqXML : xml.getSupportRequest()) {
				result.add(new SupportRequest(reqXML));
			}
			
			return result;
		} catch (Exception e) {
			throw new DataLoadException("Failed to load xml file " + file, e);
		}
	}
}
