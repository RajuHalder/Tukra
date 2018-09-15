/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This CLASS defines the methods that evaluate expressions (arithmatic and boolean) in an abstract domain
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageEvalExpr;

import javax.swing.JOptionPane;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.regex.*;

import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.Operator.*;
import packageAbstraction.Value.*;
import packageAbstraction.DomainInterface.*;
import packageAbstraction.Domain.*;
import packageAbstraction.ProgEnvironment.*;

import packageMyLibrary.*;


public class evaluateExpr implements myPatterns
{

    // Evaluating arithmatic expressions
    public abstractValue evaluateArithExpr(String Expr, abstractEnvironment absEnv, abstractDomain ADobj) 
    {
	          	   	
    	// Need to separate the tokens in "Expr" by spaces
    	String InfixExpr = parseArithExpr(Expr);
    	
    	// Converting infix into postfix...
      	stack myStack=new stack();
	   	String PostfixExpr=doArithTrans(InfixExpr, myStack); 
	    	    
	   	
	   	// Replacing concrete postfix expression into abstract postfix expression....
	    LinkedList abstractExpr=new LinkedList();
	    ADobj.getAbstractExpressionList(PostfixExpr, absEnv, abstractExpr);
	    
	    // Evaluating abstract postfix expression....	    
	   	abstractValue l_operand=null;
     	abstractValue r_operand=null;
     	abstractOperator op=null;
      	
      	Stack stk=new Stack();
      	
      	// Iterating over abstract postfix expression
     	ListIterator itr = abstractExpr.listIterator(); 
     	while(itr.hasNext())
     	{
     		abstractElement token=(abstractElement)itr.next(); //Extracting abstract elements in the postfix expression....   
     		 		
     		if(token.isOperator()) //If the abstract element is operator
     		{
     			
     			abstractArithOperator oper=(abstractArithOperator)token;
     			
     			if(oper.isArithBinaryOperator()) // If it is arithmatic binary operator
     			{
     				//Extract two top elements from the stack.....
     				if(! stk.isEmpty())
     					r_operand = (abstractValue)stk.pop();
     				if(! stk.isEmpty())
		    			l_operand = (abstractValue)stk.pop();
		      		
		      		//Perform the abstract binary arithmatic operation between two abstract elements
		      		abstractValue result = ADobj.evaluate(oper, l_operand, r_operand);
		      		
		      		//Push the abstract result again onto the stack.....
		      		stk.push(result);
      			}
      			else if(oper.isArithUnaryOperator()) // If it is arithmatic unary operator
      			{
      				//Extract one top element from the stack.....
      				if(! stk.isEmpty())
      					r_operand = (abstractValue)stk.pop();  
      				
      				//Perform the abstract unary arithmatic operation on the element....
      				abstractValue result = ADobj.evaluate(oper, r_operand);
      				
      				//Push the abstract result again onto the stack
      				stk.push(result);    			
      			}
      			     
      		}
      		else if(token.isValue()) // If the abstract element is abstract value
      		{
      			//Push the abstract value onto the stack.....
      			abstractValue val=(abstractValue)token;      				
      			stk.push(val);
      		}
      	}
      	
      	//Extacting the resultant abstract value from the stack
      	abstractValue result=null;	
      	if(! stk.isEmpty())
      	{
      		result=(abstractValue)stk.pop();      	
      		System.out.println("packageEvalExpr.evaluateExpr.evaluateArithExpr(): The End.");      	     	
      	}
      	else
      	{
      		System.out.println("packageEvalExpr.evaluateExpr.evaluateArithExpr(): stack is empty, it does not contain result!!!!");   
      	}
      	
      	return result;
   	}
   	
   	
   	// Evaluating relational expressions
   	public String evaluateRelExpr(String expr, abstractEnvironment absEnv, abstractDomain ADobj) 
    {       
    		    
	    String l_expr="";
		String r_expr="";
		String relOp="";
				
		Matcher matArithExpr=patrn_arith_expr.matcher(expr);
		if(matArithExpr.find())
		{
			l_expr=matArithExpr.group(); // Extracting left arithmatic expression from "expr"
		}
				
		if(matArithExpr.find())
		{				
			r_expr=matArithExpr.group(); // Extracting right arithmatic expression from "expr"
		}
		
		// Extracting relational operator from the expression "expr"	
		Pattern patrn_rel_op=Pattern.compile(REGEX_REL_OPERATION);	
		Matcher matRelOp=patrn_rel_op.matcher(expr); 
		if(matRelOp.find())
		{
			relOp=matRelOp.group();
		}
		
		// Evaluating left and right arithmatic expression in the abstract domain		
		abstractValue res_1= evaluateArithExpr(l_expr, absEnv, ADobj);
		abstractValue res_2= evaluateArithExpr(r_expr, absEnv, ADobj);
					
	    // Evaluating abstract relational operation	between two abstract values			
	    abstractRelOperator oper=new abstractRelOperator(relOp);
		String result=ADobj.evaluate(oper, res_1, res_2);
		
		System.out.println("packageEvalExpr.evaluateExpr.evaluateRelExpr(): The End.");
      	     	
		return result;					
	}
   	
   	
   	
