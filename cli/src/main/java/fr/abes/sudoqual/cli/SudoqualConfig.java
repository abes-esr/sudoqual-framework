package fr.abes.sudoqual.cli;

import java.nio.charset.Charset;

public interface SudoqualConfig {
	
	public String getScenarioDir();
	
	public Charset getCharset();
	
	public void setCharset(Charset charset);

}
