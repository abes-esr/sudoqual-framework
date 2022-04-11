package fr.abes.sudoqual.cli.services;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.abes.sudoqual.cli.SudoqualCommander;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Retrieve validation schema for a given scenario")
public class SchemaService implements Service {
	public static final String NAME = "schema";

    @Parameter(description = "<scenario-name>", arity = 1)
    private List<String> parameters = new ArrayList<>();

    public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
        LinkingModule module = options.getLinkingModule();
        LinkingModule toClose = null;

        try {
            if(module == null) {
                toClose = LinkingModule.create(1);
                module = toClose;
                module.registerPath(options.getScenarioDir());
            }

            out.println(module.generateSchema(parameters.get(0)).toString(options.isPrettyPrintEnable()? 1 : 0));
        } catch (LinkingModuleException e) {
            err.println("An error occurs during LinkingModule execution. See details below:");
            e.printStackTrace(err);
            return 1;
        } finally {
            if(toClose != null) {
                toClose.close();
            }
        }

        return 0;
	}



}
