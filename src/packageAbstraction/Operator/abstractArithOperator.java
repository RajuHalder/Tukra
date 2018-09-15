/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "AbstractArithOperator" implements the interface specified by 
 * "packageAbstraction.ComponentInterface.AbstractComponent.AbstractOperator" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Operator;

import packageAbstraction.ComponentInterface.abstractComponent.*;

public class abstractArithOperator implements abstractOperator
{
	
	String op; // Stores the abstract arithmatic operator
	
	public abstractArithOperator()  // Default Constructor
	{
		op=new String("");
	}
	
	public abstractArithOperator(String s) // Constructor
	{
		op=new String(s);	
	}
	
	public String getOperator() // Returns abstract arithmatic operator
	{
		return op;	
	}
	
	public boolean isArithBinaryOperator() // Is it Abstract Arithmatic Binary Operator?
	{
		if(op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/"))
			return true;
		else
			return false;	
	}
	
	public boolean isArithUnaryOperator() // Is it Abstract Arithmatic Unary Operator?
	{
		if(op.equals("u-"))
			return true;
		else
			return false;	
	}
	
	public boolean isRelOperator() // Is it Abstract Relational Operator?
	{
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
	
	public boolean equals(abstractOperator obj) // Comparing two Abstract Arithmatic Operators
	{
		if(op.equals(obj.getOperator()))
			return true;
		else
			return false;	
	}
	
	public void display() // Displaying the abstract arithmatic operator
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
