package it.minux.increase.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XMLLoader {

	private static final Log LOG = LogFactory.getLog(XMLLoader.class);
	
	protected final <T> T unmarshal(Class<T> docClass, InputStream inputStream)
			throws JAXBException {
		String packageName = docClass.getPackage().getName();
		JAXBContext jc = JAXBContext.newInstance(packageName);
		Unmarshaller u = jc.createUnmarshaller();
		// JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( inputStream );
		Object doc = u.unmarshal(inputStream);
		return (T) doc;
	}
	
	protected final <T> T unmarshallFile(Class<T> docClass, File file) throws JAXBException, FileNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return unmarshal(docClass, fis);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				LOG.error("Failed to close stream", e);
			}
		}
	}

}
