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

public class IncomingMsgWatcher implements  ServletContextListener {
	private Thread myThread = null;
	
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("Antes de crear el thread");
        if ((myThread == null) || (!myThread.isAlive())) {
            myThread =  new Thread(new IncomingMsgProcess(), "IdleConnectionKeepAlive");
            myThread.start();
        }
    }

    public void contextDestroyed(ServletContextEvent sce){
        try {           
            myThread.interrupt();
        } catch (Exception ex) {
        }
    }
    
    class IncomingMsgProcess implements Runnable {

		@Override
		public void run() {
			System.out.println("transfmodelo: RabbitMQ Receiver Thread initializing..");
			ConnectionFactory factory = new ConnectionFactory();
			String hostRabbit = getenv("OPENSHIFT_RABBITMQ_SERVICE_HOST");			
			System.out.println("transfmodelo: hostRabbit:"+hostRabbit+":default");
			
			factory.setHost(hostRabbit);				
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
    }
}
