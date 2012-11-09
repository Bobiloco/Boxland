package boxland;

import javax.media.opengl.GL2;

public class WorldObject {

    public static final float cubeSize = 0.5f;
    
    public float drawSize;
    public float growSize = cubeSize / 10.0f;
    public float growCount = -10;
    public final int explodeSize = BoxLoc.teamSize * BoxLoc.teamsNumber;
    public int explodeCount = 0;
    public String wobId = "Wob";
    public int wobDBID = -1;
    
	// mob location
	public int locX;
    public int locY;
    public int locZ;
    
    public float offsetX = 0.0f;
    public float offsetY = 0.0f;
    public float offsetZ = 0.0f;
    
    public float colourR = 0.0f;
    public float colourG = 0.0f;
    public float colourB = 0.0f;
    
    public WorldObject(){
    }
    
    public WorldObject( String wID, float r, float g, float b) {
    	wobId = wID;
    	colourR = r;
    	colourG = g;
    	colourB = b;
    }
    
    public void updateXYZ(int x, int y, int z) {
		locX = x;
		locY = y;
		locZ = z;
	}

	public String getWobID() {
		// returns the string identifier of the world object
		return wobId;
	}
	
	public void setWobDBID(int newDBID) {
		// returns the DBID, if applied
		wobDBID = newDBID;
	}

	public int getWobDBID() {
		// returns the DBID, if applied
		return wobDBID;
	}
    
    public int gX() { return locX; }
    public int gY() { return locY; }
    public int gZ() { return locZ; }
    
	public void eat(WorldObject woEaten) {
		// Nothing will eat its own type
		if (woEaten.getWobID() != this.getWobID() ) woEaten.killed();
	}

	public void killed() {
		growCount = -10;
		BoxLoc.removeObj(this);
		BoxLoc.insertObj(this);
	}

	public void startAnimate(int facing) {
		
		// Creates an offset towards the original location 
		//  that starts counting down ( because they're already in the new location )
		if ( facing == 2 ) offsetX = -10;
		if ( facing == 1 ) offsetX = 10;
		if ( facing == 4 ) offsetY = -10;
		if ( facing == 3 ) offsetY = 10;
		if ( facing == 6 ) offsetZ = -10;
		if ( facing == 5 ) offsetZ = 10;
	}
	
	public void animate() {
		
		float offsetStep = ( 10.0f / ( (float) BoxLoc.teamSize * (float) BoxLoc.teamsNumber) );
		
		// The sizeMultiple is to track animation frames and scale movement accordingly
		if ( Math.round(offsetX) > 0 ) offsetX = offsetX - offsetStep;
		if ( Math.round(offsetX) < 0 ) offsetX = offsetX + offsetStep;
		if ( Math.round(offsetY) > 0 ) offsetY = offsetY - offsetStep;
		if ( Math.round(offsetY) < 0 ) offsetY = offsetY + offsetStep;
		if ( Math.round(offsetZ) > 0 ) offsetZ = offsetZ - offsetStep;
		if ( Math.round(offsetZ) < 0 ) offsetZ = offsetZ + offsetStep;

		if ( Math.round(growCount) < 0 ) growCount = growCount + offsetStep / 10.0f;

    	if ( explodeCount > 0 ) explodeCount++;
		if ( explodeCount > explodeSize ) explodeCount = 0;
		
		drawSize = cubeSize + ( growCount * growSize ) + ( 10.0f * explodeCount * growSize ) / explodeSize;
	}
	
	public void drawAction() {};
	
	public void drawObj (GL2 gl2) {
		
		animate();
		drawAction();
		drawObjColour(gl2, colourR, colourG, colourB);

	}
	
