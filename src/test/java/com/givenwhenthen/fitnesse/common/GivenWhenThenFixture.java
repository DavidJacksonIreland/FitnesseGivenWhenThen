package com.givenwhenthen.fitnesse.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GivenWhenThenFixture is an abstract fixture base class which implements the Fitnesse TableTable pattern
 * 
 * It provides the ability to hook in Gherkin-style given/when/then syntax into a fitnesse table.
 * It supports Given/When/Then with any number of "and" clauses in the Given and Then steps
 * It uses a fixed placeholder to identify input parameters within a step definition - it uses square brackets [] for this.
 * 
 * See src/test/resources package for table example in the Fitnesse wiki
 * See src/test/java/com/fmr/fitnesse/example package for example of how to subclass this fixture and 
 * implement the sample wiki tables in src/test/resources 
 * 
 * @author David Jackson (A316102)
 * @version 1.0
 *
 */
public abstract class GivenWhenThenFixture
{
	private static final String AndKeyword = "AND";
	
	private static final String PassStyle = "pass";
	private static final String FailStyle = "fail";
	private static final String ErrorStyle = "error:";
	
	// used by subclasses to set more info (if necessary) when marking a then/and cell as failure (red)
	private String lastVerifiedValueAsString = "";
	private String lastExpectedValueAsString = "";
	
	protected String getLastVerifiedValueAsString() {
		return lastVerifiedValueAsString;
	}

	protected void setLastVerifiedValueAsString(String lastVerifiedValueAsString) {
		this.lastVerifiedValueAsString = lastVerifiedValueAsString;
	}
	
	protected String getLastExpectedValueAsString() {
		return lastExpectedValueAsString;
	}

	protected void setLastExpectedValueAsString(String lastExpectedValueAsString) {
		this.lastExpectedValueAsString = lastExpectedValueAsString;
	}

	/**
	 * Fitnesse TableTable standard method which is the entry point for a Fitnesse test
	 * 
	 * @param table the full contents of the table passed from Fitnesse (except the header row)
	 * @return the processed table contents including results
	 */
	@SuppressWarnings("rawtypes")
	public List doTable(List<List<String>> table) 
	{	
		try
		{
			doTestSetup();
			int rowIndex = 0;
			
			// GIVEN
			List given = list("","");
			doStep(table.get(rowIndex++), given);
			List<List> returnTable = list(given);
			
			// AND...
			while (table.get(rowIndex).get(0).equalsIgnoreCase(AndKeyword))
			{
				List and = list("", "");
				doStep(table.get(rowIndex++), and);
				returnTable.add(and);
			}
			
			// WHEN...
			List when = list("","");
			doStep(table.get(rowIndex++), when);
			returnTable.add(when);
			
			// THEN
			List then = list("","");
			doStep(table.get(rowIndex++), then);
			returnTable.add(then);
			
			// AND...
			while ((rowIndex < table.size()) && 
					(table.get(rowIndex).get(0).equalsIgnoreCase(AndKeyword)))
			{
				List and = list("", "");
				doStep(table.get(rowIndex++), and);
				returnTable.add(and);
			}
			
		    return returnTable;
		}
		finally 
		{
		    doTestTeardown();
		}
	}
	
	/**
	 * Call an individual given/when/then step by using string manipulation and 
	 * java reflection to call method names on the underlying subclass which match
	 * what is specified in the fitnesse test
	 * 
	 * @param tableRow the fitnesse table row containing the step
	 * @param rowResult the result produced by the step execution which will form part of the overall returned table to fitnesse including results
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doStep(List<String> tableRow, List rowResult)
	{
		// reset test fail description
		this.lastVerifiedValueAsString = "";
		
		// get given step without any parameters which are surrounded in [] brackets
		String stepInstruction = convertToCamelCase(tableRow.get(1).replaceAll("\\[.*?\\]","").trim());
		int parameterCount = tableRow.get(1).length() - tableRow.get(1).replace("[", "").length(); 
		
		Class[] parameterTypes = new Class[parameterCount];
		populateParameterTypesFromStep(tableRow.get(1), parameterTypes);
		
		Object[] parameterValues = new Object[parameterCount];
		populateParameterValuesFromStep(tableRow.get(1), parameterValues);
		
		try
		{
			Method stepMethod = this.getClass().getMethod(stepInstruction, parameterTypes);
			Object returnValue = stepMethod.invoke(this, parameterValues);
			if (returnValue instanceof Boolean)
			{
				Boolean thenResult = (Boolean) returnValue;
				rowResult.set(0, thenResult ? PassStyle : generateFailStyle(tableRow));
			}
		}
		catch (Exception e) 
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			
			rowResult.set(0, FailStyle);
			rowResult.set(1, ErrorStyle + rowResult.get(1) + " Exception: " + exceptionAsString);
		}
	}

	/**
	 * Virtual method - override in subclasses of this fixture
	 */
	public void doTestSetup()
	{
	}
	  
