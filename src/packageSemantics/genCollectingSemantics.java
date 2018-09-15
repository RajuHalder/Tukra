/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This CLASS defines the methods that compute collecting semantics of a program in an abstract domain
 *
 * Input: File containing all detail information of the program as an intermediate form, and
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
import packageAbstraction.Domain.*;
import packageAbstraction.DomainInterface.abstractDomain;
import packageAbstraction.ComponentInterface.abstractComponent.*;

public class genCollectingSemantics extends myFunctions implements myFilePaths
{	
	RandomAccessFile rafExtractInfoFile;
	abstractDomain ADobj;
	int[][] CFGmatrix;
	int ProgSize;	
	String[] ProgVars;
	
	/* Constructor takes a stream of type "random access file" containing details programs information
	 * and the abstract domain of interest */
	
	public genCollectingSemantics(RandomAccessFile rafExtractInfoFile, abstractDomain ADobj)
	{ 		
      	this.rafExtractInfoFile=rafExtractInfoFile;
       	this.ADobj=ADobj;
      	
      	CFGmatrix=(new formMatrix(rafExtractInfoFile)).getCFGmatrix(); //Generates incidence matrix of CFG for the program
      	
      	ProgSize=super.getProgSize(rafExtractInfoFile); //Computes the program size i.e. the number of statements in the program
      		
      	ProgVars=super.getProgVars(rafExtractInfoFile); //Computes the array of program variables 
	}	
	
	/* Given an edge n1--->(c) n2, the "Edge" objects store the source node n2 and the truth value c of the edge
	 * if n1 is control node, then c is either "T" or "F"
	 * if n1 is non-control node, then c is by default "T" */ 
	 
	class Edge
	{
		int Source;
		boolean edgeTruthValue;
	
		Edge(int Source, boolean boolVal)
		{
			this.Source=Source;
			this.edgeTruthValue=boolVal;	
		}
	
		public int getSource(){return this.Source;}	
	
		public boolean getEdgeTruthValue(){return this.edgeTruthValue;}	
	}
	
	/* "ContextVector" objects stores the abstract environment for all program points */
	
	class ContextVector extends myFunctions
	{
		abstractEnvironment[] ArrayOfEnvironment;
	
		//Creates "null" environment for all program points
		ContextVector(int ProgSize)
		{
			ArrayOfEnvironment=new abstractEnvironment[ProgSize];
			for(int i=0;i<ProgSize;i++)
			{	
				ArrayOfEnvironment[i]=null;
			}
		}
		
		//Creates abstract environment for all program points where each variable initialized by "bottom" value
		ContextVector(int ProgSize, String[] ProgVars, abstractDomain ADobj)
		{
			ArrayOfEnvironment=new abstractEnvironment[ProgSize];
			for(int i=0;i<ProgSize;i++)
			{
				ArrayOfEnvironment[i]=new abstractEnvironment(ProgVars, ADobj);
			}	
		}
	
		//return list of abstract environment associated with each program points
		abstractEnvironment[] getContextVector()
		{
			return ArrayOfEnvironment;
		}
			
		//Display a context vector	
		void display()
		{
			for(int i=0;i<ProgSize;i++)
			{	
				String label=super.getLabel(rafExtractInfoFile, i);
				System.out.print("Node "+label+": ");
				ArrayOfEnvironment[i].display();
				System.out.println();
			}		
		}
		
		//Copy one Contex Vector to another Context Vector
		void copy(ContextVector source)
		{
			abstractEnvironment[] arr=source.getContextVector();
			
			if(arr.length==ArrayOfEnvironment.length)
			{
				for(int i=0; i<ArrayOfEnvironment.length; i++)
				{
					ArrayOfEnvironment[i]=arr[i];
				}
			}
			else
			{
				System.out.println("packageSemantics.GenCollectingSemantics.ContextVector.Copy(): error - target is having more or less elements!!!!");
			}
		}
		
