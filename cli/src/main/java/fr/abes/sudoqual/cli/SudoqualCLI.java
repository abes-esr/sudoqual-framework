package fr.abes.sudoqual.cli;

public class SudoqualCLI {
	public static void run(SudoqualConfig config, String args[]) {
		new SudoqualCommander(config).run(args);
	}
}
