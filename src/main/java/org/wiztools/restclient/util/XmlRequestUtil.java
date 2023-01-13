package org.wiztools.restclient.util;

import consulo.logging.Logger;
import consulo.util.collection.MultiMap;
import consulo.util.jdom.JDOMUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.wiztools.restclient.XMLException;
import org.wiztools.restclient.bean.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rsubramanian
 */
public final class XmlRequestUtil{
    private XmlRequestUtil() {
    }
    private static final Logger LOG = Logger.getInstance(XmlRequestUtil.class);

    public static final String XML_MIME = "application/xml";

    private static Element getRootElement() {
        return new Element("rest-client");
    }

    public static Element getRequestElement(final RequestBean bean) {
        Element reqElement = new Element("request");

        if(bean.isFollowRedirect()) { // HTTP Follow Redirect
            Element e = new Element("http-follow-redirects");
            reqElement.addContent(e);
        }

        if(bean.isIgnoreResponseBody()) { // Response body ignored
            Element e = new Element("ignore-response-body");
            reqElement.addContent(e);
        }

        { // creating the URL child element
            Element e = new Element("URL");
            e.addContent(bean.getUrl().toString());
            reqElement.addContent(e);
        }

        { // creating the method child element
            Element e = new Element("method");
            e.addContent(bean.getMethod());
            reqElement.addContent(e);
        }

        { // auth
            Auth auth = bean.getAuth();
            if(auth != null) {
                Element eAuth = XmlAuthUtil.getAuthElement(auth);
                reqElement.addContent(eAuth);
            }
        }

        // Creating SSL elements
        if(bean.getSslReq() != null) {
            Element eSsl = XmlSslUtil.getSslReq(bean.getSslReq());
            reqElement.addContent(eSsl);
        }

        // creating the headers child element
        MultiMap<String, String> headers = bean.getHeaders();
        if (!headers.isEmpty()) {
            Element e = new Element("headers");
            for (String key : headers.keySet()) {
                for(String value: headers.get(key)) {
                    Element ee = new Element("header");
                    ee.setAttribute("key", key);
                    ee.setAttribute("value", value);
                    e.addContent(ee);
                }
            }
            reqElement.addContent(e);
        }

        // Cookies
        List<HttpCookie> cookies = bean.getCookies();
        if(!cookies.isEmpty()) {
            Element e = new Element("cookies");
            for(HttpCookie cookie: cookies) {
                Element ee = new Element("cookie");
                ee.setAttribute("name", cookie.getName());
                ee.setAttribute("value", cookie.getValue());
                e.addContent(ee);
            }
            reqElement.addContent(e);
        }

        { // creating the body child element
            ReqEntity entityBean = bean.getBody();
            if(entityBean != null) {
                Element e = XmlBodyUtil.getReqEntity(entityBean);
                reqElement.addContent(e);
            }
        }

        // creating the test-script child element
        String testScript = bean.getTestScript();
        if (testScript != null) {

            Element e = new Element("test-script");
            e.addContent(testScript);
            reqElement.addContent(e);
        }
        return reqElement;
    }

    protected static Document request2XML(final RequestBean bean)
            throws XMLException {
        Element reqRootElement = getRootElement();
        reqRootElement.addContent(getRequestElement(bean));

        Document xomDocument = new Document(reqRootElement);
        return xomDocument;
    }

    private static Map<String, String> getHeadersFromHeaderNode(final Element node)
            throws XMLException {
        Map<String, String> m = new LinkedHashMap<String, String>();

        for (int i = 0; i < node.getChildren().size(); i++) {
            Element headerElement = node.getChildren().get(i);
            if (!"header".equals(headerElement.getQualifiedName())) {
                throw new XMLException("<headers> element should contain only <header> elements");
            }

            m.put(headerElement.getAttributeValue("key"),
                    headerElement.getAttributeValue("value"));

        }
        return m;
    }

