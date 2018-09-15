
/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This CLASS defines the methods that compute the Trace Semantics of a program in an abstract domain
 *
 * Input: File containing all detail information of the program as an intermediate form
 *        Abstract Domain
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/

package packageSemantics;

import java.io.*;
import java.io.File;
import java.util.*;

import packageMyLibrary.*;
import packageMatrix.*;
import packageEvalExpr.*;

import packageAbstraction.ProgEnvironment.abstractEnvironment;
import packageAbstraction.DomainInterface.abstractDomain;
import packageAbstraction.ComponentInterface.abstractComponent.*;

import packageAbstraction.Operator.*;
import packageAbstraction.Value.*;
import packageAbstraction.Domain.*;

public class genTraceSemantics extends myFunctions implements myFilePaths, myPatterns
{	
	RandomAccessFile rafExtractInfoFile;
	abstractDomain ADobj;
	int[][] CFGmatrix;
	int ProgSize;	
	String[] ProgVars;
	
	/* Constructor takes a stream of type "random access file" containing details programs information
	 * and the abstract domain of interest */
	 
	public genTraceSemantics(RandomAccessFile rafExtractInfoFile, abstractDomain ADobj)
	{ 		
      	this.rafExtractInfoFile=rafExtractInfoFile;
       	this.ADobj=ADobj;
      	
      	CFGmatrix=(new formMatrix(rafExtractInfoFile)).getCFGmatrix(); //Generates incidence matrix of CFG for the program
      	
      	ProgSize=super.getProgSize(rafExtractInfoFile); //Computes the program size i.e. the number of statements in the program
      		
      	ProgVars=super.getProgVars(rafExtractInfoFile); //Computes the array of program variables 
	}
	
	// Class "SuccessorNode" stores the true- and false-successors of each node
	class SuccessorNode
	{
		int TSuccNode;
		int FSuccNode;
	
		SuccessorNode()
		{
			this.TSuccNode=-1;
			this.FSuccNode=-1;	
		}
		
		public void Add_T_Succ(int nodeVal) // Adding true-successor
		{
			this.TSuccNode=nodeVal;
		}
		
		public void Add_F_Succ(int nodeVal) // Adding flase-successor
		{
			this.FSuccNode=nodeVal;
		}
	
		public int get_T_Succ(){return this.TSuccNode;}	
	
		public int get_F_Succ(){return this.FSuccNode;}	
	}
	
	// Class "state" stores two tuple <program point, abstract environment>
	public class state extends myFunctions
	{
		int ProgPoint;
		abstractEnvironment env;
	
		state(int line_no) // Empty state
		{
			ProgPoint=line_no;			
			env=null;
		}
		
		state(int line_no, String[] ProgVars, abstractDomain ADobj) // Initial state
		{
			ProgPoint=line_no;
			env=new abstractEnvironment(ProgVars, ADobj);	
		}
		
		state(int line_no, abstractEnvironment environment) // non-empty state
		{
			ProgPoint=line_no;
			env=environment;	
		}
		
		public int getProgPoint() // Return program point
		{
			return this.ProgPoint;
		}
		
		public abstractEnvironment getEnvironment()
		{
			return this.env;
		}
		
		public boolean equals(state st) // Comparing two states
		{
			if(st.getProgPoint()==ProgPoint && env.equals(st.getEnvironment()))
				return true;
			return false;
		}
		
		public void display() // Displaying state
		{
			System.out.println(ProgPoint);
			System.out.print(" :   ");
			env.display();	
		}
	
	}
	
	
	// Generating Trace Semantics
	public LinkedList getTraceSemantics()
	{
		
		LinkedList trace=new LinkedList<state>();
			
		// Determine the successor nodes for every node started from node-0
		SuccessorNode[] SuccessorVector=new SuccessorNode[ProgSize];
		for(int i=0; i<ProgSize;i++)
		{
			SuccessorVector[i]=new SuccessorNode(); // contains successor nodes of i
			
			for(int j=0;j<ProgSize;j++)
			{
				if(CFGmatrix[i][j]==1) // j is the true successor of i
				{
					if(SuccessorVector[i].get_T_Succ()==-1) // No true-successor present
						SuccessorVector[i].Add_T_Succ(j);  // Adding j as true-successor
					else
						System.out.println("packageSemantics.TraceSemantics.GenTraceSemantics.GetTraceSemantics: Error: More than one true successor!!");
				}	
				else if(CFGmatrix[i][j]==-1) // j is the false successor of i
				{
					if(SuccessorVector[i].get_F_Succ()==-1) // No false successor present
						SuccessorVector[i].Add_F_Succ(j);   // Adding j as false-successor
					else
						System.out.println("packageSemantics.TraceSemantics.GenTraceSemantics.GetTraceSemantics: Error: More than one false successor!!");
				}
			}
		}
		
		int root=0; // starting program point
		abstractEnvironment environment=new abstractEnvironment(ProgVars, ADobj); // initial environment
		Traverse(root, environment, SuccessorVector, trace); // Traverse CFG and generate traces
		
		return trace;
	}
	
