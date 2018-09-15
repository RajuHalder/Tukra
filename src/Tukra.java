/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * These classes define Graphical User Interfaces of Tukra
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.text.*;

import packageProgInfo.*;
import packageSemanticProgInfo.*;
import packageMatrix.*;
import packageMyLibrary.*;
import packageSSAconversion.*;
import packageDCGannotations.*;
import packageDCGannotations.genDCGannotations.*;
import packageDCGProgInfo.*;

import java.net.URL;
import java.io.IOException;

// First GUI asking for Input Program on which slicing computations will be performed
class startGUI1 extends JPanel implements ActionListener,myFilePaths
{	
	JTextField GUI1_chooseTextField;
	JButton GUI1_chooseButton, GUI1_nextButton, GUI1_cancelButton, GUI1_notepadButton;
	JTextArea GUI1_textArea;

	JPanel GUI1_topPanel, GUI1_leftPanel, GUI1_rightPanel;
	JPanel GUI1_choosePanel, GUI1_buttonPanel;
	
	Container GUI1_container;
      			     			
 	startGUI1(Container c)
	{
			
		this.GUI1_container=c;
		
		// topPanel for the Blinking Instruction
		GUI1_topPanel=new JPanel();
		
		String instruction="Browse the program file or write your program in the text area on right side.";
		BlinkLabel instrLabel = new BlinkLabel(instruction);
		instrLabel.setFont(new Font("Serif", Font.ITALIC, 20));
		instrLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.blue));
		GUI1_topPanel.add(instrLabel, BorderLayout.CENTER);
        	
        // choosePanel - For Browsing Input Program File
		GUI1_choosePanel=new JPanel(new GridBagLayout());

		GUI1_chooseTextField=new JTextField(15); // The textfield to Browse the file
		JLabel GUI1_chooseLabel=new JLabel("Choose Program File:"); // Label for the TextField
		GUI1_chooseLabel.setFont(new Font("Serif", Font.BOLD, 16));
		GUI1_chooseLabel.setLabelFor(GUI1_chooseTextField);		
		GUI1_chooseButton=new JButton("Browse"); // Button to browse program in your computer
		
		// Constraint to layout the components	
        GridBagConstraints constraint = new GridBagConstraints();            
        constraint.anchor = GridBagConstraints.FIRST_LINE_START;        
        //constraint.fill = GridBagConstraints.NONE;
        //constrint.gridwidth = GridBagConstraints.REMAINDER;
        //constrint.gridheigth = GridBagConstraints.REMAINDER;
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.gridy = 0;
        constraint.insets = new Insets(147,0,0,0);                        
        GUI1_choosePanel.add(GUI1_chooseLabel, constraint);
        constraint.insets = new Insets(150,10,0,0);  
        GUI1_choosePanel.add(GUI1_chooseTextField, constraint);
        constraint.insets = new Insets(147,20,0,0);   
        GUI1_choosePanel.add(GUI1_chooseButton, constraint);
        
        GUI1_choosePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		// bottonPanel - For three buttons "NEXT/CANCEL/NotePad"				
		GUI1_buttonPanel=new JPanel(new GridBagLayout());		
		GUI1_notepadButton=new JButton("Open NotePad");
		GUI1_nextButton=new JButton("Next");
		GUI1_cancelButton=new JButton("Cancel");
		
		// Constraint to layout three buttons	
		constraint.anchor = GridBagConstraints.CENTER;
        //constraint.fill = GridBagConstraints.NONE;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.REMAINDER;
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.gridy = 0;
        constraint.insets = new Insets(0,50,80,0);				
		GUI1_buttonPanel.add(GUI1_notepadButton,constraint);     
        constraint.insets = new Insets(0,50,80,0);
		GUI1_buttonPanel.add(GUI1_nextButton, constraint);
		constraint.insets = new Insets(0,50,80,0);				
		GUI1_buttonPanel.add(GUI1_cancelButton,constraint);       
        
        GUI1_buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        // leftPanel containing two panels: choosePanel and buttonPanel        
        GUI1_leftPanel= new JPanel(new BorderLayout());
        GUI1_leftPanel.add(GUI1_choosePanel, BorderLayout.PAGE_START);
        GUI1_leftPanel.add(GUI1_buttonPanel, BorderLayout.PAGE_END);
                        
        // rightPanel containing a Text Area to write program as input                  
        GUI1_textArea = new JTextArea();
        GUI1_textArea.setEditable(true);
		JScrollPane areaScrollPane = new JScrollPane(GUI1_textArea); 
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(350, 400));
        areaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Write your Program"),BorderFactory.createEmptyBorder(5,5,5,5)), areaScrollPane.getBorder()));
        
        // Constraint to layout the Text Area	
        JPanel GUI1_rightPanel = new JPanel(new GridBagLayout()); 
        constraint.anchor = GridBagConstraints.CENTER;
        //constraint.fill = GridBagConstraints.NONE;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.REMAINDER;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.insets = new Insets(25,0,0,0);     	
		GUI1_rightPanel.add(areaScrollPane, constraint);
		
		// Add all Components to the Container: topPanel, leftPanel, rightPanel
		GUI1_container.add(GUI1_topPanel, BorderLayout.NORTH);
		GUI1_container.add(GUI1_leftPanel, BorderLayout.WEST);
		GUI1_container.add(GUI1_rightPanel, BorderLayout.EAST);
		
		GUI1_container.setVisible(true);

		// Enable the ActionListener
		GUI1_chooseButton.addActionListener(this);
		GUI1_notepadButton.addActionListener(this);
		GUI1_nextButton.addActionListener(this);
		GUI1_cancelButton.addActionListener(this);		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if(e.getSource().equals(GUI1_chooseButton)) // action for "Browse" button
			{
				Chooser inframe = new Chooser();
				inframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				
				GUI1_chooseTextField.setText(inframe.fileName);
			}				
			else if(e.getSource().equals(GUI1_nextButton)) // Going to next GUI "GUI2"
			{
				// Getting path of input program flie
				String in_file_name=GUI1_chooseTextField.getText();
				
				// Computing program length written on the rightside Text Area
				Document doc=GUI1_textArea.getDocument(); 
				int docLen=doc.getLength();
								
				if(in_file_name.equals("") && docLen==0) // No input file choosen and no program on the right textarea
				{
					// show message
					JOptionPane.showMessageDialog(this, "Provide Input Program.");	
				}
				if(!in_file_name.equals("") && docLen!=0) // Both the input file choosen and program is written in the textarea
				{
					// show message not to provide multiple input program
					JOptionPane.showMessageDialog(this, "Please Choose Single Input.");	
				}				
				else // Input program found correctly
				{
					genProgInfo ProgObj=null;
					
					// Create an object of the class "packageProgInfo.genProgInfo"
					if(!in_file_name.trim().equals("") && docLen==0)
					{      			
      				
   		   				DataInputStream disInput = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(in_file_name))));			
    					
      					ProgObj=new genProgInfo(disInput);
      				
     					disInput.close();  				      				
      				}      			
	      			else if(in_file_name.trim().equals("")   && docLen!=0)
    	  			{

						String ProgText=doc.getText(0, docLen);
						ProgText=ProgText.replaceAll("[ \\s]+", " ");		
      				
      					ProgObj=new genProgInfo(ProgText);
      				}  
      			
      				// Rephrase the program that will be used to preview on the screen in future
      				// Now store it to the file "File_Out_Preview"
      				PrintStream	psPreview = new PrintStream( new FileOutputStream(File_Out_Preview) );      				
					ProgObj.previewProg(psPreview);
					psPreview.close();
      			
      				// Extract details information from the preview as an intermediate representation
      				// and store it to file "File_Out_ExtractInfo"
      				DataInputStream disPreview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(File_Out_Preview))));
					PrintStream	psExtractInfo = new PrintStream( new FileOutputStream(File_Out_ExtractInfo) );			

					ProgObj.GenerateInformation(disPreview, psExtractInfo);
			
	      			disPreview.close();	
    	  			psExtractInfo.close();
    	  
      				// Call to next Graphical User Interface "GUI2" with necessay information      				
      				String Preview_File=File_Out_Preview;
      				String ExtractInfo_File=File_Out_ExtractInfo;
      				GUI1_container.setVisible(false);
					GUI1_container.removeAll();
					new startGUI2(GUI1_container, Preview_File, ExtractInfo_File);
				}   			      			
      		}
      		else if(e.getSource().equals(GUI1_notepadButton)) // Opening NotePad software
      		{
      			
				Runtime r=Runtime.getRuntime();
        		Process p=null;
        
        		try
        		{
           			p=r.exec("notepad");
        		}
        		catch(Exception exc)
        		{
            		System.out.print("Fail to Open Notepad Opened");
        		}
        		System.out.print("Notepad Closed"+p.exitValue());	
      		}
			else if(e.getSource().equals(GUI1_cancelButton)) // Cancelling the window.....
			{
				System.exit(1);
			}
		}
		catch(Exception err) // Error found
		{
			System.err.println("Tukra.startGUI1.ActionListener(): Error Occurred......" + err);
		}
	}	
}



