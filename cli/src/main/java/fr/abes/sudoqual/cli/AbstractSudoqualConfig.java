package fr.abes.sudoqual.cli;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public abstract class AbstractSudoqualConfig implements SudoqualConfig {
	
	private Charset charset = StandardCharsets.UTF_8;
	
	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public void setCharset(Charset charset) {
		this.charset = charset;
	}


}
