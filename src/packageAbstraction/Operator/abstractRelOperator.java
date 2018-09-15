/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "AbstractRelOperator" implements the interface specified by 
 * "packageAbstraction.ComponentInterface.AbstractComponent.AbstractOperator" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Operator;

import packageAbstraction.ComponentInterface.abstractComponent.*;

public class abstractRelOperator implements abstractOperator
{
	
	String op; // Stores the abstract relational operator
	
	public abstractRelOperator() // Default Constructor
	{
		op=new String("");
	}
	
	public abstractRelOperator(String s) // Constructor
	{
		op=new String(s);	
	}
	
	public String getOperator() // Returns abstract relational operator
	{
		return op;	
	}
	
	public boolean isArithBinaryOperator() // Is it Abstract Arithmatic Binary Operator?
	{
		return false;	
	}
	
	public boolean isArithUnaryOperator() // Is it Abstract Arithmatic Unary Operator?
	{
		return false;	
	}
	
	public boolean isRelOperator() // Is it Abstract Relational Operator?
	{
		if(op.equals("==") || op.equals("!=") || op.equals("<") || op.equals("<=") || op.equals(">=") || op.equals(">="))
			return true;
		else
			return false;	
	}
	

	public boolean isBoolBinaryOperator() // Is it Abstract AND/OR Operator?
	{
		return false;	
	}
	
	public boolean isBoolUnaryOperator() // Is it Abstract NOT Operator?
	{
		return false;	
	}
	
	public boolean equals(abstractOperator obj) // Comparing two Abstract Relational Operators
	{
		if(op.equals(obj.getOperator()))
			return true;
		else
			return false;	
	}
	
	public void display() // Displaying the abstract relational operator
	{
		System.out.print(op);	
	}
	
	public boolean isOperator() // Is it Abstract Operator?
	{
		return true;	
	}
	public boolean isValue() // Is it Abstract Value?
	{
		return false;	
	}
	
}	
