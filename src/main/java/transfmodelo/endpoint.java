package transfmodelo;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.XML;
import org.json.JSONObject;

import static java.lang.System.getenv;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

@Path("xml2json")
public class endpoint {
	public static String nextStepName = ""; 
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getDataJSON() {
		return "It works the transf!";
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_XML)
	public String xmlToJSON(String data) {
		JSONObject xmlJSONObj = XML.toJSONObject(data);
		System.out.println(xmlJSONObj.toString());
		return xmlJSONObj.toString();
	}
	@Path("initMQ")
	@POST
	public void initializeMessageQueue2() {
		System.out.println("transfmodelo: Initializing MQ");
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {
			public void run() {
				System.out.println("transfmodelo: RabbitMQ Receiver Thread initializing..");
				System.out.println("Las env son:");
				Map<String,String> mapEnv = getenv();
				for (String key : mapEnv.keySet()) {
					System.out.println(key);
				}
				if (getenv("TRANSFMODELO_NEXTSTEP") != null) 
					System.out.println("1" + getenv("TRANSFMODELO_NEXTSTEP"));
				else if (getenv("TRANSFMODELO_TRANSF_NEXTSTEP") != null) 
					System.out.println("2" + getenv("TRANSFMODELO_TRANSF_NEXTSTEP"));
				else if (getenv("TRANSFMODELO_TRANSF") != null) 
					System.out.println("3" + getenv("TRANSFMODELO_TRANSF"));
				else if (getenv("nextstep") != null) 
					System.out.println("4" + getenv("nextstep"));
				else 
					System.out.println("No estan accesibles las variables de entorno :( ");
				
				ConnectionFactory factory = new ConnectionFactory();
				String hostRabbit = getenv("OPENSHIFT_RABBITMQ_SERVICE_HOST");
				factory.setHost(hostRabbit);
				
				Connection connection;
				try {
					connection = factory.newConnection();
					Channel channel = connection.createChannel();
					channel.queueDeclare("transfmodelo", false, false, false, null);
					
					System.out.println("transfmodelo: Queue declarada, agregando el metodo receptor...");
					
					Consumer consumer = new DefaultConsumer(channel) {
					  @Override
					  public void handleDelivery(String consumerTag, Envelope envelope,
					                             AMQP.BasicProperties properties, byte[] body)
					      throws IOException {
					    String message = new String(body, "UTF-8");
					    System.out.println(" [x] Received '" + message + "'");
					  }
					};
					channel.basicConsume("transfmodelo", true, consumer);
					System.out.println("transfmodelo: Listo. Esperando mensajes...");
				} catch (IOException | TimeoutException e) {					
					e.printStackTrace();
				}
				
			}
		});
	}
}
