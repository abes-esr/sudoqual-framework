package fr.abes.sudoqual.util.json;

import org.everit.json.schema.ValidationException;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class JSONValidationReportImpl implements JSONValidationReport {
	
	private boolean isSuccess;
	private String message;
	
	JSONValidationReportImpl(ValidationException e) {
		StringBuilder sb = new StringBuilder();
    	sb.append(e.getMessage()).append('\n');
    	e.getCausingExceptions().stream()
    	      .map(ValidationException::getMessage)
    	      .forEach(x -> {sb.append(x).append('\n');});
    	this.message = sb.toString();
    	this.isSuccess = false;
	}
	
	JSONValidationReportImpl() {
		this.message = "";
		this.isSuccess = true;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	//	
	// /////////////////////////////////////////////////////////////////////////
	
    @Override
	public boolean isSuccess() {
    	return this.isSuccess;
    }

    @Override
	public String getErrorMessage() {
    	return this.message;
    }
    
    @Override
	public String toString() {
    	return this.message;
    }
    
    
}