	// To traverse the CFG and to generate the abstract traces
	private void Traverse(int node, abstractEnvironment environment, SuccessorNode[] SuccessorVector, LinkedList trace)
	{
		// Initialize the next state
		int nextNode=-1;
		abstractEnvironment nextEnvironment=null;
		
		// Extracting statement details corresponding to the current node
		String stmt=super.extractInfoLine(rafExtractInfoFile, node);  
		
		if(!stmt.trim().equals(""))
		{
			
			// Extracting the type of the command at the current node
			String[] field=stmt.split(" ");
			String type=field[1].trim(); 
									
			if(type.equals("as")) // Assignment command
			{
	
				// Adding the current state	to the trace
				state s=new state(node, environment);
				trace.addLast(s);		
						
				String def=field[3].trim();
				String cmd=field[5].trim();
				String[] split_cmd=cmd.split("=");
				String expr=split_cmd[1].trim();
				
				// Obtain next state										
	   	 		abstractValue result= (new evaluateExpr()).evaluateArithExpr(expr, environment, ADobj); 
		   		nextEnvironment=environment.getModifiedEnvironment(def, result);	   		
	   			nextNode=SuccessorVector[node].get_T_Succ();
    		
    	 	}
			else if(type.equals("pi")) // Phi statement
			{
				// Adding the current state	to the trace
				state s=new state(node, environment);
				trace.addLast(s);			
				
				// Obtaining next state								
				String def=field[3].trim();
				String use=field[4].trim();
			
				String[] useVars=use.split("\\|");
			
				abstractEnvironment new_environment=null;
				for(int i=0;i<useVars.length;i++) // For all USED variables generate the next states
				{
					abstractValue absVal=environment.getVariableValue(useVars[i]);
					abstractEnvironment TempEnv=environment.getModifiedEnvironment(def, absVal); 
					
					// Compute LUB of all next states
					if(nextEnvironment==null)   
					{
						nextEnvironment=TempEnv;
					}
					else
					{ 													
						nextEnvironment=nextEnvironment.getLUB(TempEnv, ADobj);
					}	
				}
		
				nextNode=SuccessorVector[node].get_T_Succ();
													     							
			}
			else if(type.equals("if")) // "if" command
			{
				// Adding the current state	to the trace
	       		state s=new state(node, environment);
				trace.addLast(s); 
		
				//Obtaining next state
				nextEnvironment=environment; // command's execution does not change environment
				
				String cmd=field[5].trim();						
		
				abstractValue result= (new evaluateExpr()).evaluateBoolExpr(cmd, environment, ADobj); 
				String resVal=result.getProperty()[0];
				
				// next command will be either true-successor or false-successor depeneding on the evaluation of the "if"		
				if(resVal.equals("true")) 
				{
					nextNode=SuccessorVector[node].get_T_Succ();
       			}
	       		else if(resVal.equals("false"))
    	   		{
       				nextNode=SuccessorVector[node].get_F_Succ();
		    	}  
    		   	else if(resVal.equals("top"))
       			{
	       			nextNode=SuccessorVector[node].get_T_Succ();
    	   		}
       			else 
	       		{
    	   			nextNode=SuccessorVector[node].get_T_Succ();
       			}
       		
			}
			else if(type.equals("wh")) // "while" statement
			{
		
				state s=new state(node, environment); // Current state
			
				// Obtaining next state
				nextEnvironment=environment; // command's execution does not change environment
				
				String cmd=field[5].trim();						
		
				abstractValue result= (new evaluateExpr()).evaluateBoolExpr(cmd, environment, ADobj); 
				String resVal=result.getProperty()[0];
				
				// next command will be either true-successor or false-successor depeneding on the evaluation of the "while"	
				if(resVal.equals("true")) 
				{
					nextNode=SuccessorVector[node].get_T_Succ();
	       		}
    	   		else if(resVal.equals("false"))
       			{
       				nextNode=SuccessorVector[node].get_F_Succ();
		    	}  
    		   	else if(resVal.equals("top"))
       			{
	       			if(isRepeat(trace, s)) // Checking if there is multiple occurance of the same state in the trace
    	   			{
       					nextNode=SuccessorVector[node].get_F_Succ(); // exit the while loop, add the false-successor
       				}
	       			else
    	   			{
       					nextNode=SuccessorVector[node].get_T_Succ();	
       				}
  	     		}
    	   		else 
       			{
	       			if(isRepeat(trace, s)) // Checking if there is multiple occurance of the same state in the trace
    	   			{
       					nextNode=SuccessorVector[node].get_F_Succ(); // exit the while loop, add the false-successor
       				}
	       			else
    	   			{
       					nextNode=SuccessorVector[node].get_T_Succ();	
       				}
   	    		}
   	    		
       		 	trace.addLast(s); // Add the current state
			}
			else // Other type of statements
			{
				// Adding the current state	to the trace
				state s=new state(node, environment);
				trace.addLast(s);
				
				// Obtaining next state
				nextEnvironment=environment;
				nextNode=SuccessorVector[node].get_T_Succ();					
			}
	
			// Recursively traverse the CFG consider the next state as the current one
			Traverse(nextNode, nextEnvironment, SuccessorVector, trace); 
		}		
	}
	
