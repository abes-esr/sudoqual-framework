package fr.abes.sudoqual.cli.services;

import javax.annotation.Nullable;

import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import fr.abes.sudoqual.util.json.JSONObjects;
import org.json.JSONObject;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;

import java.io.*;

@Parameters(commandDescription = "run link")
public class LinkService implements Service {
	public static final String NAME = "link";
	private static final String SOURCES_KEY = "sources";
	private static final String TARGETS_KEY = "targets";
	private static final String INITIAL_LINKS_KEY = "initialLinks";
	private static final String COMPUTED_LINKS_KEY = "computedLinks";

	//private static final Logger logger = LoggerFactory.getLogger(FeatureCommand.class);

	@Parameter(names = { "-f", "--input" }, description = "Input file in JSON")
	private String input_filepath = "-";

    @Parameter(names = { "-d", "--diagnostic" }, description = "Enable diagnostic (available only for many-to-one configuration)")
    private boolean isDiagnosticEnable = false;

    @Parameter(names = { "-c", "--clustering" }, description = "Enable clustering (available only for many-to-many with\n" +
        "targets==sources configuration)")
    private boolean isClusteringEnable = false;

	@Parameter(names = { "--nb-threads" }, description = "Number of threads to use.")
	private int nbThreads = Runtime.getRuntime().availableProcessors();

	private SudoqualCommander mainOptions;
	public void setMainOptions(SudoqualCommander mainOptions) {
	    this.mainOptions = mainOptions;
    }

	public LinkService() {
	}

	@Override
	public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
		this.mainOptions = options;

		// check option consistency
		if(this.isClusteringEnable && this.isDiagnosticEnable) {
			err.println("Theses two options are not compatible.");
			return 1;
		}

		// run
        JSONObject result = null;
        try {
            InputStream is = (input_filepath.equals("-"))? System.in : new FileInputStream(this.input_filepath);
            result = this.link(this.mainOptions.getLinkingModule(), is, err);
        } catch (FileNotFoundException e) {
            err.println("Enable to read input file: " + this.input_filepath + ". An error occurred: ");
            e.printStackTrace(err);
            return 1;
        }

        if(result == null) {
			err.println("Something went wrong, no result produced.");
			return 1;
		}

		if(this.mainOptions.isVerbose()) {
			err.println("Displaying...");
		}

		CLIUtils.printResult(result, this.mainOptions.isPrettyPrintEnable(), out);
		return 0;

	}

    public @Nullable JSONObject link(LinkingModule module, InputStream inputStream, PrintStream err) {
        JSONObject input = JSONObjects.from(inputStream, this.mainOptions.getCharset());
        return this.link(module, input, err, this.isDiagnosticEnable, this.isClusteringEnable);
    }

    public @Nullable JSONObject link(LinkingModule module, JSONObject input, PrintStream err, boolean isDiagnosticEnable, boolean isClusteringEnable) {
        JSONObject result = null;
        LinkingModule toClose = null;

        try {
            if(module == null) {
                if(this.mainOptions.isVerbose()) {
                    err.println("Starting the linking module using " + this.nbThreads + " threads.");
                }
                toClose = LinkingModule.create(this.nbThreads);
                module = toClose;
                module.registerPath(this.mainOptions.getScenarioDir());
            }

            if(this.mainOptions.isVerbose()) {
                err.println("Executing linking module...");
            }
            result = module.execute(input);
        } catch (LinkingModuleException e) {
            err.println("An error occurs during LinkingModule execution. See details below:");
            e.printStackTrace(err);
            return null;
        } catch (InterruptedException e) {
            err.println("The LinkingModule processus was interrupted.");
            return null;
        } finally {
        	if(toClose != null) {
        		toClose.close();
        	}
        }

        if(isDiagnosticEnable) {
            if(this.mainOptions.isVerbose()) {
                err.println("Preparing diagnostic module input...");
            }
            JSONObject diagInput = new JSONObject();
            diagInput.put(SOURCES_KEY, CLIUtils.removeAllSourceFromSafeLinks(input.getJSONArray(SOURCES_KEY), input.optJSONArray("safeLinks")));
            diagInput.put(TARGETS_KEY, input.getJSONArray(TARGETS_KEY));
            diagInput.put(INITIAL_LINKS_KEY, input.getJSONArray(INITIAL_LINKS_KEY));
            diagInput.put(COMPUTED_LINKS_KEY, result.getJSONArray(COMPUTED_LINKS_KEY));
            if(this.mainOptions.isVerbose()) {
                err.println("Executing diagnostic module...");
            }

            JSONObject diagResult = DiagnosticService.diagnostic(diagInput, err);
            if (diagResult != null) {
                result.remove(COMPUTED_LINKS_KEY);
                for (String key : diagResult.keySet()) {
                    result.put(key, diagResult.get(key));
                }
            } else {
                // error
                result = null;
            }
        }
        if(isClusteringEnable) {
            if(this.mainOptions.isVerbose()) {
                err.println("Launches clustering...");
            }
            JSONObject clusterRes = ClusteringService.clustering(result, err);
            if (clusterRes != null) {
                for (String key : clusterRes.keySet()) {
                    result.put(key, clusterRes.get(key));
                }
            } else {
                // error
                result = null;
            }
        }

        return result;
	}


}
