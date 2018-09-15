/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "abstractParValue" implements the interface specified by 
 * "packageAbstraction.ComponentInterface.AbstractComponent.AbstractOperator" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Value;

import packageAbstraction.ComponentInterface.abstractComponent.*;

public class abstractParValue implements abstractValue
{
		
	String val; // Stores the abstract parity value
	
	public abstractParValue() // Default Constructor
	{
		val=new String("");	
	}
	
	public abstractParValue(String s) // Constructor
	{
		val=new String(s);
	}
	
	public abstractParValue(int c) // Constructor
	{		
		if(c%2==0)
	    {
	    	val="even";	
	    }
	    else 
	    {
	    	val="odd";	
	    }
	}
	
	public abstractParValue(float c) // Constructor
	{
		
		if(c%2==0.0)
	    {
	    	val="even";	
	    }
	    else
	    {
	    	val="odd";	
	    }
	}
	
	// Returns the property of the parity abstract value in the form of an array of string 
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
		if(val.equals("even") || val.equals("odd") )
			return true;
		else
			return false;
	}
	
	// Comparing two parity abstract values
	public boolean equals(abstractValue obj)
	{
		if(val.equals(obj.getProperty()[0]))
			return true;
		else
			return false;	
	}
	
	
	// Displaying the property of the parity abstract value
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

	