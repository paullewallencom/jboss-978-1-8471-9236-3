package com.seewhy.jbpm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class connect to JMS and sends the notification to the SeeWhy Event Interface. The queue
 * to use is identified by a system property called 'jBPMSeeWhyQueue' - if the property doesn't
 * exist then a default is used (queue/jBPMSourceQueue).
 *
 */
public class SeeWhyExternalMessageSender implements Serializable 
{
	static final long serialVersionUID = 1;

	/**
	 * This class encapsulates any exceptions that carries to the outside world.
	 *
	 */
	public static class SeeWhyExternalMessageSenderException extends Exception
	{
		static final long serialVersionUID = 1;
		private String myMessage = null;
		public SeeWhyExternalMessageSenderException (String aMessage)
		{
			myMessage = aMessage;
		}

		public String getMessage() 
		{
			return super.getMessage() + myMessage;
		}		

	}

	private QueueConnectionFactory m_Factory = null;
	private QueueConnection m_Connection = null;
	private Queue m_Queue = null;
	private QueueSession m_Session = null;
	private QueueSender m_sender = null;	
	private String m_queueName = null;

	private static final String DEFAULT_QUEUE = "queue/jBPMSourceQueue";
	private static final String QUEUE_NAME_PARAM = "jBPMSeeWhyQueue";
	private static final String QUEUE_PREFIX = "queue/";
	private static final String DEFAULT_FACTORY_ADDRESS = "localhost:1099";
	static String EXTERNAL_NAMING_FACTORY_REF="seewhy.external.naming.factory";	
	private static final String CONNECTION_FACTORY = "ConnectionFactory";
	private static final String SEE_WHY_JNDIPROPERTIES = "SeeWhyJNDIProperties";


	private static final Log log = LogFactory.getLog( SeeWhyExternalMessageSender.class );

	public SeeWhyExternalMessageSender() 
	{
		super();
		if(System.getProperty(EXTERNAL_NAMING_FACTORY_REF) == null)
		{
			//set the default - presently the default is for single operation, to default to a clustered operation
			// either add a -D to the startup script (correct solution) or reverse 

			//for clustered operation
			//System.setProperty(EXTERNAL_NAMING_FACTORY_REF, DEFAULT_CLUSTER_FACTORY_ADDRESS);

			//for standalone operation
			System.setProperty(EXTERNAL_NAMING_FACTORY_REF,DEFAULT_FACTORY_ADDRESS);
		}
	}

	/**
	 * This will attempt to retrieve a system property supplying the name of the queue it should try to connect to.
	 * The sys parameter is 'jBPMSeeWhyQueue' The absence of a parameter and the default will be used.
	 * @throws NamingException thrown trying to connect to JMS
	 * @throws JMSException  thrown trying to connect to JMS
	 * @throws RemoteException  thrown trying to connect to JMS
	 * @throws CreateException  thrown trying to connect to JMS
	 */
	private void connect()
	{
		InitialContext context = null;
		log.debug("Standard JMS connect method invoked");
		//Make the queue configurable so we can use multiple simulators with multiple
		//event interfaces.
		try
		{
			log.debug("creating context");
			context = getContext();
			log.info("context name space:" + context.getNameInNamespace());

			log.debug("creating factory");		
			m_Factory = (QueueConnectionFactory) context.lookup(CONNECTION_FACTORY);


			log.debug("creating connection");		
			m_Connection = m_Factory.createQueueConnection();



			log.debug("getting queue name");
			m_queueName = System.getProperty(QUEUE_NAME_PARAM);
			if (m_queueName == null)
			{
				log.info("no queue name defined so forcing to " + DEFAULT_QUEUE);
				m_queueName = DEFAULT_QUEUE;
			}

			if (!m_queueName.startsWith(QUEUE_PREFIX))
			{
				m_queueName = QUEUE_PREFIX + m_queueName;
			}


			log.info("Looking up queue " + m_queueName);
			m_Queue = (Queue) context.lookup(m_queueName);
			log.debug("creating session");
			m_Session = m_Connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			log.debug("creating sender");
			m_sender = m_Session.createSender(m_Queue);
			log.debug("all done");
		}
		catch (NullPointerException nullPtrError)
		{
			log.error("trapped pointer exception : " + nullPtrError.getMessage(), nullPtrError);

		}
		catch (NamingException error)
		{
			log.error("trapped naming exception : " + error.getMessage(), error);
		}
		catch (JMSException jmsError)
		{
			log.error("trapped JMS exception : " + jmsError.getMessage(), jmsError);
		}
	}  

	/**
	 * This will oad the properties from the identified file
	 * @param filename the file containing the appropriate properties
	 * @return the properties loaded
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Properties getApplicationProperties(String filename) throws FileNotFoundException, IOException
	{
		Properties props = null;
		FileInputStream in = null;

		log.info("Properties file to use:" + filename);

		try
		{
			in = new FileInputStream(filename);
			props = new Properties();
			props.load(in);
		}
		finally
		{
			in.close();
		}

		return props;
	}
	/**
	 * This provides the means to obtain an initial context
	 * @param id the context id
	 * @return the new context based upon the id
	 * @throws NamingException
	 */
	private InitialContext getContext() throws NamingException
	{
		InitialContext context = null;

		try
		{
			String propertiesFilename = System.getProperty(SEE_WHY_JNDIPROPERTIES);
			Properties props = getApplicationProperties (propertiesFilename);
			if (props != null)
			{
				context = new InitialContext( props );
			}
			else
			{
				context = new InitialContext();
			}
		}
		catch (Exception error)
		{
			log.warn("Caught an exception trying to load properties to create initial context so creating with no props\n"+error.getMessage());
			context = new InitialContext();
		}
		return context;
	}


	/**
	 * This takes the string (should be formatted to XML) and places it into a JMS message and sends.
	 * @param message the string representation of the XML to be sent.
	 * @throws SeeWhyExternalMessageSenderException any exception in trying to send is carried by this class
	 */
	public void send (String message) throws SeeWhyExternalMessageSenderException
	{
		try
		{
			if (m_Queue == null)
			{
				connect();
			}
			TextMessage jmsMessage = m_Session.createTextMessage();
			jmsMessage.setText(message);
			m_sender.send(jmsMessage);
			
		}
		catch (Exception error)
		{
			String msg = "Could not send message:"+message+"\n because:" + error.getMessage() + "\nError class is " + error.getClass().getName();
			log.error(msg, error);
			throw new SeeWhyExternalMessageSenderException (msg);
		}
	}

	/**
	 * Attempt to release all the resources used for JMS actions, and initialise the internal variables
	 * back to a start state.
	 */
	public void close()
	{

		if (m_Session != null)
		{
			try
			{
				m_Session.close();				
			}
			catch (JMSException error)
			{
				log.warn ("Caught exception closing session" + error.getMessage());
			}


		}
		if (m_sender != null)
		{
			try
			{
				m_sender.close();
			}
			catch (JMSException error)
			{
				log.warn ("Caught exception closing sender" + error.getMessage());
			}

		}

		if (m_Connection != null)
		{
			try
			{
				m_Connection.close();
			}
			catch (JMSException error)
			{
				log.warn ("Caught exception closing connection" + error.getMessage());
			}

		}		

		m_Session = null;
		m_Connection = null;
		m_sender = null;
		m_Factory = null;
		m_queueName = null;
		m_Queue = null;

	}


	/**
	 * invoke the necessarey actions to release all the resources we hold.
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable 
	{
		close();
		super.finalize();
	}

}
