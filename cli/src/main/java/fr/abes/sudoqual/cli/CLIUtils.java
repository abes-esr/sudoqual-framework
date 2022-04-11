package fr.abes.sudoqual.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.BusinessClassLoader;
import fr.abes.sudoqual.linking_module.exception.BusinessClassException;
import fr.abes.sudoqual.linking_module.impl.BusinessClassLoaderHelper;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.impl.FeatureManagerImpl;
import fr.abes.sudoqual.rule_engine.impl.ReferenceImpl;
import fr.abes.sudoqual.util.json.JSONObjects;

public final class CLIUtils {

	private CLIUtils() {

	}

	public static void printVersion(String applicationName, PrintStream out) {

		String conf = null;
		String version = null;
		String vendor = null;
		String buildDate = null;

		// GET DATA
		try {
			Manifest manifest;
			URL pathToManifest = CLIUtils.class.getResource("/META-INF/MANIFEST.MF");
			if(pathToManifest != null) {
    			try(InputStream is = pathToManifest.openStream()) {
    				manifest = new Manifest(is);
    			}
    			Attributes att = manifest.getMainAttributes();

    			conf = att.getValue("Specification-Title");
    			version = att.getValue("Specification-Version");
    			vendor = att.getValue("Specification-Vendor");
    			buildDate = att.getValue("Built-On");
			}
		} catch (IOException e) {
			e.printStackTrace(out);
	    }

		// PRINT DATA
		if(applicationName != null) {
			printHeader(applicationName, 10, out);
		}

		out.print("Configuration: ");
		out.println((conf != null)? conf : "?");

		out.print("Version: ");
		out.println((version != null)? version : "?");

		out.print("Built on: ");
		out.println((buildDate != null)? buildDate : "?");

		out.print("Produced by: ");
		out.println((vendor != null)? vendor : "?");
	}

	public static void printHeader(String title, int borderSize, PrintStream out) {
		int len = title.length() + borderSize*2;

		// create line
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < len; ++i) {
			sb.append('=');
		}
		String line = sb.toString();

		// create border blanks
		sb = new StringBuilder();
		for(int i = 0; i < borderSize - 1; ++i) {
			sb.append(' ');
		}
		String blanks = sb.toString();

		out.println(line);
		out.print("=");
		out.print(blanks);
		out.print(title);
		out.print(blanks);
		out.println("=");
		out.println(line);
	}


	public static void printResult(JSONObject resultJSON, boolean prettyPrint, PrintStream out) {
		if(prettyPrint) {
			out.println(JSONObjects.prettyPrint(resultJSON, 4));
		} else {
			out.println(resultJSON);
		}
	}

	public static @Nullable JSONObject readInputJSON(String input_filepath, Charset charset, PrintStream err) {
		JSONObject inputJSON = (input_filepath.equals("-"))?
			JSONObjects.from(System.in, charset)
			: readJSONFromFile(input_filepath, charset, err);

		return inputJSON;
	}

	public static JSONObject readJSONFromFile(String path, Charset charset, PrintStream err) {
		return readJSONFromFile(new File(path), charset, err);
	}

	public static JSONObject readJSONFromFile(File f, Charset charset, PrintStream err) {
		try {
			return JSONObjects.from(f, charset);
		} catch (JSONException e) {
			err.println("Error when trying to parse the following file as JSON: " + f.getPath());
			e.printStackTrace(err);
		} catch (IOException e) {
			err.println("Error when trying to read: " + f.getPath());
			e.printStackTrace(err);
		}
		return null;
	}

	public static JSONArray removeAllSourceFromSafeLinks(JSONArray jsonArray, @Nullable JSONArray links) {
		if(links == null) {
			return jsonArray;
		}
		Collection<Object> result = new LinkedList<>();
		for(Object o : jsonArray) {
			result.add(o);
		}

		for(Object o : links) {
			JSONObject l = (JSONObject) o;
			result.remove(l.getString("source"));
		}

		return new JSONArray(result);
	}

	public static Collection<JSONObject> prepareFeature(BusinessClassLoader loader, ComputedFeature<?> feat, JSONArray input) throws Exception {
		// create feature map
		Map<String, Feature> featureMap = null;
		try {
			featureMap = BusinessClassLoaderHelper.loadFeaturesFromNames(loader, feat.getRelatedFeatures(), true);
		} catch (BusinessClassException e) {
			throw new Exception("An error occured when loading features associated to the predicate: ", e);
		}
		assert featureMap != null;

		// create main JSON Object
		Collection<String> relatedFeatures = feat.getRelatedFeatures();
		boolean isUnique = relatedFeatures.size() == 1;
		String uniqueFeat = (isUnique)? relatedFeatures.iterator().next() : null;

		Collection<Reference> refs = new LinkedList<>();
		int i = 0;
		JSONObject json = new JSONObject();
		for(Object o : input) {
			Reference ref = new ReferenceImpl("ref" + ++i);
			refs.add(ref);
			if(o instanceof JSONObject) {
				json.put(ref.getName(), o);
			} else if(isUnique) {
				JSONObject tmp = new JSONObject();
				tmp.put(uniqueFeat, o);
				json.put(ref.getName(), tmp);
			} else {
				throw new Exception("An error occured when parsing data: JSONArray of JSONObject expected.");
			}
		}

		// create feature manager
		FeatureManagerImpl manager = new FeatureManagerImpl(json, featureMap);
		return manager.getFeaturesValue(refs, relatedFeatures);
	}
}
