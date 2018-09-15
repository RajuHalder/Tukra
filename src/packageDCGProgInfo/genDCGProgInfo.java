/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This class refines DCGs based on the satisfiability of annotations over the DCG paths
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageDCGProgInfo;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

import packageSSAconversion.*;
import packageSemantics.*;
import packageSemantics.genTraceSemantics.*;
import packageDCGannotations.*;
import packageDCGannotations.genDCGannotations.*;
import packageMyLibrary.*;
import packageMatrix.*;
import packageEvalExpr.*;

import packageAbstraction.ProgEnvironment.abstractEnvironment;
import packageAbstraction.DomainInterface.abstractDomain;
import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.Operator.*;
import packageAbstraction.Value.*;
import packageAbstraction.Domain.*;

public class genDCGProgInfo extends myFunctions implements myFilePaths, myPatterns
{	
	String InputPreview; // Path of the preview file of the input program
	RandomAccessFile rafInputExtractInfoFile; // Detail information about the program
	abstractDomain ADobj; // Abstract Domain
	
	RandomAccessFile rafSSAExtractInfoFile; // Detail information about the SSA program
	LinkedList traceSemantics; // Abstract Trace Semantics of SSA program
	LinkedList[] atomicTraceSemantics; // Atomic Trace semantics of SSA program: each traces containing atomic states
	
	annotate[][] DCGmatrix; // Matrix of DCG annotations
	int[][] SSAPDGmatrix; // PDG of SSA program
	
	int SSAProgSize;	// no. of program statements
	
	
	
	/*Constructor "genDCGProgInfo" takes preview program file's path and details program information in the 
	 * form of random access file type and the abstract domain of interest */
	 
	public genDCGProgInfo(String InputPreview, RandomAccessFile rafInputExtractInfoFile, String AbsDom)
	{ 	
		this.InputPreview=InputPreview;	// Input program's preview file path
      	this.rafInputExtractInfoFile=rafInputExtractInfoFile; // Input program's details
       	
       	if(AbsDom.equals("sign")) // Input abstract domain
      	{
      		ADobj=new abstractSignDomain();
      	}
      	else if(AbsDom.equals("par"))
      	{
      		ADobj=new abstractParDomain();
      	}
      	
      	
      	try{
      		     
      		// Generating detail program information and preview form in SSA form 		
      		genSSAform objSSA=new genSSAform(rafInputExtractInfoFile);
      		objSSA.genSSAprogram(InputPreview, File_Out_SSA_Preview, File_Out_SSA_ExtractInfo);
      
      		rafSSAExtractInfoFile = new RandomAccessFile(File_Out_SSA_ExtractInfo, "r");
      	
      		// Generating Trace semantics of the SSA program
      		genTraceSemantics objTrSem=new genTraceSemantics(rafSSAExtractInfoFile, ADobj);      		
      		traceSemantics=objTrSem.getTraceSemantics();
      		
      		// Generating Set of traces containing atomic program states
      		//atomicTraceSemantics=objTrSem.getAtomicTraceSemantics(10);
      				
      		// Computing DCG annotations
      		genDCGannotations objDCG=new genDCGannotations(rafSSAExtractInfoFile);
      		DCGmatrix=objDCG.getAnnotations();
      		
      		// Creating PDG matrix for SSA program
      		SSAPDGmatrix=(new formMatrix(rafSSAExtractInfoFile)).getPDGmatrix(); 
      		
      		// Computing the no. of statements in SSA program
      		SSAProgSize=super.getProgSize(rafSSAExtractInfoFile); 
      	
      		
      	}catch (FileNotFoundException e){
        	 System.err.println("packageDCGProgInfo.genDCGProgInfo(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageDCGProgInfo.genDCGProgInfo(): Problem with IO exception." + e);}
      	    		
	}
	
	
	// To check whether "trace" satisfying the annotation "annot" of the CDG edge "source --->(cond) dest"
	public boolean isSatCDGedge(int source, int dest, String cond, annotate annot, LinkedList trace)
	{
		
		boolean flag=false; // Initialy set false indicating not satisfied by the trace
		
		// Get reach and avoid sequences: list of conditions of the form "source:cond:dest"
		LinkedList ReachSeq=annot.getReachSeq(); 
		LinkedList AvoidSeq=annot.getAvoidSeq(); 
		
		ListIterator itrReachSeq=ReachSeq.listIterator();		
		if(itrReachSeq.hasNext())
		{
			String strReachSeq = itrReachSeq.next().toString(); // Extracting reach sequence
			
			String newReachSeq =new String(source+":"+cond+":"+dest); // Creating new reach sequence based on the CDG edge
			
			if(strReachSeq.equals(newReachSeq) && AvoidSeq.isEmpty()) // If both are same and the avoid sequence is empty
			{
				
				String[] token={Integer.toString(source), cond}; // creating a tuple contains {"source", "condition"}
				
								
				// Checking for satisfaction by sending "trace" and "token"
				// "test" is true if the trace satisfies the token at index "i". The "listIndexs" contains all "i".
				LinkedList listIndexes=new LinkedList();
				boolean test=check(token, trace, listIndexes); 
			
				if(test==true && !listIndexes.isEmpty())
				{
					flag=true;  // Satisfied by the trace
				}
			}
		}
		
		return flag;
	}
	
