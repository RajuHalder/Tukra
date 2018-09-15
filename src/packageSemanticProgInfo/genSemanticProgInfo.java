/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This class computes
 * (i) Statements Semantic Relevancy and 
 * (ii) Semantic Data Depenedences.
 *
 * Input:  Details program information and Abstract Domain
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/

package packageSemanticProgInfo;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.DomainInterface.abstractDomain;
import packageAbstraction.ProgEnvironment.abstractEnvironment;
import packageAbstraction.Operator.*;
import packageAbstraction.Value.*;
import packageAbstraction.Domain.*;

import packageMyLibrary.*;
import packageEvalExpr.*;
import packageSemantics.*;
import packageProgInfo.*;

public class genSemanticProgInfo extends myFunctions implements myPatterns
{
	
	RandomAccessFile rafExtractInfo;
	abstractDomain ADobj;
	String[] ProgVars;
	abstractEnvironment[] ProgramContext;
	
	/* Constructor "GenSemanticProgInfo" takes details program information in the form of random access file type
	 * and the abstract domain of interest */
	 
	public genSemanticProgInfo(RandomAccessFile rafExtractInfo, String AbsDom)
	{
		this.rafExtractInfo=rafExtractInfo; // Input program's details
		
		if(AbsDom.equals("sign"))  // Input abstract domain
      	{
      		ADobj=new abstractSignDomain();
      	}
      	else if(AbsDom.equals("par"))
      	{
      		ADobj=new abstractParDomain();
      	}
		
		//Compute array of programs variables in the program
		ProgVars=super.getProgVars(rafExtractInfo); 
		
		//Computes Collecting Semantics of the program
		genCollectingSemantics semanticsObject=new genCollectingSemantics(rafExtractInfo, ADobj); 
		ProgramContext=semanticsObject.getCollectingSemantics(); 

	}
	
	
	/* compute relevancy of statements, and returns the list of irrelevant statements' labels */
	public LinkedList applyRelevancy(String PreviewFilePath, String File_Out_Rel_SemanticPreview, String File_Out_Rel_SemanticExtractInfo)
	{	   						
		
		//linkedlist contains the list of irrelevant statements' labels	
		LinkedList irrelevant_list=new LinkedList(); 
	
		try{
			
			DataInputStream dis_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(PreviewFilePath))));	
    		PrintStream	ps_Rel_SemanticPreview = new PrintStream( new FileOutputStream(File_Out_Rel_SemanticPreview) );
    	
    		//Checking for relevancy of each statement   		
			rafExtractInfo.seek(0);
			String stmt=rafExtractInfo.readLine();
			while(stmt.length()!=0)
			{

				String[] field=stmt.split(" "); // tokenization of the statements' details
				String type=field[1].trim(); // Extracting the type of the command
			
				if(type.equals("as")) // It is assignment command
				{
																
					int line_no=Integer.parseInt(field[0].trim()); // Extracting line number associated with the command
					abstractEnvironment environment = ProgramContext[line_no]; //Extract associated environment
					
					//Narrowing the abstract environment to the subset of variables appearing in the command							
					LinkedList coveringEnvironment=new LinkedList();
					
					String def=field[3].trim(); // DEFINED variable
					String use=field[4].trim();	// USED variables		
					if(! use.equals("*"))
					{
						String variables=def+"|"+use; 
						String[] variables_arr=variables.split("\\|"); // Array of variables in the whole command
						
						// Narrowing the abstract environment only to present variables
						abstractEnvironment RestrictedEnvironment = environment.getRestrictedEnvironment(variables_arr); 
						
						// Computes the atomic covering of the restricted environment
						coveringEnvironment=RestrictedEnvironment.getAtomicCovering(ADobj); 
					}
					else
					{	// There is no USED variables in the command, 
					    // So, evaluation of the command does not need atomic covering at all
						coveringEnvironment.addLast(environment);					
					}
							
					
					// The assignment statement is considered as irrelevnt initially
					boolean flag=true; 
					
					// Extracting expression on the R.H.S. of the assignment command
					String[] spl_cmd=(field[5].trim()).split("="); 
					String expr=spl_cmd[1].trim();	
					
					//Executing statement over all atomic environments in the covering and checking for relevancy
					ListIterator itr = coveringEnvironment.listIterator(); 					
					while(itr.hasNext())
					{
						// Extract atomic abstract environment
						abstractEnvironment subEnv=(abstractEnvironment)itr.next();
						
						// Evaluate the expression    						
   						abstractValue result= (new evaluateExpr()).evaluateArithExpr(expr, subEnv, ADobj); 
   					
   						// Previous environment 
     					abstractEnvironment prevEnv=new abstractEnvironment(subEnv);
     					
     					// New environment after execution   						
   						abstractEnvironment newEnv=subEnv.getModifiedEnvironment(def, result); 							
   						 
   						//Checking for relevancy: whether the execution changing the property of abstract states   									
   						if(!prevEnv.equals(newEnv)) 
   						{  						
   							flag=false; // relevant!!!!
     						break;
   	 					}
					}
										
					if(flag) // irrevant!!!!
					{
						//adding the label to the irrelevant list		
						String label=field[6].trim();	
						irrelevant_list.addLast(label);				
					}
				}
				
				stmt=rafExtractInfo.readLine(); // Go for other statement details
			}
			
			// Writing the relevant statements to the preview file of the refined program
			writeToRelPreviewFile(dis_Preview, irrelevant_list, ps_Rel_SemanticPreview);
		
			dis_Preview.close();
	    	ps_Rel_SemanticPreview.close();	
	    	
	    	// Extract details information of the relevant program and write to "Rel_SemanticExtractInfo" file				
	    	DataInputStream dis_Rel_SemanticPreview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(File_Out_Rel_SemanticPreview))));
			PrintStream	ps_Rel_SemanticExtractInfo = new PrintStream( new FileOutputStream(File_Out_Rel_SemanticExtractInfo) );
																	
			(new genProgInfo()).GenerateInformation(dis_Rel_SemanticPreview, ps_Rel_SemanticExtractInfo);
							
			dis_Rel_SemanticPreview.close();
    		ps_Rel_SemanticExtractInfo.close();
    		
    		
		}catch (FileNotFoundException e){
        	 System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.applyRelevancy(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.applyRelevancy(): Problem with IO exception." + e);}	
									
		
		System.out.println("packageSemanticProgInfo.genSemanticProgInfo.applyRelevancy(): The End.");
		
		return irrelevant_list;	// Returning the list of irrelevant statements' labels		
	}
	
	// Writing the relevant statements from original preview file to a refined preview file
	void writeToRelPreviewFile(DataInputStream disPreview, LinkedList irrelevant_list, PrintStream psSemanticPreview)
	{
    	    	
		try{

			// Put all relevant lines (not the irrelevant statements) to a temporary file	
			String path=System.getProperty("user.dir");	
			String File_Temp=path+"\\Output\\Ex_Out_Temp.txt";
				
			PrintStream psTemp = new PrintStream(new FileOutputStream(File_Temp));
	
			while(disPreview.available()!=0)
			{	
				String stmt=disPreview.readLine();
					
				if(stmt.length()!=0)
				{			
					String[] spl_stmt=stmt.split(":");
					String label=spl_stmt[0].trim();
					
					if(! super.isExist(label, irrelevant_list)) 
					{
						// Label not belonging to the irrelevant list: so write to temporary file
						psTemp.println(stmt);
					}
				}
			}
			psTemp.println("\n\n");
			psTemp.close();
			
			// Compute the relevancy of control statements			
			RandomAccessFile rafTemp = new RandomAccessFile(File_Temp, "r");			
			rafTemp.seek(0);
			
			String stmt=rafTemp.readLine(); // Extracting relevant statements from temporary file
			
			while(stmt.length()!=0)	
			{
				String[] spl_stmt=stmt.split(":");
				String label=spl_stmt[0].trim(); // Extracting labels of the command
				String cmd=spl_stmt[1].trim();	 // Extracting the command
			
				Matcher match_if=patrn_if.matcher(cmd);
				boolean found_if=match_if.find();
		
				Matcher match_while=patrn_while.matcher(cmd);
				boolean found_while=match_while.find();	
				
				if(found_if) // It is "if" control command
				{
					String next_stmt=rafTemp.readLine();
					String[] spl_next_stmt=next_stmt.split(":");
					String next_cmd=spl_next_stmt[1].trim();
					
					if(next_cmd.equals("End-If")) // "if"-block is empty, no else block
					{
						// don't do anything: go to next statement
						stmt=rafTemp.readLine(); 
						spl_stmt=stmt.split(":");
						cmd=spl_stmt[1].trim();
					}
					else if(next_cmd.equals("}Else{")) //"if"-block is empty, else-block present
					{
						String next_to_next_stmt=rafTemp.readLine();
						String[] spl_next_to_next_stmt=next_to_next_stmt.split(":");
						String next_to_next_cmd=spl_next_to_next_stmt[1].trim();
						
						if(next_to_next_cmd.equals("End-Else")) // Oh!!! "else"-block is also empty
						{
							// don't do anything: go to next statement
							stmt=rafTemp.readLine();
							spl_stmt=stmt.split(":");
							cmd=spl_stmt[1].trim();
						} 
						else // "else"-block is not empty
						{
							// write "skip" command
							psSemanticPreview.println("skip;"); 
							psSemanticPreview.println("End-If");
							stmt=next_to_next_stmt;
							cmd=next_to_next_cmd;
						}
					}
					else // "if"-block is not empty
					{
						// write "if"-statement to file
						psSemanticPreview.println(stmt);
						stmt=next_stmt;
						cmd=next_cmd;		
					}
				}
				else if(found_while) // It is "while" control statement
				{
					String next_stmt=rafTemp.readLine();
					String[] spl_next_stmt=next_stmt.split(":");
					String next_cmd=spl_next_stmt[1].trim();
					
					if(next_cmd.equals("End-While")) // "while"-block is empty
					{
						// don't do anything: go to next statement
						stmt=rafTemp.readLine(); 
						spl_stmt=stmt.split(":");
						cmd=spl_stmt[1].trim();
					}
					else //"while"-block is not empty
					{
						// write "while"-statement to file
						psSemanticPreview.println(stmt); 
						stmt=next_stmt;
						cmd=next_cmd;	
					}
				}
				else if(cmd.equals("}Else{")) // It is end of "if" and beginning of "else"
				{
					String next_stmt=rafTemp.readLine();
					String[] spl_next_stmt=next_stmt.split(":");
					String next_cmd=spl_next_stmt[1].trim();
					
					if(next_stmt.equals("End-Else")) // "else"-block is empty 
					{
						// write the end of "if" i.e. "End-If", and go for the next statement
						psSemanticPreview.println("End-If"); 
						stmt=rafTemp.readLine();
						spl_stmt=stmt.split(":");
						cmd=spl_stmt[1].trim();
					}
					else // "else"-block is not empty 
					{
						// write "else" control statement to file
						psSemanticPreview.println(stmt); 
						stmt=next_stmt;	
						cmd=next_cmd;
					}
				}
				
				psSemanticPreview.println(stmt); // Write the statement to the preview file
				
				stmt=rafTemp.readLine(); // Extracting relevant statements from temporary file		
			}	

			psSemanticPreview.println("\n\n"); // leave empty statements

        }catch (FileNotFoundException e){
        	 System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.writeToFile(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.writeToFile(): Problem with IO exception." + e);}
	}

	// To compute semantic data dependences		
	public LinkedList applyDependency(String File_In_Preview, String File_Out_Dep_SemanticPreview, String File_Out_Dep_SemanticExtractInfo)
	{
		// Keep track of the information: List of irrelevant variables at each labels
		LinkedList nonDepList=new LinkedList(); 
    	
		try{
			
			PrintStream ps_Dep_SemanticExtractInfo = new PrintStream( new FileOutputStream(File_Out_Dep_SemanticExtractInfo) );
			
			// Scanning the input program's statements' details			
			rafExtractInfo.seek(0);	
			String stmt=rafExtractInfo.readLine().trim();			
			while(stmt.length()!=0)
			{
				
				String[] field=stmt.split(" "); 
				int line_no=Integer.parseInt(field[0].trim()); // extracting line numbers				
				abstractEnvironment AbsEnv=ProgramContext[line_no];	// extracting associated environment							
				String type=field[1].trim(); // Extracting type of the command
				
				if(type.equals("as")) // Assignment command
				{
					// Extracting the expression on right hand side	
					String[] cmd=(field[5].trim()).split("=");
					String expr=cmd[1].trim();			    
					
					// Extracting USED variables
					LinkedList useVars=super.getUseVars(expr, REGEX_ARITH_OPERATION+"|"+REGEX_PARANTHESIS);									
					
					// Computing irrelevant variables in the expressions
					LinkedList useVars_nonDeps=find_N_Deps(expr, AbsEnv, useVars); 
					
					// Removing or disregarding the irrelevant USED variables
					ListIterator itr=useVars.listIterator();
					while(itr.hasNext())
					{
						String var=(String)itr.next();
						if(isExist(var, useVars_nonDeps))
						{
							itr.remove();	
						}	
					}
					
					// Checking if relevant USED variable list is empty or not
					String use=super.convertListToString(useVars);					
					if(use.equals(""))
					{
						use="*";	
					}		
					
					// Writing the refined information back to file
					ps_Dep_SemanticExtractInfo.println(field[0]+" "+field[1]+" "+field[2]+" "+field[3]+" "+use+" "+field[5]+" "+field[6]);
					
					// Storing the semantic independence information in the linkedlist "nonDepList"
					String label=field[6].trim();
					String str=convertListToString(useVars_nonDeps);
					if(!str.equals(""))
						nonDepList.addLast(label+"_"+str);
					else
						nonDepList.addLast(label+"_"+"*");				
				
				}
				else if(type.equals("if") || type.equals("wh")) // Conditional command
				{
	
					LinkedList Temp_useVars_nonDeps=new LinkedList();
					String use="";
					
					String cmd=field[5].trim(); // Extracting the command
									
					// Extracting arithmatic expressions involved in the conditional command
					StringTokenizer st=new StringTokenizer(cmd, REGEX_BOOL_OPERATION+"|"+REGEX_PARANTHESIS+"|"+REGEX_REL_OPERATION);					
					while(st.hasMoreTokens()) // Iterating for each arithmatic expression 
					{
						String expr=st.nextToken().trim(); 
						
						// Verifying whether it is arithmatic expression or not
						Matcher matArithExpr=patrn_arith_expr.matcher(expr);
						if(matArithExpr.find())
						{
							// Extracting USED variables in the expression
							LinkedList useVars=super.getUseVars(expr, REGEX_ARITH_OPERATION+"|"+REGEX_PARANTHESIS);								
							
							// Computing irrelevant variables in the expressions
							LinkedList useVars_nonDeps=find_N_Deps(expr, AbsEnv, useVars); 
							
							
							// Removing or disregarding the irrelevant USED variables
							ListIterator itr=useVars.listIterator();
							while(itr.hasNext())
							{
								String var=(String)itr.next();
								if(isExist(var, useVars_nonDeps))
								{
									itr.remove();	
								}	
							}
							
							// Temp contains only relevant variables of the current expression
							String temp=super.convertListToString(useVars);		
							
							// Add "Temp" to "use" that conatins all relevant variables for all expressions involved in the conditional command							
							if(!temp.equals("")) 							
								use=use+temp;
								
								
							// Add the irrelevant USED variables of the current expression to "Temp_useVars_nonDeps"
							itr=useVars_nonDeps.listIterator();	
							while(itr.hasNext())
							{
								Temp_useVars_nonDeps.addLast(itr.next());
							}
		
						}
						else
						{
							System.out.println("packageSemanticProgInfo.GenSemanticProgInfo.applyDependency: Problem occured in tokenizing the expressions from the condition "+cmd);
						}
					}
					
					if(use.equals("")) // There is no relevant USED variable in the conditional command
					{
						use="*";	
					}
					
					// Writing the refined information back to file
					ps_Dep_SemanticExtractInfo.println(field[0]+" "+field[1]+" "+field[2]+" "+field[3]+" "+use+" "+field[5]+" "+field[6]);
					
					// Storing the semantic independence information in the linkedlist "nonDepList"
					String label=field[6].trim();
					String str=convertListToString(Temp_useVars_nonDeps);
					if(!str.equals(""))
						nonDepList.addLast(label+"_"+str);
					else
						nonDepList.addLast(label+"_"+"*");	
				}
				else // Other statements: no need to compute data independences
				{
					// Writing original information back to file
					ps_Dep_SemanticExtractInfo.println(stmt); 
					
					// Storing the EMPTY semantic independence information in the linkedlist "nonDepList" in order to keep track
					String label=field[6].trim();
					nonDepList.addLast(label+"_"+"*");
				}
				
				stmt=rafExtractInfo.readLine().trim(); // scan next statement details
			}
			
			ps_Dep_SemanticExtractInfo.println("\n\n");	// Leave empty lines in the Dep_ExtractInfo File 
			
			ps_Dep_SemanticExtractInfo.close();
			
			// Writing the refined information to preview file "File_Out_Dep_SemanticPreview"
			DataInputStream dis_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(File_In_Preview))));
    		PrintStream	ps_Dep_SemanticPreview = new PrintStream( new FileOutputStream(File_Out_Dep_SemanticPreview) );
							
			writeToDepPreviewFile(dis_Preview, ps_Dep_SemanticPreview);
									
			dis_Preview.close();
    	  	ps_Dep_SemanticPreview.close();
    	  	
		}catch (FileNotFoundException e){
        	 System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.applyDependency(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.applyDependency(): Problem with IO exception." + e);}
		
		System.out.println("packageSemanticProgInfo.GenSemanticProgInfo.applyDependency(): The End.");
		
		return nonDepList;
	}
	
	// Copying information from a Preview File to Dependency-based Preview File
	public void writeToDepPreviewFile(DataInputStream dis_Preview, PrintStream	ps_Dep_SemanticPreview)
	{
		
		// Just copy from one to another
		try{
			
			while(dis_Preview.available()!=0)
			{	
				
				String stmt=dis_Preview.readLine();
				if(stmt.length()!=0)
				{	
					ps_Dep_SemanticPreview.println(stmt);
				}
			}
			
			ps_Dep_SemanticPreview.println("\n\n");
			
		}catch (FileNotFoundException e){
           System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.copyDepPreview(): Input File not found.");
 	  	}catch (IOException e){
           System.err.println("packageSemanticProgInfo.GenSemanticProgInfo.copyDepPreview(): Problem with IO exception.");}
	}
	
	
	// To compute semantic data dependences of an expression
	private LinkedList find_N_Deps(String expr, abstractEnvironment AbsEnv, LinkedList useVars)
	{
		LinkedList nonDeps=new LinkedList();
		
		if(! useVars.isEmpty())
		{
			String[] useVarsArray= super.convertListToStringArray(useVars);
			abstractEnvironment RestrictEnv = AbsEnv.getRestrictedEnvironment(useVarsArray);		
		
			LinkedList X_Vars=new LinkedList();
			ListIterator itr=useVars.listIterator();
			while(itr.hasNext())
			{
				X_Vars.addLast(itr.next());	
			}
		
			prove(expr, RestrictEnv, X_Vars, nonDeps); // Initaially call prove for set of all USED variables "X_Vars"
		}
		
		return nonDeps;		
	}
	
	// Proving independences of variables "X_Vars" in evaluating the expression "expr"
	void prove(String expr, abstractEnvironment AbsEnv, LinkedList X_Vars, LinkedList nonDeps)
	{
		if(X_Atom(expr, AbsEnv, X_Vars)) // yes, the evaluation of "expr" results into atomic value considering only "X_vars"
		{
			
			// Adding "X_vars" to the list "nonDeps"
			ListIterator itr=X_Vars.listIterator();
			while(itr.hasNext())
			{
				String var=itr.next().toString();
				if(! isExist(var, nonDeps))
					nonDeps.addLast(var);	
			}
			return;	
		}
		else //evaluation of "expr" results to composite abstract value considering only "X_vars"
		{
			for(int i=0;i<X_Vars.size();i++)	
			{
				LinkedList subVars=new LinkedList();
				ListIterator itr=X_Vars.listIterator();
				while(itr.hasNext())
				{
					subVars.addLast(itr.next());	
				}
				
				Object obj=subVars.remove(i);
				
				if(! subVars.isEmpty())
				{					
					prove(expr, AbsEnv, subVars, nonDeps);	// recursively call prove for all possible subsets of "X_vars"
				}
			}
		}
	}
	
	// To verify whether evaluation of expr results into atomic values or not by considering only "X_vars"
	boolean X_Atom(String expr, abstractEnvironment AbsEnv, LinkedList X_Vars)
	{
		boolean flag=false;
				
		if(isAtomicResult(expr, AbsEnv)) // Evaluation of expr over abstract environment is always atomic
		{
			flag=true;	// so return true
		}
		else
		{
			
			boolean Cov_flag=true;
			
			// Generating X-covering of the abstract environment
			String[] X_Vars_Arr=super.convertListToStringArray(X_Vars);			
			LinkedList X_Covering=AbsEnv.getXSubCovering(X_Vars_Arr, ADobj); 
			
			abstractEnvironment ae=(abstractEnvironment)X_Covering.getFirst();
		 	
		 	if(!(X_Covering.size()==1 && AbsEnv.equals(ae))) // there is X-covering
		 	{
		 		ListIterator itr=X_Covering.listIterator();
		 		while(itr.hasNext())
			 	{
			 		abstractEnvironment XcovEnv=(abstractEnvironment)itr.next();
					if(! X_Atom(expr, XcovEnv, X_Vars)) // Recursive call on all X-convering environment 
					{
						// Evaluation of "expr" on X-covering by considering "X_Vars" does not produce atomic abstract value
						Cov_flag=false;break;	
					}
				}
			}
			else // There exist no X-covering
			{
				Cov_flag=false;
			}
			
			if(Cov_flag) // Evaluation of "expr" on X-covering by considering "X_Vars" results into atomic abstract value
			{
				flag=true; // So return true
			}
		}
		return flag;
	}
	
	// To check whether the evaluation of expr results to atomic abstract value or not
	boolean isAtomicResult(String expr, abstractEnvironment AbsEnv)
	{	
				
		boolean flag=false;
		
		abstractValue result= (new evaluateExpr()).evaluateArithExpr(expr, AbsEnv, ADobj); // Evaluating..
		
		if(result.isAtomicValue() && !result.isBottom()) // results is atomic abstract value and it is not bottom
		{
			flag=true;
		}
		else if(!AbsEnv.isAtomicEnvironment()) //"AbsEnv" is not atomic environment
		{		
			
			boolean Cov_flag=true;			
			abstractValue atom=null;
			
			LinkedList Covering=AbsEnv.getAtomicCovering(ADobj); // Generate atomic covering of "AbsEnv"
		 	
		 	// Evaluation of "expr" on first covering element
		 	abstractEnvironment ae1=(abstractEnvironment)Covering.removeFirst();
		 	abstractValue covResult1= (new evaluateExpr()).evaluateArithExpr(expr, ae1, ADobj);		 	

			System.out.println("%%%%%%%%%%% 1. Evaluating "+expr+" on"); ae1.display(); System.out.println();
					
		 	if(! covResult1.isAtomicValue()) // not producing atomic result
		 	{
		 		Cov_flag=false;	
		 	}
		 	else
		 	{
		 		atom=covResult1; // Storing the atomic result temporarily
		 	}
		 	
		 	// If previous evaluation results atomic value, then do the same for other atomic environments	 	
		 	if(Cov_flag && !Covering.isEmpty()) 
		 	{
		 		ListIterator itrCov=Covering.listIterator();
			 	while(itrCov.hasNext())
			 	{
			 		abstractEnvironment ae2=(abstractEnvironment)itrCov.next();
			 		abstractValue covResult2= (new evaluateExpr()).evaluateArithExpr(expr, ae2, ADobj); 
			 		
			 		System.out.println("%%%%%%%%%%% 2. Evaluating "+expr+" on"); ae2.display();
						
					if(! (covResult2.isAtomicValue() &&  covResult2.equals(atom))) // Not producing same atomic value as before
		 			{
		 				Cov_flag=false;break;		
		 			}
				}
			}
			
			
			if(Cov_flag) // If evaluation on all atomic abstract environments produces same atomic value
			{
				flag=true;
				System.out.println("isAtomicResult() is TRUE");	
			}
		}
		
		return flag;
	}

}
