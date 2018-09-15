
/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This CLASS defines the methods that generate 
 * (i) "preview" form of the program
 * (ii) detail program information in an intermediate form
 *
 * Input: File containing input program
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageProgInfo;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import packageMyLibrary.*;

public class genProgInfo extends myFunctions implements myPatterns
{
	String ProgString; // contains program in the form of string

	//*************************************************************************************************
	//                    Constructors
	//*************************************************************************************************
	
	public genProgInfo(){} // Default constructor
	
	public genProgInfo(DataInputStream dis) // Constructor taking data stream of the file containing program
	{
		
		// Coverting the program in a string
		
		String s="start{"; // adding dummy "starting" statement
		try{
			
			while (dis.available()!=0)
      		{
      			s=s + " " + dis.readLine().trim();
      		}

			s=s+"stop; }"; // adding dummy "ending" statement
			this.ProgString=s;
			
		}
		catch (FileNotFoundException e){System.err.println("packageProgInfo.genProgInfo(): Input File not found." + e);}
 	   	catch (IOException e){System.err.println("packageProgInfo.genProgInfo(): Problem with IO exception." + e);}
	}
	
	public genProgInfo(String s) // Constructor taking program in a form of string
	{
		this.ProgString="start{ " + s + "stop; }";
	}
	
				
	
	//*************************************************************************************************
	//                    To Preview the Input Program
	//*************************************************************************************************
		
