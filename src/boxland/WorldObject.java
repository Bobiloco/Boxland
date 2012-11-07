package boxland;

import javax.media.opengl.GL2;

public class WorldObject {

    public static final float cubeSize = 0.5f;
    
    public float drawSize = 0.0f;
    public float growSize = cubeSize / 10.0f;
    public float growCount = -10;
    
	// mob location
	public int locX;
    public int locY;
    public int locZ;
    
    public float offsetX = 0.0f;
    public float offsetY = 0.0f;
    public float offsetZ = 0.0f;
    
    public void updateXYZ(int x, int y, int z) {
		locX = x;
		locY = y;
		locZ = z;
	}

    public int gX() { return locX; }
    public int gY() { return locY; }
    public int gZ() { return locZ; }
    
	public void eat(WorldObject woEaten) {
		woEaten.killed();
	}

	public void killed() {
		TerrLoc.removeObj(this);
		TerrLoc.insertObj(this);
	}

	public String getID() {
		return "Neutral";
	}

	public int getMobID() {
		return -1;
	}
	
	public String getWobType() {
		return "Default";
	}

	public int getMobDBID() {
		return -1;
	}
	

	public void startAnimate(int facing) {
		
		if ( facing == 2 ) offsetX = -10;
		if ( facing == 1 ) offsetX = 10;
		if ( facing == 4 ) offsetY = -10;
		if ( facing == 3 ) offsetY = 10;
		if ( facing == 6 ) offsetZ = -10;
		if ( facing == 5 ) offsetZ = 10;
		
	}
	
	public void animate() {
		
		if ( Math.round(offsetX) > 0 ) offsetX = offsetX - ( 1.0f / ( (float) TerrLoc.sizeMultiple * 2.0f ) );
		if ( Math.round(offsetX) < 0 ) offsetX = offsetX + ( 1.0f / ( (float) TerrLoc.sizeMultiple * 2.0f ) );
		if ( Math.round(offsetY) > 0 ) offsetY = offsetY - ( 1.0f / ( (float) TerrLoc.sizeMultiple * 2.0f ) );
		if ( Math.round(offsetY) < 0 ) offsetY = offsetY + ( 1.0f / ( (float) TerrLoc.sizeMultiple * 2.0f ) );
		if ( Math.round(offsetZ) > 0 ) offsetZ = offsetZ - ( 1.0f / ( (float) TerrLoc.sizeMultiple * 2.0f ) );
		if ( Math.round(offsetZ) < 0 ) offsetZ = offsetZ + ( 1.0f / ( (float) TerrLoc.sizeMultiple * 2.0f ) );
		
	}
	
	public void growSize(){
    	if ( Math.round(growCount) < 0 ) growCount = growCount + 0.1f / ( (float) TerrLoc.sizeMultiple * 2.0f );
    }
	
	public void drawActions() {
		growSize();
		animate();
		drawSize = cubeSize + ( growCount * growSize );
    }
	
	public void drawObj (GL2 gl2) {

		drawActions();
		
		// if not overwritten, air~
	}
	
	public void drawObjColour (GL2 gl2, float red, float green, float blue) {

		float squareSize = drawSize / 2.0f;
		
		final GL2 gl = gl2.getGL().getGL2();
		
		gl.glLoadIdentity();
        gl.glTranslatef( offsetX*0.1f + TerrLoc.startX + (float) locX, 
        		         offsetY*0.1f + TerrLoc.startY + (float) locY, 
        		         offsetZ*0.1f + TerrLoc.startZ + (float) locZ );

        float tDimX = TerrLoc.dimX-1.0f;
        float tDimY = TerrLoc.dimY-1.0f;
        float tDimZ = TerrLoc.dimZ-1.0f;
        
        float xUp = ( (float) locX/tDimX + red ) / 2;
        float yUp = ( (float) locY/tDimY + green ) / 2;
        float zUp = ( (float) locZ/tDimZ + blue ) / 2;
        
        // xUp-green-blue, yUp-red-blue, zUp-red-green
        
        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        if ( locY < TerrLoc.dimY/2.0f) {
        gl.glColor3f( xUp, yUp, zUp );   // set the top color of the quad
        gl.glVertex3f(-squareSize, squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize, squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f(-squareSize, squareSize,-squareSize);   // Bottom Left
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        if ( locY > TerrLoc.dimY/2.0f) {
        gl.glColor3f( xUp, yUp, zUp);   // set the bottom colour of the quad
        gl.glVertex3f(-squareSize, -squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, -squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize, -squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f(-squareSize, -squareSize,-squareSize);   // Bottom Left
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        if ( locX < TerrLoc.dimX/2) {
        gl.glColor3f( xUp*1.1f, yUp*1.1f, zUp*1.1f);   // set the right of the quad
        gl.glVertex3f( squareSize, -squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize, squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f( squareSize, -squareSize,-squareSize);   // Bottom Left
        }
        gl.glEnd();
        
        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        if ( locX > TerrLoc.dimX/2) {
        gl.glColor3f( xUp*1.1f, yUp*1.1f, zUp*1.1f);   // set the left of the quad
        gl.glVertex3f( -squareSize, -squareSize, squareSize);   // Top Left
        gl.glVertex3f( -squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( -squareSize, squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f( -squareSize, -squareSize,-squareSize);   // Bottom Left
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        gl.glColor3f( xUp*0.8f, yUp*0.8f, zUp*0.8f);   // set the color of front of the quad
        gl.glVertex3f(-squareSize, squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize,-squareSize, squareSize);   // Bottom Right
        gl.glVertex3f(-squareSize,-squareSize, squareSize);   // Bottom Left
        gl.glEnd();
        
        /* back of the cube
        gl.glColor3f( mobX/highX, 0.0f, mobZ/highZ);   // set the color of the back of the quad
        gl.glVertex3f(-squareSize/2f, squareSize/2f, -squareSize/2f);   // Top Left
        gl.glVertex3f( squareSize/2f, squareSize/2f, -squareSize/2f);   // Top Right
        gl.glVertex3f( squareSize/2f,-squareSize/2f, -squareSize/2f);   // Bottom Right
        gl.glVertex3f(-squareSize/2f,-squareSize/2f, -squareSize/2f);   // Bottom Left
         */
        
        if ( locZ == (int) tDimZ ) {
        	
        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        gl.glColor3f( red/4f, green/4f, blue/4f);         // set the color of front of the quad
        gl.glVertex3f(-WorldObject.cubeSize/3.0f, WorldObject.cubeSize/3.0f, squareSize);   // Top Left
        gl.glVertex3f( WorldObject.cubeSize/3.0f, WorldObject.cubeSize/3.0f, squareSize);   // Top Right
        gl.glVertex3f( WorldObject.cubeSize/3.0f,-WorldObject.cubeSize/3.0f, squareSize);   // Bottom Right
        gl.glVertex3f(-WorldObject.cubeSize/3.0f,-WorldObject.cubeSize/3.0f, squareSize);   // Bottom Left
        gl.glEnd();
        
        }
	}
}
