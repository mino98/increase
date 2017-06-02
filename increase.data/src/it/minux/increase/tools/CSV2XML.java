package it.minux.increase.tools;

import it.minux.increase.data.CoverageRequest;
import it.minux.increase.data.CoverageRequestsLoader;
import it.minux.increase.data.DataLoadException;
import it.minux.increase.xml.CoverageRequestType;
import it.minux.increase.xml.CoverageRequests;
import it.minux.increase.xml.ObjectFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CSV2XML {

	private static final Log LOG = LogFactory.getLog(CSV2XML.class);

	private static void usage() {
		System.out.println("Covert coverage requests from CSV to XML");
		System.out.println("usage: CSV2XML csvFile xmlFile");
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			return;
		}

		File csvFile = new File(args[0]);
		File xmlFile = new File(args[1]);

		System.out.println("Converting " + csvFile + " to " + xmlFile);
		CSV2XML convertor = new CSV2XML(csvFile, xmlFile);
		try {
			convertor.run();
		} catch (Exception e) {
			LOG.error("Failed to convert", e);
			System.err.println("Failed to convert: " + e);
		}
	}

	private final File csvFile;
	private final File xmlFile;

	private void run() throws DataLoadException, DatatypeConfigurationException, FileNotFoundException, JAXBException {
		List<CoverageRequest> requests = CoverageRequestsLoader.INSTANCE.loadFromCSV(csvFile); 
		
		
		ObjectFactory objFactory = new ObjectFactory();
		DatatypeFactory typeFactory = DatatypeFactory.newInstance(); 
		CoverageRequests requestsXML = objFactory.createCoverageRequests();
		
		for (CoverageRequest req : requests) {
			CoverageRequestType reqXML = req.toJAXB(typeFactory);
			requestsXML.getCoverageRequest().add(reqXML);
		}
		
		FileOutputStream fos = new FileOutputStream(xmlFile);
		try {
			marshal(CoverageRequests.class, requestsXML, fos);
		} finally {
			try {
				fos.flush();
				fos.close();
			} catch (Exception e) {
				LOG.error("Failed to close stream", e);
			}
		}
	}

	protected final <T> void marshal(Class<T> docClass, T obj,
			OutputStream outputStream) throws JAXBException {
		String packageName = docClass.getPackage().getName();
		JAXBContext jc = JAXBContext.newInstance(packageName);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
		m.marshal(obj, outputStream);
	}

	public CSV2XML(File csvFile, File xmlFile) {
		this.csvFile = csvFile;
		this.xmlFile = xmlFile;
	}
}
