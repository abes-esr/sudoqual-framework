package fr.abes.sudoqual.cli.services;

import javax.annotation.Nullable;

import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import org.json.JSONObject;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import fr.abes.sudoqual.modules.diagnostic.Diagnostician;
import fr.abes.sudoqual.modules.diagnostic.exception.DiagnosticianException;

import java.io.PrintStream;

@Parameters(commandDescription = "run diagnostic")
public class DiagnosticService implements Service {
	public static final String NAME = "diagnostic";

	@Parameter(names = { "-f", "--input" }, description = "Input file in JSON")
	private String input_filepath = "-";

	public DiagnosticService() {
	}

    public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
		JSONObject o = this.diagnostic(options, err);
		CLIUtils.printResult(o, options.isPrettyPrintEnable(), out);
		return 0;
	}

	private JSONObject diagnostic(SudoqualCommander options, PrintStream err) {
		if(options.isVerbose()) {
			err.println("Executing diagnostic module...");
		}

        JSONObject input = CLIUtils.readInputJSON(this.input_filepath, options.getCharset(), err);
		JSONObject result = diagnostic(input, err);

		if(options.isVerbose()) {
			err.println("Done.");
		}

		return result;
	}

	public static @Nullable JSONObject diagnostic(JSONObject input, PrintStream err) {
        JSONObject result = null;
        try {
            result = Diagnostician.createManyToOneDiagnostician().execute(input);
        } catch (DiagnosticianException e) {
            err.println("An error occured during diagnostic:");
            e.printStackTrace(err);
        }
        return result;
    }




}
