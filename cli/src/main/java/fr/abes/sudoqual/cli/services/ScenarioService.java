package fr.abes.sudoqual.cli.services;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.abes.sudoqual.cli.SudoqualCommander;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Retrieve scenario names and show needed attributes for each")
public class ScenarioService implements Service {
	public static final String NAME = "scenario";

    @Parameter(description = "<scenario-name>")
    private String scenario = null;

    public int run(SudoqualCommander options, PrintStream out, PrintStream err) {
		if(scenario == null) {
            for(String scenario : listAvailableScenarios(options.getScenarioDir(), err)) {
                out.println(scenario);
            }
        } else {
            LinkingModule module = null;
            try {
                module = LinkingModule.create(1);
                module.registerPath(options.getScenarioDir());
                for(String scenario : module.listFeatures(scenario)) {
                    out.println(scenario);
                }
            } catch (LinkingModuleException e) {
                err.println("An error occured during scenario feature listing: ");
                e.printStackTrace(err);
            } finally {
                if(module != null) {
                    module.close();
                }
            }
        }
		return 0;
	}


    public static List<String> listAvailableScenarios(String scenarioDirPath, PrintStream err) {
        try {
            URL url = ResourceUtils.getResource(ScenarioService.class, scenarioDirPath, "application.properties");
            Properties properties = new Properties();
            try (InputStream stream = url.openStream()) {
                properties.load(stream);
            }
            return Arrays.stream(properties.getProperty("scenarios").split(";")).map(s -> s.trim()).collect(Collectors.toList());
        } catch (ResourceNotFoundException | IOException e) {
            err.println("Unable to list available scenarios: application.properties not found: ");
            e.printStackTrace(err);
            return Collections.emptyList();
        }
    }


}
