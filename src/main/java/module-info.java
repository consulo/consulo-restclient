/**
 * @author VISTALL
 * @since 13/01/2023
 */
module consulo.restclient
{
	requires consulo.ide.api;

	requires org.apache.httpcomponents.core5.httpcore5;
	requires org.apache.httpcomponents.core5.httpcore5.h2;
	requires org.apache.httpcomponents.client5.httpclient5;

	// TODO remove in future
	requires java.desktop;
	requires forms.rt;
}