	// To check whether "trace" satisfying the annotation "annot" of the DDG edge "source ---> dest" at the position "TracePosition"
	public boolean isSatDDGedge(int source, int dest, annotate annot, int TracePosition, LinkedList trace)
	{
	
		boolean reachFlag=true; // Suppose the "reach sequences" are satisfied by the trace initially
		boolean avoidFlag=true;	// Suppose the "avoid sequences" are avoided by the trace initially			
		
		// Extracting a set of indexes in "trace" where "source" occurs
		LinkedList sourceIndexes=getTraceIndex(source, trace);
		
		if(isExist(TracePosition, sourceIndexes)) // "TracePosition" is valid
		{	
		
			//%%%%%%%%%% Satisfying Reach Sequence %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
			
			// Extracting reach sequence of the DDG edge
			LinkedList ReachSeq=annot.getReachSeq(); 
				
			ListIterator itrReachSeq=ReachSeq.listIterator();		
			while(itrReachSeq.hasNext())
			{
				// For all reach sequences
				String strReachSeq = itrReachSeq.next().toString(); 
					
				//Remove the last node which is not conditional, but destination.....
				strReachSeq=strReachSeq.substring(0, strReachSeq.lastIndexOf(":")).trim();
				
				// creating array of strings that contains {"node 1", "cond 1", "node 2", "cond 2", and so on..}				
				String[] token=strReachSeq.split(":");
				
				
				// Checking for satisfaction by sending "trace" and "token"
				// "flag" is true if the trace satisfies the token. 
				// The "listIndexes" contains all indexes "r" if trace satisfies {node i, cond i} at "r".	
				LinkedList listIndexes=new LinkedList();												
				boolean flag=check(token, trace, listIndexes);
				
				if(flag==false) // Not satisfied by the trace
				{
					reachFlag=false; // Set false
					break;	
				}
				else // Satisfied by the trace
				{
					int firstIndex=((Integer)listIndexes.get(0)).intValue();
					int secondIndex=((Integer)listIndexes.get(1)).intValue();
					
					if(!(firstIndex<= TracePosition && TracePosition<=secondIndex))
					{
						// Given "TracePosition" does not matches with the indexes where "token" actually satisfied 
						reachFlag=false; // Set false
						break;
					}					
				}
			}
			
			//%%%%%%%%%% Satisfying Avoid Sequence %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			
			// Extracting avoid sequence of the DDG edge
			LinkedList AvoidSeq=annot.getAvoidSeq();
				
			ListIterator itrAvoidSeq=AvoidSeq.listIterator();		
			while(itrAvoidSeq.hasNext())
			{
				// For all avoid sequences
				String strAvoidSeq = itrAvoidSeq.next().toString();
					
				//Remove the last node which is not conditional, but destination.....
				strAvoidSeq=strAvoidSeq.substring(0, strAvoidSeq.lastIndexOf(":")).trim();				
				String[] token=strAvoidSeq.split(":");
				
				//Identifying subtrace in the "trace" starting from "TracePosition" and ending to the first occurance of "dest"
				LinkedList subTrace=getSubTrace(TracePosition, dest, trace);
				
				// Checking for satisfaction by sending "sunTrace" and "token"
				// "flag" is true if the "subTrace" satisfies the token at index "i". The "listIndexs" contains all "i".		
				LinkedList listIndexes=new LinkedList();												
				boolean flag=check(token, subTrace, listIndexes);
				
				if(flag==true) // Satisfied by the trace, i.e. not avoided
				{
					avoidFlag=false; // Set false. Because our aim was to avoid it.
					break;	
				}
			}
		}
		else // "TracePosition" is invalid
		{
			reachFlag=false;
			avoidFlag=false;	
		}
	
		if(reachFlag==true && avoidFlag==true) 
		{
			// Reach sequence in satisfied and avoid sequence is avoided
			return true;
		}	
		
		return false;
	}
	
	
	// To check the satisfiability: returns true if satisfied and "listIndexes" contains the indexes where annotations are satisfied in the trace
 	private	boolean check(String[] token, LinkedList trace, LinkedList listIndexes)
	{

		evaluateExpr eval=new evaluateExpr();	
		
		// Flag initialized to true indicating that trace satisfies "token"
		boolean flag=true;
		
		if( trace.size()!=0 )
		{
		
			ListIterator itrTrace = trace.listIterator(); 
			
			// Scanning "token" of the form {"node 1", "cond 1", "node 2", "cond 2", and so on..}
			for(int i=1; i<token.length;i=i+2) 
			{
				// Extracting node and associated condition {node i, cond i}
				int node=Integer.parseInt(token[i-1]); 
				String cond=token[i];
				
				// Flag initialized to false indicating that trace does not satisfy the "{node i, cond i}"
				boolean nodeFlag=false; 
				
				while(itrTrace.hasNext())
				{
					// Extracting each state and its index in the "trace"
					int index=itrTrace.nextIndex();
					state st=(state)itrTrace.next();
					
					// Extracting the "program point" in the state			
					int pp=st.getProgPoint();
					
					if(pp==node && node==0)
					{
						// "node i" matches with the "program point" and "node i" is the "Start" node
						// Add the index in the list, because by default the "cond i" is true
						listIndexes.addLast(index); 
						
						// Go to next state of the "trace"
						itrTrace = trace.listIterator(index+1);
						
						// "{node i, cond i}" is satisfied
						nodeFlag=true; 
						break;
					}				
					else if(pp==node && node>0)
					{
						// "node i" matches with the "program point" and "node i" is not the entry node
						
						// Extract associated environment 
						abstractEnvironment env=st.getEnvironment();
						
						// Extract associated statement details					
						String stmt=super.extractInfoLine(rafSSAExtractInfoFile, node);
						String[] field=stmt.split(" ");
						String type=field[1].trim();
						String cmd=field[5].trim();
						
						if(type.equals("if") || type.equals("wh")) // Control command
						{
							// Evaluate command over associated environment
							abstractValue result=eval.evaluateBoolExpr(cmd, env, ADobj);					
							String resVal=result.getProperty()[0];
						
							if(resVal.equals("top") || (resVal.equals("true") && cond.equals("T")) || (resVal.equals("false") && cond.equals("F")))
							{
								// command is satisfied, so add the index in the list
								listIndexes.addLast(index);
								
								// Go to next state of the "trace"  
								itrTrace = trace.listIterator(index+1);
								
								// "{node i, cond i}" is satisfied
								nodeFlag=true;
								break;
							}
						}
						else
						{
							System.out.println("packageDCGProgInfo.check(): Node is not conditional!!!!");
							break;
						}
					}
				}
				
				if(nodeFlag==false) // "{node i, cond i}" is not satisfied
				{
					flag=false; // Set false
					break;	
				}
			}
		}
		else // Trace is empty
		{
			flag=false;
			System.out.println("packageDCGProgInfo.check(): Trace to satisfy is empty!!!!");
		}
		
		return flag;
	}
	
	
	// To check whether "trace" satisfying the Phi sequence
	public boolean isSatPhiSeq(String PhiSeq, LinkedList trace)
	{	
		// Initialized flag to false indication trace does not satify "PhiSeq"
		boolean flag=false;
			
		LinkedList Rseq=new LinkedList();
		LinkedList Aseq=new LinkedList();
				
		String[] token=PhiSeq.split(":");		
		int startNode=Integer.parseInt(token[0]); // "startNode": the starting real node
		int endNode=Integer.parseInt(token[token.length-1]); // "endNode": the end real not		
		
		for(int target=1; target<token.length; target++) // Iterating over the Phi Sequence
		{
			// Extracting each edge in the Phi Sequence: "source ---> dest"
			int source=Integer.parseInt(token[target-1]); 
			int dest=Integer.parseInt(token[target]); 
			
			// Extracting the DCG annotations assocaited with "source ---> dest"
			annotate ant=DCGmatrix[source][dest];
			
			// Extracting reach and avoid sequences of "source ---> dest"
			LinkedList ReachSeq=ant.getReachSeq();
			LinkedList AvoidSeq=ant.getAvoidSeq();
			
			// Collecting reach sequences and avoid sequences for all edges in the "PhiSeq"
			ListIterator itrReach=ReachSeq.listIterator();
			while(itrReach.hasNext())
			{
				Rseq.addLast(itrReach.next());				
			}
			
			ListIterator itrAvoid=AvoidSeq.listIterator();
			while(itrAvoid.hasNext())
			{
				Aseq.addLast(itrAvoid.next());				
			}							
		}
		
		// Creating an annotation object having reach and avoid sequences  "Rseq" and "Aseq" containing conditions for all edges in the "PhiSeq"
		genDCGannotations dcgObj=new genDCGannotations(rafSSAExtractInfoFile);
		genDCGannotations.annotate annot=dcgObj.new annotate(Rseq, Aseq); 
		
		// Get all indexes in the trace where "startNode" occurs
		LinkedList listIndexes=getTraceIndex(startNode, trace);	
							
		ListIterator itrIndexes=listIndexes.listIterator();
		while(itrIndexes.hasNext())
		{
			// For each index where "startNode" occurs in the trace
			int tracePosition=((Integer)itrIndexes.next()).intValue(); 
			
			// Checking for the satisfaction of DCG path
			if(isSatDDGedge(startNode, endNode, annot, tracePosition, trace)) 
			{		
				// Satisfied: set flag to true			
				flag=true;
				System.out.println("packageDCGProgInfo.isSatPhiSeq(): annotation satisfied for"+PhiSeq);
				annot.display();
				break;
			}
		}
		
		return flag;
	}
	
	
	// Returns the list of indexes in the trace where "node" occurs
	private LinkedList getTraceIndex(int node, LinkedList trace)
	{
			
		LinkedList list=new LinkedList();
				
		ListIterator Titr = trace.listIterator();
		while(Titr.hasNext())
		{
			int index=Titr.nextIndex();
			state st=(state)Titr.next();
			int pp=st.getProgPoint();
					
			if(pp==node)
			{
				list.addLast(index);
			}
		}
		
		return list;
	}
	
	
	// Returns the subtrace starting from "startIndex" and ending to the first occurance of "destinationNode"
	private LinkedList getSubTrace(int startIndex, int destinationNode, LinkedList trace)
	{

		int endIndex=-1;
		
		// Scanning of trace started from "startIndex"		
		ListIterator Titr = trace.listIterator(startIndex);
		while(Titr.hasNext())
		{
			int index=Titr.nextIndex(); 
			state st=(state)Titr.next();
			int pp=st.getProgPoint();
					
			if(pp==destinationNode)
			{
				// "destinationNode" matches
				endIndex=index;
				break;
			}
		}
						
		LinkedList subtrace=new LinkedList();
					
		if(endIndex != -1)
		{
			// "destinationNode" present in the trace
			subtrace=copyLinkedList(trace, startIndex, endIndex);
		}
		else
		{
			// "destinationNode" is not found
			subtrace=copyLinkedList(trace, startIndex, trace.size()-1);
		}
		
		return subtrace;
	}
	