	public void drawObjColour (GL2 gl2, float red, float green, float blue) {

		float squareSize = drawSize / 2.0f;
		
		final GL2 gl = gl2.getGL().getGL2();
		
		gl.glLoadIdentity();
        gl.glTranslatef( offsetX*0.1f + BoxLoc.startX + (float) locX, 
        		         offsetY*0.1f + BoxLoc.startY + (float) locY + Boxland.sizeAdjustY, 
        		         offsetZ*0.1f + BoxLoc.startZ + (float) locZ );

        float tDimX = BoxLoc.dimX - 1.0f;
        float tDimY = BoxLoc.dimY - 1.0f;
        float tDimZ = BoxLoc.dimZ - 1.0f;
        
        float xUp = (((float)locX/tDimX) + red)/2;
        float yUp = (((float)locY/tDimY) + green)/2;
        float zUp = (((float)locZ/tDimZ) + blue)/2;
        
        if ( locY < BoxLoc.dimY/2.0f) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        gl.glColor3f( xUp*1.1f, yUp*1.1f, zUp*1.1f );   // set the top color of the quad
        gl.glNormal3d(0, 1, 0);
        gl.glVertex3f(-squareSize, squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize, squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f(-squareSize, squareSize,-squareSize);   // Bottom Left
        gl.glEnd();
        }
        
        if ( locY > BoxLoc.dimY/2.0f) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        gl.glColor3f( xUp*1.1f, yUp*1.1f, zUp*1.1f);   // set the bottom colour of the quad
        gl.glNormal3d(0, -1, 0);        
        gl.glVertex3f(-squareSize, -squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, -squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize, -squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f(-squareSize, -squareSize,-squareSize);   // Bottom Left
        gl.glEnd();
        }

        if ( locX < BoxLoc.dimX/2) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        gl.glColor3f( xUp, yUp, zUp);   // set the right of the quad
        gl.glNormal3d(1, 0, 0);
        gl.glVertex3f( squareSize, -squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize, squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f( squareSize, -squareSize,-squareSize);   // Bottom Left
        gl.glEnd();
        }
        
       
        if ( locX > BoxLoc.dimX/2) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glColor3f( xUp, yUp, zUp);   // set the left of the quad
        gl.glNormal3d(-1, 0, 0);
        gl.glVertex3f( -squareSize, -squareSize, squareSize);   // Top Left
        gl.glVertex3f( -squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( -squareSize, squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f( -squareSize, -squareSize,-squareSize);   // Bottom Left
        gl.glEnd();
        }


        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        gl.glNormal3d(0, 0, 1);
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
        	
        	gl.glLoadIdentity();

        	float calcX = 0;
        	float calcY = 0;
        	float calcZ = 0;
        	
        	if ( offsetX > 4 ) calcX = BoxLoc.startX + (float) locX + 1.0f;
        	if ( offsetX <= 4 && offsetX >= -4) calcX = BoxLoc.startX + (float) locX;
        	if ( offsetX < -4 ) calcX = BoxLoc.startX + (float) locX - 1.0f;
        	
        	if ( offsetY > 4 ) calcY = BoxLoc.startY + (float) locY + 1.0f;
        	if ( offsetY <= 4 && offsetY >= -4) calcY = BoxLoc.startY + (float) locY;
        	if ( offsetY < -4 ) calcY = BoxLoc.startY + (float) locY - 1.0f;

        	calcZ = BoxLoc.startZ + (float) locZ;

        	gl.glTranslatef( calcX, calcY + Boxland.sizeAdjustY, calcZ );
        	
	        float drawSize = 0.5f;
        	
	        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        	gl.glColor4f( (float) locX/tDimX, (float) locY/tDimY, 1.0f, 0.3f);         // set the color of front of the quad
	        gl.glVertex3f(-drawSize, drawSize, squareSize);   // Top Left
	        gl.glVertex3f( drawSize, drawSize, squareSize);   // Top Right
	        gl.glVertex3f( drawSize,-drawSize, squareSize);   // Bottom Right
	        gl.glVertex3f(-drawSize,-drawSize, squareSize);   // Bottom Left
	        /*	        gl.glVertex3f(-WorldObject.cubeSize/3.0f, WorldObject.cubeSize/3.0f, squareSize);   // Top Left
	        gl.glVertex3f( WorldObject.cubeSize/3.0f, WorldObject.cubeSize/3.0f, squareSize);   // Top Right
	        gl.glVertex3f( WorldObject.cubeSize/3.0f,-WorldObject.cubeSize/3.0f, squareSize);   // Bottom Right
	        gl.glVertex3f(-WorldObject.cubeSize/3.0f,-WorldObject.cubeSize/3.0f, squareSize);   // Bottom Left
	        */
	        gl.glEnd();
        
        }
	}
}
