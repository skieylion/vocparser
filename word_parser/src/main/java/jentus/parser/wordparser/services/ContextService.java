package jentus.parser.wordparser.services;

import jentus.parser.common.model.FormJson;
import jentus.parser.wordparser.domain.ContextFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;

public interface ContextService {
    FormJson convert(ContextFile contextFile) throws URISyntaxException, IOException, InterruptedException, ParserConfigurationException, SAXException, XPathExpressionException;
}
