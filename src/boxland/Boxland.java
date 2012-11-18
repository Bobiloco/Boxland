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
import com.jogamp.opengl.util.Animator;

public class Boxland {
	
	/**
	 * Boxland - Ant Colony Simulator 
	 * Uses an oracle database to weigh the decisions of the cube populations
	 *   so that they become more proficient at staying alive
	 *   
	 * OpenGL structure based on the example by:
	 * @author Julien Gouesse (http://tuer.sourceforge.net)
	 * 
	 * Bernard McManus - 2012
	 * Source code under CC BY 3.0
	 */
 
    private final GLCanvas canvas;
    private final Frame frame;
    private final Animator animator;
    
    public static final double sizeAdjustY = 9.5;

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
    
    private static final class boxGLEventListener implements GLEventListener {
    	
    	private final Boxland box;
 
        private GLU glu;

		// private BoxLoc boxLoc = new BoxLoc();
 
        private boxGLEventListener(Boxland box) {
            this.box = box;
        }
 
        @Override
        public void display(GLAutoDrawable gLDrawable) {
            final GL2 gl = gLDrawable.getGL().getGL2();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

            BoxLoc.display(gl);
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
            glu.gluLookAt(-0.5f,10.5,0, -0.5f, 10.5, -10, 0, 1, 0);
            gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
 
        @Override
        public void dispose(GLAutoDrawable gLDrawable) {
            // do nothing
        }
    }
 
    private Boxland() {
		BoxData.openConnection();
        BoxData.runProcSql("BEGIN INIT_TABLES(); END;");
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
 
	private void exit() {
    	try { 
    		BoxData.closeConnection();
    	   	animator.stop();
	        frame.dispose();
	        System.exit(0);
    	} catch ( Exception e) {
    		System.out.println("Exception is: " + e);
    	}
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