	// Checking if there is multiple occurance of the same state in the trace
	private boolean isRepeat(LinkedList trace, state st)
	{
		
		boolean flag=true;
			
		int start=-1;
		int end=-1;
		
		// Identifying starting and ending points in the trace with occurrance of the same state "st" 
		ListIterator itr=trace.listIterator();
		while(itr.hasNext())
		{
			state newst=(state)itr.next();
			
			if(newst.equals(st))
			{
				if(start==-1)
					start=itr.nextIndex();
				else
					end=itr.nextIndex();	
			}
		}	
			
		
		if(start!=-1 && end!=-1 && start!=end) // There exist multiple occurrance of "st"
		{
			ListIterator itr1=trace.listIterator(start);
			ListIterator itr2=trace.listIterator(end);
		
			while(itr2.hasNext()) // Iterate till the end of the trace by starting from index "end"
			{
				state extract_state1=(state)itr1.next();
				state extract_state2=(state)itr2.next();
			
				if(! extract_state1.equals(extract_state2)) // subtraces do not match
				{
					flag=false;	
					break;
				}
			}
		}
		else // No two occurrances of "st"
		{
			flag=false;	
		}
		
		if(flag==true) // Remove multiple occurrance of subtraces
		{
			int i=trace.size();
			while(i>=end)
			{
				trace.remove(i-1);
				i--;
			}
		}
		
		return flag;	
	}
	
	
	//To compute trace semantics(set of traces) whose states are atomic in nature
	public LinkedList[] getAtomicTraceSemantics(int limit)
	{
		// computes trace semantics (one single trace) whose states may not atomic
		LinkedList TrSemantics=getTraceSemantics(); 
		
		//no. of rows=no. of states in the trace "TrSemantics"
		state[][] matrixAtomicStates=new state[TrSemantics.size()][]; 
		
		//for each state in "TrSemantics" - computes their atomic states and store into corresponding row
		ListIterator itr=TrSemantics.listIterator();
		while(itr.hasNext())
		{
			int index=itr.nextIndex();
			state st=(state)itr.next(); // For each state in the trace
			
			int point=st.getProgPoint(); // Extract Program point
			abstractEnvironment env=st.getEnvironment(); // Extract Abstract Environment
			
			LinkedList coveringEnvironment=new LinkedList();
			
			//Narrowing the abstract environment to the subset of variables appearing in the command
			String use=convertListToString(getUseVars(rafExtractInfoFile, point)); // USED variables in the corresponding command
			if(! use.equals(""))
			{
				String def=getDef(rafExtractInfoFile, point); // DEF variable in the corresponding command
				String variables=def+"|"+use;
				String[] variables_arr=variables.split("\\|"); // Array of variables in the whole command
				
				// Narrowing the abstract environment only to present variables
				abstractEnvironment RestrictedEnvironment = env.getRestrictedEnvironment(variables_arr);
				
				// Computes the atomic covering of the restricted environment
				coveringEnvironment=RestrictedEnvironment.getAtomicCovering(ADobj);
			}
			else
			{	// There is no USED variables in the command, 
				// So, evaluation of the command does not need atomic covering at all
				coveringEnvironment.addLast(env);					
			}
				
			// Abstract Environment in the state is replaced by set of atomic coverings of that environment					
			matrixAtomicStates[index]=new state[coveringEnvironment.size()]; 
			
			for(int i=0; i<coveringEnvironment.size(); i++)
			{
				matrixAtomicStates[index][i]=new state(point, (abstractEnvironment)coveringEnvironment.get(i));
			}
		}
		
		// Converting two dimentional array "matrixAtomicStates" (where each element is a state) into
		// a two dimentional array (where each element is linkedlist containing the state)
		LinkedList[][] list=convert2List(matrixAtomicStates);
		
		// Computing Cartesian Product of the array of list of atomic states: produce all possible atomic traces
		LinkedList[] prodResult= super.CartesianProduct(list);
		
		// Returning the set of atomic abstract traces
		return prodResult;
	}
	
	
	
	// Converting two dimentional array (each element is state) into 
	// a two dimentional array (each element is linkedlist containing the state)
	private LinkedList[][] convert2List(state[][] arr)
	{
		LinkedList[][] ls=new LinkedList[arr.length][];
		
		for(int i=0;i<arr.length;i++)
		{
			ls[i]=new LinkedList[arr[i].length];
			
			for(int j=0;j<arr[i].length;j++)
			{
				ls[i][j]=new LinkedList();
				ls[i][j].addLast(arr[i][j]);	
			}
		}
		
		return ls;
	}	
}	
