package com.seewhy.jbpm;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This class implements the action handler needed by jBPM to retrieve values from jBPm and bundle it
 * up for transmission to SeeWhy. The Base class provides the transmission mechanism.
 *
 */
public class MessageSender extends SeeWhyExternalMessageSender implements ActionHandler 
{
	private static final String DEMO_EVENT_NAME = "atmTransfer";
	// the default event name to use with SeeWhy 

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog( MessageSender.class );

	String myEventName = null;
	// the event name of the event to send - should be set by jBPM
	String myVariablesToUse = null;
	// if this is set by jBPM - as a comma separated list of variables - we'll just use this set of vars
	// to build the 1-to-1 mapping
	String myDisableSend = null;
	//can set set by jBPM to do everything BUT send he message - must have a value of "True" to take effect


	public MessageSender()
	{
		super();
		log.info( "###############################################" );
		log.info( "### MessageSender constructed");
		log.info( "###############################################" );

	}

	/** Takes the information from jBPM and sends the message then allows jBPM to move on.
	 * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
	 */
	public void execute( ExecutionContext executionContext ) throws Exception
	{

		HashSet hsKeys = null;
		HashMap hmKeyValuePairs = null;
		String strXML = null;

		// if thev event name has not be defined explictly then determine it from the
		if (myEventName == null)
		{
			log.info("eventname has not been set - so will assume from event name of jBPM action");
			myEventName = executionContext.getEventSource().getName();
		}

		log.info( "###############################################" );
		log.info( "### MessageSender.execute() Event Name : " + myEventName );
		log.info( "###############################################" );

		//Retrieve the key names from the event schema.
		//hsKeys = retrieveXMLSchemaData( myEventName );
		if (myVariablesToUse == null)
		{
			log.info("if you have a 1-to-1 mapping between the event parameters and jBPM variables can use prepareHashMapWithEventVariables instead of retrieveXMLSchemaData");
			hsKeys = prepareHashMapWithEventVariables (hsKeys, executionContext);
		}
		else
		{
			hsKeys = prepareHashMapWithNamedVariables (hsKeys);
		}


		//Populate the key/value pairs using the Process data.
		hmKeyValuePairs = populateKeyValuePairsFromProcessData( executionContext, hsKeys );

		//Format an XML string using the key/value pairs.
		strXML = produceXMLEvent( hmKeyValuePairs );

		//Send the XML message to SeeWhy.
		sendXMLMessageToEventInterface( strXML );

	}

	/**
	 * If the event being supplied is one we know about then add the appropriate keys into the
	 * data structure
	 * @param strEventName the event name - should be one we know about to populate the data keys
	 * @return a hashset with the keys added for the event supplied - if the event is not know to us then
	 * the hashset will be empty.
	 */
	/**
	 * @param strEventName
	 * @return
	 */
	private HashSet retrieveXMLSchemaData( String strEventName )
	{
		HashSet hsKeys = new HashSet();

		// NOTE not non demo events need to extend 
		if((strEventName != null) &&  (strEventName.equalsIgnoreCase( DEMO_EVENT_NAME ) == true ))
		{
			hsKeys = prepareHashMapWithKeysForDemoEvent(hsKeys);

		}
		else
		{
			log.warn("Can't create element hashmap as Event type was not expected");
		}

		return hsKeys;
	}

	/**
	 * This add the elements fro the demo event type to the key set
	 * @param hsKeys the key set to populate with the elements we need
	 * @return the populated key sey
	 */
	private HashSet prepareHashMapWithKeysForDemoEvent(HashSet hsKeys) 
	{
		final String amount = "amount";
		final String reason = "reason";

		hsKeys.add( amount );

		log.info( "###############################################" );
		log.info( "### MessageSender.retrieveXMLSchemaData() Adding element : " + amount );

		hsKeys.add( reason );

		log.info( "### MessageSender.retrieveXMLSchemaData() Adding element : " + reason );
		log.info( "###############################################" );

		return hsKeys;
	}

	/**
	 * Debug/logging method which will write to the logs the keys in the collection
	 * @param keys
	 */
	private void recordVars (Collection keys)
	{
		if (keys != null)
		{
			Iterator keyIter = keys.iterator();

			while (keyIter.hasNext())
			{
				Object key = keyIter.next();
				log.info("key to add:" + key.toString());
			}
		}
	}

	/**
	 * If the variables provided have a 1-to-1 mapping with the SeeWhy event schema
	 * @param hsKeys the key set to populate with the elements we need
	 * @return the populated key set
	 */
	private HashSet prepareHashMapWithNamedVariables(HashSet hsKeys) 
	{
		StringTokenizer tokenizer = new StringTokenizer(myVariablesToUse, ",");

		if (hsKeys == null)
		{
			hsKeys = new HashSet();
		}

		if (tokenizer != null)
		{
			while (tokenizer.hasMoreTokens())
			{
				String nextVar = tokenizer.nextToken().trim();
				if (nextVar != null && nextVar.length() > 0)
				{
					log.info ("Adding " + nextVar + " to list of variables");
					hsKeys.add(nextVar);
				}
			}
		}
		return hsKeys;
	}	

