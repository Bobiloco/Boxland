package boxland;

import javax.media.opengl.GL2;

public class RedMob extends Mob {

	public RedMob(int i ) {
		super(i);
	}

	public void eat(WorldObject woEaten) {
		if (woEaten.getID() != "Red") {
        	if ( explodeCount == 0 ) explodeCount = 1; 
			experience++;
        	fedCount = fedCount + 100;
			woEaten.killed();	
		}
	}

	public String getID() {
		return "Red";
	}
	
	public void drawObj(GL2 gl2) { 
		
		drawActions();
		
		drawObjColour(gl2, 1.0f, 0.0f, 0.0f); 
	}

}
