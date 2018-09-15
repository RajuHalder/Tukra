/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This class converts a program into Static Single Assignment (SSA) form
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/



package packageSSAconversion;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

import packageMyLibrary.*;
import packageMatrix.*;
import packageProgInfo.*;


public class genSSAform extends myFunctions implements myFilePaths, myPatterns
{	
	RandomAccessFile rafExtractInfoFile;
	int[][] CFGmatrix;
	int ProgSize;	
	String[] ProgVars;
	
	public genSSAform(){} // Default constructor
	
	// Constructor takes detail program information as an intermediate form
	public genSSAform(RandomAccessFile rafExtractInfoFile)
	{ 		
      	this.rafExtractInfoFile=rafExtractInfoFile; // Detail extracted program information
      	
      	CFGmatrix=(new formMatrix(rafExtractInfoFile)).getCFGmatrix(); //construct CDG matrix
      	
      	ProgSize=super.getProgSize(rafExtractInfoFile); //compute number of statements
      		
      	ProgVars=super.getProgVars(rafExtractInfoFile); //computes array of program variables
	}	
	
	//Compute Dominance of each node in the CFG
	public LinkedList[] getDominance()
	{
						
		int entry=0;
		int exit=ProgSize-1;
		
		// get predecessors and successors of each node	
		LinkedList[] PredecessorVector=getPredecessorVector(); 	
		LinkedList[] SuccessorVector=getSuccessorVector();    
		
		//Generate a sequence of nodes in order to processing......
		LinkedList queue=new LinkedList<Integer>();
		getProcessingSequence(queue, entry, SuccessorVector);		
			
		
		//Initialize "N" by the set of all nodes.....
		LinkedList<Integer> N=new LinkedList<Integer>();		
		for(int i=0;i<ProgSize;i++)
		{
			N.addLast(i);
		}
				
		// Initialize "domain(i)" by the node "i" itself for all nodes
		LinkedList[] domin=new LinkedList[ProgSize];
		for(int i=0;i<ProgSize;i++)
		{
			domin[i]=new LinkedList<Integer>();
			domin[i].addLast(i);
		}
		
		// Set "domain(i)" by "N", except the "root" node		
		ListIterator itr_seq=queue.listIterator();
		while(itr_seq.hasNext())
		{
			int i=((Integer)itr_seq.next()).intValue();
			
			if(i!=entry)
			{
				domin[i]=super.copyLinkedList(N);
			}
		}
		
				
		boolean change=true; // Set flag to true
		
		do{
			
			change=false; // set flag to false
						
			itr_seq=queue.listIterator();
			while(itr_seq.hasNext())
			{
				int i=((Integer)itr_seq.next()).intValue();
				
				if(i!=entry) // For all nodes except the "entry"
				{
				
					LinkedList T=new LinkedList<Integer>();
						
					if(PredecessorVector[i].size()!=0)
					{
						T=super.copyLinkedList(N); // Assign all nodes "N" to "T"
					
						ListIterator itr_pred=PredecessorVector[i].listIterator();
						while(itr_pred.hasNext())
						{
							int predID=((Integer)itr_pred.next()).intValue(); // For all predecessors of "i"
							
							LinkedList T_temp=copyLinkedList(T);
							
							ListIterator itr=T_temp.listIterator();
							while(itr.hasNext())
							{
								Object obj=itr.next();
				
								if(!domin[predID].contains(obj)) // Intersecting "domain(predID)" with "T", and put the result into "T"
									T.remove(obj);	
							}
						}
					}
					
					// Union-ing "i" with "T" and storing into "D"
					LinkedList D=super.copyLinkedList(T);
					if(! isExist(i, D))
						D.addLast(i);	
							
					if(! D.equals(domin[i]))  // If D==domain(i)? Checking for fix-point....
					{					
						change=true;
						domin[i]=super.copyLinkedList(D);
					}
				}
			}
			
		}while(change);
		
		// Returning sorted dominances 
		for(int i=entry;i<=exit;i++)
		{
			Collections.sort(domin[i]);
		}
		
		return domin;
	}
	