	// Returns the index of the node "dest" in the trace where it occurs first time after the position "tracePosition"
	private int getNextTraceIndex(int tracePosition,int dest, LinkedList trace)
	{
		int newIndex=-1;
		
		// Scanning of trace started from "startIndex"	
		ListIterator Titr = trace.listIterator(tracePosition);
		while(Titr.hasNext())
		{
			int index=Titr.nextIndex(); 
			state st=(state)Titr.next();
			int pp=st.getProgPoint();
				
			if(pp==dest)
			{
				// "destinationNode" matches
				newIndex=index;
				break;
			}
		}
		
		// Return the index
		return newIndex;
	}
	
	
	
	// Returns a list of real nodes in the SSA program
	private LinkedList getRealNodesList()
	{
		LinkedList realList=new LinkedList();
		
		try{
			
			rafSSAExtractInfoFile.seek(0);
			String stmt=rafSSAExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				String type=field[1].trim();
				
				if(! type.equals("pi")) // Yes, got real node
				{
					int line_no=new Integer(field[0].trim());
					realList.addLast(line_no);
				}			
				
				stmt=rafSSAExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("packageDCGProgInfo.getRealNodesList(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageDCGProgInfo.getRealNodesList(): Problem with IO exception." + e);}  
        
        return realList;
	}
	