/* Second GUI that shows preview of input program 
 * and give options to perform syntactic slicing 
 * and give options to choose semantic computations and abstract domain */
 
class startGUI2 extends JPanel implements ActionListener, myFilePaths
{
		
	JPanel GUI2_leftPanel,GUI2_midPanel, GUI2_rightPanel, GUI2_radioPanel, GUI2_checkboxPanel, GUI2_buttonPanel;
	JEditorPane GUI2_TextPane;
	JScrollPane GUI2_editorScrollPane;
	JButton GUI2_goButton, GUI2_cancelButton, GUI2_backButton, GUI2_pdgButton, GUI2_cfgButton, GUI2_syntactic_slice_Button;
	
	JCheckBox GUI2_Semantic_Relevancy_Box, GUI2_Semantic_Dependency_Box;
	
	JRadioButton GUI2_signRadioButton, GUI2_parRadioButton;
	final ButtonGroup GUI2_groupRadioButton;
	
	Container GUI2_container;
	String Preview_File, ExtractInfo_File;
	
	startGUI2(Container c, String PreviewFile, String ExtractInfoFile){
				
				this.GUI2_container=c; // Container provided by "GUI1"
				this.Preview_File=PreviewFile; // Path of the file that contains the preview of the original program
				this.ExtractInfo_File=ExtractInfoFile; // Path of the file that contains detail information of the original program
				
				// leftPanel to show the preview of the program	
	      		GUI2_leftPanel=new JPanel(new GridBagLayout());      			      			
      			  					
				GUI2_TextPane = new createTextPane(Preview_File).getTextPane();
        		GUI2_TextPane.setEditable(false);
       			GUI2_editorScrollPane = new JScrollPane(GUI2_TextPane);  
       			GUI2_editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);     	
        		GUI2_editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        		GUI2_editorScrollPane.setPreferredSize(new Dimension(300, 400));
        		GUI2_editorScrollPane.setMinimumSize(new Dimension(300,300));
        		
        		GUI2_leftPanel.add(GUI2_editorScrollPane); 
        		
        		GUI2_leftPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Original Program"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));	
        		
        		// midPanel containing Syntactic slicing components        		
        		GUI2_midPanel=new JPanel(new GridBagLayout());
        		GUI2_pdgButton=new JButton("Show PDG");
        		GUI2_cfgButton=new JButton("Show CFG");
        		GUI2_syntactic_slice_Button=new JButton("Slice");
        		
        		GridBagConstraints constraint = new GridBagConstraints();
        		constraint.anchor = GridBagConstraints.CENTER;
        		//constraint.fill = GridBagConstraints.BOTH;
        		//constraint.gridwidth = GridBagConstraints.REMAINDER;
        		//constraint.gridheight = GridBagConstraints.RELATIVE;
        		constraint.gridx = 0;
        		constraint.gridy = 0;   
        		constraint.insets = new Insets(20,50,20,50);        		
        		GUI2_midPanel.add(GUI2_pdgButton, constraint);        		
       			constraint.gridy = 1;        		
        		GUI2_midPanel.add(GUI2_cfgButton, constraint);        		
        		constraint.gridy = 2;
       			GUI2_midPanel.add(GUI2_syntactic_slice_Button, constraint);
       			
        		GUI2_midPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Syntactic Slicing"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
                     
        		
        		// checkboxPanel - Displaying options for choosing Semantic Computations
				GUI2_checkboxPanel=new JPanel(new GridBagLayout());				
				
				GUI2_Semantic_Relevancy_Box = new JCheckBox("Apply Semantic Relevancy");
    			GUI2_Semantic_Relevancy_Box.setMnemonic(KeyEvent.VK_R);
    			GUI2_Semantic_Dependency_Box = new JCheckBox("Apply Semantic Data Dependency");
    			GUI2_Semantic_Dependency_Box.setMnemonic(KeyEvent.VK_D); 
    			
    			constraint.anchor = GridBagConstraints.FIRST_LINE_START;
        		//constraint.fill = GridBagConstraints.VERTICAL;
        		//constraint.gridwidth = 1;
        		//constraint.gridheight = 1;
        		constraint.gridx = 0;
        		constraint.gridy = GridBagConstraints.RELATIVE;
        		constraint.insets = new Insets(0,0,0,0);        		
    			GUI2_checkboxPanel.add(GUI2_Semantic_Relevancy_Box, constraint);
    			constraint.insets = new Insets(10,0,0,0);    
    			GUI2_checkboxPanel.add(GUI2_Semantic_Dependency_Box, constraint);
    			
