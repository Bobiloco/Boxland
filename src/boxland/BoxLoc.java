package boxland;

import java.util.Random;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public class BoxLoc {

	// config variables
	public static final int teamsNumber = 4;
	public static final int teamSize = 10;
	private static final int inertsNumber = 40;
	public static final float dimX = 20; 
	public static final float dimY = 10; 
	public static final float dimZ = 10; 
	public static final float startX = -dimX/2.0f;
	public static final float startY = ( -dimY/2.0f );
	public static final float startZ = -dimZ-(dimX+dimY)/2.0f;
	
	// Counters
	private static int gameSteps = 0;
	public static int animateCount = 0;
	private static float pulse = 0.0f;
	
	// Memory container arrays
	private static float teamExpScore[] = new float[teamsNumber];
	private static float teamFedScore[] = new float[teamsNumber];
	private static WorldObject[][][] wobjects;
	private static Mob theMobs[] = new Mob[teamsNumber * teamSize];
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
			//int rand6 = 1;
			
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
	
	// removes an object from its last location
	public static void removeObj(WorldObject wo) {
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
		
		// Sets the animation frames to the number of mobs
		if ( animateCount == teamSize * teamsNumber ) {
        	animateCount = 0;
    		Boxland.runProcSql("BEGIN TRIM_EVENT_SCORING; END;");
        }
				
		// Updates one mob per tick
		theMobs[animateCount].updateMob();
		animateCount++;
		
	    DrawScene.DrawBackground(gl,pulse);

	    pulse = pulse + 0.1f;
		
		// go through the grid and draw any world Objects found
		for(int i=0; i<dimZ; i++)
        	for (int j=0; j<dimY; j++)
        		for (int k=0; k<dimX; k++) 
        			if ( wobjects[k][j][i] != null ) wobjects[k][j][i].drawObj(gl);
	

        // for each mob, check what team they're in and sum the experience and food scores
		float expScore[] = new float[teamsNumber];
		float fedScore[] = new float[teamsNumber];
		
		for(int j=0; j<teamsNumber; j++) {
			expScore[j] = 0;
		    fedScore[j] = 0;
		}
		
		for ( int j=0; j<teamsNumber; j++) {
	    	for ( int i=j*teamSize; i<((j+1)*teamSize); i++ ) {
	    		expScore[j] = expScore[j] + theMobs[i].experience;
		    	fedScore[j] = fedScore[j] + theMobs[i].fedCount;
	    	}
	    }
	
		// average all the values
		for ( int j=0; j<teamsNumber; j++) {
			expScore[j] = expScore[j] /  teamSize;
			fedScore[j] = Math.round( fedScore[j] / teamSize ) - 50;
		}
				
		// update the team scores
		for ( int j=0; j<teamsNumber; j++) {
			teamExpScore[j] = teamExpScore[j] + expScore[j];
			teamFedScore[j] = teamFedScore[j] + fedScore[j];
		}
	
		gameSteps++;
		
		final GLUT glut = new GLUT();

		for ( int j=0; j<teamsNumber; j++) {
			gl.glLoadIdentity();
			gl.glTranslatef(-1.5f,1.6f-(0.05f*(float)j) + Boxland.sizeAdjustY,-1.6f);
	        gl.glColor3f(theMobs[j*teamSize].colourR,theMobs[j*teamSize].colourG,theMobs[j*teamSize].colourB);
	 		gl.glRasterPos2i(0, 0);
	 		glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Team " + (j+1) + " - Fed: " + fedScore[j] + "    Size: " + expScore[j] + "            Food: " + Math.round(teamFedScore[j]/(gameSteps/10)) + "    Avg. Kill Ratio: " + (int) (teamExpScore[j]/(gameSteps/10)));
		}
		
		gl.glLoadIdentity();
		gl.glTranslatef(-1.5f,(1.6f-(0.05f*(float)teamsNumber)) + Boxland.sizeAdjustY,-1.6f);
 		gl.glColor3f(1.0f,1.0f,1.0f);
 		gl.glRasterPos2i(0, 0);
 		glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Game steps: " + gameSteps );

	}	

	public BoxLoc() {
		
		// Initialize scoring
		for(int j=0; j<teamsNumber; j++) {
			teamExpScore[j] = 0;
		    teamFedScore[j] = 0;
		}
		
		// world stuff
        wobjects = new WorldObject[(int) dimX][(int) dimY][(int) dimZ];
        
        // set all locations to 0 ( empty )
        for(int i=0; i<dimX; i++)
        	for (int j=0; j<dimY; j++)
        		for (int k=0; k<dimZ; k++) wobjects[i][j][k] = null;
        
        //mob setup stuff 
        for ( int j=0; j<teamsNumber; j++) {
        	for(int i=teamSize*j; i<teamSize*(j+1); i++) {
        		if ( j == 0 ) theMobs[i] = new Mob("Blue", 0.0f, 0.0f, 1.0f, 3);
        		if ( j == 1 ) theMobs[i] = new Mob("Red",  1.0f, 0.0f, 0.0f, 3);
        		if ( j == 2 ) theMobs[i] = new Mob("Yellow", 1.0f, 1.0f, 0.0f, 3);
        		if ( j == 3 ) theMobs[i] = new Mob("White", 1.0f, 1.0f, 1.0f, 3);
        		// make more rows for more teams.. I know....

        		// create mobs in database, set the wobDBID and insert into the matrix
        		Boxland.runInsertSql( "INSERT INTO OBJ ( OBJ_ID, OBJ_CD, OBJ_TEAM ) SELECT 0, " + theMobs[i].getMobCD() + ", '" + theMobs[i].getWobID() + "' FROM DUAL" );
        		int MobDBID = Boxland.runSelectINTSql("SELECT MAX(OBJ_ID) FROM OBJ");
        		theMobs[i].setWobDBID( MobDBID );
        		insertObj( theMobs[i] );
        	}
    	}
    	
    	// create the inerts and place.
        for(int i=0; i<inertsNumber; i++) {
    		theInerts[i] = new WorldObject("Inert", 0.0f, 1.0f, 0.0f);    		
    		insertObj(theInerts[i]) ;
   	    }
	}
}