   	// Parsing arithmatic infix expression, and returning in a form where tokens are separated by spaces
   	String parseArithExpr(String Expr)
	{
		
		if(!Expr.equals("")) 
		{
			Expr=Expr.replaceAll(" ", ""); // Removing all unneccessary spaces in the expression
		}
		
		if(Expr.equals("") || Expr.equals("?") )
		{
			return Expr; // expression is empty or run-time input
		}
		else if(java.util.regex.Pattern.matches(REGEX_FLOAT , Expr) || java.util.regex.Pattern.matches(REGEX_INTEGER, Expr))
		{
			return Expr; // expression is float or integer value
		}
		else if(java.util.regex.Pattern.matches(REGEX_VARIABLE, Expr))
		{
			return Expr; // expression is a single program variable
		}
		else if(Expr.charAt(0)=='(') 
		{
			// If "expr" started with "(", then extract sub-expression inside the braces "()"
			 
			int index=-1;
			String braces_list="null";
			
			int i=1;
			while(i<=Expr.length()-1)
			{
				char c=Expr.charAt(i);			
				if(c=='(') // Additional "(" found
				{
					// Add "(" to "braces_list"
					braces_list=braces_list + "_" + c;		
				}
				else if(c==')') // End of braces ")" found	
				{
					if(! braces_list.equals("null"))  // Additional "(" exist in the "braces_list"?
					{
						// Remove "(" from "braces_list"
						braces_list=braces_list.substring(0, braces_list.lastIndexOf("_"));
					}
					else // No additional "(" exist in the "braces_list"?
					{
						// value in "index" represent the index of the end-bracket ")" of interest that is used to extract sub-expression
						index=i; 
						break;
					}
				}
				i++;
			}
			
			/* - Sub-expression in the braces "(" represent left expression, 
			 * - Next non-null character represents operator
			 * - Remaining part represent right expression 
			 * - Call the function "parseArithExpr(String)" recursively for all of these */
			String leftExpr="";
			String operator="";
			String rightExpr="";
			

			if(index>=1)
			{
				leftExpr=Expr.substring(1, index).trim(); //left expression is the sub-expression in the braces "()"
				Expr=Expr.substring(index+1).trim();
				
				if(Expr.length()!=0)
				{
					if(Expr.charAt(0)=='+' || Expr.charAt(0)=='-' || Expr.charAt(0)=='*' || Expr.charAt(0)=='/')
					{
						operator="" + Expr.charAt(0); // next non-null character is the operator
						rightExpr=Expr.substring(1); // Remaining of the expression is right expression
					}
					else
					{
						System.out.println("packageEvalExpr.evaluateExpr.parseArithExpr(): Expression Syntax is not ok, operator missing");	
					}
				}
				
				return (new String("(" + " " + parseArithExpr(leftExpr) + " " + ")" + " " + operator + " " + parseArithExpr(rightExpr)));
			}
			else
			{
				System.out.println("packageEvalExpr.evaluateExpr.parseArithExpr(): Expression Syntax is not ok");
				return Expr;
			}
		}
		else
		{	
			/* - The part on the left side of the first occurring operator represent left expression, 
			 * - The part on the right side of the first occurring operator represent right expression,
			 * - Call the function "parseArithExpr(String)" recursively for all of these */
				
			String leftExpr="";
			String operator="";
			String rightExpr="";
			
			int i=0;
			while(i<=Expr.length()-1)
			{
				char c=Expr.charAt(i++);			
				if(c!='+' && c!='-' && c!='*' && c!='/') // Not found operator?
				{
					leftExpr=leftExpr+c; // Extracting and adding characters in the left expression until we get operator 		
				}
				else
				{
					operator=operator+c; // Operator is found
					break;
				}
			}
			
			if(i<=Expr.length()-1)
			{
				rightExpr=Expr.substring(i, Expr.length()).trim(); // Remaining part is the right expression
			}
				
			
			if(leftExpr.equals("") && !operator.equals("")) // Left expression is missing
			{
				// considering the unary Operation "-" which we denote by "u-"
				operator="u-";
			}
			
			return (new String(" " + parseArithExpr(leftExpr) + " " + operator + " " + parseArithExpr(rightExpr)));				
		}
		
	}

   	

