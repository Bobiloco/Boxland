package boxland;

import javax.media.opengl.GL2;

public class Inert extends WorldObject {

    public String getID() {
		return "Inert";
	}
		
    public void killed() {
		growCount = -10;
    	TerrLoc.removeObj(this);
		TerrLoc.insertObj(this);
	}
    
    public void drawActions() {
		growSize();
		drawSize = cubeSize + (float) growCount * growSize;
    }
    
    public void drawObj (GL2 gl2) {
    	
    	drawActions();
    	
    	this.drawObjColour( gl2, 0.0f, 1.0f, 0.0f);
	
    }
}