		//Compare two Context Vectors for equality
		boolean equals(ContextVector obj)
		{
			abstractEnvironment[] arr=obj.getContextVector();
			boolean flag=true;
			
			if(ArrayOfEnvironment.length==arr.length)
			{
				for(int i=0; i<ArrayOfEnvironment.length; i++)
				{
					if(! arr[i].equals(ArrayOfEnvironment[i]))
					{
						flag=false;break;
					}
				}
			}
			else
			{
				flag=false;	
			}
			
			return flag;
		}
		
		//Modify the abstract environment at a program point in a context vector
		void modifyContext(int nodeID, abstractEnvironment ae)
		{
			if(nodeID>=0 && nodeID<=ArrayOfEnvironment.length)
				ArrayOfEnvironment[nodeID]=ae;
			else
				System.out.println("packageSemantics.GenCollectingSemantics.ContextVector.modifyContext(): Provide correct index to modify!!!" );
		}	
			
	}

	/*Generates Collecting Semantics which is just an array of abstract environment associated with each program point*/
	
	public abstractEnvironment[] getCollectingSemantics()
	{
			
		//"EdgeVector" stores set of incoming edges for each node in the CFG 
		LinkedList[] EdgeVector=new LinkedList[ProgSize];
		for(int nodeID=0; nodeID<ProgSize;nodeID++)
		{
			EdgeVector[nodeID]=new LinkedList<Edge>();
			
			for(int predNode=0; predNode<ProgSize; predNode++)
			{
				if(CFGmatrix[predNode][nodeID]==1)
				{
					Edge obj=new Edge(predNode, true);
					EdgeVector[nodeID].addLast(obj);
				}	
				else if(CFGmatrix[predNode][nodeID]==-1)
				{
					Edge obj=new Edge(predNode, false);
					EdgeVector[nodeID].addLast(obj);
				}
			}
		}
			
		/* Initialize the Context Vector for the program ("ProgramContext": abstract environments are bottom) 
		 * and a temporary context vector ("TempContext": abstract environments are null)	*/
		 
		ContextVector ProgramContext=new ContextVector(ProgSize, ProgVars, ADobj);		
		ContextVector TempContext=new ContextVector(ProgSize);
		
		// To determine the fix-point to obtain collecting semantics
		boolean fix_point=true;
		do{
			TempContext.copy(ProgramContext);	
			
			for(int nodeID=1; nodeID<ProgSize; nodeID++) //Iteration
			{
				iterateContext(ProgramContext, nodeID, EdgeVector[nodeID]);
			}

			fix_point=true;
			
			if(! ProgramContext.equals(TempContext)) //Does not reach fixpoint
			{
				fix_point=false;
			}
			
		}while(! fix_point);	
		
		System.out.println("packageSemantics.GenCollectingSemantics.getCollectingSemantics(): The End.");
		
		return ProgramContext.getContextVector();
	}
	
	private void iterateContext(ContextVector ProgramContext, int nodeID, LinkedList EdgeList)
	{		
		//Old Abstract Environment associated with nodeID
		abstractEnvironment OldEnvironment=ProgramContext.getContextVector()[nodeID];
		
		//New Abstract Environment incoming towards nodeID
		abstractEnvironment NewEnvironment=getIncomingEnvironment(ProgramContext, EdgeList);			
			
			
		//Compute LUB of Old and New Abstract Environments
		abstractEnvironment resultEnvironment=OldEnvironment.getLUB(NewEnvironment, ADobj);
		
		//Modify the Abstract Environment of the nodeID
		ProgramContext.modifyContext(nodeID, resultEnvironment);
		
		System.out.println("packageSemantics.GenCollectingSemantics.iterateContext(): The End.");		
	}
	
