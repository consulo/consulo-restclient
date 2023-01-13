package org.wiztools.restclient.bean;

import consulo.util.collection.MultiMap;

import java.io.Serializable;

/**
 *
 * @author subwiz
 */
public interface Response extends Cloneable, Serializable {

    long getExecutionTime();

    MultiMap<String, String> getHeaders();
    
    ContentType getContentType();

    byte[] getResponseBody();

    int getStatusCode();

    String getStatusLine();

    TestResult getTestResult();

    Object clone();
}
