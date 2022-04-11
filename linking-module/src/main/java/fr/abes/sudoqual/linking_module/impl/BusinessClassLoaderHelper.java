/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.linking_module.BusinessClassLoader;
import fr.abes.sudoqual.linking_module.Config;
import fr.abes.sudoqual.linking_module.exception.BusinessClassException;
import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.feature.PreprocessedFeature;
import fr.abes.sudoqual.rule_engine.feature.RawFeature;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;
import fr.abes.sudoqual.util.DlpUtils;
import fr.lirmm.graphik.dlgp2.parser.DLGP2Parser;
import fr.lirmm.graphik.dlgp2.parser.ParseException;
import fr.lirmm.graphik.dlgp2.parser.ParserListener;
import fr.lirmm.graphik.dlgp2.parser.TermFactory;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class BusinessClassLoaderHelper {

	private BusinessClassLoaderHelper() {}
	
	private static final Logger logger = LoggerFactory.getLogger(BusinessClassLoaderHelper.class);
	
	public static Set<String> extractPredicateNames(URL dlpFile) throws BusinessClassException {
		Set<String> predicateNames = new HashSet<>();
		PredicateNameCollectorListener listener = new PredicateNameCollectorListener(predicateNames);

		try(InputStreamReader reader = new InputStreamReader(dlpFile.openStream(), Config.CHARSET)) {
			DLGP2Parser parser = new DLGP2Parser(new MyTermFactory(), reader);
			parser.addParserListener(listener);
			parser.setDefaultBase("");
			parser.document();
		} catch (ParseException | IOException e ) {
			throw new BusinessClassException("Error during predicate names extraction from rule file", e);
		}
		
		return predicateNames;
	}
	
	public static Set<String> extractFeaturesNames(Iterable<Predicate> predicates) throws BusinessClassException {
		Set<String> res = new HashSet<>();
		for(Predicate p : predicates) {
			if(p instanceof Criterion) {
				Criterion c = (Criterion) p;
				res.addAll(c.sourceFeatureSet());
				res.addAll(c.targetFeatureSet());
			} else if(p instanceof Filter) {
				Filter f = (Filter) p;
				res.addAll(f.featureSet());
			} else {
				throw new BusinessClassException("Kind of predicate unknown (neither a filter nor a criterion): " + p.getClass());

			}
		}
		return res;
	}
	

	public static Set<String> extractFeaturesNamesFromComputedFeaturesOrPreprocessedFeature(Iterable<Feature> featureInstances) {
		Set<String> res = new HashSet<>();
		for(Feature f : featureInstances) {
			if(f instanceof ComputedFeature) {
				ComputedFeature<?> cf = (ComputedFeature<?>) f;
				res.addAll(cf.getRelatedFeatures());
			} else if (f instanceof PreprocessedFeature) {
				PreprocessedFeature<?,?> pf = (PreprocessedFeature<?,?>) f;
				res.add(pf.getRelatedRawFeature());
			}
		}
		return res;
	}
	
	public static Set<String> extractFeaturesNamesFromFilters(Iterable<Filter> filters) {
		Set<String> res = new HashSet<>();
		for(Filter f : filters) {
			res.addAll(f.featureSet());
		}
		return res;
	}

	public static Set<String> extractFeaturesNamesFromCriterions(Iterable<Criterion> criterions) {
		Set<String> res = new HashSet<>();
		for(Criterion c : criterions) {
			res.addAll(c.sourceFeatureSet());
			res.addAll(c.targetFeatureSet());
		}
		return res;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE CLASSES
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * The datalog parser used to extract predicate names used
	 * @author Clément Sipieter {@literal <clement@6pi.fr>}
	 */
	private static class PredicateNameCollectorListener implements ParserListener {

		private Set<String> predicateNames;

		public PredicateNameCollectorListener(Set<String> predicateNames) {
			this.predicateNames = predicateNames;
		}
		
		@Override
		public void createsAtom(Object predicate, Object[] terms) {
			predicateNames.add(DlpUtils.removeNotFromPredicateName(predicate.toString()));
		}

		@Override
		public void startsObject(OBJECT_TYPE objectType, String name) {
			// nothing to do
		}

		@Override
		public void declarePrefix(String prefix, String ns) {
			// nothing to do
			
		}

		@Override
		public void declareBase(String base) {
			// nothing to do
			
		}

		@Override
		public void declareTop(String top) {
			// nothing to do
			
		}

		@Override
		public void declareUNA() {
			// nothing to do
			
		}

		@Override
		public void directive(String text) {
			// nothing to do
			
		}

		@Override
		public void createsEquality(Object term1, Object term2) {
			// nothing to do
			
		}

		@Override
		public void answerTermList(Object[] terms) {
			// nothing to do
			
		}

		@Override
		public void endsConjunction(OBJECT_TYPE objectType) {
			// nothing to do
			
		}


	}

	private static class MyTermFactory implements TermFactory {
		
		public MyTermFactory() {
			super();
		}

		@Override
		public Object createIRI(String s) {
			return s;
		}

		@Override
		public Object createLiteral(Object datatype, String stringValue, String langTag) {
			// nothing to do return will be ignored
			return null;
		}

		@Override
		public Object createVariable(String stringValue) {
			// nothing to do return will be ignored
			return null;
		}

	

	}

	/**
	 * @param businessClassLoader
	 * @param predicateInstances
	 * @return a map which associate a Feature for each related feature name met in a predicate from predicateInstances. If no corresponding instance
	 * can be found, the map must contains null (so the map implementation must support null values).
	 * @throws BusinessClassException
	 */
	public static Map<String, Feature> loadFeaturesFromPredicates(BusinessClassLoader businessClassLoader, Set<Predicate> predicateInstances, boolean enableRawFeature) throws BusinessClassException {
		return BusinessClassLoaderHelper.loadFeaturesFromNames(businessClassLoader, BusinessClassLoaderHelper.extractFeaturesNames(predicateInstances), enableRawFeature);
	}
	
	public static Map<String, Feature> loadFeaturesFromNames(BusinessClassLoader businessClassLoader, Collection<String> featureNames, boolean enableRawFeature) throws BusinessClassException {
		Map<String, Feature> result = new HashMap<>();
		// load features referenced by a predicate
		for(String name : featureNames) {
			result.put(name, createBusinessFeatureOrRaw(businessClassLoader, name, enableRawFeature));
		}
		// load features referenced by a computed feature
		Set<String> featureNameRelatedToComputedFeatures = BusinessClassLoaderHelper.extractFeaturesNamesFromComputedFeaturesOrPreprocessedFeature(result.values());
		featureNameRelatedToComputedFeatures.removeAll(featureNames); // already loaded
		for(String name : featureNameRelatedToComputedFeatures) {
			result.put(name, createBusinessFeatureOrRaw(businessClassLoader, name, enableRawFeature));
		}
		return result;
	}
	
	public static Feature createBusinessFeatureOrRaw(BusinessClassLoader businessClassLoader, String key, boolean enableRawFeature) throws BusinessClassException {
		Feature feature = businessClassLoader.createFeature(key);
		if(feature == null) {
			if(!enableRawFeature && !FeatureManager.URI_KEY.equals(key)) {
				throw new BusinessClassException("Undeclared RawFeature support is disabled and the following feature is not declared: " + key);
			}
			feature = RawFeature.create(key);
			if(logger.isInfoEnabled()) {
				logger.info("Feature instance '{}' not found, loaded as a raw feature.", key);
			}
		}
		return feature;
	}

}
