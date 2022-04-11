package fr.abes.sudoqual.cli.services;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import fr.abes.sudoqual.cli.services.hidden.FeatureCommand;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.modules.constraint.Constraint;
import fr.abes.sudoqual.modules.clusterRaOverlap.ClusterRaOverlap;
import fr.abes.sudoqual.modules.transitivity.Transitivity;
import fr.abes.sudoqual.util.json.JSONArrays;
import fr.abes.sudoqual.util.json.JSONObjects;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Nullable;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Parameters(commandDescription = "run align")
public class AlignService implements Service {
    public static final String NAME = "align";
    private static final String SOURCES_KEY = "sources";
    private static final String TARGETS_KEY = "targets";
    private static final String FEATURES_KEY = "features";
    private static final String SUPPORTS_KEY = "supports";

    private static final String SAFE_LINK_KEY = "safeLinks";
    private static final String INITIAL_LINKS_KEY = "initialLinks";
    private static final String COMPUTED_LINKS_KEY = "computedLinks";
    private static final String CLUSTERS_KEY = "clusters";
    private static final String LINKS_KEY = "links";

    private static final String SOURCES_SOURCES_KEY = "sources-sources";
    private static final String SOURCES_TARGETS_KEY = "source-target";
    private static final String TARGETS_TARGETS_KEY = "targets-targets";

    private static final String SCENARIO_KEY = "scenario";
    private static final String SCENARIO_RC_RC_KEY = "scenario-rc-rc";
    private static final String SCENARIO_SRA_SRA_KEY = "scenario-ra-ra";
    private static final String OPTION_KEY = "options";


    private static final Logger logger = LoggerFactory.getLogger(FeatureCommand.class);

    @Parameter(names = {"-f", "--input"}, description = "Input file in JSON")
    private String input_filepath = "-";

    @Parameter(names = {"--nb-threads"}, description = "Number of threads to use.")
    private int nbThreads = Runtime.getRuntime().availableProcessors();

    private SudoqualCommander mainOptions;

    public AlignService() {
    }

    @Override
    public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
        this.mainOptions = options;

        LinkingModule module = this.mainOptions.getLinkingModule();
        LinkingModule toClose = null;

        try {
            if (module == null) {
                if (this.mainOptions.isVerbose()) {
                    err.println("Starting the linking module using " + this.nbThreads + " threads.");
                }
                toClose = LinkingModule.create(this.nbThreads);
                module = toClose;
                module.registerPath(this.mainOptions.getScenarioDir());
            }

            // run
            JSONObject result = null;
            try {
                InputStream is = (input_filepath.equals("-")) ? System.in : new FileInputStream(this.input_filepath);
                result = this.run(module, is, err);
            } catch (FileNotFoundException e) {
                err.println("Enable to read input file: " + this.input_filepath + ". An error occurred: ");
                e.printStackTrace(err);
                return 1;
            }

            if (result == null) {
                err.println("Something went wrong, no result produced.");
                return 1;
            }

            if (this.mainOptions.isVerbose()) {
                err.println("Displaying...");
            }

            CLIUtils.printResult(result, this.mainOptions.isPrettyPrintEnable(), out);
        } finally {
            if(toClose != null) {
                toClose.close();
            }
        }

