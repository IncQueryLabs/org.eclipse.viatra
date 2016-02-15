package org.eclipse.incquery.examples.bpm.queries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.examples.bpm.queries.ProcessTasksMatch;
import org.eclipse.incquery.examples.bpm.queries.util.ProcessTasksQuerySpecification;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseMatcher;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.util.IncQueryLoggingUtil;
import process.Activity;

/**
 * Generated pattern matcher API of the org.eclipse.incquery.examples.bpm.queries.processTasks pattern,
 * providing pattern-specific query methods.
 * 
 * <p>Use the pattern matcher on a given model via {@link #on(IncQueryEngine)},
 * e.g. in conjunction with {@link IncQueryEngine#on(Notifier)}.
 * 
 * <p>Matches of the pattern will be represented as {@link ProcessTasksMatch}.
 * 
 * <p>Original source:
 * <code><pre>
 * pattern processTasks(Proc , Task){ 
 * 	Process.contents(Proc, Task);
 * }
 * </pre></code>
 * 
 * @see ProcessTasksMatch
 * @see ProcessTasksProcessor
 * @see ProcessTasksQuerySpecification
 * 
 */
@SuppressWarnings("all")
public class ProcessTasksMatcher extends BaseMatcher<ProcessTasksMatch> {
  /**
   * Initializes the pattern matcher within an existing EMF-IncQuery engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing EMF-IncQuery engine in which this matcher will be created.
   * @throws IncQueryException if an error occurs during pattern matcher creation
   * 
   */
  public static ProcessTasksMatcher on(final IncQueryEngine engine) throws IncQueryException {
    // check if matcher already exists
    ProcessTasksMatcher matcher = engine.getExistingMatcher(querySpecification());
    if (matcher == null) {
    	matcher = new ProcessTasksMatcher(engine);
    	// do not have to "put" it into engine.matchers, reportMatcherInitialized() will take care of it
    }
    return matcher;
  }
  
  private final static int POSITION_PROC = 0;
  
  private final static int POSITION_TASK = 1;
  
  private final static Logger LOGGER = IncQueryLoggingUtil.getLogger(ProcessTasksMatcher.class);
  
  /**
   * Initializes the pattern matcher over a given EMF model root (recommended: Resource or ResourceSet).
   * If a pattern matcher is already constructed with the same root, only a light-weight reference is returned.
   * The scope of pattern matching will be the given EMF model root and below (see FAQ for more precise definition).
   * The match set will be incrementally refreshed upon updates from this scope.
   * <p>The matcher will be created within the managed {@link IncQueryEngine} belonging to the EMF model root, so
   * multiple matchers will reuse the same engine and benefit from increased performance and reduced memory footprint.
   * @param emfRoot the root of the EMF containment hierarchy where the pattern matcher will operate. Recommended: Resource or ResourceSet.
   * @throws IncQueryException if an error occurs during pattern matcher creation
   * @deprecated use {@link #on(IncQueryEngine)} instead, e.g. in conjunction with {@link IncQueryEngine#on(Notifier)}
   * 
   */
  @Deprecated
  public ProcessTasksMatcher(final Notifier emfRoot) throws IncQueryException {
    this(IncQueryEngine.on(emfRoot));
  }
  
  /**
   * Initializes the pattern matcher within an existing EMF-IncQuery engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing EMF-IncQuery engine in which this matcher will be created.
   * @throws IncQueryException if an error occurs during pattern matcher creation
   * @deprecated use {@link #on(IncQueryEngine)} instead
   * 
   */
  @Deprecated
  public ProcessTasksMatcher(final IncQueryEngine engine) throws IncQueryException {
    super(engine, querySpecification());
  }
  
  /**
   * Returns the set of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pProc the fixed value of pattern parameter Proc, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return matches represented as a ProcessTasksMatch object.
   * 
   */
  public Collection<ProcessTasksMatch> getAllMatches(final process.Process pProc, final Activity pTask) {
    return rawGetAllMatches(new Object[]{pProc, pTask});
  }
  
  /**
   * Returns an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pProc the fixed value of pattern parameter Proc, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return a match represented as a ProcessTasksMatch object, or null if no match is found.
   * 
   */
  public ProcessTasksMatch getOneArbitraryMatch(final process.Process pProc, final Activity pTask) {
    return rawGetOneArbitraryMatch(new Object[]{pProc, pTask});
  }
  
