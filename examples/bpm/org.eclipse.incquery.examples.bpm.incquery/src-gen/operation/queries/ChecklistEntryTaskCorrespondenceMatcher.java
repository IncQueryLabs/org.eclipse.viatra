package operation.queries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import operation.ChecklistEntry;
import operation.queries.ChecklistEntryTaskCorrespondenceMatch;
import operation.queries.util.ChecklistEntryTaskCorrespondenceQuerySpecification;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseMatcher;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.util.IncQueryLoggingUtil;
import process.Task;

/**
 * Generated pattern matcher API of the operation.queries.ChecklistEntryTaskCorrespondence pattern,
 * providing pattern-specific query methods.
 * 
 * <p>Use the pattern matcher on a given model via {@link #on(IncQueryEngine)},
 * e.g. in conjunction with {@link IncQueryEngine#on(Notifier)}.
 * 
 * <p>Matches of the pattern will be represented as {@link ChecklistEntryTaskCorrespondenceMatch}.
 * 
 * <p>Original source:
 * <code><pre>
 * ChecklistEntry.task relation 
 * {@literal @}QueryBasedFeature(feature = "task")
 * pattern ChecklistEntryTaskCorrespondence(CLE : ChecklistEntry, Task : Task) = {
 * 	Task.id(Task, TaskId);
 * 	ChecklistEntry.taskId(CLE,TaskId);
 * }
 * </pre></code>
 * 
 * @see ChecklistEntryTaskCorrespondenceMatch
 * @see ChecklistEntryTaskCorrespondenceProcessor
 * @see ChecklistEntryTaskCorrespondenceQuerySpecification
 * 
 */
@SuppressWarnings("all")
public class ChecklistEntryTaskCorrespondenceMatcher extends BaseMatcher<ChecklistEntryTaskCorrespondenceMatch> {
  /**
   * Initializes the pattern matcher within an existing EMF-IncQuery engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing EMF-IncQuery engine in which this matcher will be created.
   * @throws IncQueryException if an error occurs during pattern matcher creation
   * 
   */
  public static ChecklistEntryTaskCorrespondenceMatcher on(final IncQueryEngine engine) throws IncQueryException {
    // check if matcher already exists
    ChecklistEntryTaskCorrespondenceMatcher matcher = engine.getExistingMatcher(querySpecification());
    if (matcher == null) {
    	matcher = new ChecklistEntryTaskCorrespondenceMatcher(engine);
    	// do not have to "put" it into engine.matchers, reportMatcherInitialized() will take care of it
    }
    return matcher;
  }
  
  private final static int POSITION_CLE = 0;
  
  private final static int POSITION_TASK = 1;
  
