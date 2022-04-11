package fr.abes.sudoqual.util.json;

public interface JSONValidationReport {
	
	JSONValidationReportImpl SUCCESS_REPORT = new JSONValidationReportImpl();
	
	/**
	 * Tell whether the report represents a validation success
	 *
	 * @return true if validation success, false otherwise.
	 */
	boolean isSuccess();

	/**
	 * If validation fails, this method return a message representing these errors 
	 * @return a String representing errors, if any. An empty String, otherwise.
	 */
	String getErrorMessage();

}