   	// Infix to postfix conversion for arithmatic expression
	public String doArithTrans(String inString, stack myStack) 
    {
     			
      	String outString=""; 
      	
      	StringTokenizer st = new StringTokenizer(inString, " "); // Tokens in the infix expression are seperated by spaces
      	while(st.hasMoreTokens())
     	{
     		String ch=st.nextToken(); // Extract each token in the infix expression
      		ch=ch.trim();
      		
			if(ch.equals("+") || ch.equals("-")) // Operator having "1" precedence found
      		{
      			// pushing operators in the stack based on their precedences
      			outString=gotArithOper(ch, 1, myStack, outString);
      		}
      		else if(ch.equals("*") || ch.equals("/")) // // Operator having "2" precedence found
      		{
      			// pushing operators in the stack based on their precedences
      			outString=gotArithOper(ch, 2, myStack,outString); 	
      		}
      		else if(ch.equals("u-")) // Operator having "3" precedence found
      		{
      			// pushing operators in the stack based on their precedences
      			outString=gotArithOper(ch, 3, myStack, outString);	
      		}
      		else if(ch.equals("(")) // got left parenthesis
      		{
      			// push into the stack
      			myStack.push(ch); 
      		}
      		else if(ch.equals(")")) // got right parenthesis
      		{
      			// pop and write to output until we get left parenthesis
      			outString=gotParen(myStack, outString);  			
      		}
      		else
      		{
      			if(outString.equals("")) // must be an operand and so write it to output
      			 	outString = outString + ch; 
      			else
      			 	outString = outString + " " + ch;	
      		}
      	}
       
       	while (!myStack.isEmpty()) // Write all the remaining elements in the stack to the output
      	{     		
      		if(outString.equals(""))
      			outString = outString + myStack.pop();
      		else
      		 	outString = outString + " " + myStack.pop();
      	}
	   		
	   	return outString; // Return postfix expression
	}
     
          
     
    // pushing or poping the stack based on the precedence of the incoming operators
	public String gotArithOper(String opThis, int prec1, stack myStack, String outString) 
    {
    	while (!myStack.isEmpty()) 
    	{
   			String opTop = myStack.pop(); // Pop-ing top-most element from the stack
   			
   			if (opTop.equals("(")) // Top-most element of the stack is "("
    		{
      			myStack.push(opTop);// do nothing
      			break;
      		}
      		else // Top-most element of the stack is an operator
      		{
      			// Assinging precedence to the top-most operator
      			int prec2;
      			if (opTop.equals("+") || opTop.equals("-"))
      				prec2 = 1;
      			else if(opTop.equals("*") || opTop.equals("/"))
      				prec2 = 2;
      			else
      				prec2 = 3;
      			
      			// precedence ofthe incoming operator is more than the precedence of the top-most operator?	
      			if (prec2 < prec1) // Yes
      			{
      				myStack.push(opTop); // do nothing
      				break;
      			} 
      			else // No
      			{
      				// Write the top-most operator to the output
      				if(outString.equals(""))
      					outString = outString + opTop;
      				else
      		 			outString = outString + " " +  opTop;
      		 	}
     		}
      	}
      	
      	// Finally push the incoming operator onto the stack		
      	myStack.push(opThis);
      	
      	return outString;
	}
      
      
    // Pop and write to the output until got beginning parenthesis "("
	public String gotParen(stack myStack, String outString)
    {
    	while (!myStack.isEmpty())
      	{
      		String chx = myStack.pop();
      		if (chx.equals("("))
      			break;
      		else
      		{
      			if(outString.equals(""))
      				outString = outString + chx;
      			else
      			 	outString = outString + " " +  chx;
     		}	
      	}
      	
      	return outString;
    }
      
   	
   	// Defining stack required to convert infix to postfix
   	class stack
   	{
    
    	LinkList list;      	
    	
    	stack() // Default constructor
    	{
    		list=new LinkList(); // New object of the type of "linklist"
    	}
    	
   		public void push(String j) 
   		{
   	  		list.insertFront(j);
   		}
  
      
   		public String pop() 
   		{
   	  		return list.deleteFront();
   		}
   		
   		public void clear()
   		{
   			list.doClear();
   		}
         
   		public boolean isEmpty() 
   		{
    	   	return (list.isEmpty());
		}
         