    			GUI2_checkboxPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Tick Your Choices"),
                        BorderFactory.createEmptyBorder(25,5,25,5)));
    			
    			
    			//radioPanel - Displaying options for selecting Abstract Domain
    			GUI2_radioPanel=new JPanel(new GridBagLayout());
    			
    			GUI2_signRadioButton=new JRadioButton("Sign");
    			GUI2_signRadioButton.setMnemonic(KeyEvent.VK_S);
				GUI2_parRadioButton=new JRadioButton("Par");
				GUI2_parRadioButton.setMnemonic(KeyEvent.VK_P);
				
				GUI2_groupRadioButton = new ButtonGroup();
        		GUI2_groupRadioButton.add(GUI2_signRadioButton);
        		GUI2_groupRadioButton.add(GUI2_parRadioButton);
 						
				constraint.anchor = GridBagConstraints.FIRST_LINE_START;
        		//constraint.fill = GridBagConstraints.NONE;
        		//constraint.gridwidth = GridBagConstraints.RELATIVE;
        		//constraint.gridheight = GridBagConstraints.REMAINDER;
        		constraint.gridx = GridBagConstraints.RELATIVE;
        		constraint.gridy = 0;
        		constraint.insets = new Insets(0,10,0,0);        		
				GUI2_radioPanel.add(GUI2_signRadioButton,constraint);
				constraint.insets = new Insets(0,30,0,0);
				GUI2_radioPanel.add(GUI2_parRadioButton, constraint);
				
				GUI2_radioPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Choose Abstract Domain"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
		
		
				//buttonPanel - For "Go", "Back" and "Cancel" Buttons					
				GUI2_buttonPanel=new JPanel(new GridBagLayout()); 				
				GUI2_backButton=new JButton("Back");
				GUI2_goButton=new JButton("Go");
				GUI2_cancelButton=new JButton("Cancel");  
				
				constraint.anchor = GridBagConstraints.CENTER;
        		//constraint.fill = GridBagConstraints.NONE;
        		//constraint.gridwidth = GridBagConstraints.RELATIVE;
        		//constraint.gridheight = GridBagConstraints.REMAINDER;
        		constraint.gridx = GridBagConstraints.RELATIVE;
        		constraint.gridy = 0;
        		constraint.insets = new Insets(0,30,0,0);
				GUI2_buttonPanel.add(GUI2_backButton, constraint);
        		constraint.insets = new Insets(0,30,0,0);
				GUI2_buttonPanel.add(GUI2_goButton, constraint);
				constraint.insets = new Insets(0,40,0,0);				
				GUI2_buttonPanel.add(GUI2_cancelButton,constraint);      		
        		        		
        		GUI2_buttonPanel.setBorder(BorderFactory.createEmptyBorder(50,5,50,5));
                  
                                		
        		// rightPanel containg three sub panels : checkboxPanel, radioPanel, buttonPanel        			       		
        		GUI2_rightPanel=new JPanel(new BorderLayout());
        		
       			GUI2_rightPanel.add(GUI2_checkboxPanel, BorderLayout.PAGE_START);
       			GUI2_rightPanel.add(GUI2_radioPanel, BorderLayout.CENTER);
       			GUI2_rightPanel.add(GUI2_buttonPanel, BorderLayout.PAGE_END);				
					    		
        		      		
        		GUI2_rightPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Abstract Semantic-based Slicing"),
                        BorderFactory.createEmptyBorder(5,25,5,25)));

				
				// Add all panels to the Container:  leftPanel, midPanel, rightPanel				
      			GUI2_container.add(GUI2_leftPanel, BorderLayout.LINE_START);
      			GUI2_container.add(GUI2_midPanel, BorderLayout.CENTER);
      			GUI2_container.add(GUI2_rightPanel, BorderLayout.LINE_END);
      			
      			GUI2_container.setVisible(true);
      			
      			// Enable Action Listeners     			
      			GUI2_pdgButton.addActionListener(this);
      			GUI2_cfgButton.addActionListener(this);
      			GUI2_syntactic_slice_Button.addActionListener(this);
      			GUI2_backButton.addActionListener(this);
      			GUI2_goButton.addActionListener(this);
      			GUI2_cancelButton.addActionListener(this);
      			
	}
			
	public void actionPerformed(ActionEvent e)
	{	

		try
		{
			if(e.getSource().equals(GUI2_goButton)) // To go to the next GUI "GUI3"
			{
				
				// Checking which abstract domain and which options for semantics computations are chosen
				StringBuffer SemOptions=new StringBuffer("--");
				String AbsDom="";
				
				if(GUI2_Semantic_Relevancy_Box.isSelected()==true) // If relevancy computation option is selected
				{
					SemOptions.setCharAt(0,'r');
				}
				
				if(GUI2_Semantic_Dependency_Box.isSelected()==true) // If semantic dependences option is selected
				{
					SemOptions.setCharAt(1,'d');
				}
				
				if(GUI2_signRadioButton.isSelected()==true) // SIGN abstract domain selected
				{
					AbsDom="sign";
				}
				else if(GUI2_parRadioButton.isSelected()==true) // PAR abstract domain selected
				{
					AbsDom="par";
				}
				
				if(SemOptions.toString().equals("--")) // If no semantic computation option selected
				{
					// Give error message
					JOptionPane.showMessageDialog(this, "Please choose one Semantic Approach.");		
				}
				else
				{
					if(AbsDom.equals("")) // If no abstract domain selected
					{
						// Give error message
						JOptionPane.showMessageDialog(this, "Please choose one Abstract Domain.");		
					}
					else
					{
						// Both the abstract domain and semantic compuation options are selected
						
						String SemanticPreview=""; // Contains the path of the file that contains refined preview of the program
						String SemanticExtractInfo=""; // Contains the path of the file that contains details of the refined program
						
						LinkedList nonRelList=new LinkedList(); // Contains the list of irrelevant statements
						LinkedList nonDepList=new LinkedList(); // Contains the program labels and associated irrelevant variables
						
						if(SemOptions.toString().equals("r-")) 
						{
							/* Perform relevancy computation in the abstract domain "AbsDom"
							 * Return the list of irrelevant statements' labels
							 * Write the preview of refined program in "File_Out_Rel_SemanticPreview" 
							 * Write the details of the refined program in "File_Out_Rel_SemanticExtractInfo" */
							 
							RandomAccessFile raf_ExtractInfo = new RandomAccessFile(ExtractInfo_File, "r");   					
							genSemanticProgInfo obj=new genSemanticProgInfo(raf_ExtractInfo, AbsDom);   						
    						nonRelList=obj.applyRelevancy(Preview_File, File_Out_Rel_SemanticPreview, File_Out_Rel_SemanticExtractInfo);
    						
    						raf_ExtractInfo.close();
    	  					
							SemanticPreview=Preview_File;
							SemanticExtractInfo=File_Out_Rel_SemanticExtractInfo;
												
						}
						else if(SemOptions.toString().equals("-d"))
						{
							/* Perform semantic dependences computation in the abstract domain "AbsDom"
							 * Return the list of independent variables associated with each statement labels
							 * Write the preview of the refined program in "File_Out_Dep_SemanticPreview" 
							 * Write the details of the refined program in "File_Out_Dep_SemanticExtractInfo" */
							 
							RandomAccessFile raf_ExtractInfo = new RandomAccessFile(ExtractInfo_File, "r");   					
							genSemanticProgInfo obj=new genSemanticProgInfo(raf_ExtractInfo, AbsDom);
							nonDepList=obj.applyDependency(Preview_File, File_Out_Dep_SemanticPreview, File_Out_Dep_SemanticExtractInfo);
					
							raf_ExtractInfo.close();
 	  					
							SemanticPreview=Preview_File;
							SemanticExtractInfo=File_Out_Dep_SemanticExtractInfo;
							
						}
						else if(SemOptions.toString().equals("rd"))
						{
							
							/* Perform semantic relevancy computation first
							 * And then, performs semantic data dependences computation on the result obtained by the first 
							 * Write the preview of the finally refined program in "File_Out_Dep_SemanticPreview" 
							 * Write the details of the finally refined program in "File_Out_Dep_SemanticExtractInfo"*/
							
							RandomAccessFile raf_ExtractInfo = new RandomAccessFile(ExtractInfo_File, "r");   					
							genSemanticProgInfo obj=new genSemanticProgInfo(raf_ExtractInfo, AbsDom);
  						
    						nonRelList=obj.applyRelevancy(Preview_File, File_Out_Rel_SemanticPreview, File_Out_Rel_SemanticExtractInfo);
    						
    						raf_ExtractInfo.close();
	  					
    	  					System.out.println("Semantic Relevency is done....Now starting dependency computation....");
    						
    						RandomAccessFile raf_Rel_SemanticExtractInfo = new RandomAccessFile(File_Out_Rel_SemanticExtractInfo, "r");   					
							genSemanticProgInfo RelObj=new genSemanticProgInfo(raf_Rel_SemanticExtractInfo, AbsDom);
							
							nonDepList=RelObj.applyDependency(File_Out_Rel_SemanticPreview, File_Out_Dep_SemanticPreview, File_Out_Dep_SemanticExtractInfo);
							
							raf_Rel_SemanticExtractInfo.close();    	  					
	  					
							SemanticPreview=Preview_File;
							SemanticExtractInfo=File_Out_Dep_SemanticExtractInfo;	      					
    	  					
    					}
						 	
						// Call to next Graphical User Interface "GUI3" with necessay information  				
						startGUI3 GUI3obj=new startGUI3(SemanticPreview,SemanticExtractInfo,nonRelList,nonDepList,AbsDom);
						GUI3obj.setSize(700,425);
						GUI3obj.setLocation(475,200);
						GUI3obj.setVisible(true); 					
					}	
				}
			}
			else if(e.getSource().equals(GUI2_pdgButton)) // To draw the PDG of the original program
			{
				
				RandomAccessFile raf = new RandomAccessFile(ExtractInfo_File, "r");      			
      			formMatrix obj=new formMatrix(raf);

      			int[][] PDGmat=obj.getPDGmatrix();
      			obj.DrawMatix(PDGmat);
      			
				raf.close();
			}
			else if(e.getSource().equals(GUI2_cfgButton)) // To draw the CFG of the original program
			{
 				
				RandomAccessFile raf = new RandomAccessFile(ExtractInfo_File, "r");      			
      			formMatrix obj=new formMatrix(raf);

      			int[][] CFGmat=obj.getCFGmatrix();
      			obj.DrawMatix(CFGmat);
      			
				raf.close();		
			}
			else if(e.getSource().equals(GUI2_syntactic_slice_Button)) //Performing syntax-based slicing of the original Program
			{
				// Get slicing criteria where list of program variables are seperates by comma.
				String[] input=(new OptionPaneMultiple()).getInput();
				String ProgPoint=input[0].trim();
				String Vars=input[1].trim();
				
				if(!ProgPoint.equals("") && !Vars.equals(""))
				{
					
      				// Converting variable string (separated by comma) in the criteria into list of program variables
      				LinkedList VarsList=new LinkedList();
      				String[] VarsArr=Vars.split(",");
      				for(int i=0; i<VarsArr.length;i++)
      				{
      					VarsList.addLast(VarsArr[i].trim());
      				}
      				
      				// Performing slicing and return "true" if succeed
      				RandomAccessFile raf = new RandomAccessFile(File_Out_ExtractInfo, "r");      			
      				formMatrix matrixObj=new formMatrix(raf);
      				boolean flag=matrixObj.performSlicing(ProgPoint, VarsList, Preview_File, File_Out_Slice_Preview, File_Out_Slice_ExtractInfo);
      				
      				if(flag) // If slicing is successful
      				{
      					// Display the preview of the slice in a slice-window
      					SliceWindow slc=new SliceWindow(File_Out_Slice_Preview, new LinkedList(), new LinkedList(), ProgPoint, Vars, new String(""));
      					slc.setSize(350,350);
						slc.setLocation(600,30);
						slc.setVisible(true);	
      				}
      				else
      				{
      					// Slicing is not successful: error message
      					JOptionPane.showMessageDialog(this, "Error in the slicing criteria!");
      				}
      				
      				raf.close();
				}
				else // Error in slicing criterion
				{
					JOptionPane.showMessageDialog(this, "Without Slicing Criteria I can't Proceed.");
				}
				
			}
			else if(e.getSource().equals(GUI2_backButton)) // Going back to previous GUI "GUI1"
			{
				GUI2_container.setVisible(false);
				GUI2_container.removeAll();
				new startGUI1(GUI2_container);
			}
			else if(e.getSource().equals(GUI2_cancelButton)) // Cancelling the window
			{
				System.exit(0);
			}
		}
		catch(Exception ex) // Error found
		{
			System.err.println("Tukra.startGUI2.ActionListener(): Error Occurred..... " + ex);
		}
	}
}


