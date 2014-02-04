package org.vcell.vis.mapping;

import java.util.HashMap;
import java.util.List;

import org.vcell.util.ISize;
import org.vcell.vis.core.Box3D;
import org.vcell.vis.vcell.CartesianMesh;
import org.vcell.vis.vcell.MembraneElement;
import org.vcell.vis.vismesh.VisDataset;
import org.vcell.vis.vismesh.VisIrregularPolyhedron;
import org.vcell.vis.vismesh.VisIrregularPolyhedron.PolyhedronFace;
import org.vcell.vis.vismesh.VisMesh;
import org.vcell.vis.vismesh.VisPoint;
import org.vcell.vis.vismesh.VisPolygon;
import org.vcell.vis.vismesh.VisPolyhedron;
import org.vcell.vis.vismesh.VisVoxel;

public class CartesianMeshMapping {
	
	public CartesianMeshMapping(){
		
	}

	public VisMesh fromMeshData(CartesianMesh cartesianMesh, String domainName, boolean bVolume){
		int dimension = cartesianMesh.getDimension();
		if (dimension==2 && bVolume){
			return fromMesh2DVolume(cartesianMesh, domainName);
		}else if (dimension==3 && bVolume){
			return fromMesh3DVolume(cartesianMesh, domainName);
		}else if (dimension==3 && !bVolume){
			return fromMesh3DMembrane(cartesianMesh, domainName);
		}else{
			throw new RuntimeException("unsupported: mesh dimension = "+dimension+", volumeDomain ="+bVolume);
		}
	}
	
	private VisMesh fromMesh2DVolume(CartesianMesh cartesianMesh, String domainName){
	    ISize size = cartesianMesh.getSize();
	    int numX = size.getX();
	    int numY = size.getY();
	    int numZ = size.getZ();
	    int dimension = 2;
	   
	    int z = 0;
	    VisMesh visMesh = new VisMesh(dimension,cartesianMesh.getOrigin(), cartesianMesh.getExtent()); // invoke VisMesh() constructor
	    int currPointIndex = 0;
	    HashMap<String,Integer> pointDict = new HashMap<String,Integer>();
	    List<Integer> volumeRegionIDs = cartesianMesh.getVolumeRegionIDs(domainName);
	    
	    int volumeIndex = 0;
	    for (int k=0;k<numZ;k++){
		    for (int j=0;j<numY;j++){
			    for (int i=0;i<numX;i++){
	    	    	int regionIndex = cartesianMesh.getVolumeRegionIndex(volumeIndex);
	    	    	if (volumeRegionIDs.contains(regionIndex)){
		    	    	Box3D element = cartesianMesh.getVolumeElementBox(i,j,k);
	                    double minX = element.x_lo;
	                    double maxX = element.x_hi;
	                    double minY = element.y_lo;
	                    double maxY = element.y_hi;
	
	                    //
	                    // counter clockwise points for a VisPolygon ... initially a quad ... then may be
	                    //
	                    //  minX,minY
	                    //  minX,maxY
	                    //  maxX,maxY
	                    //  maxX,minY
	                    //
	                    VisPoint p1Coord = new VisPoint(minX,minY,z);
	                    String p1Key = p1Coord.toStringKey();
	                    Integer i1 = pointDict.get(p1Key);
	                    if (i1 == null){
	                        pointDict.put(p1Key,currPointIndex);
	                        i1 = currPointIndex;
	                        visMesh.addPoint(p1Coord);
	                        currPointIndex++;
	                    }
	
	                    VisPoint p2Coord = new VisPoint(minX,maxY,z);
	                    String p2Key = p2Coord.toStringKey();
	                    Integer i2 = pointDict.get(p2Key);
	                    if (i2 == null){
	                        pointDict.put(p2Key,currPointIndex);
	                        i2 = currPointIndex;
	                        visMesh.addPoint(p2Coord);
	                        currPointIndex++;
	                    }
	
	                    VisPoint p3Coord = new VisPoint(maxX,maxY,z);
	                    String p3Key = p3Coord.toStringKey();
	                    Integer i3 = pointDict.get(p3Key);
	                    if (i3 == null){
	                        pointDict.put(p3Key, currPointIndex);
	                        i3 = currPointIndex;
	                        visMesh.addPoint(p3Coord);
	                        currPointIndex++;
	                    }
	
	                    VisPoint p4Coord = new VisPoint(maxX,minY,z);
	                    String p4Key = p4Coord.toStringKey();
	                    Integer i4 = pointDict.get(p4Key);
	                    if (i4 == null){
	                        pointDict.put(p4Key,currPointIndex);
	                        i4 = currPointIndex;
	                        visMesh.addPoint(p4Coord);
	                        currPointIndex++;
	                    }
	        
	                    VisPolygon quad = new VisPolygon(new int[] { i1,i2,i3,i4 },0,0,volumeIndex,0,cartesianMesh.getVolumeRegionIndex(volumeIndex));
	                  //  print('adding a cell at level '+str(currLevel.getLevel())+" from "+str(p1Coord)+" to "+str(p3Coord))
	                    visMesh.addPolygon(quad);
		            } // end if
	    		    volumeIndex++;
			    } // end i
	        } // end j
	    }
	    return visMesh;
	}

