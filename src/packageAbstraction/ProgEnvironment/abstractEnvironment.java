/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * This CLASS defines the abstract environment that maps program variables to the abstract values
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/



package packageAbstraction.ProgEnvironment;

import java.util.*;
import packageMyLibrary.*;

import packageAbstraction.ComponentInterface.abstractComponent.*;
import packageAbstraction.DomainInterface.abstractDomain;

// class "abstractEnvironment" that maps program variables to the abstract values
public class abstractEnvironment extends myFunctions
{
	String[] vars; // Program Variables
	abstractValue[] env; // Abstract Values
	
	// Constructor for initial Abstract Environment where all abstract values are bottom element of the corresponding abstract domain
	public abstractEnvironment(String[] ProgVars, abstractDomain ADobj)
	{
		vars=new String[ProgVars.length];
		for(int i=0; i<ProgVars.length; i++)
		{
			vars[i]=ProgVars[i]; // Copying program variables from parameter
		}
		
		env=new abstractValue[ProgVars.length];
		for(int i=0;i<env.length;i++)
		{
			env[i]=ADobj.getBottom(); // Mapping to bottom element 	
		}	
	}
	
	// Constructor for Abstract Environment
	public abstractEnvironment(String[] ProgVars, abstractValue[] val)
	{
		if(ProgVars.length==val.length)
		{
			vars=new String[ProgVars.length];
			for(int i=0; i<ProgVars.length; i++)
			{
				vars[i]=ProgVars[i]; // Copying program variables from parameter
			}
		
			env=new abstractValue[val.length];
			for(int i=0;i<val.length;i++)
			{
				env[i]=val[i];	// Copying abstract values from parameter
			}
		}
		else
		{
			System.out.println("abstractEnvironment:abstractEnvironment(String[], AbatractValue[]): less or more Abstract Values in parameter.");
		}
	}
	
	// Copy Constructor
	public abstractEnvironment(abstractEnvironment x)
	{
		String[] ProgVars=x.getVariables(); // Extracting program variables from "x"
		vars=new String[ProgVars.length];   
		
		for(int i=0;i<ProgVars.length;i++)
		{
			vars[i]=ProgVars[i]; // Copying extracted program variables	
		}
		
		abstractValue[] val=x.getEnvironment(); // Extracting abstract values from "x"
		env=new abstractValue[val.length];
		
		for(int i=0;i<val.length;i++)
		{
			env[i]=val[i];	// Copying extracted abstract values
		}
	}
	
	// Setting abstract environment from information provided in the parameter
	public void setEnvironment(String[] ProgVars, abstractValue[] val)
	{
		if(ProgVars.length==val.length)
		{
			vars=new String[ProgVars.length];
			for(int i=0; i<ProgVars.length; i++)
			{
				vars[i]=ProgVars[i]; // Copying program variables from parameter
			}
		
			env=new abstractValue[val.length];
			for(int i=0;i<val.length;i++)
			{
				env[i]=val[i];		// Copying abstract values from parameter
			}
		}
		else
		{
			System.out.println("abstractEnvironment:setEnvironment(String[], AbatractValue[]): less or more Abstract Values in parameter.");			
		}
	}
	
	// Setting new abstract values to the program variables
	public void setEnvironment(abstractValue[] val)
	{
		if(val.length==vars.length)
		{
			env=new abstractValue[val.length];
			for(int i=0;i<val.length;i++)
			{
				env[i]=val[i];	
			}
		}
		else
		{
			System.out.println("abstractEnvironment:setEnvironment(AbatractValue[]): less or more Abstract Values in parameter.");
		}
	}
	
	// Get the array of program variables
	public String[] getVariables()
	{
		String[] ProgVars=new String[vars.length];
		for(int i=0;i<vars.length;i++)
		{
			ProgVars[i]=vars[i];	
		}
		return ProgVars;	
	}
	
	// Get the array of abstract values associated with the program variables
	public abstractValue[] getEnvironment()
	{
		abstractValue[] val=new abstractValue[env.length];
		for(int i=0;i<env.length;i++)
		{
			val[i]=env[i];	
		}
		return val;	
	}
	
	// To change the abstract value of a given program variable, and get modified abstract environment 
	public abstractEnvironment getModifiedEnvironment(String Variable, abstractValue newVal)	
	{
		String[] ProgVars=this.getVariables();
		abstractValue[] val=this.getEnvironment();
		
		int index=super.getIndex(Variable, ProgVars);
		
		if(index!=-1)
		{
			val[index]=newVal;
			return (new abstractEnvironment(ProgVars, val));
		}
		else
		{	
			System.out.println("abstractEnvironment:getModifiedEnvironment():The given variable "+ Variable +" does not belong to environment's variables!!!!");
			return this;
		}	
	}
	
