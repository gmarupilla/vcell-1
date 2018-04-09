/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vcell.util.Matchable;


/**
 * Insert the type's description here.
 * Creation date: (10/3/00 2:07:11 PM)
 * @author: 
 */
@SuppressWarnings("serial")
public class VariableType implements java.io.Serializable, org.vcell.util.Matchable {

	private int type = -1;
	private VariableDomain variableDomain;
	private final String name;
	private final String units;
	private final String label;
	/**
	 * mark types that were not previously supported by {@link #getVariableTypeFromInteger(int)}, issue warning if triggered
	 */
	private boolean legacyWarn = false;
	private static final List<VariableType> allTypes = new ArrayList<>();
	private static final Logger LG = LogManager.getLogger(VariableType.class);
	
	//match constants in numerics SimTypes.h
	private static final int UNKNOWN_TYPE = 0;
	private static final int VOLUME_TYPE = 1;
	private static final int MEMBRANE_TYPE = 2;
	private static final int CONTOUR_TYPE = 3;
	private static final int VOLUME_REGION_TYPE = 4;
	private static final int MEMBRANE_REGION_TYPE = 5;
	private static final int CONTOUR_REGION_TYPE = 6;
	private static final int NONSPATIAL_TYPE = 7;
	private static final int VOLUME_PARTICLE = 8; 
	private static final int MEMBRANE_PARTICLE = 9; 
	private static final int POINT_VARIABLE_TYPE = 10; 
	/**
	 * not generated by solver 
	 */
	private static final int POSTPROCESSING_TYPE = 999;
	
//	private static final String[] NAMES = {"Unknown","Volume","Membrane","Contour","Volume_Region","Membrane_Region","Contour_Region","Nonspatial","Post_Process"};
//	private static final String[] LABEL = {"Unknown","Conc","Density","Density","Conc","Density","Density","Conc","Unknown"};
//	private static final String[] UNITS = {"Unknown","uM","molecules/um^2","molecules/um","uM","molecules/um^2","molecules/um","uM","Unknown"};
	
	public static final VariableType UNKNOWN = new VariableType(UNKNOWN_TYPE,"Unknown","Unknown","Unknown");
	public static final VariableType VOLUME = new TwoCodeType(VOLUME_TYPE,VOLUME_PARTICLE,"Volume","uM","Conc");
	public static final VariableType MEMBRANE = new TwoCodeType(MEMBRANE_TYPE,MEMBRANE_PARTICLE,"Membrane","molecules/um^2","Density");
	public static final VariableType CONTOUR = new VariableType(CONTOUR_TYPE,"Contour","molecules/um","Density");
	public static final VariableType VOLUME_REGION = new VariableType(VOLUME_REGION_TYPE,"Volume_Region","uM","Conc");
	public static final VariableType MEMBRANE_REGION = new VariableType(MEMBRANE_REGION_TYPE,"Membrane_Region","molecules/um^2","Density");
	public static final VariableType CONTOUR_REGION = new VariableType(CONTOUR_REGION_TYPE,"Contour_Region","molecules/um","Density");
	public static final VariableType NONSPATIAL = new VariableType(NONSPATIAL_TYPE,"Nonspatial","uM","Conc");
	public static final VariableType POSTPROCESSING = new VariableType(POSTPROCESSING_TYPE,"Post_Process","Unknown","Unknown");
	public static final VariableType POINT_VARIABLE = new VariableType(POINT_VARIABLE_TYPE,"Point","uM","Conc");
	static {
		UNKNOWN.legacyWarn = true;
		NONSPATIAL.legacyWarn = true;
	}
	
	public enum VariableDomain {
		VARIABLEDOMAIN_POSTPROCESSING("PostProcessing"),
		VARIABLEDOMAIN_UNKNOWN("Unknown"),
		VARIABLEDOMAIN_VOLUME("Volume"),
		VARIABLEDOMAIN_MEMBRANE("Membrane"),
		VARIABLEDOMAIN_CONTOUR("Contour"),
		VARIABLEDOMAIN_NONSPATIAL("Nonspatial"),
		VARIABLEDOMAIN_POINT("Point");
		