  /**
   * Indicates whether the given combination of specified pattern parameters constitute a valid pattern match,
   * under any possible substitution of the unspecified parameters (if any).
   * @param pProc the fixed value of pattern parameter Proc, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return true if the input is a valid (partial) match of the pattern.
   * 
   */
  public boolean hasMatch(final process.Process pProc, final Activity pTask) {
    return rawHasMatch(new Object[]{pProc, pTask});
  }
  
  /**
   * Returns the number of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pProc the fixed value of pattern parameter Proc, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the number of pattern matches found.
   * 
   */
  public int countMatches(final process.Process pProc, final Activity pTask) {
    return rawCountMatches(new Object[]{pProc, pTask});
  }
  
  /**
   * Executes the given processor on each match of the pattern that conforms to the given fixed values of some parameters.
   * @param pProc the fixed value of pattern parameter Proc, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @param processor the action that will process each pattern match.
   * 
   */
  public void forEachMatch(final process.Process pProc, final Activity pTask, final IMatchProcessor<? super ProcessTasksMatch> processor) {
    rawForEachMatch(new Object[]{pProc, pTask}, processor);
  }
  
  /**
   * Executes the given processor on an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pProc the fixed value of pattern parameter Proc, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @param processor the action that will process the selected match.
   * @return true if the pattern has at least one match with the given parameter values, false if the processor was not invoked
   * 
   */
  public boolean forOneArbitraryMatch(final process.Process pProc, final Activity pTask, final IMatchProcessor<? super ProcessTasksMatch> processor) {
    return rawForOneArbitraryMatch(new Object[]{pProc, pTask}, processor);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pProc the fixed value of pattern parameter Proc, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public ProcessTasksMatch newMatch(final process.Process pProc, final Activity pTask) {
    return ProcessTasksMatch.newMatch(pProc, pTask);
  }
  
  /**
   * Retrieve the set of values that occur in matches for Proc.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<process.Process> rawAccumulateAllValuesOfProc(final Object[] parameters) {
    Set<process.Process> results = new HashSet<process.Process>();
    rawAccumulateAllValues(POSITION_PROC, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for Proc.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<process.Process> getAllValuesOfProc() {
    return rawAccumulateAllValuesOfProc(emptyArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Proc.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<process.Process> getAllValuesOfProc(final ProcessTasksMatch partialMatch) {
    return rawAccumulateAllValuesOfProc(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Proc.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<process.Process> getAllValuesOfProc(final Activity pTask) {
    return rawAccumulateAllValuesOfProc(new Object[]{
    null, 
    pTask
    });
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<Activity> rawAccumulateAllValuesOfTask(final Object[] parameters) {
    Set<Activity> results = new HashSet<Activity>();
    rawAccumulateAllValues(POSITION_TASK, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Activity> getAllValuesOfTask() {
    return rawAccumulateAllValuesOfTask(emptyArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Activity> getAllValuesOfTask(final ProcessTasksMatch partialMatch) {
    return rawAccumulateAllValuesOfTask(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Activity> getAllValuesOfTask(final process.Process pProc) {
    return rawAccumulateAllValuesOfTask(new Object[]{
    pProc, 
    null
    });
  }
  
  @Override
  protected ProcessTasksMatch tupleToMatch(final Tuple t) {
    try {
    	return ProcessTasksMatch.newMatch((process.Process) t.get(POSITION_PROC), (process.Activity) t.get(POSITION_TASK));
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in tuple not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ProcessTasksMatch arrayToMatch(final Object[] match) {
    try {
    	return ProcessTasksMatch.newMatch((process.Process) match[POSITION_PROC], (process.Activity) match[POSITION_TASK]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ProcessTasksMatch arrayToMatchMutable(final Object[] match) {
    try {
    	return ProcessTasksMatch.newMutableMatch((process.Process) match[POSITION_PROC], (process.Activity) match[POSITION_TASK]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  /**
   * @return the singleton instance of the query specification of this pattern
   * @throws IncQueryException if the pattern definition could not be loaded
   * 
   */
  public static IQuerySpecification<ProcessTasksMatcher> querySpecification() throws IncQueryException {
    return ProcessTasksQuerySpecification.instance();
  }
}
