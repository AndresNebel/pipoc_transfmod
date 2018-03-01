package transfmodelo;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static java.lang.System.getenv;

import java.io.IOException;

@Path("xml2json")
public class RestEndpoint {
	 
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
		else {
			HttpPost req = new HttpPost(getNextStepURL()); //Esa parte deberia estar parametrizada tambien
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse internalResponse;	
			String responseStr = "";
			try {
				req.setEntity(new StringEntity(processedData));
				req.setHeader("Content-Type", "application/json");
				internalResponse = httpClient.execute(req);
				responseStr = EntityUtils.toString(internalResponse.getEntity());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return responseStr;
		}
	}
	
	
	public String getNextStepURL() {
		String resourcePath = getenv("nextstep_syncpath");
		String baseUrl = "";
		String nextStepName = getNextStep().toUpperCase();
		if (!isEmpty(getenv(nextStepName+"_SERVICE_HOST")) && !isEmpty(getenv(nextStepName+"_SERVICE_PORT")))
			baseUrl = "http://" + getenv(nextStepName+"_SERVICE_HOST") + ":" + System.getenv(nextStepName+"_SERVICE_PORT"); 
				
		return baseUrl + resourcePath;
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	public  String getNextStep(){
		return getenv("nextstep");
	}
	
	public  String getNextStepContentType(){
		return getenv("nextstep_contenttype");
	}
}