	public void previewProg(PrintStream psPreview)
	{
		
		final char DELIMETER=';'; // Ending character of each statement
	
    	String stmt="";
    
    	String cntrl_seq="_xx.-1"; // Stores the current control block information
    	int cntrl_depth=-1;        // Stores the current control depth	
    	int line_no=-1;            // Stores the current line no.
    	
    	int space_offset=6;  
    	
		while(ProgString.length()!=0)
		{

			//To find the index of the end of a statement
			int DelimeterIndex=ProgString.indexOf(DELIMETER);
				
			if(DelimeterIndex <= -1)
			{
				//Last statement is not properly ended
				System.err.println("packageProgInfo.previewProg(): Syntactic Error - Program statements must be ended with \";\".");
				psPreview.println("packageProgInfo.previewProg(): Syntactic Error - Program statements must be ended with \";\".");
				System.exit(1);
			}			
			else
			{
				//Extracting statements from the whole input code
				stmt=ProgString.substring(0,DelimeterIndex+1).trim();
				ProgString=ProgString.substring(DelimeterIndex+1).trim();
				
			
				//Classifying statements
				while(stmt.length()!=0)
				{
					stmt=stmt.replaceAll(" ", "");
					
					Matcher match_start=patrn_start.matcher(stmt);
					boolean found_start=match_start.find();
				
					Matcher match_if=patrn_if.matcher(stmt);
					boolean found_if=match_if.find();
				
					Matcher match_while=patrn_while.matcher(stmt);
					boolean found_while=match_while.find();
									
										
					if(!found_start && !found_if && !found_while)
					{
						// Statement is not "start" nor "if" nor "while" statement
						// Writing the program point and statement to the preview file
						
						psPreview.print("L"+(++line_no)+":");						
						int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
						for(int i=0; i<space_len;i++){psPreview.print(" ");}						
						psPreview.println (stmt);			
											
				    	break;
					}
					else
					{	
						// Statement is either "start" or "if" or "while" statement
						
						if(found_start)
						{
							// Extracting "start" statement						
							String stmt_start=match_start.group().trim();
							
							// Writing the program point and the "start" statement to the preview file
							psPreview.print("L"+(++line_no)+":");						
							int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
							for(int i=0; i<space_len;i++){psPreview.print(" ");}									
							psPreview.println (stmt_start);
							
							//Extracting statement nested in the "start" control block
							int after_start_index= match_start.end();
							stmt=stmt.substring(after_start_index).trim();
							
							// Updating and storing control information i.e. the control depth and cntrol block information
							cntrl_seq=cntrl_seq+"_start:"+(++cntrl_depth);														
						}
						
						if(found_if)
						{			
							// Extracting "if" statement										
							String stmt_if=match_if.group().trim();
							
							// Writing the program point and the "if" statement to the preview file
							psPreview.print("L"+(++line_no)+":");						
							int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
							for(int i=0; i<space_len;i++){psPreview.print(" ");}
							psPreview.println (stmt_if);
			
							//Extracting statement nested in the "if" control block
							int after_if_index= match_if.end();	
							stmt=stmt.substring(after_if_index).trim();
							
							// Updating and storing control information i.e. the control depth and cntrol block information
							cntrl_seq=cntrl_seq+"_if:"+(++cntrl_depth);
														
						}
				
						if(found_while)
						{					
						    // Extracting "while" statement										
							String stmt_while=match_while.group().trim();
							
							// Writing the program point and the "while" statement to the preview file
							psPreview.print("L"+(++line_no)+":");						
							int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
							for(int i=0; i<space_len;i++){psPreview.print(" ");}
							psPreview.println (stmt_while);
							
							//Extracting statement nested in the "while" control block
							int after_wh_index= match_while.end();
							stmt=stmt.substring(after_wh_index).trim();
							
							// Updating and storing control information i.e. the control depth and cntrol block information
							cntrl_seq=cntrl_seq+"_while:"+(++cntrl_depth);
						}
					}
				}
				
				while(ProgString.length()!=0 && ProgString.charAt(0)=='}')  // If end of control '}' is found at the beginning of "ProgString"
				{
					
					ProgString=ProgString.substring(1).trim(); // Remove "}" from the program string
					
					// Extract the control block information you are supposed to be inside
					String cntrl=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1).trim();						
					String[] cntrl_spl=cntrl.split(":");
					String extract_cntrl_type=cntrl_spl[0].trim();
					int extract_cntrl_depth=new Integer(cntrl_spl[1].trim());
					
					// If extracted conrol depth is ok with the actual control depth presently you are in
					if(cntrl_depth==extract_cntrl_depth)
					{
												
						if(extract_cntrl_type.equals("if")) // If you are in "if" block
						{
							
							// Going out of "if" block
							cntrl_depth--;	
							cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();	
								
							// Identifying the existence of "else" part associated with "if"	
							Matcher match_else=patrn_else.matcher(ProgString);
							boolean found_else=match_else.find();
						
							if(found_else) // If "else" block found
							{	
								// Writing the program point and the statement "}Else{" to the preview file
								psPreview.print("L"+(++line_no)+":");						
								int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
								for(int i=0; i<space_len;i++){psPreview.print(" ");}
								psPreview.println ("}Else{");
								
								// Removing "else" from the program string
								int after_else_index= match_else.end();
								ProgString=ProgString.substring(after_else_index).trim();	
								
								// Updating and storing control information i.e. the control depth and cntrol block information
								cntrl_seq=cntrl_seq+"_else:"+(++cntrl_depth);				
							}
							else // If no "else" block found
							{	
								// Writing "end of if block" i.e. "End-If" to the preview file
								psPreview.print("L"+(++line_no)+":");						
								int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
								for(int i=0; i<space_len;i++){psPreview.print(" ");}
								psPreview.println ("End-If");	
							}					
						}
						else if(extract_cntrl_type.equals("else"))
						{	
							// Going out of "else" block
							cntrl_depth--;	
							cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();
							
							// Writing "end of else block" i.e. "End-Else" to the preview file
							psPreview.print("L"+(++line_no)+":");						
							int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
							for(int i=0; i<space_len;i++){psPreview.print(" ");}
							psPreview.println ("End-Else");
						}
						else if(extract_cntrl_type.equals("while"))
						{	
							// Going out of "while" block
							cntrl_depth--;	
							cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();
							
							// Writing "end of while block" i.e. "End-While" to the preview file
							psPreview.print("L"+(++line_no)+":");						
							int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
							for(int i=0; i<space_len;i++){psPreview.print(" ");}
							psPreview.println ("End-While");
						}
						else if(extract_cntrl_type.equals("start"))
						{	
						
							// Going out of "start" block
							cntrl_depth--;	
							cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();
							
							// Writing "end of start block" i.e. "End-Start" to the preview file
							psPreview.print("L"+(++line_no)+":");						
							int space_len=(cntrl_depth+space_offset)-(new String("L"+line_no+":")).length();
							for(int i=0; i<space_len;i++){psPreview.print(" ");}
							psPreview.println ("End-Start");
						}
					}
					else  // If extracted control depth does not match with the actual control depth you are presently in
					{
						System.out.println("packageProgInfo.previewProg(): Syntactic Error: Mismatch of  }.");
						psPreview.println("packageProgInfo.previewProg(): Syntactic Error: Mismatch of  }.");
						System.exit(1);							
					}
								
				}		
			}
		}
		
			
		// To check whether the "cntrl_seq" is back to its initial state i.e. propoer end of parsing of the program occurs
		