		private String name = null;
		private VariableDomain(String arg_name) {
			name = arg_name;
		}
		public String getName() {
			return name;
		}
	}
	/**
	 * create and store reference in class list
	 * @param varType
	 */
	protected VariableType(int varType, String name, String units, String label) {
		super();
		this.type = varType;
		this.name = name;
		this.units = units;
		this.label = label;
		switch (type) {
		case UNKNOWN_TYPE:
			variableDomain = VariableDomain.VARIABLEDOMAIN_UNKNOWN;
			break;
		case POSTPROCESSING_TYPE:
			variableDomain = VariableDomain.VARIABLEDOMAIN_POSTPROCESSING;
			break;
		case VOLUME_TYPE:
		case VOLUME_REGION_TYPE:
			variableDomain = VariableDomain.VARIABLEDOMAIN_VOLUME;
			break;
		case MEMBRANE_TYPE:
		case MEMBRANE_REGION_TYPE:
			variableDomain = VariableDomain.VARIABLEDOMAIN_MEMBRANE;
			break;
		case CONTOUR_TYPE:
		case CONTOUR_REGION_TYPE:
			variableDomain = VariableDomain.VARIABLEDOMAIN_CONTOUR;
			break;
		case NONSPATIAL_TYPE:
			variableDomain = VariableDomain.VARIABLEDOMAIN_NONSPATIAL;
			break;
		case POINT_VARIABLE_TYPE:
			variableDomain = VariableDomain.VARIABLEDOMAIN_POINT;
			break;
		default:
			throw new RuntimeException("Unknown variable type " + type);
		}
		allTypes.add(this);
	}
/**
 * Insert the method's description here.
 * Creation date: (5/24/2001 9:28:51 PM)
 * @return boolean
 * @param obj cbit.util.Matchable
 */
public boolean compareEqual(Matchable obj) {
	return equals(obj);
}
/**
 * Insert the method's description here.
 * Creation date: (10/3/00 5:42:33 PM)
 */
public boolean equals(Object obj) {
	if (obj == null) {
		return false;
	}
	if (!(obj instanceof VariableType)) {
		return false;
	}
	VariableType pdeVT = (VariableType) obj;
	if (type!=pdeVT.type) {
		return false;
	}
	return true;
}

public String getDefaultLabel() {
	 return  label;
}

public String getDefaultUnits() {
	return units; 
}

public int getType() {
	return type;
}

public String getTypeName() {
	return name;
}
///**
// * Insert the method's description here.
// * Creation date: (10/3/00 2:48:55 PM)
// * @return cbit.vcell.simdata.PDEVariableType
// * @param mesh cbit.vcell.solvers.CartesianMesh
// * @param dataLength int
// */
//public static final VariableType getVariableTypeFromInteger(int varType) {
//	if (varType==VOLUME.type){
//		return VOLUME;
//	}else if (varType==MEMBRANE.type){
//		return MEMBRANE;
//	}else if (varType==CONTOUR.type){
//		return CONTOUR;
//	}else if (varType==VOLUME_REGION.type){
//		return VOLUME_REGION;
//	}else if (varType==MEMBRANE_REGION.type){
//		return MEMBRANE_REGION;
//	}else if (varType==CONTOUR_REGION.type){
//		return CONTOUR_REGION;
//	}else{
//		throw new IllegalArgumentException("varType="+varType+" is undefined");
//	}
//}



/**
 * search types to match criteria
 * @param criteria
 * @param errorMsg to display if miss
 * @return type that matches
 * @throws IllegalArgumentException if no match
 */
private static final VariableType find(Predicate<VariableType> criteria, String errorMsg) {
	Optional<VariableType> vt = allTypes.stream().filter( criteria ).findFirst();
	if (vt.isPresent()) {
		return vt.get();
	}
	
	throw new IllegalArgumentException("varType="+errorMsg+" is undefined");	
	
}
public static final VariableType getVariableTypeFromInteger(int varType) {
	Predicate<VariableType> pred = v -> v.supportsCode(varType);
	return find(pred,Integer.toString(varType));
	
}
public static final VariableType getVariableTypeFromVariableTypeName(String type) {	
	Predicate<VariableType> pred = v -> v.name.equals(type);
	return find(pred,type);
}

public static final VariableType getVariableTypeFromVariableTypeNameIgnoreCase(String type) {	
	Predicate<VariableType> pred = v -> v.name.equalsIgnoreCase(type);
	return find(pred,type);
}

/**
 * Insert the method's description here.
 * Creation date: (10/5/00 11:01:55 AM)
 * @return int
 */
public int hashCode() {
	return type;
}
/**
 * Insert the method's description here.
 * Creation date: (7/6/01 4:05:42 PM)
 * @return boolean
 * @param varType cbit.vcell.simdata.VariableType
 */
public boolean isExpansionOf(VariableType varType) {
	//
	// an enclosing domain (e.g. VOLUME) is an expansion of an enclosed region (e.g. VOLUME_REGION).
	//
	// example: if VOLUME_REGION and VOLUME data are used in same function,
	// then function must be evaluated at each volume index (hence VOLUME wins).
	//
	if (type == VOLUME_TYPE && varType.type == VOLUME_REGION_TYPE) return true;
	if (type == MEMBRANE_TYPE && varType.type == MEMBRANE_REGION_TYPE) return true;
	if (type == CONTOUR_TYPE && varType.type == CONTOUR_REGION_TYPE) return true;
	if (type != POINT_VARIABLE_TYPE && varType.type == POINT_VARIABLE_TYPE) return true;
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (5/8/01 2:12:01 PM)
 * @return java.lang.String
 */
public String toString() {
	return getTypeName()+"_VariableType";
}
public final VariableDomain getVariableDomain() {
	return variableDomain;
}

public static VariableType getVariableType(Variable var) {
	if (var instanceof VolVariable || var instanceof VolumeParticleVariable) {
		return VariableType.VOLUME;
	} else if (var instanceof VolumeRegionVariable) {
		return VariableType.VOLUME_REGION;
	} else if (var instanceof MemVariable || var instanceof MembraneParticleVariable) {
		return VariableType.MEMBRANE;
	} else if (var instanceof MembraneRegionVariable) {
		return VariableType.MEMBRANE_REGION;
	} else if (var instanceof FilamentVariable) {
		return VariableType.CONTOUR;
	} else if (var instanceof FilamentRegionVariable) {
		return VariableType.CONTOUR_REGION;
	} else if (var instanceof InsideVariable) {
		return VariableType.MEMBRANE;
	} else if (var instanceof OutsideVariable) {
		return VariableType.MEMBRANE;
	} else if (var instanceof DataGenerator){
		return VariableType.POSTPROCESSING;
	} else {
		return VariableType.UNKNOWN;
	}
}
public boolean incompatibleWith(VariableType funcType) {
	if ((this.type == POSTPROCESSING_TYPE || funcType.type == POSTPROCESSING_TYPE) && this.type != funcType.type){
		return false;
	}
	return true;
}

/**
 * does variable support code value? 
 * @param code
 * @return true if does
 */
protected boolean supportsCode(int code) {
	final boolean matches = ( code == type );
	if (matches && legacyWarn  && LG.isWarnEnabled()) {
		LG.warn("code match found on " + code + ", " + getTypeName() + ", not previously supported");
	}
	return matches;
}

private static class TwoCodeType extends VariableType {

	private final int secondaryType;

	protected TwoCodeType(int varType, int secondaryType,String name, String units, String label) {
		super(varType, name, units, label);
		this.secondaryType = secondaryType;
	}

	/**
	 * check against both codes
	 */
	@Override
	protected boolean supportsCode(int code) {
		return super.supportsCode(code) || code == secondaryType;
	}
	
	
	
}

}
