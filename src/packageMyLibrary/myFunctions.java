package packageMyLibrary;

import java.util.*;
import java.io.*;
import java.io.File;

public class myFunctions
{
	
	public boolean isExist(String target, String[] list)
	{
		for(int i=0;i<list.length;i++)
		{
			if(target.equals(list[i]))
			{
				return true;
			}	
		}
		return false;
	}
	
	public boolean isExist(String target, LinkedList list)
	{
		ListIterator itr=list.listIterator();
		
		while(itr.hasNext())
		{
			String str=(String)itr.next();
			
			if(target.equals(str))
				return true;
		}
		return false;
	}
	
	public boolean isExist(int target, int[] list)
	{
		for(int i=0;i<list.length;i++)
		{
			if(target==list[i])
			{
				return true;
			}	
		}
		return false;
	}
	
	
	public boolean isExist(int target, LinkedList list)
	{
		ListIterator itr=list.listIterator();
		while(itr.hasNext())
		{
			int IntVal=((Integer)itr.next()).intValue();
		
			if(target==IntVal)	
			{
				return true;	
			}
		}
		
		return false;
	}

	
	public int getIndex(String target, String[] list)
   	{   		
   		int index=-1;   				 		
	
		for(int i=0;i<list.length;i++)
   		{
   			if(target.equals(list[i]))
   			{	
   				index=i;break;
   			}
   		}
   		
   		if(index==-1)
   		{
   			System.out.println("The target "+target+" is not in the given list.");
   			return -1;
   		}
		else
		{
   			return index;
   		}	    
   	}
   	