	/**
	 * Virtual method - override in subclasses of this fixture
	 */
	public void doTestTeardown() 
	{
	}
	
	/**
	 * Convert a string that may be of the form "Joe wants to create a profile" into a camelCase method name "joeWantsToCreateAProfile"
	 * 
	 * @param strdata input string for conversion
	 * @return camel-case version of input string
	 */
	protected static String convertToCamelCase(String strdata){

		StringBuffer strbufCamelCase = new StringBuffer();
		StringTokenizer st = new StringTokenizer(strdata);

		if(null == strdata || strdata.length() == 0){
			return "";
		}

		boolean firstToken = true;
		while(st.hasMoreTokens()){
			String strWord = st.nextToken();
			
			// first word should start lower case
			if (firstToken) {
				strbufCamelCase.append(strWord.substring(0,1).toLowerCase());
				firstToken = false;
			}
			else {
				strbufCamelCase.append(strWord.substring(0,1).toUpperCase());	
			}
			
			if(strWord.length()>1){
				strbufCamelCase.append(strWord.substring(1).toLowerCase());
			}
		}

		return strbufCamelCase.toString();
	}
	
	/**
	 * Take the parameter values (contained within [] brackets) and put them in the given parameter array
	 * 
	 * @param stepString The step instruction containing the marked [parameters]
	 * @param parameterValues The output array to put the parameter values in
	 */
	private void populateParameterValuesFromStep(String stepString, Object[] parameterValues)
	{
		Pattern parameterPattern = Pattern.compile("\\[([^\\]]+)]");
        Matcher matcherValidity = parameterPattern.matcher(stepString);
        
        int paramIndex = 0;
        while (matcherValidity.find()){
        	String rawValue = matcherValidity.group(1);
            parameterValues[paramIndex] = isNumeric(rawValue) ? 
										Double.parseDouble(rawValue) : rawValue;
            paramIndex++;
        }
	}
	
	/**
	 * Take the parameter values (contained within [] brackets) and put them in the given parameter array
	 * 
	 * @param stepString The step instruction containing the marked [parameters]
	 * @param parameterTypes The output array to put the parameter values in
	 */
	@SuppressWarnings("rawtypes")
	private void populateParameterTypesFromStep(String stepString, Class[] parameterTypes) 
	{
		Pattern parameterPattern = Pattern.compile("\\[([^\\]]+)]");
        Matcher matcherValidity = parameterPattern.matcher(stepString);
        
        int paramIndex = 0;
        while (matcherValidity.find()){
        	parameterTypes[paramIndex] = isNumeric(matcherValidity.group(1)) ? 
        							Double.class : String.class;
            paramIndex++;
        }
	}
	
	/**
	 * Helper method to check if a string is a numeric value
	 * Note that we could use apache lib for this or others but just keeping this fixture as thin as possible
	 * in terms of dependencies. Don't like throwing exceptions unnecessarily but easiest way for now.
	 * 
	 * @param paramValue The string to check if it's numeric
	 * @return true or false
	 */
	private boolean isNumeric(String paramValue) 
	{
		  try  
		  {  
			  Double.parseDouble(paramValue);  
		  }  
		  catch(NumberFormatException nfe)  
		  {  
			  return false;  
		  }  
		  return true;
	}

	/**
	 * Generate the failure style to mark a table cell with when a test has failed
	 * @return
	 */
	private String generateFailStyle(List<String> tableRow) {
		if (this.lastVerifiedValueAsString == null || this.lastVerifiedValueAsString.isEmpty())
		{
			return FailStyle;
		}
		else
		{
			StringBuilder failMessage = new StringBuilder(FailStyle);
			failMessage.append(":");
			failMessage.append(tableRow.get(0));
			failMessage.append(" (");
			if (this.lastExpectedValueAsString != null && !this.lastExpectedValueAsString.isEmpty())
			{
				failMessage.append("expected: ");
				failMessage.append(this.lastExpectedValueAsString);
				failMessage.append(", ");
			}
			failMessage.append("actual: ");
			failMessage.append(this.lastVerifiedValueAsString);
			failMessage.append(")");
			return failMessage.toString();
		}
	}
	
	/**
	 * Defining private method here to make the GivenWhenThen fixture self-contained
	 * If using in a project where you include the fitnesse jar, you could use the built-in
	 * ListUtility methods instead and remove this method
	 * 
	 * @param objects The series of objects to create a list from
	 * @return List containing the given objects
	 */
	private static <T> List<T> list(T... objects) {
		List<T> list = new ArrayList<T>();
		for (T object : objects)
			list.add(object);
		return list;
	}


}