/* Third GUI that shows preview of refined program obtained by semantic relevancy and data dependences computations 
 * Gives options to perform semantic-based slicing on the refined program obtained before
 * Also give options to perform further refinement based on the DCG annotations and slicing based on it */
class startGUI3 extends JFrame  implements ActionListener, myFilePaths
{
		
	JPanel GUI3_leftPanel, GUI3_rightPanel, GUI3_upperRightPanel, GUI3_lowerRightPanel, GUI3_DCG_pan_1, GUI3_DCG_pan_2, GUI3_DCG_pan_3;
	
	JTextPane GUI3_leftPanel_TextPane;
	JScrollPane GUI3_leftPanel_editorScrollPane;
	
	JButton GUI3_upperPDGbutton, GUI3_upperCFGbutton, GUI3_upperSliceButton;
	JButton GUI3_lowerDCGbutton, GUI3_lowerRefineButton, GUI3_lowerSliceButton, GUI3_lowerCancelButton;
	JLabel GUI3_lb_SliceProgPoint, GUI3_lb_SliceProgVar;
	JTextField GUI3_tf_SliceProgPoint, GUI3_tf_SliceProgVar;
	
	String GUI3_SemanticPreviewFile, GUI3_SemanticExtractInfo, GUI3_AbsDom; 
	LinkedList nonDepList, nonRelList;
	
	startGUI3(String SemanticPreviewFile, String SemanticExtractInfo, LinkedList nonRelList, LinkedList nonDepList, String AbsDom)
	{
		super("Abstract Slicing");
		
		// Preview of the refined program obtained by two semantic computations
		this.GUI3_SemanticPreviewFile=SemanticPreviewFile; 
		
		// Detail information of the refined program obtained by two semantic computations
		this.GUI3_SemanticExtractInfo=SemanticExtractInfo;
		
		// Abstract domain choosen before
		this.GUI3_AbsDom=AbsDom;
		
		// List of irrelevant statements labels and non-data-dependences information
		this.nonDepList=nonDepList;
		this.nonRelList=nonRelList;
				
				
		//leftPanel containing preview area for previously refined program		
		GUI3_leftPanel=new JPanel(new GridBagLayout());      			      			
      			
		if(nonDepList.size()==0 && nonRelList.size()==0)
		{
			GUI3_leftPanel_TextPane = new createTextPane(GUI3_SemanticPreviewFile).getTextPane();
		}
		else
		{
			GUI3_leftPanel_TextPane = new createTextPane(GUI3_SemanticPreviewFile, nonRelList, nonDepList).getTextPane();
		}
			
        GUI3_leftPanel_TextPane.setEditable(false);
       	GUI3_leftPanel_editorScrollPane = new JScrollPane(GUI3_leftPanel_TextPane);  
       	GUI3_leftPanel_editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);     	
        GUI3_leftPanel_editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        GUI3_leftPanel_editorScrollPane.setPreferredSize(new Dimension(275, 250));
        GUI3_leftPanel_editorScrollPane.setMinimumSize(new Dimension(275,250));
        		
        GUI3_leftPanel.add(GUI3_leftPanel_editorScrollPane); 
        		
        GUI3_leftPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Semantics-based Program"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));	
        		
        //upperRightPanel containing buttons to show PDG/CDG and to perform semantics-based slicing        		
        GUI3_upperRightPanel=new JPanel(new GridBagLayout());
        
        GUI3_upperPDGbutton=new JButton("Show PDG");
        GUI3_upperCFGbutton=new JButton("Show CFG");
        GUI3_upperSliceButton=new JButton("Slice");
        		
        GridBagConstraints constraint = new GridBagConstraints();

        constraint.anchor = GridBagConstraints.CENTER;
        //constraint.fill = GridBagConstraints.BOTH;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.REMAINDER;
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.gridy = 0;   
        constraint.insets = new Insets(5,25,5,0);        		
        GUI3_upperRightPanel.add(GUI3_upperPDGbutton, constraint);
        GUI3_upperRightPanel.add(GUI3_upperCFGbutton, constraint);        		
		GUI3_upperRightPanel.add(GUI3_upperSliceButton, constraint);
       			
