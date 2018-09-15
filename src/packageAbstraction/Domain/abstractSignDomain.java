/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 *
 * Class "abstractSignDomain" implements the interface specified by 
 * "packageAbstraction.DomainInterface.abstractDomain" 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageAbstraction.Domain;

import java.util.*;
import packageMyLibrary.*;

import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.Operator.*;
import packageAbstraction.Value.abstractSignValue;
import packageAbstraction.ProgEnvironment.abstractEnvironment;



public class abstractSignDomain extends myFunctions implements packageAbstraction.DomainInterface.abstractDomain, myPatterns
{
	// Returns the Top most Abstract Value of the Sign domain
	public abstractValue getTop() 
   	{
   		return 	(new abstractSignValue("top"));	
   	}
   	
   	// Returns the Bottom most Abstract Value of the Sign domain
	public abstractValue getBottom() 
	{
		return 	(new abstractSignValue("bot"));	
	}
	
	// Returns the LUB of a list of Abstract Values of the Sign domain
	public abstractValue getLUB(abstractValue[] list) 
	{
		if(list.length==0) // No Abstract Value in the list
		{
			System.out.println("abstractSignDomain:getLUB():Its difficult to obtain LUB from a list of zero abstract sign element!!!");
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
			
			// Get encryption and LUB table in Sign domain
			SignOperation sign_op=new SignOperation();
			String[][] EncTab=sign_op.getEncTab();
   			String[][] LUBtab=sign_op.getLUBtab();
			
			int index_val1=getIndex(val1.getProperty()[0], EncTab[0]); //Get index of the first abstract sign value from the encryption table
	   		int index_val2=getIndex(val2.getProperty()[0], EncTab[0]); //Get index of the second abstract sign value from the encryption table
   		
			String res=LUBtab[index_val1][index_val2]; //extract the result of LUB(val1, val2) from the LUB table
			return (new abstractSignValue(res));       // Return the resultant abstract sign value
		}
	}
	
	// Returns the GLB of a list of Abstract Values of the Sign domain
	// abstractValue getGLB(abstractValue[] list);
	
	
	// Returns the set of atomic sign values whose LUB is "val" in the parameter
	public abstractValue[] getAtoms(abstractValue val) 
	{
		
		LinkedList list=new LinkedList();
		addAtoms2List(val, list);// Function call that returns a list of atomic sign values of "val"
	
		//Converting "list" to an array of atomic sign values	
		Object[] arr=list.toArray();
		abstractValue[] result=new abstractSignValue[arr.length];
		for(int i=0;i<arr.length;i++)
		{
			result[i]=(abstractSignValue)arr[i];	
		}
		
		return result; //Returning the array of atomic sign values
	}
	
	
	// Recursive function to determining a list of atomic sign values of "val"
	private void addAtoms2List(abstractValue val, LinkedList list)
	{
		if(val.isAtomicValue()) // "val" is atomic, so add to the list
		{
			list.addLast(val);
			return;	
		} 
		else // "val" is composite
		{
			
			// Function call that returns sub-abstract values just one level below the Sign lattice
			abstractValue[] SubVal=getSubValues(val); 
						
			for(int i=0;i<SubVal.length;i++)
			{
				addAtoms2List(SubVal[i], list); // Recursively call for all sub-abstract values
			}
		}
	}
	
	// Returns the set of sign abstract values just one level below in the Sign lattice whose LUB is "val" in the parameter
	public abstractValue[] getSubValues(abstractValue val) 
	{
		// Get encryption and sub-value tables in Sign domain
		SignOperation sign_op=new SignOperation();
		String[][] EncTab=sign_op.getEncTab();
		String[][] SubValTab=sign_op.getSubValTab();
		
		// Get index of the "val" from the encryption table
		int index_val=getIndex(val.getProperty()[0], EncTab[0]);
		
		// Extract the array of sub-abstract sign values cooresponding to the index from the table that stores sub-abstract sign values
		String[] vals=SubValTab[index_val];
		
		abstractSignValue[] result=new abstractSignValue[vals.length]; 
		for(int i=0;i<vals.length;i++)
		{
			result[i]=new abstractSignValue(vals[i]);
		}
		
		return result;
	}
	
