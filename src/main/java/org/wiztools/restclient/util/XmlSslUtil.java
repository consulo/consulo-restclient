package org.wiztools.restclient.util;

import consulo.util.lang.StringUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.wiztools.restclient.bean.SSLHostnameVerifier;
import org.wiztools.restclient.bean.SSLReq;
import org.wiztools.restclient.bean.SSLReqBean;

import java.util.List;

/**
 *
 * @author subwiz
 */
class XmlSslUtil {
    private XmlSslUtil() {}
    
    static Element getSslReq(SSLReq req) {
        Element eSsl = new Element("ssl");
        
        if(req.isTrustSelfSignedCert()) {
            Element e = new Element("trust-self-signed-cert");
            eSsl.addContent(e);
        }
        
        { // Hostname verifier
            Element e = new Element("hostname-verifier");
            e.addContent(req.getHostNameVerifier().name());
            eSsl.addContent(e);
        }
        
        // Key store
        if(StringUtil.isNotEmpty(req.getKeyStore())) {
            Element e = new Element("keystore");
            e.setAttribute("file", req.getKeyStore());
			e.setAttribute("password", Util.base64encode(new String(req.getKeyStorePassword())));
            eSsl.addContent(e);
        }
        
        // Trust store
        if(StringUtil.isNotEmpty(req.getTrustStore())) {
            Element e = new Element("truststore");
			e.setAttribute(new Attribute("file", req.getTrustStore()));
			e.setAttribute(new Attribute("password", Util.base64encode(new String(req.getTrustStorePassword()))));
            eSsl.addContent(e);
        }
        
        return eSsl;
    }
    
    static SSLReq getSslReq(Element eSsl) {
        SSLReqBean out = new SSLReqBean();
        
        List<Element> eChildren = eSsl.getChildren();
        for(int i=0; i<eChildren.size(); i++) {
            Element e = eChildren.get(i);
            final String name = e.getName();
            if("trust-self-signed-cert".equals(name)) {
                out.setTrustSelfSignedCert(true);
            }
            else if("hostname-verifier".equals(name)) {
                out.setHostNameVerifier(SSLHostnameVerifier.valueOf(e.getValue()));
            }
            else if("keystore".equals(name)) {
                out.setKeyStore(e.getAttributeValue("file"));
                out.setKeyStorePassword(Util.base64decode(e.getAttributeValue("password")).toCharArray());
            }
            else if("truststore".equals(name)) {
                out.setTrustStore(e.getAttributeValue("file"));
                out.setTrustStorePassword(Util.base64decode(e.getAttributeValue("password")).toCharArray());
            }
        }
        
        return out;
    }
}