        GUI3_upperRightPanel.setBorder(BorderFactory.createCompoundBorder(
                  		BorderFactory.createTitledBorder("Semantics PDG-based Slice"),
                   		BorderFactory.createEmptyBorder(25,5,25,5)));
             
             
                   		
					
		
		// GUI3_DCG_pan_1 contains two buttons: "Show DCG" and "Refinement"			
        GUI3_DCG_pan_1=new JPanel(new GridBagLayout());
        
        GUI3_lowerDCGbutton=new JButton("Show DCG");
        GUI3_lowerRefineButton=new JButton("Refinement");               		
        
        constraint.anchor = GridBagConstraints.CENTER;
        //constraint.fill = GridBagConstraints.BOTH;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.REMAINDER;
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.gridy = 0;   
        constraint.insets = new Insets(30,25,30,0);        		
        GUI3_DCG_pan_1.add(GUI3_lowerDCGbutton, constraint);
        GUI3_DCG_pan_1.add(GUI3_lowerRefineButton, constraint);
		
		// GUI3_DCG_pan_2 contains (i) text area for slicing criterion: program point and program variables and (ii) slice button
		GUI3_DCG_pan_2=new JPanel(new GridBagLayout());
		
		GUI3_tf_SliceProgPoint=new JTextField(5); 
		GUI3_tf_SliceProgVar=new JTextField(8);
		GUI3_lb_SliceProgPoint=new JLabel("Choose Program Point:"); 
		GUI3_lb_SliceProgVar=new JLabel("Choose Program Variable:");
		GUI3_lb_SliceProgPoint.setFont(new Font("Serif", Font.BOLD, 12));
		GUI3_lb_SliceProgVar.setFont(new Font("Serif", Font.BOLD, 12));
		GUI3_lb_SliceProgPoint.setLabelFor(GUI3_tf_SliceProgPoint);
		GUI3_lb_SliceProgVar.setLabelFor(GUI3_tf_SliceProgVar);
		GUI3_lowerSliceButton=new JButton("Slice"); 
				
        constraint.anchor = GridBagConstraints.LINE_START;
        //constraint.fill = GridBagConstraints.NONE;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.RELATIVE;
		constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.insets = new Insets(5,0,0,0);                        
        GUI3_DCG_pan_2.add(GUI3_lb_SliceProgPoint, constraint);
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.insets = new Insets(5,10,0,0);  
        GUI3_DCG_pan_2.add(GUI3_tf_SliceProgPoint, constraint);
        
        constraint.gridx = 0;
        constraint.gridy = 1;
        constraint.insets = new Insets(5,0,0,0);                        
        GUI3_DCG_pan_2.add(GUI3_lb_SliceProgVar, constraint);
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.insets = new Insets(5,10,0,0);  
        GUI3_DCG_pan_2.add(GUI3_tf_SliceProgVar, constraint);        
        
        //constraint.gridx = GridBagConstraints.CENTER;
        //constraint.gridy = 2;
        constraint.insets = new Insets(0,10,0,0);                        
        GUI3_DCG_pan_2.add(GUI3_lowerSliceButton, constraint);
       
       
       	// GUI3_DCG_pan_3 contains "Close" button that used to close the window......
       	GUI3_DCG_pan_3=new JPanel(new GridBagLayout());
        GUI3_lowerCancelButton=new JButton("Close"); 
        
        constraint.anchor = GridBagConstraints.CENTER;
        //constraint.fill = GridBagConstraints.BOTH;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.REMAINDER;
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.gridy = 0;   
        constraint.insets = new Insets(30,25,30,0);        		
        GUI3_DCG_pan_3.add(GUI3_lowerCancelButton, constraint);
       		
       
        // lowerRightPanel containing two more sub panels for DCG computation:GUI3_DCG_pan_1, GUI3_DCG_pan_2, GUI3_DCG_pan_3
       	GUI3_lowerRightPanel=new JPanel(new BorderLayout());
       	GUI3_lowerRightPanel.add(GUI3_DCG_pan_1, BorderLayout.NORTH);	
       	GUI3_lowerRightPanel.add(GUI3_DCG_pan_2, BorderLayout.CENTER);
       	GUI3_lowerRightPanel.add(GUI3_DCG_pan_3, BorderLayout.SOUTH);	
       	
        GUI3_lowerRightPanel.setBorder(BorderFactory.createCompoundBorder(
                  		BorderFactory.createTitledBorder("Semantics DCG-based Slice"),
                   		BorderFactory.createEmptyBorder(5,5,5,5)));		

		//Add two panel to the Right Panel: upperRightPanel, lowerRightPanel
		GUI3_rightPanel=new JPanel(new BorderLayout()); 		
		GUI3_rightPanel.add(GUI3_upperRightPanel, BorderLayout.PAGE_START);
		GUI3_rightPanel.add(GUI3_lowerRightPanel, BorderLayout.PAGE_END);
		
		//Add left and right panel to the Container
		this.getContentPane().add(GUI3_leftPanel, BorderLayout.LINE_START);
		this.getContentPane().add(GUI3_rightPanel, BorderLayout.LINE_END);
		