		if(cntrl_seq.equals("_xx.-1"))
		{
			psPreview.println ("\n\n");				
		}
		else
		{
			System.out.println("packageProgInfo.previewProg(): Syntactic Error: Extra } found.");
			psPreview.println("packageProgInfo.previewProg(): Syntactic Error: Extra } found.");
			System.exit(1);
		}
		
		System.out.println("packageProgInfo.previewProg(): The End.");
	}
	
	
	// To generate details program information of the program in an intermediate form and write it to the file in the parameter
	public void GenerateInformation(DataInputStream disPreview, PrintStream psExtractInfo)
	{
		try{
		
			String path=System.getProperty("user.dir");		
			String File_Out_Indent=path+"\\Output\\Ex_Out_Indent.txt";		
    		PrintStream	psIndent = new PrintStream( new FileOutputStream(File_Out_Indent) );
      			
      		indentProg(disPreview, psIndent); // First indenting from preview form
      			
      		psIndent.close();
      		      		
      		DataInputStream disIndent = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(File_Out_Indent))));
      		
      		extractProg(disIndent, psExtractInfo); // Then extarcting the details from indenting form
      		
      		disIndent.close();
      		
      	}catch (FileNotFoundException e){System.err.println("packageProgInfo.GenerateInformation(): Input File not found." + e);
 	  	}catch (IOException e){System.err.println("packageProgInfo.GenerateInformation(): Problem with IO exception." + e);}		
	}
	
	
	//*************************************************************************************************
	//                    To Indent the Input Program
	//
	//  Format of each non-control statement:
	//  	Label:CommandType:ControlDepth Command
	//
	//  Format of each control statement:
	//  	Label:CommandType:ControlDepth Command
	//  	mid-CommandType
	//		end-CommandType    
	//*************************************************************************************************
	
	private void indentProg(DataInputStream disPreview, PrintStream psIndent)
	{
    	
    	String cntrl_seq="_xx:-1";
    	int cntrl_depth=-1;
    			
		try{
			
			while (disPreview.available()!=0) 
      		{
      			String stmt=disPreview.readLine().trim();  // Read statements from Preview File   			
					
				if(stmt.length()!=0)
				{
					// Extract Program Points and Commands from Statements
					String[] stmt_spl=stmt.split(":");
					String label=stmt_spl[0].trim();    
					String cmd=stmt_spl[1].trim();
					
					// Checking for control command
					Matcher match_start=patrn_start.matcher(cmd); 
					boolean found_start=match_start.find();
				
					Matcher match_if=patrn_if.matcher(cmd);
					boolean found_if=match_if.find();
			
					Matcher match_while=patrn_while.matcher(cmd);
					boolean found_while=match_while.find();
				
					if(found_start || found_if || found_while) // Control command found
					{
						
						// Write the information to "Indent File" and
						// Store the corresponding control block information
											   	  				
						if(found_start)
						{
							// Write whole command			
							psIndent.println (label+":st:"+(++cntrl_depth)+" "+cmd);
							
							cntrl_seq=cntrl_seq+"_st:"+cntrl_depth;	
						}
						
						if(found_if)
						{
							// Write only the conditional expression			
							String expr_if=cmd.substring(cmd.indexOf("(")+1, cmd.indexOf(")")).trim(); 		
							psIndent.println (label+":if:"+(++cntrl_depth)+" "+expr_if);				
							
							cntrl_seq=cntrl_seq+"_if:"+cntrl_depth;
						}
				
						if(found_while)
						{
							// Write only the conditional expression															
							String expr_while=cmd.substring(cmd.indexOf("(")+1, cmd.indexOf(")")).trim(); 
							psIndent.println(label+":wh:"+(++cntrl_depth)+" "+expr_while);
						
							cntrl_seq=cntrl_seq+"_wh:"+cntrl_depth;
						}
						
					}
					else if(cmd.equals("End-If") || cmd.equals("}Else{") || cmd.equals("End-Else") || cmd.equals("End-While") || cmd.equals("End-Start") )
					{
						// End of control command is found
						// Extract the control block information you are supposed to be inside
											   	  					
						String cntrl=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1).trim();						
						String[] cntrl_spl=cntrl.split(":");
						String extract_cntrl_type=cntrl_spl[0].trim();
						int extract_cntrl_depth=new Integer(cntrl_spl[1].trim());
						
						// If extracted control depth is ok with the actual control depth presently you are in
						if(cntrl_depth==extract_cntrl_depth)
						{
						
							if(extract_cntrl_type.equals("mid-wh"))
							{	
								// You are about to exist the "FALSE" block of "while" command
								// Write the information to Indent File
								psIndent.println (label + ":end-wh:" + (cntrl_depth+1) + " " + "}");
								
								// Update control information
								cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();
								
								// Extract control information
								cntrl=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1).trim();				
								cntrl_spl=cntrl.split(":");
								extract_cntrl_type=cntrl_spl[0].trim();
								extract_cntrl_depth=new Integer(cntrl_spl[1].trim());
							}	
						
						
							if(extract_cntrl_type.equals("st") && cmd.equals("End-Start")) 
							{
								// You are about to exist the "start" control block
								// write the information to Indent File
								psIndent.println (label + ":mid-st:" + cntrl_depth + " " + "}{");
								psIndent.println (label + ":end-st:" + cntrl_depth + " " + "}");
								
								// Update control information
								cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();	
								cntrl_depth--;		
							}
							else if(extract_cntrl_type.equals("if"))
							{
								if(cmd.equals("}Else{"))
								{
									// You are about to exist the "if" control block and entering to "else" block
									// write the information to Indent File
									psIndent.println (label + ":mid-if:"+cntrl_depth+" "+"}{");
									
								}				
								else if(cmd.equals("End-If"))
								{
									// You are about to exist the "if" control block
									// write the information to Indent File
									psIndent.println (label + ":mid-if:"+cntrl_depth+" "+"}{");
									psIndent.println (label + ":end-if:"+cntrl_depth+" "+"}");
									
									// Update control information
									cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();	
									cntrl_depth--;								
								}
								else if(cmd.equals("End-Else"))
								{
									// You are about to exist the "if-else" control block
									// write the information to Indent File
									psIndent.println (label + ":end-if:"+cntrl_depth+" "+"}");
									
									// Update control information
									cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();	
									cntrl_depth--;
								}
								
							}
							else if(extract_cntrl_type.equals("wh") && cmd.equals("End-While"))
							{	
								// You are about to exist the "while" control block
								// write the information to Indent File
								psIndent.println (label + ":mid-wh:"+cntrl_depth+" "+"}{");
								
								// Update control information
								cntrl_seq=cntrl_seq.substring(0,cntrl_seq.lastIndexOf("_")).trim();	
								cntrl_depth--;
								cntrl_seq=cntrl_seq+"_mid-wh:"+cntrl_depth;
							}
						}
						else // If extracted control depth does not match with the actual control depth you are presently in
						{
							
							System.out.println("ProgInfo.indentProg(): Syntactic Error: Mismatch of  }.");
							psIndent.println("ProgInfo.indentProg(): Syntactic Error: Mismatch of  }.");
							System.exit(1);							
						}
					}					   	
					else
					{
					   	// Non-Control Command is found
					   	// Write the information to Indent File
				   									
						if(cmd.indexOf(";")>=0)
					   		cmd=cmd.substring(0, cmd.indexOf(";")).trim(); // remove the delimeter	
					   		
					   	Boolean is_correct_stmt=false;
					   						    	
					   	String type="";
					   	if(patrn_assign.matcher(cmd).find()) // Assignment statement
     	 				{
     	 					type="as";
     	 					is_correct_stmt=true;
     	 				}
     	 				else if(patrn_print.matcher(cmd).find()) // Print statement
     	 				{
     	 					type="pr";
     	 					is_correct_stmt=true;
     	 				}
     	 				else if(patrn_phi.matcher(cmd).find()) //Phi statement
     	 				{
     	 					type="pi";
     	 					is_correct_stmt=true;
     	 				}
     	 				else if(cmd.toLowerCase().equals("skip")) //Skip statement
     	 				{
     	 					type="sk";
     	 					is_correct_stmt=true;
     	 				}
     	 				else if(cmd.toLowerCase().equals("stop")) //Stop statement
     	 				{
     	 					type="sp";
     	 					is_correct_stmt=true;
     	 				}
     	 					
     	 				if(is_correct_stmt==false) // Unknown command found
     	 				{
     	 					System.out.println("ProgInfo.indentProg(): Incorrect Syntax at  - " + cmd);	
     	 				}
					   		
					   	psIndent.println (label+":"+type+":"+cntrl_depth+" "+cmd); // write to file			    	
					}
				}
      		}
      		
	      	// To check whether the cntrl_seq is back to its initial state
		
			if(cntrl_seq.equals("_xx:-1"))
			{
				psIndent.println ("\n\n");				
			}
			else
			{
				System.out.println("packageProgInfo.indentProg(): Syntactic Error: Extra } found.");
				psIndent.println("packageProgInfo.indentProg(): Syntactic Error: Extra } found.");
				System.exit(1);
			}

		}catch (FileNotFoundException e){
        	 System.err.println("packageProgInfo.indentProg(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("packageProgInfo.indentProg(): Problem with IO exception." + e);}
           
        System.out.println("packageProgInfo.indentProg(): The End.");	
	}			
			
		
		
	//*********************************************************************************
	//				To Extract all Information from indented Program
	//
	//  Format of each statement:
	//  	LineNo CommandType ControlInfo DEFvariables USEvariables COMMAND LABEL
	//*********************************************************************************

	private void extractProg(DataInputStream disIndent, PrintStream psExtractInfo)
	{	
				
		try{
   	
			int line_no=-1;
			String cntrl_seq="_xx:-1:T";
			String cntrl="-1:T";
				
			while (disIndent.available() != 0)
      		{
      	
      			String stmt=disIndent.readLine().trim(); // Read Statements from Indented File   	
      		
      			if(stmt.length()!=0)
      			{
      				String[] stmt_spl=stmt.split(" ");
      				String header=stmt_spl[0].trim();
      				String cmd=stmt_spl[1].trim(); // Extracting Command
      		
      				String[] header_spl=header.split(":");
      				
      				String label=header_spl[0].trim(); // Extracting Program Point
      				String type=header_spl[1].trim(); // Extracting Command Type
      				      		
      				if(type.equals("st") || type.equals("if") || type.equals("wh")) // You have control commands
      				{
      				   
      				    // Identify USE variables
      				   	String use="";
      					if(type.equals("if") || type.equals("wh"))
      					{
      						String TokenSeperator=REGEX_ARITH_OPERATION+"|"+REGEX_REL_OPERATION+"|"+REGEX_BOOL_OPERATION+"|"+REGEX_PARANTHESIS;   				
      						LinkedList useVars= super.getUseVars(cmd, TokenSeperator);     	 				   	 				
					
     	 					use=super.convertListToString(useVars);
     	 					if(use.equals(""))
							{
								use="*"; // No USE variable present	
							}
						}
						else if(type.equals("st"))
						{
							use="*"; // By default no USE variable present		
						}
     	 				
     	 				// Writing details to ExtractInfo File
      					psExtractInfo.println((++line_no) + " " + type + " " + cntrl + " " + "*" + " " + use + " " + cmd + " " + label);
      					
      					// Identify and store which control statement is controling now
      					cntrl_seq=cntrl_seq+"_"+type+":"+line_no+":"+"F";
      					cntrl_seq=cntrl_seq+"_"+type+":"+line_no+":"+"T";
      					cntrl=line_no+":"+"T";
     	 			}
     	 			else if(type.equals("as")) // You have ASSIGNMET commands
     	 			{
     	 				// Identify DEF and USE variables
     	 				String[] def_expr=cmd.split("=");
     	 				String def=def_expr[0].trim();
     	 				String expr=def_expr[1].trim();
     	 			
     	 				String TokenSeperator=REGEX_ARITH_OPERATION+"|"+REGEX_PARANTHESIS;
     	 				LinkedList useVars= super.getUseVars(expr, TokenSeperator);     	 				   	 				
					
     	 				String use=super.convertListToString(useVars);
     	 				if(use.equals(""))
						{
							use="*";	// no USE variable present
						}
					
     	 				// Writing details to ExtractInfo File
     	 				psExtractInfo.println((++line_no) + " " + type + " " + cntrl + " " + def+ " " + use + " " + cmd + " " + label);
     	 			}	
     	 			else if(type.equals("pr")) // You have PRINT commands
     	 			{
     	 				
     	 				// Identify USE variables
     	 				String expr=cmd.substring(cmd.indexOf("(")+1,cmd.indexOf(")")).trim();     	 	
     	 				
     	 				String TokenSeperator=",";
     	 				LinkedList useVars= super.getUseVars(expr, TokenSeperator);     	 				   	 				
					
     	 				String use=super.convertListToString(useVars);
     	 				if(use.equals(""))
						{
							use="*"; // no USE variable present	
						}
						
						// Writing details to ExtractInfo File			
     	 				psExtractInfo.println((++line_no) + " " + type + " " + cntrl + " " + "*"+ " " + use + " " + cmd + " " + label);
     	 			}
     	 			else if(type.equals("pi")) // You have PHI commands
     	 			{
     	 				
     	 				// Identify DEF and USE variables
     	 				String[] def_expr=cmd.split("<<");
     	 				String def=def_expr[0].trim();
     	 				String expr=def_expr[1].trim();
     	 				expr=cmd.substring(cmd.indexOf("(")+1,cmd.indexOf(")")).trim();     	 	
     	 				
     	 				String TokenSeperator=",";
     	 				LinkedList useVars= super.getUseVars(expr, TokenSeperator);     	 				   	 				
					
     	 				String use=super.convertListToString(useVars);
     	 				if(use.equals(""))
						{
							use="*";	// no USE variable present	
						}
						
						// Writing details to ExtractInfo File			
     	 				psExtractInfo.println((++line_no) + " " + type + " " + cntrl + " " + def+ " " + use + " " + cmd + " " + label);
     	 			}
     	 			else if(type.equals("sk")||type.equals("sp")) // You have "skip" and "stop" command
     	 			{
     	 				// Writing details to ExtractInfo File
     	 				psExtractInfo.println((++line_no) + " " + type + " " + cntrl + " " + "*"+ " " + "*" + " " + cmd + " " + label);	
     	 			}
     	 			
      		
    	  			if(type.equals("mid-st") || type.equals("mid-if") || type.equals("mid-wh"))
    	  			{
      					// You are about to exit TRUE block and entering into FALSE block of the control statements
      					
      					// Extract TRUE control Information
      					String extract_cntrl_T=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1).trim();
      					String[] extract_cntrl_T_spl=extract_cntrl_T.split(":");
      					
      					// Remove TRUE control Information from "cntrl_seq"
      					cntrl_seq=cntrl_seq.substring(0, cntrl_seq.lastIndexOf("_")).trim();
      				
      					// Extract FALSE control Information
      					String extract_cntrl_F=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1).trim();
      					String[] extract_cntrl_F_spl=extract_cntrl_F.split(":");
      				
      					// Extract type of the control command
      					String extract_type_T=new String("mid-"+extract_cntrl_T_spl[0].trim());
      					String extract_type_F=new String("mid-"+extract_cntrl_F_spl[0].trim());
      				
      					if(type.equals(extract_type_T) && type.equals(extract_type_F)) // Control Type matches with extracted type
      					{
      					
      						// Update control information
      						cntrl=extract_cntrl_F_spl[1].trim()+":"+extract_cntrl_F_spl[2].trim();
  	    				}
  	    				else
  	    				{
  	    					System.out.println("ProgInfo.extractProg(): Incorrect Syntax at the middle of control block - " + cmd);		
  	    				}
    	  			}
      		
      				if(type.equals("end-st") || type.equals("end-if") || type.equals("end-wh"))
      				{
      					// You are about to exit the control blocks
      					
      					// Extract FALSE control Information
 	     				String extract_cntrl_F=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1).trim();      			
    	  				String[] extract_cntrl_F_spl=extract_cntrl_F.split(":");
      			
      					if(type.equals("end-"+extract_cntrl_F_spl[0].trim()))// Control Type matches with extracted type
      					{
      						// Remove FALSE control Information from "cntrl_seq"
      						cntrl_seq=cntrl_seq.substring(0, cntrl_seq.lastIndexOf("_")).trim();
      						
      						if(cntrl_seq.length()!=0)
      						{
      							// Update control information
      							String extract_cntrl=cntrl_seq.substring(cntrl_seq.lastIndexOf("_")+1).trim();
      							String[] extract_cntrl_spl=extract_cntrl.split(":");
      							cntrl=extract_cntrl_spl[1].trim()+":"+extract_cntrl_spl[2].trim();
	      					}
	      					else
  	    					{
  	    						System.out.println("ProgInfo.extractProg(): Incorrect Syntax at the end of control block - " + cmd);		
  	    					}
      					}
      					else
  	    				{
  	    					System.out.println("ProgInfo.extractProg(): Incorrect Syntax at the end of control block - " + cmd);		
  	   		 			}
      				}	
      			}
      	 	}		
		  
		// To check whether "cntrl_seq" is back to its initial state
		
		if(cntrl_seq.equals("_xx:-1:T"))
		{
			psExtractInfo.println("\n\n");				
		}
		else
		{
			System.out.println("packageProgInfo.extractProg(): Syntactic Error: Control sequence is not empty.");
			psExtractInfo.println("packageProgInfo.extractProg(): Syntactic Error: Control sequence is not empty.");
			System.exit(1);
		}    		

		System.out.println("packageProgInfo.extractProg(): The End.");
	
	   }
	   catch (FileNotFoundException e){System.err.println("packageProgInfo.extractProg(): Input File not found."+e);}
	   catch (IOException e){ System.err.println("packageProgInfo.extractProg(): Problem with IO exception."+e);}
	
	}
		
}