    	public void displayStack() 
    	{
      		System.out.print("Stack: ");
        	list.displayList();
    	}
    }
    
    // Class "linklist"     
    class LinkList 
    {
        private Link head;
      
        public LinkList() 
        {
        	head = null;
        }
         
        public boolean isEmpty()
        {
        	return (head == null);
        }
         
        public void insertFront(String d) 
        {
        	Link newLink = new Link(d);
        	newLink.next = head;
        	head = newLink;
        }
         
        public String deleteFront() 
        {
        	Link temp = head;
        	head = head.next;
        	return temp.data;
        }
        
        public void doClear()
        {
        	head=null;
        }
        
        public void displayList() 
        {
        	Link current = head;
        	while (current != null) 
        	{
       			current.displayLink();
        		current = current.next;  
  			}
        	System.out.println("");
        }
         
        // Class "Link"
        class Link 
        {
      		public String data; //data item       
      		public Link next;   //reference to next "Link" object
      		public Link(String d) 
      		{
      			data = d;
      		}
       
      		public void displayLink() 
      		{
      			System.out.print(data + " ");
      		}
      	}
    }
     
     
   	// To evaluate abstract boolean operation 
    public abstractValue evaluateBoolExpr(String Expr, abstractEnvironment absEnv, abstractDomain ADobj) 
    {
	      	   	
    	// Need to separate the tokens in boolean expression "Expr" by spaces
    	String infixExpr= parseBoolExpr(Expr, absEnv, ADobj);
      	
      	// Converting infix boolean expression to postfix boolean expression
      	stack myStack=new stack();
	   	String postfixExpr=doBoolTrans(infixExpr, myStack); 	     	
	     
	    // Replacing concrete postfix expression into abstract postfix expression....	
	    abstractBoolDomain ABoolobj=new abstractBoolDomain();	    
	    LinkedList abstractExpr=new LinkedList();
	    ABoolobj.getAbstractExpressionList(postfixExpr, absEnv, abstractExpr);
	    
	    // Evaluating abstract boolean postfix expression
	   	abstractValue l_operand=null;
     	abstractValue r_operand=null;
     	abstractOperator op=null;
      	
      	Stack stk=new Stack();
      	
      	// Iterating over abstract postfix expression
	    ListIterator itr = abstractExpr.listIterator();
     	while(itr.hasNext())
     	{
     		abstractElement token=(abstractElement)itr.next(); //Extracting abstract elements in the postfix expression 
     		    		
     		if(token.isOperator()) //If the abstract element is operator
     		{
     			abstractBoolOperator oper=(abstractBoolOperator)token;
     			
     			if(oper.isBoolBinaryOperator()) // If it is boolean binary operator
     			{
     				//Extract two top elements from the stack
     				if(! stk.isEmpty())
     					r_operand = (abstractValue)stk.pop();
     				if(! stk.isEmpty())
		    			l_operand = (abstractValue)stk.pop();
		      
		      		//Perform the abstract boolean binary operation between two abstract elements
		      		abstractValue result = ABoolobj.evaluate(oper, l_operand, r_operand);
		      		
		      		//Push the abstract result again onto the stack
		      		stk.push(result);
      			}
      			else if(oper.isBoolUnaryOperator()) // If it is boolean unary operator
      			{
      				//Extract one top element from the stack
      				if(! stk.isEmpty())
      					r_operand = (abstractValue)stk.pop();  
      				
      				//Perform the abstract boolean unary operation on the abstract element
      				abstractValue result = ABoolobj.evaluate(oper, r_operand);
      				
      				//Push the abstract result again onto the stack
      				stk.push(result);    			
      			}
      		}
      		else if(token.isValue()) //If the abstract element is abstract value
      		{
      			//Push the abstract value onto the stack
      			abstractValue val=(abstractValue)token;
      			stk.push(val);
      		}
      	}
      	
      	//Extacting the resultant abstract value from the stack
      	abstractValue result=(abstractValue)stk.pop();
      	System.out.println("packageEvalExpr.evaluateExpr.evaluateBoolExpr(): The End.");
      	     	
      	return result;
   	}     
    
    
    