	// Function to create an ordered sequence used in computing dominances: Breadth First Traversal
	public void getProcessingSequence(LinkedList queue, int root, LinkedList[] vector)
	{
		LinkedList q=new LinkedList<Integer>();
		q.add(root);
		while(!q.isEmpty())
		{
			int n=((Integer)q.removeFirst()).intValue();
			queue.addLast(n);
			
			ListIterator itr=vector[n].listIterator();
			while(itr.hasNext())
			{
				int val=((Integer)itr.next()).intValue();
				if(! isExist(val, queue))
					q.addLast(val);
			}
		}
	}
	
	//Compute Immediate Dominance of each node in the CFG
	public LinkedList[] getImmediateDominance()
	{
		// Computing dominance for all nodes
		LinkedList[] Domin=getDominance();
		
		// Assiging "ImdDomain(i)"="Domain(i)"-{i} 
		LinkedList[] ImdDomin=new LinkedList[ProgSize];	
			
		for(int i=0;i<ProgSize;i++)
		{
			// First setting dominance set "Domain(i)" as immediate dominance set "ImdDomain(i)"
			ImdDomin[i]=super.copyLinkedList(Domin[i]); 
			
			ListIterator itr_rem=ImdDomin[i].listIterator();
			while(itr_rem.hasNext())
			{
				int val=((Integer)itr_rem.next()).intValue();				
				if(val==i){itr_rem.remove();} // Then, removing {i}				 
			}
		}
		
		// For all nodes "n" except the "entry" node
		for(int n=1;n<ProgSize;n++)
		{
			
			int index=0;				
			
			while(index>=0 && index<=ImdDomin[n].size()-1)
			{
				
				Object obj=ImdDomin[n].get(index); 
				
				int s=((Integer)obj).intValue(); //For all nodes "s" in "ImdDomin(n)"
				
				LinkedList list=super.copyLinkedList(ImdDomin[n]);
				ListIterator itr_rem=list.listIterator();
				while(itr_rem.hasNext())
				{
					int val=((Integer)itr_rem.next()).intValue();				
					if(val==s){itr_rem.remove();} // "list"	= "ImdDomin(n)" - {s}		 
				}
				
				
				ListIterator itr=list.listIterator();
				while(itr.hasNext())
				{
					int t=((Integer)itr.next()).intValue(); // For each "t" in "list"
					
					if( isExist(t, ImdDomin[s])) // "t" in "ImdDomain(s)"?
					{
						itr_rem=ImdDomin[n].listIterator();
						while(itr_rem.hasNext())
						{
							int val=((Integer)itr_rem.next()).intValue();				
							if(val==t){itr_rem.remove();} // "ImdDomin[n]"= "ImdDomin[n]" -	{t}		 
						}
					}
				}	
				
				index=ImdDomin[n].indexOf(s)+1; // Going for next "s" in "ImdDomin[n]"
			}		
		}
				
		return ImdDomin; // Returns immediate dominance for all nodes
	}
	
	
	
	//compute strict dominance for each node in the CFG
	public LinkedList[] getStrictDominance()
	{
		
		// Computing dominance for all nodes
		LinkedList[] Domin=getDominance(); 
		
		LinkedList[] StrictDomin=new LinkedList[ProgSize];		
		for(int i=0;i<ProgSize;i++)
		{
			// First setting dominance set "Domain(i)" as strict dominance set "StrictDomain(i)"
			StrictDomin[i]=super.copyLinkedList(Domin[i]); 
			
			ListIterator itr_rem=StrictDomin[i].listIterator();
			while(itr_rem.hasNext())
			{
				int val=((Integer)itr_rem.next()).intValue();				
				if(val==i){itr_rem.remove();}	// Removing the node "i" itselt			 
			}
		}
		
		return StrictDomin;
		
	}
	
