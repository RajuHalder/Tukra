/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This CLASS defines the methods that compute DCG annotations associated with PDG edges
 *
 * Input: File containing all detail information of the program in SSA form as an intermediate form
 *        
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/



package packageDCGannotations;

import java.io.*;
import java.io.File;
import java.util.*;

import packageMyLibrary.*;
import packageMatrix.*;
import packageSSAconversion.*;

public class genDCGannotations extends myFunctions implements myFilePaths, myPatterns
{	
	RandomAccessFile rafSSAExtractInfoFile;
	int[][] SSAPDGmatrix;
	int ProgSize;	
	
	//Contructor takes detail program information in SSA form 
	public genDCGannotations(RandomAccessFile rafSSAExtractInfoFile)
	{ 		
      	this.rafSSAExtractInfoFile=rafSSAExtractInfoFile;
      	
      	SSAPDGmatrix=(new formMatrix(rafSSAExtractInfoFile)).getPDGmatrix(); //Generates PDG of the program in SSA form
      	
      	ProgSize=super.getProgSize(rafSSAExtractInfoFile); //Computes size of the program in SSA form i.e. the no. of statements in the progarm	
	}
	
	//"annotate" objects stores the reach sequences and avoid sequences
	public class annotate
	{
		LinkedList reachSeq; // list of conditions: each element is a condition which is represented by 
		LinkedList avoidSeq; // string "node_1:cond_1:node_2:cond_2:...node_n" where "node_i" is statement number(not label)
		
		public annotate() 
		{
			reachSeq=new LinkedList(); //initialize by empty reach and avoid sequences
			avoidSeq=new LinkedList();
		}
		
		public annotate(LinkedList Rseq, LinkedList Aseq) 
		{
			reachSeq=Rseq; //initialize by given reach and avoid sequences
			avoidSeq=Aseq;
		}
		
		public LinkedList getReachSeq() //return reach sequences
		{
			return reachSeq;
		}
		
		public LinkedList getAvoidSeq() //return avoid sequences
		{
			return avoidSeq;
		}
		
		public void addReachSeq(String cond) //add a condition in the reach sequences
		{
			reachSeq.addLast(cond);
		}
		
		public void addAvoidSeq(String cond) //add a condition in the avoid sequences
		{
			avoidSeq.addLast(cond);
		}
		
		public boolean isEmpty() //checking for emptyness: whether both reach and avoid sequences are empty
		{
			if(reachSeq.isEmpty() && avoidSeq.isEmpty())
				return true;
				
			return false;	
		}
		
