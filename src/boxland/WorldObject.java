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
	
	public static final double cubeSize = 1.0;
    private double drawSize;
    private double growSize = cubeSize/10;
    private double growCount = -10;
    private final int explodeSize = BoxLoc.teamSize*BoxLoc.teamsNumber;
    private int explodeCount = 0;
    private String wobId = "Wob";
    private int wobDBID = -1;
    
	// mob location
	private int locX;
    private int locY;
    private int locZ;
    
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    
    private double baseR = 0;
    private double baseG = 0;
    private double baseB = 0;
    
    private double shiftR = 0;
    private double shiftG = 0;
    private double shiftB = 0;
   
    public WorldObject(){}
    
    public WorldObject( String wID, double r, double g, double b) {
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
    public double getBaseR() { return baseR; }
	public void setBaseR(double baseR) { this.baseR = baseR; }
	public double getBaseG() { return baseG; }
	public void setBaseG(double baseG) { this.baseG = baseG; }
	public double getBaseB() { return baseB; }
	public void setBaseB(double baseB) { this.baseB = baseB; }
	public double getShiftR() { return shiftR; }
	public void setShiftR(double shiftR) { this.shiftR = shiftR; }
	public double getShiftG() { return shiftG; }
	public void setShiftG(double shiftG) { this.shiftG = shiftG; }
	public double getShiftB() { return shiftB; }
	public void setShiftB(double shiftB) { this.shiftB = shiftB; }
    
	public void eat(WorldObject woEaten) {
		try { // Nothing will eat its own type
			if (woEaten.getWobID() != this.getWobID() ) woEaten.died("Killed");

		} catch ( Exception e) { System.out.println("WorldObject.eat(): " + e); }
	}

	public void died(String way) {

		try {
			setGrowCount(-10);
			BoxLoc.removeObj(this);
			BoxLoc.insertObj(this);

		} catch ( Exception e ) { System.out.println("WorldObject.died(): " + e); }
	}

	public void startAnimate(int facing) {
		try { // Creates an offset towards the original location 
			  //  that starts counting down ( because they're already in the new location )
			if ( facing == 2 ) offsetX = -10;
			if ( facing == 1 ) offsetX = 10;
			if ( facing == 4 ) offsetY = -10;
			if ( facing == 3 ) offsetY = 10;
			if ( facing == 6 ) offsetZ = -10;
			if ( facing == 5 ) offsetZ = 10;
		} catch ( Exception e ) { System.out.println("WorldObject.startAnimate(): " + e); }
	}
	
	public void animate() {
		try {
			double offsetStep = (double) 10/(BoxLoc.teamSize*BoxLoc.teamsNumber);
			
			// The sizeMultiple is to track animation frames and scale movement accordingly
			if ( Math.round(offsetX) > 0 ) offsetX = offsetX - offsetStep;
			if ( Math.round(offsetX) < 0 ) offsetX = offsetX + offsetStep;
			if ( Math.round(offsetY) > 0 ) offsetY = offsetY - offsetStep;
			if ( Math.round(offsetY) < 0 ) offsetY = offsetY + offsetStep;
			if ( Math.round(offsetZ) > 0 ) offsetZ = offsetZ - offsetStep;
			if ( Math.round(offsetZ) < 0 ) offsetZ = offsetZ + offsetStep;
	
			if ( Math.round(getGrowCount()) < 0 ) setGrowCount(getGrowCount() + offsetStep / 10);
	
	    	if ( getExplodeCount() > 0 ) setExplodeCount(getExplodeCount() + 1);
			if ( getExplodeCount() > explodeSize ) setExplodeCount(0);
			
			setDrawSize(cubeSize + ( getGrowCount() * growSize ) + ( getExplodeCount() * growSize ) / explodeSize);
		} catch ( Exception e ) { System.out.println("WorldObject.animate(): " + e); }
	}
	
	public void drawAction() {};
	
	public void drawObj (GL2 gl2) {
		try {
			animate();
			drawAction();
			drawObjColour(gl2,(getBaseR()+getShiftR()), 
					          (getBaseG()+getShiftG()), 
					          (getBaseB()+getShiftB()));
		} catch ( Exception e ) {
			System.out.println("WorldObject.drawObj(): " + e);
		}
	}
	
	public void drawObjColour (GL2 gl2, double red, double green, double blue) {
		try {
			double squareSize=getDrawSize()/2;
			
			final GL2 gl = gl2.getGL().getGL2();
			
			gl.glLoadIdentity();
	        gl.glTranslated( offsetX*0.1+BoxLoc.startX+locX, 
	        		         offsetY*0.1+BoxLoc.startY+locY+Boxland.sizeAdjustY, 
	        		         offsetZ*0.1+BoxLoc.startZ+locZ );
	
	        double tDimX = BoxLoc.dimX-1;
	        double tDimY = BoxLoc.dimY-1;
	        double tDimZ = BoxLoc.dimZ-1;
	        
	        double xUp = ((locX/tDimX) + red)/2;
	        double yUp = ((locY/tDimY) + green)/2;
	        double zUp = ((locZ/tDimZ) + blue)/2;
	        
	        if ( locY <= (BoxLoc.dimY-Boxland.sizeAdjustY/10)/2) {
	        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
	        gl.glColor3d(xUp*1.1,yUp*1.1,zUp*1.1);   // set the top color of the quad
	        gl.glNormal3d(0,1,0);
	        gl.glVertex3d(-squareSize, squareSize, squareSize);   // Top Left
	        gl.glVertex3d( squareSize, squareSize, squareSize);   // Top Right
	        gl.glVertex3d( squareSize, squareSize,-squareSize);   // Bottom Right
	        gl.glVertex3d(-squareSize, squareSize,-squareSize);   // Bottom Left
	        gl.glEnd();
	        }
	        
	        if ( locY > (BoxLoc.dimY-Boxland.sizeAdjustY/10)/2) {
	        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
	        gl.glColor3d(xUp*1.1,yUp*1.1,zUp*1.1);   // set the bottom colour of the quad
	        gl.glNormal3d(0,-1,0);        
	        gl.glVertex3d(-squareSize, -squareSize, squareSize);   // Top Left
	        gl.glVertex3d( squareSize, -squareSize, squareSize);   // Top Right
	        gl.glVertex3d( squareSize, -squareSize,-squareSize);   // Bottom Right
	        gl.glVertex3d(-squareSize, -squareSize,-squareSize);   // Bottom Left
	        gl.glEnd();
	        }
	
	        if ( locX < BoxLoc.dimX/2) {
	        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
	        gl.glColor3d(xUp,yUp,zUp);   // set the right of the quad
	        gl.glNormal3d(1,0,0);
	        gl.glVertex3d( squareSize, -squareSize, squareSize);   // Top Left
	        gl.glVertex3d( squareSize, squareSize, squareSize);   // Top Right
	        gl.glVertex3d( squareSize, squareSize,-squareSize);   // Bottom Right
	        gl.glVertex3d( squareSize, -squareSize,-squareSize);   // Bottom Left
	        gl.glEnd();
	        }
	       
	        if ( locX > BoxLoc.dimX/2) {
	        gl.glBegin(GL2.GL_TRIANGLE_FAN);
	        gl.glColor3d(xUp,yUp,zUp);   // set the left of the quad
	        gl.glNormal3d(-1,0,0);
	        gl.glVertex3d( -squareSize, -squareSize, squareSize);   // Top Left
	        gl.glVertex3d( -squareSize, squareSize, squareSize);   // Top Right
	        gl.glVertex3d( -squareSize, squareSize,-squareSize);   // Bottom Right
	        gl.glVertex3d( -squareSize, -squareSize,-squareSize);   // Bottom Left
	        gl.glEnd();
	        }
	
	        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
	        gl.glNormal3d(0,0,1);
	        gl.glColor3d(xUp*0.8,yUp*0.8,zUp*0.8);   // set the color of front of the quad
	        gl.glVertex3d(-squareSize, squareSize, squareSize);   // Top Left
	        gl.glVertex3d( squareSize, squareSize, squareSize);   // Top Right
	        gl.glVertex3d( squareSize,-squareSize, squareSize);   // Bottom Right
	        gl.glVertex3d(-squareSize,-squareSize, squareSize);   // Bottom Left
	        gl.glEnd();
	        
	        /* back of the cube
	        gl.glColor3d( mobX/highX, 0.0f, mobZ/highZ);   // set the color of the back of the quad
	        gl.glVertex3d(-squareSize/2f, squareSize/2f, -squareSize/2f);   // Top Left
	        gl.glVertex3d( squareSize/2f, squareSize/2f, -squareSize/2f);   // Top Right
	        gl.glVertex3d( squareSize/2f,-squareSize/2f, -squareSize/2f);   // Bottom Right
	        gl.glVertex3d(-squareSize/2f,-squareSize/2f, -squareSize/2f);   // Bottom Left
	         */
	        
	        if ( locZ == (int) tDimZ ) {
	        	
	        	gl.glLoadIdentity();
	
	        	double calcX = 0;
	        	double calcY = 0;
	        	double calcZ = 0;
	        	
	        	if ( offsetX > 4 ) calcX = BoxLoc.startX + locX + 1;
	        	if ( offsetX <= 4 && offsetX >= -4) calcX = BoxLoc.startX + locX;
	        	if ( offsetX < -4 ) calcX = BoxLoc.startX + locX - 1;
	        	
	        	if ( offsetY > 4 ) calcY = BoxLoc.startY + locY + 1;
	        	if ( offsetY <= 4 && offsetY >= -4) calcY = BoxLoc.startY + locY;
	        	if ( offsetY < -4 ) calcY = BoxLoc.startY + locY - 1;
	
	        	calcZ = BoxLoc.startZ + locZ;
	
	        	gl.glTranslated(calcX,calcY+Boxland.sizeAdjustY,calcZ);
	        	
		        double drawSize = 0.5;
	        	
		        gl.glBegin(GL2.GL_TRIANGLE_FAN);       
	        	gl.glColor4d(locX/tDimX,locY/tDimY,1,0.3);         // set the color of front of the quad
		        gl.glVertex3d(-drawSize, drawSize, squareSize);   // Top Left
		        gl.glVertex3d( drawSize, drawSize, squareSize);   // Top Right
		        gl.glVertex3d( drawSize,-drawSize, squareSize);   // Bottom Right
		        gl.glVertex3d(-drawSize,-drawSize, squareSize);   // Bottom Left
		        gl.glEnd();
	        	}	 
		} catch ( Exception e ) { System.out.println("WorldObject.drawObjColour(): " + e);
		}
	}

	public String getWobId() {
		return wobId;
	}

	public void setWobId(String wobId) {
		this.wobId = wobId;
	}

	public double getGrowCount() {
		return growCount;
	}

	public void setGrowCount(double growCount) {
		this.growCount = growCount;
	}

	public int getExplodeCount() {
		return explodeCount;
	}

	public void setExplodeCount(int explodeCount) {
		this.explodeCount = explodeCount;
	}

	public double getDrawSize() {
		return drawSize;
	}

	public void setDrawSize(double drawSize) {
		this.drawSize = drawSize;
	}

}