	// Returns a list of Phi nodes in the SSA program
	private LinkedList getPhiNodesList()
	{
		
		LinkedList phiList=new LinkedList();
		
		try{
			
			rafSSAExtractInfoFile.seek(0);
			String stmt=rafSSAExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				String type=field[1].trim();
				
				if(type.equals("pi")) // Yes, got phi node
				{
					int line_no=new Integer(field[0].trim());
					phiList.addLast(line_no);
				}			
				
				stmt=rafSSAExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("packageDCGProgInfo.getPhiNodesList(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageDCGProgInfo.getPhiNodesList(): Problem with IO exception." + e);}  
        
        return phiList;
	}



	// Computing the list of Phi Sequences in the SSA program
	public LinkedList computePhiSeq(int source)
	{
		
		// List of real nodes and Phi nodes
		LinkedList phiList=getPhiNodesList();
		LinkedList realList=getRealNodesList();
		
        LinkedList PhiSeq=new LinkedList();
        	
        String seq=Integer.toString(source); // Real source
        
        int target=0;
        for(; target<SSAProgSize;target++) // For all target
        {
        	if(SSAPDGmatrix[source][target]==2) // Is there a data edge?
        	{
        		
        		if(super.isExist(target, phiList)) // Is the target a Phi node? yes, got a phi edge..
        		{
        			// Add to the list, and treat the target as the new phi source and recursively go
           			seq=seq+":"+target;        			
        			source=target;
        			target=0;
        			
        			// Looking for data edge from the new PHI node to any REAL node
        			// If found that would be a Phi Sequence
        			for(int realnode=0; realnode<SSAProgSize; realnode++) 
        			{
	        			if(SSAPDGmatrix[source][realnode]==2 && super.isExist(realnode, realList))
        				{
	        				PhiSeq.addLast(seq+":"+realnode);
	        			}
	        		}
        		}
        	}
        }
        
        // Returning the list of Phi Sequence starting with real node "source"
        return PhiSeq;
	}



