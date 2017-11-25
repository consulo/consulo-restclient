package org.wiztools.restclient.bean;

import java.io.Serializable;

import com.intellij.util.containers.MultiMap;

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
