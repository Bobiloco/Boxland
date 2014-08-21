package boxland;

import javax.media.opengl.GL2;

public class DrawBox {

	/**
	 * DrawBox - Drawing the inside panels
	 * 
	 * - Some OpenGL loops that draw the panels
	 *  
	 * Bernard McManus, Nov 2012
	 * Source code under CC BY 3.0 
	 */
	
	public static void DrawBackground(GL2 gl2, double pulse) {
	try { 
		 final GL2 gl = gl2.getGL().getGL2();
		 
		 double cubeSize = 0.4;
		 double offsetSize = 0.5; // draws the walls out a little
		 
		 double posX = BoxLoc.startX;
		 double posY = BoxLoc.startY+Boxland.sizeAdjustY;
		 double negY = -BoxLoc.startY+Boxland.sizeAdjustY;
		 double posZ = BoxLoc.startZ;
		 
		 double dimX = BoxLoc.dimX;
		 double dimY = BoxLoc.dimY;
		 double dimZ = BoxLoc.dimZ;
		 
		 pulse = Math.cos(pulse)/100;
		 
         // x and y - Background
         for(int i=0;i<BoxLoc.dimX;i++){
         	for(int j=0;j<BoxLoc.dimY;j++){
                 gl.glLoadIdentity();
                 gl.glTranslated(posX,posY,posZ);
                 gl.glTranslated(i,j,-offsetSize);
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3d(i/dimX+pulse,j/dimY+pulse,pulse);
                 gl.glVertex3d(-cubeSize,cubeSize,0);  // Top Left
                 gl.glVertex3d(cubeSize,cubeSize,0);   // Top Right
                 gl.glVertex3d(cubeSize,-cubeSize,0);  // Bottom Right
                 gl.glVertex3d(-cubeSize,-cubeSize,0); // Bottom Left
                 gl.glEnd();
     		}
         } 
         
         // x and z - bottom
         for(int i=0; i<BoxLoc.dimX; i++){
         	for(int j=0; j<BoxLoc.dimZ; j++){
                 gl.glLoadIdentity();
                 gl.glTranslated(posX,posY,posZ);
                 gl.glTranslated(i,-offsetSize, j);
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3d(i/dimX+pulse,pulse,j/dimZ+pulse);
                 gl.glVertex3d(-cubeSize,0,cubeSize);  // Top Left
                 gl.glVertex3d(cubeSize,0,cubeSize);   // Top Right
                 gl.glVertex3d(cubeSize,0,-cubeSize);  // Bottom Right
                 gl.glVertex3d(-cubeSize,0,-cubeSize); // Bottom Left
                 gl.glEnd();
         	}
         } 

         // x and z ( top )
         for(int i=0; i<BoxLoc.dimX; i++){
         	for(int j=0; j<BoxLoc.dimZ; j++){
                 gl.glLoadIdentity();
                 gl.glTranslated(posX,negY,posZ);
                 gl.glTranslated(i,offsetSize-1,j);
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3d((i/dimX)+pulse,1,(j/dimZ)+pulse);
                 gl.glVertex3d(-cubeSize,0,cubeSize);  // Top Left
                 gl.glVertex3d(cubeSize,0,cubeSize);   // Top Right
                 gl.glVertex3d(cubeSize,0,-cubeSize);  // Bottom Right
                 gl.glVertex3d(-cubeSize,0,-cubeSize); // Bottom Left
                 gl.glEnd();
         	}
        } 

         // y an z ( left )
         for(int i=0; i<BoxLoc.dimY; i++){
         	for(int j=0; j<BoxLoc.dimZ; j++){
                 gl.glLoadIdentity();
                 gl.glTranslated(posX,posY,posZ);
                 gl.glTranslated(-offsetSize,i,j);
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3d(pulse,(i/dimY)+pulse,(j/dimZ)+pulse);
                 gl.glVertex3d(0,-cubeSize,cubeSize);  // Top Left
                 gl.glVertex3d(0,cubeSize,cubeSize);   // Top Right
                 gl.glVertex3d(0,cubeSize,-cubeSize);  // Bottom Right
                 gl.glVertex3d(0,-cubeSize,-cubeSize); // Bottom Left
                 gl.glEnd();
        	}
         }           

         // y an z ( right )
         for(int i=0; i<BoxLoc.dimY; i++){
         	for(int j=0; j<BoxLoc.dimZ; j++){
                 gl.glLoadIdentity();
                 gl.glTranslated(-posX,posY,posZ);
                 gl.glTranslated(offsetSize-1,i,j);
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3d(pulse+1,(i/dimY)+pulse,(j/dimZ)+pulse);
                 gl.glVertex3d(0,-cubeSize,cubeSize);  // Top Left
                 gl.glVertex3d(0,cubeSize,cubeSize);   // Top Right
                 gl.glVertex3d(0,cubeSize,-cubeSize);  // Bottom Right
                 gl.glVertex3d(0,-cubeSize,-cubeSize); // Bottom Left
                 gl.glEnd();
         	 }
         }           
	} catch ( Exception e ) { System.out.println("DrawBox.DrawBackground(): " + e);	}
	}
}
