package packageMyLibrary;

import java.util.regex.*;

public interface myPatterns
{
	    public static final String REGEX_VARIABLE = "[a-zA-Z$_]\\w*";
	    public static final String REGEX_INTEGER = "\\d+"; 		
		public static final String REGEX_FLOAT = "\\d*\\.\\d+";    	
    	public static final String REGEX_ARITH_OPERATION = "[-*/+]";
    	public static final String REGEX_REL_OPERATION = "[<|>|==|!=|<=|>=]+";
    	public static final String REGEX_BOOL_OPERATION = "[! | & | \\|]+";
    	public static final String REGEX_PARANTHESIS = "[()]";
    	
    	public static final String REGEX_START = "\\Astart\\s*?\\{";
    	public static final String REGEX_IF = "\\Aif\\s*?\\(.+?\\)\\s*?\\{";
    	public static final String REGEX_ELSE = "\\Aelse\\s*?\\{";
    	public static final String REGEX_WHILE = "\\Awhile\\s*?\\(.+?\\)\\s*?\\{";
    	public static final String REGEX_PRINT = "\\Aprint\\s*?\\(.+?\\)\\s*?";
    	
    	//public static final String REGEX_START_in_PREVIEW = "\\d+\\:\\s*start\\{";
    	//public static final String REGEX_IF_in_PREVIEW = "\\d+\\:\\s*if\\(.+?\\)\\{";
    	//public static final String REGEX_WHILE_in_PREVIEW = "\\d+\\:\\s*while\\(.+?\\)\\{";
    	//public static final String REGEX_STOP_in_PREVIEW = "\\d+\\:\\s*stop;";
    	
    	public static final Pattern patrn_start=Pattern.compile(REGEX_START);
    	public static final Pattern patrn_if=Pattern.compile(REGEX_IF);
    	public static final Pattern patrn_else=Pattern.compile(REGEX_ELSE);
    	public static final Pattern patrn_while=Pattern.compile(REGEX_WHILE);
    	public static final Pattern patrn_print=Pattern.compile(REGEX_PRINT);    	
    	public static final Pattern patrn_assign=Pattern.compile( REGEX_VARIABLE + "=[" + REGEX_ARITH_OPERATION + "|" + REGEX_INTEGER + "|" + REGEX_FLOAT + "|" + REGEX_VARIABLE + "|" + "?" + "|" + REGEX_PARANTHESIS +"]+");
 		public static final Pattern patrn_arith_expr=Pattern.compile( "[" + REGEX_ARITH_OPERATION + "|" + REGEX_INTEGER + "|" + REGEX_FLOAT + "|" + REGEX_VARIABLE  + "|" + REGEX_PARANTHESIS +"]+");
 		public static final Pattern patrn_phi=Pattern.compile( REGEX_VARIABLE + "<<phi([" + REGEX_VARIABLE + "|" + "," + "]*)");

    	
    	//public static final Pattern patrn_start_in_preview=Pattern.compile(REGEX_START_in_PREVIEW);
    	//public static final Pattern patrn_if_in_preview=Pattern.compile(REGEX_IF_in_PREVIEW);
    	//public static final Pattern patrn_while_in_preview=Pattern.compile(REGEX_WHILE_in_PREVIEW);
    	//public static final Pattern patrn_stop_in_preview=Pattern.compile(REGEX_STOP_in_PREVIEW);
    	
    	//public static final Pattern pat_AssignCmd=Pattern.compile( REGEX_VARIABLE + "=[" + REGEX_ARITH_OPERATION + "|" + REGEX_INTEGER + "|" + REGEX_FLOAT + "|" + REGEX_VARIABLE + "|" + "?" + "|" + REGEX_PARANTHESIS +"]+");
		//public static final Pattern pat_PrintCmd=Pattern.compile(REGEX_PRINT);
 		//public static final Pattern pat_ArithExpr=Pattern.compile( "[" + REGEX_ARITH_OPERATION + "|" + REGEX_INTEGER + "|" + REGEX_FLOAT + "|" + REGEX_VARIABLE  + "|" + REGEX_PARANTHESIS +"]+");
 		//public static final Pattern pat_PhiCmd=Pattern.compile( REGEX_VARIABLE + "<<phi([" + REGEX_VARIABLE + "|" + "," + "]*)");

}