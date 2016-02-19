package org.eclipse.viatra.query.application.queries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.viatra.query.application.queries.ClassesInPackageMatch;
import org.eclipse.viatra.query.application.queries.util.ClassesInPackageQuerySpecification;
import org.eclipse.viatra.query.runtime.api.IMatchProcessor;
import org.eclipse.viatra.query.runtime.api.IQuerySpecification;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.impl.BaseMatcher;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;
import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;
import org.eclipse.viatra.query.runtime.util.ViatraQueryLoggingUtil;

/**
 * Generated pattern matcher API of the org.eclipse.viatra.query.application.queries.classesInPackage pattern,
 * providing pattern-specific query methods.
 * 
 * <p>Use the pattern matcher on a given model via {@link #on(ViatraQueryEngine)},
 * e.g. in conjunction with {@link ViatraQueryEngine#on(Notifier)}.
 * 
 * <p>Matches of the pattern will be represented as {@link ClassesInPackageMatch}.
 * 
 * <p>Original source:
 * <code><pre>
 * {@literal @}Edge(source = p, target = ec, label = "classIn")
 * pattern classesInPackage(p : EPackage, ec: EClass) { EPackage.eClassifiers(p,ec); }
 * </pre></code>
 * 
 * @see ClassesInPackageMatch
 * @see ClassesInPackageProcessor
 * @see ClassesInPackageQuerySpecification
 * 
 */
@SuppressWarnings("all")
public class ClassesInPackageMatcher extends BaseMatcher<ClassesInPackageMatch> {
  /**
   * Initializes the pattern matcher within an existing VIATRA Query engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing VIATRA Query engine in which this matcher will be created.
   * @throws ViatraQueryException if an error occurs during pattern matcher creation
   * 
   */
  public static ClassesInPackageMatcher on(final ViatraQueryEngine engine) throws ViatraQueryException {
    // check if matcher already exists
    ClassesInPackageMatcher matcher = engine.getExistingMatcher(querySpecification());
    if (matcher == null) {
    	matcher = new ClassesInPackageMatcher(engine);
    	// do not have to "put" it into engine.matchers, reportMatcherInitialized() will take care of it
    }
    return matcher;
  }
  
  private final static int POSITION_P = 0;
  
  private final static int POSITION_EC = 1;
  
  private final static Logger LOGGER = ViatraQueryLoggingUtil.getLogger(ClassesInPackageMatcher.class);
  
  /**
   * Initializes the pattern matcher over a given EMF model root (recommended: Resource or ResourceSet).
   * If a pattern matcher is already constructed with the same root, only a light-weight reference is returned.
   * The scope of pattern matching will be the given EMF model root and below (see FAQ for more precise definition).
   * The match set will be incrementally refreshed upon updates from this scope.
   * <p>The matcher will be created within the managed {@link ViatraQueryEngine} belonging to the EMF model root, so
   * multiple matchers will reuse the same engine and benefit from increased performance and reduced memory footprint.
   * @param emfRoot the root of the EMF containment hierarchy where the pattern matcher will operate. Recommended: Resource or ResourceSet.
   * @throws ViatraQueryException if an error occurs during pattern matcher creation
   * @deprecated use {@link #on(ViatraQueryEngine)} instead, e.g. in conjunction with {@link ViatraQueryEngine#on(Notifier)}
   * 
   */
  @Deprecated
  public ClassesInPackageMatcher(final Notifier emfRoot) throws ViatraQueryException {
    this(ViatraQueryEngine.on(emfRoot));
  }
  
  /**
   * Initializes the pattern matcher within an existing VIATRA Query engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing VIATRA Query engine in which this matcher will be created.
   * @throws ViatraQueryException if an error occurs during pattern matcher creation
   * @deprecated use {@link #on(ViatraQueryEngine)} instead
   * 
   */
  @Deprecated
  public ClassesInPackageMatcher(final ViatraQueryEngine engine) throws ViatraQueryException {
    super(engine, querySpecification());
  }
  
  /**
   * Returns the set of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pP the fixed value of pattern parameter p, or null if not bound.
   * @param pEc the fixed value of pattern parameter ec, or null if not bound.
   * @return matches represented as a ClassesInPackageMatch object.
   * 
   */
  public Collection<ClassesInPackageMatch> getAllMatches(final EPackage pP, final EClass pEc) {
    return rawGetAllMatches(new Object[]{pP, pEc});
  }
  
  /**
   * Returns an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pP the fixed value of pattern parameter p, or null if not bound.
   * @param pEc the fixed value of pattern parameter ec, or null if not bound.
   * @return a match represented as a ClassesInPackageMatch object, or null if no match is found.
   * 
   */
  public ClassesInPackageMatch getOneArbitraryMatch(final EPackage pP, final EClass pEc) {
    return rawGetOneArbitraryMatch(new Object[]{pP, pEc});
  }
  
