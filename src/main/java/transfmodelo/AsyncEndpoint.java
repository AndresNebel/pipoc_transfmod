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
    
    public static boolean isEmpty(String str) {
  		return str == null || str.trim().length() == 0;
  	}
    
    class IncomingMsgProcess implements Runnable {

		@Override
		public void run() {
			System.out.println("Transformación: Inicializando Msg Endpoint..");
			
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
					    System.out.println(" [x] Received '" + message + "'");
					    
					    String json = XML2JSON.transform(message);
					    
					    System.out.println("Transformación: '" + json + "'");
					    
					    
					  //*** Envio al proximo el mensaje transformado. ***
					    if (!getNextStep().equals("Fin")) 
					        sendAsyncMessage2NextStep(json);
					    
				  }
				};
				
				channel.basicConsume("transfmodelo", true, consumer);				
				System.out.println("Transformación: Todo listo. Esperando pedidos...");	
				
			} catch (IOException | TimeoutException e) {					
				e.printStackTrace();
			}
		}   
		
		public void sendAsyncMessage2NextStep(String message) {
			ConnectionFactory factory = new ConnectionFactory();
			String hostRabbit = getenv("OPENSHIFT_RABBITMQ_SERVICE_HOST");
			factory.setHost(hostRabbit);
			
			Connection connection;
			try {
				connection = factory.newConnection();
				Channel channel = connection.createChannel();
				channel.queueDeclare(getNextStep(), false, false, false, null);
				
				System.out.println("Transformación: Invocando al proximo paso de la SI: " + getNextStep());
				
				channel.basicPublish("", getNextStep(), null, message.getBytes("UTF-8"));
				
				System.out.println("Transformación: Enviado!: "+message);	
				
			} catch (IOException | TimeoutException e) {					
				e.printStackTrace();
			}
		}
		
		public  String getNextStep(){
			return getenv("nextstep");
		}
		
    }
}