	// Performs abstract evaluation of unary arithmatic operation
	public abstractValue evaluate(abstractArithOperator ArithUnaryOp, abstractValue val) 
	{
		// Get encryption and arithmatic operational table in Sign domain
		SignOperation sign_op=new SignOperation();
		String[][][] OpTab=sign_op.getArithOpTab();
		String[][] EncTab=sign_op.getEncTab();
		
		// Get index of the "val" from the encryption table
   		int index_val=getIndex(val.getProperty()[0], EncTab[0]); 
   		
   		// Get index of the "ArithUnaryOp" from the encryption table   		
   		int index_op=getIndex(ArithUnaryOp.getOperator(), EncTab[1]);
		
		if(index_val!=-1 && index_op!=-1)
		{
			String result=OpTab[0][index_op][index_val]; //extract the result from the operational table 
			return (new abstractSignValue(result));
		}
		else // Wrong operators or values
		{
			System.out.println("abstractSignDomain:evaluate():Either wrong arithmatic unary operator or wrong abstract sign value!!!");
			return getBottom();
		}
	}
	
	
	// Performs abstract evaluation of binary arithmatic operation
	public abstractValue evaluate(abstractArithOperator ArithBinaryOp, abstractValue val1, abstractValue val2) 
	{
		
		// Get encryption and arithmatic operational table in Sign domain
		SignOperation sign_op=new SignOperation();
		String[][][] OpTab=sign_op.getArithOpTab();
		String[][] EncTab=sign_op.getEncTab();
		
		// Get index of the "val1" and "val2" from the encryption table
   		int index_val1=getIndex(val1.getProperty()[0], EncTab[0]);
   		int index_val2=getIndex(val2.getProperty()[0], EncTab[0]);
   		
   		// Get index of the "ArithBinaryOp" from the encryption table    
   		int index_op=getIndex(ArithBinaryOp.getOperator(), EncTab[2]);			
		
		if(index_val1!=-1 && index_val2!=-1 && index_op!=-1)
		{
			//extract the result from the operational table
			String result=OpTab[index_op+1][index_val1][index_val2];
			return (new abstractSignValue(result));
		}
		else // Wrong operators or values
		{
			System.out.println("abstractSignDomain:evaluate():Either wrong arithmatic binary operator or wrong abstract sign value!!!");
			return getBottom();
		}
	}
	
	// Performs abstract evaluation of relational operation: returns "true"/"false"/"top" as string
	public String evaluate(abstractRelOperator RelOp, abstractValue val1, abstractValue val2)
	{
		// Get encryption and relational operational table in Sign domain
		SignOperation sign_op=new SignOperation();
		String[][][] OpTab=sign_op.getRelOpTab();
   		String[][] EncTab=sign_op.getEncTab();
		
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
			System.out.println("abstractSignDomain:evaluate():Either wrong relational operator or wrong abstract sign value!!!");
			return "bot";
		}
	}
	
	// Performs abstract evaluation of unary boolean operation NOT
	public abstractValue evaluate(abstractBoolOperator BoolUnaryOp, abstractValue val)
	{
		return getBottom(); // Hey! Its a Sign domain
	}
	
	// Performs abstract evaluation of binary boolean operation AND/OR
	public abstractValue evaluate(abstractBoolOperator BoolBinaryOp, abstractValue val1, abstractValue val2)
	{
		return getBottom(); // Hey! Its a Sign domain
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
				// Substituting concrete integer values by corresponding abstract sign values
				ExprList.addLast(new abstractSignValue(Integer.parseInt(token)));
			}
			else if(java.util.regex.Pattern.matches(REGEX_FLOAT, token))
			{
				// Substituting concrete float values by corresponding abstract sign values
				Float flt=new Float(token);
				ExprList.addLast(new abstractSignValue(flt.floatValue()));
			}
			else if(java.util.regex.Pattern.matches(REGEX_VARIABLE, token))
			{
				// Substituting concrete variables by abstract sign values from the abstract environment
				ExprList.addLast(absEnv.getVariableValue(token));
			}
     	} 		
	}
}


