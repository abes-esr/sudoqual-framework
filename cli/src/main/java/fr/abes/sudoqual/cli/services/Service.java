package fr.abes.sudoqual.cli.services;

import fr.abes.sudoqual.cli.SudoqualCommander;

import java.io.PrintStream;

public interface Service {
    int run(SudoqualCommander options, PrintStream out, PrintStream err);
}
