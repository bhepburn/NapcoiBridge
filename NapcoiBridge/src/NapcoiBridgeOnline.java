import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.NodeList;

public class NapcoiBridgeOnline {

	private static final String PROCESSING = "Processing";
	private static final String BAD = "Bad";
	private static final String GOOD = "Good";
	private static String sessionId = null;// "55b1a3de-fe8d-4834-b0b6-eb2df5417571";

	public static void main(String[] args) throws Exception {
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();
		boolean run = true;

		while (run) {
			String result = startConnection(soapConnection);
			while (result.equalsIgnoreCase(PROCESSING)) {
				Thread.sleep(2500);
				result = checkConnection(soapConnection);
			}

			while (result.equalsIgnoreCase(GOOD)) {
				Thread.sleep(2500);
				result = getKeypadStatus(soapConnection);
			}
		
			// End connection returns no response
			endConnection(soapConnection);
			run = false;
		}
		
		soapConnection.close();
	}

	private static String checkConnection(SOAPConnection soapConnection) throws Exception {
		SOAPMessage response = postCall(soapConnection, "CheckConnection");

		SOAPBody body = response.getSOAPBody();
		String result = getFirstElementValue(body, "CheckConnectionResult");
		if (result == null) result = BAD;

		return result;
	}

	private static String startConnection(SOAPConnection soapConnection) throws Exception {
		SOAPMessage response = postCall(soapConnection, "StartConnection");

		SOAPBody body = response.getSOAPBody();
		String result = getFirstElementValue(body, "StartConnectionResult");
		if (result == null) result = BAD;
		
		// Find the sessionId if it exists
		sessionId = getFirstElementValue(body, "sessionId");

		return result;
	}
	
	private static void endConnection(SOAPConnection soapConnection) throws Exception {
		postCall(soapConnection, "EndConnection");
		sessionId = null;
	}

	private static String getKeypadStatus(SOAPConnection soapConnection) throws Exception {
		SOAPMessage response = postCall(soapConnection, "GetKeypadStatus");

		SOAPBody body = response.getSOAPBody();
		String result = getFirstElementValue(body, "a:ConnectionResult");
		if (result == null) result = BAD;

		return result;
	}
	
	private static String getFirstElementValue(SOAPBody body, String elementName){
		NodeList elements = body.getElementsByTagName(elementName);
		if (elements.getLength() > 0) {
			return elements.item(0).getTextContent();
		}
		return null;
	}

	private static SOAPMessage postCall(SOAPConnection soapConnection, String command) throws Exception {
		URIBuilder b = new URIBuilder("https://security.ibridgeonline.com/RCMP.asp");
		b.addParameter("t", Double.toString(Math.random()));
		b.addParameter("Command", command);

		if (sessionId != null) {
			b.addParameter("sid", sessionId);
		} else {
			b.addParameter("Account", "");
			b.addParameter("Dealer", "");
		}

		// Send SOAP Message to SOAP Server
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPMessage soapResponse = soapConnection.call(soapMessage, b.toString());
	
		// Print for debugging
		if (soapResponse != null) {
			soapResponse.writeTo(System.out);
			System.out.println();
		}
		
		return soapResponse;
	}

}
