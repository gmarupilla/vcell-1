/* Generated By:JJTree: Do not edit this line. ASTRationalNumberExponent.java */

package cbit.vcell.units.parser;

import cbit.vcell.matrix.RationalNumber;

public class ASTRationalNumberExponent extends SimpleNode {
	RationalNumber value = null;

	public ASTRationalNumberExponent(int id) {
		super(id);
	}

	public ASTRationalNumberExponent(UnitSymbolParser p, int id) {
		super(p, id);
	}

	public String toInfix(RationalNumber power) {
		RationalNumber product = value.mult(power);
		if (product.intValue() == product.doubleValue()) {
			return product.infix();
		} else {
			return "(" + product.infix() + ")";
		}
	}

	public String toSymbol(RationalNumber power) {
		RationalNumber product = value.mult(power);
		if (product.intValue() == product.doubleValue()) {
			return product.infix();
		} else {
			return "(" + product.infix() + ")";
		}
	}

}
