package boxland;

import java.util.Random;
import javax.media.opengl.GL2;

public class BoxLoc {

	public static int animateCount = 0;
	
	public static final int sizeMultiple = 1;
	
	private static final int mobsNumber = 20 * sizeMultiple;
	private static final int inertsNumber = 10 * sizeMultiple;

	public static final float dimX = sizeMultiple * 12; // 16
	public static final float dimY = sizeMultiple * 8;  // 8
	public static final float dimZ = sizeMultiple * 10; // 10
		
	public static float startX = -dimX/2.0f;
	public static float startY = -dimY/2.0f;
	public static float startZ = -dimZ-(dimX+dimY)/2.0f;
	
	private static float pulse = 0.0f;
    
	private static WorldObject[][][] wobjects;

	private static Mob theMobs[] = new Mob[mobsNumber];
	private static WorldObject theInerts[] = new WorldObject[inertsNumber];
	
	// used to identify other elements in the grid by their string
	public static String getWobID(int x, int y, int z) {
		// inbounds
		if ( !((x < 0)  || (y < 0) || (z < 0) || (x > dimX-1) || (y > dimY-1) || (z > dimZ-1) )) { 

			if ( wobjects[x][y][z] != null ) 
				return wobjects[x][y][z].getWobID();
		}
		return "Vacant";
	}

	// Check for along borders, later for climbable surfaces
	public static boolean checkHasGround(int x, int y, int z) {
		// inbounds
		if ( !((x < 0)  || (y < 0) || (z < 0) || (x > dimX-1) || (y > dimY-1) || (z > dimZ-1) )) {

			if ( (x == 0) || (y == 0) || (z == 0) || (x == dimX-1) || (y == dimY-1) || (z == dimZ-1) ) 
				return true;
		}
		return false;
	}
	
	// Check for the walls
	public static boolean checkIsGround(int x, int y, int z) {
		// inbounds
		if ( !((x < 0)  || (y < 0) || (z < 0) || (x > dimX-1) || (y > dimY-1) || (z > dimZ-1) )) {

			if ( (x == -1) || (y == -1) || (z == -1) || (x == dimX) || (y == dimY) || (z == dimZ) ) 
				return true;
		}
		return false;
	}
	
	public static WorldObject getObj(int x, int y, int z) {
		// inbounds
		if ( !((x < 0)  || (y < 0) || (z < 0) || (x > dimX-1) || (y > dimY-1) || (z > dimZ-1) )) { 
			return wobjects[x][y][z];
		}
		return null;
	}
	
	public static boolean insertObj(WorldObject wo) {

		Random random = new Random();
		
		// try 100 times to places it randomly
		for (int i=0; i<100; i++) {
			
			int randX = random.nextInt((int) dimX);
			int randY = random.nextInt((int) dimY);
			int randZ = random.nextInt((int) dimZ);
		
			//place it against some edge by min/maxing a random coordinate
			int rand6 = random.nextInt(6);

			if ( rand6 == 0 ) randX = 0;
			if ( rand6 == 1 ) randY = 0;
			if ( rand6 == 2 ) randZ = 0;
			if ( rand6 == 3 ) randX = (int) (dimX-1);
			if ( rand6 == 4 ) randY = (int) (dimY-1);
			if ( rand6 == 5 ) randZ = (int) (dimZ-1);
			
			if ( wobjects[randX][randY][randZ] == null ) { 
				wobjects[randX][randY][randZ] = wo; 
				wo.updateXYZ(randX, randY, randZ);
				return true;
			}
		}
		return false;
	}
	
	public static void removeObj(WorldObject wo) {
		// removes an object from its last location
		wobjects[wo.gX()][wo.gY()][wo.gZ()] = null;
	}
	
	
	public static boolean moveObj(WorldObject wo, int x, int y, int z ) {

		// inbounds
		if ( !((x < 0)  || (y < 0) || (z < 0) || (x > dimX-1) || (y > dimY-1) || (z > dimZ-1) )) { 

			// Check that space is empty then update mobXYZ and the grid
	    	if ( wobjects[x][y][z] == null ) {

	    		// vacate old spot and claim new one
	    		wobjects[wo.gX()][wo.gY()][wo.gZ()] = null;
	    		wobjects[x][y][z] = wo;
	    		wo.updateXYZ(x, y, z);
	    		
	    		return true; // this triggers a move animation
	    		
	    	} else { 
	    		
	    		// eat them
	    		wobjects[wo.gX()][wo.gY()][wo.gZ()].eat(wobjects[x][y][z]);
	    		
	    	}
		}
		return false; // no move animation
	}
	
	public void display(GL2 gl) {
		
        if ( animateCount == mobsNumber ) {
        	animateCount = 0;
    		Boxland.runProcSql("BEGIN TRIM_EVENT_SCORING; END;");
        }
				
		theMobs[animateCount].updateMob();

		animateCount++;
		
	    DrawScene.DrawBackground(gl,pulse);

	    pulse = pulse + 0.1f;
		
		// go through the grid and draw any world Objects found
		for(int i=0; i<dimZ; i++)
        	for (int j=0; j<dimY; j++)
        		for (int k=0; k<dimX; k++) 
        			if ( wobjects[k][j][i] != null ) wobjects[k][j][i].drawObj(gl);
	}	

	public BoxLoc() {
		
		// world stuff
        wobjects = new WorldObject[(int) dimX][(int) dimY][(int) dimZ];
        
        // set all locations to 0 ( empty )
        for(int i=0; i<dimX; i++)
        	for (int j=0; j<dimY; j++)
        		for (int k=0; k<dimZ; k++) wobjects[i][j][k] = null;
        
        //mob setup stuff 
    	for(int i=0; i<mobsNumber; i++) { 
    		if ( i<mobsNumber/2 ) {
    			theMobs[i] = new Mob("Red", 1.0f, 0.0f, 0.0f); 
    		} else {
    			theMobs[i] = new Mob("Blue", 0.0f, 0.0f, 1.0f);
    		}
    		
    		// Insert the object ID into the database
    		Boxland.runInsertSql( "INSERT INTO OBJ ( OBJ_ID, OBJ_CD, OBJ_TEAM ) SELECT 0, " + i + ", '" + theMobs[i].getWobID() + "' FROM DUAL" );
    		int MobDBID = Boxland.runSelectINTSql("SELECT MAX(OBJ_ID) FROM OBJ");
    		theMobs[i].setWobDBID( MobDBID );
    		
    		// Insert the object pointer into the location matrix
    		insertObj( theMobs[i] );
    		
    	}
    	
    	for(int i=0; i<inertsNumber; i++) {
    	// create the inerts and place. If xyz is taken they will be moved and their object's xyx updated
    		theInerts[i] = new WorldObject("Inert", 0.0f, 1.0f, 0.0f);    		
    		insertObj(theInerts[i]) ;
   	    }
	}
}
