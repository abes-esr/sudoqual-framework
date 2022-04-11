package fr.abes.sudoqual.cli.services;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import fr.abes.sudoqual.cli.services.hidden.FeatureCommand;
import fr.abes.sudoqual.modules.clusterRaOverlap.ClusterRaOverlap;
import fr.abes.sudoqual.modules.constraint.Constraint;
import fr.abes.sudoqual.modules.diagnostic.Diagnostician;
import fr.abes.sudoqual.modules.diagnostic.exception.DiagnosticianException;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.modules.inconsistencies.Inconsistencies;
import fr.abes.sudoqual.modules.transitivity.Transitivity;
import fr.abes.sudoqual.util.json.JSONArrays;
import fr.abes.sudoqual.util.json.JSONObjects;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

@Parameters(commandDescription = "run complete")
public class CompleteService implements Service {
    public static final String NAME = "complete";
    private static final String SOURCES_KEY = "sources";
    private static final String TARGETS_KEY = "targets";
    private static final String FEATURES_KEY = "features";

    private static final String SAFE_LINK_KEY = "safeLinks";
    private static final String INITIAL_LINKS_KEY = "initialLinks";
    private static final String COMPUTED_LINKS_KEY = "computedLinks";
    private static final String CLUSTERS_KEY = "clusters";
    private static final String LINKS_KEY = "links";

    private static final String SOURCES_SOURCES_KEY = "sources-sources";
    private static final String SOURCES_TARGETS_KEY = "sources-targets";
    private static final String TARGETS_TARGETS_KEY = "targets-targets";

    private static final String SCENARIO_KEY = "scenarios";
    private static final String OPTION_KEY = "options";
    private static final String SUPPORT_KEY = "support";

    private static final Logger logger = LoggerFactory.getLogger(FeatureCommand.class);

    @Parameter(names = {"-f", "--input"}, description = "Input file in JSON")
    private String input_filepath = "-";

    @Parameter(names = {"--nb-threads"}, description = "Number of threads to use.")
    private int nbThreads = Runtime.getRuntime().availableProcessors();

    private SudoqualCommander mainOptions;

    public CompleteService() {
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
            result = this.complete(module, link, input, err);
        } catch (InterruptedException e) {
            err.println("The processus was interrupted.");
            return null;
        }

