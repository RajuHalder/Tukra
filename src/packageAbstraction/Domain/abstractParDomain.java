/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "abstractParDomain" implements the interface specified by 
 * "packageAbstraction.DomainInterface.abstractDomain" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Domain;

import java.util.*;
import packageMyLibrary.*;

import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.Operator.*;
import packageAbstraction.Value.abstractParValue;
import packageAbstraction.ProgEnvironment.abstractEnvironment;


public class abstractParDomain extends myFunctions implements packageAbstraction.DomainInterface.abstractDomain, myPatterns
{
	
	// Returns the Top most Abstract Value of the Parity domain
	public abstractValue getTop() 
   	{
   		return 	(new abstractParValue("top"));	
   	}
   	
   	// Returns the Bottom most Abstract Value of the Parity domain
	public abstractValue getBottom() 
	{
		return 	(new abstractParValue("bot"));	
	}
	
	// Returns the LUB of a list of Abstract Values of the Parity domain
	public abstractValue getLUB(abstractValue[] list) 
	{
		if(list.length==0) // No Abstract Value in the list
		{
			System.out.println("abstractParDomain:getLUB():Its difficult to obtain LUB from a list of zero abstract parity element!!!");
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
			
			// Get encryption and LUB table in Parity domain
			ParOperation par_op=new ParOperation();
			String[][] EncTab=par_op.getEncTab();
   			String[][] LUBtab=par_op.getLUBtab();
			
			int index_val1=getIndex(val1.getProperty()[0], EncTab[0]); //Get index of the first abstract parity value from the encryption table
	   		int index_val2=getIndex(val2.getProperty()[0], EncTab[0]); //Get index of the second abstract parity value from the encryption table
   		
			String res=LUBtab[index_val1][index_val2]; //extract the result of LUB(val1, val2) from the LUB table
			return (new abstractParValue(res));        // Return the resultant abstract parity value
		}
	}
	
	
	// Returns the GLB of a list of Abstract Values of the Parity domain
	// abstractValue getGLB(abstractValue[] list);
	
	
	// Returns the set of atomic parity values whose LUB is "val" in the parameter
	public abstractValue[] getAtoms(abstractValue val)
	{
		
		LinkedList list=new LinkedList();
		addAtoms2List(val, list);// Function call that returns a list of atomic parity values of "val"
	
		//Converting "list" to an array of atomic parity values	
		Object[] arr=list.toArray();
		abstractValue[] result=new abstractParValue[arr.length];
		for(int i=0;i<arr.length;i++)
		{
			result[i]=(abstractParValue)arr[i];	
		}
		
		return result; //Returning the array of atomic parity values
	}
	
	
	// Recursive function to determining a list of atomic parity values of "val"
	private void addAtoms2List(abstractValue val, LinkedList list)
	{
		if(val.isAtomicValue()) // "val" is atomic, so add to the list
		{
			list.addLast(val);
			return;	
		}
		else // "val" is composite
		{
			
			// Function call that returns sub-abstract values just one level below the Parity lattice
			abstractValue[] SubVal=getSubValues(val);  
						
			for(int i=0;i<SubVal.length;i++)
			{
				addAtoms2List(SubVal[i], list); // Recursively call for all sub-abstract values
			}
		}
	}
	
	// Returns the set of parity abstract values just one level below in the Parity lattice whose LUB is "val" in the parameter
	public abstractValue[] getSubValues(abstractValue val) 
	{
		// Get encryption and sub-value tables in Parity domain
		ParOperation par_op=new ParOperation();
		String[][] EncTab=par_op.getEncTab();
		String[][] SubValTab=par_op.getSubValTab();
		
		// Get index of the "val" from the encryption table
		int index_val=getIndex(val.getProperty()[0], EncTab[0]);
		
		// Extract the array of sub-abstract parity values cooresponding to the index from the table that stores sub-abstract parity values
		String[] vals=SubValTab[index_val];
		
		abstractParValue[] result=new abstractParValue[vals.length]; 
		for(int i=0;i<vals.length;i++)
		{
			result[i]=new abstractParValue(vals[i]);
		}
		
		return result;
	}
	
	
	// Performs abstract evaluation of unary arithmatic operation
	public abstractValue evaluate(abstractArithOperator ArithUnaryOp, abstractValue val) 
	{
		// Get encryption and arithmatic operational table in Parity domain
		ParOperation par_op=new ParOperation();
		String[][][] OpTab=par_op.getArithOpTab();
		String[][] EncTab=par_op.getEncTab();
		
		// Get index of the "val" from the encryption table
   		int index_val=getIndex(val.getProperty()[0], EncTab[0]); 
   		
   		// Get index of the "ArithUnaryOp" from the encryption table   		
   		int index_op=getIndex(ArithUnaryOp.getOperator(), EncTab[1]);
		
		if(index_val!=-1 && index_op!=-1)
		{
			String result=OpTab[0][index_op][index_val]; //extract the result from the operational table 
			return (new abstractParValue(result));
		}
		else // Wrong operators or values
		{
			System.out.println("abstractParDomain:evaluate():Either wrong arithmatic unary operator or wrong abstract par value!!!");
			return getBottom();
		}
	}
	
	
	// Performs abstract evaluation of binary arithmatic operation
	public abstractValue evaluate(abstractArithOperator ArithBinaryOp, abstractValue val1, abstractValue val2) 
	{
		
		// Get encryption and arithmatic operational table in Parity domain
		ParOperation par_op=new ParOperation();
		String[][][] OpTab=par_op.getArithOpTab();
		String[][] EncTab=par_op.getEncTab();
		
		// Get index of the "val1" and "val2" from the encryption table
   		int index_val1=getIndex(val1.getProperty()[0], EncTab[0]);
   		int index_val2=getIndex(val2.getProperty()[0], EncTab[0]);
   		
   		// Get index of the "ArithBinaryOp" from the encryption table    
   		int index_op=getIndex(ArithBinaryOp.getOperator(), EncTab[2]);			
		
		if(index_val1!=-1 && index_val2!=-1 && index_op!=-1)
		{
			//extract the result from the operational table
			String result=OpTab[index_op+1][index_val1][index_val2];
			return (new abstractParValue(result));
		}
		else // Wrong operators or values
		{
			System.out.println("abstractParDomain:evaluate():Either wrong arithmatic binary operator or wrong abstract par value!!!");
			return getBottom();
		}
	}
	
