package org.wiztools.restclient.bean;

/**
 *
 * @author rsubramanian
 */
public final class ReqResBean implements Cloneable{
    
    private RequestBean requestBean;
    private Response responseBean;

    public RequestBean getRequestBean() {
        return requestBean;
    }

    public void setRequestBean(RequestBean requestBean) {
        this.requestBean = requestBean;
    }

    public Response getResponseBean() {
        return responseBean;
    }

    public void setResponseBean(Response responseBean) {
        this.responseBean = responseBean;
    }
    
    @Override
    public Object clone(){
        ReqResBean cloned = new ReqResBean();
        if(requestBean != null){
			RequestBean clonedRequestBean = (RequestBean)requestBean.clone();
            cloned.requestBean = clonedRequestBean;
        }
        if(responseBean != null){
            //Response clonedResponseBean = (Response)responseBean.clone();
            //cloned.responseBean = clonedResponseBean;
        }
        return cloned;
    }
}
