/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * - Class "AbstractComponent" contains three INTERFACEs: "AbstractElement", "AbstractValue", "AbstractOperator".
 *
 * - "AbstractElement" specifies the type of abstract elements, i.e. either abstract values or abstract operators.
 *
 * - "AbstractValue" inherits the specification of "AbstractElement" and specifies additional methods' signatures.
 *   Specific abstract value, e.g. sign value or parity value, implements this interface.
 *
 * - "AbstractOperator" inherits the specification of "AbstractElement" and specifies additional methods' signatures.
 *   Specific abstract operator, e.g. arithmatic, boolean, relational operator, implements this interface.
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.ComponentInterface;


public class abstractComponent
{
	// Interface "AbstractElement" refers to Abstract Values or Abstract Operators
	public static interface abstractElement
	{
		boolean isOperator(); // Is it Abstract Operator?
		boolean isValue();	  // Is it Abstract Value?
	}

	// Interface "AbstractValue" extends "AbstractElement" and specifes the necessary methods' signatures particular to it
	public static interface abstractValue extends abstractElement
	{	
		String[] getProperty(); 			// Returns the property of the abstract value in the form of an array of string 
		boolean isBottom();     			// Is it bottom most element?
		boolean isAtomicValue(); 			// Is it atomic abstract value?
		boolean equals(abstractValue val); 	// Comparing two abstract values
		void display();						// Displaying the property of the abstract value
	}

	// Interface "AbstractOperator" extends "AbstractElement" and specifies the necessary methods' sgnature particular to it
	public static interface abstractOperator extends abstractElement
	{	
	
		public String getOperator();			// Returns the abstract operator
		boolean isArithBinaryOperator();	    // Is it Abstract Arithmatic Binary Operator?
		boolean isArithUnaryOperator();			// Is it Abstract Arithmatic Unary Operator?
		boolean isRelOperator();				// Is it Abstract Relational Operator?
		boolean isBoolBinaryOperator();			// Is it Abstract AND/OR Operator?
		boolean isBoolUnaryOperator();			// Is it Abstract NOT Operator?
		boolean equals(abstractOperator op);	// Comparing two Abstract Operators
		void display();							// Displaying the abstract operator
	}
}



	