	/**
	 * If the variables provided have a 1-to-1 mapping with the SeeWhy event schema
	 * @param hsKeys the key set to populate with the elements we need
	 * @return the populated key set
	 */
	private HashSet prepareHashMapWithEventVariables(HashSet hsKeys, ExecutionContext executionContext) 
	{
		ContextInstance instance = executionContext.getContextInstance();		

		if (hsKeys == null)
		{
			hsKeys = new HashSet();
		}

		if (instance != null)
		{
			Map varMap = instance.getVariables();
			if (varMap != null)
			{
				Collection keys = varMap.keySet();
				//recordVars (keys);
				hsKeys.addAll(keys);
			}
		}
		return hsKeys;
	}	


	/**
	 * Pull out of the jBPM context the values and throw it into the hashmap
	 * @param hsKeys the hashmap holding the data pairs that need to be sent to SeeWhy
	 * @param executionContent the jBPM context for this action which will have a handle the
	 * jBPM variables we need to retrieve.
	 */
	private HashMap populateKeyValuePairsFromProcessData( ExecutionContext executionContext, HashSet hsKeys )
	{
		HashMap hmKVPairs = new HashMap();
		Iterator itr = null;
		String strKey = null;
		Object objValue = null;

		ContextInstance contextInstance = executionContext.getContextInstance();
		log.info( "###############################################" );

		if (hsKeys != null)
		{
			itr = hsKeys.iterator();
			while( itr.hasNext() == true )
			{
				strKey = (String)itr.next();

				objValue = contextInstance.getVariable( strKey );

				log.info( "### MessageSender.populateKeyValuePairsFromProcessData() Adding key/value pair : " + strKey + "/" + objValue );

				hmKVPairs.put( strKey, objValue );
			}
		}
		log.info( "###############################################" );

		return hmKVPairs;
	}

	/**
	 * Take the Key value pairs and build the XML as a string adding the mandatory elements
	 * @param hmKeyValuePairs the key value pairs to build into the event
	 * @return XML as a string
	 */
	private String produceXMLEvent( HashMap hmKeyValuePairs )
	{
		Iterator itr = null;
		Map.Entry entry = null;
		String strEventXML = "";

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS");
		SimpleDateFormat zoneFormatter = new SimpleDateFormat("Z");
		
		//target for pattern is : '2005-06-12T13:25:26.000+00:00' 
		Date now = new Date (System.currentTimeMillis());
		String eventDateTime = formatter.format(now);
		String zoneStr = zoneFormatter.format(now);
		if ((zoneStr != null) && (zoneStr.length()>3))
		{
			String newZoneStr = zoneStr.substring(0, 3) + ":" + zoneStr.substring(3);
			zoneStr = newZoneStr;
			System.out.println("modified zone string to " + zoneStr);
		}
		else
		{
			System.out.println("had problems with time zone offset - so setting to +00:00");
			zoneStr = "+00:00";
		}
		eventDateTime = eventDateTime +zoneStr; 
		
		strEventXML =
			"<?xml version='1.0' encoding='UTF-8'?>" + "<" + myEventName + " EventName='" +
			myEventName + "' EventDateTime='" + eventDateTime + "' >";

		itr = hmKeyValuePairs.entrySet().iterator();
		while( itr.hasNext() == true )
		{
			entry = (Map.Entry)itr.next();
			strEventXML = strEventXML + "<" + entry.getKey() + " Content='" + entry.getValue() + "'/>";
		}

		strEventXML = strEventXML + "</" + myEventName + ">";

		log.info( "###############################################" );
		log.info( "### MessageSender.produceXMLEvent() Produced XML : " + strEventXML );
		log.info( "###############################################" );

		return strEventXML;
	}

	/**
	 * Invoke the transmission layer of the mechanism so that SeeWhy receives an event
	 * in a suitable manner
	 * @param strXML the XML to be sent
	 */
	private void sendXMLMessageToEventInterface( String strXML )
	{
		try
		{
			if ((myDisableSend != null) && (myDisableSend.equalsIgnoreCase("True")))
			{
				log.info( "###############################################" );
				log.info( "### Send switched off - but would have sent:" + strXML);
				log.info( "###############################################" );

			}
			else
			{
				log.info( "###############################################" );
				log.info( "### Send start ...  (NB during ActionHandler development comment out next line)" );			
				send (strXML);
				close();
				log.info( "### Send completed" );
				log.info( "###############################################" );
			}
		}
		catch (SeeWhyExternalMessageSenderException error)
		{
			log.error("send had a problems :\n" + error.getMessage());
			close(); // force the releaee of resources
		}
	}

}