        return 0;

    }

    private @Nullable
    JSONObject run(LinkingModule module, InputStream inputStream, PrintStream err) {
        JSONObject input = JSONObjects.from(inputStream, this.mainOptions.getCharset());
        LinkService link = new LinkService();
        link.setMainOptions(this.mainOptions);

        JSONObject result = null;
        try {
            if (this.mainOptions.isVerbose()) {
                err.println("Executing linking module...");
            }
            result = this.align(module, link, input, err);
        } catch (InterruptedException e) {
            err.println("The processus was interrupted.");
            return null;
        }

        return result;
    }

    private JSONObject align(LinkingModule module, LinkService cmd, JSONObject input, PrintStream err) throws InterruptedException {

        TargetTarget targetTarget = new TargetTarget(module, input, cmd, err);//SRA/SRA
        SourceSource sourceSource = new SourceSource(module, input, cmd, err);//RC/RC
        sourceSource.start();
        targetTarget.start();
        sourceSource.join();
        targetTarget.join();

        JSONObject res = new JSONObject();
        if( targetTarget.isDone.get() && sourceSource.isDone.get()) {
        	//logger.warn("target:" + targetTarget.getResult().toString(1));
    //    	logger.warn("source:" + sourceSource.getResult().toString(1));

        	JSONObject sourceSourceResult = sourceSource.getResult();

        	//Cluster RA
        	JSONObject clusterRAInput = new JSONObject();
            clusterRAInput.put(CLUSTERS_KEY, sourceSourceResult.get(CLUSTERS_KEY));
            clusterRAInput.put(LINKS_KEY, input.get(INITIAL_LINKS_KEY));
            JSONObject clusterRAResult = ClusterRaOverlap.INSTANCE.execute(clusterRAInput);

            //Transitivity
            JSONObject transitivityInput = new JSONObject();
            transitivityInput.append("looksForLinks", new JSONObject("{ 'from': 'source', 'to': 'target'}"));
            transitivityInput.put(SOURCES_KEY, input.get(SOURCES_KEY));
            transitivityInput.put(TARGETS_KEY, input.get(TARGETS_KEY));
            JSONArray array = new JSONArray();
            transitivityInput.put("links", array);
            JSONArrays.appendAllTo(input.optJSONArray(SAFE_LINK_KEY), array);
            JSONArrays.appendAllTo(clusterRAResult.get(COMPUTED_LINKS_KEY), array);
            JSONArrays.appendAllTo(sourceSourceResult.get(CLUSTERS_KEY), array);

            JSONObject transitivityResult = Transitivity.INSTANCE.execute(transitivityInput);

        	JSONArray links = Constraint.oneToOne(transitivityResult.getJSONArray(SOURCES_TARGETS_KEY), targetTarget.getResult().getJSONArray(COMPUTED_LINKS_KEY), "source", "target");

        	res.put(COMPUTED_LINKS_KEY, links);
        	res.put(CLUSTERS_KEY, sourceSourceResult.get(CLUSTERS_KEY));
            JSONObject metadata = targetTarget.getResult().optJSONObject("metadata");
            metadata.remove("scenario");
            metadata.put("service", "align");
        	res.put("metadata", metadata);


        } else {
            logger.warn("An error occured!");
        }

        return res;
    }

    private static abstract class Job extends Thread {
        final LinkingModule module;
        final JSONObject mainInput;
        final LinkService link;
        final PrintStream err;
        AtomicBoolean isDone;
        JSONObject result;
        boolean isDiagnosticEnabled = false;
        boolean isClusteringEnabled = false;

        public Job(LinkingModule module, JSONObject input, LinkService link, PrintStream err) {
            this.mainInput = input;
            this.link = link;
            this.module = module;
            this.err = err;
            this.isDone = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            JSONObject preparedInput = prepareInput(mainInput);
            this.result = this.link.link(module, preparedInput, err, isDiagnosticEnabled, isClusteringEnabled);
            //if (isClusteringEnabled)
            	//logger.warn("result : "+this.result.toString(1));
            this.isDone.set(true);
        }

        public JSONObject getResult() {
            if(isDone.get()) {
                return this.result;
            }
            return null;
        }

        protected abstract JSONObject prepareInput(JSONObject input);
    }


    //RC/RC
    private static class SourceSource extends Job {

        public SourceSource(LinkingModule module, JSONObject input, LinkService link, PrintStream err) {
            super(module, input, link, err);
            this.isClusteringEnabled = true;
        }

        protected JSONObject prepareInput(JSONObject input) {
            JSONObject res = new JSONObject();
            res.put(SCENARIO_KEY, input.get(SCENARIO_RC_RC_KEY));
            res.put(FEATURES_KEY, input.get(FEATURES_KEY));
            res.put(SOURCES_KEY, input.get(SUPPORTS_KEY));
            res.put(TARGETS_KEY, SOURCES_KEY);

            return res;
        }
    }


    //SRA/SRA
    private static class TargetTarget extends Job {

        public TargetTarget(LinkingModule module, JSONObject input, LinkService link, PrintStream err) {
            super(module, input, link, err);
        }

        protected JSONObject prepareInput(JSONObject input) {
        	JSONObject res = new JSONObject();
            res.put(SCENARIO_KEY, input.get(SCENARIO_SRA_SRA_KEY));
            res.put(FEATURES_KEY, input.get(FEATURES_KEY));
            res.put(SAFE_LINK_KEY, input.get(INITIAL_LINKS_KEY));
            res.put(SOURCES_KEY, input.get(SOURCES_KEY));
            res.put(TARGETS_KEY, input.get(TARGETS_KEY));
            res.put(SUPPORTS_KEY, input.get(SUPPORTS_KEY));

            return res;
        }
    }



}
