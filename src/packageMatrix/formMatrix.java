/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * These CLASSes define the methods that 
 * (i)   construct CFG and PDG of a given program
 * (ii)  perform PDG-based backward slicing
 * (iii) Draw Graphs
 *
 * Input: File containing all detail information of the program as an intermediate form 
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/

package packageMatrix;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

import packageMyLibrary.*;

public class formMatrix extends myFunctions implements myPatterns
{
	RandomAccessFile rafExtractInfoFile; // File contains all detail information of the program as an intermediate form 
	int ProgSize; //contains the size of the program, i.e. the no. of statements in the program
	String[] label; //contains the set of labels in the program


	/* constructor takes detail program information file in form of Random Access File Type 
	 * and computes program's size and set of labels associated with the program */
	public formMatrix(RandomAccessFile raf)
	{		
		rafExtractInfoFile=raf;
		ProgSize=super.getProgSize(raf); 
		label=super.getLabelSet(raf); 
	}
	
	
	// Generates control dependences in the PDG in the form of incidence matrix 
	public int[][] getPDGmatrix_cntrl()
	{
		 	
		// Initializing the "PDGmatrix_cntrl" matrix		
		int[][] PDGmatrix_cntrl= new int[ProgSize][];
					
		for(int i=0; i<PDGmatrix_cntrl.length;i++)
		{
			PDGmatrix_cntrl[i]=new int[ProgSize];
			
			for(int j=0; j<PDGmatrix_cntrl[i].length;j++)
			{
				PDGmatrix_cntrl[i][j]=0;
			}
		}

		// Compute the True-labelled control edge (+1) and the False-labelled control edge (-1)
		try{ 			
			
			// Scanning the program's detail from the file
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine().trim();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				int line_no=Integer.parseInt(field[0].trim()); // Extracting node number
				String[] spl_cntrl=field[2].trim().split(":");			
				int cntrl_node=Integer.parseInt(spl_cntrl[0].trim()); // Extracting the controlling node
				String Bool_Val=spl_cntrl[1].trim(); // Extracting truth value of control
			     
				if(line_no>=1 && cntrl_node>=0)
				{
					if(Bool_Val.equals("T"))
					{
						PDGmatrix_cntrl[cntrl_node][line_no]=1; // '1' indicate True-control
					}
					else 
					{
						PDGmatrix_cntrl[cntrl_node][line_no]=-1; // '-1' indicate False-control
					}
				}
				
				stmt=rafExtractInfoFile.readLine().trim();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("packageMatrix.formMatrix:getPDGmatrix_cntrl(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageMatrix.formMatrix:getPDGmatrix_cntrl(): Problem with IO exception." + e);}
			
		System.out.println("packageMatrix.formMatrix:getPDGmatrix_cntrl(): The End.");
		
		return PDGmatrix_cntrl; // Returning control dependence information of PDG 
	}
	
	
	
