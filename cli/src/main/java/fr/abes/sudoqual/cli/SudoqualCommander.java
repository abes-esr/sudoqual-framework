package fr.abes.sudoqual.cli;

import java.io.PrintStream;
import java.nio.charset.Charset;

import fr.abes.sudoqual.cli.services.*;
import fr.abes.sudoqual.cli.services.hidden.FeatureCommand;
import fr.abes.sudoqual.cli.services.hidden.PredicateCommand;
import fr.abes.sudoqual.linking_module.LinkingModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import ch.qos.logback.classic.Level;



public class SudoqualCommander {
	private static final Logger logger = LoggerFactory.getLogger(SudoqualCommander.class);

	public static final String PROGRAM_NAME = SudoqualCommander.class.getSimpleName();
	private static final String CMD_USAGE = "java -jar sudoqual-cli.jar [main-options] command [command-options]";

	@Parameter(names = { "--scenario-dir"}, description = "Specify directory path where find scenario files.")
	private String scenarioDir = null;
	public String getScenarioDir() {
		return this.scenarioDir;
	}

	@Parameter(names = { "--charset"}, description = "Specify charset to use for read input files")
	private String charsetName;
	public Charset getCharset() {
		return Charset.forName(charsetName);
	}

	@Parameter(names = { "--no-pretty-print" }, description = "Disable pretty print.")
	private boolean isPrettyPrintEnable = true;
	public boolean isPrettyPrintEnable() {
		return this.isPrettyPrintEnable;
	}

	@Parameter(names = { "--log-level" }, description = "Set the logging level. Possible values: TRACE, DEBUG, INFO, WARN, ERROR, ALL and OFF.")
	private String logLevel = "WARN";

	@Parameter(names = { "-v", "--verbose" }, description = "Enable verbose mode")
	private boolean verbose = false;
	public boolean isVerbose() {
		return this.verbose;
	}

	@Parameter(names = { "-h", "--help" }, description = "Print this message", help = true)
	private boolean help;

	@Parameter(names = { "-V", "--version" }, description = "Print version information", help = true)
	private boolean version = false;

	private LinkingModule linkingModule = null;
	public LinkingModule getLinkingModule() {
		return this.linkingModule;
	}
	public void setLinkingModule(LinkingModule m) {
	    this.linkingModule = m;
    }

	public SudoqualCommander(SudoqualConfig config) {
	    this.scenarioDir = config.getScenarioDir();
	    this.charsetName = config.getCharset().displayName();
	}

	public SudoqualCommander(SudoqualConfig config, LinkingModule linkModule, boolean prettyPrint) {
	    this(config);
		this.isPrettyPrintEnable = prettyPrint;
		this.linkingModule = linkModule;
	}

	public int run(String args[]) {
		return run(args, null, null, true);
	}

	public int run(String args[], PrintStream out, PrintStream err) {
		return run(args, out, err, false);
	}

	private int run(String args[], PrintStream out, PrintStream err, boolean fromCLI) {
        if (out == null) {
            out = System.out;
        }
        if (err == null) {
            err = System.err;
        }

        try {
            LinkService linkCmd = new LinkService();
            CompareService cmpCmd = new CompareService();
            EvalService evalCmd = new EvalService();
            DiagnosticService diagCmd = new DiagnosticService();
            ClusteringService clusterCmd = new ClusteringService();
            CompleteService compCmd = new CompleteService();
            AlignService alignCmd = new AlignService();

            FeatureCommand featCmd = new FeatureCommand();
            PredicateCommand predCmd = new PredicateCommand();
            ScenarioService scenCmd = new ScenarioService();
            SchemaService schemaCmd = new SchemaService();

            JCommander commander = new JCommander(this);
            commander.setProgramName("");

            commander.addCommand(LinkService.NAME, linkCmd);
            commander.addCommand(CompareService.NAME, cmpCmd);
            commander.addCommand(EvalService.NAME, evalCmd);
            commander.addCommand(DiagnosticService.NAME, diagCmd);
            commander.addCommand(ClusteringService.NAME, clusterCmd);
            commander.addCommand(CompleteService.NAME, compCmd);
            commander.addCommand(AlignService.NAME,alignCmd);
            commander.addCommand(ScenarioService.NAME, scenCmd);
            commander.addCommand(SchemaService.NAME, schemaCmd);

            commander.addCommand(FeatureCommand.NAME, featCmd);
            commander.addCommand(PredicateCommand.NAME, predCmd);


            try {
                commander.parse(args);
            } catch (ParameterException e) {
                err.println("\nError: " + e.getMessage() + "\n");
                out.println(CMD_USAGE);
                commander.usage();
                return 1;
            }

            if (fromCLI) {
                this.setLoggingLevel();
            }

            if (this.help) {
                this.printHelp(commander, out);
                return 0;
            }

            if (this.version) {
                CLIUtils.printVersion(PROGRAM_NAME, out);
                return 0;
            }


            try {
                String command = commander.getParsedCommand();
                if (command == null) {
                    err.println("\nError: no command specified.\n");
                    out.println(CMD_USAGE);
                    commander.usage();
                    return 1;
                } else {
                    switch (command) {
                        case LinkService.NAME:
                            return linkCmd.run(this, out, err);
                        case CompareService.NAME:
                            return cmpCmd.run(this, out, err);
                        case EvalService.NAME:
                            return evalCmd.run(this, out, err);
                        case DiagnosticService.NAME:
                            return diagCmd.run(this, out, err);
                        case ClusteringService.NAME:
                            return clusterCmd.run(this, out, err);
                        case CompleteService.NAME:
                            return compCmd.run(this, out, err);
                        case AlignService.NAME:
                            return alignCmd.run(this, out, err);
                        case ScenarioService.NAME:
                            return scenCmd.run(this, out, err);
                        case SchemaService.NAME:
                            return schemaCmd.run(this, out, err);
                        case FeatureCommand.NAME:
                            return featCmd.run(this);
                        case PredicateCommand.NAME:
                            return predCmd.run(this);
                        default:
                            err.println("The command " + command + " was not recognized.");
                            printHelp(commander, out);
                            return 1;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(err);
                return 1;
            }
        } finally {
	        out.flush();
	        err.flush();
        }
	}

	private void printHelp(JCommander commander, PrintStream out) {
		CLIUtils.printHeader(PROGRAM_NAME, 10, out);
		out.println();
		out.println(CMD_USAGE);
		commander.usage();
	}

	private void setLoggingLevel() {
		 final Level level = Level.toLevel(this.logLevel, null);
		 if(level != null) {
			 ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root")).setLevel(level);
		 } else {
			 logger.warn("The provided log level was not recognized: {}. The default configuration was kept.", this.logLevel);
		 }
	}
}
