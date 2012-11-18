package boxland;

import javax.media.opengl.GL2;

public class WorldObject {

	/**
	 * WorldObject - Objects that can be placed in BoxLoc
	 * 
	 * - Basic interaction code for objects, handles 'Inerts'
	 *  
	 * Bernard McManus, Nov 2012
	 * Source code under CC BY 3.0 
	 */
	
	public static final float cubeSize = 0.5f;
    
    private float drawSize;
    private float growSize = cubeSize / 10.0f;
    private float growCount = -10;
    private final int explodeSize = BoxLoc.teamSize * BoxLoc.teamsNumber;
    private int explodeCount = 0;
    private String wobId = "Wob";
    private int wobDBID = -1;
    
	// mob location
	private int locX;
    private int locY;
    private int locZ;
    
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float offsetZ = 0.0f;
    
    private float baseR = 0.0f;
    private float baseG = 0.0f;
    private float baseB = 0.0f;
    
    private float shiftR = 0.0f;
    private float shiftG = 0.0f;
    private float shiftB = 0.0f;
   
    public WorldObject(){}
    
    public WorldObject( String wID, float r, float g, float b) {
    	setWobId(wID);
    	setBaseR(r);
    	setBaseG(g);
    	setBaseB(b);
    }
    
    public void setXYZ(int x, int y, int z) {
		locX = x;
		locY = y;
		locZ = z;
	}

    public void setWobDBID(int newDBID) { wobDBID = newDBID; }
    public String getWobID() { return getWobId(); }
    public int getWobDBID() { return wobDBID; }
    public int gX() { return locX; }
    public int gY() { return locY; }
    public int gZ() { return locZ; }
    public float getBaseR() { return baseR; }
	public void setBaseR(float baseR) {	this.baseR = baseR;	}
	public float getBaseG() { return baseG;	}
	public void setBaseG(float baseG) {	this.baseG = baseG;	}
	public float getBaseB() { return baseB;	}
	public void setBaseB(float baseB) {	this.baseB = baseB;	}
	public float getShiftR() { return shiftR; }
	public void setShiftR(float shiftR) { this.shiftR = shiftR; }
	public float getShiftG() { return shiftG; }
	public void setShiftG(float shiftG) { this.shiftG = shiftG; }
	public float getShiftB() { return shiftB; }
	public void setShiftB(float shiftB) { this.shiftB = shiftB;	}
    
	public void eat(WorldObject woEaten) {
		try {
			// Nothing will eat its own type
			if (woEaten.getWobID() != this.getWobID() ) woEaten.died("Killed");
		} catch ( Exception e) {
			
		}
	}

	public void died(String way) {
		try {
		setGrowCount(-10);
		BoxLoc.removeObj(this);
		BoxLoc.insertObj(this);
		} catch ( Exception e ) { System.out.println("Exception is: " + e); }
	}

	public void startAnimate(int facing) {
		try {
		// Creates an offset towards the original location 
		//  that starts counting down ( because they're already in the new location )
		if ( facing == 2 ) offsetX = -10;
		if ( facing == 1 ) offsetX = 10;
		if ( facing == 4 ) offsetY = -10;
		if ( facing == 3 ) offsetY = 10;
		if ( facing == 6 ) offsetZ = -10;
		if ( facing == 5 ) offsetZ = 10;
		} catch ( Exception e ) { System.out.println("Exception is: " + e); }
	}
	
	public void animate() {
		try {
			float offsetStep = ( 10.0f / ( (float) BoxLoc.teamSize * (float) BoxLoc.teamsNumber) );
			
			// The sizeMultiple is to track animation frames and scale movement accordingly
			if ( Math.round(offsetX) > 0 ) offsetX = offsetX - offsetStep;
			if ( Math.round(offsetX) < 0 ) offsetX = offsetX + offsetStep;
			if ( Math.round(offsetY) > 0 ) offsetY = offsetY - offsetStep;
			if ( Math.round(offsetY) < 0 ) offsetY = offsetY + offsetStep;
			if ( Math.round(offsetZ) > 0 ) offsetZ = offsetZ - offsetStep;
			if ( Math.round(offsetZ) < 0 ) offsetZ = offsetZ + offsetStep;
	
			if ( Math.round(getGrowCount()) < 0 ) setGrowCount(getGrowCount() + offsetStep / 10.0f);
	
	    	if ( getExplodeCount() > 0 ) setExplodeCount(getExplodeCount() + 1);
			if ( getExplodeCount() > explodeSize ) setExplodeCount(0);
			
			setDrawSize(cubeSize + ( getGrowCount() * growSize ) + ( getExplodeCount() * growSize ) / explodeSize);
		} catch ( Exception e ) { System.out.println("Exception is: " + e); }
	}
	
	public void drawAction() {};
	
	public void drawObj (GL2 gl2) {
		try {
			animate();
			drawAction();
			drawObjColour(gl2, (getBaseR() + getShiftR()) , (getBaseG() + getShiftG()), (getBaseB() + getShiftB()));
		} catch ( Exception e ) {
			System.out.println("Exception is: " + e);
		}
	}
	
	public void drawObjColour (GL2 gl2, float red, float green, float blue) {
		try {
		float squareSize = getDrawSize() / 2.0f;
		
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
        
        if ( locY < (BoxLoc.dimY - Boxland.sizeAdjustY/10f ) /2.0f) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
        gl.glColor3f( xUp*1.1f, yUp*1.1f, zUp*1.1f );   // set the top color of the quad
        gl.glNormal3d(0, 1, 0);
        gl.glVertex3f(-squareSize, squareSize, squareSize);   // Top Left
        gl.glVertex3f( squareSize, squareSize, squareSize);   // Top Right
        gl.glVertex3f( squareSize, squareSize,-squareSize);   // Bottom Right
        gl.glVertex3f(-squareSize, squareSize,-squareSize);   // Bottom Left
        gl.glEnd();
        }
        
        if ( locY > (BoxLoc.dimY - Boxland.sizeAdjustY/10f ) /2.0f) {
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
	        gl.glEnd();
        	}	 
		} catch ( Exception e ) { System.out.println("Exception is: " + e);
		}
	}

	public String getWobId() {
		return wobId;
	}

	public void setWobId(String wobId) {
		this.wobId = wobId;
	}

	public float getGrowCount() {
		return growCount;
	}

	public void setGrowCount(float growCount) {
		this.growCount = growCount;
	}

	public int getExplodeCount() {
		return explodeCount;
	}

	public void setExplodeCount(int explodeCount) {
		this.explodeCount = explodeCount;
	}

	public float getDrawSize() {
		return drawSize;
	}

	public void setDrawSize(float drawSize) {
		this.drawSize = drawSize;
	}

	
}
