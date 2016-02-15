package system.queries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
import system.Job;
import system.queries.JobTaskCorrespondenceMatch;
import system.queries.util.JobTaskCorrespondenceQuerySpecification;

/**
 * Generated pattern matcher API of the system.queries.JobTaskCorrespondence pattern,
 * providing pattern-specific query methods.
 * 
 * <p>Use the pattern matcher on a given model via {@link #on(IncQueryEngine)},
 * e.g. in conjunction with {@link IncQueryEngine#on(Notifier)}.
 * 
 * <p>Matches of the pattern will be represented as {@link JobTaskCorrespondenceMatch}.
 * 
 * <p>Original source:
 * <code><pre>
 * // first query n-to-m traceability
 *  Job.tasks relation 
 * {@literal @}QueryBasedFeature(feature = "tasks")
 * pattern JobTaskCorrespondence(Job : Job, Task : Task) = {
 * 	Job.taskIds(Job,TaskId);
 * 	Task.id(Task,TaskId);
 * }
 * </pre></code>
 * 
 * @see JobTaskCorrespondenceMatch
 * @see JobTaskCorrespondenceProcessor
 * @see JobTaskCorrespondenceQuerySpecification
 * 
 */
@SuppressWarnings("all")
public class JobTaskCorrespondenceMatcher extends BaseMatcher<JobTaskCorrespondenceMatch> {
  /**
   * Initializes the pattern matcher within an existing EMF-IncQuery engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing EMF-IncQuery engine in which this matcher will be created.
   * @throws IncQueryException if an error occurs during pattern matcher creation
   * 
   */
  public static JobTaskCorrespondenceMatcher on(final IncQueryEngine engine) throws IncQueryException {
    // check if matcher already exists
    JobTaskCorrespondenceMatcher matcher = engine.getExistingMatcher(querySpecification());
    if (matcher == null) {
    	matcher = new JobTaskCorrespondenceMatcher(engine);
    	// do not have to "put" it into engine.matchers, reportMatcherInitialized() will take care of it
    }
    return matcher;
  }
  
  private final static int POSITION_JOB = 0;
  
  private final static int POSITION_TASK = 1;
  
  private final static Logger LOGGER = IncQueryLoggingUtil.getLogger(JobTaskCorrespondenceMatcher.class);
  
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
  public JobTaskCorrespondenceMatcher(final Notifier emfRoot) throws IncQueryException {
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
  public JobTaskCorrespondenceMatcher(final IncQueryEngine engine) throws IncQueryException {
    super(engine, querySpecification());
  }
  
  /**
   * Returns the set of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return matches represented as a JobTaskCorrespondenceMatch object.
   * 
   */
  public Collection<JobTaskCorrespondenceMatch> getAllMatches(final Job pJob, final Task pTask) {
    return rawGetAllMatches(new Object[]{pJob, pTask});
  }
  
  /**
   * Returns an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return a match represented as a JobTaskCorrespondenceMatch object, or null if no match is found.
   * 
   */
  public JobTaskCorrespondenceMatch getOneArbitraryMatch(final Job pJob, final Task pTask) {
    return rawGetOneArbitraryMatch(new Object[]{pJob, pTask});
  }
  
  /**
   * Indicates whether the given combination of specified pattern parameters constitute a valid pattern match,
   * under any possible substitution of the unspecified parameters (if any).
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return true if the input is a valid (partial) match of the pattern.
   * 
   */
  public boolean hasMatch(final Job pJob, final Task pTask) {
    return rawHasMatch(new Object[]{pJob, pTask});
  }
  
  /**
   * Returns the number of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the number of pattern matches found.
   * 
   */
  public int countMatches(final Job pJob, final Task pTask) {
    return rawCountMatches(new Object[]{pJob, pTask});
  }
  
  /**
   * Executes the given processor on each match of the pattern that conforms to the given fixed values of some parameters.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @param processor the action that will process each pattern match.
   * 
   */
  public void forEachMatch(final Job pJob, final Task pTask, final IMatchProcessor<? super JobTaskCorrespondenceMatch> processor) {
    rawForEachMatch(new Object[]{pJob, pTask}, processor);
  }
  
  /**
   * Executes the given processor on an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @param processor the action that will process the selected match.
   * @return true if the pattern has at least one match with the given parameter values, false if the processor was not invoked
   * 
   */
  public boolean forOneArbitraryMatch(final Job pJob, final Task pTask, final IMatchProcessor<? super JobTaskCorrespondenceMatch> processor) {
    return rawForOneArbitraryMatch(new Object[]{pJob, pTask}, processor);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public JobTaskCorrespondenceMatch newMatch(final Job pJob, final Task pTask) {
    return JobTaskCorrespondenceMatch.newMatch(pJob, pTask);
  }
  
  /**
   * Retrieve the set of values that occur in matches for Job.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  protected Set<Job> rawAccumulateAllValuesOfJob(final Object[] parameters) {
    Set<Job> results = new HashSet<Job>();
    rawAccumulateAllValues(POSITION_JOB, parameters, results);
    return results;
  }
  
  /**
   * Retrieve the set of values that occur in matches for Job.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Job> getAllValuesOfJob() {
    return rawAccumulateAllValuesOfJob(emptyArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Job.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Job> getAllValuesOfJob(final JobTaskCorrespondenceMatch partialMatch) {
    return rawAccumulateAllValuesOfJob(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Job.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Job> getAllValuesOfJob(final Task pTask) {
    return rawAccumulateAllValuesOfJob(new Object[]{
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
  public Set<Task> getAllValuesOfTask(final JobTaskCorrespondenceMatch partialMatch) {
    return rawAccumulateAllValuesOfTask(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Task.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Task> getAllValuesOfTask(final Job pJob) {
    return rawAccumulateAllValuesOfTask(new Object[]{
    pJob, 
    null
    });
  }
  
  @Override
  protected JobTaskCorrespondenceMatch tupleToMatch(final Tuple t) {
    try {
    	return JobTaskCorrespondenceMatch.newMatch((system.Job) t.get(POSITION_JOB), (process.Task) t.get(POSITION_TASK));
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in tuple not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected JobTaskCorrespondenceMatch arrayToMatch(final Object[] match) {
    try {
    	return JobTaskCorrespondenceMatch.newMatch((system.Job) match[POSITION_JOB], (process.Task) match[POSITION_TASK]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected JobTaskCorrespondenceMatch arrayToMatchMutable(final Object[] match) {
    try {
    	return JobTaskCorrespondenceMatch.newMutableMatch((system.Job) match[POSITION_JOB], (process.Task) match[POSITION_TASK]);
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
  public static IQuerySpecification<JobTaskCorrespondenceMatcher> querySpecification() throws IncQueryException {
    return JobTaskCorrespondenceQuerySpecification.instance();
  }
}
