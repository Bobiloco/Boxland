package boxland;

public class Mob extends WorldObject {

	public int experience = 0;
    public int fedCount = 50;
    int mobCD = 0;

    public Mob( String x, float r, float g, float b) {
    	super(x, r, g, b);
    }
    
    public Mob( String wID, float r, float g, float b, int i) {
        	wobId = wID;
        	colourR = r;
        	colourG = g;
        	colourB = b;
        	mobCD = i;
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
            
    		if ( !BoxLoc.checkIsGround(dirX,dirY,dirZ) ) {
    			if ( BoxLoc.checkHasGround(dirX,dirY,dirZ) ) {
    				
    				faceToken[facing] = BoxLoc.getWobID(dirX, dirY, dirZ);
    				if ( facing == 0 ) faceToken[facing] = "Self";
    			
    			}
    		}
    	}

    	// Choice Events create decisions, choices ( if new ), then score them and return the 'best' facing
    	int facing = Boxland.runChoiceEvent("{ ? = call choice_event( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}",
    												wobDBID, locX, locY, locZ,
    												faceToken[0],
    												faceToken[1],
    												faceToken[2],
    												faceToken[3],
    												faceToken[4],
    												faceToken[5],
    												faceToken[6]);

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
    		
            if ( BoxLoc.moveObj(this,dirX,dirY,dirZ) ) startAnimate(facing);
    	}
    }
    
	public void killed() {
		
		growCount = -10;
		experience = 0;
    	explodeCount = 0;
    	fedCount = 50;
    	
    	// Removes the mob from the grid and record it in the database as 'Killed' xyz
    	Boxland.runKilledEvent("{ call killed_event( ? )}", wobDBID );
		
    	// Remove it from the grid
    	BoxLoc.removeObj(this);
    	
    	// Re-insert the mob randomly
    	BoxLoc.insertObj(this);

	}
    
	public void eat(WorldObject woEaten) {
		
		if (woEaten.getWobID() != this.getWobID()) {
        	
			// This is new from superclass, mob animation stuff
			if ( explodeCount == 0 ) explodeCount = 1;
    		if ( experience < 10 ) experience++;
        	fedCount = fedCount + 50;
        	woEaten.killed();	
		}
	}
	
    public void drawAction() {

    	// This line adds experience to the draw size
		drawSize = drawSize + experience*0.05f;
    }

	public int getMobCD() {
		return mobCD;
	}
}
