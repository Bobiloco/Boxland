package boxland;

import java.util.Random;
import javax.media.opengl.GL2;

public class Mob extends WorldObject {

    public int experience = 0;
    public int explodeSize = 10;
    public int explodeCount = 0;
    public int fedCount = 50;
    
    private int mobID = -1;
    private int mobDBID = -1;
    
    Random generator = new Random();
    
    public Mob(int mID) {
		mobID = mID;
	}

	public int getMobID() {
    	return mobID;
    }
    
	public String getID() {
		return "Mob";
	}
    
    public void setMobDBID(int MobDBID) { 
    	this.mobDBID = MobDBID;
    }
    
    public int getMobDBID() {
    	return mobDBID;
    }
    
    public void updateMob() {
		
    	String[] faceToken;
    	faceToken = new String[7];

    	// Check for starvation
    	fedCount--;
    	if ( fedCount < 1 ) {
    		killed();
    		return;
    	}
    	
        // Create choice tokens
    	// it should have 6 possible moves plus 'do nothing'
    	for ( int facing = 0; facing < 7; facing++) {
    	
    		int dirX = locX;
        	int dirY = locY;
        	int dirZ = locZ;
        	
        	// facing 0 ignored, it is the 'do nothing' move
        	if ( facing == 1 ) dirX = dirX-1;
            if ( facing == 2 ) dirX = dirX+1;
    		if ( facing == 3 ) dirY = dirY-1;
            if ( facing == 4 ) dirY = dirY+1;
    		if ( facing == 5 ) dirZ = dirZ-1;
            if ( facing == 6 ) dirZ = dirZ+1;
            
    		if ( !TerrLoc.checkIsGround(dirX,dirY,dirZ) ) {
    			if ( TerrLoc.checkHasGround(dirX,dirY,dirZ) ) {
    				
    				faceToken[facing] = TerrLoc.getWobID(dirX, dirY, dirZ);
    				if ( facing == 0 ) faceToken[facing] = "Self";
    			
    				// System.out.println("Created faceToken[" + facing + "] = " + faceToken[facing] );
    			}
    		}
    	}
    	// This will return an existing decisionDBID or create a new one ( and a new choice node ) and return it
    	int facing = Cubists.runChoiceEvent("{ ? = call choice_event( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}",
    												mobDBID,
    												locX,
    												locY,
    												locZ,
    												faceToken[0],
    												faceToken[1],
    												faceToken[2],
    												faceToken[3],
    												faceToken[4],
    												faceToken[5],
    												faceToken[6]);

    	// System.out.println("Created or chose facing: " + facing );
    	
    	if ( facing == 0 ) {
    		// sit still
    	} else { 
    		
    		int dirX = locX;
        	int dirY = locY;
        	int dirZ = locZ;
        	
        	if ( facing == 1 ) dirX = dirX-1;
            if ( facing == 2 ) dirX = dirX+1;
    		if ( facing == 3 ) dirY = dirY-1;
            if ( facing == 4 ) dirY = dirY+1;
    		if ( facing == 5 ) dirZ = dirZ-1;
            if ( facing == 6 ) dirZ = dirZ+1;
    		
            if ( TerrLoc.moveObj(this,dirX,dirY,dirZ) ) startAnimate(facing);
    	}
    }
    
	public void killed() {
		
		growCount = -10;
		experience = 0;
    	explodeCount = 0;
    	fedCount = 50;
    	
    	// Removes the mob from the grid and record it in the database as 'Killed' xyz

    	Cubists.runKilledEvent("{ call killed_event( ? )}", mobDBID );
		
    	// Remove it from the grid
    	TerrLoc.removeObj(this);
    	
    	// Re-insert the mob randomly
		TerrLoc.insertObj(this);

	}
    
    public void explode() {
    	if ( explodeCount > 0 ) {
			explodeCount++;
			if ( explodeCount > explodeSize ) explodeCount = 0;
		}
    }
    
    public void drawActions() {

    	growSize();
		explode();
		this.animate();
		if ( experience > 10 ) experience = 10;
   	    drawSize = cubeSize + ( growCount + experience ) * growSize + explodeCount * growSize ;

    }
    
	public void drawObj(GL2 gl2) { 
		
		drawActions();
		
		this.drawObjColour(gl2, 0.0f, 0.0f, 0.0f);
	
	};
    
}