		// Enable action listeners
		GUI3_upperPDGbutton.addActionListener(this);
		GUI3_upperCFGbutton.addActionListener(this);
		GUI3_upperSliceButton.addActionListener(this);
		GUI3_lowerDCGbutton.addActionListener(this);
		GUI3_lowerRefineButton.addActionListener(this);
		GUI3_lowerSliceButton.addActionListener(this);
		GUI3_lowerCancelButton.addActionListener(this);
	}	
	public void actionPerformed(ActionEvent e)
	{	
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		
		try
		{
			if(e.getSource().equals(GUI3_upperPDGbutton)) // Showing semantics-based PDG
			{
				RandomAccessFile raf = new RandomAccessFile(GUI3_SemanticExtractInfo, "r");      			
      			formMatrix matrixObject=new formMatrix(raf);
      			int[][] PDGmat=matrixObject.getPDGmatrix();
      			matrixObject.DrawMatix(PDGmat);
      			
				raf.close();
			}
			else if(e.getSource().equals(GUI3_upperCFGbutton)) // Showing semantics-based CFG
			{
				RandomAccessFile raf = new RandomAccessFile(GUI3_SemanticExtractInfo, "r");      			
      			formMatrix matrixObject=new formMatrix(raf);
      			int[][] CFGmat=matrixObject.getCFGmatrix();
      			matrixObject.DrawMatix(CFGmat);
      			
				raf.close();
			}
			else if(e.getSource().equals(GUI3_upperSliceButton)) // Performing semantics-based slicing
			{
				String[] input=(new OptionPaneMultiple()).getInput();
				String ProgPoint=input[0].trim();
				String Vars=input[1].trim();
				
				if(!ProgPoint.equals("") && !Vars.equals(""))
				{
					
					// Converting variable string in the criteria into list of program variables
      				LinkedList VarsList=new LinkedList();
      				String[] VarsArr=Vars.split(",");
      				for(int i=0; i<VarsArr.length;i++)
      				{
      					VarsList.addLast(VarsArr[i].trim());
      				}
      				
      				// Performign slicing, returns true if succeed
      				RandomAccessFile raf = new RandomAccessFile(GUI3_SemanticExtractInfo, "r");      			
      				formMatrix matrixObj=new formMatrix(raf);
      				boolean flag=matrixObj.performSlicing(ProgPoint, VarsList, GUI3_SemanticPreviewFile, File_Out_Slice_Preview, File_Out_Slice_ExtractInfo);
      				
      				if(flag) // Slicing is successful
      				{
      					SliceWindow slc=new SliceWindow(File_Out_Slice_Preview, nonRelList, nonDepList, ProgPoint, Vars, GUI3_AbsDom);
      					slc.setSize(350,350);
						slc.setLocation(600,30);
						slc.setVisible(true);
      				}
      				else // Slicing not successful, show error message
      				{
      					JOptionPane.showMessageDialog(this, "Error in the slicing criteria!");
      				}
      				raf.close();
				}
				else // Problem in the criteria
				{
					JOptionPane.showMessageDialog(this, "Without Slicing Criteria I can't Proceed.");
				}
			}
			else if(e.getSource().equals(GUI3_lowerDCGbutton)) // Converting into DCG
			{
				
				// Converting into SSA form
				RandomAccessFile raf = new RandomAccessFile(GUI3_SemanticExtractInfo, "r");				
      			genSSAform SSAobj=new genSSAform(raf);
    	  		SSAobj.genSSAprogram(GUI3_SemanticPreviewFile, File_Out_SSA_Preview, File_Out_SSA_ExtractInfo);
    	  		
    	  		// Computing DCG annotations
   				RandomAccessFile rafSSAExtractInfoFile = new RandomAccessFile(File_Out_SSA_ExtractInfo, "r");      	
   				genDCGannotations objDCG=new genDCGannotations(rafSSAExtractInfoFile);   				
   				objDCG.writeDCGannotations(File_Out_DCG_Preview); 
      			
      			// Calling GUI to display DCG information	
   				DCGWindow dcg=new DCGWindow(File_Out_SSA_Preview, File_Out_SSA_ExtractInfo, File_Out_DCG_Preview);
   				dcg.setSize(700,350);
				dcg.setLocation(300,50);
				dcg.setVisible(true);
								
			}
			else if(e.getSource().equals(GUI3_lowerRefineButton)) // Refinement based on DCG annotations
			{
				
				// Refine based on DCG annotations
	  			RandomAccessFile raf = new RandomAccessFile(GUI3_SemanticExtractInfo, "r");	  			
      			genDCGProgInfo obj=new genDCGProgInfo(GUI3_SemanticPreviewFile, raf, GUI3_AbsDom);      		
      			LinkedList msg=obj.refineDCG();
      			
      			// Displaying the  refinement information that happen
      			String str=new String("");
      			if(!msg.isEmpty()) 
      			{
      				ListIterator itr=msg.listIterator();
 	     			while(itr.hasNext())
    	  			{
      					str=str+"\n"+itr.next().toString();	
      				}
      			}
      			else // No refinement occurs
      			{
      				str=new String("No refinement possible!!!");
      			}
      			
      			JOptionPane.showMessageDialog(this, str);
      			
      			raf.close();
			}
			else if(e.getSource().equals(GUI3_lowerSliceButton)) // Slicing on semantics-based DCG
			{
				String ProgPoint=GUI3_tf_SliceProgPoint.getText();
				String Vars=GUI3_tf_SliceProgVar.getText();
				
				if(!ProgPoint.equals("") && !Vars.equals(""))
				{	
					// Converting variable string in the criteria into list of program variables				
					LinkedList VarsList=new LinkedList();
      				String[] VarsArr=Vars.split(",");
      				for(int i=0; i<VarsArr.length;i++)
      				{
      					VarsList.addLast(VarsArr[i].trim());
      				}
      				
      				// Performing slicing: returns true if succeed
	  				RandomAccessFile raf = new RandomAccessFile(GUI3_SemanticExtractInfo, "r");	  			
      				genDCGProgInfo obj=new genDCGProgInfo(GUI3_SemanticPreviewFile, raf, GUI3_AbsDom);      		
      				LinkedList msg=obj.refineDCG();     				
      				
      				boolean flag=obj.performSlicing(ProgPoint, VarsList, File_Out_Slice_Preview, File_Out_Slice_ExtractInfo);
      				
      				if(flag) // Slicing is successful
      				{
      					SliceWindow slc=new SliceWindow(File_Out_Slice_Preview, new LinkedList(), new LinkedList(), ProgPoint, Vars, GUI3_AbsDom);
      					slc.setSize(350,350);
						slc.setLocation(600,30);
						slc.setVisible(true);
      				}
      				else // Error in slicing
      				{
      					JOptionPane.showMessageDialog(this, "Error in the slicing criteria!");
      				}		
				}
				else // Error in slicing criterion
				{
					JOptionPane.showMessageDialog(this, "Without Slicing Criteria I can't Proceed.");
				}
			}
			else if(e.getSource().equals(GUI3_lowerCancelButton)) // Cancelling the window
			{
				this.setVisible(false);
			}
		}
		catch(Exception err) // Error found
		{
			System.err.println("Tukra.startGUI3.ActionListener(): Error Occurred..... " + err);
		}
	}		
}	


// GUI to display slice preview
class SliceWindow extends JFrame  implements ActionListener
{
	
	String SlicePreviewFile; 
	
	JPanel lowerPannel, upperPannel;
	JButton CancelButton;
	
	JTextPane upperPannel_TextPane;
	JScrollPane upperPannel_editorScrollPane;
	
	
	SliceWindow(String SlicePreviewFile, LinkedList nonRelList, LinkedList nonDepList, String ProgPoint, String ProgVar, String AbsDom)
	{
		super("Slice");
		
		// Stores the path of the preview file containing slice 
		this.SlicePreviewFile=SlicePreviewFile;
			
		// upperPanel to provide preview Area	
		upperPannel=new JPanel(new GridBagLayout()); 
		
		if(nonRelList.size()==0 && nonDepList.size()==0)
			upperPannel_TextPane = new createTextPane(SlicePreviewFile).getTextPane();
		else
			upperPannel_TextPane = new createTextPane(SlicePreviewFile, nonRelList, nonDepList).getTextPane();
			    			      			
        upperPannel_TextPane.setEditable(false);
       	upperPannel_editorScrollPane = new JScrollPane(upperPannel_TextPane);  
       	upperPannel_editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);     	
        upperPannel_editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        upperPannel_editorScrollPane.setPreferredSize(new Dimension(300, 230));
        upperPannel_editorScrollPane.setMinimumSize(new Dimension(300,230));
        		
        upperPannel.add(upperPannel_editorScrollPane); 
        
        // Displaying Tile : includes Slicing Criteria		
       	if(AbsDom.equals(""))
       	{
        	upperPannel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Slice w.r.t. < "+ProgPoint+", {"+ProgVar+"} >"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
      	}
      	else
      	{
         	upperPannel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Slice w.r.t. < "+ProgPoint+", {"+ProgVar+"}, "+AbsDom+" >"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
      	}
                
        // lowerPanel to add a "Done" button        
                
        lowerPannel=new JPanel(new GridBagLayout());        
        CancelButton=new JButton("Done");
        
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.anchor = GridBagConstraints.CENTER;
        //constraint.fill = GridBagConstraints.BOTH;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.REMAINDER;
        constraint.gridx = GridBagConstraints.CENTER;
        constraint.gridy = GridBagConstraints.RELATIVE;   
        constraint.insets = new Insets(10,0,10,0);
         	
        lowerPannel.add(CancelButton, constraint);
        
         // Add two Pannels to the container: epperPanel, lowerPanel	
        this.getContentPane().add(upperPannel, BorderLayout.PAGE_START);
		this.getContentPane().add(lowerPannel, BorderLayout.PAGE_END);
		
