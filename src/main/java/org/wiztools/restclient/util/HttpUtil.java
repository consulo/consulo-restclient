package org.wiztools.restclient.util;

import consulo.util.collection.MultiMap;
import consulo.util.lang.StringUtil;
import org.wiztools.restclient.bean.ContentType;
import org.wiztools.restclient.bean.ContentTypeBean;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author subwiz
 */
public final class HttpUtil {
    
    public static final ContentType DEFAULT_CONTENT_TYPE = new ContentTypeBean("text/plain", StandardCharsets.UTF_8);
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    
    public static ContentType getContentType(String header) {
        if(header.contains("charset")) {
            Pattern p = Pattern.compile("([^;]+);\\s*charset=([^;]+)");
            Matcher m = p.matcher(header);
            if(m.matches()) {
                String contentType = m.group(1);
                Charset charset = Charset.forName(m.group(2));
                return new ContentTypeBean(contentType, charset);
            }
        }
        else { // no charset header
            return new ContentTypeBean(header, null);
        }
        return null;
    }
    
    public static ContentType getContentType(MultiMap<String, String> headers) {
        for(String key: headers.keySet()) {
            if("content-type".equalsIgnoreCase(key.trim())) {
                return getContentType(headers.get(key).iterator().next());
            }
        }
        return null;
    }
    
    /**
     * Parses the HTTP response status line, and returns the status code.
     * @param statusLine
     * @return The status code from HTTP response status line.
     */
    public static int getStatusCodeFromStatusLine(final String statusLine){
        int retVal = -1;
        final String STATUS_PATTERN = "[^\\s]+\\s([0-9]{3})\\s.*";
        Pattern p = Pattern.compile(STATUS_PATTERN);
        Matcher m = p.matcher(statusLine);
        if(m.matches()){
            retVal = Integer.parseInt(m.group(1));
        }
        return retVal;
    }

    /**
     * Method formats content-type and charset for use as HTTP header value
     * @param contentType
     * @param charset
     * @return The formatted content-type and charset.
     */
    public static String getFormattedContentType(final String contentType, final String charset){
        String charsetFormatted = StringUtil.isEmpty(charset)? "": "; charset=" + charset;
        return contentType + charsetFormatted;
    }
    
    public static String getFormattedContentType(final String contentType, final Charset charset){
        return getFormattedContentType(contentType, (charset!=null? charset.name(): null));
    }
    
    public static String getFormattedContentType(final ContentType contentType){
        return getFormattedContentType(contentType.getContentType(), contentType.getCharset());
    }

    public static String getCharsetFromContentType(final String contentType) {
        Pattern p = Pattern.compile("^.+charset=([^;]+).*$");
        Matcher m = p.matcher(contentType);
        if(m.matches()) {
            return m.group(1).trim();
        }
        return null;
    }

    /**
     * Parses the Content-Type HTTP header and returns the MIME type part of the
     * response. For example, when receiving Content-Type header like:
     *
     * application/xml;charset=UTF-8
     *
     * This method will return "application/xml".
     * @param contentType
     * @return
     */
    public static String getMimeFromContentType(final String contentType) {
        final int occurance = contentType.indexOf(';');
        if(occurance == -1) {
            return contentType;
        }
        else {
            return contentType.substring(0, occurance);
        }
    }
    
    private static String getContentTypeBeforeSemiColon(String contentType) {
        return (contentType.indexOf(';') != -1)?
                contentType.split(";")[0]:
                contentType;
    }
    
    public static boolean isWebImageContentType(final String contentType) {
        final String ct = getContentTypeBeforeSemiColon(contentType);
        if(ct.equals("image/jpeg")
                || ct.equals("image/png")
                || ct.equals("image/gif")) {
            return true;
        }
        return false;
    }
    
    public static boolean isTextContentType(final String contentType) {
        final String ct = getContentTypeBeforeSemiColon(contentType);
        if(ct.startsWith("text/")
                || isXmlContentType(ct)
                || isJsonContentType(ct)) {
            return true;
        }
        return false;
    }
    
    public static boolean isXmlContentType(final String contentType) {
        final String ct = getContentTypeBeforeSemiColon(contentType);
        if(ct.startsWith("application/xml")
                || ct.startsWith("text/xml")
                || ct.endsWith("+xml")){
            return true;
        }
        return false;
    }
    
    public static boolean isJsonContentType(final String contentType) {
        final String ct = getContentTypeBeforeSemiColon(contentType);
        if(ct.startsWith("application/json")
                || ct.endsWith("+json")){
            return true;
        }
        return false;
    }
    
    public static Charset getCharsetDefault(final ContentType type) {
        return type != null?
                (type.getCharset() != null? type.getCharset(): DEFAULT_CHARSET)
                : DEFAULT_CHARSET;
    }
}
