package boxland;

import java.util.Random;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public class BoxLoc {

	/**
	 * BoxLoc - The simulation environment
	 * 
	 * - Creates the space and initialized the mob objects within
	 * - Also controls movement and collisions and the main game loop
	 * 
	 * Bernard McManus, Nov 2012
	 * Source code under CC BY 3.0 
	 */
	
	//public static final String gameMode = "FindFoodPR";
	public static final String gameMode = "FindFood";
	//public static final String gameMode = "Survival";
	//public static final String gameMode = "RedBlue";
	//public static final String gameMode = "FourSpawn";
	
	// config variables
	public static final int teamsNumber = 1;
	public static final int teamSize = 10;
	public static final int inertsNumber = 40;
	public static final int dimX = 20; 
	public static final int dimY = 8; 
	public static final int dimZ = 10; 
	public static final double startX = -dimX/2;
	public static final double startY = -dimY/2;
	public static final double startZ = -dimZ-((dimX+dimY)/2);
	
	// Counters
	private static int gameSteps    = 0;
	private static int animateCount = 0;
	private static double pulse     = 0;
	
	// Memory container arrays
	private static double teamExpScore[] = new double[teamsNumber];
	private static double expScore[]     = new double[teamsNumber];
	private static int fedScore[]        = new int[teamsNumber];
	private static int deathScore[]      = new int[teamsNumber];
	private static WorldObject[][][] wobjects;
	private static Mob theMobs[] = new Mob[teamsNumber * teamSize];
	private static WorldObject theInerts[] = new WorldObject[inertsNumber];
	
	// used to identify other elements in the grid by their string
	public static String getWobID(int x, int y, int z) {
		try {
			if ( wobjects[x][y][z] != null ) 
				return wobjects[x][y][z].getWobID();
		} catch (Exception e) {  
	        System.out.println("The exception raised is:" + e);
	        return "OutofBound";
	    }  
		return "Vacant";
	}

	// Check for along borders, later for climbable surfaces
	public static boolean checkHasGround(int x, int y, int z) {
		if ( (x == 0) || (y == 0) || (z == 0) || (x == dimX-1) || (y == dimY-1) || (z == dimZ-1) )				
				return true;
		return false;
	}
	
	// Check for the walls
	public static boolean checkIsGround(int x, int y, int z) {
		if ( (x == -1) || (y == -1) || (z == -1) || (x == dimX) || (y == dimY) || (z == dimZ) ) 
			return true;
		return false;
	}
	
	public static WorldObject getObj(int x, int y, int z) {
		try {			
			return wobjects[x][y][z];
		} catch (Exception e) {
			System.out.println("The exception raised is:" + e);
			return null;
		}
	}
	
	public static boolean insertObj(WorldObject wo) {
		
		Random random = new Random();
		
		try {
			// try 100 times to places it randomly
			for (int i=0; i<100; i++) {
				
				int randX = random.nextInt((int) dimX);
				int randY = random.nextInt((int) dimY);
				int randZ = random.nextInt((int) dimZ);
			
				//place it against some edge by min/maxing a random coordinate
				int rand6 = random.nextInt(6);
				
				if ( gameMode.equals("FindFood"))
					if ( wo.getWobID().equals("Inert") ) rand6 = 1; else { rand6 = 4; };
				
				if ( gameMode.equals("FindFoodPR")) {
					if ( wo.getWobID().equals("Inert") ) rand6 = 1;
					if ( wo.getWobID().equals("Red") ) rand6 = 4;
					if ( wo.getWobID().equals("Blue") ) rand6 = 3;
				}
			
				if ( gameMode.equals("FourSpawn")) {
					if ( wo.getWobID().equals("Inert") ) rand6 = 1;
					if ( wo.getWobID().equals("Red") ) rand6 = 0;
					if ( wo.getWobID().equals("Blue") ) rand6 = 3;
					if ( wo.getWobID().equals("Yellow") ) rand6 = 2;
					if ( wo.getWobID().equals("Zombie") ) rand6 = 1;
				}
				
				if ( gameMode.equals("RedBlue")) {
					if ( wo.getWobID().equals("Red") ) rand6 = 0;
					if ( wo.getWobID().equals("Blue") ) rand6 = 3;
				}
			
				if ( rand6 == 0 ) randX = 0;
				if ( rand6 == 1 ) randY = 0;
				if ( rand6 == 2 ) randZ = 0;
				if ( rand6 == 3 ) randX = (int) (dimX-1);
				if ( rand6 == 4 ) randY = (int) (dimY-1);
				if ( rand6 == 5 ) randZ = (int) (dimZ-1);
				
				if ( wobjects[randX][randY][randZ] == null ) { 
					wobjects[randX][randY][randZ] = wo; 
					wo.setXYZ(randX, randY, randZ);
					return true;
				}
			}
			throw new Exception("NoPlaceFound");
		} catch (Exception e) {
			System.out.println("Wasn't able to insert after 100 times: " + e );
			return false;
		}
	}
	
	// removes an object from its last location
	public static void removeObj(WorldObject wo) {
		try {
			wobjects[wo.gX()][wo.gY()][wo.gZ()] = null;
		} catch (Exception e) {
			System.out.println("Exception is:" + e);
		}
	}
		
	public static boolean moveObj(WorldObject wo, int x, int y, int z ) {
		try {
			// Check that space is empty then update mobXYZ and the grid
	    	if ( wobjects[x][y][z] == null ) {
	    		// vacate old spot and claim new one
	    		wobjects[wo.gX()][wo.gY()][wo.gZ()] = null;
	    		wobjects[x][y][z] = wo;
	    		wo.setXYZ(x, y, z);
	    		return true; // this triggers a move animation
	    	} else { 
	    		// eat them
	    		wobjects[wo.gX()][wo.gY()][wo.gZ()].eat(wobjects[x][y][z]);
	    	}
		} catch ( Exception e) { System.out.println("OutOfBounds");	}
    	
		return false; // no move animation
	}
	
	public static void display(GL2 gl) {
		try {
			// Sets the animation frames to the number of mobs and updates screen variables
			if ( animateCount == teamSize * teamsNumber ) {
	        	animateCount = 0;
	    		
	        	BoxData.runProcSql("BEGIN TRIM_EVENT_SCORING; END;");
	        
	        	// for each mob, check what team they're in and sum the experience and food scores
	    		for(int j=0; j<teamsNumber; j++) {
	    			expScore[j] = 0;
	    		    fedScore[j] = 0;
	    			deathScore[j] = 0;
	    		}
	    		
	    		for ( int j=0; j<teamsNumber; j++) {
	    	    	for ( int i=j*teamSize; i<((j+1)*teamSize); i++ ) {
	    	    		expScore[j] = expScore[j] + theMobs[i].getExperience();
	    	    		fedScore[j] = fedScore[j] + theMobs[i].getFedCount();
	    	    		deathScore[j] = deathScore[j] + theMobs[i].getDeathCount();
	    	    	}
	    	    }
	    	
	    		// average and update team values
	    		for ( int j=0; j<teamsNumber; j++) {
	    			expScore[j] = (double) expScore[j] / (double) teamSize;
	    			teamExpScore[j] = teamExpScore[j] + expScore[j];
	    		}
	    				
			}
					
			// Updates one mob per tick
			theMobs[animateCount].updateMob();
			animateCount++;
			
		    DrawBox.DrawBackground(gl,pulse);
		    pulse = pulse + 0.1f;
			
			// go through the grid and draw any world Objects found
			for(int i=0; i<dimZ; i++)
	        	for (int j=0; j<dimY; j++)
	        		for (int k=0; k<dimX; k++) 
	        			if ( wobjects[k][j][i] != null ) wobjects[k][j][i].drawObj(gl);
		
			gameSteps++;
			
			final GLUT glut = new GLUT();
	
			for ( int j=0; j<teamsNumber; j++) {
				gl.glLoadIdentity();
				gl.glTranslated(-1.5,1.6-(0.05*j) + Boxland.sizeAdjustY,-1.6);
		        gl.glColor3d(theMobs[j*teamSize].getBaseR(),theMobs[j*teamSize].getBaseG(),theMobs[j*teamSize].getBaseB());
		 		gl.glRasterPos2i(0, 0);
		 		glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Team# " + (j+1) + " -   Current Food: " + fedScore[j] + "     Deaths: " + deathScore[j] + "     Current Size: " + expScore[j] + "     Avg. Size (x10): " + (int) (teamExpScore[j]/(gameSteps/10)) );
			}
			
			gl.glLoadIdentity();
			gl.glTranslated(-1.5,(1.6-(0.05*teamsNumber)) + Boxland.sizeAdjustY,-1.6);
	 		gl.glColor3d(1,1,1);
	 		gl.glRasterPos2i(0, 0);
	 		glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Game steps: " + gameSteps );
		
		} catch ( Exception e) { System.out.println("Exception is BoxLoc.display: " + e ); }
	}	

	static {
		try {
		
			// world stuff
	        wobjects = new WorldObject[(int) dimX][(int) dimY][(int) dimZ];
	        
	        // set all locations to 0 ( empty )
	        for(int i=0; i<dimX; i++)
	        	for (int j=0; j<dimY; j++)
	        		for (int k=0; k<dimZ; k++) wobjects[i][j][k] = null;
	        
	        //mob setup stuff 
	        for ( int j=0; j<teamsNumber; j++) {
	        	for(int i=teamSize*j; i<teamSize*(j+1); i++) {
	        		if ( j == 0 ) theMobs[i] = new Mob("Red"   , 1.0f, 0.0f, 0.0f, 2, 1);
	        		if ( j == 1 ) theMobs[i] = new Mob("Blue"  , 0.0f, 0.0f, 1.0f, 1, 1);
	        		if ( j == 2 ) theMobs[i] = new Mob("Yellow" , 1.0f, 1.0f, 0.0f, 1, 3);
	        		if ( j == 3 ) theMobs[i] = new Mob("Zombie" , 0.0f, 0.3f, 0.05f, 0, 2);
	        		// make more rows for more teams.. I know....
	
	        		// create mobs in database, set the wobDBID and insert into the matrix
	        		BoxData.runInsertSql( "INSERT INTO OBJ ( OBJ_ID, OBJ_CD, OBJ_TEAM ) SELECT 0, " + theMobs[i].getMobCD() + ", '" + theMobs[i].getWobID() + "' FROM DUAL" );
	        		int MobDBID = BoxData.runSelectINTSql("SELECT MAX(OBJ_ID) FROM OBJ");
	        		theMobs[i].setWobDBID( MobDBID );
	        		insertObj( theMobs[i] );
	        	}
	    	}
	    	
	    	// create the inerts and place.
	        for(int i=0; i<inertsNumber; i++) {
	    		theInerts[i] = new WorldObject("Inert", 0.0f, 0.3f, 0.0f);    		
	    		insertObj(theInerts[i]) ;
	        }
	        
	  		// Initialize scoring
	   		for(int j=0; j<teamsNumber; j++) {
	   			teamExpScore[j] = 0;
	   		}
		
		} catch ( Exception e ) { System.out.println("Exception is BoxLoc.static: " + e ); }
	}
}
