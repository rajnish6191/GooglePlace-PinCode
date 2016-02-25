package lat_lng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class LatLongNew {

	private static HttpClient httpClient = new HttpClient(
			new MultiThreadedHttpConnectionManager());

	public static void process(String input, String output, int indexAdd1,
			int indexAdd2) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output),
				true));
		String line = null;

		int cnt = 0;
		while ((line = br.readLine()) != null) {
			String result = "";
			cnt++;
			if (cnt % 3 == 0) {
				System.out.println("Count :" + cnt);
			}
			if (StringUtils.isNotBlank(line)) {
				try {
					String arr[] = StringUtils
							.splitPreserveAllTokens(line, "|");
					String res = "";
					for (int i = indexAdd1; i <= indexAdd2; i++) {
						res = res + "," + arr[i];
					}

					String TEXT_REQUEST_URL = "https://maps.googleapis.com/maps/api/place/textsearch/xml?query="
							+ res
							+ "&key=AIzaSyCZjZayN0XmZdh5cFO6zZh9bEK-4smTKMw";
					result = getLongitudeLatitude(TEXT_REQUEST_URL);
					for (int i = 0; i < arr.length; i++) {
						bw.write(arr[i] + "|");
					}
				} catch (Exception e) {
					e.printStackTrace();
					result = "null";
					System.out.println(line);
				}
				bw.write(result + "");
				bw.newLine();
			}
		}

		bw.flush();
		br.close();
		bw.close();

	}

	public static void main(String[] args) {
		try {
			process("/Users/RajnishKumar/Documents/Misc/CHM/Input/input_try.txt",
					"/Users/RajnishKumar/Documents/Misc/CHM/Output/Output_1g.txt",
					1, 4);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getLongitudeLatitude(String query) {
		String res = "";
		try {

			System.out.print(query);
			StringBuilder urlBuilder = new StringBuilder(query);

			final GetMethod getMethod = new GetMethod(urlBuilder.toString());
			try {
				httpClient.executeMethod(getMethod);
				Reader reader = new InputStreamReader(
						getMethod.getResponseBodyAsStream(),
						getMethod.getResponseCharSet());

				int data = reader.read();
				char[] buffer = new char[1024];
				Writer writer = new StringWriter();
				while ((data = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, data);
				}

				@SuppressWarnings("unused")
				String result = writer.toString();

				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader("<"
						+ writer.toString().trim()));
				Document doc = db.parse(is);

				String strstatus = getXpathValue(doc,
						"PlaceSearchResponse/status/text()");
				if (strstatus.equals("ZERO_RESULTS")) {
					return "ZERO_RESULTS";
				}
				String strLatitude = getXpathValue(doc,
						"PlaceSearchResponse/result/geometry/location/lat/text()");

				System.out.println("Latitude:" + strLatitude);

				String strLongtitude = getXpathValue(doc,
						"//PlaceSearchResponse/result/geometry/location/lng/text()");
				System.out.println("Longitude:" + strLongtitude);

				String strPincode = getXpathValue(doc,
						"//PlaceSearchResponse/result/formatted_address/text()").trim();

				String arr[] = StringUtils.splitPreserveAllTokens(strPincode,
						",");

				for (int i = 0; i < arr.length; i++) {
					String temp = arr[i].replaceAll("\\D+", "");
					if (temp.length() == 6) {
						strPincode = temp;
						break;
					} else
						strPincode = "";
				}

				// strPincode = strPincode.replaceAll("\\D+", "");

				System.out.println(strPincode);

				// String arr[] = StringUtils.splitPreserveAllTokens(strPincode,
				// ",");
				//
				// String pin[] = StringUtils.splitPreserveAllTokens(arr[1],
				// " ");
				// System.out.print(pin[pin.length - 1]);

				res = strLatitude + "|" + strLongtitude + "|" + strPincode;

			} finally {
				getMethod.releaseConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private static String getXpathValue(Document doc, String strXpath)
			throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xPath.compile(strXpath);
		String resultData = null;
		Object result4 = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result4;
		for (int i = 0; i < nodes.getLength(); i++) {
			resultData = nodes.item(i).getNodeValue();
		}
		return resultData;
	}
}