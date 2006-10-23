package org.vcell.ncbc.physics.component;

import cbit.vcell.units.VCUnitDefinition;

/**
 * Insert the type's description here.
 * Creation date: (1/13/2004 11:43:09 AM)
 * @author: Jim Schaff
 */
public class ConcentrationPort extends Port {
	public static final String NAME = "conc";

/**
 * ConcentrationPort constructor comment.
 * @param argVariable ncbc.physics.component.Variable
 * @param argRole int
 */
public ConcentrationPort(int argRole) {
	super(new Variable(NAME, VCUnitDefinition.UNIT_uM), argRole);
}
}