    private static List<HttpCookie> getCookiesFromCookiesNode(final Element node)
            throws XMLException {
        List<HttpCookie> out = new ArrayList<HttpCookie>();

        for (int i = 0; i < node.getChildren().size(); i++) {
            Element e = node.getChildren().get(i);
            if(!"cookie".equals(e.getQualifiedName())) {
                throw new XMLException("<cookies> element should contain only <cookie> elements");
            }

            HttpCookie cookie = new HttpCookie(e.getAttributeValue("name"),
                    e.getAttributeValue("value"));
            out.add(cookie);
        }

        return out;
    }

    public static RequestBean getRequestBean(Element requestNode)
            throws MalformedURLException, XMLException {
        RequestBean requestBean = new RequestBean();

        for (int i = 0; i < requestNode.getChildren().size(); i++) {
            Element tNode = requestNode.getChildren().get(i);
            String nodeName = tNode.getQualifiedName();
            if("http-follow-redirects".equals(nodeName)) {
                requestBean.setFollowRedirect(true);
            }
            else if("ignore-response-body".equals(nodeName)) {
                requestBean.setIgnoreResponseBody(true);
            }
            else if ("URL".equals(nodeName)) {
                URL url = new URL(tNode.getValue());
                requestBean.setUrl(url);
            }
            else if ("method".equals(nodeName)) {
                requestBean.setMethod(tNode.getValue());
            }
            else if("auth".equals(nodeName)) {
                requestBean.setAuth(XmlAuthUtil.getAuth(tNode));
            }
            else if("ssl".equals(nodeName)) {
                requestBean.setSslReq(XmlSslUtil.getSslReq(tNode));
            }
            else if ("headers".equals(nodeName)) {
                Map<String, String> m = getHeadersFromHeaderNode(tNode);
                for (String key : m.keySet()) {
                    requestBean.addHeader(key, m.get(key));
                }
            }
            else if ("cookies".equals(nodeName)) {
                List<HttpCookie> cookies = getCookiesFromCookiesNode(tNode);
                for (HttpCookie cookie: cookies) {
                    requestBean.addCookie(cookie);
                }
            }
            else if ("body".equals(nodeName)) {
                ReqEntity body = XmlBodyUtil.getReqEntity(tNode);
                requestBean.setBody(body);
            }
            else if ("test-script".equals(nodeName)) {
                requestBean.setTestScript(tNode.getValue());
            }
        }
        return requestBean;
    }

    protected static RequestBean xml2Request(final Document doc)
            throws MalformedURLException, XMLException {
        // get the rootNode
        Element rootNode = doc.getRootElement();

        if (!"rest-client".equals(rootNode.getQualifiedName())) {
            throw new XMLException("Root node is not <rest-client>");
        }

        // if more than two request element is present then throw the exception
        if (rootNode.getChildren().size() != 1) {
            throw new XMLException("There can be only one child node for root node: <request>");
        }
        // minimum one request element is present in xml
        if (rootNode.getChild("request") == null) {
            throw new XMLException("The child node of <rest-client> should be <request>");
        }
        Element requestNode = rootNode.getChild("request");

        return getRequestBean(requestNode);
    }

