package fr.abes.sudoqual.cli.services;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import org.json.JSONObject;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import fr.abes.sudoqual.linking_module.util.OutputComparator;

@Parameters(commandDescription = "Compare two output files")
public class CompareService implements Service {
	public static final String NAME = "compare";

	@Parameter(description = "<JSON actual file> <JSON expected file>", required = true, arity = 2)
	private List<String> jsonFiles = new ArrayList<String>();

    public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
		JSONObject actual = CLIUtils.readJSONFromFile(this.jsonFiles.get(0), options.getCharset(), out);
		JSONObject expected = CLIUtils.readJSONFromFile(this.jsonFiles.get(1), options.getCharset(), out);

		if(actual == null || expected == null) {
			err.println("Unable to load actual or expected file.");
			err.flush();
			out.flush();
			return 1;
		}

		compare(actual, expected);
		return 0;
	}

	public static void compare(JSONObject actual, JSONObject expected) {
		String report = OutputComparator.cmp(actual, expected);
		System.out.println(report);
		if (report.isEmpty()) {
			System.out.println("All is as expected :)");
		}
	}

}
