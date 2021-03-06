package org.wiztools.restclient.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.wiztools.restclient.XMLException;
import org.wiztools.restclient.bean.RequestBean;

/**
 *
 * @author subwiz
 */
public final class XMLCollectionUtil {

    private XMLCollectionUtil() {}
    
    public static void writeRequestCollectionXML(final List<RequestBean> requests, final File f)
            throws IOException, XMLException {
        Element eRoot = new Element("request-collection");
        for(RequestBean req: requests) {
            Element e = XmlRequestUtil.getRequestElement(req);
            eRoot.addContent(e);
        }
        Document doc = new Document(eRoot);
        XmlRequestUtil.writeXML(doc, f);
    }
    
    public static List<RequestBean> getRequestCollectionFromXMLFile(final File f)
            throws IOException, XMLException {
        List<RequestBean> out = new ArrayList<RequestBean>();
        Document doc = XmlRequestUtil.getDocumentFromFile(f);
        Element eRoot = doc.getRootElement();
        if(!"request-collection".equals(eRoot.getName())) {
            throw new XMLException("Expecting root element <request-collection>, but found: "
                    + eRoot.getName());
        }
        final String version = eRoot.getAttributeValue("version");
        List<Element> eRequests = doc.getRootElement().getChildren();
        for(int i=0; i<eRequests.size(); i++) {
            Element eRequest = eRequests.get(i);
            RequestBean req = XmlRequestUtil.getRequestBean(eRequest);
            out.add(req);
        }
        return out;
    }
    
}