	//Compute PostDominance of each node in the CFG
	public LinkedList[] getPostDominance()
	{

		//Reverse the edges and treating "start" as "exist" and "exist" as "start"......so working with SuccessorVector.....
		
		int entry=ProgSize-1;
		int exit=0;

		LinkedList[] PredecessorVector=getPredecessorVector();		
		LinkedList[] SuccessorVector=getSuccessorVector();	
						
		//Generate a sequence of nodes in order to processing......
		LinkedList queue=new LinkedList<Integer>();
		getProcessingSequence(queue, entry, PredecessorVector);	
		
		//Initialize "N" by all nodes.....But in reverse....
		LinkedList<Integer> N=new LinkedList<Integer>();		
		for(int i=0;i<ProgSize;i++)
		{
			N.addLast(i);
		}
		
		// Initialize "PostDomain(i)" by the node "i" itself for all nodes
		LinkedList[] PostDomin=new LinkedList[ProgSize];
		for(int i=0;i<ProgSize;i++)
		{
			PostDomin[i]=new LinkedList<Integer>();
			PostDomin[i].addLast(i);
		}
		
		// Setting "PostDomain(i)" by "N", except the "entry" node		
		ListIterator itr_seq=queue.listIterator();
		while(itr_seq.hasNext())
		{
			int i=((Integer)itr_seq.next()).intValue();
			
			if(i!=entry)
			{
				PostDomin[i]=super.copyLinkedList(N);
			}
		}
		
		boolean change=true; // Set flag to ture	
		
		do{
			
			change=false; // Set flag to false
			
			itr_seq=queue.listIterator();
			
			while(itr_seq.hasNext())
			{
				int i=((Integer)itr_seq.next()).intValue();
				
				if(i!=entry) // Iterate for all nodes, except the entry node
				{
					LinkedList T=new LinkedList<Integer>();
						
					if(SuccessorVector[i].size()!=0)
					{
						T=super.copyLinkedList(N);  // Assign all nodes "N" to "T"
					
						ListIterator itr_succ=SuccessorVector[i].listIterator();
						while(itr_succ.hasNext())
						{
							int succID=((Integer)itr_succ.next()).intValue(); // For all successor of "i"
							
							LinkedList T_temp=copyLinkedList(T);
							
							ListIterator itr=T_temp.listIterator();
							while(itr.hasNext())
							{
								Object obj=itr.next();
				
								if(!PostDomin[succID].contains(obj))
									T.remove(obj);	// Intersecting "PostDomain(succID)" with "T", and put the result into "T"
							}
						}
					}
					
					// Union-ing "i" with "T" and storing into "D"
					LinkedList D=super.copyLinkedList(T);
					if(! isExist(i, D))
						D.addLast(i);	
								
					if(! D.equals(PostDomin[i])) // Is D==PostDomain(i)? checking for fix-point....
					{							
						change=true;
						PostDomin[i]=super.copyLinkedList(D);	
					}
				}
			}
			
		}while(change);
		
		for(int i=0;i<ProgSize;i++)
		{
			Collections.sort(PostDomin[i]); // Sorting the post dominances
		}					
	
		return PostDomin;	// Returning post dominaces for all nodes	
	}
	
	
	
	// To compute strict post dominances for all nodes
	public LinkedList[] getStrictPostDominance()
	{
		
		// Copute post dominances for all nodes
		LinkedList[] PostDomain=getPostDominance();
		
		// First setting post dominance set "PostDomain(i)" as strict post-dominance set "StrictPostDomain(i)"
		LinkedList[] StrictPostDomain=new LinkedList[ProgSize];		
		for(int i=0;i<ProgSize;i++)
		{
			StrictPostDomain[i]=super.copyLinkedList(PostDomain[i]);
			
			ListIterator itr_rem=StrictPostDomain[i].listIterator();
			while(itr_rem.hasNext())
			{
				int val=((Integer)itr_rem.next()).intValue();				
				if(val==i){itr_rem.remove();}	// Removing "i" itself			 
			}
		}
		
		return StrictPostDomain;
		
	}
	
	
	// Is x dominates y?
	public boolean isDominate(int x, int y, LinkedList[] matrix)
	{
		if(isExist(x, matrix[y]))
			return true;
		else
			return false;
	}
	
	
	// To compute Dominance Tree	
	public int[][] getDominatorTree(LinkedList[] idom)
	{
		
		// Initializing the array representing dominance tree
		int[][] domTree= new int[ProgSize][];
					
		for(int i=0; i<domTree.length;i++)
		{
			domTree[i]=new int[ProgSize];
			
			for(int j=0; j<domTree[i].length;j++)
			{
				domTree[i][j]=0;
			}
		}
		
		// Iterating for each node "i"
		for(int i=1; i<domTree.length;i++)
		{
			if(idom[i].size()==1) // Good!!! "i" is the immediate dominance of only one node 
			{
				ListIterator itr=idom[i].listIterator();
				if(itr.hasNext())
				{
					int val=((Integer)itr.next()).intValue();
					domTree[val][i]=1; // "i" is the immediate dominance of "val"	
				}
			}
			else if(idom[i].size()>1) 
			{
				System.out.println("packageSSAconversion.genSSAform.getDominatorTree(): More than one immediate dominators.....!!!");	
			}
		}
		
		return domTree;
		
	}
	
