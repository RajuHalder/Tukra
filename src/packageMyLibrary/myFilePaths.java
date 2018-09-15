package packageMyLibrary;


public interface myFilePaths
{

	public static final String path=System.getProperty("user.dir");
	
	public static final String File_Out_Preview = path+"\\Output\\Ex_Out_Preview.txt";	
	public static final String File_Out_ExtractInfo = path+"\\Output\\Ex_Out_ExtractInfo.txt";
	public static final String File_Out_Rel_SemanticPreview = path+"\\Output\\Ex_Out_Rel_SemanticPreview.txt";
	public static final String File_Out_Rel_SemanticExtractInfo = path+"\\Output\\Ex_Out_Rel_SemanticExtractInfo.txt";
	public static final String File_Out_Dep_SemanticPreview = path+"\\Output\\Ex_Out_Dep_SemanticPreview.txt";
	public static final String File_Out_Dep_SemanticExtractInfo = path+"\\Output\\Ex_Out_Dep_SemanticExtractInfo.txt";
	public static final String File_Out_Slice_Preview=path+"\\Output\\Ex_Out_Slice_Preview.txt";
	public static final String File_Out_Slice_ExtractInfo=path+"\\Output\\Ex_Out_Slice_ExtractInfo.txt";
	public static final String File_Out_SSA_ExtractInfo=path+"\\Output\\Ex_Out_SSA_ExtractInfo.txt";
	public static final String File_Out_SSA_Preview=path+"\\Output\\Ex_Out_SSA_Preview.txt";
	public static final String File_Out_DCG_Preview=path+"\\Output\\Ex_Out_DCG_Preview.txt";
}