//***************** Operaion Table for SIGN *****************************************************

class SignOperation
{

    /* Encryption Table
	 * Enc_SIGN[0]: for abstract values
	 * Enc_SIGN[1]: for abstract uniary operations
	 * Enc_SIGN[2]: for abstract binary operations 
	 * Enc_SIGN[3]: for abstract relational operations */
    String[][] getEncTab()
    {
   		String[][] Enc_SIGN={{"bot", "zero", "neg", "pos", "top"},{"u-"},{"+", "-", "*", "/"},{"==", "!=", "<", "<=", ">", ">="}};
   		return Enc_SIGN;
    }
    
    
    // Arithmatic Operational Table
    String[][][] getArithOpTab()
    {

     	int i=-1;
     	int j=-1;
     	int k=-1;
     	
		String[][][] SIGN=new String[5][][]; //for 5 operation [u-, +, -, *, /] and for 5 operand [bot, zero, negative, positive, top]....	
						
		//**************************************************
					//Arithmatic Operations	
		//**************************************************
					i=0;  // SIGN[0]: defines  unary operation "u-" 
		//**************************************************
			SIGN[i]=new String[1][5]; 			
			
			SIGN[i][0][0]="bot";  // u-(bot) = bot							
			SIGN[i][0][1]="zero"; // u-(zero) = zero		
			SIGN[i][0][2]="pos";  // u-(neg) = pos		
			SIGN[i][0][3]="neg";  // u-(pos) = neg		
			SIGN[i][0][4]="top";  // u-(bot) = bot		
			

		//**************************************************
					i=1;  // SIGN[1]: defines  unary operation "+" 			
		//**************************************************
			SIGN[i]=new String[5][5]; 
			 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="zero";
			SIGN[i][j][2]="neg";
			SIGN[i][j][3]="pos";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="neg";
			SIGN[i][j][2]="neg";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="pos";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="pos";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			

		//**************************************************
					i=2;  // SIGN[2]: defines  unary operation "-" 			
		//**************************************************
			SIGN[i]=new String[5][5]; 
			 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="zero";
			SIGN[i][j][2]="pos";
			SIGN[i][j][3]="neg";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="neg";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="neg";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="pos";
			SIGN[i][j][2]="pos";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
		//**************************************************
					i=3;  // SIGN[3]: defines  unary operation "*" 			
		//**************************************************
			SIGN[i]=new String[5][5]; 
			
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="zero";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="zero";						
			SIGN[i][j][1]="zero";
			SIGN[i][j][2]="zero";
			SIGN[i][j][3]="zero";
			SIGN[i][j][4]="zero";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="zero";
			SIGN[i][j][2]="pos";
			SIGN[i][j][3]="neg";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="zero";
			SIGN[i][j][2]="neg";
			SIGN[i][j][3]="pos";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="zero";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";

		//**************************************************
					i=4;  // SIGN[4]: defines  unary operation "/" 			
		//**************************************************
		 	SIGN[i]=new String[5][5]; 
		 	
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="zero";
			SIGN[i][j][3]="zero";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			return SIGN;
    }
    
