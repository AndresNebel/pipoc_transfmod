package transfmodelo;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static java.lang.System.getenv;


@Path("xml2json")
public class SyncEndpoint {
	 
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getDataJSON() {
		return "El modulo de transformación está disponible y sano.";
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_XML)
	public String xmlToJSON(String data) {
		
		String processedData = XML2JSON.transform(data);
		
		if (getNextStep() == "Fin")
			return processedData;
		else 			
			return callSync2NextStep(processedData);
		
	}
	
	
	public String callSync2NextStep(String message){
		//TODO: Parametrizar si invocar con GET o POST al next step
		HttpPost req = new HttpPost(getNextStepURL()); 
		HttpClient httpClient = HttpClients.createDefault();
		HttpResponse internalResponse;	
		String responseStr = "";
		try {
			req.setEntity(new StringEntity(message));
			req.setHeader("Content-Type", "application/json");
			internalResponse = httpClient.execute(req);
			responseStr = EntityUtils.toString(internalResponse.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return responseStr;
	} 
	
	
	public  String getNextStep(){
		return getenv("nextstep");
	}
	
	public String getNextStepURL() {
		String resourcePath = getenv("nextstep_syncpath");
		String baseUrl = "";
		String nextStepName = getNextStep().toUpperCase();
		if (!isEmpty(getenv(nextStepName+"_SERVICE_HOST")) && !isEmpty(getenv(nextStepName+"_SERVICE_PORT")))
			baseUrl = "http://" + getenv(nextStepName+"_SERVICE_HOST") + ":" + System.getenv(nextStepName+"_SERVICE_PORT"); 
				
		return baseUrl + resourcePath;
	}
	
	public  String getNextStepContentType(){
		return getenv("nextstep_contenttype");
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
}
