/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "AbstractBoolDomain" implements the interface specified by 
 * "packageAbstraction.DomainInterface.abstractDomain" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Domain;

import java.util.*;
import packageMyLibrary.*;

import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.Operator.*;
import packageAbstraction.Value.abstractBoolValue;
import packageAbstraction.ProgEnvironment.abstractEnvironment;

public class abstractBoolDomain extends myFunctions implements packageAbstraction.DomainInterface.abstractDomain, myPatterns
{
	
	// Returns the Top most Abstract Value of the Bool domain
	public abstractValue getTop() 
   	{
   		return 	(new abstractBoolValue("top"));
   	}
   	
   	// Returns the Bottom most Abstract Value of the Bool domain
	public abstractValue getBottom() 
	{
		return 	(new abstractBoolValue("bot"));	
	}
	
	// Returns the LUB of a list of Abstract Values of the Bool domain
	public abstractValue getLUB(abstractValue[] list) 
	{
		if(list.length==0) // No Abstract Value in the list
		{
			System.out.println("AbstractBoolDomain:getLUB():Its difficult to obtain LUB from a list of zero abstract boolean element!!!");
			return null;
		}
		else if(list.length==1) // One Abstract Value in the list
		{
			return list[0];
		}	
		else // Two or more Abstract Values in the list
		{
			// Extract the first Abstract Value from the list
			abstractValue[] first=new abstractValue[1];
			first[0]=list[0];
			
			// Extract the remaining Abstract Values from the list
			abstractValue[] rest=new abstractValue[list.length-1];
			for(int i=0;i<list.length-1;i++)
			{
				rest[i]=list[i+1];	
			}
			
			abstractValue val1=getLUB(first); // Compute the LUB of the first Abstract Value
			abstractValue val2=getLUB(rest);  // Compute the LUB of the remaining Abstract Values
			
			// Get encryption and LUB tables in Bool domain
			BoolOperation bool_op=new BoolOperation();			
			String[][] EncTab=bool_op.getEncTab();
			String[][] LUBtab=bool_op. getLUBtab();
			
			int index_val1=super.getIndex(val1.getProperty()[0], EncTab[0]); //Get index of the first abstract bool value from the encryption table
	   		int index_val2=super.getIndex(val2.getProperty()[0], EncTab[0]); //Get index of the second abstract bool value from the encryption table
   		
			String res=LUBtab[index_val1][index_val2]; //extract the result of LUB(val1, val2) from the LUB table
			return (new abstractBoolValue(res));       // Return the resultant abstract bool value
		}
	}
	
	
	// Returns the GLB of a list of Abstract Values of the Bool domain
	// abstractValue getGLB(abstractValue[] list); 
		
		
	// Returns the set of atomic bool values whose LUB is "val" in the parameter
	public abstractValue[] getAtoms(abstractValue val) 
	{
		
		LinkedList list=new LinkedList();
		addAtoms2List(val, list); // Function call that returns a list of atomic bool values of "val"
	
		//Converting "list" to an array of atomic bool values
		Object[] arr=list.toArray(); 
		abstractValue[] result=new abstractBoolValue[arr.length];
		for(int i=0;i<arr.length;i++)
		{
			result[i]=(abstractBoolValue)arr[i];	
		}
		
		return result; //Returning the array of atomic bool values
	}
	
	
	// Recursive function to determining a list of atomic bool values of "val"
	private void addAtoms2List(abstractValue val, LinkedList list)
	{
		if(val.isAtomicValue()) // "val" is atomic, so add to the list
		{
			list.addLast(val); 
		}
		else // "val" is composite
		{
			// Function call that returns sub-abstract values just one level below the Bool lattice
			abstractValue[] SubVal=getSubValues(val);  
						
			for(int i=0;i<SubVal.length;i++)
			{
				addAtoms2List(SubVal[i], list); // Recursively call for all sub-abstract values
			}
		}
	}	
	

	// Returns the set of bool abstract values just one level below in the Bool lattice whose LUB is "val" in the parameter
	public abstractValue[] getSubValues(abstractValue val) 
	{
		// Get encryption and sub-values table in Bool domain
		BoolOperation bool_op=new BoolOperation();
		String[][] EncTab=bool_op.getEncTab();
		String[][] SubValTab=bool_op.getSubValTab(); 
		
		// Get index of the "val" from the encryption table		
		int index_val=super.getIndex(val.getProperty()[0], EncTab[0]); 
		
		// Extract the array of sub-abstract bool values cooresponding to the index from the table that stores sub-abstract bool values
		String[] vals=SubValTab[index_val];
		
		abstractBoolValue[] result=new abstractBoolValue[vals.length]; 
		for(int i=0;i<vals.length;i++)
		{
			result[i]=new abstractBoolValue(vals[i]);
		}
		
		return result;	
	}
	
	
	
