package fr.abes.sudoqual.api;

import fr.abes.sudoqual.api.exception.SudoqualModuleException;
import fr.abes.sudoqual.util.Strings;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface SudoqualModule {

    default String execute(String input) throws SudoqualModuleException, InterruptedException {
        assert input != null;

        return this.execute(Strings.toInputStream(input, StandardCharsets.UTF_16), StandardCharsets.UTF_16);
    }

    default String execute(InputStream input, Charset charset) throws SudoqualModuleException, InterruptedException {
        assert input != null;

        JSONObject res = this.execute(new JSONObject(new JSONTokener(new InputStreamReader(input, charset))));
        return res.toString();
    }

    JSONObject execute(JSONObject input) throws SudoqualModuleException, InterruptedException;

}