  private final static Logger LOGGER = IncQueryLoggingUtil.getLogger(ChecklistEntryTaskCorrespondenceMatcher.class);
  
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
  public ChecklistEntryTaskCorrespondenceMatcher(final Notifier emfRoot) throws IncQueryException {
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
  public ChecklistEntryTaskCorrespondenceMatcher(final IncQueryEngine engine) throws IncQueryException {
    super(engine, querySpecification());
  }
  
  /**
   * Returns the set of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return matches represented as a ChecklistEntryTaskCorrespondenceMatch object.
   * 
   */
  public Collection<ChecklistEntryTaskCorrespondenceMatch> getAllMatches(final ChecklistEntry pCLE, final Task pTask) {
    return rawGetAllMatches(new Object[]{pCLE, pTask});
  }
  
  /**
   * Returns an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return a match represented as a ChecklistEntryTaskCorrespondenceMatch object, or null if no match is found.
   * 
   */
  public ChecklistEntryTaskCorrespondenceMatch getOneArbitraryMatch(final ChecklistEntry pCLE, final Task pTask) {
    return rawGetOneArbitraryMatch(new Object[]{pCLE, pTask});
  }
  
  /**
   * Indicates whether the given combination of specified pattern parameters constitute a valid pattern match,
   * under any possible substitution of the unspecified parameters (if any).
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return true if the input is a valid (partial) match of the pattern.
   * 
   */
  public boolean hasMatch(final ChecklistEntry pCLE, final Task pTask) {
    return rawHasMatch(new Object[]{pCLE, pTask});
  }
  
  /**
   * Returns the number of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the number of pattern matches found.
   * 
   */
  public int countMatches(final ChecklistEntry pCLE, final Task pTask) {
    return rawCountMatches(new Object[]{pCLE, pTask});
  }
  
  /**
   * Executes the given processor on each match of the pattern that conforms to the given fixed values of some parameters.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @param processor the action that will process each pattern match.
   * 
   */
  public void forEachMatch(final ChecklistEntry pCLE, final Task pTask, final IMatchProcessor<? super ChecklistEntryTaskCorrespondenceMatch> processor) {
    rawForEachMatch(new Object[]{pCLE, pTask}, processor);
  }
  
  /**
   * Executes the given processor on an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @param processor the action that will process the selected match.
   * @return true if the pattern has at least one match with the given parameter values, false if the processor was not invoked
   * 
   */
  public boolean forOneArbitraryMatch(final ChecklistEntry pCLE, final Task pTask, final IMatchProcessor<? super ChecklistEntryTaskCorrespondenceMatch> processor) {
    return rawForOneArbitraryMatch(new Object[]{pCLE, pTask}, processor);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public ChecklistEntryTaskCorrespondenceMatch newMatch(final ChecklistEntry pCLE, final Task pTask) {
    return ChecklistEntryTaskCorrespondenceMatch.newMatch(pCLE, pTask);
  }
  
  /**
   * Retrieve the set of values that occur in matches for CLE.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<ChecklistEntry> rawAccumulateAllValuesOfCLE(final Object[] parameters) {
    Set<ChecklistEntry> results = new HashSet<ChecklistEntry>();
    rawAccumulateAllValues(POSITION_CLE, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for CLE.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<ChecklistEntry> getAllValuesOfCLE() {
    return rawAccumulateAllValuesOfCLE(emptyArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for CLE.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<ChecklistEntry> getAllValuesOfCLE(final ChecklistEntryTaskCorrespondenceMatch partialMatch) {
    return rawAccumulateAllValuesOfCLE(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for CLE.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<ChecklistEntry> getAllValuesOfCLE(final Task pTask) {
    return rawAccumulateAllValuesOfCLE(new Object[]{
    null, 
    pTask
    });
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<Task> rawAccumulateAllValuesOfTask(final Object[] parameters) {
    Set<Task> results = new HashSet<Task>();
    rawAccumulateAllValues(POSITION_TASK, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Task> getAllValuesOfTask() {
    return rawAccumulateAllValuesOfTask(emptyArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Task> getAllValuesOfTask(final ChecklistEntryTaskCorrespondenceMatch partialMatch) {
    return rawAccumulateAllValuesOfTask(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Task> getAllValuesOfTask(final ChecklistEntry pCLE) {
    return rawAccumulateAllValuesOfTask(new Object[]{
    pCLE, 
    null
    });
  }
  
  @Override
  protected ChecklistEntryTaskCorrespondenceMatch tupleToMatch(final Tuple t) {
    try {
    	return ChecklistEntryTaskCorrespondenceMatch.newMatch((operation.ChecklistEntry) t.get(POSITION_CLE), (process.Task) t.get(POSITION_TASK));
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in tuple not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ChecklistEntryTaskCorrespondenceMatch arrayToMatch(final Object[] match) {
    try {
    	return ChecklistEntryTaskCorrespondenceMatch.newMatch((operation.ChecklistEntry) match[POSITION_CLE], (process.Task) match[POSITION_TASK]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ChecklistEntryTaskCorrespondenceMatch arrayToMatchMutable(final Object[] match) {
    try {
    	return ChecklistEntryTaskCorrespondenceMatch.newMutableMatch((operation.ChecklistEntry) match[POSITION_CLE], (process.Task) match[POSITION_TASK]);
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
  public static IQuerySpecification<ChecklistEntryTaskCorrespondenceMatcher> querySpecification() throws IncQueryException {
    return ChecklistEntryTaskCorrespondenceQuerySpecification.instance();
  }
}
