# Tukra: An Abstract Program Slicing Tool

TUKRA  allows the practical evaluation of abstract program slicing algorithms. It exploits the notions of statement relevancy, semantic data dependences and conditional dependences. The combination of these three notions allows TUKRA to refine traditinal syntax-based program dependence graphs, generating more accurate slices. 

Given a program and an abstract program slicing criterion from end-user as input, TUKRA is able to perform both syntax- and semantics-based intraprocedural slicing of the program w.r.t. the slicing criterion.



The main features of TUKRA are:

* It allows to perform syntax-based as well as semantics-based slicing of a program.
* Users are free to perform single or any combination of semantic computaions: statement relevancy, semantic data dependences, or conditional dependences.
* Easy to integrate new abstract domains.
* Easy to use by naive users due to its friendly graphical user interfaces.
* It allows the practical evaluation of abstract program slicing algorithms.


# Tool Usage

* Graphical Interface I (accepting inputs from users): This is the starting interface window accepting the input program shown below: 

![alt text](https://github.com/RajuHalder/Tukra/blob/master/Images/gui1_input.png)

With this interface, we can browse the input program file, or we can write the source code in the text area provided on the right side of the screen, or we can open nodepad software by clicking on "Open NotePad" button to write and save the program code if it does not exist. On clicking the "Next" button we can go to the next window.

* Graphical Interface II (syntactic slicing and semantic computations): With this interface, users can perform the followings: (i) generating CDG and PDG of the input programs, (ii) performing syntax-based slicing, (iii) choosing the options to perform the type of abstract semantic computations on the input programs. 

![alt text](https://github.com/RajuHalder/Tukra/blob/master/Images/gui2_syntax.jpg) 

Observe that when the button "Show PDG" is clicked, the PDG of the program is displayed in a window. Similarly, the "Show CDG" button is used to display the CDG of the input program. When users click on "Slice" button, it asks for slicing criterion: a program point and a list of variables (separated by comma) used/defined at that program point. It then computes and shows the syntactic slice of the input program w.r.t. the given criterion. On the right side of the screen, the tool displays the options for two types of abstract computation: abstract semantic relevancy of statements and abstract semantic data dependence computation, as check-boxes. Users can choose any one or both of them. However, in doing so, users must choose an abstract domain of interest shown as radio buttons (we provided here only two: SIGN and PAR domain, but we can easily add more). The "Go" button moves the tool to the next window.

* Graphical Interface III (semantics-based abstract program slicing): This is a child window displaying over the main window shown below: 

![alt text](https://github.com/RajuHalder/Tukra/blob/master/Images/gui3_semantic.jpg) 

Users can see in the preview area of this interface the refined program where irrelevant statements and/or irrelevant variables in the expressions are disregarded (marked in red color), depending on the options they choose on the previous interface. This interface provides two options: (i) generate CDG or PDG of this refined program and perform slicing on it (upper right part of the screen), and (ii) generate its DCG, refine it into more precise one by computing unrealizable paths based on the satisfiability of DCG annotations against their trace semantics and perform slicing on it (lower right part of the screen). The button "Show DCG" displays the SSA form of the program and the DCG annotations over all the edges of the dependence graph of the program in SSA form as shown in the following figure: 

![alt text](https://github.com/RajuHalder/Tukra/blob/master/Images/gui4_dcg.jpg) 

The "Refinement" button shows a message with a list of refinement performed based on the DCG annotations. The slicing based on this refined semantics-based abstract DCG can be performed by supplying slicing criterion in the text areas provided on the lower right area of the screen as depicted below: 

![alt text](https://github.com/RajuHalder/Tukra/blob/master/Images/gui5_slice.jpg)  

# How To Run

Below is the information that assists users to run the tool TUKRA. 

* System requirements (Recommended):
  ----------------------------------
 Processor: Pentium or higher
 Operating system: Microsoft Windows XP or later
 Memory: 1 GB RAM or higher
 The Platform: Java 2 Platform, Standard Edition (J2SE)

* How to run Tukra:
  --------------------
 Step 1: Download the source code (click here to download) and put in a directory “Tool”.
 Step 2: Open the command prompt and change the current directory to “Tool”.
 Step 3: Compile the source code “Tukra.java” by issuing the command “javac Tukra.java”
 Step 4: Run the class file by issuing the command “java Tukra”. 

# Limitations on input programs:
At this preliminary stage of implementation, we do not focus on any specific programming language for input programs. We consider Imperative Programming Languages with the following assumptions on the syntax: 
- All control blocks should be enclosed with "{ }" irrespective of the number of statements in it.
- Empty control block must have “skip;” statement in it.
- Run-time input for any statement is denoted by “?”, e.g. the statement asking input from user for variable “var” is written as “var =?;”. 
- The syntax of the statement displaying variables’ values is “print(x, y, z);” where x, y, z are the program variables.

> Example: 
The following example program can also be found in the folder “Input".

i = -2;
x = ?;
y = ?;
w = ?;
if(x>=0){
x=x+w;
y = 4*w*0;}
while(i<=0){
y = 2*y;
i = i + 1;}
print(x,y);



* Types of Semantic Computations:
  -------------------------------------
We consider the following three semantic computations: 
- The computation of semantic relevancy
- The computation of semantic data dependences
- The computation of conditional dependences



* Abstract Domains:
  ---------------------
Currently, TUKRA provides two abstract domains: SIGN and PAR. The abstract domain SIGN represents the sign property, whereas PAR represents parity property of the variables of interest. 

# Contributors
Developed By: Raju Halder
Release Date: July 2012
# References
* TUKRA: An Abstract Program Slicing Tool. Author's Names. In Proceedings of the 7th International Conference on Software Paradigm Trends (ICSOFT '12), Pages 178-183, Rome, Italy, 24-27 July 2012. SciTePress. 

* Dependence Condition Graph for Semantics-based Abstract Program Slicing. Agostino Cortesi, Raju Halder. In Proceedings of the 10th International Workshop on Language Descriptions Tools and Applications (LDTA '10), satellite event of European Joint Conferences on Theory and Practice of Software (ETAPS '10), Paphos, Cyprus, 27-28 March 2010. ACM Press.

* Data dependencies and program slicing: from syntax to abstract semantics. Isabella Mastroeni, Damiano Zanardini. In Proceedings of the ACM SIGPLAN symposium on Partial evaluation and semantics-based program manipulation (PEPM '08), pages 125–134, San Francisco, California, USA. ACM Press.

* The dependence condition graph: Precise conditions for dependence between program points. S. Sukumarana, A. Sreenivasb, R. Metta. Computer Languages, Systems & Structures, 36(1):96–121, 2010.