		// Add actionlistener
		CancelButton.addActionListener(this);
                        
    }	
	public void actionPerformed(ActionEvent e)
	{
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		try{
			if(e.getSource().equals(CancelButton)) // Cancelling the window
			{
				//System.exit(0);
				this.setVisible(false);
			}
		}catch(Exception err){
			System.err.println("Tukra.sliceWindow.ActionListener(): Error Occurred....." + err);}
	}
}


// GUI to display SSA progrem preview and DCG annotations
class DCGWindow extends JFrame  implements ActionListener
{
	
	String SSAPreviewFile;
	String SSAExtractInfo;
	String DCGPreviewFile;
	
	JPanel lowerPannel, upperLeftPannel, upperRightPannel;
	JButton PDGbutton, CFGbutton, Cancelbutton;
	
	JTextPane upperLeftPannel_TextPane;
	JScrollPane upperLeftPannel_editorScrollPane;
	
	JTextPane upperRightPannel_TextPane;
	JScrollPane upperRightPannel_editorScrollPane;
	
	
	DCGWindow(String SSAPreviewFile, String SSAExtractInfo, String DCGPreviewFile)
	{
		super("SSA form with Annotations");
		
		this.SSAPreviewFile=SSAPreviewFile; // Path to preview file of SSA program
		this.SSAExtractInfo=SSAExtractInfo; // Path to details information file of SSA program
		this.DCGPreviewFile=DCGPreviewFile; // Path to the file containing DCG annotations of SSA program
			
		// upperLeftPanel to provide preview Area of SSA program	
		upperLeftPannel=new JPanel(new GridBagLayout()); 
		upperLeftPannel_TextPane = new createTextPane(SSAPreviewFile).getTextPane();
        upperLeftPannel_TextPane.setEditable(false);
       	upperLeftPannel_editorScrollPane = new JScrollPane(upperLeftPannel_TextPane);  
       	upperLeftPannel_editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);     	
        upperLeftPannel_editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        upperLeftPannel_editorScrollPane.setPreferredSize(new Dimension(300, 230));
        upperLeftPannel_editorScrollPane.setMinimumSize(new Dimension(300,230));
        		
        upperLeftPannel.add(upperLeftPannel_editorScrollPane); 
        		
        upperLeftPannel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("SSA Form"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
        
        // upperRightPanel to provide preview Area of DCG annotations
		upperRightPannel=new JPanel(new GridBagLayout()); 
		upperRightPannel_TextPane = new createTextPane(DCGPreviewFile).getTextPane();
        upperRightPannel_TextPane.setEditable(false);
       	upperRightPannel_editorScrollPane = new JScrollPane(upperRightPannel_TextPane);  
       	upperRightPannel_editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);     	
        upperRightPannel_editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        upperRightPannel_editorScrollPane.setPreferredSize(new Dimension(300, 230));
        upperRightPannel_editorScrollPane.setMinimumSize(new Dimension(300,230));
        		
        upperRightPannel.add(upperRightPannel_editorScrollPane); 
        		
        upperRightPannel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("DCG Annotations"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
                                
        // Set lower panel to add a buttons for "PDG"/"CFG"/"Done"	       
                
        lowerPannel=new JPanel(new GridBagLayout());        
        PDGbutton=new JButton("Show PDG");
        CFGbutton=new JButton("Show CFG");
        Cancelbutton=new JButton("Done");
        
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.anchor = GridBagConstraints.CENTER;
        //constraint.fill = GridBagConstraints.BOTH;
        //constraint.gridwidth = GridBagConstraints.RELATIVE;
        //constraint.gridheight = GridBagConstraints.REMAINDER;
        constraint.gridx = GridBagConstraints.RELATIVE;
        constraint.gridy = 0;   
        constraint.insets = new Insets(5,25,5,0);
         	
        lowerPannel.add(PDGbutton, constraint);
        lowerPannel.add(CFGbutton, constraint);
        lowerPannel.add(Cancelbutton, constraint);
        
        
         // Add all Pannels to the container	
        this.getContentPane().add(upperLeftPannel, BorderLayout.LINE_START);
        this.getContentPane().add(upperRightPannel, BorderLayout.LINE_END);
		this.getContentPane().add(lowerPannel, BorderLayout.PAGE_END);
		
		// Enable action listeners
        PDGbutton.addActionListener(this);
        CFGbutton.addActionListener(this); 
        Cancelbutton.addActionListener(this);               
    }	
	public void actionPerformed(ActionEvent e)
	{
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE); // To show PDG of SSA program
		try{
			
			if(e.getSource().equals(PDGbutton))
			{
				RandomAccessFile raf = new RandomAccessFile(SSAExtractInfo, "r");      			
      			formMatrix matrixObject=new formMatrix(raf);
      			int[][] PDGmat=matrixObject.getPDGmatrix();
      			matrixObject.DrawMatix(PDGmat);
      			
				raf.close();
			}
			else if(e.getSource().equals(CFGbutton)) // To show CFG of SSA program
			{
				RandomAccessFile raf = new RandomAccessFile(SSAExtractInfo, "r");      			
      			formMatrix matrixObject=new formMatrix(raf);
      			int[][] CFGmat=matrixObject.getCFGmatrix();
      			matrixObject.DrawMatix(CFGmat);
      			
				raf.close();
			}
			else if(e.getSource().equals(Cancelbutton)) // Cancelling the window
			{
				//System.exit(0);
				this.setVisible(false);
			}
			
		}catch(Exception err){
			System.err.println("Tukra.sliceWindow.ActionListener(): Error Occurred....." + err);}
	}
}


// To browse input program file from your computer
class Chooser extends JFrame
{
	JFileChooser chooser;
	String fileName;

	public Chooser()
	{
		chooser = new JFileChooser();
		
		try{
		File f = new File(new File(".").getCanonicalPath());
    	chooser.setCurrentDirectory(f);
    	}catch(IOException err){System.err.println("Tukra.Chooser(). Error Occurred....." + err);}
    
		int r = chooser.showOpenDialog(new JFrame());
		if (r == JFileChooser.APPROVE_OPTION)
		{
			fileName = chooser.getSelectedFile().getPath();
		}
	}
}


// Class to display preview of code using different formats and colors
class createTextPane extends myFunctions implements  myPatterns
{
	JTextPane textPane;
	
