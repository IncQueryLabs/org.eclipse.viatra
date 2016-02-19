package org.eclipse.viatra.query.application.queries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.viatra.query.application.queries.EObjectMatch;
import org.eclipse.viatra.query.application.queries.util.EObjectQuerySpecification;
import org.eclipse.viatra.query.runtime.api.IMatchProcessor;
import org.eclipse.viatra.query.runtime.api.IQuerySpecification;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.impl.BaseMatcher;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;
import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;
import org.eclipse.viatra.query.runtime.util.ViatraQueryLoggingUtil;

/**
 * Generated pattern matcher API of the org.eclipse.viatra.query.application.queries.eObject pattern,
 * providing pattern-specific query methods.
 * 
 * <p>Use the pattern matcher on a given model via {@link #on(ViatraQueryEngine)},
 * e.g. in conjunction with {@link ViatraQueryEngine#on(Notifier)}.
 * 
 * <p>Matches of the pattern will be represented as {@link EObjectMatch}.
 * 
 * <p>Original source:
 * <code><pre>
 * pattern eObject(o) {
 * 	EObject(o);
 * }
 * </pre></code>
 * 
 * @see EObjectMatch
 * @see EObjectProcessor
 * @see EObjectQuerySpecification
 * 
 */
@SuppressWarnings("all")
public class EObjectMatcher extends BaseMatcher<EObjectMatch> {
  /**
   * Initializes the pattern matcher within an existing VIATRA Query engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing VIATRA Query engine in which this matcher will be created.
   * @throws ViatraQueryException if an error occurs during pattern matcher creation
   * 
   */
  public static EObjectMatcher on(final ViatraQueryEngine engine) throws ViatraQueryException {
    // check if matcher already exists
    EObjectMatcher matcher = engine.getExistingMatcher(querySpecification());
    if (matcher == null) {
    	matcher = new EObjectMatcher(engine);
    	// do not have to "put" it into engine.matchers, reportMatcherInitialized() will take care of it
    }
    return matcher;
  }
  
  private final static int POSITION_O = 0;
  
  private final static Logger LOGGER = ViatraQueryLoggingUtil.getLogger(EObjectMatcher.class);
  
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
  public EObjectMatcher(final Notifier emfRoot) throws ViatraQueryException {
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
  public EObjectMatcher(final ViatraQueryEngine engine) throws ViatraQueryException {
    super(engine, querySpecification());
  }
  
  /**
   * Returns the set of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pO the fixed value of pattern parameter o, or null if not bound.
   * @return matches represented as a EObjectMatch object.
   * 
   */
  public Collection<EObjectMatch> getAllMatches(final EObject pO) {
    return rawGetAllMatches(new Object[]{pO});
  }
  
  /**
   * Returns an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pO the fixed value of pattern parameter o, or null if not bound.
   * @return a match represented as a EObjectMatch object, or null if no match is found.
   * 
   */
  public EObjectMatch getOneArbitraryMatch(final EObject pO) {
    return rawGetOneArbitraryMatch(new Object[]{pO});
  }
  
  /**
   * Indicates whether the given combination of specified pattern parameters constitute a valid pattern match,
   * under any possible substitution of the unspecified parameters (if any).
   * @param pO the fixed value of pattern parameter o, or null if not bound.
   * @return true if the input is a valid (partial) match of the pattern.
   * 
   */
  public boolean hasMatch(final EObject pO) {
    return rawHasMatch(new Object[]{pO});
  }
  
  /**
   * Returns the number of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pO the fixed value of pattern parameter o, or null if not bound.
   * @return the number of pattern matches found.
   * 
   */
  public int countMatches(final EObject pO) {
    return rawCountMatches(new Object[]{pO});
  }
  
  /**
   * Executes the given processor on each match of the pattern that conforms to the given fixed values of some parameters.
   * @param pO the fixed value of pattern parameter o, or null if not bound.
   * @param processor the action that will process each pattern match.
   * 
   */
  public void forEachMatch(final EObject pO, final IMatchProcessor<? super EObjectMatch> processor) {
    rawForEachMatch(new Object[]{pO}, processor);
  }
  
  /**
   * Executes the given processor on an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pO the fixed value of pattern parameter o, or null if not bound.
   * @param processor the action that will process the selected match.
   * @return true if the pattern has at least one match with the given parameter values, false if the processor was not invoked
   * 
   */
  public boolean forOneArbitraryMatch(final EObject pO, final IMatchProcessor<? super EObjectMatch> processor) {
    return rawForOneArbitraryMatch(new Object[]{pO}, processor);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pO the fixed value of pattern parameter o, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public EObjectMatch newMatch(final EObject pO) {
    return EObjectMatch.newMatch(pO);
  }
  
  /**
   * Retrieve the set of values that occur in matches for o.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<EObject> rawAccumulateAllValuesOfo(final Object[] parameters) {
    Set<EObject> results = new HashSet<EObject>();
    rawAccumulateAllValues(POSITION_O, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for o.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<EObject> getAllValuesOfo() {
    return rawAccumulateAllValuesOfo(emptyArray());
  }
  
  @Override
  protected EObjectMatch tupleToMatch(final Tuple t) {
    try {
    	return EObjectMatch.newMatch((EObject) t.get(POSITION_O));
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in tuple not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected EObjectMatch arrayToMatch(final Object[] match) {
    try {
    	return EObjectMatch.newMatch((EObject) match[POSITION_O]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected EObjectMatch arrayToMatchMutable(final Object[] match) {
    try {
    	return EObjectMatch.newMutableMatch((EObject) match[POSITION_O]);
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
  public static IQuerySpecification<EObjectMatcher> querySpecification() throws ViatraQueryException {
    return EObjectQuerySpecification.instance();
  }
}
