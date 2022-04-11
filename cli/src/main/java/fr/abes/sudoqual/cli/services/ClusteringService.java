package fr.abes.sudoqual.cli.services;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import fr.abes.sudoqual.modules.clustering.Clustering;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.PrintStream;

@Parameters(commandDescription = "run clustering")
public class ClusteringService implements Service {
	public static final String NAME = "clustering";

	@Parameter(names = { "-f", "--input" }, description = "Input file in JSON")
	private String input_filepath = "-";

	public ClusteringService() {
	}

    public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
		JSONObject o = this.clustering(options, err);
		CLIUtils.printResult(o, options.isPrettyPrintEnable(), out);
		return 0;
	}

	private JSONObject clustering(SudoqualCommander options, PrintStream err) {
		if(options.isVerbose()) {
			err.println("Executing clustering module...");
		}

        JSONObject input = CLIUtils.readInputJSON(this.input_filepath, options.getCharset(), err);
		JSONObject result = clustering(input, err);

		if(options.isVerbose()) {
			err.println("Done.");
		}

		return result;
	}

	public static @Nullable JSONObject clustering(JSONObject input, PrintStream err) {
        JSONObject result = null;
        try {
            result = Clustering.INSTANCE.execute(input);
        } catch (Exception e) {
            err.println("An error occured during clustering:");
            e.printStackTrace(err);
        }
        return result;
    }

}
