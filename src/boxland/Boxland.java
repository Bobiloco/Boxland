package boxland;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import com.jogamp.opengl.util.Animator;

public class Boxland {
 
	public static Connection connection;
    private final GLCanvas canvas;
    private final Frame frame;
    private final Animator animator;
    
    public static final float sizeAdjustY = 10.0f;

	private static final class boxKeyListener extends KeyAdapter {
 
        private final Boxland box;
 
        private boxKeyListener(Boxland box) {
            this.box = box;
        }
 
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            	box.exit(); }
        }
    }
    
    public static final class boxGLEventListener implements GLEventListener {
    	
    	private final Boxland box;
 
        private GLU glu;

		private BoxLoc boxLoc = new BoxLoc();
 
        private boxGLEventListener(Boxland box) {
            this.box = box;
        }
 
        @Override
        public void display(GLAutoDrawable gLDrawable) {
            final GL2 gl = gLDrawable.getGL().getGL2();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

            boxLoc.display(gl);
        }
 
    	@Override    
    	public void init(GLAutoDrawable glDrawable) {

    		GL2 gl = glDrawable.getGL().getGL2();
            glu = GLU.createGLU(gl);
            gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glClearDepth(1.0f);
            gl.glEnable(GL.GL_DEPTH_TEST);
        	gl.glEnable(GL.GL_BLEND);
        	gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDepthFunc(GL.GL_LEQUAL);
            gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
            ((Component) glDrawable).addKeyListener(new boxKeyListener(box));
            
    	}
 
        @Override
        public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
            GL2 gl = gLDrawable.getGL().getGL2();
            if (height <= 0) {
                height = 1;
            }
            float h = (float) width / (float) height;
            gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
            gl.glLoadIdentity();
            glu.gluPerspective(50.0f, h, 1.0, 1000.0);
            glu.gluLookAt(-0.5f,11,0, -0.5f, 11, -10, 0, 1, 0);
            gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
 
        @Override
        public void dispose(GLAutoDrawable gLDrawable) {
            // do nothing
        }
    }
 
    /**** Database Block ****/
    
    private void openConnection() {
    	try {  
        	System.out.println("Connecting to the database..."); 
        	connection = DriverManager.getConnection( "jdbc:oracle:thin:@localhost:1521:XE", "terre", "terrep");
        } catch (Exception e) {  
            System.out.println("The exception raised is:" + e);  
        }  
    	System.out.println("Connection Opened");
    }
    
    private void closeConnection() {
        try {
        	System.out.println("Disconnecting from the database...");
        	connection.close();
		} catch (Exception e) {
			System.out.println("The exception raised is:" + e);
		}
        System.out.println("Connection Closed");
    }
    
    // This is really for non-SELECT statments
    public static void runInsertSql(String statementSQL) {
            
            try {
            	Statement statement = connection.createStatement();
            	statement.execute(statementSQL);
            	statement.close();
            } catch (Exception e) {
				System.out.println("The exception raised is:" + e);
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
			System.out.println("The exception raised is:" + e);
			return -1;
		}
    }
    
 // For select statements that return an integer value
    public static String runSelectVarcharSql(String statementSQL) {
        
        try {
        	Statement statement = connection.createStatement();
        	ResultSet resultset = statement.executeQuery(statementSQL);
        	resultset.next(); 
        	String s = resultset.getString(1);
        	resultset.close();
        	statement.close();
        	
        	return s;
        	
        } catch (Exception e) {
			System.out.println("The exception raised is:" + e);
			return null;
		}
    }
    
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
			System.out.println("The exception raised is:" + e);
		}
		return result;
    }
    
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
			System.out.println("The exception raised is:" + e);
		}
    }
    
    public static void runProcSql(String procSQL) {
        
        try {
        	CallableStatement callStmt = connection.prepareCall(procSQL);
        	callStmt.execute();
        	callStmt.close();
        	
        } catch (Exception e) {
			System.out.println("The exception raised is:" + e);
		}
    }
    
    public Boxland() {

    	// set up the database connection
        try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		} catch (SQLException e) {
			System.out.println("The exception raised is:" + e);
		}
        openConnection();
        
        // Empty tables and create the dummy 'killed' rows
        Boxland.runProcSql("BEGIN INIT_TABLES(); END;");
    	
    	// kick off OpenGL
        canvas = new GLCanvas();
        frame = new Frame("Boxland");
        animator = new Animator(canvas);
        canvas.addGLEventListener(new boxGLEventListener(this));
        frame.add(canvas);
        frame.setSize(640, 480);
        frame.setUndecorated(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        frame.setVisible(true);
        animator.start();
        canvas.requestFocus();
    }
 
	public void exit() {
    	closeConnection();
    	animator.stop();
        frame.dispose();
        System.exit(0);
    }
 
    public static void main(String[] args) {
        // AWT methods have to be invoked on the AWT EDT
        EventQueue.invokeLater(new Runnable() {
 
            @Override
            public void run() {
                new Boxland();
            }
        });
    }
}