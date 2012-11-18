package boxland;

public class Mob extends WorldObject {
    
	private int mobEat = 0;
    private int mobCD = 0;
	public int  getMobCD() { return mobCD; }
	private int experience = 0;
	public int  getExperience() { return experience; }
	public void setExperience( int experience ) { this.experience = experience; }
	private int fedCount = 50;
	public int  getFedCount() { return fedCount; }
	public void setFedCount( int fedCount ) { this.fedCount = fedCount; }
	private int deathCount = 0;
	public int  getDeathCount() { return deathCount; }
	public void setDeathCount( int deathCount ) { this.deathCount = deathCount;}

    public Mob( String x, float r, float g, float b) {
    	super(x, r, g, b);
    }
    
    public Mob( String wID, float r, float g, float b, int i, int j) {
        	setWobId(wID);
        	mobCD = i;
        	mobEat = j;
        	setBaseR(r);
        	setBaseG(g);
        	setBaseB(b);
    }
    
	public void updateMob() {
		try { 
	    	String[] faceToken;
	    	faceToken = new String[7];
	
	    	// Check for starvation
	    	setFedCount(getFedCount() - 1);
	    	if ( getFedCount() < 1 ) {
	    		died("Starved");
	    		return;
	    	}
	    	
	        // Create choice tokens
	    	// it should have 6 possible moves plus 'do nothing'
	    	for ( int facing = 0; facing < 7; facing++) {
	    	
	    		int dirX = gX();
	        	int dirY = gY();
	        	int dirZ = gZ();
	        	
	        	// facing 0 ignored, it is the 'do nothing' move
	        	if ( facing == 1 ) dirX = dirX-1;
	            if ( facing == 2 ) dirX = dirX+1;
	    		if ( facing == 3 ) dirY = dirY-1;
	            if ( facing == 4 ) dirY = dirY+1;
	    		if ( facing == 5 ) dirZ = dirZ-1;
	            if ( facing == 6 ) dirZ = dirZ+1;
	            
	    		if ( BoxLoc.checkIsGround(dirX,dirY,dirZ) ) {
	    			faceToken[facing] = "Ground"; 
	    			} else { 
	    			if ( BoxLoc.checkHasGround(dirX,dirY,dirZ) ) {
	       			   faceToken[facing] = BoxLoc.getWobID(dirX, dirY, dirZ);
	    			   if ( facing == 0 ) 
	    			  	 faceToken[facing] = "Self";
	    			} else {
	    				 faceToken[facing] = "Air";
	    			}
	    		}
	    	}
	
	    	// Choice Events create decisions, choices ( if new ), then score them and return the 'best' facing
	    	int facing = BoxData.runChoiceEvent("{ ? = call choice_event( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}",
	    												getWobDBID(), gX(), gY(), gZ(),
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
	    		
	    		int dirX = gX();
	        	int dirY = gY();
	        	int dirZ = gZ();
	        	
	        	if ( facing == 1 ) dirX = dirX-1;
	            if ( facing == 2 ) dirX = dirX+1;
	    		if ( facing == 3 ) dirY = dirY-1;
	            if ( facing == 4 ) dirY = dirY+1;
	    		if ( facing == 5 ) dirZ = dirZ-1;
	            if ( facing == 6 ) dirZ = dirZ+1;
	    		
	            if ( BoxLoc.moveObj(this,dirX,dirY,dirZ) ) startAnimate(facing);
	    	}

		} catch ( Exception e ) { System.out.println("Exception is: " + e); }
    }
    
	public void died(String way) {
		try {
			setGrowCount(-10);
	    	setExplodeCount(0);
			setExperience(0);
	    	setFedCount(50);
	    	setDeathCount(getDeathCount() + 1);
	    	// Removes the mob from the grid and record it in the database as 'Killed' xyz
	    	BoxData.runDiedEvent("{ call died_event( ?,?,?,?,? )}", getWobDBID(), way, gX(), gY(), gZ() );
	    	BoxLoc.removeObj(this);
	    	BoxLoc.insertObj(this);
		} catch ( Exception e ) { System.out.println("Exception is: " + e); }
	}
    
	public void eat(WorldObject woEaten) {
		try {
			String eatenWobID = woEaten.getWobID();
			
			if ( !eatenWobID.equals(this.getWobID()))   // teams never eat themselves
			if ( mobEat != 0) 							// for mobs that don't eat
			if ( ( ( mobEat == 1 ) && ( eatenWobID.equals("Inert")) ) ||  // herbivores
				 ( ( mobEat == 2 ) && ( !eatenWobID.equals("Inert")) ) || // or carnivores
				   ( mobEat == 3 ) ) 									  // or anyone else
			{	// mob animation stuff
				if ( getExplodeCount() == 0 ) setExplodeCount(1);
	    		if ( getExperience() < 10 ) setExperience(getExperience() + 1);
	        	setFedCount(50);
	        	woEaten.died("Killed");	
	        };
		} catch ( Exception e ) { System.out.println("Exception is: " + e); }
	}
	
    public void drawAction() {
    	try {
	    	// This line adds experience to the draw size
			setShiftR(getExperience() * 0.05f);
			setShiftG(getExperience() * 0.05f);
			setShiftB(getExperience() * 0.05f);
			setDrawSize(getDrawSize() * ( (float) getFedCount() / 20f ));
    	} catch ( Exception e ) { System.out.println("Exception is: " + e); }		
    }

}
