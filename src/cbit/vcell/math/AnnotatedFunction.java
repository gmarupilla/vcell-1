package cbit.vcell.math;
import cbit.vcell.parser.Expression;
import cbit.vcell.math.Function;
import cbit.vcell.simdata.VariableType;
/**
 * Insert the type's description here.
 * Creation date: (1/29/2004 11:48:16 AM)
 * @author: Anuradha Lakshminarayana
 */
public class AnnotatedFunction extends Function implements org.vcell.util.Matchable {
	private java.lang.String fieldErrorString = null;
	private VariableType fieldFunctionType = null;
	private boolean bIsUserDefined = false;
	protected transient java.beans.PropertyChangeSupport propertyChange;
	//private cbit.vcell.parser.Expression fieldSimplifiedExpression = null;

/**
 * AnnotatedFunction constructor comment.
 */
public AnnotatedFunction(String argFunctionName, Expression argFunctionExpression, String argErrString, VariableType argFunctionType, boolean userDefined) {
	super(argFunctionName, argFunctionExpression);
	if (argFunctionName.indexOf(" ") > 0) {
		throw new RuntimeException("Spaces are not allowed in user-defined function names. Try adding the function without spaces in its name.");
	}
	//fieldSimplifiedExpression = null;
	fieldErrorString = argErrString;
	fieldFunctionType = argFunctionType;
	bIsUserDefined = userDefined;
}


/**
 * Insert the method's description here.
 * Creation date: (1/29/2004 11:53:36 AM)
 * @return java.lang.String
 */
public java.lang.String getErrorString() {
	return fieldErrorString;
}


/**
 * Insert the method's description here.
 * Creation date: (1/29/2004 2:22:09 PM)
 * @return cbit.vcell.simdata.VariableType
 */
public VariableType getFunctionType() {
	return fieldFunctionType;
}


///**
// * Gets the simplifiedExpression property (cbit.vcell.parser.Expression) value.
// * @return The simplifiedExpression property value.
// * @see #setSimplifiedExpression
// */
//public cbit.vcell.parser.Expression getSimplifiedExpression() {
//	return fieldSimplifiedExpression;
//}


/**
 * Insert the method's description here.
 * Creation date: (2/20/2004 11:05:24 AM)
 * @return boolean
 */
public boolean isUserDefined() {
	return bIsUserDefined;
}

public String getDisplayName() {
	return getName() + " : " + getExpression().infix();
}

///**
// * Sets the simplifiedExpression property (cbit.vcell.parser.Expression) value.
// * @param simplifiedExpression The new value for the property.
// * @see #getSimplifiedExpression
// */
//public void setSimplifiedExpression(cbit.vcell.parser.Expression simplifiedExpression) {
//	fieldSimplifiedExpression = simplifiedExpression;
//}
}