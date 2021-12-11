package jentus.audioloader.services;

import jentus.audioloader.model.AudioFile;
import jentus.audioloader.model.FormJson;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface FormService {
    List<AudioFile> convert(FormJson formJson) throws IOException;
}