	// Refining DCG based on unrealizable dependence paths
	public LinkedList refineDCG()
	{
		// Contains the information of refinement happens
		LinkedList refineInfo=new LinkedList();
		
		// Get list of real and phi nodes
		LinkedList realList=getRealNodesList();
		LinkedList phiList=getPhiNodesList();
		
		for(int node=0; node<SSAProgSize; node++) // For all nodes "node" in the DCG
		{		
			if(isExist(node, realList)) // if it is real node
			{
				
				// Extract DEFINED variable and LABEL of the real node "node"
				String defVar=super.getDef(rafSSAExtractInfoFile, node);
				String nodeLabel=super.getLabel(rafSSAExtractInfoFile, node);
				
				
				/*// For all CDG edges "cntrl--->node"
				for(int cntrl=0; cntrl<ProgSize; cntrl++) 
				{
					
					if(SSAPDGmatrix[cntrl][node]==1)
					{
						if(! isSatCDGedge(cntrl, node, "T", DCGmatrix[cntrl][node], traceSemantics))
						{
							// "node" never be executable under the control node "cntrl"
							String cntrlLabel=getLabel(rafSSAExtractInfoFile, cntrl);
							
							refineInfo.addLast(new String(cntrlLabel+" --->T "+nodeLabel+" is Unrealizable!!!"));
							
							// Refine the control edge
							SSAPDGmatrix[cntrl][node]=0;
							
							// Refine the corresponding data edges with "node" as source
							for(int data=0; data<ProgSize; data++)
							{
								String dataLabel=getLabel(rafSSAExtractInfoFile, data);							
								refineInfo.addLast(new String(nodeLabel+" --->"+defVar+" "+dataLabel+" is Unrealizable!!!"));
								SSAPDGmatrix[node][data]=0;
							}
						}
					}
					else if (SSAPDGmatrix[cntrl][node]==-1)
					{
						if(! isSatCDGedge(cntrl, node, "F", DCGmatrix[cntrl][node], traceSemantics))
						{
							// "node" never be executable under the control node "cntrl"
							String cntrlLabel=getLabel(rafSSAExtractInfoFile, cntrl);
							
							refineInfo.addLast(new String(cntrlLabel+" --->F "+nodeLabel+" is Unrealizable!!!"));
							
							// Refine the control edge
							SSAPDGmatrix[cntrl][node]=0;
							
							// Refine the corresponding data edges with "node" as source
							for(int data=0; data<ProgSize; data++)
							{
								String dataLabel=getLabel(rafSSAExtractInfoFile, data);							
								refineInfo.addLast(new String(nodeLabel+" --->"+defVar+" "+dataLabel+" is Unrealizable!!!"));
								SSAPDGmatrix[node][data]=0;
							}
						}	
					}
				}
				
				// For all DDG edge "node ---> data"
				for(int data=0; data<ProgSize; data++) 
				{
					if(SSAPDGmatrix[node][data]==2 &&  isExist(data, realList))
					{
												
						boolean flag=false;
						
						// Extracting indexes in "trace" where "node" occurs
						LinkedList listIndexes=getTraceIndex(node, traceSemantics);
												
						ListIterator itrIndexes=listIndexes.listIterator();
						while(itrIndexes.hasNext())
						{
							// For all occurrance indexes of "node" in the trace
							int tracePosition=((Integer)itrIndexes.next()).intValue();
							
							if(isSatDDGedge(node, data, DCGmatrix[node][data], tracePosition, traceSemantics))
							{
								// DDG edge "node ---> data" is satisfied
								flag=true; break;
							}
						}
						
						if(!flag)
						{
							// DDG edge "node ---> data" is not satisfied
							// Refine the corresponding data edges with "node" as source
							String dataLabel=getLabel(rafSSAExtractInfoFile, data);						
							refineInfo.addLast(new String(nodeLabel+" --->"+defVar+"  "+dataLabel+"  is Unrealizable!!!"));							
        					SSAPDGmatrix[node][data]=0;
						}
					}
				}*/
				
				
				//Generating list of Phi Sequences that started from the real node "node"				
				LinkedList listPhiSeq=computePhiSeq(node);
				
				// Initializing flag to "true" indicating all Phi Sequences are not satified
				boolean flag=true;
				
				if(! listPhiSeq.isEmpty())
				{
					// For all phi sequences starting form "node"
					ListIterator itrPhiSeq=listPhiSeq.listIterator();
					while(itrPhiSeq.hasNext())
					{
						String strPhiSeq=itrPhiSeq.next().toString(); 
						
						//for(int i=0;i<atomicTraceSemantics.length;i++)
						//{					
							if(isSatPhiSeq(strPhiSeq, traceSemantics))
							{
								// Atleast one Phi Sequence is satisfied
								flag=false;
								break;
							}
						//}
					}
				}
				else
				{
					// No Phi sequence exist with "node" as source
					flag=false;
				}
				
				if(flag==true)
				{
					// All Phi Sequences are not satified
					String[] nodeToken=listPhiSeq.get(0).toString().split(":");
					int nextNode=Integer.parseInt(nodeToken[1]);
					
					// Refine the data dependences associated with the "node"
					if(SSAPDGmatrix[node][nextNode]==2 && isExist(nextNode, phiList))
        			{
        				refineInfo.addLast(new String(defVar+" from "+nodeLabel+" never reachable to any target node!!!"));
        				SSAPDGmatrix[node][nextNode]=0;
        			}
        			else
        			{
        				System.out.println("packageDCGProgInfo.refineDCG(): corresponding unrealizable edge from "+node+" to "+nextNode+" of PhiSequence does not exist!!!");
        			}
				}				
			}
		}
		
		// Return the list of refinements that happen
		return refineInfo;
	}
	
	
	
