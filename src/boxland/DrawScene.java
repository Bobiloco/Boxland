package boxland;

import javax.media.opengl.GL2;

public class DrawScene {

	public static void DrawBackground(GL2 gl2, float pulse) {
		 
		 final GL2 gl = gl2.getGL().getGL2();
		 
		 float squareSize = 0.8f;
		 
		 pulse = (float) Math.cos(pulse)/50;
		 
      // Background
         // x and y
         for(float i=0; i<BoxLoc.dimX; i++){
         	for(float j=0; j<BoxLoc.dimY; j++){
         		
                 gl.glLoadIdentity();
                 gl.glTranslatef(BoxLoc.startX, BoxLoc.startY, BoxLoc.startZ);

                 // move to the proper position on the grid
                 gl.glTranslatef(i, j, 0.0f);
                
                 // Draw A Quad
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3f( (i/(BoxLoc.dimX+1.0f))+pulse, (j/(BoxLoc.dimY+1.0f))+pulse, pulse);   // set the color of the quad
                 gl.glVertex3f(-squareSize/2f, squareSize/2f, -squareSize/2f);   // Top Left
                 gl.glVertex3f( squareSize/2f, squareSize/2f, -squareSize/2f);   // Top Right
                 gl.glVertex3f( squareSize/2f,-squareSize/2f, -squareSize/2f);   // Bottom Right
                 gl.glVertex3f(-squareSize/2f,-squareSize/2f, -squareSize/2f);   // Bottom Left
                 // Done Drawing The Quad
                 gl.glEnd();

         	}
        } 
        
         // x and z ( bottom )
         for(float i=0; i<BoxLoc.dimX; i++){
         	for(float j=0; j<BoxLoc.dimZ; j++){
         		
                 gl.glLoadIdentity();
                 gl.glTranslatef(BoxLoc.startX, BoxLoc.startY, BoxLoc.startZ);

                 // move to the proper position on the grid
                 gl.glTranslatef(i, 0.0f, j);
                
                 // Draw A Quad
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3f( (i/(BoxLoc.dimX+1.0f))+pulse, pulse, (j/(BoxLoc.dimZ+1))+pulse);   // set the color of the quad
                 gl.glVertex3f(-squareSize/2f, -squareSize/2f, squareSize/2f);   // Top Left
                 gl.glVertex3f( squareSize/2f, -squareSize/2f, squareSize/2f);   // Top Right
                 gl.glVertex3f( squareSize/2f, -squareSize/2f,-squareSize/2f);   // Bottom Right
                 gl.glVertex3f(-squareSize/2f, -squareSize/2f,-squareSize/2f);   // Bottom Left
                 // Done Drawing The Quad
                 gl.glEnd();

         	}
        } 
         
         // x and z ( top )
         for(float i=0; i<BoxLoc.dimX; i++){
         	for(float j=0; j<BoxLoc.dimZ; j++){
         		
                 gl.glLoadIdentity();
                 gl.glTranslatef(BoxLoc.startX, -BoxLoc.startY, BoxLoc.startZ);

                 // move to the proper position on the grid
                 gl.glTranslatef(i, -1.0f, j);
                
                 // Draw A Quad
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3f( (i/(BoxLoc.dimX+1.0f))+pulse, 1.0f, (j/(BoxLoc.dimZ+1.0f))+pulse);   // set the color of the quad
                 gl.glVertex3f(-squareSize/2f, squareSize/2, squareSize/2f);   // Top Left
                 gl.glVertex3f( squareSize/2f, squareSize/2, squareSize/2f);   // Top Right
                 gl.glVertex3f( squareSize/2f, squareSize/2,-squareSize/2f);   // Bottom Right
                 gl.glVertex3f(-squareSize/2f, squareSize/2,-squareSize/2f);   // Bottom Left
                 // Done Drawing The Quad
                 gl.glEnd();

         	}
        } 
         
         // y an z ( left )
         for(float i=0; i<BoxLoc.dimY; i++){
         	for(float j=0; j<BoxLoc.dimZ; j++){
         		
                 gl.glLoadIdentity();
                 gl.glTranslatef(BoxLoc.startX, BoxLoc.startY, BoxLoc.startZ);

                 // move to the proper position on the grid
                 gl.glTranslatef(0.0f, i, j );
                
                 // Draw A Quad
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3f( pulse, (i/(BoxLoc.dimY+1.0f))+pulse, (j/(BoxLoc.dimZ+1.0f))+pulse );   // set the color of the quad
                 gl.glVertex3f( -squareSize/2f, -squareSize/2f, squareSize/2f);   // Top Left
                 gl.glVertex3f( -squareSize/2f, squareSize/2f, squareSize/2f);   // Top Right
                 gl.glVertex3f( -squareSize/2f, squareSize/2f,-squareSize/2f);   // Bottom Right
                 gl.glVertex3f( -squareSize/2f, -squareSize/2f,-squareSize/2f);   // Bottom Left
                 // Done Drawing The Quad
                 gl.glEnd();

         	}
         }           

      // y an z ( right )
         for(float i=0; i<BoxLoc.dimY; i++){
         	for(float j=0; j<BoxLoc.dimZ; j++){
         		
                 gl.glLoadIdentity();
                 gl.glTranslatef(-BoxLoc.startX, BoxLoc.startY, BoxLoc.startZ);

                 // move to the proper position on the grid
                 gl.glTranslatef(-1.0f, i, j );
                
                 // Draw A Quad
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);       
                 gl.glColor3f( 1.0f+pulse, (i/(BoxLoc.dimY+1.0f))+pulse, (j/(BoxLoc.dimZ+1.0f))+pulse );   // set the color of the quad
                 gl.glVertex3f( squareSize/2f, -squareSize/2f, squareSize/2f);   // Top Left
                 gl.glVertex3f( squareSize/2f, squareSize/2f, squareSize/2f);   // Top Right
                 gl.glVertex3f( squareSize/2f, squareSize/2f,-squareSize/2f);   // Bottom Right
                 gl.glVertex3f( squareSize/2f, -squareSize/2f,-squareSize/2f);   // Bottom Left
                 // Done Drawing The Quad
                 gl.glEnd();

         	 }
         }           
  	 }
}