	// Performs abstract evaluation of unary arithmatic operation
	public abstractValue evaluate(abstractArithOperator UnaryOp, abstractValue val) 
	{
		return getBottom(); // Hey! Its a Bool domain
	}



	// Performs abstract evaluation of binary arithmatic operation
	public abstractValue evaluate(abstractArithOperator BinaryOp, abstractValue val1, abstractValue val2) 
	{
		return getBottom(); // Hey! Its a Bool domain
	}
	
	
	
	// Performs abstract evaluation of relational operation: returns "true"/"false"/"top" as string
	public String evaluate(abstractRelOperator RelOp, abstractValue val1, abstractValue val2)
	{
		return "bot"; // Hey! Its a Bool domain
	}
	
	
	
	// Performs abstract evaluation of unary boolean operation NOT
	public abstractValue evaluate(abstractBoolOperator BoolUnaryOp, abstractValue val)
	{
		// Get encryption and operational table in Bool domain
		BoolOperation bool_op=new BoolOperation();
		String[][][] OpTab=bool_op.getOpTab(); 
   		String[][] EncTab=bool_op.getEncTab();
   		
   		// Get index of the "val" from the encryption table
   		int index_val=super.getIndex(val.getProperty()[0], EncTab[0]); 
   		
   		// Get index of the "BoolUnaryOp" from the encryption table  		
   		int index_op=super.getIndex(BoolUnaryOp.getOperator(), EncTab[1]);
		
		if(index_val!=-1 && index_op!=-1)
		{
			//extract the result from the operational table
			String result=OpTab[0][index_op][index_val];  
			return (new abstractBoolValue(result));
		}
		else // Wrong operators or values
		{
			System.out.println("AbstractBoolDomain:evaluate():Either wrong boolean unary operator or wrong abstract boolean value!!!");
			return getBottom();
		}
	}
	
	// Performs abstract evaluation of binary boolean operation AND/OR
	public abstractValue evaluate(abstractBoolOperator BoolBinaryOp, abstractValue val1, abstractValue val2)
	{
		
		// Get encryption and operational table in Bool domain
		BoolOperation bool_op=new BoolOperation();
		String[][][] OpTab=bool_op.getOpTab();
		String[][] EncTab=bool_op.getEncTab();
		
		// Get index of the "val1" and "val2" from the encryption table
   		int index_val1=super.getIndex(val1.getProperty()[0], EncTab[0]);
   		int index_val2=super.getIndex(val2.getProperty()[0], EncTab[0]);
   		
   		// Get index of the "BoolBinaryOp" from the encryption table    		
   		int index_op=super.getIndex(BoolBinaryOp.getOperator(), EncTab[2]);			
		
		if(index_val1!=-1 && index_val2!=-1 && index_op!=-1)
		{
			//extract the result from the operational table
			String result=OpTab[index_op][index_val1][index_val2];
			return (new abstractBoolValue(result));
		}
		else // Wrong operators or values
		{
			System.out.println("AbstractBoolDomain:evaluate():Either wrong boolean binary operator or wrong abstract boolean value!!!");
			return getBottom();
		}
	}
	

   	
	// Substitutes concrete variables and concrete operations in the postfix expression "PostfixExpr" by the abstract values
	// and abstract operations from abstract environment "absEnv", and obtains an abstract posfix expression "AbstractExprList" 
	public void getAbstractExpressionList(String PostfixExpr, abstractEnvironment absEnv, LinkedList ExprList)
	{
		
		// Iterating for all tokens in the concrete expression
		StringTokenizer st = new StringTokenizer(PostfixExpr, " ");
      	while(st.hasMoreTokens())
     	{
     		String ch=st.nextToken().trim();
     		
     		if(ch.equals("!")||ch.equals("&")||ch.equals("|")) 
     		{
     			// Substituting concrete boolean operations by abstract versions
     			abstractBoolOperator bo=new abstractBoolOperator(ch);
     			ExprList.addLast(bo);	
     		}
     		else if(ch.equals("top")) 
			{
				// Substituting concrete boolean operations by abstract versions
     			ExprList.addLast(getTop());	
			}
			else if(ch.equals("bot"))
			{
				// Substituting concrete boolean operations by abstract versions
     			ExprList.addLast(getBottom());	
			}
			else if(ch.equals("true")||ch.equals("false"))
			{
				// Substituting concrete boolean values by abstract versions
				ExprList.addLast(new abstractBoolValue(ch));
			}
			else if(java.util.regex.Pattern.matches(REGEX_VARIABLE, ch) && (!(ch.equals("true")||ch.equals("false")||ch.equals("top")||ch.equals("bot"))))
			{
				// Substituting concrete variables by abstract bool values from the abstract environment
				ExprList.addLast(absEnv.getVariableValue(ch));
			}
     	} 		
	}
}


