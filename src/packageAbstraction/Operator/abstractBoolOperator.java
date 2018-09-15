/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "AbstractBoolOperator" implements the interface specified by 
 * "packageAbstraction.ComponentInterface.AbstractComponent.AbstractOperator" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Operator;

import packageAbstraction.ComponentInterface.abstractComponent.*;

public class abstractBoolOperator implements abstractOperator
{
	
	String op; // Stores the abstract boolean operator
	
	public abstractBoolOperator() // Default Constructor
	{
		op=new String("");
	}
	
	public abstractBoolOperator(String s) // Constructor
	{
		op=new String(s);	
	}
	
	public String getOperator() // Returns abstract boolean operator
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
		return false;	
	}

	
	public boolean isBoolBinaryOperator() // Is it Abstract AND/OR Operator?
	{
		if(op.equals("&") || op.equals("|"))
			return true;
		else
			return false;	
	}
	
	public boolean isBoolUnaryOperator() // Is it Abstract NOT Operator?
	{
		if(op.equals("!"))
			return true;
		else
			return false;	
	}
	
	public boolean equals(abstractOperator obj) // Comparing two Abstract Boolean Operators
	{
		if(op.equals(obj.getOperator()))
			return true;
		else
			return false;	
	}
	
	public void display() // Displaying the abstract boolean operator
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






