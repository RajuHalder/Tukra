/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "abstractBoolValue" implements the interface specified by 
 * "packageAbstraction.ComponentInterface.AbstractComponent.AbstractOperator" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Value;

import packageAbstraction.ComponentInterface.abstractComponent.*;

public class abstractBoolValue implements abstractValue
{	
	String val; // Stores the abstract bool value
	
	public abstractBoolValue() // Default Constructor
	{
		val=new String("");
	}
	
	public abstractBoolValue(String s) // Constructor
	{
		val=new String(s);
	}
	
	public abstractBoolValue(boolean bool) // Constructor
	{
		if(bool)
			val=new String("true");
		else
			val=new String("false");
	}
	
	// Returns the property of the bool abstract value in the form of an array of string 
	public String[] getProperty() 
	{
		String[] arr={val};
		return arr;
	}
	
	// Is it bottom most element?
	public boolean isBottom()
	{	
		if(val.equals("bot") )
			return true;
		else
			return false;
	}
	
	// Is it atomic abstract value?
	public boolean isAtomicValue()
	{	
		if(val.equals("true") || val.equals("false") )
			return true;
		else
			return false;
	}
	
	// Comparing two bool abstract values
	public boolean equals(abstractValue obj)
	{
		if(val.equals(obj.getProperty()[0]))
			return true;
		else
			return false;	
	}
	
	
	// Displaying the property of the bool sign abstract value
	public void display()
	{
		System.out.print(val);	
	}
	
	// Is it Abstract Operator?
	public boolean isOperator() 
	{
		return false;	
	}
	
	// Is it Abstract Value?	
	public boolean isValue() 
	{
		return true;	
	}	
}


