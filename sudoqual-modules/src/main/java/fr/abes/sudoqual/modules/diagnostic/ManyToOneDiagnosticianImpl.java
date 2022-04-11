/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.modules.diagnostic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.modules.diagnostic.exception.DiagnosticianException;
import fr.abes.sudoqual.modules.diagnostic.exception.JSONInputValidationException;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;
import fr.abes.sudoqual.util.Strings;
import fr.abes.sudoqual.util.json.JSONValidationReport;
import fr.abes.sudoqual.util.json.JSONValidator;

/**
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class ManyToOneDiagnosticianImpl implements Diagnostician {

	private static final Logger logger = LoggerFactory.getLogger(ManyToOneDiagnosticianImpl.class);

	private static final String SOURCES_KEY = "sources";
	private static final String TARGETS_KEY = "targets";

	private static final String INIT_LINKS_KEY = "initialLinks";
	private static final String COMPUTED_LINKS_KEY = "computedLinks";
	private static final String LINK_TYPE_KEY = "type";
	private static final String SOURCE_KEY = "source";
	private static final String TARGET_KEY = "target";
	private static final String WHY_KEY = "why";
	private static final String CONFIDENCE_KEY = "confidence";
	private static final String INIT_LINK_KEY = "initialLink";
	private static final String COMPUTED_LINK_KEY = "computedLink";
	private static final String SUGGESTED_LINKS_KEY = "suggestedLinks";
	private static final String IMPOSSIBLE_LINKS_KEY = "impossibleLinks";
	private static final String CASE_KEY = "case";
	private static final String STATUS_KEY = "status";

	private static final String SAME_AS = "sameAs";
	private static final String DIFF_FROM = "diffFrom";
	private static final String SUGGESTED_SAME_AS = "suggestedSameAs";


	private final JSONValidator inputValidator;
	private final JSONValidator outputValidator;

	// /////////////////////////////////////////////////////////////////////////
	//	CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////

	public ManyToOneDiagnosticianImpl() throws DiagnosticianException {
		try {
			URL inputFile = ResourceUtils.getResource(this.getClass(), Config.RESOURCE_DIR, "diagnostic/schema-input.json");
    		JSONObject rawInputSchema = new JSONObject(new JSONTokener(new InputStreamReader(inputFile.openStream(), Config.CHARSET)));
    		this.inputValidator = JSONValidator.instance(rawInputSchema);

			URL outputFile = ResourceUtils.getResource(this.getClass(), Config.RESOURCE_DIR, "diagnostic/schema-output.json");
    		JSONObject rawOutputSchema = new JSONObject(new JSONTokener(new InputStreamReader(outputFile.openStream(), Config.CHARSET)));
    		this.outputValidator = JSONValidator.instance(rawOutputSchema);
		} catch (ResourceNotFoundException | IOException e) {
			throw new DiagnosticianException("Input or output validation schema not found", e);
		} catch (JSONException e) {
			throw new DiagnosticianException("Input or output validation schema incorrect", e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public String execute(String input) throws DiagnosticianException {
		assert input != null;

		return this.execute(Strings.toInputStream(input, StandardCharsets.UTF_16), StandardCharsets.UTF_16);
	}

	@Override
	public String execute(InputStream input, Charset charset) throws DiagnosticianException {
		assert input != null;

		JSONObject res = this.execute(new JSONObject(new JSONTokener(new InputStreamReader(input, charset))));
		return res.toString();
	}

	@Override
	public JSONObject execute(JSONObject input) throws DiagnosticianException {
		assert input != null;

		JSONValidationReport report = inputValidator.validate(input);
		if (!report.isSuccess()) {
			logger.info("invalid input: {}", report);
			throw new JSONInputValidationException("The input does not fulfill the requirements:\n"
			                                       + report.toString());
		}

		JSONObject res = process(input);
		assert outputValidator.validate(res).isSuccess() :
			"Produced JSON output does not pass validation:" + outputValidator.validate(res).getErrorMessage();
		return res;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	private static JSONObject process(JSONObject input) throws DiagnosticianException {

		Map<String, String> mapAL = new HashMap<>();
		Map<String, TargetWithWhy> mapCL = new HashMap<>();
		Map<String, List<TargetWithWhy>> mapSL = new HashMap<>();
		Map<String, List<TargetWithWhy>> mapIL = new HashMap<>();

		fulfillMaps(input, mapAL, mapCL, mapSL, mapIL);

		JSONArray array = diagnostic(input.getJSONArray(SOURCES_KEY), input.getJSONArray(TARGETS_KEY), mapAL, mapCL, mapSL, mapIL);
		JSONObject res = new JSONObject();
		res.put("diagnostic", array);
		return res;
	}

	private static void fulfillMaps(JSONObject input, Map<String, String> mapAL, Map<String, TargetWithWhy> mapCL,
	    Map<String, List<TargetWithWhy>> mapSL, Map<String, List<TargetWithWhy>> mapIL) throws DiagnosticianException {

		fulfillWithInitialLinks(input.getJSONArray(INIT_LINKS_KEY), mapAL, mapIL);
		fulfillWithComputedLinks(input.getJSONArray(COMPUTED_LINKS_KEY), mapCL, mapSL, mapIL);
	}

	private static void fulfillWithInitialLinks(JSONArray initialLinks, Map<String, String> mapAL, Map<String, List<TargetWithWhy>> mapIL) throws DiagnosticianException {
		for(Object o : initialLinks) {
			assert o instanceof JSONObject;
			JSONObject link = (JSONObject) o;
			String type = link.getString(LINK_TYPE_KEY);
			String source = link.getString(SOURCE_KEY);
			String target = link.getString(TARGET_KEY);
			switch(type) {
				case SAME_AS:
					String oldValue = mapAL.put(source, target);
					if(oldValue != null && oldValue != target) {
						throw new DiagnosticianException("Multiple initial sameAs links for the same source: " + source);
					}
					break;
				case DIFF_FROM:
					// do nothing
					break;
				default:
					throw new DiagnosticianException("Wrong link type : " +type);
			}
		}
	}


	private static void fulfillWithComputedLinks(JSONArray computedLinks, Map<String, TargetWithWhy> mapCL,
	    Map<String, List<TargetWithWhy>> mapSL, Map<String, List<TargetWithWhy>> mapIL) throws DiagnosticianException {
		for(Object o : computedLinks) {
			assert o instanceof JSONObject;
			JSONObject link = (JSONObject) o;
			String type = link.getString(LINK_TYPE_KEY);
			String source = link.getString(SOURCE_KEY);
			String target = link.getString(TARGET_KEY);
			Integer confidence = link.optInt(CONFIDENCE_KEY);
			JSONObject why = link.optJSONObject(WHY_KEY);
			switch(type) {
				case SAME_AS:
					TargetWithWhy oldValue = mapCL.put(source, TargetWithWhy.instance(target, confidence, why));
					if(oldValue != null && !oldValue.getTarget().equals(target)) {
						throw new DiagnosticianException("Multiple computed sameAs links for the same source: " + source);
					}
					break;
				case SUGGESTED_SAME_AS:
					List<TargetWithWhy> listSL = mapSL.get(source);
					if(listSL == null) {
						listSL = new LinkedList<>();
						mapSL.put(source, listSL);
					}
					listSL.add(TargetWithWhy.instance(target, confidence, why));
					break;
				case DIFF_FROM:
					List<TargetWithWhy> listIL = mapIL.get(source);
					if(listIL == null) {
						listIL = new LinkedList<>();
						mapIL.put(source, listIL);
					}
					listIL.add(TargetWithWhy.instance(target, confidence, why));
					break;
				default:
					throw new DiagnosticianException("Wrong link type : " +type);
			}
		}

	}

	private static JSONArray diagnostic(JSONArray sources, JSONArray targets, Map<String, String> mapAL,
	    Map<String, TargetWithWhy> mapCL, Map<String, List<TargetWithWhy>> mapSL, Map<String, List<TargetWithWhy>> mapIL) {

		JSONArray res = new JSONArray();

		List<String> allCandidates = new LinkedList<String>();
		for(Object o : targets) {
			assert o instanceof String;
			allCandidates.add((String)o);
		}

		for(Object o : sources) {
			assert o instanceof String;
			String source = (String) o;
			String al = mapAL.get(source);
			TargetWithWhy cl = mapCL.get(source);
			List<TargetWithWhy> il = mapIL.get(source);
			List<TargetWithWhy> sl = mapSL.get(source);

			List<String> cxa = new LinkedList<>(allCandidates);
			if(il != null) {
				for(TargetWithWhy t : il) {
					cxa.remove(t.getTarget());
				}
			}

			List<String> sl2 = null;
			if(sl != null) {
				sl2 = sl.stream().map(e -> e.getTarget()).collect(Collectors.toList());
			}
			Diagnostic diagnostic = getDiagnostic(al, (cl != null)? cl.getTarget(): null, sl2, cxa);
			res.put(createReport(diagnostic, source, al, cl, sl, il));
		}
		return res;
	}


	/**
    *
    * @param uriAL the URI of the initial link
    * @param uriCL the URI of the computed link
    * @param uriSLs a List of URI of suggested links
    * @param uriCXAs a List of URI of acceptable links
    * @return
    */
   public static Diagnostic getDiagnostic(String uriAL, String uriCL, List<String> uriSLs, List<String> uriCXAs) {
       Diagnostic diag = Diagnostic.CASE_INVALID;
       if (uriAL != null && !uriAL.isEmpty()) {	// il y a un lien asserte
           if (uriCL != null && !uriCL.isEmpty()) { // il y a un lien calcule
               if (uriAL.equals(uriCL)) {
                   diag = Diagnostic.CASE_1;
               } else {
                   diag = Diagnostic.CASE_2;
               }
           } else if (uriCXAs != null && uriCXAs.contains(uriAL)) { // le lien asserte est encore possible (i.e. LA n'appartient pas a LI)
               if (uriSLs == null || uriSLs.isEmpty()) {
                   diag = Diagnostic.CASE_7;
               } else if (uriSLs != null && uriSLs.contains(uriAL)) {
                   diag = Diagnostic.CASE_6;
               } else {
                   diag = Diagnostic.CASE_8;
               }
           } else // al est dans les interdits
           {
               if (uriCXAs == null || uriCXAs.isEmpty()) {
                   diag = Diagnostic.CASE_4;
               } else // il reste des candidats
               {
                   if (uriSLs != null && !uriSLs.isEmpty()) {
                       diag = Diagnostic.CASE_3;
                   } else {
                       diag = Diagnostic.CASE_5;
                   }
               }
           }
       } else // pas de lien asserte
       {
           if (uriCL != null && !uriCL.isEmpty()) {
               diag = Diagnostic.CASE_9;
           } else if (uriCXAs == null || uriCXAs.isEmpty()) {
               diag = Diagnostic.CASE_10;
           } else if (uriSLs == null || uriSLs.isEmpty()) {
               diag = Diagnostic.CASE_11;
           } else {
               diag = Diagnostic.CASE_12;
           }
       }
       return diag;
   }


	private static JSONObject createReport(Diagnostic diagnostic, String source, String al, TargetWithWhy cl, List<TargetWithWhy> sl, List<TargetWithWhy> il) {
		JSONObject res = new JSONObject();
		res.put(SOURCE_KEY, source);
		res.put(CASE_KEY, diagnostic.ordinal());
		res.put(STATUS_KEY, diagnostic.getStatus().toString());
		if(al != null) {
			res.put(INIT_LINK_KEY, al);
		}
		if(cl != null) {
			res.put(COMPUTED_LINK_KEY, toJSONObject(cl));
		}
		if(sl != null && !sl.isEmpty()) {
			res.put(SUGGESTED_LINKS_KEY, toJSONArray(sl));
		}
		if(il != null && !il.isEmpty()) {
			res.put(IMPOSSIBLE_LINKS_KEY, toJSONArray(il));
		}
		return res;
	}

	private static JSONObject toJSONObject(TargetWithWhy tww) {
		JSONObject object = new JSONObject();
		object.put(TARGET_KEY, tww.getTarget());
		if(tww.getWhy() != null) {
			object.put(WHY_KEY, tww.getWhy());
		}
		if(tww.getConfidence() != 0) {
			object.put(CONFIDENCE_KEY, tww.getConfidence());
		}
		return object;
	}

	private static JSONArray toJSONArray(Iterable<TargetWithWhy> it) {
		JSONArray array = new JSONArray();
		for(TargetWithWhy tww : it) {
    		array.put(toJSONObject(tww));
		}
		return array;
	}
}