	private VisMesh fromMesh3DMembrane(CartesianMesh cartesianMesh, String domainName){
	    ISize size = cartesianMesh.getSize();
	    int numX = size.getX();
	    int numY = size.getY();
	    int dimension = 3;
	   
	    VisMesh visMesh = new VisMesh(dimension,cartesianMesh.getOrigin(), cartesianMesh.getExtent()); // invoke VisMesh() constructor
	    int currPointIndex = 0;
	    HashMap<String,Integer> pointDict = new HashMap<String,Integer>();
	    List<MembraneElement> membraneElements = cartesianMesh.getMembraneElements(domainName);
	    
	    for (MembraneElement membraneElement : membraneElements){
    		// inside
    		int insideVolumeIndex = membraneElement.getInsideVolumeIndex();
			int insideI = insideVolumeIndex % numX;
			int insideJ = (insideVolumeIndex % (numX*numY))/numX;
    		int insideK = insideVolumeIndex / (numX*numY);
    		Box3D insideBox = cartesianMesh.getVolumeElementBox(insideI, insideJ, insideK);
    		// outside
    		int outsideVolumeIndex = membraneElement.getOutsideVolumeIndex();
			int outsideI = outsideVolumeIndex % numX;
    		int outsideJ = (outsideVolumeIndex % (numX*numY))/numX;
    		int outsideK = outsideVolumeIndex / (numX*numY);
    		
            VisPoint p1Coord;
            VisPoint p2Coord;
            VisPoint p3Coord;
            VisPoint p4Coord;

            if (insideI == outsideI + 1){
            	// x-   z cross y
            	double x = insideBox.x_lo; 
                p1Coord = new VisPoint(x,insideBox.y_lo,insideBox.z_lo);
                p2Coord = new VisPoint(x,insideBox.y_lo,insideBox.z_hi);
                p3Coord = new VisPoint(x,insideBox.y_hi,insideBox.z_hi);
                p4Coord = new VisPoint(x,insideBox.y_hi,insideBox.z_lo);
     		}else if (outsideI == insideI + 1){
            	// x+   y cross z
            	double x = insideBox.x_hi; 
                p1Coord = new VisPoint(x,insideBox.y_lo,insideBox.z_lo);
                p2Coord = new VisPoint(x,insideBox.y_hi,insideBox.z_lo);
                p3Coord = new VisPoint(x,insideBox.y_hi,insideBox.z_hi);
                p4Coord = new VisPoint(x,insideBox.y_lo,insideBox.z_hi);
    		}else if (insideJ == outsideJ + 1){
            	// y-   x cross z
            	double y = insideBox.y_lo;
                p1Coord = new VisPoint(insideBox.x_lo,y,insideBox.z_lo);
                p2Coord = new VisPoint(insideBox.x_hi,y,insideBox.z_lo);
                p3Coord = new VisPoint(insideBox.x_hi,y,insideBox.z_hi);
                p4Coord = new VisPoint(insideBox.x_lo,y,insideBox.z_hi);
    		}else if (outsideJ == insideJ + 1){
            	// y+   z cross x
            	double y = insideBox.y_hi; 
                p1Coord = new VisPoint(insideBox.x_lo,y,insideBox.z_lo);
                p2Coord = new VisPoint(insideBox.x_lo,y,insideBox.z_hi);
                p3Coord = new VisPoint(insideBox.x_hi,y,insideBox.z_hi);
                p4Coord = new VisPoint(insideBox.x_hi,y,insideBox.z_lo);
    		}else if (insideK == outsideK + 1){
            	// z-   y cross x
            	double z = insideBox.z_lo; 
                p1Coord = new VisPoint(insideBox.x_lo,insideBox.y_lo,z);
                p2Coord = new VisPoint(insideBox.x_lo,insideBox.y_hi,z);
                p3Coord = new VisPoint(insideBox.x_hi,insideBox.y_hi,z);
                p4Coord = new VisPoint(insideBox.x_hi,insideBox.y_lo,z);    
    		}else if (outsideK == insideK + 1){
            	// z+   x cross y
            	double z = insideBox.z_hi; 
                p1Coord = new VisPoint(insideBox.x_lo,insideBox.y_lo,z);
                p2Coord = new VisPoint(insideBox.x_hi,insideBox.y_lo,z);
                p3Coord = new VisPoint(insideBox.x_hi,insideBox.y_hi,z);
                p4Coord = new VisPoint(insideBox.x_lo,insideBox.y_hi,z);    
    		}else{
    			throw new RuntimeException("inside/outside volume indices not reconciled in membraneElement "+membraneElement.getMembraneIndex()+" in domain "+domainName);
    		}

            //
            // make sure vertices are added to model without duplicates and get the assigned identifier.
            //
            String p1Key = p1Coord.toStringKey();
            Integer i1 = pointDict.get(p1Key);
            if (i1 == null){
                pointDict.put(p1Key,currPointIndex);
                i1 = currPointIndex;
                visMesh.addPoint(p1Coord);
                currPointIndex++;
            }

            String p2Key = p2Coord.toStringKey();
            Integer i2 = pointDict.get(p2Key);
            if (i2 == null){
                pointDict.put(p2Key,currPointIndex);
                i2 = currPointIndex;
                visMesh.addPoint(p2Coord);
                currPointIndex++;
            }

            String p3Key = p3Coord.toStringKey();
            Integer i3 = pointDict.get(p3Key);
            if (i3 == null){
                pointDict.put(p3Key, currPointIndex);
                i3 = currPointIndex;
                visMesh.addPoint(p3Coord);
                currPointIndex++;
            }

            String p4Key = p4Coord.toStringKey();
            Integer i4 = pointDict.get(p4Key);
            if (i4 == null){
                pointDict.put(p4Key,currPointIndex);
                i4 = currPointIndex;
                visMesh.addPoint(p4Coord);
                currPointIndex++;
            }

            VisPolygon quad = new VisPolygon(new int[] { i1,i2,i3,i4 },0,0,membraneElement.getMembraneIndex(),0,cartesianMesh.getMembraneRegionIndex(membraneElement.getMembraneIndex()));
          //  print('adding a cell at level '+str(currLevel.getLevel())+" from "+str(p1Coord)+" to "+str(p3Coord))
            visMesh.addPolygon(quad);

	    }
	    return visMesh;
	}

