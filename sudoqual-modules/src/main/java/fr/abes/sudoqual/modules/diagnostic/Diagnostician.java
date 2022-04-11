package fr.abes.sudoqual.modules.diagnostic;

import java.io.InputStream;
import java.nio.charset.Charset;

import fr.abes.sudoqual.api.SudoqualModule;
import org.json.JSONObject;

import fr.abes.sudoqual.modules.diagnostic.exception.DiagnosticianException;


public interface Diagnostician extends SudoqualModule {

    @Override
	String execute(String input) throws DiagnosticianException;
	@Override
    String execute(InputStream input, Charset charset) throws DiagnosticianException;
	@Override
	JSONObject execute(JSONObject input) throws DiagnosticianException;

	static Diagnostician createManyToOneDiagnostician() throws DiagnosticianException {
		return new ManyToOneDiagnosticianImpl();
	}

}
