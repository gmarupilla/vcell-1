package cbit.gui.graph;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.util.*;

import org.vcell.util.BeanUtils;

import cbit.gui.graph.GraphListener;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class GraphModel {
	private Vector<Shape> shapeList = new Vector<Shape>();
	protected transient cbit.gui.graph.GraphListener aGraphListener = null;
	protected transient java.beans.PropertyChangeSupport propertyChange;
	private int fieldZoomPercent = 100;
	private boolean fieldResizable = true;

	/**
	 * Insert the method's description here.
	 * Creation date: (5/13/2003 3:09:07 PM)
	 */
	public GraphModel() {}


	/**
	 * 
	 * @param newListener cbit.vcell.graph.GraphListener
	 */
	public void addGraphListener(GraphListener newListener) {
		aGraphListener = GraphEventMulticaster.add(aGraphListener, newListener);
		return;
	}


	/**
	 * The addPropertyChangeListener method was generated to support the propertyChange field.
	 */
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		getPropertyChange().addPropertyChangeListener(listener);
	}


	/**
	 * The addPropertyChangeListener method was generated to support the propertyChange field.
	 */
	public synchronized void addPropertyChangeListener(java.lang.String propertyName, java.beans.PropertyChangeListener listener) {
		getPropertyChange().addPropertyChangeListener(propertyName, listener);
	}


	/**
	 * This method was created in VisualAge.
	 * @param shape cbit.vcell.graph.Shape
	 */
	protected final void addShape(Shape shape) {
		shapeList.addElement(shape);
	}


	/**
	 * This method was created in VisualAge.
	 */
	protected final void clearAllShapes() {
		shapeList.removeAllElements();
	}


	/**
	 * This method was created by a SmartGuide.
	 * @return cbit.vcell.model.Feature
	 */
	public void clearSelection() {
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs.isSelected()){
				fs.unselect();
			} 
		}
		fireGraphChanged(new GraphEvent(this));
	}


	/**
	 * This method was created by a SmartGuide.
	 * @param shape cbit.vcell.graph.Shape
	 */
	public void deselect(Shape shape) {
		if (shape.isSelected()){
			shape.unselect();
			fireGraphChanged(new GraphEvent(this));
		}	
	}


	/**
	 * Method to support listener events.
	 * @param event cbit.vcell.graph.GraphEvent
	 */
	public void fireGraphChanged() {
		fireGraphChanged(new GraphEvent(this));
	}


	/**
	 * Method to support listener events.
	 * @param event cbit.vcell.graph.GraphEvent
	 */
	protected void fireGraphChanged(GraphEvent event) {
		if (aGraphListener == null) {
			return;
		};
		aGraphListener.graphChanged(event);
	}


	/**
	 * The firePropertyChange method was generated to support the propertyChange field.
	 */
	public void firePropertyChange(java.beans.PropertyChangeEvent evt) {
		getPropertyChange().firePropertyChange(evt);
	}


	/**
	 * The firePropertyChange method was generated to support the propertyChange field.
	 */
	public void firePropertyChange(java.lang.String propertyName, int oldValue, int newValue) {
		getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
	}


	/**
	 * The firePropertyChange method was generated to support the propertyChange field.
	 */
	public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
		getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
	}


	/**
	 * The firePropertyChange method was generated to support the propertyChange field.
	 */
	public void firePropertyChange(java.lang.String propertyName, boolean oldValue, boolean newValue) {
		getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
	}


	/**
	 * This method was created in VisualAge.
	 * @return cbit.vcell.graph.ElipseShape
	 */
	ElipseShape firstNode() {
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs instanceof ElipseShape){
				return (ElipseShape)fs;
			}
		}
		return null;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @return cbit.vcell.model.Feature
	 */
	public Shape[] getAllSelectedShapes() {
		Vector<Shape> selectedShapeList = new Vector<Shape>();
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs.isSelected()){
				selectedShapeList.add(fs);
			} 
		}
		Shape selectedShapes[] = new Shape[selectedShapeList.size()];
		selectedShapeList.copyInto(selectedShapes);
		return selectedShapes;
	}


	/**
	 * This method was created in VisualAge.
	 * @return int
	 * @param shape cbit.vcell.graph.ElipseShape
	 */
	int getIndexFromNode(ElipseShape shape) {
		return shapeList.indexOf(shape);
	}


	/**
	 * This method was created in VisualAge.
	 * @return int
	 * @param shape cbit.vcell.graph.ElipseShape
	 */
	ElipseShape getNodeFromIndex(int index) {
		if (index<0 || index>=shapeList.size()){
			return null;
		}
		Shape shape = shapeList.elementAt(index);
		if (shape instanceof ElipseShape){
			return (ElipseShape)shape;
		}else{
			return null;
		}
	}


	/**
	 * This method was created in VisualAge.
	 * @return int
	 */
	public int getNumShapes() {
		return shapeList.size();
	}


	/**
	 * Insert the method's description here.
	 * Creation date: (9/5/2002 10:21:31 PM)
	 * @return java.awt.Dimension
	 * @param g java.awt.Graphics2D
	 */
	public java.awt.Dimension getPreferedSize(java.awt.Graphics2D g) {
		java.awt.Dimension dim = null;
		if (getTopShape()==null){
			dim = new java.awt.Dimension(1,1);
		}else{
			java.awt.geom.AffineTransform oldTransform = g.getTransform();
			g.scale(fieldZoomPercent/100.0,fieldZoomPercent/100.0);
			java.awt.Dimension oldDim = getTopShape().getPreferedSize(g);
			g.setTransform(oldTransform);
			double newWidth = oldDim.width*(fieldZoomPercent/100.0);
			double newHeight = oldDim.height*(fieldZoomPercent/100.0);
			dim = new java.awt.Dimension((int)newWidth,(int)newHeight);
		}
		return dim;
	}


	/**
	 * Accessor for the propertyChange field.
	 */
	protected java.beans.PropertyChangeSupport getPropertyChange() {
		if (propertyChange == null) {
			propertyChange = new java.beans.PropertyChangeSupport(this);
		};
		return propertyChange;
	}


	/**
	 * Gets the resizable property (boolean) value.
	 * @return The resizable property value.
	 * @see #setResizable
	 */
	public boolean getResizable() {
		return fieldResizable;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @return cbit.vcell.model.Feature
	 */
	public Shape getSelectedShape() {
		Shape[] theSelectedShapes = getSelectedShapes();
		if(theSelectedShapes != null && theSelectedShapes.length > 0){
			return theSelectedShapes[0];
		}
		//for (int i=0;i<shapeList.size();i++){
		//Shape fs = (Shape)shapeList.elementAt(i);
		//if (fs.isSelected()){
		//return fs;
		//} 
		//}	
		return null;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @return cbit.vcell.model.Feature
	 */
	public Shape[] getSelectedShapes() {
		Vector<Shape> theSelectedShapesV = new Vector<Shape>();
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs.isSelected()){
				theSelectedShapesV.add(fs);
			} 
		}

		if (theSelectedShapesV.size() > 0){
			Shape[] theSelectedShapeArr = new Shape[theSelectedShapesV.size()];
			theSelectedShapesV.copyInto(theSelectedShapeArr);
			return theSelectedShapeArr;
		}

		return null;
	}


	/**
	 * This method was created in VisualAge.
	 * @return cbit.vcell.graph.Shape
	 * @param obj java.lang.Object
	 */
	public Shape getShapeFromLabel(String label) {
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (label.equals(fs.getLabel())){
				return fs;
			}
		}
		return null;
	}


	/**
	 * This method was created in VisualAge.
	 * @return cbit.vcell.graph.Shape
	 * @param obj java.lang.Object
	 */
	public Shape getShapeFromModelObject(Object obj) {
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs.getModelObject()==obj){
				return fs;
			}
		}
		return null;
	}


	/**
	 * This method was created in VisualAge.
	 * @return java.util.Enumeration
	 */
	public Enumeration<Shape> getShapes() {
		return shapeList.elements();
	}


	/**
	 * This method was created by a SmartGuide.
	 * @param g java.awt.Graphics
	 */
	public Shape getTopShape() {
		if (shapeList==null){
			return null;
		}	
		int numShapes = shapeList.size();
		if (numShapes == 0){
			return null;
		}	
		Shape topShape = null;
		for (int i=0;i<numShapes;i++){
			Shape fs = shapeList.elementAt(i);
			if (fs.getParent()==null){
				if (topShape!=null){
					showShapeHierarchyBottomUp();
					showShapeHierarchyTopDown();
					throw new RuntimeException("ERROR: too many top level shapes, at least "+topShape+" and "+fs);
				}	
				topShape = fs;
			} 
		}
		if (topShape==null){
			throw new RuntimeException("there are no top shapes");
		}
		return topShape;
	}


	/**
	 * Gets the zoomPercent property (int) value.
	 * @return The zoomPercent property value.
	 * @see #setZoomPercent
	 */
	public int getZoomPercent() {
		return fieldZoomPercent;
	}


	/**
	 * Called whenever the part throws an exception.
	 * @param exception java.lang.Throwable
	 */
	protected void handleException(Throwable exception) {

		/* Uncomment the following lines to print uncaught exceptions to stdout */
		System.out.println("--------- UNCAUGHT EXCEPTION --------- in GraphModel");
		exception.printStackTrace(System.out);
	}

	/**
	 * This method was created in VisualAge.
	 * @return boolean
	 * @param node1 cbit.vcell.graph.ElipseShape
	 * @param node2 cbit.vcell.graph.ElipseShape
	 */
	boolean hasEdge(ElipseShape node1, ElipseShape node2) {
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs instanceof EdgeShape){
				EdgeShape edge = (EdgeShape)fs;
				if ((edge.startShape==node1 && edge.endShape==node2) ||
						(edge.startShape==node2 && edge.endShape==node1)){
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * The hasListeners method was generated to support the propertyChange field.
	 */
	public synchronized boolean hasListeners(java.lang.String propertyName) {
		return getPropertyChange().hasListeners(propertyName);
	}


	/**
	 * This method was created in VisualAge.
	 * @return cbit.vcell.graph.ElipseShape
	 * @param shape cbit.vcell.graph.ElipseShape
	 */
	ElipseShape nextNode(ElipseShape shape) {
		int startIndex = shapeList.indexOf(shape)+1;
		if (startIndex >= shapeList.size()){
			return null;
		}
		for (int i=startIndex;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs instanceof ElipseShape){
				return (ElipseShape)fs;
			}
		}
		return null;
	}


	/**
	 * This method was created by a SmartGuide.
	 */
	public void notifyChangeEvent() {
		fireGraphChanged(new GraphEvent(this));
	}


	/**
	 * This method was created in VisualAge.
	 * @return int
	 */
	int numberOfEdges() {
		int nodeCount = 0;
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs instanceof EdgeShape){
				nodeCount++;
			}
		}
		return nodeCount;
	}


	/**
	 * This method was created in VisualAge.
	 * @return int
	 */
	int numberOfNodes() {
		int nodeCount = 0;
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs instanceof ElipseShape){
				nodeCount++;
			}
		}
		return nodeCount;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @param g java.awt.Graphics
	 */
	public void paint(java.awt.Graphics2D g, GraphPane canvas) {
		//	showShapeHierarchyBottomUp();
		//	showShapeHierarchyTopDown();
		try {
			if (g == null){
				System.out.println("graphics is null");
				return;
			}
			if (canvas == null){
				System.out.println("canvas is null");
				//return;
			}
			Shape topShape = getTopShape();
			if (shapeList==null && canvas != null){
				canvas.clear(g);
				return;
			}else if (topShape!=null){
				java.awt.geom.AffineTransform oldTransform = g.getTransform();
				g.scale(fieldZoomPercent/100.0,fieldZoomPercent/100.0);
				topShape.paint(g,0,0);
				g.setTransform(oldTransform);
			}	
		}catch (Exception e){
			g.setColor(java.awt.Color.red);
			g.drawString("EXCEPTION IN GraphModel.paint(): "+e.getMessage(),20,20);
			e.printStackTrace(System.out);
			return;
		}	
	}


	/**
	 * Finds the first edge that can be picked (no heirarchy)
	 * @return cbit.vcell.graph.Shape
	 * @param x int
	 * @param y int
	 */
	public Shape pickEdgeWorld(java.awt.Point point) {
		for (int i=0;i<shapeList.size();i++){
			Shape fs = shapeList.elementAt(i);
			if (fs instanceof EdgeShape){
				Shape pickedShape = fs.pick(point);
				if (pickedShape == fs){
					return pickedShape;
				}
			}
		}
		return null;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @return cbit.vcell.graph.Shape
	 * @param x int
	 * @param y int
	 */
	public Shape pickWorld(java.awt.Point argPoint) {
		Shape topShape = getTopShape();
		if (topShape==null) return null;	
		return topShape.pick(argPoint);
	}


	/**
	 * This method was created by a SmartGuide.
	 * @return cbit.vcell.graph.Shape
	 * @param x int
	 * @param y int
	 */
	public Shape[] pickWorld(java.awt.Rectangle argRectWorld) {
		Shape topShape = getTopShape();
		if (topShape==null) return null;
		Vector<Shape> pickedList = new Vector<Shape>();
		for (int i = 0; i < shapeList.size(); i++){
			Shape shape = shapeList.elementAt(i);
			if (argRectWorld.contains(shape.getAbsLocation())){
				pickedList.add(shape);
			}
		}
		return (Shape[])BeanUtils.getArray(pickedList,Shape.class);
	}


	/**
	 * Insert the method's description here.
	 * Creation date: (3/22/01 2:19:00 PM)
	 */
	public abstract void refreshAll();


	/**
	 * 
	 * @param newListener cbit.vcell.graph.GraphListener
	 */
	public void removeGraphListener(GraphListener newListener) {
		aGraphListener = GraphEventMulticaster.remove(aGraphListener, newListener);
		return;
	}


	/**
	 * The removePropertyChangeListener method was generated to support the propertyChange field.
	 */
	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
		getPropertyChange().removePropertyChangeListener(listener);
	}


	/**
	 * The removePropertyChangeListener method was generated to support the propertyChange field.
	 */
	public synchronized void removePropertyChangeListener(java.lang.String propertyName, java.beans.PropertyChangeListener listener) {
		getPropertyChange().removePropertyChangeListener(propertyName, listener);
	}


	/**
	 * This method was created in VisualAge.
	 * @param shape cbit.vcell.graph.Shape
	 */
	public void removeShape(Shape shape) {
		if (shapeList.contains(shape)){
			shapeList.removeElement(shape);
		}
		Shape parent = shape.getParent();
		if (parent!=null){
			parent.removeChild(shape);
		}
		fireGraphChanged(new GraphEvent(this));
	}


	/**
	 * This method was created by a SmartGuide.
	 * @param g java.awt.Graphics
	 */
	public void resize(java.awt.Graphics2D g, java.awt.Dimension newSize) throws Exception {

		if (getTopShape()!=null){
			double newWidth = (100.0/fieldZoomPercent)*newSize.getWidth();
			double newHeight = (100.0/fieldZoomPercent)*newSize.getHeight();
			getTopShape().resize(g,new java.awt.Dimension((int)newWidth,(int)newHeight));
		}
	}


	/**
	 * This method was created by a SmartGuide.
	 * @param shape cbit.gui.Shape
	 */
	public void select(Shape shape) {
		if(shape == null){return;}
		if (!shape.isSelected()){
			shape.select();
			fireGraphChanged(new GraphEvent(this));
		}	
	}


	/**
	 * Sets the resizable property (boolean) value.
	 * @param resizable The new value for the property.
	 * @see #getResizable
	 */
	public void setResizable(boolean resizable) {
		boolean oldValue = fieldResizable;
		fieldResizable = resizable;
		firePropertyChange("resizable", new Boolean(oldValue), new Boolean(resizable));
	}


	/**
	 * Sets the zoomPercent property (int) value.
	 * @param zoomPercent The new value for the property.
	 * @see #getZoomPercent
	 */
	public void setZoomPercent(int zoomPercent) {
		if (zoomPercent<1 || zoomPercent>1000){
			throw new RuntimeException("zoomPercent must be between 1 and 1000");
		}
		int oldValue = fieldZoomPercent;
		fieldZoomPercent = zoomPercent;
		firePropertyChange("zoomPercent", new Integer(oldValue), new Integer(zoomPercent));
		fireGraphChanged();
	}


	/**
	 * Insert the method's description here.
	 * Creation date: (3/22/01 12:12:10 PM)
	 */
	public void showShapeHierarchyBottomUp() {
		System.out.println("<<<<<<<<<Shape Hierarchy Bottom Up>>>>>>>>>");
		Vector<Shape> shapes = new Vector<Shape>(shapeList);

		//
		// gather top(s) ... should only have one
		//
		Vector<Shape> topList = new Vector<Shape>();
		for (int i=0;i<shapes.size();i++){
			if (shapes.elementAt(i).getParent() == null){
				topList.add(shapes.elementAt(i));
			}
		}
		//
		// for each top, print tree
		//
		Stack<Shape> stack = new Stack<Shape>();
		for (int j=0;j<topList.size();j++){
			Shape top = topList.elementAt(j);
			System.out.println(top.toString());
			stack.push(top);
			shapes.remove(top);
			while (true){
				//
				// find first remaining children of current parent and print
				//
				boolean bChildFound = false;
				for (int i=0;i<shapes.size() && stack.size()>0;i++){
					Shape shape = shapes.elementAt(i);
					if (shape.getParent() == stack.peek()){
						char padding[] = new char[4*stack.size()];
						for (int k=0;k<padding.length;k++) padding[k] = ' ';
						String pad = new String(padding);
						System.out.println(pad+shape.toString());
						stack.push(shape);
						shapes.remove(shape);
						bChildFound = true;
						break;
					}
				}
				if (stack.size()==0){
					break;
				}
				if (bChildFound == false){
					stack.pop();
				}
			}
		}	
		if (shapes.size()>0){
			System.out.println(".......shapes left over:");
			for (int i=0;i<shapes.size();i++){
				System.out.println((shapes.elementAt(i)).toString());
			}
		}


	}


	/**
	 * Insert the method's description here.
	 * Creation date: (3/22/01 12:12:10 PM)
	 */
	public void showShapeHierarchyTopDown() {
		System.out.println("<<<<<<<<<Shape Hierarchy Top Down>>>>>>>>>");
		Vector<Shape> shapes = new Vector<Shape>(shapeList);

		//
		// gather top(s) ... should only have one
		//
		Vector<Shape> topList = new Vector<Shape>();
		for (int i=0;i<shapes.size();i++){
			if (shapes.elementAt(i).getParent() == null){
				topList.add(shapes.elementAt(i));
			}
		}
		//
		// for each top, print tree
		//
		Stack<Shape> stack = new Stack<Shape>();
		for (int j=0;j<topList.size();j++){
			Shape top = topList.elementAt(j);
			System.out.println(top.toString());
			stack.push(top);
			shapes.remove(top);
			while (stack.size()>0){
				//
				// find first remaining children of current parent and print
				//
				boolean bChildFound = false;
				Shape currShape = stack.peek();
				for(Shape shape : currShape.getChildren()) {
					if (!shapes.contains(shape)) continue;
					char padding[] = new char[4*stack.size()];
					for (int k=0;k<padding.length;k++) padding[k] = ' ';
					String pad = new String(padding);
					System.out.println(pad+shape.toString());
					stack.push(shape);
					shapes.remove(shape);
					bChildFound = true;
					break;
				}
				if (bChildFound == false){
					stack.pop();
				}
			}
		}
		if (shapes.size()>0){
			System.out.println(".......shapes left over:");
			for (int i=0;i<shapes.size();i++){
				System.out.println((shapes.elementAt(i)).toString());
			}
		}


	}
}