	// Generates data dependences in the PDG in the form of incidence matrix 
	public int[][] getPDGmatrix_data()
	{
			
		// Initialize the matrix "PDGmatrix_data"			
		int[][] PDGmatrix_data= new int[ProgSize][];					
		for(int i=0; i<PDGmatrix_data.length;i++)
		{
			PDGmatrix_data[i]=new int[ProgSize];
			
			for(int j=0; j<PDGmatrix_data[i].length;j++)
			{
				PDGmatrix_data[i][j]=0;
			}
		}
					
		 
		// Genarating incidence matrix of CFG			
		int[][] CFGmatrix=getCFGmatrix(); 
		
		// determine the list of predecessors for every nodes
		LinkedList[] PredecessorVector=new LinkedList[ProgSize];
		for(int j=0; j<ProgSize;j++)
		{
			PredecessorVector[j]=new LinkedList();

			for(int i=0;i<ProgSize;i++)
			{
				if(CFGmatrix[i][j]==1 || CFGmatrix[i][j]==-1) // There is a edge in the CFG from node "i" to node "j"
				{
				 	PredecessorVector[j].addLast(i); // Adding "i" as the predecessor of the node "j"
				}	
			}
		}
			
		// Initialize the Reaching Definitions for all nodes
		LinkedList[] RDVector=new LinkedList[ProgSize];
		LinkedList[] TempRDVector=new LinkedList[ProgSize];
				
		for(int nodeID=0; nodeID<ProgSize;nodeID++)
		{
			RDVector[nodeID]=new LinkedList();	
			TempRDVector[nodeID]=new LinkedList();	
		}
		
		
		// To determine the fix-point to get resultant Reaching Definitions		
		boolean fix_point=true;
		do{
			for(int nodeID=0; nodeID<ProgSize;nodeID++) // Copy "TempRDVector" to "RDVector"
			{
				TempRDVector[nodeID].clear();
				ListIterator itr = RDVector[nodeID].listIterator();
       			while (itr.hasNext()) 
       			{
					TempRDVector[nodeID].addLast(itr.next());
				}	
			}
			
			for(int nodeID=1; nodeID<ProgSize;nodeID++) // Iterate the functions to calculate Reaching Definitions for each node "nodeID"
			{
				iterateRDVector(RDVector, nodeID, PredecessorVector[nodeID]);	
			}
			
			fix_point=true; // Checking whether Fix-Point has reached: no changes occurs in "TempRDVector" and "RDVector"
			for(int nodeID=0; nodeID<ProgSize;nodeID++)
			{
				if(! RDVector[nodeID].equals(TempRDVector[nodeID]))
				{
					fix_point=false;
					break;	
				}
			}
			
		} while(! fix_point);
		
		
		// Fill the data dependences in the matrix: "2" denotes the data edges
		for(int nodeID=0; nodeID<ProgSize; nodeID++)
		{
			// Identifying USE variables at each node "nodeID"
			LinkedList useVars=super.getUseVars(rafExtractInfoFile, nodeID); 
			
			ListIterator itr = RDVector[nodeID].listIterator();
	   		while (itr.hasNext()) 
       		{
				String RDitem=itr.next().toString();
				String[] spl_RDitem=RDitem.split(":");
				
				// Identifying the reaching variables
				String ExtractRDVariable=spl_RDitem[0].trim(); 
				
				// Identifying "source" of reaching variables
				int ExtractRDLine=Integer.parseInt(spl_RDitem[1].trim()); 
				
				// Checking if reaching variable is actually used at "nodeID"
				if(super.isExist(ExtractRDVariable, useVars)) 
				{
					PDGmatrix_data[ExtractRDLine][nodeID]=2; // Add a data dependence from "source" to "nodeID"
				}
			}
		}	
		
		System.out.println("packageMatrix.formMatrix:getPDGmatrix_data(): The End.");
		
		return PDGmatrix_data; // Returning data dependence information of PDG 
	}

	
	// Computing reaching definitions of a given node
	private void iterateRDVector(LinkedList[] RDVector, int nodeID, LinkedList PredecessorList)
	{
			
					
		// Collect the Reaching Definition from all predecessors of "nodeID"
		LinkedList collectRD=new LinkedList();
		
		ListIterator itrPredecessor=PredecessorList.listIterator();
		while(itrPredecessor.hasNext())
		{ 
			int PredID=((Integer)itrPredecessor.next()).intValue();	
			
			// Get the detail information from the file "ExtractInfoFile" corresponding to the predecessor "PredID" 
			String stmt=super.extractInfoLine(rafExtractInfoFile, PredID);
			String[] field=stmt.split(" ");

			String type=field[1].trim(); // Extracting the type of predecessor node
									
			if(type.equals("as") || type.equals("pi")) // If predecessor node is "assignment" or "phi" node in SSA form
			{
				// Collecting all RD objects of the predecessor, except the one that is defined by the predecessor node				
				String def=field[3].trim();
						
				ListIterator itrPredRD = RDVector[PredID].listIterator();
				while(itrPredRD.hasNext())
				{
					String rdObj=itrPredRD.next().toString();
					String[] split_rdObj=rdObj.split(":");
					String ExtractVar=split_rdObj[0].trim();
								
					// Checking if the Reaching Definition object is not defined by the assignment or the phi node
					if(! def.equals(ExtractVar) && ! super.isExist(rdObj, collectRD))
					{	
						collectRD.addLast(rdObj);  // Add to the collection list
					}
				}
				
				// Add a new Reaching Definition object that is defined by the predecessor node			
				String newRD=new String(def+":"+PredID);
					
				if(! super.isExist(newRD, collectRD))
				{
					collectRD.addLast(newRD); // Add the new one to the collection list
				}	
			}
			else
			{
				// Otherwise, add all Predecessor's Reaching Definitions to the collection list
				ListIterator itrPredRD = RDVector[PredID].listIterator();
				while(itrPredRD.hasNext())
				{
					String rdObj=itrPredRD.next().toString();
						
					if(! super.isExist(rdObj, collectRD))
					{	
						collectRD.addLast(rdObj);
					}
				}
			}
		}
		
		// Put this collected reaching definitions as the Reaching definitions for nodeID
		RDVector[nodeID]=collectRD;
						
		System.out.println("packageMatrix.formMatrix:iterateRDVector(): The End");		
	}
	
	
	//Generate the incidence matrix for Program Dependence Graph
	public int[][] getPDGmatrix() 
	{
		
		// Initializing the matrix........
		int[][] PDGmatrix= new int[ProgSize][];
					
		for(int i=0; i<PDGmatrix.length;i++)
		{
			PDGmatrix[i]=new int[ProgSize];
			
			for(int j=0; j<PDGmatrix[i].length;j++)
			{
				PDGmatrix[i][j]=0;
			}
		}
		
		// Compute Control dependences information
		int[][] PDGmatrix_cntrl=getPDGmatrix_cntrl(); 
		
		// Compute Data dependences information
		int[][] PDGmatrix_data=getPDGmatrix_data(); 
		
		for(int i=0;i<ProgSize;i++)
		{
			for(int j=0;j<ProgSize;j++)
			{
				if(PDGmatrix_cntrl[i][j]!=0)
					PDGmatrix[i][j]=PDGmatrix_cntrl[i][j];	// adding control dependences information
			}	
		}
		
		for(int i=0;i<ProgSize;i++)
		{
			for(int j=0;j<ProgSize;j++)
			{
				
				if(PDGmatrix_data[i][j]!=0)
					PDGmatrix[i][j]=PDGmatrix_data[i][j];	// adding data dependences information
			}	
		}
		
		System.out.println("packageMatrix.formMatrix: getPDGmatrix(): The End.");
		
		return PDGmatrix; // Return PDG with all control- and data-dependences information			
	}
	
	
	