   	// Parsing boolean infix command, and returning in a form where tokens are separated by spaces
   	String parseBoolExpr(String cmd, abstractEnvironment absEnv, abstractDomain ADobj)
	{
		
		String out="";
	 	   						
		cmd=cmd.replaceAll(" ", ""); // Removing all unneccessary spaces in the expression
			
		String expr="";
		while(cmd.length()!=0)
		{
			char c=cmd.charAt(0);
			cmd=cmd.substring(1);
		
			if(c== '&' || c== '|' || c=='!' || c=='(' || c==')') // operator or bracket found
			{
				if(expr.length()!=0) // "expr" is not empty string
				{
					
					if(expr.equals("true") || expr.equals("false")) // "expr" is default boolean value
					{
						out=out + " " + expr + " " + c; // Add spaces
						expr="";  // Reset "expr" to empty string
					}
					else  // "expr" is an relational expression
					{
						String boolResult=evaluateRelExpr(expr, absEnv, ADobj); // Evaluate in abstract domain 						
						out=out + " " + boolResult + " " + c; // Add spaces
						expr=""; // Reset "expr" to empty string
					}
				}
				else // "expr" is empty
				{
					out=out + " " + c; // insert space before operator
				}
			}
			else // neither operator nor bracket found
			{
				expr=expr+c; // Add the character to "expr"
			}		
		}
		
		// For the right most expression "expr"
		if(expr.length()!=0)
		{
			if(expr.equals("true") || expr.equals("false")) // "expr" is default boolean value
			{
				out=out + " " + expr; // Add spaces
			}
			else  // "expr" is an relational expression
			{
				String boolResult=evaluateRelExpr(expr, absEnv, ADobj); // Evaluate in abstract domain 						
				out=out + " " + boolResult; // Add spaces
			}			
		}
		
		return out;		
	}


   	// Infix to postfix conversion for boolean expression
	public String doBoolTrans(String inString, stack myStack) 
    {
      			
      	String outString="";
      		
      	StringTokenizer st = new StringTokenizer(inString, " "); // Tokens in the infix boolean expression are seperated by spaces
      	while(st.hasMoreTokens())
     	{
     		String ch=st.nextToken();  // Extract each token in the infix expression
      		ch=ch.trim();

      		if(ch.equals("|")) // Operator having "1" precedence found
      		{
      			// pushing operators in the stack based on their precedences
      			outString=gotBoolOper(ch, 1, myStack, outString); 	
      		}
      		else if(ch.equals("&")) // Operator having "2" precedence found
      		{
      			// pushing operators in the stack based on their precedences
      			outString=gotBoolOper(ch, 2, myStack,outString);  	
      		}
      		else if(ch.equals("!")) // Operator having "3" precedence found
      		{
      			// pushing operators in the stack based on their precedences
      			outString=gotBoolOper(ch, 3, myStack,outString); 	
      		}
      		else if(ch.equals("(")) // Got left parenthesis
      		{
      			myStack.push(ch); // push into the stack
      		}
      		else if(ch.equals(")")) // Got right parenthesis
      		{
      			// pop and write to output until we get left parenthesis
      			outString=gotParen(myStack, outString);  				
      		}
      		else
      		{
      			if(outString.equals("")) // must be an operand and so write it to output
      			 	outString = outString + ch; 
      			else
      			 	outString = outString + " " + ch;	
      		}
      	}
       
       	// Write all the remaining elements in the stack to the output
      	while (!myStack.isEmpty()) 
      	{
      		
      		if(outString.equals(""))
      			outString = outString + myStack.pop();
      		else
      		 	outString = outString + " " + myStack.pop();
      	}
	   		
	   	return outString; // Returning postfix boolean expression
	}  	
	
	// pushing or poping the stack based on the precedence of the incoming operators
	public String gotBoolOper(String opThis, int prec1, stack myStack, String outString) 
    {
    	while (!myStack.isEmpty()) 
    	{
   			String opTop = myStack.pop(); // Pop-ing top-most element from the stack
   		
   			if (opTop.equals("("))  // Top-most element of the stack is "("
    		{
      			myStack.push(opTop); // Do nothing
      			break;
      		}
      		else  // Top-most element of the stack is an operator
      		{	
      		
      			// Assinging precedence to the top-most operator
      			int prec2;
      			if (opTop.equals("|"))
      				prec2 = 1;
      			else if(opTop.equals("&"))
      				prec2 = 2;
      			else
      				prec2 = 3;
      			
      			// precedence ofthe incoming operator is more than the precedence of the top-most operator?		
      			
      			if (prec2 < prec1) // Yes 
      			{
      				myStack.push(opTop); // Do nothing
      				break;
      			} 
      			else // No
      			{
      				// Write the top-most operator to the output
      				if(outString.equals(""))
      					outString = outString + opTop;
      				else
      		 			outString = outString + " " +  opTop;
      		 	}
     		}
      	}
      	
      	// Finally push the incoming operator onto the stack		
      	myStack.push(opThis);
      	return outString;
	}
}

