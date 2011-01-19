package cbit.vcell.client.desktop.biomodel.pathway;

import java.awt.Dimension;
import java.util.List;
import java.util.Random;

import org.vcell.pathway.BioPaxObject;
import org.vcell.pathway.Control;
import org.vcell.pathway.Conversion;
import org.vcell.pathway.Interaction;
import org.vcell.pathway.PathwayEvent;
import org.vcell.pathway.PathwayListener;
import org.vcell.pathway.PathwayModel;
import org.vcell.pathway.PhysicalEntity;

import cbit.gui.graph.GraphModel;
import cbit.gui.graph.Shape;

public class PathwayGraphModel extends GraphModel implements PathwayListener {
	
	private PathwayModel pathwayModel;
	protected Random random = new Random();

	public PathwayModel getPathwayModel() {
		return pathwayModel;
	}

	public void setPathwayModel(PathwayModel pathwayModel) {
		if (this.pathwayModel!=null){
			this.pathwayModel.removePathwayListener(this);
		}
		this.pathwayModel = pathwayModel;
		if (this.pathwayModel!=null){
			this.pathwayModel.addPathwayListener(this);
		}
		refreshAll();
	}

	@Override
	public void refreshAll() {
		if (pathwayModel == null){
			clearAllShapes();
			fireGraphChanged();
			return;
		}
		objectShapeMap.clear();
		PathwayContainerShape pathwayContainerShape = new PathwayContainerShape(this,pathwayModel);
		// TODO What size?
		pathwayContainerShape.getSpaceManager().setSize(400, 300);
		for (BioPaxObject bpObject : pathwayModel.getBiopaxObjects()){
			BioPaxShape bpObjectShape;
			if(bpObject instanceof Conversion) {
				bpObjectShape = new BioPaxConversionShape((Conversion) bpObject, this);				
			} else if(bpObject instanceof PhysicalEntity) {
				bpObjectShape = new BioPaxPhysicalEntityShape((PhysicalEntity) bpObject, this);
			} else {
				bpObjectShape = new BioPaxObjectShape(bpObject, this);				
			}
			pathwayContainerShape.addChildShape(bpObjectShape);
			addShape(bpObjectShape);
			Dimension shapeSize = bpObjectShape.getSpaceManager().getSize();
			Dimension containerSize = pathwayContainerShape.getSpaceManager().getSize();
			int xPos = random.nextInt(containerSize.width - shapeSize.width);
			int yPos = random.nextInt(containerSize.height - shapeSize.height);
			bpObjectShape.getSpaceManager().setRelPos(xPos, yPos);
		}
		for (BioPaxObject bpObject : pathwayModel.getBiopaxObjects()) {
			if (bpObject instanceof Conversion) {
				Conversion conversion = (Conversion) bpObject;
				BioPaxConversionShape conversionShape = 
					(BioPaxConversionShape) getShapeFromModelObject(conversion);
				for(PhysicalEntity physicalEntity : conversion.getLeftSide()) {
					Shape shape = getShapeFromModelObject(physicalEntity);
					if(shape instanceof BioPaxPhysicalEntityShape) {
						BioPaxPhysicalEntityShape physicalEntityShape = (BioPaxPhysicalEntityShape) shape;
						BioPaxConversionEdgeShape edgeShape = 
							new BioPaxConversionEdgeShape(conversionShape, physicalEntityShape, this);
						pathwayContainerShape.addChildShape(edgeShape);
						addShape(edgeShape);
					}
				}
				for(PhysicalEntity physicalEntity : conversion.getRightSide()) {
					Shape shape = getShapeFromModelObject(physicalEntity);
					if(shape instanceof BioPaxPhysicalEntityShape) {
						BioPaxPhysicalEntityShape physicalEntityShape = (BioPaxPhysicalEntityShape) shape;
						BioPaxConversionEdgeShape edgeShape = 
							new BioPaxConversionEdgeShape(conversionShape, physicalEntityShape, this);
						pathwayContainerShape.addChildShape(edgeShape);
						addShape(edgeShape);
					}
				}
			} else if (bpObject instanceof Control) {
				Control control = (Control) bpObject;
				Interaction controlledInteraction = control.getControlledInteraction();
				if(controlledInteraction instanceof Conversion) {
					List<PhysicalEntity> physicalControllers = control.getPhysicalControllers();
					if(physicalControllers != null) {
						Conversion conversion = (Conversion) controlledInteraction;
						BioPaxConversionShape conversionShape = 
							(BioPaxConversionShape) getShapeFromModelObject(conversion);
						if(conversionShape instanceof BioPaxConversionShape) {
							for(PhysicalEntity physicalEntity : physicalControllers) {
								Shape shape = getShapeFromModelObject(physicalEntity);
								if(shape instanceof BioPaxPhysicalEntityShape) {
									BioPaxPhysicalEntityShape physicalEntityShape = 
										(BioPaxPhysicalEntityShape) shape;
									BioPaxConversionEdgeShape edgeShape = 
										new BioPaxConversionEdgeShape(conversionShape, 
												physicalEntityShape, this);
									pathwayContainerShape.addChildShape(edgeShape);
									addShape(edgeShape);
								}
							}
							
						}
					}
				}
				// TODO
			}
		}
		addShape(pathwayContainerShape);
		fireGraphChanged();
	}
	
	public void pathwayChanged(PathwayEvent event) {
		refreshAll();
	}

}