  /**
   * Indicates whether the given combination of specified pattern parameters constitute a valid pattern match,
   * under any possible substitution of the unspecified parameters (if any).
   * @param pP the fixed value of pattern parameter p, or null if not bound.
   * @param pEc the fixed value of pattern parameter ec, or null if not bound.
   * @return true if the input is a valid (partial) match of the pattern.
   * 
   */
  public boolean hasMatch(final EPackage pP, final EClass pEc) {
    return rawHasMatch(new Object[]{pP, pEc});
  }
  
  /**
   * Returns the number of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pP the fixed value of pattern parameter p, or null if not bound.
   * @param pEc the fixed value of pattern parameter ec, or null if not bound.
   * @return the number of pattern matches found.
   * 
   */
  public int countMatches(final EPackage pP, final EClass pEc) {
    return rawCountMatches(new Object[]{pP, pEc});
  }
  
  /**
   * Executes the given processor on each match of the pattern that conforms to the given fixed values of some parameters.
   * @param pP the fixed value of pattern parameter p, or null if not bound.
   * @param pEc the fixed value of pattern parameter ec, or null if not bound.
   * @param processor the action that will process each pattern match.
   * 
   */
  public void forEachMatch(final EPackage pP, final EClass pEc, final IMatchProcessor<? super ClassesInPackageMatch> processor) {
    rawForEachMatch(new Object[]{pP, pEc}, processor);
  }
  
  /**
   * Executes the given processor on an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pP the fixed value of pattern parameter p, or null if not bound.
   * @param pEc the fixed value of pattern parameter ec, or null if not bound.
   * @param processor the action that will process the selected match.
   * @return true if the pattern has at least one match with the given parameter values, false if the processor was not invoked
   * 
   */
  public boolean forOneArbitraryMatch(final EPackage pP, final EClass pEc, final IMatchProcessor<? super ClassesInPackageMatch> processor) {
    return rawForOneArbitraryMatch(new Object[]{pP, pEc}, processor);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pP the fixed value of pattern parameter p, or null if not bound.
   * @param pEc the fixed value of pattern parameter ec, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public ClassesInPackageMatch newMatch(final EPackage pP, final EClass pEc) {
    return ClassesInPackageMatch.newMatch(pP, pEc);
  }
  
  /**
   * Retrieve the set of values that occur in matches for p.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<EPackage> rawAccumulateAllValuesOfp(final Object[] parameters) {
    Set<EPackage> results = new HashSet<EPackage>();
    rawAccumulateAllValues(POSITION_P, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for p.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<EPackage> getAllValuesOfp() {
    return rawAccumulateAllValuesOfp(emptyArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for p.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<EPackage> getAllValuesOfp(final ClassesInPackageMatch partialMatch) {
    return rawAccumulateAllValuesOfp(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for p.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<EPackage> getAllValuesOfp(final EClass pEc) {
    return rawAccumulateAllValuesOfp(new Object[]{
    null, 
    pEc
    });
  }
  
  /**
   * Retrieve the set of values that occur in matches for ec.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<EClass> rawAccumulateAllValuesOfec(final Object[] parameters) {
    Set<EClass> results = new HashSet<EClass>();
    rawAccumulateAllValues(POSITION_EC, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for ec.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<EClass> getAllValuesOfec() {
    return rawAccumulateAllValuesOfec(emptyArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for ec.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<EClass> getAllValuesOfec(final ClassesInPackageMatch partialMatch) {
    return rawAccumulateAllValuesOfec(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for ec.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<EClass> getAllValuesOfec(final EPackage pP) {
    return rawAccumulateAllValuesOfec(new Object[]{
    pP, 
    null
    });
  }
  
  @Override
  protected ClassesInPackageMatch tupleToMatch(final Tuple t) {
    try {
    	return ClassesInPackageMatch.newMatch((EPackage) t.get(POSITION_P), (EClass) t.get(POSITION_EC));
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in tuple not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ClassesInPackageMatch arrayToMatch(final Object[] match) {
    try {
    	return ClassesInPackageMatch.newMatch((EPackage) match[POSITION_P], (EClass) match[POSITION_EC]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ClassesInPackageMatch arrayToMatchMutable(final Object[] match) {
    try {
    	return ClassesInPackageMatch.newMutableMatch((EPackage) match[POSITION_P], (EClass) match[POSITION_EC]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  /**
   * @return the singleton instance of the query specification of this pattern
   * @throws ViatraQueryException if the pattern definition could not be loaded
   * 
   */
  public static IQuerySpecification<ClassesInPackageMatcher> querySpecification() throws ViatraQueryException {
    return ClassesInPackageQuerySpecification.instance();
  }
}
