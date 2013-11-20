package org.wiztools.restclient.bean;

import java.io.Serializable;
import java.net.HttpCookie;
import java.net.URL;
import java.util.List;

import com.intellij.util.containers.MultiMap;

/**
 *
 * @author subwiz
 */
public interface Request extends Cloneable, Serializable {

    Auth getAuth();

    ReqEntity getBody();

    MultiMap<String, String> getHeaders();
    
    List<HttpCookie> getCookies();

    HTTPVersion getHttpVersion();

    HTTPMethod getMethod();
    
    SSLReq getSslReq();

    String getTestScript();

    URL getUrl();

    boolean isFollowRedirect();
    
    boolean isIgnoreResponseBody();

    Object clone();
}
