package transfmodelo;

import org.json.JSONObject;
import org.json.XML;

public class XML2JSON {
	public static String transform(String data) {
		JSONObject xmlJSONObj = XML.toJSONObject(data);
		System.out.println("Transformando, tamaño: "+ data.length());
		return xmlJSONObj.toString();
	}
}
