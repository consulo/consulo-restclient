package org.wiztools.restclient.util;

import consulo.util.lang.StringUtil;
import org.jdom.Element;
import org.wiztools.restclient.XMLException;
import org.wiztools.restclient.bean.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author subwiz
 */
class XmlBodyUtil {
    private XmlBodyUtil() {}
    
    static ReqEntity getReqEntity(Element eEntity) {
        List<Element> eChildren = eEntity.getChildren();
        for(int i=0; i<eChildren.size(); i++) {
            Element e = eChildren.get(i);
            final String name = e.getName();
            if("string".equals(name)) {
                ContentType ct = getContentType(e);
                String body = e.getValue();
                return new ReqEntityStringBean(body, ct);
            }
            else if("file".equals(name)) {
                ContentType ct = getContentType(e);
                String filePath = e.getValue();
                return new ReqEntityFileBean(new File(filePath), ct);
            }
            else if("byte-array".equals(name)) {
                ContentType ct = getContentType(e);
                byte[] body = Util.base64decodeByteArray(e.getValue());
                return new ReqEntityByteArrayBean(body, ct);
            }
            else if("url-stream".equals(name)) {
                try {
                    ContentType ct = getContentType(e);
                    URL url = new URL(e.getValue());
                    return new ReqEntityUrlStreamBean(ct, url);
                }
                catch(MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else if("multipart".equals(name)) {
                List<ReqEntityPart> parts = getMultipartParts(e);
                return new ReqEntityMultipartBean(parts);
            }
            else {
                throw new XMLException("Unsupported element encountered inside <body>: " + name);
            }
        }
        return null;
    }
    
    private static List<ReqEntityPart> getMultipartParts(Element e) {
        List<ReqEntityPart> parts = new ArrayList<ReqEntityPart>();
        List<Element> children = e.getChildren();
        for(int i=0; i<children.size(); i++) {
            ReqEntityPart part = getMultipartPart(children.get(i));
            parts.add(part);
        }
        return parts;
    }
    
    private static ReqEntityPart getMultipartPart(Element e) {
        final String name = e.getName();
        
        final String partName = e.getAttributeValue("name");
        final ContentType ct = getContentType(e);
        
        if("string".equals(name)) {
            String partBody = e.getValue();
            return new ReqEntityStringPartBean(partName, ct, partBody);
        }
        else if("file".equals(name)) {
            File file = new File(e.getValue());
            return new ReqEntityFilePartBean(partName, ct, file);
        }
        else {
            throw new XMLException("Unsupported element encountered inside <multipart>: " + name);
        }
    }
    
    private static ContentType getContentType(Element e) {
        String contentType = e.getAttributeValue("content-type");
        String charsetStr = e.getAttributeValue("charset");
        if(StringUtil.isNotEmpty(contentType)) {
            return new ContentTypeBean(contentType,
                (charsetStr!=null? Charset.forName(charsetStr): null));
        }
        else {
            return null;
        }
    }
    
    static Element getReqEntity(ReqEntity bean) {
        Element eBody = new Element("body");
        
        if(bean instanceof ReqEntityString) {
            ReqEntityString entity = (ReqEntityString) bean;
            
            Element eStringContent = new Element("string");
            addContentTypeCharsetAttribute(entity.getContentType(), eStringContent);
            eStringContent.addContent(entity.getBody());
            
            eBody.addContent(eStringContent);
        }
        else if(bean instanceof ReqEntityFile) {
            ReqEntityFile entity = (ReqEntityFile) bean;
            
            Element eFile = new Element("file");
            addContentTypeCharsetAttribute(entity.getContentType(), eFile);
            eFile.addContent(entity.getBody().getAbsolutePath());
            
            eBody.addContent(eFile);
        }
        else if(bean instanceof ReqEntityByteArray) {
            ReqEntityByteArray entity = (ReqEntityByteArray) bean;
            
            Element eByte = new Element("byte-array");
            addContentTypeCharsetAttribute(entity.getContentType(), eByte);
            eByte.addContent(Util.base64encode(entity.getBody()));
            
            eBody.addContent(eByte);
        }
        else if(bean instanceof ReqEntityUrlStream) {
            ReqEntityUrlStream entity = (ReqEntityUrlStream) bean;
            
            Element eUrlStream = new Element("url-stream");
            addContentTypeCharsetAttribute(entity.getContentType(), eUrlStream);
            eUrlStream.addContent(entity.getUrl().toString());
            
            eBody.addContent(eUrlStream);
        }
        else if(bean instanceof ReqEntityMultipart) {
            ReqEntityMultipart entity = (ReqEntityMultipart) bean;
            
            Element eMultipart = new Element("multipart");
            
            List<ReqEntityPart> parts = entity.getBody();
            for(ReqEntityPart part: parts) {
                if(part instanceof ReqEntityStringPart) {
                    ReqEntityStringPart p = (ReqEntityStringPart) part;
                    
                    Element ePart = new Element("string");
                    addContentTypeCharsetAttribute(p.getContentType(), ePart);
                    ePart.setAttribute("name", p.getName());
                    ePart.addContent(p.getPart());
                    
                    eMultipart.addContent(ePart);
                }
                else if(part instanceof ReqEntityFilePart) {
                    ReqEntityFilePart p = (ReqEntityFilePart) part;
                    
                    Element ePart = new Element("file");
                    addContentTypeCharsetAttribute(p.getContentType(), ePart);
                    ePart.setAttribute("name", p.getName());
                    ePart.addContent(p.getPart().getAbsolutePath());
                    
                    eMultipart.addContent(ePart);
                }
            }
            
            eBody.addContent(eMultipart);
        }
        
        return eBody;
    }
    
    private static void addContentTypeCharsetAttribute(ContentType c, Element e) {
        if(c != null) {
            e.setAttribute("content-type", c.getContentType());
            if(c.getCharset() != null) {
                e.setAttribute("charset", c.getCharset().name());
            }
        }
    }
}