	// Performs abstract evaluation of relational operation: returns "true"/"false"/"top" as string
	public String evaluate(abstractRelOperator RelOp, abstractValue val1, abstractValue val2)
	{
		// Get encryption and relational operational table in Parity domain
		ParOperation par_op=new ParOperation();
		String[][][] OpTab=par_op.getRelOpTab();
   		String[][] EncTab=par_op.getEncTab();
		
		// Get index of the "val1" and "val2" from the encryption table
   		int index_val1=getIndex(val1.getProperty()[0], EncTab[0]);   			
   		int index_val2=getIndex(val2.getProperty()[0], EncTab[0]); 
   		
   		// Get index of the "RelOp" from the encryption table      		
   		int index_op=getIndex(RelOp.getOperator(), EncTab[3]);
		
		if(index_val1!=-1 && index_val2!=-1 && index_op!=-1)
		{
			//extract the result from the operational table
			String result=OpTab[index_op][index_val1][index_val2];
			return result;
		}
		else // Wrong operators or values
		{
			System.out.println("abstractParDomain:evaluate():Either wrong relational operator or wrong abstract par value!!!");
			return "bot";
		}
	}
	
	// Performs abstract evaluation of unary boolean operation NOT
	public abstractValue evaluate(abstractBoolOperator BoolUnaryOp, abstractValue val)
	{
		return getBottom(); // Hey! Its a Parity domain
	}
	
	// Performs abstract evaluation of binary boolean operation AND/OR
	public abstractValue evaluate(abstractBoolOperator BoolBinaryOp, abstractValue val1, abstractValue val2)
	{
		return getBottom(); // Hey! Its a Parity domain
	}
	   	
	// Substitutes concrete variables and concrete operations in the postfix expression "PostfixExpr" by the abstract values
	// and abstract operations from abstract environment "absEnv", and obtains an abstract posfix expression "AbstractExprList" 
	public void getAbstractExpressionList(String PostfixExpr, abstractEnvironment absEnv, LinkedList ExprList)
	{
		
		// Iterating for all tokens in the concrete expression
		StringTokenizer st = new StringTokenizer(PostfixExpr, " ");
      	while(st.hasMoreTokens())
     	{
     		String token=st.nextToken().trim();
     		
     		if(token.equals("u-")||token.equals("+")||token.equals("-")||token.equals("*")||token.equals("/"))
     		{
     			// Substituting concrete arithmatic operations by abstract versions
     			abstractArithOperator ao=new abstractArithOperator(token);
     			ExprList.addLast(ao);	
     		}
     		else if(token.equals("?")) 
			{
				// Substituting concrete input values by top most abstract value
     			ExprList.addLast(getTop());	
			}
			else if(java.util.regex.Pattern.matches(REGEX_INTEGER, token))
			{
				// Substituting concrete integer values by corresponding abstract parity values
				ExprList.addLast(new abstractParValue(Integer.parseInt(token)));
			}
			else if(java.util.regex.Pattern.matches(REGEX_FLOAT, token))
			{
				// Substituting concrete float values by corresponding abstract parity values
				Float flt=new Float(token);
				ExprList.addLast(new abstractParValue(flt.floatValue()));
			}
			else if(java.util.regex.Pattern.matches(REGEX_VARIABLE, token))
			{
				// Substituting concrete variables by abstract parity values from the abstract environment
				ExprList.addLast(absEnv.getVariableValue(token));
			}
     	} 		
	}
}


//***************** Operation Table for PARITY *****************************************************

class ParOperation
{

	/* Encryption Table
	 * Enc_PAR[0]: for abstract values
	 * Enc_PAR[1]: for abstract uniary operations
	 * Enc_PAR[2]: for abstract binary operations 
	 * Enc_PAR[3]: for abstract relational operations */
    
