package transfmodelo;

import static java.lang.System.getenv;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class AsyncEndpoint implements  ServletContextListener {
	private Thread myThread = null;
	
	public void contextInitialized(ServletContextEvent sce) {
		if ((myThread == null) || (!myThread.isAlive())) {
            myThread =  new Thread(new IncomingMsgProcess(), "IdleConnectionKeepAlive");
            myThread.start();
        }
    }

    public void contextDestroyed(ServletContextEvent sce){
        try {           
            myThread.interrupt();
        } catch (Exception ex) {}
    }
    
    class IncomingMsgProcess implements Runnable {

		@Override
		public void run() {
			ConnectionFactory factory = new ConnectionFactory();
			String hostRabbit = getenv("OPENSHIFT_RABBITMQ_SERVICE_HOST");			
			factory.setHost(hostRabbit);
			
			Connection connection;
			try {
				connection = factory.newConnection();
				Channel channel = connection.createChannel();
				channel.queueDeclare("transfmodelo", false, false, false, null);
				
				Consumer consumer = new DefaultConsumer(channel) {
				  @Override
				  public void handleDelivery(String consumerTag, Envelope envelope, 
						  						AMQP.BasicProperties prop, byte[] body) 
						  							throws IOException {
				      
					    String message = new String(body, "UTF-8");
					    String nextStep = getNextStep();
					    try {
						    String json = XML2JSON.transform(message);						    
						    if (!nextStep.equals("Fin")) 
						        sendAsyncMessage2NextStep(json, nextStep);
					    }
					    catch(Exception e) {
					    	System.out.println("Catch: "+ e.toString());
					    }
					    
				  }
				};
				
				channel.basicConsume("transfmodelo", true, consumer);				
				System.out.println("Transformación: Todo listo. Esperando pedidos...");	
				
			} catch (IOException | TimeoutException e) {					
				e.printStackTrace();
			}
		}   
		
		public void sendAsyncMessage2NextStep(String message, String nextStep) {
			ConnectionFactory factory = new ConnectionFactory();
			String hostRabbit = getenv("OPENSHIFT_RABBITMQ_SERVICE_HOST");
			factory.setHost(hostRabbit);
			
			Connection connection;
			try {
				connection = factory.newConnection();
				Channel channel = connection.createChannel();
				channel.queueDeclare(nextStep, false, false, false, null);
				channel.basicPublish("", nextStep, null, message.getBytes("UTF-8"));					
				
			} catch (IOException | TimeoutException e) {					
				e.printStackTrace();
			}
		}
		
		public  String getNextStep(){
			return getenv("nextstep");
		}
    }
}
