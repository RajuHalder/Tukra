/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This INTERFACE specification contains the methods' signatures that are necessary in the Abstract Domains of interest
 * Any specific abstract domain, e.g. SIGN, PAR, must implement this interface
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.DomainInterface;

import java.util.*;

import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.Operator.*;
import packageAbstraction.ProgEnvironment.abstractEnvironment;

//Interface AbstractDomain specifying methods necessary for an abstract domain
public interface abstractDomain
{
		
	abstractValue getTop(); 				// Returns the Top most Abstract Value of the domain
	abstractValue getBottom();				// Returns the Bottom most Abstract Value of the domain
	
	abstractValue getLUB(abstractValue[] list); 	// Returns the LUB of a list of Abstract Values of the domain
	//abstractValue getGLB(abstractValue[] list);	// Returns the GLB of a list of Abstract Values of the domain
	
	abstractValue[] getAtoms(abstractValue val);	// Returns the set of atomic abstract values whose LUB is "val" in the parameter
	abstractValue[] getSubValues(abstractValue val);// Returns the set of abstract values just one level below in the lattice whose LUB is "val" in the parameter
	
	abstractValue evaluate(abstractArithOperator ArithUnaryOp, abstractValue val); // Performs abstract evaluation of unary arithmatic operation
	abstractValue evaluate(abstractArithOperator ArithBinaryOp, abstractValue val1, abstractValue val2); // Performs abstract evaluation of binary arithmatic operation
	String evaluate(abstractRelOperator RelOp, abstractValue val1, abstractValue val2);	 // Performs abstract evaluation of relational operation: returns "true"/"false"/"top" as string
	abstractValue evaluate(abstractBoolOperator BoolUnaryOp, abstractValue val); // Performs abstract evaluation of unary boolean operation NOT
	abstractValue evaluate(abstractBoolOperator BoolBinaryOp, abstractValue val1, abstractValue val2); // Performs abstract evaluation of binary boolean operation AND/OR

	/* Substitutes concrete variables and concrete operations in the postfix expression "PostfixExpr" by the abstract values
	 * and abstract operations from abstract environment "absEnv", and obtains an abstract posfix expression "AbstractExprList" */
	void getAbstractExpressionList(String PostfixExpr, abstractEnvironment absEnv, LinkedList AbstractExprList); 
}