	// Display preview in "regular format"
    createTextPane(String File_Out_Preview)
    {    	
    	String newline = "\n";
		
        textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        addStylesToDocument(doc);
		
		try{
			
			DataInputStream dis_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(File_Out_Preview))));	  		
			while(dis_Preview.available()!=0)
			{
				String stmt=dis_Preview.readLine(); // Scanning statements in the preview file
			
				doc.insertString(doc.getLength(), stmt+newline, doc.getStyle("regular"));
			}
        }catch (FileNotFoundException e){System.err.println("Tukra.createTextPane(): Input File not found." + e);
 	   	}catch (IOException e){System.err.println("Tukra.createTextPane(): Problem with IO exception." + e);
 	   	}catch (BadLocationException ble) {System.err.println("Tukra.createTextPane(): Couldn't insert initial text into text pane.");}
 	}
 	
 	// Display irrelevant statement and variables in red color for the preview
 	createTextPane(String Preview, LinkedList nonRelList, LinkedList nonDepList)
    {    	
    	
    	String newline = "\n";
		
        textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        addStylesToDocument(doc);
		
		try{
			
			DataInputStream dis_Preview = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(Preview))));	  		
			while(dis_Preview.available()!=0)
			{
				String stmt=dis_Preview.readLine().trim(); // Scanning statements in preview file
				
				if(stmt.length()!=0)
				{
					String[] spl_stmt=stmt.split(":");
					String cmd=spl_stmt[1].trim(); // Extracting commands
					String label=spl_stmt[0].trim(); // Extracting labels
					
					if(cmd.equals("End-If") || cmd.equals("End-Else") || cmd.equals("End-While") || cmd.equals("End-Start") || cmd.equals("}Else{") )
					{
						// End of control command
						doc.insertString(doc.getLength(), stmt+newline, doc.getStyle("regular"));
					}
					else if(! nonRelList.isEmpty() && isExist(label, nonRelList))
					{
						// Statement is irrelevant
						doc.insertString(doc.getLength(), stmt+newline, doc.getStyle("red"));
					}
					else if(!nonDepList.isEmpty())
					{
						// Variables in statement might be irrelevant: 
						// Extract irrelevant variables associated with the statement
						String nonVarsString="";
						ListIterator itr=nonDepList.listIterator();
						while(itr.hasNext())
						{
							String item=(String)itr.next();
							String[] field=item.split("_");	
						
							String extracted_label=field[0].trim();
						
							if(label.equals(extracted_label))
							{
								nonVarsString=field[1].trim();
								break;
							}
						}
						
						
						if(nonVarsString.equals("*"))
						{
							// No variable in the statement is irrelevant
							doc.insertString(doc.getLength(), stmt+newline ,doc.getStyle("regular"));
						}
						else
						{
							// Some variables in the statement are irrelevant			
							Matcher match_if=patrn_if.matcher(cmd);
							boolean found_if=match_if.find();
		
							Matcher match_while=patrn_while.matcher(cmd);
							boolean found_while=match_while.find();	
					
							Matcher match_assign=patrn_assign.matcher(cmd);
							boolean found_assign=match_assign.find();	
				
							if(found_if || found_while || found_assign)
							{
								// Splitting the statement into multiple substrings: separting irrelevant variables
								LinkedList SplitString=new LinkedList();
								
								if(found_assign) 
								{
									// Splitting into a part till "=" for the assignment statement
									String[] stmt_spl=stmt.split("=");
									
									SplitString.addLast(stmt_spl[0]+"=");
									stmt=stmt_spl[1];
								}
									
								Pattern patrn_var=Pattern.compile(nonVarsString);									
																
								while(stmt.length()!=0)
								{
									Matcher match_var=patrn_var.matcher(stmt);
									if(match_var.find()) // Irrelevant variable found in the statement
									{
										int start_index=match_var.start();
										String var=match_var.group();
										int end_index=match_var.end();
										
										if(start_index>0)
										{			
											// Splitting into a part till the starting index of the irrelevant variable that found							
											String extract_str=stmt.substring(0, start_index);
											SplitString.addLast(extract_str);
										}
										SplitString.addLast(var); // Consider the irrelevant variable that is found, as a part
										
										stmt=stmt.substring(end_index).trim();
									}
									else
									{
										// No irrelevant variable found: consider whole statement as a part
										SplitString.addLast(stmt);
										stmt="";
										break;
									}
								}	
								
								String[] extracted_vars=nonVarsString.split("\\|");
									
								// Extracting last splitting part	
     							String lastString=(String)SplitString.removeLast();
     							
     							// For all part except the last one
     							ListIterator itr1=SplitString.listIterator();
     							while(itr1.hasNext())
     							{
     								
     								String str=itr1.next().toString();
     								
     								if( isExist(str, extracted_vars))
     								{
     									// If the part is irrelevant variable
     									doc.insertString(doc.getLength(), str, doc.getStyle("red"));
     								}
     								else
     								{
     									// If the part is not irrelevant variable
     									doc.insertString(doc.getLength(), str, doc.getStyle("regular"));
     								}	
     							}
     							
     							if( isExist(lastString, extracted_vars))
     							{
     								// If the last part is irrelevant variable
     								doc.insertString(doc.getLength(), lastString, doc.getStyle("red"));
     							}
     							else
     							{
     								// If the last part is not irrelevant variable
     								doc.insertString(doc.getLength(), lastString+newline, doc.getStyle("regular"));
     							}
															
							}
							else
							{
								// Statement is not "assignment"/"if"/"while", although irrelevant variables exist
								doc.insertString(doc.getLength(), stmt+newline ,doc.getStyle("regular"));
							}	
						}
					}
					else // No statement is irrelevant and no variable is irrelevant
					{
						doc.insertString(doc.getLength(), stmt+newline ,doc.getStyle("regular"));	
					}
				}
			
			}
        }catch (FileNotFoundException e){System.err.println("Tukra.createTextPane(): Input File not found." + e);
 	   	}catch (IOException e){System.err.println("Tukra.createTextPane(): Problem with IO exception." + e);
 	   	}catch (BadLocationException ble) {System.err.println("Tukra.createTextPane(): Couldn't insert initial text into text pane.");}
 	}
    
    protected void addStylesToDocument(StyledDocument doc) 
    {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style bold = doc.addStyle("bold", regular);
        StyleConstants.setBold(bold, true);
        
        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);
        
        s= doc.addStyle("red", bold);
        StyleConstants.setForeground(s, Color.red);

    }
 	
 	// Rturning the TextPane
 	JTextPane getTextPane()
    {
    	return 	textPane;
    }
}


// GUI to accept slicing criteria
class OptionPaneMultiple extends JFrame
{
	String[] input;
	
	OptionPaneMultiple()
	{
		input=new String[2];
		for(int i=0;i<input.length;i++)
		{
			input[i]="";	
		}
		
		final JTextField ProgPoint = new JTextField();		
		final JTextField ProgVar = new JTextField();
		Object[] msg = {"Program Point:", ProgPoint, "Variables (Seperated by comma (,)):", ProgVar};
 		String[] options = {"OK", "Cancel"};
		
		int result = JOptionPane.showOptionDialog(
		 	null, 
		 	msg, 
		 	"Enter Input", 
		 	JOptionPane.OK_CANCEL_OPTION, 
		 	JOptionPane.QUESTION_MESSAGE, 
		 	null, 
		 	options, 
		 	null);
 
		try
		{
				
			if(result == JOptionPane.OK_OPTION)
			{
				
				String in_1=ProgPoint.getText();
				String in_2=ProgVar.getText();
			
				while(in_1.equals("") || in_2.equals(""))
				{
					JOptionPane.showMessageDialog(this,"Please Enter Input!");
 
					result = JOptionPane.showOptionDialog(
					 	null, 
					 	msg, 
					 	"Enter Input", 
					 	JOptionPane.OK_CANCEL_OPTION, 
					 	JOptionPane.QUESTION_MESSAGE, 
					 	null, 
					 	options, 
					 	null);
		 	
					if(result == JOptionPane.OK_OPTION)
					{
						in_1=ProgPoint.getText();
						in_2=ProgVar.getText();
					}
					else
					{
						//System.out.println("Canceled");
						break;
					}
				}
				
				input[0]=in_1;
				input[1]=in_2;
			}
			else
			{
				//System.out.println("Canceled");
			}
			
			this.setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		    
		}
		catch(Exception uninitializedValue)
		{}
	}
	
	String[] getInput()
	{
		return input;	
	}
}


// Main function for Tukra
public class Tukra
{	
	public static void main(String args[])
	{
		
		SwingUtilities.invokeLater(new Runnable() {
            public void run() 
            {           	
            	
				JFrame f=new JFrame("Tukra: the slicing tool"); 
				
				f.setSize(850,550);
				f.setLocation(250,100);
				f.setVisible(true);
				//f.pack();
				//f.validate();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				    	
				Container container=f.getContentPane();
				new startGUI1(container);				
				
				System.out.println("Main(): The End.");
            }
        });
	}
}

