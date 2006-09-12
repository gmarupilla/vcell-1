package cbit.vcell.model.render;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.gui.graph.*;
import cbit.vcell.model.*;

import java.awt.*;
import java.awt.geom.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class ReactionStepShape extends ElipseShape {
	ReactionStep reactionStep = null;
	int radius = 5;
	Area icon = null;
	private static boolean bDisplayLabels = false;

/**
 * SpeciesShape constructor comment.
 * @param label java.lang.String
 * @param graphModel cbit.vcell.graph.GraphModel
 */
public ReactionStepShape(ReactionStep reactionStep, GraphModel graphModel) {
	super(graphModel);
	this.reactionStep = reactionStep;
	defaultBG = java.awt.Color.yellow;
	defaultFGselect = java.awt.Color.black;
	backgroundColor = defaultBG;
}


/**
 * Insert the method's description here.
 * Creation date: (9/6/2002 10:19:18 PM)
 * @return boolean
 */
public static boolean getDisplayLabels() {
	return bDisplayLabels;
}


/**
 * This method was created in VisualAge.
 * @return java.lang.Object
 */
public Object getModelObject() {
	return reactionStep;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 * @param g java.awt.Graphics
 */
public Dimension getPreferedSize(java.awt.Graphics2D g) {

	preferedSize.width = 20;
	preferedSize.height = 16;

	if(getLabel() != null && getLabel().length() > 0){
		labelSize.width = g.getFontMetrics().stringWidth(getLabel());
		labelSize.height = g.getFontMetrics().getMaxAscent()+g.getFontMetrics().getMaxDescent();
	}
	return preferedSize;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.model.Species
 */
public ReactionStep getReactionStep() {
	return reactionStep;
}

/**
 * This method was created by a SmartGuide.
 * @return int
 */
public Point getSeparatorDeepCount() {	
	return new Point(0,0);
}


/**
 * This method was created by a SmartGuide.
 * @return int
 * @param g java.awt.Graphics
 */
public final void layout() {

//	if (screenSize.width<labelSize.width ||
//		 screenSize.height<labelSize.height){
//		 throw new Exception("screen size smaller than label");
//	} 
	//
	// this is like a row/column layout  (1 column)
	//
	int centerX = screenSize.width/2;
	
	//
	// position label
	//
	labelPos.x = centerX - labelSize.width/2;
	labelPos.y = 0;
}


/**
 * This method was created by a SmartGuide.
 * @param newSize java.awt.Dimension
 */
public final void resize(Graphics2D g, Dimension newSize) {
	return;
}


/**
 * Insert the method's description here.
 * Creation date: (9/6/2002 10:17:47 PM)
 * @param bDisplayLabels boolean
 */
public static void setDisplayLabels(boolean argDisplayLabels) {
	bDisplayLabels = argDisplayLabels;
}
}