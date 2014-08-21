package boxland;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;


public class BoxData {

	/**
	 * BoxData - Database connection class
	 *
	 * - Static class that handles the JDBC connection all the PLSQL calls
	 * - Should use more prepared procedures as it allows for 
	 * 		the execution of some pretty generic strings
	 * 
	 * Bernard McManus - 2012
	 * Source code under CC BY 3.0 
	 */
	
	private static final String jdbcConn = "jdbc:oracle:thin:@localhost:1521:XE";
	private static final String jdbcDB   = "terre"; 
	private static final String jdbcPass = "terrep";
	private static Connection connection;

	// set up the database connection using registerDriver
	static {

		try {
        	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        } catch (SQLException e) {
			System.out.println("BoxData.registerDriver(): " + e);
		}
	}
	
	static void openConnection() {

		try {  
        	System.out.println("Connecting to the database..."); 
        	connection = DriverManager.getConnection(jdbcConn,jdbcDB,jdbcPass);

        	System.out.println("Connection Opened");
        	
		} catch (Exception e) {  
            System.out.println("BoxData.openConnection(): " + e);  
        }  
    }
    
    static void closeConnection() {

    	try {
        	System.out.println("Disconnecting from the database...");
        	connection.close();

            System.out.println("Connection Closed");        	
        	
        } catch (Exception e) {
			System.out.println("BoxData.closeConnection(): " + e);
		}
    }
    
    // This is for non-SELECT statements
    static void runInsertSql(String statementSQL) {

    	try {
        	Statement statement = connection.createStatement();
        	statement.execute(statementSQL);
        	statement.close();

        } catch (Exception e) {
			System.out.println("BoxData.runInsertSql(): " + e);
		}
	}
    
    // For select statements that return an integer value
    public static int runSelectINTSql(String statementSQL) {
        
        try {
        	Statement statement = connection.createStatement();
        	ResultSet resultset = statement.executeQuery(statementSQL);
        	resultset.next(); 
        	int s = resultset.getInt(1);
        	resultset.close();
        	statement.close();
        	
        	return s;
        	
        } catch (Exception e) {
			System.out.println("BoxData.runSelectINTSql(): " + e);
			return -1;
		}
    }
    
    // Prepares the dynamic SQL statemend for a Choice event    
    public static int runChoiceEvent(String procSQL, int mobDBID, int locX, int locY, int locZ, String faceToken, String faceToken2, String faceToken3, String faceToken4, String faceToken5, String faceToken6, String faceToken7) {
        
        int result = 0;
        
    	try {
        	CallableStatement callStmt = connection.prepareCall(procSQL);
        	callStmt.setInt(2, mobDBID);
        	callStmt.setInt(3, locX);
        	callStmt.setInt(4, locY);
        	callStmt.setInt(5, locZ);
        	callStmt.setString(6, faceToken);
        	callStmt.setString(7, faceToken2);
        	callStmt.setString(8, faceToken3);
        	callStmt.setString(9, faceToken4);
        	callStmt.setString(10, faceToken5);
        	callStmt.setString(11, faceToken6);
        	callStmt.setString(12, faceToken7);
        	callStmt.registerOutParameter(1, oracle.jdbc.OracleTypes.INTEGER);
       	
        	callStmt.execute();
        	result = callStmt.getInt(1);
        	callStmt.close();
        	
        } catch (Exception e) {
			System.out.println("BoxData.runChoiceEvent(): " + e);
		}
    	return result;
    }
    
    // Prepares the dynamic SQL for a Died event
    public static void runDiedEvent(String procSQL, int mobDBID, String way, int locX, int locY, int locZ ) {
    	try {
        	CallableStatement callStmt = connection.prepareCall(procSQL);
        	callStmt.setInt(1, mobDBID);
        	callStmt.setString(2, way);
        	callStmt.setInt(3, locX);
        	callStmt.setInt(4, locY);
        	callStmt.setInt(5, locZ);
        	callStmt.execute();
        	callStmt.close();
        } catch (Exception e) {
			System.out.println("BoxData.runDiedEvent(): " + e);
		}
    }
    
    // Runs procedures in the database 
    public static void runProcSql(String procSQL) {
        try {
        	CallableStatement callStmt = connection.prepareCall(procSQL);
        	callStmt.execute();
        	callStmt.close();
        	
        } catch (Exception e) {
			System.out.println("BoxData.runProcSql(): " + e);
		}
    }

}