		public void display() //display the reach and avoid sequences
		{
			System.out.print("Reach Sequences: ");
			ListIterator itrReach=reachSeq.listIterator();
			while(itrReach.hasNext())
			{
				String s=itrReach.next().toString();
				System.out.print(s+ ", ");
			}
			System.out.println();
			
			System.out.print("Avoid Sequences: ");
			ListIterator itrAvoid=avoidSeq.listIterator();
			while(itrAvoid.hasNext())
			{
				String s=itrAvoid.next().toString();
				System.out.print(s+ ", ");
			}
			System.out.println();
		}
	}
	
	
	/* coputes the annotations (reach sequences and avoid sequences) over each edges in the PDG of the program in SSA form*/
	public annotate[][] getAnnotations() 
	{		
		annotate[][] DCGmatrix= new annotate[ProgSize][]; // Creates a incidence matrix of PDG where each cell contains				
		for(int i=0; i<ProgSize;i++)                      // annotations of the corresponding edge represented by that cell
		{
			DCGmatrix[i]=new annotate[ProgSize];
			
			for(int j=0; j<DCGmatrix[i].length;j++)
			{
				DCGmatrix[i][j]=new annotate(); //Initialize with empty annotations
			}
		}
      	
      	
		genSSAform postDomObj=new genSSAform(rafSSAExtractInfoFile);    // For each node n in the program - 
		LinkedList[] postDomList=postDomObj.getPostDominance(); // computes the list of nodes who post dominate n
		
		int root=0;
		
		for(int n=0; n<ProgSize;n++)
		{
			
			String type=super.getType(rafSSAExtractInfoFile, n);
			
			if(type.equals("st") || type.equals("if") || type.equals("wh") ) //For all control nodes n
			{
				LinkedList path=new LinkedList(); // Computes PDG path to that control node n
				getCDGPath(root, n, path);
				
				for(int n2=0; n2<ProgSize;n2++)  
				{
					
					if(SSAPDGmatrix[n][n2]==1 || SSAPDGmatrix[n][n2]==-1) // For all CDG edge c: n--->n2
					{
						
						//Computing Reach Seqence for CDG edge
						
						String condR=new String(""); //Generates reach sequence for edge c which is just the string "n:T/F:n2"						
						if(SSAPDGmatrix[n][n2]==1)
						{
							condR=new String(n+":T:"+n2);
						}
						else if(SSAPDGmatrix[n][n2]==-1)
						{
							condR=new String(n+":F:"+n2);
						}
					
						DCGmatrix[n][n2].addReachSeq(condR); //Added the reach sequence condition to the annotation  of "n -->(c) n2"
						
						
						//Computing Reach Seqence for DDG edge
						for(int n1=0; n1<ProgSize;n1++)
						{
							if(SSAPDGmatrix[n1][n2]==2)  //For all DDG edge d: n1--->n2 
							{								
								if(! super.isExist(n2, postDomList[n1])) //If n2 does not posdominate n1
								{
											
									//added the reach sequence conditions from "parent(n1)" till "n2" along the "path" to annotations  of "n1 --> n2"					
									LinkedList L=allCDGParentsOn(n1, path); 

									ListIterator itr=L.listIterator();
									while(itr.hasNext())
									{						
																										
										int index=((Integer)itr.next()).intValue(); //index of the "parent(n1)" in "path"
										
										LinkedList subPath=super.extractSubList(path, index);
										subPath.addLast(n2); // "subPath" is now from "parent(n1)" till "n2"	
																		
										String cond=getCondition(subPath); //Convert the sub path into reach condition form
																				
										DCGmatrix[n1][n2].addReachSeq(cond); //added to annotation
									}
								}	
							}
						}
						
						//Computing Avoid Seqences. for DDG edges.
						for(int n3=0; n3<ProgSize; n3++)
						{
							if(SSAPDGmatrix[n2][n3]==2) //DDG edges d2: n2---> n3
							{
								String defn2=super.getDef(rafSSAExtractInfoFile, n2); //SSA variable
								defn2=defn2.substring(0,defn2.lastIndexOf("_")); //original variable
															
								for(int n1=0; n1<ProgSize; n1++)   
								{
									
									if(SSAPDGmatrix[n1][n3]==2) //DDG edges d1: n1---> n3
									{						
										String defn1=super.getDef(rafSSAExtractInfoFile, n1); //SSA variable
										defn1=defn1.substring(0,defn1.lastIndexOf("_")); //original variable
											
										//For all DDG edges d2: n2--->(x) n3 and d1: n1--->(x) n3	
										if( (!defn1.equals("")) && defn1.equals(defn2) && n1!=n2) 
										{
											
											//added the avoid sequence conditions from "parent(n1)" till "n2" along the "path" to annotations of "n1 -->(x) n3"
											LinkedList L=allCDGParentsOn(n1, path);
											
											ListIterator itr=L.listIterator();
											while(itr.hasNext())
											{																	
												int index=((Integer)itr.next()).intValue(); //index of the "parent(n1)" in "path"
												
												LinkedList subPath=super.extractSubList(path, index);
												subPath.addLast(n2);		// "subPath" is now from "parent(n1)" till "n2"
																		
												String cond=getCondition(subPath); //Convert the sub path into avoid condition form
																						
												DCGmatrix[n1][n3].addAvoidSeq(cond); //added to annotation												
											}
										}
									}
								}
							}
						}
					}
				}
			}	
		}
		
		System.out.println("packageDCGannotations.genDCGannotations.getAnnotations(): The End.");
		
		return DCGmatrix;
	}
	
	/* Computes the list of indexes of "parent(node)" in the "path"*/
	private LinkedList allCDGParentsOn(int node, LinkedList path)
	{
		LinkedList L=new LinkedList();
		
		for(int parent=0; parent<ProgSize; parent++)
		{
			if(SSAPDGmatrix[parent][node]==1 || SSAPDGmatrix[parent][node]==-1) //parent(node)="parent" in the "path"
			{
				int index=path.indexOf(parent); //index of the "parent(node)" in "path"
				if(index>=0 && index <= path.size()-2)
				{
					int child=((Integer)path.get(index+1)).intValue(); //"child" is the child node of "parent" in the path
					
					if(SSAPDGmatrix[parent][node]==SSAPDGmatrix[parent][child]) //checking the truth value of the edges "parent-->child" and "parent-->node"
					{
						L.addLast(index); //yes, same truth value
					}
				}
				
			}
		}
		
		return L;
	}
	
	//Given a node n, it computes the CDG path "path"(list of node numbers) from "root" to "n"	
	private void getCDGPath(int root, int n, LinkedList path)
	{
				
		if(n==root)
		{
			path.addFirst(root); return; //n is the root node
		}
		else
		{
			path.addFirst(n); 
			
			for(int i=0; i<ProgSize; i++)
			{
				if(SSAPDGmatrix[i][n]==1 || SSAPDGmatrix[i][n]==-1)
				{
					getCDGPath(root, i, path);break; //recursively call the CDG path with "Parent(n)"
				}	
			}
		}	
	}
	
