package fr.abes.sudoqual.cli.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import org.json.JSONArray;
import org.json.JSONObject;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import fr.abes.sudoqual.eval.EvalResult;
import fr.abes.sudoqual.eval.SudoqualEval;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;

@Parameters(commandDescription = "Run evaluation", hidden = false)
public class EvalService implements Service {

	private static final String SOURCES_KEY = "sources";

	public static final String NAME = "eval";

	private static final String INPUT_KEY = "input";
	private static final String COMPUTED_LINKS_KEY = "computedLinks";
	private static final String EXPECTED_LINKS_KEY = "expectedLinks";



	@Parameter(description = "<JSON bench file> [<JSON bench file>...]", required = true)
	private List<String> jsonFiles = new ArrayList<String>();

    public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
		if(this.jsonFiles.size() < 1) {
			err.println("There should be at least 1 main parameter.");
		}
		EvalResult result = eval(this.jsonFiles, options, out, err);
		if(this.jsonFiles.size() > 1 || new File(this.jsonFiles.get(0)).isDirectory()) {
			out.println("===============================");
			out.println("=           TOTAL             =");
			out.println("===============================");
			out.println(result);
		}
		return 0;
	}

	public static EvalResult eval(List<String> filepaths, SudoqualCommander options, PrintStream out, PrintStream err) {
		EvalResult total = new EvalResult();
		for(String path : filepaths) {
			EvalResult result = eval(path, options, out, err);
			total.add(result);
		}
		return total;
	}


	public static EvalResult eval(String filepath, SudoqualCommander options, PrintStream out, PrintStream err) {
		File file = new File(filepath);
		EvalResult result = null;
		out.println("=== " + filepath + " ===");
		if(file.isFile()) {
			if(file.getName().endsWith(".json")) {
				result = evalFile(filepath, options, out, err);
			} else {
				out.println("This file was ignored because is not a JSON file.\n");
			}
		} else if(file.isDirectory()) {
			result = evalDir(file, options, out, err);
			out.println("===");
			out.println("= SUB-TOTAL");
			out.println("===");
		}
		if(result != null) {
			out.println(result);
			return result;
		} else {
			return new EvalResult();
		}
	}

	public static EvalResult evalDir(File dir, SudoqualCommander options, PrintStream out, PrintStream err) {
		try {
			List<String> filepaths = Files.list(dir.toPath()).map(path -> path.toString()).collect(Collectors.toList());
			return eval(filepaths, options, out, err);
		} catch (IOException e) {
            err.println("An error occured while reading following directory: " + dir.getAbsolutePath());
			return null;
		}
	}

	public static EvalResult evalFile(String filepath, SudoqualCommander options, PrintStream out, PrintStream err) {
		JSONObject benchFile = CLIUtils.readJSONFromFile(filepath, options.getCharset(), out);
		JSONObject input = benchFile.getJSONObject(INPUT_KEY);
		JSONArray expected = benchFile.getJSONArray(EXPECTED_LINKS_KEY);

		if(input == null || expected == null) {
			return null;
		}

		JSONObject actual = link(input, options, err);
		return SudoqualEval.evalResults(input.getJSONArray(SOURCES_KEY), actual.getJSONArray(COMPUTED_LINKS_KEY), expected);
	}


	public static JSONObject link(JSONObject input, SudoqualCommander options, PrintStream err) {
		JSONObject result = null;
		try (LinkingModule module = LinkingModule.create(4)) {
			module.registerPath(options.getScenarioDir());

			try {
				if(options.isVerbose()) {
					err.println("Executing linking module...");
				}
				result = module.execute(input);
			} catch (LinkingModuleException e) {
				err.println("An error occurs during LinkingModule execution. See details below:");
				e.printStackTrace(err);
			} catch (InterruptedException e) {
				err.println("The LinkingModule processus was interrupted.");
			}

		}

		if(options.isVerbose()) {
			err.println("Linking done.");
		}

		return result;
	}

}
