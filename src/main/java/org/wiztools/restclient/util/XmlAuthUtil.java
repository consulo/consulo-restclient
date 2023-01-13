package org.wiztools.restclient.util;

import consulo.util.lang.StringUtil;
import org.jdom.Element;
import org.wiztools.restclient.XMLException;
import org.wiztools.restclient.bean.*;

import java.util.List;

/**
 *
 * @author subwiz
 */
class XmlAuthUtil {
    private XmlAuthUtil() {}
    
    static Element getAuthElement(Auth auth) {
        Element eAuth = new Element("auth");
        
        if(auth instanceof BasicAuth) {
            eAuth.addContent(getBasicAuthElement((BasicAuth)auth));
        }
        else if(auth instanceof DigestAuth) {
            eAuth.addContent(getDigestAuthElement((DigestAuth)auth));
        }
        else if(auth instanceof NtlmAuth) {
            eAuth.addContent(getNtlmAuthElement((NtlmAuth)auth));
        }
        else if(auth instanceof OAuth2BearerAuth) {
            eAuth.addContent(getOAuth2BearerElement((OAuth2BearerAuth)auth));
        }
        
        return eAuth;
    }
    
    static Element getBasicAuthElement(BasicAuth auth) {
        Element e = new Element("basic");
        
        populateBasicDigestElement(e, auth);
        
        return e;
    }
    
    static Element getDigestAuthElement(DigestAuth auth) {
        Element e = new Element("digest");
        
        populateBasicDigestElement(e, auth);
        
        return e;
    }
    
    static Element getNtlmAuthElement(NtlmAuth auth) {
        Element e = new Element("ntlm");
        
        if(StringUtil.isNotEmpty(auth.getDomain())) {
            Element eDomain = new Element("domain");
            eDomain.addContent(auth.getDomain());
            e.addContent(eDomain);
        }
        
        if(StringUtil.isNotEmpty(auth.getWorkstation())) {
            Element eWorkstation = new Element("workstation");
            eWorkstation.addContent(auth.getWorkstation());
            e.addContent(eWorkstation);
        }
        
        populateUsernamePasswordElement(e, auth);
        
        return e;
    }
    
    static Element getOAuth2BearerElement(OAuth2BearerAuth auth) {
        Element e = new Element("oauth2-bearer");
        
        if(StringUtil.isNotEmpty(auth.getOAuth2BearerToken())) {
            Element eToken = new Element("token");
            eToken.addContent(auth.getOAuth2BearerToken());
            e.addContent(eToken);
        }
        
        return e;
    }
    
    static OAuth2BearerAuth getOAuth2BearerAuth(Element eAuth) {
        OAuth2BearerAuthBean out = new OAuth2BearerAuthBean();
        
        List<Element> eChildren = eAuth.getChildren();
        for(int i=0; i<eChildren.size(); i++) {
            Element e = eChildren.get(i);
            final String name = e.getName();
            if(name.equals("token")) {
                out.setOAuth2BearerToken(e.getValue());
            }
            else {
                throw new XMLException("Unknown element in oauth2-bearer auth: " + name);
            }
        }
        
        return out;
    }
    
    static void populateBasicDigestElement(Element eParent, BasicDigestAuth auth) {
        if(StringUtil.isNotEmpty(auth.getHost())) {
            Element eHost = new Element("host");
            eHost.addContent(auth.getHost());
            eParent.addContent(eHost);
        }
        
        if(StringUtil.isNotEmpty(auth.getRealm())) {
            Element eRealm = new Element("realm");
            eRealm.addContent(auth.getRealm());
            eParent.addContent(eRealm);
        }
        
        if(auth.isPreemptive()) {
            Element ePreemptive = new Element("preemptive");
            eParent.addContent(ePreemptive);
        }
        
        populateUsernamePasswordElement(eParent, auth);
    }
    
    static void populateUsernamePasswordElement(Element eParent, UsernamePasswordAuth auth) {
        if(StringUtil.isNotEmpty(auth.getUsername())) {
            Element eUsername = new Element("username");
            eUsername.addContent(auth.getUsername());
            eParent.addContent(eUsername);
        }
        
        if(auth.getPassword() != null && auth.getPassword().length > 0) {
            Element ePassword = new Element("password");
            ePassword.addContent(Util.base64encode(new String(auth.getPassword())));
            eParent.addContent(ePassword);
        }
    }
    
    static Auth getAuth(Element eAuth) {
        List<Element> eChildren = eAuth.getChildren();
        for(int i=0; i<eChildren.size(); i++) {
            Element e = eChildren.get(i);
            final String name = e.getName();
            if(name.equals("basic")) {
                return getBasicAuth(e);
            }
            else if(name.equals("digest")) {
                return getDigestAuth(e);
            }
            else if(name.equals("ntlm")) {
                return getNtlmAuth(e);
            }
            else if(name.equals("oauth2-bearer")) {
                return getOAuth2BearerAuth(e);
            }
            else {
                throw new XMLException("Invalid auth element encountered: " + name);
            }
        }
        return null;
    }
    
    static BasicAuth getBasicAuth(Element eBasicAuth) {
        BasicAuthBean out = new BasicAuthBean();
        
        populateBasicDigestAuth(out, eBasicAuth);
        
        return out;
    }
    
    static DigestAuth getDigestAuth(Element eDigestAuth) {
        DigestAuthBean out = new DigestAuthBean();
        
        populateBasicDigestAuth(out, eDigestAuth);
        
        return out;
    }
    
    static void populateBasicDigestAuth(BasicDigestAuthBaseBean bean, Element eAuth) {
        List<Element> eChildren = eAuth.getChildren();
        for(int i=0; i<eChildren.size(); i++) {
            Element e = eChildren.get(i);
            final String name = e.getName();
            if(name.equals("host")) {
                bean.setHost(e.getValue());
            }
            else if(name.equals("realm")) {
                bean.setRealm(e.getValue());
            }
            else if(name.equals("username")) {
                bean.setUsername(e.getValue());
            }
            else if(name.equals("password")) {
                bean.setPassword(getPassword(e));
            }
            else if(name.equals("preemptive")) {
                bean.setPreemptive(true);
            }
            else {
                throw new XMLException("Unknown element in basic/digest auth: " + name);
            }
        }
    }
    
    static NtlmAuth getNtlmAuth(Element eNtlmAuth) {
        NtlmAuthBean out = new NtlmAuthBean();
        
        List<Element> eChildren = eNtlmAuth.getChildren();
        for(int i=0; i<eChildren.size(); i++) {
            Element e = eChildren.get(i);
            final String name = e.getName();
            if(name.equals("domain")) {
                out.setDomain(e.getValue());
            }
            else if(name.equals("workstation")) {
                out.setWorkstation(e.getValue());
            }
            else if(name.equals("username")) {
                out.setUsername(e.getValue());
            }
            else if(name.equals("password")) {
                out.setPassword(getPassword(e));
            }
            else {
                throw new XMLException("Unknown element in ntlm auth: " + name);
            }
        }
        
        return out;
    }
    
    static char[] getPassword(Element ePassword) {
        return Util.base64decode(ePassword.getValue()).toCharArray();
    }
}