	// Post-Order Traversal of the dominance tree
	public void PostOrderTraversal(int[][] tree, int root, LinkedList list)
	{
		for(int i=0;i<tree[root].length;i++) // Scanning the edges from root, i.e. scanning children from left to right
		{
			if(tree[root][i]==1) // Is there any outgoing edge from root? 
			{
				PostOrderTraversal(tree, i, list); // Traverse the child in post-order 
			}
		}
		list.addLast(root); // Finally, add the "root" to the list
	}
	
	
	// Compute the set of successors of each node	
	public LinkedList[] getSuccessorVector()
	{
		LinkedList[] SuccessorVector=new LinkedList[ProgSize];
		for(int i=0; i<ProgSize;i++)
		{
			SuccessorVector[i]=new LinkedList<Integer>(); // Initializing successor list of "j"th node

			for(int j=0;j<ProgSize;j++)
			{
				if(CFGmatrix[i][j]==1 || CFGmatrix[i][j]==-1) // There is a control edge from "i" to "j" in the CFG
				{
				 	SuccessorVector[i].addLast(j); // add "j" as a successor of "i"
				}	
			}
			
			Collections.sort(SuccessorVector[i]); // Sort the successors for each node
		}
		return 	SuccessorVector; // Return successors of each node
	}
	
	
	
	// Compute the set of predecessors of each node		
	public LinkedList[] getPredecessorVector()
	{
		LinkedList[] PredecessorVector=new LinkedList[ProgSize];
		for(int j=0; j<ProgSize;j++)
		{
			PredecessorVector[j]=new LinkedList<Integer>(); // Initializing predecessor list of "j"th node

			for(int i=0;i<ProgSize;i++)
			{
				if(CFGmatrix[i][j]==1 || CFGmatrix[i][j]==-1) // There is a control edge from "i" to "j" in the CFG
				{
				 	PredecessorVector[j].addLast(i); // add "i" as a predecessor of "j"
				}	
			}
			
			Collections.sort(PredecessorVector[j]); // Sort the predecessors for each node
		}
		
		return PredecessorVector; // Return Predecessors of each node
	}
	
	
	//To Compute Dominance Frontier for each node in the CFG
	public LinkedList[] getDominanceFrontier()
	{
		
		//Initialize DF for each node with empty list
		LinkedList[] DF=new LinkedList[ProgSize];
		for(int i=0; i<ProgSize;i++)
		{
			DF[i]=new LinkedList<Integer>(); 
		}
		
		//Compute Predecessor and successor nodes
		LinkedList[] PredecessorVector=getPredecessorVector();
		LinkedList[] SuccessorVector=getSuccessorVector();  
		
		//Compute Immediate Dominance
		LinkedList[] idom=getImmediateDominance(); 
		LinkedList[] IDOMset=getIDOMset(idom);
		
		//Compute Dominance Tree
		int[][] tree=getDominatorTree(idom); 
		
		//Post Order Traversal of the dominance tree
		LinkedList postTrav=new LinkedList<Integer>();
		PostOrderTraversal(tree, 0, postTrav);  
		
		// Iterate nodes in post order of its dominace tree
		ListIterator itr=postTrav.listIterator();
		while(itr.hasNext())
		{
			int i=((Integer)itr.next()).intValue();	// "i" is the index of the current node
			
			DF[i]=new LinkedList<Integer>(); // initialize "DF(i)" with empty list
			
			//Compute Local Component			
			ListIterator itr_succ=SuccessorVector[i].listIterator();
			while(itr_succ.hasNext())
			{
				int y=((Integer)itr_succ.next()).intValue(); // For each "y" in "successor(i)"
				
				if(! isDominate(y, i, IDOMset)) // if "y" not belong to "IDOMset(i)"
				{
					if(!isExist(y, DF[i])) // Add "y" to "DF(i)"
						DF[i].addLast(y);
				}	
			}
			
			//Add on up component			
			ListIterator itr_idom_set=IDOMset[i].listIterator();
			while(itr_idom_set.hasNext())
			{
				int z=((Integer)itr_idom_set.next()).intValue(); // For each "z" in "IDOMset(i)"
				
				ListIterator itr_df=DF[z].listIterator(); 
				while(itr_df.hasNext())
				{
					int y=((Integer)itr_df.next()).intValue(); // For each "y" in "DF(z)"
					
					if(! isDominate(y, i, IDOMset)) // "y" not in "IDOMset(i)"
					{
						if(!isExist(y, DF[i])) // Add "y" to "DF(i)"
							DF[i].addLast(y);
					}
				}
			}
		}
		
		return DF; // Returns the dominance frontier
	}
	