	private abstractEnvironment getIncomingEnvironment(ContextVector ProgramContext, LinkedList EdgeList)
	{
					
		abstractEnvironment[] ProgEnvironments=ProgramContext.getContextVector();
		
		LinkedList EnvironmentList=new LinkedList();		
		
		// Collect the Environments coming from all predecessors of the nodeID
		ListIterator itr = EdgeList.listIterator();
		while(itr.hasNext())
		{
			
			Edge edgeObj=(Edge) itr.next();				
			int SourceNode=edgeObj.getSource(); //Extract Predecessor node
			boolean EdgeTruthVal=edgeObj.getEdgeTruthValue(); //Extract the truth value of incming edge
			
			//Extract Abstract Environment associated with Predecessor node
			abstractEnvironment SourceEnvironment=ProgEnvironments[SourceNode]; 
			
			//Extract Predecessor statement details
			String stmt=super.extractInfoLine(rafExtractInfoFile, SourceNode); 
			String[] field=stmt.split(" ");
			String type=field[1].trim();
									
			if(type.equals("as")) //Predecessor statement is assignment statement
			{			
													
				String def=field[3].trim();
				String cmd=field[5].trim();
				String[] split_cmd=cmd.split("=");
				String expr=split_cmd[1].trim();
														
     			abstractValue result= (new evaluateExpr()).evaluateArithExpr(expr, SourceEnvironment, ADobj); 
     			
     			//after execution obtain the new environment			
     			abstractEnvironment changeEnv=SourceEnvironment.getModifiedEnvironment(def, result); 
     													
				EnvironmentList.addLast(changeEnv);				
			}
			if(type.equals("pi")) //Predecessor statement is Phi statement
			{			
													
				String def=field[3].trim();
				String use=field[4].trim();
				
				String[] useVars=use.split("\\|");
				
				for(int i=0;i<useVars.length;i++)
				{
					//after execution obtain the new environment
					abstractValue absVal=SourceEnvironment.getVariableValue(useVars[i]);				
					abstractEnvironment changeEnv=SourceEnvironment.getModifiedEnvironment(def, absVal);    													
					EnvironmentList.addLast(changeEnv);	
				}										     							
			}
			else if(type.equals("if") || type.equals("wh")) //Predecessor statement is conditional statement
			{
						
				String cmd=field[5].trim();						
				
				abstractValue result= (new evaluateExpr()).evaluateBoolExpr(cmd, SourceEnvironment, ADobj); 
				String resVal=result.getProperty()[0];
				
				//Execution yields same truth value as assocated with the edge		
				if((EdgeTruthVal==true && resVal.equals("true")) || resVal.equals("top")) 
				{
					EnvironmentList.addLast(SourceEnvironment);
       			}
       			else if((EdgeTruthVal==false && resVal.equals("false")) || resVal.equals("top"))
       			{
       				EnvironmentList.addLast(SourceEnvironment);
	       		}  
    	   		else
       			{
       				EnvironmentList.addLast(new abstractEnvironment(ProgVars, ADobj)); //if the result is "bottom"
       			}
			}
			else //Predecessor statement is non-conditional statement
			{
				EnvironmentList.addLast(SourceEnvironment);						
			}
		}
			
		if( EnvironmentList.isEmpty()) //If there is no predecessor nodes
		{	
			EnvironmentList.addLast(new abstractEnvironment(ProgVars, ADobj));
		}
		
		//returns the LUB of all incoming environments
		return computeLUBofEnvironmentList(EnvironmentList); 
		
	}
	
	
	//To compute the LUB of a list of environemnts
    private	abstractEnvironment computeLUBofEnvironmentList(LinkedList list)
	{
		if(list.isEmpty()) //if no abstract environment, return null
		{
			return null;
		}
		else if(list.size()==1) //if one abstract environment, return that environment only
		{
			return ((abstractEnvironment)list.getFirst());
		}
		else if(list.size()==2) //if two abstract environments, return the LUB of that two environments
		{
			abstractEnvironment obj1=(abstractEnvironment)list.getFirst();
			abstractEnvironment obj2=(abstractEnvironment)list.getLast();
			return obj1.getLUB(obj2, ADobj);	
		}
		else //if more than two abstract environments: split into two lists and recall the function recursively
		{
			LinkedList temp=new LinkedList();
			ListIterator itr=list.listIterator();
			while(itr.hasNext())
			{
				temp.addLast(itr.next());	
			}
			
			// Extract the first environment
			abstractEnvironment first=(abstractEnvironment)temp.getFirst(); 
			
			// Computing LUB of the others
			Object obj=temp.removeFirst();
			abstractEnvironment rest=computeLUBofEnvironmentList(temp);
			
			// Returning the LUB of the above two result
			return first.getLUB(rest, ADobj);
		}	
	}
	
}