	// To get the abstract value associated with a program variable
	public abstractValue getVariableValue(String Variable)
	{
		String[] ProgVars=this.getVariables();
		abstractValue[] val=this.getEnvironment();
		
		int index=super.getIndex(Variable, ProgVars);
		
		if(index!=-1)
		{
			return val[index];
		}
		else
		{
			System.out.println("abstractEnvironment:getVariableValue():The given variable "+ Variable +" does not belong to environment's variables!!!!");
			return null;
		}
	}
	
	// To narrow down the environment to the given subset of program variables
	public abstractEnvironment getRestrictedEnvironment(String[] ProgVars)
	{
		if(ProgVars.length!=0)
		{
			abstractValue[] val=new abstractValue[ProgVars.length];		
			for(int i=0;i<ProgVars.length;i++)
			{
				val[i]=getVariableValue(ProgVars[i]);
			}
			return (new abstractEnvironment(ProgVars, val));
		}
		else
		{
			return this;
		}
	}	
	
	// Comparing two abstract environments
	public boolean equals(abstractEnvironment obj)
	{
		boolean flag=true;
		
		String[] ProgVars=obj.getVariables();
		abstractValue[] val=obj.getEnvironment();
		
		if(ProgVars.length==vars.length && val.length==env.length)
		{		
			for(int i=0;i<ProgVars.length;i++)
			{
				if(! ProgVars[i].equals(vars[i]))
				{
					flag=false;break;
				}
			}	
		
			for(int i=0;i<val.length;i++)
			{
				if(! val[i].equals(env[i]))
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
	
	// To display the abstract environment
	public void display()
	{
		int i=0;
		for(;i<vars.length-1;i++)
		{
			System.out.print(vars[i]+"=");
			env[i].display();
			System.out.print(",");	
		}
		System.out.print(vars[i]+"=");
		env[i].display();
	}
	
	// Computing LUB of two abstract environments componentwise
	public abstractEnvironment getLUB(abstractEnvironment obj, abstractDomain ADobj)
	{
		abstractValue[] result=new abstractValue[env.length];		
		
		String[] ProgVars=obj.getVariables();
		abstractValue[] val=obj.getEnvironment();
		
		if(env.length==val.length)
		{			
			for(int i=0;i<env.length;i++)
			{
				abstractValue[] arr={env[i], val[i]};
				result[i]=ADobj.getLUB(arr); // Computing LUB of a array of abstract values in the abstract domain
			}
			return (new abstractEnvironment(ProgVars, result));
		}
		else
		{
			System.out.println("abstractEnvironment:getLUB(): Mismatch in length of two environments");
			return null;
		}		
	}
	
	// Does the abstract values in an environment refer to atomic abstract values
	public boolean isAtomicEnvironment()
	{
		for(int i=0;i<env.length;i++)
		{
			if(! env[i].isAtomicValue()) // Is the abstract value atomic?
			{
				return false;	
			}	
		}
		return true;	
	}
	
	// Returning a list of atomic abstract environments, called atomic-covering
	public LinkedList getAtomicCovering(abstractDomain ADobj)
	{	
				
		String[] ProgVars=this.getVariables();
		
		/* i-th row in "arr" contains an array of atomic values obtained by  
		 * partitioning the i-th abstract value of the environment into atoms */
		abstractValue[][] arr=new abstractValue[env.length][]; 
		for(int i=0; i<env.length; i++)
		{
			arr[i]=ADobj.getAtoms(env[i]);	
		}
		
		/* Converting into two dimensional array of linkedlist, 
		 * where each linkedlist in (i,j)-th cell contains atomic abstract value in the (i,j)-th cell of "arr"*/
		LinkedList[][] list=convert2List(arr); 
		
		/* Computing productes of all rows in "list" 
		 * Here, the product of any two rows results into a single row that contains all possible combination
		 * Therefore, the final result contains a single row of linkedlist 
		 * Each Linkedlist element in the row contains all possible combination of atomic values, representing an abstract atomic environment*/	
		LinkedList[] prodResult= super.CartesianProduct(list); 
		
		// Each Linkedlist element in the array represents an atomic environment
		LinkedList envList=new LinkedList(); 
		for(int i=0; i<prodResult.length; i++)
		{
			Object[] temp=prodResult[i].toArray();
			abstractValue[] AEnv=new abstractValue[temp.length];
			for(int j=0; j<temp.length;j++)
			{
				AEnv[j]=(abstractValue)temp[j];
			}
			envList.addLast(new abstractEnvironment(ProgVars, AEnv)); // Adding resultant atomic abstract environemt to the list	
		}
		
		System.out.println("abstractEnvironment:getAtomicCovering(): The End.");
		return envList; // Returning the list of atomic environments
	}
	
	// Generating atomic-covering of the abstract environment, disregarding the subset of varibales "X_Vars" 
	public LinkedList getXAtomicCovering(String[] X_Vars, abstractDomain ADobj)
	{
			
		String[] ProgVars=this.getVariables();
		
		/* i-th row in "arr" contains an array of atomic values obtained by  
		 * partitioning the i-th abstract value of the environment into atoms */
		abstractValue[][] arr=new abstractValue[env.length][];
		
		for(int i=0; i<env.length; i++)
		{
			/* Partition abstract values of the environment into atoms, but
			 * only for those that correspond to the variables not belong to "X_Vars"*/
			if(super.isExist(ProgVars[i], X_Vars)) 
			{
				arr[i]=new abstractValue[1];
				arr[i][0]=env[i];
			}
			else
			{
				arr[i]=ADobj.getAtoms(env[i]);
			}	
		}
		
		/* Converting into two dimensional array of linkedlist, 
		 * where each linkedlist in (i,j)-th cell contains atomic abstract value in the (i,j)-th cell of "arr"*/
		LinkedList[][] list=convert2List(arr);
		
		/* Computing productes of all rows in "list" 
		 * Here, the product of any two rows results into a single row that contains all possible combination
		 * Therefore, the final result contains a single row of linkedlist 
		 * Each Linkedlist element in the row contains all possible combination of atomic values, representing an abstract atomic environment*/	
		LinkedList[] prodResult= super.CartesianProduct(list);
		
		// Each Linkedlist element in the array represents an atomic environment
		LinkedList envList=new LinkedList(); 
		for(int i=0; i<prodResult.length; i++)
		{
			Object[] temp=prodResult[i].toArray();
			abstractValue[] AEnv=new abstractValue[temp.length];
			for(int j=0; j<temp.length;j++)
			{
				AEnv[j]=(abstractValue)temp[j];
			}
			envList.addLast(new abstractEnvironment(ProgVars, AEnv));	
		}
		
		System.out.println("abstractEnvironment:getXAtomicCovering(): The End.");
		return envList; // Returning the list of atomic environments disregarding "X_Vars"
		
	}
	
	// Generating sub-covering that contains only sub-abstract values for the variables not belong to subset of variables "X_Vars"
	public LinkedList getXSubCovering(String[] X_Vars, abstractDomain ADobj)
	{
			
		String[] ProgVars=this.getVariables();
		
		abstractValue[][] arr=new abstractValue[env.length][];
		
		for(int i=0; i<env.length; i++)
		{
			/* Partition abstract values of the environment into the the sub-values just one level below in the abstract attice, but
			 * only for those that corresponds to the variables not belong to "X_Vars"*/
			if(super.isExist(ProgVars[i], X_Vars))
			{
				arr[i]=new abstractValue[1];
				arr[i][0]=env[i];
			}
			else
			{
				arr[i]=ADobj.getSubValues(env[i]);
			}	
		}
		
		/* Converting into two dimensional array of linkedlist, 
		 * where each linkedlist in (i,j)-th cell contains sub-abstract value in the (i,j)-th cell of "arr"*/
		LinkedList[][] list=convert2List(arr);
		
		/* Computing productes of all rows in "list" 
		 * Here, the product of any two rows results into a single row that contains all possible combination
		 * Therefore, the final result contains a single row of linkedlist 
		 * Each Linkedlist element in the row contains all possible combination of sub-abstract values, representing an sub-abstract environment*/
		LinkedList[] prodResult= super.CartesianProduct(list);
		
		// Each Linkedlist element in the array represents a sub covering, disregarding "X_Vars"
		LinkedList envList=new LinkedList(); 
		for(int i=0; i<prodResult.length; i++)
		{
			Object[] temp=prodResult[i].toArray();
			abstractValue[] Env=new abstractValue[temp.length];
			for(int j=0; j<temp.length;j++)
			{
				Env[j]=(abstractValue)temp[j];
			}
			envList.addLast(new abstractEnvironment(ProgVars, Env));	
		}
		
		System.out.println("abstractEnvironment:getXSubCovering(): The End.");
		return envList; // Returning sub-covering disregarding the "X_Vars"
		
	}
	
	/* Converting two dimensional array of abstract values to a two-dimentionals array of linkedlist 
	 * where each linkedlist contains the abstract value in the corresponding array cell */
	private LinkedList[][] convert2List(abstractValue[][] arr)
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

	