    String[][] getEncTab()
    {
   		String[][] Enc_PAR={{"bot", "even", "odd", "top"},{"u-"},{"+", "-", "*", "/"},{"==", "!=", "<", "<=", ">", ">="}};
   		return Enc_PAR;
    }
    
    
    // Arithmatic Operational Table
    String[][][] getArithOpTab()
    {

     	int i=-1;
     	int j=-1;
     	int k=-1;
     	
		String[][][] PAR=new String[5][][]; //for 5 operation [u-, +, -, *, /] and for 4 operand [bot, even, odd, top]....	
						
		//**************************************************
					//Arithmatic Operations	
		//**************************************************
					i=0;  // PAR[0]: defines  unary operation "u-" 
		//**************************************************
			PAR[i]=new String[1][4]; 			
			
			PAR[i][0][0]="bot";	  // u-(bot) = bot					
			PAR[i][0][1]="even";  // u-(even) = even
			PAR[i][0][2]="odd";   // u-(odd) = odd
			PAR[i][0][3]="top";   // u-(top) = top
			

		//**************************************************
					i=1;  // PAR[1]: defines binary operation "+"				
		//**************************************************
			PAR[i]=new String[4][4]; 
			 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="even";
			PAR[i][j][2]="odd";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="odd";
			PAR[i][j][2]="even";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			

		//**************************************************
					i=2;  // PAR[2]: defines binary operation "-"			
		//**************************************************
			PAR[i]=new String[4][4]; 
			 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="even";
			PAR[i][j][2]="odd";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="odd";
			PAR[i][j][2]="even";
			PAR[i][j][3]="top";
			
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
		//**************************************************
					i=3;  // PAR[3]: defines binary operation "*"				
		//**************************************************
			PAR[i]=new String[4][4]; 
			
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="even";
			PAR[i][j][2]="even";
			PAR[i][j][3]="even";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="even";
			PAR[i][j][2]="odd";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="even";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			

		//**************************************************
					i=4;  // PAR[4]: defines binary operation "/"			
		//**************************************************
		 	PAR[i]=new String[4][4]; 
		 	
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			return PAR;
    }
    
    // Relational Operation Table
    String[][][] getRelOpTab()
    {

     	int i=-1;
     	int j=-1;
     	int k=-1;
     	

		String[][][] PAR=new String[6][4][4]; //for 6 operation [==, != , <, <=, >, >=] and for 4 operand [bot, even, odd top]....	
    
    	//**************************************************
				 //Relational Operations	
    	//**************************************************
			i=0;  // PAR[0]: defines  relational operation "=="			
		//**************************************************
			 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			

		//**************************************************
			i=1;  // PAR[1]: defines  relational operation "!="		
		//**************************************************
			 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
		//**************************************************
			i=2;  // PAR[2]: defines  relational operation "<"			
		//**************************************************
			 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";

		//**************************************************
			i=3;  // PAR[3]: defines  relational operation "<="				
		//**************************************************
		 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
		//**************************************************
			i=4;  // PAR[4]: defines  relational operation ">"				
		//**************************************************
		 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
		//**************************************************
			i=5;  // PAR[5]: defines  relational operation ">="			
		//**************************************************
		 
			j=0;  // for botom
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="bot";
			PAR[i][j][2]="bot";
			PAR[i][j][3]="bot";
						 
			j=1;  // for even
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=2;  // for odd
			PAR[i][j][0]="bot";						
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			j=3;  // for top
			PAR[i][j][0]="bot";							
			PAR[i][j][1]="top";
			PAR[i][j][2]="top";
			PAR[i][j][3]="top";
			
			return PAR;

    }
    
    
    // LUB Table
    String[][] getLUBtab()
    {
    		int i=-1;
    		String[][] PAR=new String[4][4];
 
			i=0;  // for LUB(bottom, other)
			PAR[i][0]="bot";						
			PAR[i][1]="even";
			PAR[i][2]="odd";
			PAR[i][3]="top";
						 
			i=1;  // for LUB(even, other)
			PAR[i][0]="even";						
			PAR[i][1]="even";
			PAR[i][2]="top";
			PAR[i][3]="top";
			
			i=2;  // for LUB(odd, other)
			PAR[i][0]="odd";						
			PAR[i][1]="top";
			PAR[i][2]="odd";
			PAR[i][3]="top";
					
			i=3;  // for LUB(top, other)
			PAR[i][0]="top";						
			PAR[i][1]="top";
			PAR[i][2]="top";
			PAR[i][3]="top";
			
			return PAR;
    }
    
    
    /* Sub Abstract Values Table
     * Sub_PAR[0] contains sub abstract values for "bot"
     * Sub_PAR[1] contains sub abstract values for "even"
     * Sub_PAR[2] contains sub abstract values for "odd" 
     * Sub_PAR[3] contains sub abstract values for "top" */
         
    String[][] getSubValTab()
    {
    	String[][] Sub_PAR={{"bot"}, {"even"}, {"odd"}, {"even", "odd"}};
		
		return Sub_PAR;
    }
}

