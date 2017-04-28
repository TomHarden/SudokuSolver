package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class File_Manipulation
{
	//****************
	//*****CREATE*****
	public static ArrayList<String> getFileText(String fileName)
	{
	    ArrayList<String> lineList = new ArrayList<String>();
	    String line;
	    try
	    {
	        BufferedReader reader = new BufferedReader(new FileReader(fileName));
		    while((line = reader.readLine()) != null)
		    	lineList.add(line);
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	    return lineList;
	}
	
}