    protected static Element getResponseElement(final Response bean) {
        Element respElement = new Element("response");
        Element respChildSubElement = null;
        Element respChildSubSubElement = null;

        // adding first sub child element - execution-time and append to response child element
        respChildSubElement = new Element("execution-time");
        respChildSubElement.addContent(String.valueOf(bean.getExecutionTime()));
        respElement.addContent(respChildSubElement);

        // adding second sub child element - status and code attributes and append to response child element
        respChildSubElement = new Element("status");
        respChildSubElement.setAttribute("code", String.valueOf(bean.getStatusCode()));
        respChildSubElement.addContent(bean.getStatusLine());
        respElement.addContent(respChildSubElement);

        // adding third sub child element - headers
        MultiMap<String, String> headers = bean.getHeaders();
        if (!headers.isEmpty()) {

            // creating sub child-child element
            respChildSubElement = new Element("headers");
            for (String key : headers.keySet()) {
                for(String value: headers.get(key)) {
                    respChildSubSubElement = new Element("header");

                    respChildSubSubElement.setAttribute("key", key);
                    respChildSubSubElement.setAttribute("value", value);
                    respChildSubElement.addContent(respChildSubSubElement);
                }
            }
            // add response child element - headers
            respElement.addContent(respChildSubElement);
        }

        byte[] responseBody = bean.getResponseBody();
        if (responseBody != null) {
            //creating the body child element and append to response child element
            respChildSubElement = new Element("body");
            final String base64encodedBody = Util.base64encode(responseBody);
            respChildSubElement.addContent(base64encodedBody);
            respElement.addContent(respChildSubElement);
        }
        // test result
        TestResult testResult = bean.getTestResult();
        if (testResult != null) {
            //creating the test-result child element
            respChildSubElement = new Element("test-result");

            // Counts:
            Element e_runCount = new Element("run-count");
            e_runCount.addContent(String.valueOf(testResult.getRunCount()));
            Element e_failureCount = new Element("failure-count");
            e_failureCount.addContent(String.valueOf(testResult.getFailureCount()));
            Element e_errorCount = new Element("error-count");
            e_errorCount.addContent(String.valueOf(testResult.getErrorCount()));
            respChildSubElement.addContent(e_runCount);
            respChildSubElement.addContent(e_failureCount);
            respChildSubElement.addContent(e_errorCount);

            // Failures
            if (testResult.getFailureCount() > 0) {
                Element e_failures = new Element("failures");
                List<TestExceptionResult> l = testResult.getFailures();
                for (TestExceptionResult b : l) {
                    Element e_message = new Element("message");
                    e_message.addContent(b.getExceptionMessage());
                    Element e_line = new Element("line-number");
                    e_line.addContent(String.valueOf(b.getLineNumber()));
                    Element e_failure = new Element("failure");
                    e_failure.addContent(e_message);
                    e_failure.addContent(e_line);
                    e_failures.addContent(e_failure);
                }
                respChildSubElement.addContent(e_failures);
            }

            //Errors
            if (testResult.getErrorCount() > 0) {
                Element e_errors = new Element("errors");
                List<TestExceptionResult> l = testResult.getErrors();
                for (TestExceptionResult b : l) {
                    Element e_message = new Element("message");
                    e_message.addContent(b.getExceptionMessage());
                    Element e_line = new Element("line-number");
                    e_line.addContent(String.valueOf(b.getLineNumber()));
                    Element e_error = new Element("error");
                    e_error.addContent(e_message);
                    e_error.addContent(e_line);
                    e_errors.addContent(e_error);
                }
                respChildSubElement.addContent(e_errors);
            }
            // Trace
            Element e_trace = new Element("trace");
            e_trace.addContent(testResult.toString());
            respChildSubElement.addContent(e_trace);

            respElement.addContent(respChildSubElement);
        }
        return respElement;
    }

    protected static Document response2XML(final Response bean)
            throws XMLException {
        Element respRootElement = getRootElement();
        respRootElement.addContent(getResponseElement(bean));

        Document xomDocument = new Document(respRootElement);
        return xomDocument;
    }