    // Relational Operation Table
    String[][][] getRelOpTab()
    {

     	int i=-1;
     	int j=-1;
     	int k=-1;
     	

		String[][][] SIGN=new String[6][5][5]; //for 6 operation [==, != , <, <=, >, >=] and for 5 operand [bot, zero, minus, pos, top]....	
    
    	//**************************************************
				 //Relational Operations	
    	//**************************************************
			i=0;  // SIGN[0]: defines  relational operation "=="			
		//**************************************************
			 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="false";
			SIGN[i][j][3]="false";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="false";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="false";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			

		//**************************************************
			i=1;  // SIGN[1]: defines  relational operation "!="			
		//**************************************************
			 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="true";
			SIGN[i][j][3]="true";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="true";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="true";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
		//**************************************************
			i=2;  // SIGN[2]: defines  relational operation "<"			
		//**************************************************
			 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="false";
			SIGN[i][j][3]="true";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="true";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="false";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";

		//**************************************************
			i=3;  // SIGN[3]: defines  relational operation "<="	
		//**************************************************
		 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="false";
			SIGN[i][j][3]="true";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="true";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="false";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
		//**************************************************
			i=4;  // SIGN[4]: defines  relational operation ">"
		//**************************************************
		 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="true";
			SIGN[i][j][3]="false";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="false";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="true";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
		//**************************************************
			i=5;  // SIGN[5]: defines  relational operation ">="		
		//**************************************************
		 
			j=0;  // for botom
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="bot";
			SIGN[i][j][2]="bot";
			SIGN[i][j][3]="bot";
			SIGN[i][j][4]="bot";
						 
			j=1;  // for zero
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="true";
			SIGN[i][j][3]="false";
			SIGN[i][j][4]="top";
			
			j=2;  // for negative
			SIGN[i][j][0]="bot";						
			SIGN[i][j][1]="false";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="false";
			SIGN[i][j][4]="top";
			
			j=3;  // for positive
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="true";
			SIGN[i][j][2]="true";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			j=4;  // for top
			SIGN[i][j][0]="bot";							
			SIGN[i][j][1]="top";
			SIGN[i][j][2]="top";
			SIGN[i][j][3]="top";
			SIGN[i][j][4]="top";
			
			return SIGN;

    }
    
    // LUB Table
    String[][] getLUBtab()
    {
    		int i=-1;
    		String[][] SIGN=new String[5][5];
 
			i=0;  // for LUB(bottom, other)
			SIGN[i][0]="bot";						
			SIGN[i][1]="zero";
			SIGN[i][2]="neg";
			SIGN[i][3]="pos";
			SIGN[i][4]="top";
						 
			i=1;  // for LUB(zero, other)
			SIGN[i][0]="zero";						
			SIGN[i][1]="zero";
			SIGN[i][2]="top";
			SIGN[i][3]="top";
			SIGN[i][4]="top";
			
			i=2;  // for LUB(neg, other)
			SIGN[i][0]="neg";						
			SIGN[i][1]="top";
			SIGN[i][2]="neg";
			SIGN[i][3]="top";
			SIGN[i][4]="top";
			
			i=3;  // for LUB(pos, other)
			SIGN[i][0]="pos";						
			SIGN[i][1]="top";
			SIGN[i][2]="top";
			SIGN[i][3]="pos";
			SIGN[i][4]="top";
			
			i=4;  // for LUB(top, other)
			SIGN[i][0]="top";						
			SIGN[i][1]="top";
			SIGN[i][2]="top";
			SIGN[i][3]="top";
			SIGN[i][4]="top";
			
			return SIGN;
    }
    
    
     /* Sub Abstract Values Table
     * Sub_SIGN[0] contains sub abstract values for "bot"
     * Sub_SIGN[1] contains sub abstract values for "zero"
     * Sub_SIGN[2] contains sub abstract values for "neg" 
     * Sub_SIGN[3] contains sub abstract values for "pos"
     * Sub_SIGN[4] contains sub abstract values for "top" */
    String[][] getSubValTab()
    {
    	String[][] Sub_SIGN={{"bot"}, {"zero"}, {"neg"}, {"pos"}, {"zero", "neg", "pos"}};
		
		return Sub_SIGN;
    }
}