	// Given immediate dominance for all nodes: "getIDOMset" computes Immediate Dominance Set for all nodes
	public LinkedList[] getIDOMset(LinkedList[] idom)
	{
		// Initializing IDOMset by empty linkedlists corresponding to all nodes
		LinkedList[] IDOMset=new LinkedList[idom.length];
		for(int i=0;i<idom.length;i++)
		{
			IDOMset[i]=new LinkedList<Integer>();
		}
		
		
		for(int i=0;i<idom.length;i++) // For all node "i"
		{
			ListIterator itr=idom[i].listIterator();
			while(itr.hasNext())
			{	
				int z=((Integer)itr.next()).intValue(); // For all nodes "z" whose immediate dominance is "i"
				IDOMset[z].addLast(i); // "IDOMset[z]" contains those nodes that are immediate dominance of "z"
			}
		}
		
		return IDOMset;
	}
	
	
	//Iterated Dominance Frontier: S is a set of all nodes that defines a particular program variable
	public LinkedList getDF_Plus(LinkedList S)
	{
			
		boolean change=true;
		
		LinkedList DFP=getDFset(S);  //Call getDFset() function

		do{
			change = false;
					
			LinkedList total=super.getUnion(S, DFP); // Add the result of "getDFset(S)" with "S" itself: Union of two sets		
						
			LinkedList D=getDFset(total); // Again perform "getDFset()"
			
			if(! D.equals(DFP)) // Reaches fix-point?
			{
				DFP=copyLinkedList(D);
				change=true;
			}			
		
		}while( change);
		
		return DFP;
	}
	
	
	// Function called when computing "Iterated Dominance Frontier"
	public LinkedList getDFset(LinkedList S)
	{
		LinkedList D=new LinkedList<Integer>();
		
		//Compute Dominance Frontiers for all CFG nodes 
		LinkedList[] DF=getDominanceFrontier(); 
		
		// Union the dominance frontiers for all nodes in the parameter
		ListIterator itr=S.listIterator();
		while(itr.hasNext())
		{
			int x=((Integer)itr.next()).intValue(); 
			
			ListIterator itr_df=DF[x].listIterator();
			while(itr_df.hasNext())
			{
				int y=((Integer)itr_df.next()).intValue();
				if(! isExist(y, D))
				{
					D.addLast(y);
				}
			}
			
		}
		
		return D;
	}
	
	
	/* PhiElement stores information regarding a phi statement - 
	 * source: original variable for which phi is needed 
	 * def: defined renamed variable for Phi Statement
	 * useVars: used renamed variables for Phi Statement */
	 
	class PhiElement
	{
		String source;
		String def;
		LinkedList useVars;
		
		// constructor with original source "s"
		PhiElement(String s)
		{
			source=s;
			def="";
			useVars=new LinkedList<String>();
		}
		
		// Return the original source variable
		String getSource()
		{
			return source;	
		}
			
		// set "d" as defined variable	
		void setDef(String d)
		{
			def=d;
		}
		
		// Set "uv" as the use variable
		void setUseVar(String uv)
		{
			if(!isExist(uv,useVars) && !uv.equals(def))
				useVars.addLast(uv);
		}	
		
		// Is var original source variable?
		boolean isSource(String var)
		{
			if(var.equals(source))
				return true;
			else
				return false;
		}
		
		// Displaying Phi Statement in the form "x3<<phi(x1, x2)"
		void display()
		{
			System.out.print(def+ "<<phi(");
			
			ListIterator itr=useVars.listIterator();
			if(itr.hasNext())
			{
				String str=itr.next().toString();				
				System.out.print(str);
			}
			while(itr.hasNext())
			{
				String str=itr.next().toString();				
				System.out.print(","+str);	
			}
			
			System.out.print(")");
		}
		
