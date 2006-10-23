package org.vcell.ncbc.physics.component.gui;

import cbit.gui.graph.GraphModel;
/**
 * Insert the type's description here.
 * Creation date: (7/8/2003 11:58:47 AM)
 * @author: Jim Schaff
 */
public class LocationNode extends PhysicalModelGraphNode {
	private org.vcell.ncbc.physics.component.Location location = null;

	private static java.awt.Polygon triangle2D = null;
	private static java.awt.Polygon triangle3D_1 = null;
	private static java.awt.Polygon triangle3D_2 = null;
/**
 * ConstraintVarNode constructor comment.
 * @param node cbit.vcell.mapping.potential.Node
 * @param graphModel cbit.vcell.graph.GraphModel
 */
public LocationNode(org.vcell.ncbc.physics.component.Location argLocation, GraphModel graphModel) {
	super(graphModel);
	this.location = argLocation;
	if (argLocation instanceof org.vcell.ncbc.physics.component.ResolvedVolumeLocation){
		defaultBG = java.awt.Color.yellow;
	}else if (argLocation instanceof org.vcell.ncbc.physics.component.UnresolvedVolumeLocation){
		defaultBG = java.awt.Color.white;
	}else if (argLocation instanceof org.vcell.ncbc.physics.component.ResolvedSurfaceLocation){
		defaultBG = java.awt.Color.lightGray;
	}else if (argLocation instanceof org.vcell.ncbc.physics.component.UnresolvedSurfaceLocation){
		defaultBG = java.awt.Color.white;
	}
	defaultFGselect = java.awt.Color.black;
	backgroundColor = defaultBG;
	refreshLabel();
}
/**
 * This method was created in VisualAge.
 * @return java.lang.Object
 */
public org.vcell.ncbc.physics.component.Location getLocation_model() {
	return location;
}
/**
 * This method was created in VisualAge.
 * @return java.lang.Object
 */
public java.lang.Object getModelObject() {
	return location;
}
/**
 * Insert the method's description here.
 * Creation date: (1/15/2004 9:33:37 PM)
 * @return int
 */
int getRadius() {
	return 20;
}
/**
 * This method was created by a SmartGuide.
 * @param g java.awt.Graphics
 */
public void paint(java.awt.Graphics2D g, int parentOriginX, int parentOriginY) {
	

	int radius = getRadius();
	if (triangle2D==null){
		triangle2D = new java.awt.Polygon(new int[] { radius*4/5, (2*radius*4/5), 0}, new int[] { 0, (radius*9/5), (radius*6/5) }, 3);
	}
	if (triangle3D_1==null){
		triangle3D_1 = new java.awt.Polygon(new int[] { radius*3/5, (2*radius*4/5), 0}, new int[] { 0, (radius*9/5), (radius*9/5) }, 3);
	}
	if (triangle3D_2==null){
		triangle3D_2 = new java.awt.Polygon(new int[] { radius*3/5,  0, 0}, new int[] { 0, (radius*9/5), (radius*4/5) }, 3);
	}
	
	int absPosX = screenPos.x + parentOriginX;
	int absPosY = screenPos.y + parentOriginY;
	g.translate(absPosX,absPosY);
	//
	boolean isBound = false;
	if (getLocation_model() instanceof org.vcell.ncbc.physics.component.SurfaceLocation){
		g.setColor(backgroundColor);
		g.fill(triangle2D);
		g.setColor(forgroundColor);
		g.draw(triangle2D);
		//g.drawOval(0,0,2*radius,2*radius);
	}else if (getLocation_model() instanceof org.vcell.ncbc.physics.component.VolumeLocation){
		g.setColor(backgroundColor);
		g.fill(triangle3D_1);
		g.setColor(backgroundColor.darker());
		g.fill(triangle3D_2);
		g.setColor(backgroundColor.darker().darker());
		g.drawLine((2*radius*4/5),(radius*9/5),0,(radius*4/5));
		g.setColor(forgroundColor);
		g.draw(triangle3D_1);
		g.draw(triangle3D_2);
		//g.drawOval(0,0,2*radius,2*radius);
	}else{
		//
		// draw elipse
		//
		g.setColor(backgroundColor);
		g.fillOval(1,1,2*radius-1,2*radius-1);
		g.setColor(forgroundColor);
		g.drawOval(0,0,2*radius,2*radius);
	}
	//
	// draw label
	//
//	if (isSelected()){
		java.awt.FontMetrics fm = g.getFontMetrics();
		int textX = labelPos.x;
		int textY = labelPos.y-2;
		g.setColor(forgroundColor);
		if (getLabel()!=null && getLabel().length()>0){
			g.drawString(getLabel(),textX,textY);
		}
//	}
	g.translate(-absPosX,-absPosY);
	return;
}
/**
 * This method was created in VisualAge.
 */
public void refreshLabel() {
	setLabel(location.getName());
}
}
