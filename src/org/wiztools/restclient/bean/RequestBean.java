package org.wiztools.restclient.bean;

import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.intellij.util.containers.MultiMap;
import com.squareup.okhttp.Protocol;

/**
 *
 * @author subwiz
 */
public final class RequestBean {
    
    private URL url;
    private String method = "GET";
    private Auth auth;
    private final MultiMap<String, String> headers = new MultiMap<String, String>();
    private final List<HttpCookie> cookies = new ArrayList<HttpCookie>();
    private ReqEntity body;
    private String testScript;
    private SSLReq sslReq;
    private Protocol httpVersion = Protocol.HTTP_1_1;
    private boolean isFollowRedirect;
    private boolean isIgnoreResponseBody = false;
    
    public void setAuth(Auth auth) {
        this.auth = auth;
    }
    
    
    public Auth getAuth() {
        return auth;
    }

    
    public SSLReq getSslReq() {
        return sslReq;
    }

    public void setSslReq(SSLReq sslReq) {
        this.sslReq = sslReq;
    }

    
    public Protocol getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(Protocol httpVersion) {
        this.httpVersion = httpVersion;
    }
    
    
    public String getTestScript() {
        return testScript;
    }

    public void setTestScript(String testScript) {
        this.testScript = testScript;
    }

    
    public ReqEntity getBody() {
        return body;
    }
    
    public void setBody(final ReqEntity body){
        this.body = body;
    }

    
    public MultiMap<String, String> getHeaders() {
        return new MultiMap<String, String>(headers);
    }

    public void addHeader(final String key, final String value){
        this.headers.putValue(key, value);
    }
    
    public void addCookie(HttpCookie cookie) {
        this.cookies.add(cookie);
    }
    
    
    public List<HttpCookie> getCookies() {
        return Collections.unmodifiableList(this.cookies);
    }

    
    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    
    public boolean isFollowRedirect() {
        return isFollowRedirect;
    }

    public void setFollowRedirect(boolean isFollowRedirect) {
        this.isFollowRedirect = isFollowRedirect;
    }
    
    public void setIgnoreResponseBody(boolean isIgnoreResponseBody) {
        this.isIgnoreResponseBody = isIgnoreResponseBody;
    }
    
    
    public boolean isIgnoreResponseBody() {
        return isIgnoreResponseBody;
    }
    
    
    public Object clone(){
        RequestBean cloned = new RequestBean();
        cloned.setSslReq(sslReq);
        cloned.setHttpVersion(httpVersion);
        if(body != null){
            cloned.setBody((ReqEntityStringBean)body.clone());
        }
        if(!headers.isEmpty()){
            for(String header: headers.keySet()){
                for(String value: headers.get(header)) {
                    cloned.addHeader(header, value);
                }
            }
        }
        if(!cookies.isEmpty()) {
            for(HttpCookie cookie: cookies) {
                cloned.addCookie(cookie);
            }
        }
        cloned.setMethod(method);
        cloned.setTestScript(testScript);
        cloned.setUrl(url);
        cloned.setFollowRedirect(isFollowRedirect);
        cloned.setIgnoreResponseBody(isIgnoreResponseBody);
        return cloned;
    }

    
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RequestBean other = (RequestBean) obj;
        if (this.url != other.url && (this.url == null || !this.url.equals(other.url))) {
            return false;
        }
        if (this.method != other.method) {
            return false;
        }
        if (this.auth != other.auth && (this.auth == null || !this.auth.equals(other.auth))) {
            return false;
        }
        if (this.headers != other.headers && (this.headers == null || !this.headers.equals(other.headers))) {
            return false;
        }
        if (this.cookies != other.cookies && (this.cookies == null || !this.cookies.equals(other.cookies))) {
            return false;
        }
        if (this.body != other.body && (this.body == null || !this.body.equals(other.body))) {
            return false;
        }
        if ((this.testScript == null) ? (other.testScript != null) : !this.testScript.equals(other.testScript)) {
            return false;
        }
        if (this.sslReq != other.sslReq && (this.sslReq == null || !this.sslReq.equals(other.sslReq))) {
            return false;
        }
        if (this.httpVersion != other.httpVersion) {
            return false;
        }
        if (this.isFollowRedirect != other.isFollowRedirect) {
            return false;
        }
        if (this.isIgnoreResponseBody != other.isIgnoreResponseBody) {
            return false;
        }
        return true;
    }

    
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 23 * hash + (this.method != null ? this.method.hashCode() : 0);
        hash = 23 * hash + (this.auth != null ? this.auth.hashCode() : 0);
        hash = 23 * hash + (this.headers != null ? this.headers.hashCode() : 0);
        hash = 23 * hash + (this.cookies != null ? this.cookies.hashCode() : 0);
        hash = 23 * hash + (this.body != null ? this.body.hashCode() : 0);
        hash = 23 * hash + (this.testScript != null ? this.testScript.hashCode() : 0);
        hash = 23 * hash + (this.sslReq != null ? this.sslReq.hashCode() : 0);
        hash = 23 * hash + (this.httpVersion != null ? this.httpVersion.hashCode() : 0);
        hash = 23 * hash + (this.isFollowRedirect ? 1 : 0);
        hash = 23 * hash + (this.isIgnoreResponseBody ? 1 : 0);
        return hash;
    }

    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("@Request[");
        sb.append(url).append(", ");
        sb.append(method).append(", ");
        sb.append(headers).append(", ");
        sb.append(cookies).append(", ");
        sb.append(body).append(", ");
        sb.append(auth).append(", ");
        sb.append(sslReq).append(", ");
        sb.append(httpVersion).append(", ");
        sb.append(isFollowRedirect).append(", ");
        sb.append(isIgnoreResponseBody).append(", ");
        sb.append(testScript);
        sb.append("]");
        return sb.toString();
    }
}