	// Generates CFG matrix in the form of incidence matrix
	public int[][] getCFGmatrix()
	{

		// Initialize the matrix for CFG	
		int[][] CFGmatrix= new int[ProgSize][];
					
		for(int i=0; i<CFGmatrix.length;i++)
		{
			CFGmatrix[i]=new int[ProgSize];
			
			for(int j=0; j<CFGmatrix[i].length;j++)
			{
				CFGmatrix[i][j]=0;
			}
		}
				
		// Putting control edges into CFG Matrix
		
		int[][] PDGmatrix_cntrl=getPDGmatrix_cntrl(); // compute control dependences information of PDG
		
		int root=0; 
		int last=PDGmatrix_cntrl[0].length-1; 
		int start=root; 
		for(int next=root+1; next<last; next++) // Iterate for all "next" nodes, except the "root" node and the "last" node
		{				
			if(PDGmatrix_cntrl[root][next]==1) // check for True-control edges from "root" node to scanned "next" nodes
			{
				//Call recursive function "putEdge()" to put edge from "start" node to "next" node
				putEdge(start, super.getType(rafExtractInfoFile, start), next, CFGmatrix, PDGmatrix_cntrl);  
				
				// Treating "next" node as "start" node
				start=next; 
			}
		}
		//Call recursive function "putEdge()" to put edge from "start" node to "last" node
		putEdge(start, super.getType(rafExtractInfoFile, start), last, CFGmatrix, PDGmatrix_cntrl); 
			
		System.out.println("packageMatrix.formMatrix: getCFGmatrix(): The End.");
			
		return CFGmatrix; // Returning CFG as incidence matrix	
	}
	
	
	// Recursive function to put control edges from "pred" to "succ"
	private void putEdge(int pred, String pred_type, int succ, int[][] CFGmatrix, int[][] PDGmatrix_cntrl)
	{
		int last = PDGmatrix_cntrl[pred].length-1;
		
		if(!pred_type.equals(""))
		{		
			if(!pred_type.equals("if") && !pred_type.equals("wh")) //"pred" node is not control nodes...put an edge
			{				
				CFGmatrix[pred][succ]=1;
			}
			else if(pred_type.equals("if"))  //"pred" node is "if" control nodes
			{
				int yes_start=-1;
				int yes_end=-1;
			
				for(int i=0;i<last; i++) //Identifying first node "yes_start" in "if"-block
				{
					if(PDGmatrix_cntrl[pred][i]==1)
					{
						yes_start=i;
						break;
					}
				}
				for(int i=0;i<last; i++) //Identifying last node "yes_end" in "if"-block
				{
					if(PDGmatrix_cntrl[pred][i]==1)
					{
						yes_end=i;
					}
				}
				
				if(yes_start!=-1 && yes_end!=-1)
				{

					// Put control edge from "pred" to the first node "yes_start" in the "if"-block
					CFGmatrix[pred][yes_start]=1;   

					// Recursively put edges between the nodes in the "if"-block
					for(int next=yes_start+1;next<=yes_end; next++)
					{		
						if(PDGmatrix_cntrl[pred][next]==1)
						{						
							putEdge(yes_start, super.getType(rafExtractInfoFile, yes_start), next, CFGmatrix, PDGmatrix_cntrl);
							yes_start=next;
						}				
					}
					
					// Recursively put control edge from the last node "yes_end" in the "if"-block to the "succ"
					putEdge(yes_start, super.getType(rafExtractInfoFile, yes_start), succ, CFGmatrix, PDGmatrix_cntrl);
					

					// For the "else"-block if present.....
				
					int no_start=-1;
					int no_end=-1;
			
					for(int i=0;i<last; i++) //Identifying first node "no_start" in the "else"-block
					{
						if(PDGmatrix_cntrl[pred][i]==-1)
						{
							no_start=i;
							break;
						}
					}
					for(int i=0;i<last; i++) //Identifying last node "no_end" in the "else"-block
					{
						if(PDGmatrix_cntrl[pred][i]==-1)
						{
							no_end=i;
						}
					}
					
					if(no_start!=-1 && no_end!=-1)
					{
						// Put control edge from "pred" to the first node "no_start" in the "else"-block
						CFGmatrix[pred][no_start]=-1;
						
						// Recursively put edges between the nodes in the "else"-block
						for(int next=no_start+1;next<=no_end; next++)
						{		
							if(PDGmatrix_cntrl[pred][next]==-1)
							{						
								putEdge(no_start, super.getType(rafExtractInfoFile, no_start), next, CFGmatrix, PDGmatrix_cntrl);
								no_start=next;
							}				
						}
						
						// Recursively put control edge from the last node "no_end" in the "else"-block to the "succ"
						putEdge(no_start, super.getType(rafExtractInfoFile, no_start), succ, CFGmatrix, PDGmatrix_cntrl);
						
					}
					else // No "else"-block present
					{
						CFGmatrix[pred][succ]=-1;
					}

				}
				else // No "if"-block present
				{
					CFGmatrix[pred][succ]=1;	
				}
			
			}				
			else if(pred_type.equals("wh"))  //"pred" node is "while" control nodes
			{
					
				int yes_start=-1;
				int yes_end=-1;
				
				for(int i=0;i<last; i++) //Identifying first node "yes_start" in "while"-block
				{
					if(PDGmatrix_cntrl[pred][i]==1)
					{
						yes_start=i;
						break;
					}
				}
				for(int i=0;i<last; i++) //Identifying last node "yes_end" in "while"-block
				{
					if(PDGmatrix_cntrl[pred][i]==1)
					{
						yes_end=i;
					}
				}
				
				
				if(yes_start!=-1 && yes_end!=-1)
				{
					// Put control edge from "pred" to the first node "yes_start" in the "while"-block
					CFGmatrix[pred][yes_start]=1;
					
					// Recursively put edges between the nodes in the "while"-block
					for(int next=yes_start+1;next<=yes_end; next++)
					{		
						if(PDGmatrix_cntrl[pred][next]==1)
						{						
							putEdge(yes_start, super.getType(rafExtractInfoFile, yes_start), next, CFGmatrix, PDGmatrix_cntrl);
							yes_start=next;
						}				
					}
					
					// If there is Phi node associated with the "while" statement -- return to that Phi node
					
					int saveNode=pred; // Save the "pred" node
					
					int prevNode=0;
					for(;prevNode<CFGmatrix.length;prevNode++)
					{
						if(CFGmatrix[prevNode][pred]!=0) // Check for predecessor of "pred"
						{
						 	if(! super.getType(rafExtractInfoFile, prevNode).equals("pi")) // the predecessor of "pred" is not phi node
						 	{
						 		// Recursively put control edge from the last node "yes_end" in the "while"-block to the "pred"
						 		putEdge(yes_start, super.getType(rafExtractInfoFile, yes_start), pred, CFGmatrix, PDGmatrix_cntrl);
								break;
							}
							else // Yes, the predecessor of "pred" is a phi node
							{
								pred=prevNode; // Treat that phi node as the "pred" node and find predecessor of it recursively
								prevNode=0;	
							}
						}
					}
					
					pred=saveNode; //Restore the "pred" node
					
					
					// For the false-block of "while", if present 					
					int no_start=-1;
					int no_end=-1;
				
					for(int i=0;i<last; i++) //Identifying first node "no_start" in the false-block of "while"
					{
						if(PDGmatrix_cntrl[pred][i]==-1)
						{
							no_start=i;
							break;
						}
					}
					for(int i=0;i<last; i++) //Identifying end node "no_end" in the false-block of "while"
					{
						if(PDGmatrix_cntrl[pred][i]==-1)
						{
							no_end=i;
						}
					}
					
					if(no_start!=-1 && no_end!=-1)
					{
						// Put control edge from "pred" to the first node "no_start" in the false-block of "while"
						CFGmatrix[pred][no_start]=-1;
						
						// Recursively put edges between the nodes in the false-block of "while"
						for(int next=no_start+1;next<=no_end; next++)
						{		
							if(PDGmatrix_cntrl[pred][next]==-1)
							{						
								putEdge(no_start, super.getType(rafExtractInfoFile, no_start), next, CFGmatrix, PDGmatrix_cntrl);
								no_start=next;
							}				
						}
						
						// Recursively put control edge from the last node "no_end" in the false-block of "while" to the "succ" node
						putEdge(no_start, super.getType(rafExtractInfoFile, no_start), succ, CFGmatrix, PDGmatrix_cntrl);
					}
					else // No false-block of "while" present
					{
						CFGmatrix[pred][succ]=-1;
	
					}
				}
				else // No "while"-block present
				{
					CFGmatrix[pred][succ]=1;	
				}
			}
		}
		else //  Type of the "pred" node is empty
		{
			System.out.println("packageMatrix.formMatrix: putEdge(): Problem Occurs-Type is not Right!!!.");
		}
				
	}
	
	
	// To draw graph from a given incidence matrix
	public void DrawMatix(int[][] mat)
	{
		
		Graph g=new Graph();
		g.Draw(mat, label); // Draw Graph
	}
	
	
	// To perform backward slicing based on the PDG w.r.t. <label, VarsList>
	// Write the sliced code to files "File_Out_Slice_Preview" and "File_Out_Slice_ExtractInfo"
	public boolean performSlicing(String label, LinkedList VarsList, String PreviewFilePath, String File_Out_Slice_Preview, String File_Out_Slice_ExtractInfo)
	{
		boolean flag=true;
		
		// Does the "label" belong to the input program's labels? Checking....
		int line_no=super.getLineNo(rafExtractInfoFile, label);
		
		if(!(line_no>=0 && line_no<=(ProgSize-1)))
		{
			flag=false;
			System.out.println("packageMatrix.formMatrix: performSlicing(): Please provide correct Line Number!!!!");
		}
		
		// Does the list of variables "VarsList" referring to program variables? Checking....
		String[] progVarsList=super.getProgVars(rafExtractInfoFile);
		
		ListIterator itr1=VarsList.listIterator();
		while(itr1.hasNext())
		{
			String var=(String)itr1.next();
			if(!super.isExist(var, progVarsList))
			{
				flag=false;
				System.out.println("packageMatrix.formMatrix: performSlicing(): "+var+" is not a Program Variable!!!!");
				break;
			}
		}
		
		// Extracting USE and DEF variables at "line_no"
		LinkedList useVarsList=super.getUseVars(rafExtractInfoFile, line_no);
		String def=super.getDef(rafExtractInfoFile, line_no);
		
		// Does the variables "VarsList" in the criteria actually defined or used at "line_no"
		// Because we are performing PDG-based slicing
		ListIterator itr2=VarsList.listIterator();
		while(itr2.hasNext())
		{
			String var=(String)itr2.next();
			if(!var.equals(def) && !isExist(var, useVarsList))
			{
				flag=false;
				System.out.println("packageMatrix.formMatrix: performSlicing(): "+var+" is not defined or used in the statement "+line_no+" !!!!");
				break;
			}
		}
		
		
		if(flag) // If everything is fine....go for slicing
		{
			int[][] PDGmatrix=getPDGmatrix(); // Create PDG for the program
			
			LinkedList list=new LinkedList(); // "list" contains the nodes appearing in the slice
				
			goBackward(line_no, VarsList, PDGmatrix, list); // Call the function to traverse backward and to generates "list"
			writeToPreviewFile(PreviewFilePath, list, File_Out_Slice_Preview); // write sliced "list" to "Preview File"
			writeToExtractInfoFile(list, File_Out_Slice_ExtractInfo); // write sliced "list" to "ExtractInfo" file
				
			System.out.println("packageMatrix.formMatrix: performSlicing(): The End.");
		}
		
		return flag; // Return if slice is performed correctly or not.
	}
	
	
	// Traversing the PDG backward from the node of interest "line_no", and listing the vising nodes in "list"
	private void goBackward(int line_no, LinkedList VarsList, int[][] PDGmatrix, LinkedList list)
	{
		if(!isExist(line_no, list)) // Add the current node denoted by "line_no" to the list
				list.addLast(line_no);
				
		
		if(line_no==0) // "Starting" node or "root" node of the PDG
		{
			return;
		}
		else
		{
			
			for(int pred=0;pred<ProgSize;pred++) // iterate for all predecessor nodes of the current node of interest
			{
				
				// if predecessor node is a control node
				if(PDGmatrix[pred][line_no]==1 || PDGmatrix[pred][line_no]==-1 ) 
				{
					
					if(!isExist(pred, list)) // traverse backward by considering the predecessor node as current node
					{
						LinkedList useVarList=super.getUseVars(rafExtractInfoFile, pred);
						goBackward(pred, useVarList, PDGmatrix, list);
					}
				}
					
				// if predecessor node is a non-control node
				if(PDGmatrix[pred][line_no]==2)	
				{
					String def=super.getDef(rafExtractInfoFile, pred);
					if(isExist(def, VarsList) && !isExist(pred, list)) // if the predecessor node defined the data that affects the current node
					{												   // traverse backward by considering the predecessor node as current node
						LinkedList useVarList=super.getUseVars(rafExtractInfoFile, pred);
						goBackward(pred, useVarList, PDGmatrix, list);
					}
				}		
			}			
		}
	}
	
	
	// Writting the statement to the preview file of slice corresponding to the nodes listed in "list"
	private void writeToPreviewFile(String PreviewFilePath, LinkedList list, String File_Out_Slice_Preview)
	{
    	    
    	int cntrl=-1;
    	String cntrl_seq="_xx:-1";
    		
		try{	
		
			DataInputStream dis_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(PreviewFilePath))));
      		PrintStream	ps_Slice_Preview = new PrintStream( new FileOutputStream(File_Out_Slice_Preview) );
      						
			while(dis_Preview.available()!=0) // Extracting statements from original preview program's file
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
						
						// get the line number of the statement having label name "label"
						int line_no=super.getLineNo(rafExtractInfoFile, label); 
						
						Matcher match_start=patrn_start.matcher(cmd);
						boolean found_start=match_start.find();
						
						Matcher match_if=patrn_if.matcher(cmd);
						boolean found_if=match_if.find();
						
						Matcher match_while=patrn_while.matcher(cmd);
						boolean found_while=match_while.find();
						
						if(found_if) // "If" control node
						{
							++cntrl; // Increase control depth
							if(super.isExist(line_no, list))   // If "line_no" exist in the list of slice nodes
							{
								ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
								cntrl_seq=cntrl_seq+"_If:"+cntrl; // Add control information 
							}	
						}	
						else if(found_while) // "While" control node
						{
							++cntrl; // Increase control depth
							if(super.isExist(line_no, list)) // If "line_no" exist in the list of slice nodes
							{
								ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
								cntrl_seq=cntrl_seq+"_While:"+cntrl; // Add control information 
							}
						}
						else if(found_start)  // "Start" control node
						{
							++cntrl; // Increase control depth
							if(super.isExist(line_no, list)) // If "line_no" exist in the list of slice nodes
							{
								ps_Slice_Preview.println(stmt); // Write the control statement to the slice preview file
								cntrl_seq=cntrl_seq+"_Start:"+cntrl; // Add control information 
							}
						}
						else if(cmd.toLowerCase().equals("stop;") || super.isExist(line_no, list))  // non-control statements and "line_no" exist in the list of slice nodes
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
			else
			{
				System.out.println(cntrl_seq);
				System.out.println("packageMatrix.formMatrix.performSlicing().writeToPreviewFile(): Syntactic Error found.");
				ps_Slice_Preview.println("packageMatrix.formMatrix.performSlicing().writeToPreviewFile(): Syntactic Error found.");
				System.exit(1);
			}
			
        }catch (FileNotFoundException e){
        	 System.err.println("packageMatrix.formMatrix:performSlicing():writeToPreviewFile(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageMatrix.formMatrix:performSlicing():writeToPreviewFile(): Problem with IO exception." + e);}
 	}
	
	
	// Writing the detail information of the sliced code 
	private void writeToExtractInfoFile(LinkedList list, String File_Out_Slice_ExtractInfo)
	{

		try{	
		
      		PrintStream	ps_Slice_ExtractInfo = new PrintStream( new FileOutputStream(File_Out_Slice_ExtractInfo) );
      		
      		// To extract those lines from the ExtractInfo file that exist in "list"
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine().trim();

			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				int line_no=Integer.parseInt(field[0].trim());
				
				// Checking for existence of the extracted line in the "list" or it is "stop" command
				if(field[5].trim().toLowerCase().equals("stop") || super.isExist(line_no, list)) 
				{
					ps_Slice_ExtractInfo.println(stmt); // Then write to file
				}
					
				stmt=rafExtractInfoFile.readLine().trim();		
			}
				
			ps_Slice_ExtractInfo.println("\n\n"); //put two extra empty lines			
			
        }catch (FileNotFoundException e){
        	 System.err.println("packageMatrix.formMatrix:performSlicing():writeToExtractInfoFile(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageMatrix.formMatrix:performSlicing():writeToExtractInfoFile(): Problem with IO exception." + e);}
 	}
}
	
	



// This class draw a PDG or CFG from the incidence matrix
class Graph
{
	
	// Function to draw PDG or CFG
    void Draw(int[][] mat, String[] label)
	{     					
			String edge_string=getEdgesString(mat, label);	 // Form an edges-string where edges are separate by comma		
			packageGraphDrawing.myGraph applet = new packageGraphDrawing.myGraph(); // Calling Graph applet
			applet.init(edge_string); // Drawing the graph using edges-string
	}
    
    //Form an edges-string where edges are separated by comma.......
    String getEdgesString(int[][] mat, String[] label)
	{
		
		
    	String edge_string="";
    	for(int i=0; i<mat.length;i++)
		{
			for(int j=0; j<mat[i].length;j++)
			{
				if(mat[i][j]!=0) // There is a data or control edge
				{
					if(edge_string.equals(""))
						edge_string=edge_string+(label[i])+"-"+(label[j]);
					else
						edge_string=edge_string+","+(label[i])+"-"+(label[j]);	
				}
			}
		}
		return 	edge_string; // Return edges-string
	}	
}