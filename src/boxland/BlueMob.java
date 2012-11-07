package boxland;

import javax.media.opengl.GL2;

public class BlueMob extends Mob {

	public BlueMob(int i ) {
		super(i);
	}

	public void eat(WorldObject woEaten) {
		if (woEaten.getID() != "Blue") {
        	if ( explodeCount == 0 ) explodeCount = 1;
        	experience++;
        	fedCount = fedCount + 100;
        	woEaten.killed();	
		}
	}

	public String getID() {
		return "Blue";
	}
	
	public void drawObj(GL2 gl2) { 
		
		drawActions();
		
		drawObjColour(gl2,0.0f,0.0f,1.0f); 
	}

}
