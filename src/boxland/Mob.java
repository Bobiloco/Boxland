package boxland;

public class Mob extends WorldObject {

	public int experience = 0;
    public int fedCount = 50;
    private int mobCD = 0;
    private int mobEat = 0;
    public int deathCount = 0;
    
    public float colourR = 0.0f;
    public float colourG = 0.0f;
    public float colourB = 0.0f;
    
    public Mob( String x, float r, float g, float b) {
    	super(x, r, g, b);
    }
    
    public Mob( String wID, float r, float g, float b, int i, int j) {
        	wobId = wID;
        	baseR = r;
        	baseG = g;
        	baseB = b;
        	mobCD = i;
        	mobEat = j;
    }
    
	public int getMobCD() {
		return mobCD;
	}
    
    public void updateMob() {
		
    	String[] faceToken;
    	faceToken = new String[7];

    	// Check for starvation
    	fedCount--;
    	if ( fedCount < 1 ) {
    		died("Starved");
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
    
	public void died(String way) {
		
		growCount = -10;
		experience = 0;
    	explodeCount = 0;
    	fedCount = 50;
    	deathCount++;
    	
    	// Removes the mob from the grid and record it in the database as 'Killed' xyz
    	Boxland.runDiedEvent("{ call died_event( ?,?,?,?,? )}", wobDBID, way, this.locX, this.locY, this.locZ );
		
    	// Remove it from the grid
    	BoxLoc.removeObj(this);
    	
    	// Re-insert the mob randomly
    	BoxLoc.insertObj(this);

	}
    
	public void eat(WorldObject woEaten) {
		
		String eatenWobID = woEaten.getWobID();
		
		if ( !eatenWobID.equals(this.getWobID()))   // teams never eat themselves
		if ( mobEat != 0) 							// for mobs that don't eat
		if ( ( ( mobEat == 1 ) && ( eatenWobID.equals("Inert")) ) ||  // herbivores
			 ( ( mobEat == 2 ) && ( !eatenWobID.equals("Inert")) ) || // or carnivores
			   ( mobEat == 3 ) ) 									  // or anyone else
		{
			// mob animation stuff
			if ( explodeCount == 0 ) explodeCount = 1;
    		if ( experience < 10 ) experience++;
        	fedCount = fedCount + 50;
        	if ( fedCount > 100 ) fedCount = 100;
        	woEaten.died("Killed");	
		};
	}
	
    public void drawAction() {

    	// This line adds experience to the draw size
		shiftR = experience * 0.05f;
		shiftG = experience * 0.05f;
		shiftB = experience * 0.05f;
		drawSize = drawSize * ( (float) fedCount / 50f );
		
    }

}
