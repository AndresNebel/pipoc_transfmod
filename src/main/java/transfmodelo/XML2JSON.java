package transfmodelo;

import org.json.JSONObject;
import org.json.XML;

public class XML2JSON {
	public static String transform(String data) {
		JSONObject xmlJSONObj = XML.toJSONObject(data);
		System.out.println(xmlJSONObj.toString());
		return xmlJSONObj.toString();
	}
}
