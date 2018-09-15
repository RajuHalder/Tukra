/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "abstractSignValue" implements the interface specified by 
 * "packageAbstraction.ComponentInterface.AbstractComponent.AbstractOperator" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Value;

import packageAbstraction.ComponentInterface.abstractComponent.*;

public class abstractSignValue implements abstractValue
{
		
	String val; // Stores the abstract sign value
	
	public abstractSignValue() // Default constructor
	{
		val=new String("");	
	}
	
	public abstractSignValue(String s) // Constructor
	{
		val=new String(s);
	}
	
	public abstractSignValue(int c) // Constructor
	{		
		if(c==0)
	    {
	    	val="zero";	
	    }
	    else if(c < 0)
	    {
	    	val="neg";	
	    }
	    else if(c > 0)
	    {
	    	val="pos";	
	    }
	}
	
	public abstractSignValue(float c) // Constructor
	{
		
		if(c==0.0)
	    {
	    	val="zero";	
	    }
	    else if(c < 0.0)
	    {
	    	val="neg";	
	    }
	    else if(c > 0.0)
	    {
	    	val="pos";	
	    }
	}
	
	// Returns the property of the sign abstract value in the form of an array of string 
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
		if(val.equals("zero") || val.equals("neg") || val.equals("pos") )
			return true;
		else
			return false;
	}
	
	// Comparing two sign abstract values
	public boolean equals(abstractValue obj)
	{
		if(val.equals(obj.getProperty()[0]))
			return true;
		else
			return false;	
	}
	
	// Displaying the property of the sign abstract value
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

	