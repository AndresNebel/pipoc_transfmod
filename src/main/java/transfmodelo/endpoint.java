package transfmodelo;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.XML;
import org.json.JSONObject;

import java.io.IOException;
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
	public void initializeMessageQueue() {
		System.out.println("transfmodelo: Initializing MQ");
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {
			public void run() {
				System.out.println("transfmodelo: RabbitMQ Receiver Thread initializing..");
				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost("localhost");
				Connection connection;
				try {
					connection = factory.newConnection();
					Channel channel = connection.createChannel();
					channel.queueDeclare("transfmodelo", false, false, false, null);
					System.out.println("transfmodelo: Queue declared, adding consumer...");
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
					System.out.println("transfmodelo: All set, waiting for messages now.");
				} catch (IOException | TimeoutException e) {					
					e.printStackTrace();
				}
				
			}
		});
	}
}