   	public int getIndex(int target, int[] list)
   	{   		
   		int index=-1;   				 		
	
		for(int i=0;i<list.length;i++)
   		{
   			if(target==list[i])
   			{	
   				index=i;break;
   			}
   		}
   		
   		if(index==-1)
   		{
   			System.out.println("The target "+target+" is not in the given list.");
   			return -1;
   		}
		else
		{
   			return index;
   		}	    
   	}
   		
   		
   	public String extractInfoLine(RandomAccessFile rafExtractInfoFile, int line_no)
   	{
   		String result="";
   		
		try{
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				int extract_line_no=new Integer(field[0].trim());			
			
				if(extract_line_no==line_no)
				{
					result=stmt;
					break;
				}
				else
					stmt=rafExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary:myFunctions:extractInfoLine(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary:myFunctions:extractInfoLine(): Problem with IO exception." + e);}
        	
	    return result;
   	}
   	
   	public String getType(RandomAccessFile rafExtractInfoFile, int line_no)
   	{
   		String type="";
   		
   		try{
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				int extract_line_no=Integer.parseInt(field[0].trim());			
			
				if(extract_line_no==line_no)
				{
					type= field[1].trim();
					break;
				}
				else
					stmt=rafExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getType(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getType(): Problem with IO exception." + e);}
        	
		return type;
   	}
   	
   	public String getLabel(RandomAccessFile rafExtractInfoFile, int line_no)
   	{
   		String label="";
   		
		try{
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				int extract_line_no=new Integer(field[0].trim());			
			
				if(extract_line_no==line_no)
				{
					label=field[6].trim();
					break;
				}
				else
					stmt=rafExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getLabel(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getLabel(): Problem with IO exception." + e);}
        	
	    return label;
   	}
   	
	public String getDef(RandomAccessFile rafExtractInfoFile, int line_no)
   	{
   		String def="";
   		
		try{
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				int extract_line_no=new Integer(field[0].trim());			
			
				if(extract_line_no==line_no)
				{
					String temp=field[3].trim();
					if(!temp.equals("*"))	
					{
						def= temp;
					}
					break;
				}
				else
					stmt=rafExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getDef(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getDef(): Problem with IO exception." + e);}
        	
	    return def;
   	}
   	
   	public LinkedList getUseVars(RandomAccessFile rafExtractInfoFile, int line_no)
   	{  		
   		String use="";
   		
		try{
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				int extract_line_no=new Integer(field[0].trim());			
			
				if(extract_line_no==line_no)
				{
					use= field[4].trim();
					break;
				}
				else
					stmt=rafExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getUseVars(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getUseVars(): Problem with IO exception." + e);}
        	
		LinkedList list=new LinkedList();
		if(!use.equals("*"))
		{
		   	String[] useArr=use.split("\\|");
		   	list=convertStringArrayToList(useArr);
		}
		return list;
   	}
   	
   	public LinkedList getUseVars(String expr, String VarSeperator)
	{
		LinkedList list=new LinkedList();
     	String REGEX_VARIABLE = "[a-zA-Z$_]\\w*";
     	 				
     	StringTokenizer st = new StringTokenizer(expr, VarSeperator);
     	 				
		while(st.hasMoreTokens())
     	{
     		String token=st.nextToken();
     		if(java.util.regex.Pattern.matches(REGEX_VARIABLE, token)  && !isExist(token, list))
     	 	{
     	 		list.addLast(token);
     	 	}	   	 				
     	}
     	 
     	return list;					
	}
   	
   	public int getLineNo(RandomAccessFile rafExtractInfoFile, String label)
   	{
   		int line_no=-1;
   		
   		try{
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				String extract_label=field[6].trim();			
			
				if(extract_label.equals(label))
				{
					line_no= Integer.parseInt(field[0].trim());
					break;
				}
				else
					stmt=rafExtractInfoFile.readLine();				
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getLineNo(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getLineNo(): Problem with IO exception." + e);}
        	
		return line_no;
   			
   	}
   	
   	public LinkedList getDefinedLines(RandomAccessFile rafExtractInfoFile, String var)
   	{
   		LinkedList defLines=new LinkedList<Integer>();
   		LinkedList cntrlList=new LinkedList<String>();
   		
		try{
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				String def=field[3].trim();
				
				if(def.equals(var))
				{
					int line_no=new Integer(field[0].trim());
					String cntrl=field[2].trim();
					
					if(! isExist(line_no, defLines))
					{
						defLines.addLast(line_no);
						
						if(! isExist(cntrl, cntrlList))
						{
							cntrlList.addLast(cntrl);
						}
					}
				}			
				
				stmt=rafExtractInfoFile.readLine();				
			}
			
			if(cntrlList.size()==1)
			{
				System.out.println("myLibrary: myFunctions:getDefinedLines(): There is only one line that defines "+var+"!!!");
				defLines.clear();
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getDefinedLines(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getDefinedLines(): Problem with IO exception." + e);}
        	
	    return defLines;
   	}
   	
	
	
   	public String[] getLabelSet(RandomAccessFile rafExtractInfoFile)
   	{
   		int size=getProgSize(rafExtractInfoFile);
   		String[] L=new String[size];
   		
   		try{
   			
   			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				
				int extract_line_no=new Integer(field[0].trim());
				String label=field[6].trim();
				
				L[extract_line_no]=new String(label);
				
				stmt=rafExtractInfoFile.readLine();	
			}
   			
   		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getLabel(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getLabel(): Problem with IO exception." + e);}
        	
        return L;
        	
   	}
   	
   	
   	
   	public String[] getProgVars(RandomAccessFile rafExtractInfoFile)
	{
		LinkedList list=new LinkedList();
		
		try{
		
			rafExtractInfoFile.seek(0);
			String stmt=rafExtractInfoFile.readLine();
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				String type=field[1].trim();
			
				if(type.equals("as"))
				{
					String defVar=field[3].trim();
				
					if(!defVar.equals("*") && ! isExist(defVar, list))
					{
						list.addLast(defVar);	
					}
				}
				
				String use=field[4].trim();
				if(!use.equals("*"))
				{
		 		  	String[] useVars=use.split("\\|");
		  			for(int i=0;i<useVars.length;i++)
					{
						if(!isExist(useVars[i], list))
						{
							list.addLast(useVars[i]);
						}	
					}
				}		
			
				stmt=rafExtractInfoFile.readLine();			
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getProgVars(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getProgVars(): Problem with IO exception." + e);}
		
		String[] VariableArray=convertListToStringArray(list);			
		return VariableArray;
		
	}
	
	
	public int getProgSize(RandomAccessFile rafExtractInfoFile)
	{
					
		int first_line_no=0;
		int last_line_no=0;
			
		boolean flag=true;
			
		try{
			rafExtractInfoFile.seek(0);
			
			String stmt=rafExtractInfoFile.readLine();			
			while(stmt.length()!=0)
			{
				String[] field=stmt.split(" ");
				if(flag)
				{
					first_line_no=Integer.parseInt(field[0].trim());
					flag=false;
				}
				else
				{
					last_line_no=Integer.parseInt(field[0].trim());
				}
					
				stmt=rafExtractInfoFile.readLine();
			}
			
		}catch (FileNotFoundException e){
        	 System.err.println("myLibrary: myFunctions:getProgSize(): Input File not found." + e);
 	   	}catch (IOException e){
        	System.err.println("myLibrary: myFunctions:getProgSize(): Problem with IO exception." + e);}
			
		return ((last_line_no-first_line_no)+1);		
	}
	
	public int[] convertListToIntegerArray(LinkedList list)
   	{
   		
   		int[] arr=new int[list.size()];
			
		int i=0;
		ListIterator itr=list.listIterator();
		while(itr.hasNext())
		{
			arr[i++]=((Integer)itr.next()).intValue();
			
		}
		
		return arr;	
   	}
   	
   	public LinkedList convertIntegerArrayToList(int[] arr)
   	{
   		LinkedList list=new LinkedList();
			
		for(int i=0;i<arr.length;i++)
		{
			list.addLast(arr[i]);
		}
		
		return list;	
   	}
   	
   	
	public String[] convertListToStringArray(LinkedList list)
   	{
   		String[] arr=new String[list.size()];
			
		int i=0;
		ListIterator itr=list.listIterator();
		while(itr.hasNext())
		{
			arr[i++]=itr.next().toString();
		}
		
		return arr;	
   	}
   	
   	public LinkedList convertStringArrayToList(String[] arr)
   	{
   		LinkedList list=new LinkedList();
			
		for(int i=0;i<arr.length;i++)
		{
			list.addLast(arr[i]);
		}
		
		return list;	
   	}
   	
   	
   	public String convertListToString(LinkedList list)
	{
		String str="";
		
		if(list.size()!=0)
		{
			Object[] Array_list=list.toArray();

			for(int i=0;i<Array_list.length;i++)
			{
				if(str.equals(""))
					str=(String) Array_list[i];
				else
					str=str+"|"+ (String) Array_list[i];
			}
		}

		return str;	
	}
	
	public LinkedList extractSubList(LinkedList list, int index)
	{
		LinkedList newList=new LinkedList();
		
		if(index>=0 && index<=list.size()-1)
		{
			ListIterator itr=list.listIterator(index);
		
			while(itr.hasNext())
			{
				newList.addLast(itr.next());
			}
		}
		
		return newList;
	}
	
	public LinkedList[] CartesianProduct(LinkedList[][] lst)
	{
		if(lst.length==0)
		{
			System.out.println("Trying to cross-product an empty set.....");
			return null;
		}
		else if(lst.length==1)
		{
			return lst[0];
		}
		else if(lst.length==2)
		{
			return Product2List(lst[0], lst[1]);
		}
		else
		{
			LinkedList[][] first=new LinkedList[1][];
			first[0]=new LinkedList[lst[0].length];
			for(int i=0; i<lst[0].length; i++)
			{
				first[0][i]=lst[0][i];
			}
			
			LinkedList[][] rest= new LinkedList[lst.length-1][];
			for(int i=0;i<lst.length-1;i++)
			{
				rest[i]=new LinkedList[lst[i+1].length];
				for(int j=0; j<lst[i+1].length; j++)
				{
					rest[i][j]=lst[i+1][j];
				}
				
			}
			LinkedList[] x=CartesianProduct(first);
			LinkedList[] y=CartesianProduct(rest);

			LinkedList[] result= Product2List(x, y);
			return result;
		}
	}
	
	public LinkedList[] Product2List(LinkedList[] arr1, LinkedList[] arr2)
	{
		int size=(arr1.length)*(arr2.length);
		LinkedList[] arr=new LinkedList[size];
		
		for(int i=0;i<size;i++)
		{
			arr[i]=new LinkedList();	
		}
		
		int k=0;
		for(int i=0; i<arr1.length; i++)
		{			
			for(int j=0; j<arr2.length; j++)
			{
				merge(arr1[i], arr2[j], arr[k]);
				k++;
			}	
		}
		return arr;	
	}
	
	public LinkedList merge(LinkedList lst1, LinkedList lst2, LinkedList lst)
	{
		lst.clear();
		
		ListIterator itr1=lst1.listIterator();
		
		ListIterator itr2=lst2.listIterator();
		
		while(itr1.hasNext())
		{
			lst.addLast(itr1.next());
		}
		
		while(itr2.hasNext())
		{
			lst.addLast(itr2.next());
		}
		
		return lst;
	}
	
	public LinkedList copyLinkedList(LinkedList list)
	{
		LinkedList result=new LinkedList();
		
		ListIterator itr=list.listIterator();
		while(itr.hasNext())
		{
			result.addLast(itr.next());	
		}
		
		return result;
	}
	
	public LinkedList copyLinkedList(LinkedList list, int start, int end)
	{
		LinkedList result=new LinkedList();
		
		int index=start;
		while(index<=end)
		{
			result.addLast(list.get(index));
			index++;	
		}
		
		return result;
	}
	
	public LinkedList getUnion(LinkedList list1, LinkedList list2)
	{
		LinkedList result=copyLinkedList(list1);
		
		ListIterator itr=list2.listIterator();
		while(itr.hasNext())
		{
			Object obj=itr.next();
			
			if(!result.contains(obj))
				result.addLast(obj);	
		}
		
		return result;
	}
	
	public LinkedList getIntersect(LinkedList list1, LinkedList list2)
	{
		LinkedList result=copyLinkedList(list1);
		
		ListIterator itr=list1.listIterator();
		while(itr.hasNext())
		{
			Object obj=itr.next();
			
			if(!list2.contains(obj))
				result.remove(obj);	
		}
		
		return result;
	}
	
}
