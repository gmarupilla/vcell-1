/* Generated By:JJTree: Do not edit this line. ASTRelationalNode.java */

package org.vcell.model.bngl;

public class ASTRelationalNode extends SimpleNode {
  public String operatorString = null;
  public ASTRelationalNode(int id) {
    super(id);
  }

  public ASTRelationalNode(BNGLParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(BNGLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  @Override
  public String toBNGL() {
	if (jjtGetNumChildren()!=2){
		throw new RuntimeException("expecting two operands for relational operators");
	}
	return "(("+jjtGetChild(0).toBNGL()+")"+operatorString+"("+jjtGetChild(1).toBNGL()+"))";
  }

public void setOperationFromToken(String image) {
	this.operatorString = image;
}

}
