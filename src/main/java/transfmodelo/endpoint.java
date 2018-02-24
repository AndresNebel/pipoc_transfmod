package transfmodelo;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.XML;
import org.json.JSONObject;

@Path("xml2json")
public class endpoint {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getDataJSON() {
		return "It works the transf!";
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_XML)
	public JSONObject xmlToJSON(String data) {
		JSONObject xmlJSONObj = XML.toJSONObject(data);
		System.out.println(xmlJSONObj.toString());
		return xmlJSONObj;
	}
	
}
