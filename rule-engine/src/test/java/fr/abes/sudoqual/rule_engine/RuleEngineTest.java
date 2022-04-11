package fr.abes.sudoqual.rule_engine;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Assert;
import org.junit.Test;

import fr.abes.sudoqual.rule_engine.criterions.AgeCriterion;
import fr.abes.sudoqual.rule_engine.criterions.NameCriterion;
import fr.abes.sudoqual.rule_engine.criterions.PubDateCriterion;
import fr.abes.sudoqual.rule_engine.criterions.TownCriterion;
import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.feature.RawFeature;
import fr.abes.sudoqual.rule_engine.features.PubDateAggregateFeature;
import fr.abes.sudoqual.rule_engine.filters.PersonFilter;
import fr.abes.sudoqual.rule_engine.filters.TrueFilter;
import fr.abes.sudoqual.rule_engine.filters.bookFilter;
import fr.abes.sudoqual.rule_engine.impl.FeatureManagerImpl;
import fr.abes.sudoqual.rule_engine.impl.ReferenceImpl;
import fr.abes.sudoqual.rule_engine.impl.lumbago.PredicateManagerImpl;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;

public class RuleEngineTest {

	
	private static ConcurrentMap<String, ComputedFeature<?>> createComputedFeatureMap(Collection<Feature> values) {
		ConcurrentMap<String, ComputedFeature<?>> map = new ConcurrentHashMap<>(values.size()*2);
    	for(Feature feat : values) {
    		if(feat instanceof ComputedFeature) {
    			map.put(feat.getKey(), (ComputedFeature<?>)feat);
    		}
    	}
    	return map;
	}
	
	private static ConcurrentMap<String, Feature> createFeatureMap(Collection<Feature> values) {
		ConcurrentMap<String, Feature> map = new ConcurrentHashMap<>(values.size()*2);
    	for(Feature feat : values) {
    		map.put(feat.getKey(), feat);
    	}
    	return map;
	}
	