	// Performing slicing based on the refined matrix "SSAPDGmatrix"
	public boolean performSlicing(String label, LinkedList OriginalVarsList, String File_Out_Slice_Preview, String File_Out_Slice_ExtractInfo)
	{
		boolean flag=true;
				
		// Does the real node "label" belong to the SSA program's labels? Checking....
		int SSAline_no=super.getLineNo(rafSSAExtractInfoFile, label); 
		
		if(!(SSAline_no>=0 && SSAline_no<=(SSAProgSize-1))) 
		{
			flag=false;
			System.out.println("packageDCGProgInfo: performSlicing(): Please provide correct Line Number!!!!");
		}
		
		// Does the list of real variables "OriginalVarsList" in the criteria actually defined or used at "SSAline_no"
		// Because we are performing PDG-based slicing
		LinkedList SSAVarsList=new LinkedList();
		LinkedList OriginalUseList=new LinkedList();
		String OriginalDef="";
			
		LinkedList SSAUseList=super.getUseVars(rafSSAExtractInfoFile, SSAline_no); //List of USED variables in SSA form at "SSAline_no"
		ListIterator itrSSA=SSAUseList.listIterator();
		while(itrSSA.hasNext())
		{
			String SSAUseVar=itrSSA.next().toString(); 
			
			if(SSAUseVar.indexOf("_")>0)
			{
				String OriginalUseVar=SSAUseVar.substring(0, SSAUseVar.lastIndexOf("_")); // Changing into original form 
				OriginalUseList.addLast(OriginalUseVar);  
			}                                 
		}
		String SSADef=super.getDef(rafSSAExtractInfoFile, SSAline_no); // DEFINED veriable in SSA for at "SSAline_no"
		if(!SSADef.equals(""))
		{
			if(SSADef.indexOf("_")>0)
			{
				OriginalDef=SSADef.substring(0, SSADef.lastIndexOf("_")); //get def in original form
			}
		}
		
		ListIterator itrOriginal=OriginalVarsList.listIterator();
		while(itrOriginal.hasNext())
		{
			String OriginalVar=(String)itrOriginal.next();
			
			if(!OriginalVar.equals(OriginalDef) && !isExist(OriginalVar, OriginalUseList)) 
			{
				//Real variable in the criteria does not belong to the converted list of variables at "SSAline_no"
				flag=false;
				System.out.println("DCGProgInfo: performSlicing(): "+OriginalVar+" is not defined or used in the statement "+label+" !!!!");
				break;
			}
			else 
			{
				if(OriginalVar.equals(OriginalDef))
				{
					//convert variable in the criteria in SSA form
					SSAVarsList.addLast(SSADef); 
				}
				
				if(isExist(OriginalVar, OriginalUseList))
				{
					//convert variable in the criteria in SSA form
					int index=OriginalUseList.indexOf(OriginalVar);
					SSAVarsList.addLast(SSAUseList.get(index));      
				}
			}
		}
		
		if(flag)
		{
			// Criteria is ok
			
			 // "list" contains the nodes appearing in the slice
			LinkedList list=new LinkedList();	
				
			goBackward(SSAline_no, SSAVarsList, SSAPDGmatrix, list); // Call the function to traverse backward and to generates "list"
			writeToPreviewFile(InputPreview, list, File_Out_Slice_Preview); // write sliced "list" to "Preview File"
			writeToExtractInfoFile(rafInputExtractInfoFile, list, File_Out_Slice_ExtractInfo); // write sliced "list" to "ExtractInfo" file
				
			System.out.println("packageDCGProgInfo: performSlicing(): The End.");
		}
		
		return flag; // Return if slice is performed correctly or not.
	}
	
	
	// Traversing the PDG of the SSA program backward from the node of interest "line_no", and listing the vising nodes in "list"
	private void goBackward(int line_no, LinkedList VarsList, int[][] SSAPDGmatrix, LinkedList list)
	{
		if(line_no==0) // "Starting" node or "root" node of the PDG
		{
			// get the label associated with real node "line_no"
			String label=getLabel(rafSSAExtractInfoFile, line_no);
			
			// Add to the visited list
			if(!isExist(label, list))
				list.addLast(label);
		}
		else
		{
			// get the label associated with real node "line_no"
			String label=getLabel(rafSSAExtractInfoFile, line_no);
			if(!isExist(label, list) && isExist(line_no, getRealNodesList()))
			{
				// Add to the visited list
				list.addLast(label);
			}
			
			// iterate for all predecessor nodes of the current node of interest	
			for(int pred=0;pred<SSAProgSize;pred++)
			{
				// if predecessor node is a control node
				if(SSAPDGmatrix[pred][line_no]==1 || SSAPDGmatrix[pred][line_no]==-1 )
				{
					// traverse backward by considering the predecessor node as current node
					label=getLabel(rafSSAExtractInfoFile, pred);
					if(!isExist(label, list)) 
					{
						LinkedList useVarList=super.getUseVars(rafSSAExtractInfoFile, pred);
						goBackward(pred, useVarList, SSAPDGmatrix, list);
					}
				}	
				
				// if predecessor node is a non-control node
				if(SSAPDGmatrix[pred][line_no]==2)	
				{
					label=getLabel(rafSSAExtractInfoFile, pred);
					String def=super.getDef(rafSSAExtractInfoFile, pred);
					if(isExist(def, VarsList) && !isExist(label, list))
					{ 
					   // if the predecessor node defined the data that affects the current node
                       // traverse backward by considering the predecessor node as current node
						LinkedList useVarList=super.getUseVars(rafSSAExtractInfoFile, pred);
						goBackward(pred, useVarList, SSAPDGmatrix, list);
					}
				}		
			}			
		}
	}
	
	
	// Writting the statement to the preview file of slice corresponding to the nodes listed in "list"
	private void writeToPreviewFile(String Preview, LinkedList list, String File_Out_Slice_Preview)
	{
 
    	int cntrl=-1;
    	String cntrl_seq="_xx:-1";
    		
		try{	
		
			DataInputStream dis_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(Preview)))); 
      		PrintStream	ps_Slice_Preview = new PrintStream( new FileOutputStream(File_Out_Slice_Preview) );
      						
			while(dis_Preview.available()!=0) // Extracting statements from program's original preview file
			{	
			    String stmt=dis_Preview.readLine();
				if(stmt.length()!=0)
				{
					String[] spl_stmt=stmt.split(":");
					String label=spl_stmt[0].trim(); // Extracting Labels
					String cmd=spl_stmt[1].trim(); // Extracting Commands
					
					//If the command is the end of control statements
					if(cmd.equals("End-If") || cmd.equals("End-Else") || cmd.equals("End-While") || cmd.equals("End-Start")) 
					{
						// Extract the control block information you are supposed to be inside
						String extractCntrlString=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1);
						String[] field=extractCntrlString.trim().split(":");
						String extractCntrl=field[0].trim(); // Extracting type of control command
						int extractDepth=Integer.parseInt(field[1].trim()); // Extracting the control depth
						
						// If extracted conrol depth is ok with the actual control depth presently you are in
						if(extractDepth==cntrl && cmd.equals("End-"+extractCntrl))
						{
							ps_Slice_Preview.println(stmt); // Write the end of control statement to the slice preview file
							cntrl_seq=cntrl_seq.substring(0, cntrl_seq.lastIndexOf("_")).trim(); // Remove the control information
						}
						
						cntrl--; // Decrease the control depth
					}
					else if(stmt.equals("}Else{")) //If the command is the end of "if" and beginning of "else"
					{
						// Extract the control block information you are supposed to be inside
						String extractCntrlString=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1);
						String[] field=extractCntrlString.trim().split(":");
						String extractCntrl=field[0].trim(); // Extracting type of control command
						int extractDepth=Integer.parseInt(field[1].trim()); // Extracting the control depth
							
						// If extracted conrol depth is ok with the actual control depth presently you are in							
						if(extractDepth==cntrl && extractCntrl.equals("If"))
						{
							ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
							cntrl_seq=cntrl_seq.substring(0, cntrl_seq.lastIndexOf("_")).trim(); // Remove the control information
							cntrl_seq=cntrl_seq+"_Else:"+cntrl; // Add the control information
							                                    // Do not change the control depth
						}
					}
					else // If the command is not end or middle of control
					{
						
						Matcher match_start=patrn_start.matcher(cmd);
						boolean found_start=match_start.find();
						
						Matcher match_if=patrn_if.matcher(cmd);
						boolean found_if=match_if.find();
						
						Matcher match_while=patrn_while.matcher(cmd);
						boolean found_while=match_while.find();
						
						if(found_if) // "If" control node
						{
							++cntrl; // Increase control depth
							if(super.isExist(label, list)) // If "label" exist in the list of slice nodes
							{
								ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
								cntrl_seq=cntrl_seq+"_If:"+cntrl; // Add control information 
							}	
						}	
						else if(found_while) // "while" control node
						{
							++cntrl; // Increase control depth
							if(super.isExist(label, list)) // If "label" exist in the list of slice nodes
							{
								ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
								cntrl_seq=cntrl_seq+"_While:"+cntrl; // Add control information 
							}
						}
						else if(found_start) // "start" control node
						{
							++cntrl; // Increase control depth
							if(super.isExist(label, list)) // If "label" exist in the list of slice nodes
							{
								ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
								cntrl_seq=cntrl_seq+"_Start:"+cntrl; // Add control information 
							}
						}
						else if(cmd.toLowerCase().equals("stop;") || super.isExist(label, list)) // non-control statements and "label" exist in the list of slice nodes
						{
							ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
						}
					}
				}
			}
				
			// To check whether the cntrl_seq is back to its initial state i.e. propoer end of parsing of the program occurs
			if(cntrl_seq.equals("_xx:-1"))
			{
				ps_Slice_Preview.println("\n\n");			
			}
			else // Error found
			{
				System.out.println("packageDCGProgInfo.performSlicing().writeToPreviewFile(): Syntactic Error found.");
				ps_Slice_Preview.println("packageDCGProgInfo.performSlicing().writeToPreviewFile(): Syntactic Error found.");
				System.exit(1);
			}
			
        }catch (FileNotFoundException e){
        	 System.err.println("packageDCGProgInfo.performSlicing().writeToPreviewFile(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageDCGProgInfo.performSlicing().writeToPreviewFile(): Problem with IO exception." + e);}
 	}
	
	
	
	// Writing the detail information of the sliced code 
	private void writeToExtractInfoFile(RandomAccessFile rafExtractInfo, LinkedList list, String File_Out_Slice_ExtractInfo)
	{

		try{	
		
      		PrintStream	ps_Slice_ExtractInfo = new PrintStream( new FileOutputStream(File_Out_Slice_ExtractInfo) );
      		
      		// To extract those lines from the ExtractInfo file that exist in "list"
			rafExtractInfo.seek(0);
			String stmt=rafExtractInfo.readLine().trim();

			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				String label=field[6].trim().toString();
				
				// Checking for existence of the extracted line in the "list" or it is "stop" command
				if(field[5].trim().toLowerCase().equals("stop") || super.isExist(label, list))
				{
					ps_Slice_ExtractInfo.println(stmt); // Then, write to file
				}
					
				stmt=rafExtractInfo.readLine().trim(); // Go to next statement detail		
			}
				
			ps_Slice_ExtractInfo.println("\n\n"); //put two extra empty lines			
			
        }catch (FileNotFoundException e){
        	 System.err.println("packageDCGProgInfo.performSlicing().writeToExtractInfoFile(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageDCGProgInfo.performSlicing().writeToExtractInfoFile(): Problem with IO exception." + e);}
 	}
	
}