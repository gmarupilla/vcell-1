package cbit.vcell.model;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.gui.graph.*;
import cbit.image.gui.DisplayAdapterService;

import java.beans.*;
import cbit.vcell.model.*;
import cbit.vcell.geometry.SubVolume;
import cbit.vcell.geometry.Geometry;
import java.awt.*;
import java.util.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class SubvolumeLegendShape extends ElipseShape implements PropertyChangeListener {
	SubVolume subvolume = null;
	Geometry geometry = null;
	int radius = 1;

/**
 * SpeciesShape constructor comment.
 * @param label java.lang.String
 * @param graphModel cbit.vcell.graph.GraphModel
 */
public SubvolumeLegendShape(SubVolume subvolume, Geometry geometry, GraphModel graphModel) {
	this(subvolume,geometry,graphModel,1);
}


/**
 * SpeciesShape constructor comment.
 * @param label java.lang.String
 * @param graphModel cbit.vcell.graph.GraphModel
 */
public SubvolumeLegendShape(SubVolume subvolume, Geometry geometry, GraphModel graphModel, int argRadius) {
	super(graphModel);
	this.subvolume = subvolume;
	this.geometry = geometry;
	subvolume.addPropertyChangeListener(this);
	defaultBG = java.awt.Color.red;
	defaultFGselect = java.awt.Color.black;
	backgroundColor = defaultBG;
	this.radius = argRadius;
}


/**
 * This method was created in VisualAge.
 * @return java.awt.Point
 */
public Point getAttachmentLocation(int attachmentType) {
	return new Point(screenPos.x+radius,screenPos.y+radius);
}


/**
 * This method was created in VisualAge.
 * @return java.lang.Object
 */
public Object getModelObject() {
	return subvolume;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 * @param g java.awt.Graphics
 */
public Dimension getPreferedSize(java.awt.Graphics2D g) {
	java.awt.FontMetrics fm = g.getFontMetrics();
	labelSize.height = fm.getMaxAscent() + fm.getMaxDescent();
	labelSize.width = fm.stringWidth(getLabel());
//	preferedSize.height = radius*2 + labelSize.height;
//	preferedSize.width = Math.max(radius*2,labelSize.width);
	preferedSize.height = radius*2;
	preferedSize.width = radius*2 + labelSize.width;
	return preferedSize;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public Point getSeparatorDeepCount() {	
	return new Point(1,1);
}


/**
 * This method was created in VisualAge.
 * @return java.awt.Color
 * @param subVolume cbit.vcell.geometry.SubVolume
 */
private java.awt.Color getSubvolumeColor() {
	java.awt.image.ColorModel colorModel = DisplayAdapterService.getHandleColorMap();
	int handle = subvolume.getHandle();
	return new java.awt.Color(colorModel.getRGB(handle));
}


/**
 * This method was created by a SmartGuide.
 * @return int
 * @param g java.awt.Graphics
 */
public void layout() {

	//
	// position label
	//
	labelPos.x = 2*radius + 5;
	labelPos.y = labelSize.height;
}


/**
 * This method was created by a SmartGuide.
 * @param g java.awt.Graphics
 */
public void paint ( java.awt.Graphics2D g, int parentOffsetX, int parentOffsetY ) {

   int absPosX = screenPos.x + parentOffsetX;
   int absPosY = screenPos.y + parentOffsetY;
	//
	// draw elipse
	//
	java.awt.Color fillColor = getSubvolumeColor();
	g.setColor(fillColor);
	g.fill3DRect(absPosX+1,absPosY+1,2*radius-1,2*radius-1,true);
	g.setColor(forgroundColor);
	g.draw3DRect(absPosX,absPosY,2*radius,2*radius,true);
	
//	g.drawRect(screenPos.x+parentOffsetX,screenPos.y+parentOffsetY,screenSize.width,screenSize.height);
	//
	// draw label
	//
	java.awt.FontMetrics fm = g.getFontMetrics();
	int textX = labelPos.x + absPosX;
	int textY = labelPos.y + absPosY;
	g.setColor(forgroundColor);
	if (getLabel()!=null && getLabel().length()>0){
		g.drawString(getLabel(),textX,textY);
	}
	return;
}


/**
 * Insert the method's description here.
 * Creation date: (6/5/00 10:50:17 PM)
 * @param event java.beans.PropertyChangeEvent
 */
public void propertyChange(PropertyChangeEvent event) {
	if (event.getSource() == (SubVolume)getModelObject() && event.getPropertyName().equals("name")){
		refreshLabel();
		graphModel.notifyChangeEvent();
	}
}


/**
 * This method was created in VisualAge.
 */
public void refreshLabel() {
	setLabel(subvolume.getName());
}


/**
 * This method was created by a SmartGuide.
 * @param newSize java.awt.Dimension
 */
public void resize(Graphics2D g, Dimension newSize) {
	return;
}
}