        return result;
    }

    private JSONObject complete(LinkingModule module, LinkService cmd, JSONObject input, PrintStream err) throws InterruptedException {
        SourceTarget sourceTarget = new SourceTarget(module, input, cmd, err);
        TargetTarget targetTarget = new TargetTarget(module, input, cmd, err);
        SourceSource sourceSource = new SourceSource(module, input, cmd, err);
        sourceTarget.start();
        sourceSource.start();
        targetTarget.start();
        sourceTarget.join();
        sourceSource.join();
        targetTarget.join();

        JSONObject res = new JSONObject();
        if(sourceTarget.isDone.get() && targetTarget.isDone.get() && sourceSource.isDone.get()) {
            JSONObject sourceTargetResult = sourceTarget.getResult(); // RC/RA
            JSONObject targetTargetResult = targetTarget.getResult(); // RA/RA
            JSONObject sourceSourceResult = sourceSource.getResult(); // RC/RC + cluster

            res.put("computedLinks-targets-targets", JSONArrays.copy(targetTargetResult.getJSONArray(COMPUTED_LINKS_KEY)));
            res.put("computedLinks-sources-sources", sourceSourceResult.getJSONArray(COMPUTED_LINKS_KEY));
            res.put("clusters", JSONArrays.copy(sourceSourceResult.getJSONArray(CLUSTERS_KEY)));

            // cluster/Ra Overlap
            JSONObject clusterRAInput = new JSONObject();
            clusterRAInput.put(CLUSTERS_KEY, sourceSourceResult.get(CLUSTERS_KEY));
            clusterRAInput.put(LINKS_KEY, sourceTargetResult.get(COMPUTED_LINKS_KEY));
            JSONObject clusterRAResult = ClusterRaOverlap.INSTANCE.execute(clusterRAInput);

            // transitivity
            JSONObject transitivityInput = new JSONObject();
            transitivityInput.append("looksForLinks", new JSONObject("{ 'from': 'target', 'to': 'target'}"));
            transitivityInput.append("looksForLinks", new JSONObject("{ 'from': 'source', 'to': 'target'}"));
            transitivityInput.put(SOURCES_KEY, input.get(SOURCES_KEY));
            transitivityInput.put(TARGETS_KEY, input.get(TARGETS_KEY));
            JSONArray array = new JSONArray();
            transitivityInput.put("links", array);
            JSONArrays.appendAllTo(sourceTargetResult.get(COMPUTED_LINKS_KEY), array);
            JSONArrays.appendAllTo(clusterRAResult.get(COMPUTED_LINKS_KEY), array);
            JSONArrays.appendAllTo(sourceSourceResult.get(CLUSTERS_KEY), array);
            JSONObject transitivityResult = Transitivity.INSTANCE.execute(transitivityInput);

            // constraint RC/RA many-to-one
            JSONArray linksSourcesTargets = Constraint.manyToOne(transitivityResult.getJSONArray("source-target"), sourceTargetResult.getJSONArray(COMPUTED_LINKS_KEY), "source", "target");
            res.put("computedLinks-sources-targets", linksSourcesTargets);

            // Diagnostic RC/RA many-to-one
            JSONObject diagSourceInput = new JSONObject();
            diagSourceInput.put("sources", input.get("sources"));
            diagSourceInput.put("targets", input.get("targets"));
            diagSourceInput.put("initialLinks", input.get("initialLinks"));
            diagSourceInput.put("computedLinks", linksSourcesTargets);
            try {
                res.put("diagnosticSources", Diagnostician.createManyToOneDiagnostician().execute(diagSourceInput).get("diagnostic"));
            } catch (DiagnosticianException e) {
                // TODO
                throw new RuntimeException("Exception not yet currently handled !!!!!!!!!!");
            }

            // mergeRA
            mergeJSONArraysOfLinks(transitivityResult.getJSONArray("target-target"), res.getJSONArray("computedLinks-targets-targets"));

            // inconsistencies
            JSONObject inconsistenciesEntry = new JSONObject();
            JSONArray inconsistenciesArrayEntry = new JSONArray();
            JSONArrays.appendAllTo(res.getJSONArray("computedLinks-targets-targets"), inconsistenciesArrayEntry);
            JSONArrays.appendAllTo(res.getJSONArray("computedLinks-sources-targets"), inconsistenciesArrayEntry);
            inconsistenciesEntry.put("computedLinks", inconsistenciesArrayEntry);
            JSONObject inconsistenciesResult = Inconsistencies.INSTANCE.execute(inconsistenciesEntry);
            res.put("inconsistencies", inconsistenciesResult.get("inconsistencies"));
            JSONObject metadata = sourceTarget.getResult().optJSONObject("metadata");
            metadata.remove("scenario");
            metadata.put("service", "complete");
            res.put("metadata", metadata);

        } else {
            logger.warn("An error occured!");
        }

        return res;
    }

    private static void mergeJSONArraysOfLinks(JSONArray from, JSONArray to) {
        JSONArrays.mergeIn(from, to, instanceLinkJSONObjetEqualityChecker, instanceLinkJSONObjectMerger);
    }

    private static LinkJSONObjetEqualityChecker instanceLinkJSONObjetEqualityChecker = new LinkJSONObjetEqualityChecker();
    private static class LinkJSONObjetEqualityChecker implements BiPredicate<Object, Object> {
        @Override
        public boolean test(Object o1, Object o2) {
            if(!(o1 instanceof JSONObject) || !(o2 instanceof JSONObject)) {
                return false;
            }
            return Objects.equals(
                ((JSONObject) o1).optString("source"),
                ((JSONObject) o2).optString("source"))
                && Objects.equals(
                ((JSONObject) o1).optString("target"),
                ((JSONObject) o2).optString("target"));
        }
    }

    private static LinkJSONObjectMerger instanceLinkJSONObjectMerger = new LinkJSONObjectMerger();
    private static class LinkJSONObjectMerger implements BinaryOperator<Object> {

        @Override
        public Object apply(Object o1, Object o2) {
            if(!(o1 instanceof JSONObject) && !(o2 instanceof JSONObject)) {
                return null;
            } else if(!(o1 instanceof JSONObject)) {
                return JSONObjects.copy((JSONObject) o2);
            } else if(!(o2 instanceof JSONObject)) {
                return JSONObjects.copy((JSONObject) o1);
            }

            JSONObject toMerge = (JSONObject) o2;
            JSONObject res = JSONObjects.copy((JSONObject) o1);
            for(String key : toMerge.keySet()) {
                if(res.has(key)) {
                    switch(key) {
                        case "confidence":
                            res.put(key, Math.max(res.getInt(key), toMerge.getInt(key)));
                            break;
                        case "why":
                            res.put("why", this.apply(res.opt("why"), toMerge.opt("why")));
                            break;
                    }
                } else {
                    res.put(key, toMerge.get(key));
                }
            }
            return res;
        }
    }

    private static abstract class LinkingModuleJob extends Thread {
        final LinkingModule module;
        final JSONObject mainInput;
        final LinkService link;
        final PrintStream err;
        AtomicBoolean isDone;
        JSONObject result;
        boolean isDiagnosticEnabled = false;
        boolean isClusteringEnabled = false;

        public LinkingModuleJob(LinkingModule module, JSONObject input, LinkService link, PrintStream err) {
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
            JSONObjects.rename(this.result, "computedLinks.why", "computedLinks.why.link");
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

    private static class SourceTarget extends LinkingModuleJob {

        public SourceTarget(LinkingModule module, JSONObject input, LinkService link, PrintStream err) {
           super(module, input, link, err);
        }

        protected JSONObject prepareInput(JSONObject input) {
            JSONObject res = new JSONObject();
            res.put(SOURCES_KEY, input.get(SOURCES_KEY));
            res.put(TARGETS_KEY, input.get(TARGETS_KEY));
            res.put(FEATURES_KEY, input.get(FEATURES_KEY));
            res.put(INITIAL_LINKS_KEY, input.get(INITIAL_LINKS_KEY));

            JSONObjects.mergeIn(input.getJSONObject(SOURCES_TARGETS_KEY), res);
            return res;
        }
    }

    private static class SourceSource extends LinkingModuleJob {

        public SourceSource(LinkingModule module, JSONObject input, LinkService link, PrintStream err) {
            super(module, input, link, err);
            this.isClusteringEnabled = true;
        }

        protected JSONObject prepareInput(JSONObject input) {
            JSONObject res = new JSONObject();
            res.put(SOURCES_KEY, input.get(SOURCES_KEY));
            res.put(TARGETS_KEY, SOURCES_KEY);
            res.put(FEATURES_KEY, input.get(FEATURES_KEY));
            res.put(INITIAL_LINKS_KEY, input.get(INITIAL_LINKS_KEY));

            JSONObjects.mergeIn(input.getJSONObject(SOURCES_SOURCES_KEY), res);
            return res;
        }
    }

    private static class TargetTarget extends LinkingModuleJob {

        public TargetTarget(LinkingModule module, JSONObject input, LinkService link, PrintStream err) {
            super(module, input, link, err);
        }

        protected JSONObject prepareInput(JSONObject input) {
            JSONObject res = new JSONObject();
            res.put(SOURCES_KEY, input.get(TARGETS_KEY));
            res.put(TARGETS_KEY, SOURCES_KEY);
            res.put(FEATURES_KEY, input.get(FEATURES_KEY));
            res.put(INITIAL_LINKS_KEY, input.get(INITIAL_LINKS_KEY));

            JSONObjects.mergeIn(input.getJSONObject(TARGETS_TARGETS_KEY), res);
            return res;
        }
    }



}