    protected static Response xml2Response(final Document doc)
            throws XMLException {
        ResponseBean responseBean = new ResponseBean();

        // get the rootNode
        Element rootNode = doc.getRootElement();

        if (!"rest-client".equals(rootNode.getQualifiedName())) {
            throw new XMLException("Root node is not <rest-client>");
        }


        // assign rootnode to current node and also finding 'response' node
        Element tNode = null;
        Element responseNode = null;

        // if more than two request element is present then throw the exception
        if (rootNode.getChildren().size() != 1) {
            throw new XMLException("There can be only one child node for root node: <response>");
        }
        // minimum one response element is present in xml
        if (rootNode.getChild("response") == null) {
            throw new XMLException("The child node of <rest-client> should be <response>");
        }
        responseNode = rootNode.getChild("response");
        for (int i = 0; i < responseNode.getChildren().size(); i++) {
            tNode = responseNode.getChildren().get(i);
            String nodeName = tNode.getQualifiedName();

            if ("execution-time".equals(nodeName)) {
                responseBean.setExecutionTime(Long.parseLong(tNode.getValue()));
            } else if ("status".equals(nodeName)) {
                responseBean.setStatusLine(tNode.getValue());
                responseBean.setStatusCode(Integer.parseInt(tNode.getAttributeValue("code")));
            } else if ("headers".equals(nodeName)) {
                Map<String, String> m = getHeadersFromHeaderNode(tNode);
                for (String key : m.keySet()) {
                    responseBean.addHeader(key, m.get(key));
                }
            } else if ("body".equals(nodeName)) {
                final String base64body = tNode.getValue();
                responseBean.setResponseBody(Util.base64decodeByteArray(base64body));
            } else if ("test-result".equals(nodeName)) {
                TestResultBean testResultBean = new TestResultBean();

                for (int j = 0; j < tNode.getChildren().size(); j++) {
                    String nn = tNode.getQualifiedName();
                    if ("run-count".equals(nn)) {
                        throw new XMLException("<headers> element should contain only <header> elements");
                    } else if ("failure-count".equals(nn)) {
                        throw new XMLException("<headers> element should contain only <header> elements");
                    } else if ("error-count".equals(nn)) {
                        throw new XMLException("<headers> element should contain only <header> elements");
                    } else if ("failures".equals(nn)) {
                        throw new XMLException("<headers> element should contain only <header> elements");
                    } else if ("errors".equals(nn)) {
                        throw new XMLException("<headers> element should contain only <header> elements");
                    }
                }
                responseBean.setTestResult(testResultBean);
            } else {
                throw new XMLException("Unrecognized element found: <" + nodeName + ">");
            }
        }
        return responseBean;
    }

    protected static void writeXML(final Document doc, final File f)
            throws IOException, XMLException {

        try {
			JDOMUtil.writeDocument(doc, f, "\n");
        } catch (IOException ex) {
            throw new XMLException(ex.getMessage(), ex);
        }
    }

    protected static Document getDocumentFromFile(final File f)
            throws IOException, XMLException {
        try {
       		return JDOMUtil.loadDocument(f);
        } catch (JDOMException ex) {
            throw new XMLException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new XMLException(ex.getMessage(), ex);
        }

    }

    public static String getDocumentCharset(final File f)
            throws IOException, XMLException {
        XMLEventReader reader = null;
        try {
            // using stax to get xml factory objects and read the input file
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            reader = inputFactory.createXMLEventReader(new FileInputStream(f));
            XMLEvent event = reader.nextEvent();
            // Always the first element is StartDocument
            // even if the XML does not have explicit declaration:
            StartDocument document = (StartDocument) event;
            return document.getCharacterEncodingScheme();
        }
        catch (XMLStreamException ex) {
            throw new XMLException(ex.getMessage(), ex);
        }
        finally{
            if(reader != null){
                try{
                    reader.close();
                }
                catch(XMLStreamException ex){
                    LOG.warn(ex.getMessage());
                }
            }
        }
    }

    public static void writeRequestXML(final RequestBean bean, final File f)
            throws IOException, XMLException {
        Document doc = request2XML(bean);
        writeXML(doc, f);
    }

    public static void writeResponseXML(final Response bean, final File f)
            throws IOException, XMLException {
        Document doc = response2XML(bean);
        writeXML(doc, f);
    }

    public static RequestBean getRequestFromXMLFile(final File f)
            throws IOException, XMLException {
        Document doc = getDocumentFromFile(f);
        return xml2Request(doc);
    }

    public static Response getResponseFromXMLFile(final File f)
            throws IOException, XMLException {
        Document doc = getDocumentFromFile(f);
        return xml2Response(doc);
    }
}