		// Returning Phi Statement in the form "x3<<phi(x1, x2)"
		String getPhiStatement() 
		{
			String result=def+ "<<phi(";
			
			ListIterator itr=useVars.listIterator();
			if(itr.hasNext())
			{
				String str=itr.next().toString();				
				result=result+str;
			}
			while(itr.hasNext())
			{
				String str=itr.next().toString();				
				result=result+","+str;	
			}
			
			result=result+")";
			
			return result;			
		}
	}
	
	//At each node of the CFG, generate a list of variables for which Phi is required
	public LinkedList[] insertPhiNodes()
	{
		int entry=0;
		String str="";
		
		/* "PhiVector" stores a set of "PhiElement" (each representing a Phi statement for a variable)
		 * associated with each nodes in the CFG
		 * Initialize the list with empty for each node */
		 
		LinkedList[] PhiVector=new LinkedList[ProgSize];
		for(int n=0;n<ProgSize;n++)
		{
			PhiVector[n]=new LinkedList<PhiElement>();
		}
		
		// For each variable, collect its defining statements
		for(int i=0; i<ProgVars.length;i++)
		{
			String var=ProgVars[i];
		
			LinkedList defNodes=super.getDefinedLines(rafExtractInfoFile, var); //Computes the list of defining statements for "var"
			if(defNodes.size()>1) //If no. of defining nodes for a variable is more than one
			{
				defNodes.addLast(entry); //Insert node 0 by default

				LinkedList DFset=getDF_Plus(defNodes); //Computes dominance frontiers for the set of defining nodes for the variable "var"

				//Adding info that Phi statement for "var" is needed at all nodes in DFset
				ListIterator itr=DFset.listIterator(); 
				while(itr.hasNext())                   
				{
					int n=((Integer)itr.next()).intValue();
				
					////checking if Phi statement for "var" at n is already added or not
					boolean present=false;
					ListIterator itr_phiElements=PhiVector[n].listIterator();
					while(itr_phiElements.hasNext())
					{
						PhiElement element=(PhiElement)itr_phiElements.next();
						if(element.isSource(var)) //yes present
						{
							present=true; 
							break;
						}
					}
				
					if(present==false) //if not present
					{
						PhiElement element=new PhiElement(var);
						PhiVector[n].addLast(element); //adding information that at node n, there is need of Phi statement for "var"
					}
				}
			}
		}
		
		return PhiVector;
	}

	
	/* To rename all original program statements and store them into newcommands 
	 * To add new def and new use variables in all the Phi Statements associated with each node */ 
	public void performRename(LinkedList[] PhiVector, String[] newCommands)
	{
		int entryNode=0;
		
		// Initializing stack associated with each node
		Stack[] stk=new Stack[ProgVars.length];
		for(int i=0; i<ProgVars.length; i++)
		{
			stk[i]=new Stack();
		}
		
		// Initializing counter associated with each node
		int[] counter=new int[ProgVars.length];
		for(int i=0; i<ProgVars.length; i++)
		{
			counter[i]=0;
		}
		
		// Computing successor nodes for each nodes
		LinkedList[] SuccessorVector=getSuccessorVector();
		
		// Computing immediate dominances
		LinkedList[] idom=getImmediateDominance();
		
		
		// Computing dominace tree
		int[][] idom_tree=getDominatorTree(idom);
		
		// Keeps track which nodes are already visited
		LinkedList visited=new LinkedList(); 		
		
		// Function call to rename variables in SSA form
		renameBlock(entryNode, stk, counter, SuccessorVector, idom_tree, visited, PhiVector, newCommands);
					
	}
	
	
	// Function to rename variables in SSA form of the program
	public void renameBlock(int node, Stack[] stk, int[] counter, LinkedList[] SuccessorVector, int[][] idom_tree, LinkedList visited, LinkedList[] PhiVector, String[] newCommands)
	{
		
		
		if(isExist(node, visited)) return; // is "node" already visited? then return...
		
		LinkedList toClear=new LinkedList();
		
		visited.addLast(node); // Just visited...add to te visited list
		
		
		//---Phi Nodes----Create New_Name for DEF variable
		ListIterator itr_phiElements=PhiVector[node].listIterator();
		while(itr_phiElements.hasNext())
		{
			PhiElement element=(PhiElement)itr_phiElements.next();
			
			String source=element.getSource();		
			String newDef=GenName(source, stk, counter);
			element.setDef(newDef);
			
			toClear.addLast(source);
		}
		
		//---Other Nodes---Replace all USE variables by New_Name and Create New_Name for DEF variable		
		String extractLine=extractInfoLine(rafExtractInfoFile, node);
		String[] field=extractLine.split(" ");
		
		String type=field[1].trim();			
		String cmd=field[5].trim();
		String use=field[4].trim();
		
		String def="";		
		String expr=cmd;
		
		if(type.equals("as"))
		{		
			String[] str=cmd.split("=");
			def=str[0].trim();	
			expr=str[1].trim();
		}
		
		//Replace all USE variables by New_Name........
		if(!use.equals("*"))
		{	

			String[] arr=use.split("\\|");  
			for(int i=0;i<arr.length;i++)
			{
				String use_variable=arr[i];
				int index=getIndex(use_variable, ProgVars);	
				
				if(!stk[index].empty())
				{		
					int new_counter_value=((Integer)stk[index].peek()).intValue();					
					String new_var=use_variable+"_"+new_counter_value;
					expr=replaceExprVar(expr, use_variable, new_var);
				}
				else
				{
					System.out.println("packageSSAconversion.genSSAform.renameBlock(): Stack is Empty: cant replace the variables in EXPRESSION!!!");	
				}					
			}
		}
		
		//Create New_Name for DEF variable......					
		if(type.equals("as"))
		{
			toClear.addLast(def);		
			String new_def=GenName(def, stk, counter);	
			String new_cmd=new_def+"="+expr;
			newCommands[node]=new_cmd;
		}
		else
		{
			newCommands[node]=expr;
		}
				
		//---For all Successors----Put the Phi_Parameters with New_Name....	
		ListIterator itr_succ=SuccessorVector[node].listIterator();
		while(itr_succ.hasNext())
		{
			int succ_node=((Integer)itr_succ.next()).intValue();
			System.out.println("Successor node"+succ_node);
			
			ListIterator itr_succ_phiElements=PhiVector[succ_node].listIterator();
			while(itr_succ_phiElements.hasNext())
			{
				PhiElement element=(PhiElement)itr_succ_phiElements.next();
				
				String source=element.getSource();
				
				int index=getIndex(source, ProgVars);

				if(!stk[index].empty())
				{		
					int new_counter_value=((Integer)stk[index].peek()).intValue();
					
					String new_var=source+"_"+new_counter_value;
					element.setUseVar(new_var);
				}
				else
				{
					System.out.println("packageSSAconversion.genSSAform.renameBlock(): Stack is Empty: cant put PHI parameter!!!");	
				}
			}				
		}
		
		
		//Do the same for all children in dominanator tree...........
		for(int child=0;child<idom_tree[node].length;child++)
		{
			if(idom_tree[node][child]==1)
			{
				renameBlock(child, stk, counter, SuccessorVector, idom_tree, visited, PhiVector, newCommands);
			}
		}
		
		ListIterator itr_clear=toClear.listIterator();
		while(itr_clear.hasNext())
		{
			String var=(itr_clear.next()).toString();
			int index=getIndex(var, ProgVars);
			if(! stk[index].empty())
			{	
				Object obj=stk[index].pop();
			}		
		}
				
	}
	