//***************** Operation Table for BOOL Domain *****************************************************

class BoolOperation
{
	
	/* Encryption Table
	 * Enc_BOOL[0]: for abstract values
	 * Enc_BOOL[1]: for abstract uniary operations
	 * Enc_BOOL[2]: for abstract binary operations */
	 
	String[][] getEncTab()
    {
    	String[][] Enc_BOOL={{"bot", "true", "false", "top"},{"!"},{"&", "|"}};
   		return Enc_BOOL;	
    }

    /* Operational Table */
	String[][][] getOpTab()
    {

     	int i=-1;
     	int j=-1;
     	int k=-1;
     	

		String[][][] BOOL=new String[3][][]; //for 3 operation [!, &, |] and for 4 operand [bot, true, false, top]....	
    
    	//**************************************************
				 //Relational Operations	
    	//**************************************************
			i=0;  // BOOL[0]: defines  unary operation "!" 			
		//**************************************************
			BOOL[i]=new String[1][4]; 
						
			BOOL[i][0][0]="bot";   // !bot	 = bot					
			BOOL[i][0][1]="false"; // !true	 = false
			BOOL[i][0][2]="true";  // !false = true
			BOOL[i][0][3]="top";   // !top	 = top
			

		//**************************************************
			i=1;  // BOOL[1]: defines binary operation "&"			
		//**************************************************
			BOOL[i]=new String[4][4]; 
			
			
			j=0;  // for botom
			BOOL[i][j][0]="bot";						
			BOOL[i][j][1]="bot";
			BOOL[i][j][2]="false";
			BOOL[i][j][3]="bot";
						 
			j=1;  // for true
			BOOL[i][j][0]="bot";						
			BOOL[i][j][1]="true";
			BOOL[i][j][2]="false";
			BOOL[i][j][3]="top";
			
			j=2;  // for false
			BOOL[i][j][0]="false";						
			BOOL[i][j][1]="false";
			BOOL[i][j][2]="false";
			BOOL[i][j][3]="false";
			
			j=3;  // for top
			BOOL[i][j][0]="bot";							
			BOOL[i][j][1]="top";
			BOOL[i][j][2]="false";
			BOOL[i][j][3]="top";
			

			
		//**************************************************
			i=2;  // BOOL[2]: defines binary operation "|"				
		//**************************************************
			BOOL[i]=new String[4][4]; 
			
			j=0;  // for botom
			BOOL[i][j][0]="bot";						
			BOOL[i][j][1]="true";
			BOOL[i][j][2]="bot";
			BOOL[i][j][3]="top";
						 
			j=1;  // for true
			BOOL[i][j][0]="true";						
			BOOL[i][j][1]="true";
			BOOL[i][j][2]="true";
			BOOL[i][j][3]="true";
			
			j=2;  // for false
			BOOL[i][j][0]="bot";						
			BOOL[i][j][1]="true";
			BOOL[i][j][2]="false";
			BOOL[i][j][3]="top";
			
			j=3;  // for top
			BOOL[i][j][0]="bot";							
			BOOL[i][j][1]="true";
			BOOL[i][j][2]="false";
			BOOL[i][j][3]="top";
			
			
			return BOOL;	
    }
   	
   	
   	// LUB Table
   	String[][] getLUBtab()
    {
    		int i=-1;
    		String[][] BOOL=new String[4][4];
 
			i=0;  // for LUB(bottom, other)
			BOOL[i][0]="bot";						
			BOOL[i][1]="true";
			BOOL[i][2]="false";
			BOOL[i][3]="top";
						 
			i=1;  // for LUB(true, other)
			BOOL[i][0]="true";						
			BOOL[i][1]="true";
			BOOL[i][2]="top";
			BOOL[i][3]="top";
			
			i=2;  // for LUB(false, other)
			BOOL[i][0]="false";						
			BOOL[i][1]="top";
			BOOL[i][2]="false";
			BOOL[i][3]="top";
			
			i=3;  // for LUB(top, other)
			BOOL[i][0]="top";						
			BOOL[i][1]="top";
			BOOL[i][2]="top";
			BOOL[i][3]="top";
			
			return BOOL;
    }
    
    /* Sub Abstract Values Table
     * Sub_BOOL[0] contains sub abstract values for "bot"
     * Sub_BOOL[1] contains sub abstract values for "true"
     * Sub_BOOL[2] contains sub abstract values for "false" 
     * Sub_BOOL[3] contains sub abstract values for "top" */
     
    String[][] getSubValTab()
    {
    	String[][] Sub_BOOL={{"bot"}, {"true"}, {"false"}, {"true", "false"}};
		
		return Sub_BOOL;
    }	
}