	private VisMesh fromMesh3DVolume(CartesianMesh cartesianMesh, String domainName){
	    ISize size = cartesianMesh.getSize();
	    int numX = size.getX();
	    int numY = size.getY();
	    int numZ = size.getZ();
	    int dimension = 3;
	   
	    VisMesh visMesh = new VisMesh(dimension,cartesianMesh.getOrigin(), cartesianMesh.getExtent()); // invoke VisMesh() constructor
	    int currPointIndex = 0;
	    HashMap<String,Integer> pointDict = new HashMap<String,Integer>();
	    List<Integer> volumeRegionIDs = cartesianMesh.getVolumeRegionIDs(domainName);
	    
	    int volumeIndex = 0;
	    for (int k=0;k<numZ;k++){
		    for (int j=0;j<numY;j++){
			    for (int i=0;i<numX;i++){
	    	    	int regionIndex = cartesianMesh.getVolumeRegionIndex(volumeIndex);
	    	    	if (volumeRegionIDs.contains(regionIndex)){
		    	    	Box3D element = cartesianMesh.getVolumeElementBox(i,j,k);
	                    double minX = element.x_lo;
	                    double maxX = element.x_hi;
	                    double minY = element.y_lo;
	                    double maxY = element.y_hi;
	                    double minZ = element.z_lo;
	                    double maxZ = element.z_hi;
	
	                    //
	                    // points for a VisPolyhedra ... initially a hex ... then may be clipped
	                    //
	                    //       p6-------------------p7
	                    //      /|                   /|
	                    //     / |                  / |
	                    //   p4-------------------p5  |
	                    //    |  |                 |  |
	                    //    |  |                 |  |
	                    //    |  |                 |  |         z   y
	                    //    |  p2................|..p3        |  /
	                    //    | /                  | /          | /
	                    //    |/                   |/           |/
	                    //   p0-------------------p1            O----- x
	                    //
	                    //  p0 = (X-,Y-,Z-)
	                    //  p1 = (X+,Y-,Z-)
	                    //  p2 = (X-,Y+,Z-)
	                    //	p3 = (X+,Y+,Z-)
	                    //  p4 = (X-,Y-,Z+)
	                    //  p5 = (X+,Y-,Z+)
	                    //  p6 = (X-,Y+,Z+)
	                    //	p7 = (X+,Y+,Z+)
	                    //
	                    VisPoint[] visPoints = {
	                    		new VisPoint(minX,minY,minZ),  // p0
	                    		new VisPoint(maxX,minY,minZ),  // p1
	                    		new VisPoint(minX,maxY,minZ),  // p2
	                    		new VisPoint(maxX,maxY,minZ),  // p3
	                    		new VisPoint(minX,minY,maxZ),  // p4
	                    		new VisPoint(maxX,minY,maxZ),  // p5
	                    		new VisPoint(minX,maxY,maxZ),  // p6
	                    		new VisPoint(maxX,maxY,maxZ),  // p7
	                    };
	                    int[] indices = new int[8];
	                    for (int v=0;v<8;v++){
	                    	VisPoint visPoint = visPoints[v];
	                        String key = visPoint.toStringKey();
	                        Integer p = pointDict.get(key);
	                        if (p == null){
	                            pointDict.put(key,currPointIndex);
	                            p = currPointIndex;
	                            visMesh.addPoint(visPoint);
	                            currPointIndex++;
	                        }
	                        indices[v] = p;
	                    }
	                    VisVoxel voxel = new VisVoxel(indices,0,0,volumeIndex,0,cartesianMesh.getVolumeRegionIndex(volumeIndex));
	                  //  print('adding a cell at level '+str(currLevel.getLevel())+" from "+str(p1Coord)+" to "+str(p3Coord))
	                    visMesh.addPolyhedron(voxel);
	 	            } // end if
	    		    volumeIndex++;
		        } // end for i
		    } // end for j
	    } // end for k
	    return visMesh;
	}

	public void check(VisDataset visDataset){
		VisMesh visMesh = visDataset.getDomains().get(0).getVisMesh();
		for (VisPolyhedron visPolyhedron : visMesh.getPolyhedra()){
			if (visPolyhedron instanceof VisVoxel){
				VisVoxel visVoxel = (VisVoxel)visPolyhedron;
				for (int p : visVoxel.getPointIndices()){
					VisPoint vp = visMesh.getPoints().get(p);
					if (vp==null){
						throw new RuntimeException("couldn't find point "+p);
					}
				}
			}else if (visPolyhedron instanceof VisIrregularPolyhedron){
				VisIrregularPolyhedron visIrregularPolyhedron = (VisIrregularPolyhedron)visPolyhedron;
				for (PolyhedronFace face : visIrregularPolyhedron.getFaces()){
					for (int p : face.getVertices()){
						VisPoint vp = visMesh.getPoints().get(p);
						if (vp==null){
							throw new RuntimeException("couldn't find point "+p);
						}
					}
				}
			}
		}
		System.out.println("ChomboMeshMapping:check() first mesh passed the point test");
	}
	
}