	// Function to get new name for "var"
	public String GenName(String var, Stack[] stk, int[] counter)
	{
		int index=getIndex(var, ProgVars);
		int i=counter[index];
		stk[index].push(i);
		counter[index]=i+1;
		
		return (new String(var+"_"+i));
	}
	
	// Function to replace "old_var" in the expression by the "new_var"
	public String replaceExprVar(String expr, String old_var, String new_var)
	{
		String result="";
		Pattern pat=Pattern.compile(old_var);
		Matcher mat=pat.matcher(expr);
		
		int start_index=0;
		int last_index=0;
		
		while(mat.find())
		{
			last_index=mat.start();
			
			result=result+expr.substring(start_index, last_index);
			result=result+new_var;
			
			start_index=mat.end();
		}
		
		result=result+expr.substring(start_index);
		
		if(result.equals(""))
			return expr;
		else
			return result;
	}
	
	// To generate SSA form of the program, and to write into files
	public void genSSAprogram(String Preview, String File_Out_SSA_Preview, String File_Out_SSA_ExtractInfo)
	{
		LinkedList[] PhiVector=insertPhiNodes(); //Getting details of the Phi Statements
		
		// Initializing array new commands that corresponds to array of old commands in the program
		String[] newCommands=new String[ProgSize];
		for(int i=0;i<ProgSize;i++)
		{
			newCommands[i]=new String("");
		}
		
		// Renaming the variables in SSA form and writing to "newCommands"	
		performRename(PhiVector, newCommands);
		
		int counter=-1;
		
		try{
			
	  		DataInputStream dis_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(Preview)))); 			
    		PrintStream	ps_SSA_Preview = new PrintStream( new FileOutputStream(File_Out_SSA_Preview) );
    					
			while(dis_Preview.available()!=0)
			{	
				String stmt=dis_Preview.readLine(); // Extracting old statements from Preview File
				
				if(stmt.length()!=0)
				{
					String[] spl_stmt=stmt.split(":");
					String label=spl_stmt[0].trim(); // Extracting label
					String cmd=spl_stmt[1].trim();	 // Extracting comand			
					
					
					if(cmd.equals("End-If")||cmd.equals("End-Else")||cmd.equals("End-While")||cmd.equals("End-Start")||cmd.equals("}Else{"))
					{		
						// End of control blocks: just write the same in SSA form			
						ps_SSA_Preview.println(stmt);
					}
					else
					{
						// Extract Line no.
						int line_no=super.getLineNo(rafExtractInfoFile, label); 
						if(line_no!=-1)
						{
							// Extracting Phi statements associated with the command at program point "line_no"
							ListIterator itr_phiElements=PhiVector[line_no].listIterator(); 
							while(itr_phiElements.hasNext())
							{
								// Get Phi statements
								PhiElement element=(PhiElement)itr_phiElements.next();				
								String Phistatement=element.getPhiStatement();
								
								// Write to the file by associating with "new label" started with "Q"
								ps_SSA_Preview.print("Q"+(++counter)+":");
								ps_SSA_Preview.print(Phistatement+";");
								ps_SSA_Preview.println();
							}
							
							String ins=newCommands[line_no]; // corresponding renamed statement for preview purpose
							
							// Extracting type of the old statement
							String type=super.getType(rafExtractInfoFile, line_no);
							
							if(type.equals("st")) // Start statement
							{
								ins=ins; 
							}
							else if(type.equals("if") ) // "if" conditional statement
							{
								ins="if("+ins+"){"; // rephrase the form of "if"
							}
							else if(type.equals("wh") ) //"while" conditional statement
							{
								ins="while("+ins+"){"; // rephrase the form of "while"
							}
							else // other non-control statements
							{
								ins=ins+";"; 
							}
							
							// Write the renamed command to preview file
							ps_SSA_Preview.print(label+":");
							ps_SSA_Preview.print(ins);
							ps_SSA_Preview.println();
						}
						else
						{
							System.out.println("packageSSAconversion.genSSAform.genSSAprogram(): Wrong Label.");
						}
					}
				}
			}
			
			ps_SSA_Preview.println("\n\n");
			
			ps_SSA_Preview.close();
			
			//Write details information in SSA form to the file "Extract_SSA_Info"............
			
			DataInputStream dis_SSA_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(File_Out_SSA_Preview))));
			PrintStream	ps_SSA_ExtractInfo = new PrintStream( new FileOutputStream(File_Out_SSA_ExtractInfo) );
			(new genProgInfo()).GenerateInformation(dis_SSA_Preview, ps_SSA_ExtractInfo);	
			
			
		}catch (FileNotFoundException e){
           System.err.println("packageSSAconversion.genSSAform.genSSAprogram(): Input File not found.");
 	  	}catch (IOException e){
           System.err.println("packageSSAconversion.genSSAform.genSSAprogram(): Problem with IO exception.");}
	}
	
	
	// To display elements of a linked list
	public void display(LinkedList S)
	{
		ListIterator itr=S.listIterator();
		while(itr.hasNext())
		{
			System.out.print(itr.next()+"  ");
		}
		System.out.println();
				
	}	
}