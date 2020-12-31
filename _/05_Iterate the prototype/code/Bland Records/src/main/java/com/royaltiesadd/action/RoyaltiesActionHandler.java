package com.royaltiesadd.action;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import java.sql.*;


public class RoyaltiesActionHandler implements ActionHandler {
	private static final long serialVersionUID = 1L;
	
	public void execute(ExecutionContext ctx) throws Exception {
		    // get the fired employee from the process variables.
		    String firstSong = (String) ctx.getContextInstance().getVariable("songName1");
		    String secondSong = (String) ctx.getContextInstance().getVariable("songName2");
		    String thirdSong = (String) ctx.getContextInstance().getVariable("songName3");
		    String fourthSong = (String) ctx.getContextInstance().getVariable("songName4");
		    String fifthSong = (String) ctx.getContextInstance().getVariable("songName5");
		    String sixthSong = (String) ctx.getContextInstance().getVariable("songName6");
		    String seventhSong = (String) ctx.getContextInstance().getVariable("songName7");
		    String eighthSong = (String) ctx.getContextInstance().getVariable("songName8");
		    String ninthSong = (String) ctx.getContextInstance().getVariable("songName9");
		    String tenthSong = (String) ctx.getContextInstance().getVariable("songName10");
		    String songWriterName = (String) ctx.getJbpmContext().getActorId();
		    
			Connection con = null;
		    try {
		    	Class.forName("com.mysql.jdbc.Driver").newInstance();
		    	con = DriverManager.getConnection("jdbc:mysql://localhost/royalties?user=root&password=adebola");
		    	if(!con.isClosed())
		    		//Now we add our data into the database
		    		System.out.println("Connection successfully established.");
		    		Statement statement = con.createStatement();
		    		statement.executeUpdate("INSERT INTO songs "
		    				+ "(ID,song_name,songwriter_name) "
		    				+ "VALUES (NULL,'" + firstSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + secondSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + thirdSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + fourthSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + fifthSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + sixthSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + seventhSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + eighthSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + ninthSong + "','" + songWriterName + "'),"
		    				+ "(NULL,'" + tenthSong + "','" + songWriterName + "')"
		    				);
		    		System.out.println("Songs and writers inserted.");
		    		statement.close();
		    		con.close();

		    	} catch(Exception e) {
		    		System.err.println("Exception: " + e.getMessage());
		    	} finally {
		    		try {
		    			if(con != null)
		    				con.close();
		    		} catch(SQLException e) {}
		    	}
	}
}