	/**
	 * A basic use case.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void basicTest() throws Throwable {
		
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new AgeCriterion(), new NameCriterion(), new TownCriterion());
		List<Feature> featureList = Arrays.asList(
			RawFeature.create("age"), 
			RawFeature.create("name"),
		    RawFeature.create("town"));
		
		FeatureManager store = new FeatureManagerImpl("{ \"toto\": {\"age\": 12, \"name\": \"toto\", \"town\": \"montpellier\"}, "
		                                            + " \"titi\": {\"age\": 20, \"name\": \"titi\", \"town\": \"lille\"}, "
		                                            + " \"toto2\": {\"age\": 13, \"name\": \"toto\", \"town\": \"montpellier\"}} ",
		                                            createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("sameAs(X,Y,100) :- ageCriterion(X,Y,1), nameCriterion(X,Y,2), townCriterion(X,Y,1)."),
		                                           critList);

		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl titi = new ReferenceImpl("titi");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto2, toto).value());

		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, titi).value());
		Assert.assertEquals(0, engine.check(manager, "sameAs", titi, toto).value());

	}
	
	/**
	 * a basic use case with a so called "Filter".
	 * 
	 * @throws Throwable
	 */
	@Test
	public void filterTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new PersonFilter(), new bookFilter());
		List<Feature> featureList = Arrays.asList(RawFeature.create("type"));
				FeatureManagerImpl store = new FeatureManagerImpl("{ \"book1\": {\"type\": \"book\"}, "
		                                            + " \"author1\": {\"type\": \"person\"}} ",
		                                            createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,1) :- book(X), person(Y)."),
		                                            critList);
		ReferenceImpl book1 = new ReferenceImpl("book1");
		ReferenceImpl author1 = new ReferenceImpl("author1");
		
		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(1, engine.check(manager, "sameAs", book1, author1).value());
		Assert.assertEquals(0, engine.check(manager, "sameAs", author1, book1).value());
	}

    /**
	 * The basic use case with a so called "Dimension".
	 * 
	 * @throws Throwable
	 */
	@Test
	public void dimensionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new TownCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"town\": \"montpellier\"}, "
		                                            + " \"titi\": {\"town\": \"lille\"}, "
		                                            + " \"toto2\": {\"town\": \"montpellier\"}} ", createFeatureMap(featureList));
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- locationDimension(X,Y,10)."
		                                                            + "locationDimension(X,Y,10) :- townCriterion(X,Y,1)."),
		                                            critList);
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl titi = new ReferenceImpl("titi");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");
		
		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, titi).value());
	}
	
	
	@Test
	public void variableNameDoesNotMatter3() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,1) :- nameCriterion(X,Y,1). "
		                                                            + "sameAs(X,Z,100) :- nameCriterion(X,Z,2)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
	}
	
	/**
	 * Why
	 * 
	 * townCriterion returns 1
	 * 
	 * @throws Throwable
	 */
	@Test
	public void whyTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new TownCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"town\": \"montpellier\"}, "
		                                            + " \"toto2\": {\"town\": \"montpellier\"}} ", createFeatureMap(featureList));
		RuleEngine engine = RuleEngine.create(new StringReader(""
                                                    				 + "[r1] sameAs(X,Y,2) :- townCriterion(X,Y,2)."
                                                                     + "[r2] sameAs(X,Y,1) :- townCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");
		
		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		List<Rule> why = engine.check(manager, "sameAs", toto, toto2).why();
		Assert.assertEquals(1, why.size());
		Assert.assertEquals("r2", why.get(0).getName());
	}
	
	/**
	 * Why with dimension
	 * 
	 * townCriterion returns 1
	 * nameCriterion returns 2
	 * 
	 * @throws Throwable
	 */
	@Test
	public void whyWithDimensionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new TownCriterion(), new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"), RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"town\": \"montpellier\", \"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"town\": \"montpellier\", \"name\": \"toto\"}} ", createFeatureMap(featureList));
		RuleEngine engine = RuleEngine.create(new StringReader(""
                                                				  + "[r1] sameAs(X,Y,3) :- nameCriterion(X,Y,2), locationDimension(X,Y,2). \n"
                                                                  + "[r2] sameAs(X,Y,2) :- nameCriterion(X,Y,2), locationDimension(X,Y,1). \n"
                                                                  + "[r3] sameAs(X,Y,1) :- nameCriterion(X,Y,1), locationDimension(X,Y,1). \n"
                                                                  + "[r4] locationDimension(X,Y,2) :- townCriterion(X,Y,2). \n"
                                                				  + "[r5] locationDimension(X,Y,1) :- townCriterion(X,Y,1). \n"),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		List<Rule> why = engine.check(manager, "sameAs", toto, toto2).why();
		Assert.assertEquals(2, why.size());
		String rule1 = why.get(0).getName();
		String rule2 = why.get(1).getName();
		Assert.assertTrue(
			("r2".equals(rule1) &&  "r5".equals(rule2) )
			|| ("r2".equals(rule2) &&  "r5".equals(rule1)) );
	}

	/**
	 * Test if ask for a certain value for a criterion is validated by a greater
	 * provided value. In this test, nameCriterion returns 2, 2 > 1 so it should
	 * be true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void scorePropagationOnCriterionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("" + "sameAs(X,Y,1) :- nameCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(1, engine.check(manager, "sameAs", toto, toto2).value());
	}
	
	/**
	 * Test if ask for a neutral value (0) for a criterion is validated by a greater
	 * provided value. In this test, nameCriterion returns 2, 2 > 0 so it should
	 * be true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void positiveScorePropagationToNeutralOnCriterionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("" + "sameAs(X,Y,1) :- nameCriterion(X,Y,neutral)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, toto2).value());
	}
	
	/**
	 * Test if ask for a neutral value (0) for a criterion.
	 * In this test, nameCriterion returns 0, so it should
	 * be true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void neutralOnCriterionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toti\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("" + "sameAs(X,Y,1) :- nameCriterion(X,Y,neutral)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(1, engine.check(manager, "sameAs", toto, toto2).value());
	}
	
	/**
	 * Test if ask for a neutral value (0) for a dimension is validated by a greater
	 * provided value. In this test, nameCriterion returns 2, 2 > 0 so it should
	 * be true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void positiveScorePropagationToNeutralOnDimensionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));

		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("" + "sameAs(X,Y,1) :- nameDimension(X,Y,0)."
																	   + "nameDimension(X,Y,2) :- nameCriterion(X,Y,2)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(1, engine.check(manager, "sameAs", toto, toto2).value());
	}


	/**
	 * Test if ask for a certain negative value for a criterion is validated by a
	 * lower provided negative value. In this test, nameCriterion returns -2, -2
	 * < -1 so it should be true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void negativeScorePropagationOnCriterionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"zaza\": {\"name\": \"zaza\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("" + "sameAs(X,Y,-1) :- nameCriterion(X,Y,-1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("zaza");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(-1, engine.check(manager, "sameAs", toto, toto2).value());
	}

	/**
	 * Test if a negative value for a criterion does not trigger zero or more.
	 * nameCriterion returns -2, it should not trigger 0 or 1.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void negativeScorePropagationDoesNotTriggerZeroOrMore() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"zaza\": {\"name\": \"zaza\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- nameCriterion(X,Y,0)."
		                                                            + "sameAs(X,Y,100) :- nameCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("zaza");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, toto2).value());
	}

	/**
	 * Test if a positive value for a criterion does not trigger -1 or lower.
	 * nameCriterion returns 100, it should not trigger -1.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void positiveScorePropagationDoesNotTriggerMinus1() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"zaza\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("sameAs(X,Y,100) :- nameCriterion(X,Y,-2)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("zaza");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, toto2).value());
	}

	/**
	 * Test if ask for a certain value for a dimension is validated by a greater
	 * provided value. In this test, locationDimension returns 100, 100 > 1 so it
	 * should be true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void scorePropagationOnDimensionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new TownCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"));
				
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"town\": \"montpellier\"}, "
		                                            + " \"titi\": {\"town\": \"lille\"}, "
		                                            + " \"toto2\": {\"town\": \"montpellier\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- locationDimension(X,Y,1)."
		                                                            + "locationDimension(X,Y,10) :- townCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl titi = new ReferenceImpl("titi");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, titi).value());
	}

	/**
	 * Test if ask for a certain negative value for a dimension is validated by a
	 * lower provided negative value. In this test, nameDimension returns -100,
	 * -100 < -1 so it should be true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void negativeScorePropagationOnDimensionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"zaza\": {\"name\": \"zaza\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,-100) :- nameDimension(X,Y,-1)."
		                                                            + "nameDimension(X,Y,-10) :- nameCriterion(X,Y,-1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl zaza = new ReferenceImpl("zaza");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(-100, engine.check(manager, "sameAs", toto, zaza).value());
	}

	/**
	 * Test if a negative value for a dimension does not trigger zero or more.
	 * nameCriterion returns -2.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void negativeScorePropagationOnDimensionDoesNotTriggerZeroOrMore() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"zaza\": {\"name\": \"zaza\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- nameDimension(X,Y,0)."
		                                                            + "sameAs(X,Y,100) :- nameDimension(X,Y,1)."
		                                                            + "nameDimension(X,Y,-10) :- nameCriterion(X,Y,-2)."
		                                                            + "nameDimension(X,Y,10) :- nameCriterion(X,Y,10)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("zaza");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, toto2).value());
	}

	/**
	 * Test if a positive value for a dimension does not trigger -1 or lower.
	 * nameCriterion returns 100.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void positiveScorePropagationOnDimensionDoesNotTriggerMinus1() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"zaza\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
				+ "sameAs(X,Y,100) :- nameDimension(X,Y,-1)."
				+ "nameDimension(X,Y,10) :- nameCriterion(X,Y,2)."
				+ "nameDimension(X,Y,-10) :- nameCriterion(X,Y,-10)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("zaza");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, toto2).value());
	}

	/**
	 * Test not_
	 * 
	 * @throws Throwable
	 */
	@Test
	public void notCriterionTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}, "
		                                            + " \"titi\": {\"name\": \"titi\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("sameAs(X,Y,100) :- not_nameCriterion(X,Y,2)."),
		                                            critList);

		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");
		ReferenceImpl titi = new ReferenceImpl("titi");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, toto2).value());
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, titi).value());
	}


	/**
	 * Variable name does not matter
	 * 
	 * @throws Throwable
	 */
	@Test
	public void variableNameDoesNotMatter() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(Zozo,Variable,100) :- nameCriterion(Zozo,Variable,2)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
	}

	/**
	 * Variable name does not matter
	 * 
	 * @throws Throwable
	 */
	@Test
	public void variableNameDoesNotMatter2() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(A,B,1) :- nameCriterion(A,B,1). "
		                                                            + "sameAs(Zozo,Variable,100) :- nameCriterion(Zozo,Variable,2)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
	}
	
	/**
	 * Test under threshold
	 * 
	 * @throws Throwable
	 */
	@Test
	public void underThresholdTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("" + "sameAs(X,Y,1) :- nameCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, toto2, 2).value());
	}
	
	/**
	 * Test over threshold
	 * 
	 * @throws Throwable
	 */
	@Test
	public void overThresholdTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader("" + "sameAs(X,Y,2) :- nameCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(2, engine.check(manager, "sameAs", toto, toto2, 2).value());
	}
	
	/**
	 * Test different arity on generated dimension rules.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void dimensionRuleArityTest() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> predList = Arrays.asList(new TownCriterion(), new TrueFilter());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"town\": \"montpellier\"}, "
		                                            + " \"titi\": {\"town\": \"lille\"}, "
		                                            + " \"toto2\": {\"town\": \"montpellier\"}} ", createFeatureMap(featureList));
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- locationDimension(X,Y,10), testArityDimension(X,1)."
		                                                            + "locationDimension(X,Y,10) :- townCriterion(X,Y,1)."
		                                                            + "testArityDimension(X,1) :- trueFilter(X)."),
		                                            predList);
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl titi = new ReferenceImpl("titi");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");
		
		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, titi).value());
	}

	
	
	// /////////////////////////////////////////////////////
	// Exceptions
	// /////////////////////////////////////////////////////
	
	/**
	 * Variable name does matter : variables should be in hypothesis AND conclusion
	 * 
	 * @throws Throwable
	 */
	@Test(expected = RuleEngineException.class)
	public void variableNameDoesMatter() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- nameCriterion(X,Z,2)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		engine.check(manager, "sameAs", toto, toto2);
	}
	
	/**
	 * variable instead of value or constant
	 * 
	 * @throws Throwable
	 */
	@Test(expected = RuleEngineException.class)
	public void variableInsteadValue() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- nameCriterion(X,Y,Z)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		engine.check(manager, "sameAs", toto, toto2);
	}
	
	/**
	 * Constant unknown
	 * 
	 * @throws Throwable
	 */
	@Test(expected = RuleEngineException.class)
	public void constantUnknown() throws Throwable {
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = new ConcurrentHashMap<>(); 
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("name"));
		
		FeatureManagerImpl store = new FeatureManagerImpl("{ \"toto\": {\"name\": \"toto\"}, "
		                                            + " \"toto2\": {\"name\": \"toto\"}} ", createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- nameCriterion(X,Y,test)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		engine.check(manager, "sameAs", toto, toto2);
	}
	
	/**
	 * malformed Rule Set
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	@Test(expected = RuleEngineException.class)
	public void malformedRuleSet() throws Throwable {
		List<Predicate> critList = Collections.emptyList();
		List<Feature> featureList = Collections.emptyList();
		
		RuleEngine.create(new StringReader("bzzz."),
		                                            critList);
	}
	
	/**
	 * recursion on dimension
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	@Test(expected = RuleEngineException.class)
	public void recursionOnDimension() throws Throwable {
		List<Predicate> critList = Arrays.asList(new NameCriterion());
		List<Feature> featureList = Collections.emptyList();
		
		RuleEngine.create(new StringReader(""
				+ "sameAs(X,Y,100) :- nameDimension(X,Y,1)."
				+ "nameDimension(X,Y,1) :- nameCriterion(X,Y,1), nameDimension(X,Y,1)."),
		                                            critList);
	}
	
	/**
	 * recursion on dimension (same value)
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	@Test(expected = RuleEngineException.class)
	public void recursionOnDimension2() throws Throwable {
		List<Predicate> critList = Collections.emptyList();
		List<Feature> featureList = Collections.emptyList();
		
		RuleEngine.create(new StringReader(""
				+ "sameAs(X,Y,100) :- dim1(X,Y,1)."
				+ "dim1(X,Y,1) :- dim2(X,Y,1)."
				+ "dim2(X,Y,1) :- dim1(X,Y,1)."),
		                                            critList);
	}
	
	/**
	 * recursion on dimension (increase value)
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	@Test(expected = RuleEngineException.class)
	public void recursionOnDimension3() throws Throwable {
		List<Predicate> critList = Collections.emptyList();
		List<Feature> featureList = Collections.emptyList();
		
		RuleEngine.create(new StringReader(""
				+ "sameAs(X,Y,100) :- dim1(X,Y,3)."
				+ "dim1(X,Y,3) :- dim2(X,Y,2)."
				+ "dim2(X,Y,2) :- dim1(X,Y,1)."),
		                                            critList);
	}
	
	/**
	 * Unreachable dimension 
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	@Test(expected=RuleEngineException.class)
	public void undefinedDimensionTest() throws Throwable {
		List<Predicate> critList = Arrays.asList(new TownCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"));

		RuleEngine.create(new StringReader(""
                                    + "sameAs(X,Y,100) :- locationDimension(X,Y,11)."
                                    + "locationDimension(X,Y,10) :- townCriterion(X,Y,1)."),
		                                            critList);
	}
	
	/**
	 * Undeclared criterion 
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	@Test(expected=RuleEngineException.class)
	public void undeclaredCriterionTest() throws Throwable {
		List<Predicate> critList = Arrays.asList(new TownCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"));

		RuleEngine.create(new StringReader(""
                                    + "sameAs(X,Y,100) :- locationDimension(X,Y,10), undeclaredCriterion(X,Y,10)."
                                    + "locationDimension(X,Y,10) :- townCriterion(X,Y,1)."),
		                                            critList);
	}
	
	// /////////////////////////////////////////////////////
	// Data Augmentation
	// /////////////////////////////////////////////////////
	
	/**
	 * Use ComputedFeature from data initially available
	 * Here we use the town feature to compute the country feature.
	 * 
	 * @throws Throwable
	 */
	/* @Test
	 * FIXME usecase compute something from references selected but which is not a sameAs
	 * Is it a usefull usecase ?
	public void initialComputedFeatureTest() throws Throwable {
		CountryFeature countryFeature = new CountryFeature();
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = createComputedFeatureMap(Arrays.asList(countryFeature));
		List<Predicate> critList = Arrays.asList(new CountryCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("town"), countryFeature);

		FeatureStoreImpl store = new FeatureStoreImpl("{"
		                                            + " \"montpellier\": { \"country\": \"France\" },"
		                                            + " \"lille\":       { \"country\": \"France\" },"
		                                            + " \"london\" :     { \"country\": \"England\" },"
		                                            + " \"toto\":        { \"town\": \"montpellier\"}, "
		                                            + " \"titi\":        { \"town\": \"london\"}, "
		                                            + " \"toto2\":       { \"town\": \"lille\"}} ",
		                                            createFeatureMap(featureList));
		
		RuleEngineImpl engine = new RuleEngineImpl(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- countryCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl toto = new ReferenceImpl("toto");
		ReferenceImpl titi = new ReferenceImpl("titi");
		ReferenceImpl toto2 = new ReferenceImpl("toto2");

		PredicateManager manager = new LazyPredicateManager(store, computedFeatureList);
		Assert.assertEquals(100, engine.check(manager, "sameAs", toto, toto2).value());
		Assert.assertEquals(0, engine.check(manager, "sameAs", toto, titi).value());
	}*/
	
	/**
	 * Use ComputedFeature 
	 * Here we assume that we already computed a first sameAs link from "Asimov" to "Foundation".
	 * So now we have to use it, to produce a pubDateAggregate feature on Asimov.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void computedFeatureTest() throws Throwable {
		ComputedFeature<?> feature = new PubDateAggregateFeature();
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureList = createComputedFeatureMap(Arrays.asList(feature));
		
		List<Predicate> critList = Arrays.asList(new PubDateCriterion());
		List<Feature> featureList = Arrays.asList(RawFeature.create("pubDate"), feature);

		FeatureManagerImpl store = new FeatureManagerImpl("{"
		                                            + " \"Foundation\":          { \"pubDate\": 1951 },"
		                                            + " \"The Caves of Steel\":  { \"pubDate\": 1954 },"
		                                            + " \"Second Foundation\":   { \"pubDate\": 1953 },"
		                                            + " \"Asimov\":              {  } "
		                                            + "}",
		                                            createFeatureMap(featureList));
		
		RuleEngine engine = RuleEngine.create(new StringReader(""
		                                                            + "sameAs(X,Y,100) :- pubDateCriterion(X,Y,1)."),
		                                            critList);
		
		ReferenceImpl asimov = new ReferenceImpl("Asimov");
		ReferenceImpl secondFondation = new ReferenceImpl("Second Foundation");

		PredicateManager manager = new PredicateManagerImpl(store, computedFeatureList);
		Set<Reference> sameAsList = new HashSet<>();
		sameAsList.add(secondFondation);
		
		store.updateComputedFeatures(asimov, computedFeatureList.values(), sameAsList, sameAsList);
		
		Assert.assertEquals(100, engine.check(manager, "sameAs", asimov, secondFondation).value());
	}
	
}