	//Given a CDG path "list of node numbers" - convert into string representing reach/avoid sequences "n1:c1:n2:c2:...:"
	private String getCondition(LinkedList path)
	{
		
		String cond=""; //condition string of the form reach/avoid sequences
		
		int node=-1;
		int nextNode=-1;
		
		ListIterator itr=path.listIterator();										
		if(itr.hasNext())
		{
			node=((Integer)itr.next()).intValue(); //"starting node": 1st node
		}
		while(itr.hasNext())
		{
			nextNode=((Integer)itr.next()).intValue(); //"next node": 2nd to last node
										
			if(cond.equals("")) //First time: Added "node:T/F:nextnode"
			{
				if(SSAPDGmatrix[node][nextNode]==1)
					cond=node+":T:"+nextNode;              
				else if(SSAPDGmatrix[node][nextNode]==-1)
					cond=node+":F:"+nextNode;
			}
			else
			{
				if(SSAPDGmatrix[node][nextNode]==1)      //Not first time: Added ":T/F:nextnode"
					cond=cond+":T:"+nextNode;            //avoid the repetition of "node/nextnode"
				else if(SSAPDGmatrix[node][nextNode]==-1)
					cond=cond+":F:"+nextNode;
			}
			node=nextNode;			//making the "next node" as "stating node" and iterate the same							
		}
		
		return cond;	
	}
	

	//Write the DCG annotation in a preview file : used to preview on GUI
	public void writeDCGannotations(String File_Out_DCG_Preview)
	{
		try{
		 	
			PrintStream psDCGPreview=new PrintStream( new FileOutputStream(File_Out_DCG_Preview)); //Open a print stream
		
			annotate[][] DCGmatrix=getAnnotations(); //Computes DCG annotations
			
			for(int s=0; s<ProgSize;s++)
			{
				for(int t=0; t<ProgSize;t++)
				{
					
					annotate ant=DCGmatrix[s][t]; //annotations of edges "s --> t"
					
					if(!ant.isEmpty()) //annotations are not empty
					{
						String Lsource=super.getLabel(rafSSAExtractInfoFile, s); //get the label of "s"
						String Ldest=super.getLabel(rafSSAExtractInfoFile, t); //get the label of "t"
						
						psDCGPreview.println(Lsource+" ---> "+Ldest+" :"); //write the edge info in the file
						
						LinkedList ReachSeq=ant.getReachSeq(); //get reach sequence of "s --> t"
						LinkedList AvoidSeq=ant.getAvoidSeq(); //get avoid sequence of "s --> t"
						
						//Writing reach sequences....
						psDCGPreview.print(" Reach Sequence:");
						
						ListIterator itrReach=ReachSeq.listIterator();						
						while(itrReach.hasNext())
						{
							String cond=itrReach.next().toString(); //each condition "cond"
							psDCGPreview.print(convertNode2Label(cond)); //Convert "cond" into proper form and write into file
						}
						psDCGPreview.println();
						
						//Writing avoid sequences....
						psDCGPreview.print(" Avoid Sequence: ");
						
						ListIterator itrAvoid=AvoidSeq.listIterator();
						while(itrAvoid.hasNext())
						{
							String cond=itrAvoid.next().toString();
							psDCGPreview.print(convertNode2Label(cond)); //Convert "cond" into proper form and write into file
						}
						psDCGPreview.println();
					}
				}	
			}
			
			psDCGPreview.close(); // close the print stream
			
		}catch(Exception e){System.out.println(e);}
		
		System.out.println("packageDCGannotations.genDCGannotations.writeDCGannotations(): The End.");
	}							
	
	private String convertNode2Label(String condition) //Converting reach/ avoid sequences
	{                                                  // from "n1:c1:n2:c2.." to "n1 -->(c1) n2 -->(c2) --> ..."
				                               
		String[] token=condition.split(":");		    
		for(int i=0; i<token.length; i=i+2)             //Changing the odd positions - node numbers into label
		{
			int node=Integer.parseInt(token[i]);
			String label=super.getLabel(rafSSAExtractInfoFile, node);
			token[i]=new String(label);
		}
		
		String str=new String("");      // Constructing into string of the form "n1 -->(c1) n2 -->(c2) --> ..."
		for(int i=0; i<token.length; i++) 
		{
			if(str.equals("") || i%2==0)  //even positions are label
				str=str+token[i];
			else if(i%2!=0)
			    str=str+" -->("+token[i]+") ";	//odd positions are truth values
		}
		
		return str;
	}
}