package operation.queries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import operation.ChecklistEntry;
import operation.queries.ChecklistEntryJobCorrespondenceMatch;
import operation.queries.util.ChecklistEntryJobCorrespondenceQuerySpecification;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseMatcher;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.util.IncQueryLoggingUtil;
import system.Job;

/**
 * Generated pattern matcher API of the operation.queries.ChecklistEntryJobCorrespondence pattern,
 * providing pattern-specific query methods.
 * 
 * <p>Use the pattern matcher on a given model via {@link #on(IncQueryEngine)},
 * e.g. in conjunction with {@link IncQueryEngine#on(Notifier)}.
 * 
 * <p>Matches of the pattern will be represented as {@link ChecklistEntryJobCorrespondenceMatch}.
 * 
 * <p>Original source:
 * <code><pre>
 * ChecklistEntry.jobs relation 
 * {@literal @}QueryBasedFeature(feature = "jobs")
 * pattern ChecklistEntryJobCorrespondence(CLE : ChecklistEntry, Job : Job) = {
 *   Job.name(Job,JobName);
 *   System.name(System,SysName);
 *   Job.runsOn(Job,System);
 *   ChecklistEntry.jobPaths(CLE,JobPath);
 *   check((JobPath as String).equals((SysName as String).concat('/').concat(JobName as String)));
 * }
 * </pre></code>
 * 
 * @see ChecklistEntryJobCorrespondenceMatch
 * @see ChecklistEntryJobCorrespondenceProcessor
 * @see ChecklistEntryJobCorrespondenceQuerySpecification
 * 
 */
@SuppressWarnings("all")
public class ChecklistEntryJobCorrespondenceMatcher extends BaseMatcher<ChecklistEntryJobCorrespondenceMatch> {
  /**
   * Initializes the pattern matcher within an existing EMF-IncQuery engine.
   * If the pattern matcher is already constructed in the engine, only a light-weight reference is returned.
   * The match set will be incrementally refreshed upon updates.
   * @param engine the existing EMF-IncQuery engine in which this matcher will be created.
   * @throws IncQueryException if an error occurs during pattern matcher creation
   * 
   */
  public static ChecklistEntryJobCorrespondenceMatcher on(final IncQueryEngine engine) throws IncQueryException {
    // check if matcher already exists
    ChecklistEntryJobCorrespondenceMatcher matcher = engine.getExistingMatcher(querySpecification());
    if (matcher == null) {
    	matcher = new ChecklistEntryJobCorrespondenceMatcher(engine);
    	// do not have to "put" it into engine.matchers, reportMatcherInitialized() will take care of it
    }
    return matcher;
  }
  
  private final static int POSITION_CLE = 0;
  
  private final static int POSITION_JOB = 1;
  
  private final static Logger LOGGER = IncQueryLoggingUtil.getLogger(ChecklistEntryJobCorrespondenceMatcher.class);
  
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
  public ChecklistEntryJobCorrespondenceMatcher(final Notifier emfRoot) throws IncQueryException {
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
  public ChecklistEntryJobCorrespondenceMatcher(final IncQueryEngine engine) throws IncQueryException {
    super(engine, querySpecification());
  }
  
  /**
   * Returns the set of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @return matches represented as a ChecklistEntryJobCorrespondenceMatch object.
   * 
   */
  public Collection<ChecklistEntryJobCorrespondenceMatch> getAllMatches(final ChecklistEntry pCLE, final Job pJob) {
    return rawGetAllMatches(new Object[]{pCLE, pJob});
  }
  
  /**
   * Returns an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @return a match represented as a ChecklistEntryJobCorrespondenceMatch object, or null if no match is found.
   * 
   */
  public ChecklistEntryJobCorrespondenceMatch getOneArbitraryMatch(final ChecklistEntry pCLE, final Job pJob) {
    return rawGetOneArbitraryMatch(new Object[]{pCLE, pJob});
  }
  
  /**
   * Indicates whether the given combination of specified pattern parameters constitute a valid pattern match,
   * under any possible substitution of the unspecified parameters (if any).
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @return true if the input is a valid (partial) match of the pattern.
   * 
   */
  public boolean hasMatch(final ChecklistEntry pCLE, final Job pJob) {
    return rawHasMatch(new Object[]{pCLE, pJob});
  }
  
  /**
   * Returns the number of all matches of the pattern that conform to the given fixed values of some parameters.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @return the number of pattern matches found.
   * 
   */
  public int countMatches(final ChecklistEntry pCLE, final Job pJob) {
    return rawCountMatches(new Object[]{pCLE, pJob});
  }
  
  /**
   * Executes the given processor on each match of the pattern that conforms to the given fixed values of some parameters.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param processor the action that will process each pattern match.
   * 
   */
  public void forEachMatch(final ChecklistEntry pCLE, final Job pJob, final IMatchProcessor<? super ChecklistEntryJobCorrespondenceMatch> processor) {
    rawForEachMatch(new Object[]{pCLE, pJob}, processor);
  }
  
  /**
   * Executes the given processor on an arbitrarily chosen match of the pattern that conforms to the given fixed values of some parameters.
   * Neither determinism nor randomness of selection is guaranteed.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @param processor the action that will process the selected match.
   * @return true if the pattern has at least one match with the given parameter values, false if the processor was not invoked
   * 
   */
  public boolean forOneArbitraryMatch(final ChecklistEntry pCLE, final Job pJob, final IMatchProcessor<? super ChecklistEntryJobCorrespondenceMatch> processor) {
    return rawForOneArbitraryMatch(new Object[]{pCLE, pJob}, processor);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pCLE the fixed value of pattern parameter CLE, or null if not bound.
   * @param pJob the fixed value of pattern parameter Job, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public ChecklistEntryJobCorrespondenceMatch newMatch(final ChecklistEntry pCLE, final Job pJob) {
    return ChecklistEntryJobCorrespondenceMatch.newMatch(pCLE, pJob);
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
  public Set<ChecklistEntry> getAllValuesOfCLE(final ChecklistEntryJobCorrespondenceMatch partialMatch) {
    return rawAccumulateAllValuesOfCLE(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for CLE.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<ChecklistEntry> getAllValuesOfCLE(final Job pJob) {
    return rawAccumulateAllValuesOfCLE(new Object[]{
    null, 
    pJob
    });
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
  public Set<Job> getAllValuesOfJob(final ChecklistEntryJobCorrespondenceMatch partialMatch) {
    return rawAccumulateAllValuesOfJob(partialMatch.toArray());
  }
  
  /**
   * Retrieve the set of values that occur in matches for Job.
   * @return the Set of all values, null if no parameter with the given name exists, empty set if there are no matches
   * 
   */
  public Set<Job> getAllValuesOfJob(final ChecklistEntry pCLE) {
    return rawAccumulateAllValuesOfJob(new Object[]{
    pCLE, 
    null
    });
  }
  
  @Override
  protected ChecklistEntryJobCorrespondenceMatch tupleToMatch(final Tuple t) {
    try {
    	return ChecklistEntryJobCorrespondenceMatch.newMatch((operation.ChecklistEntry) t.get(POSITION_CLE), (system.Job) t.get(POSITION_JOB));
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in tuple not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ChecklistEntryJobCorrespondenceMatch arrayToMatch(final Object[] match) {
    try {
    	return ChecklistEntryJobCorrespondenceMatch.newMatch((operation.ChecklistEntry) match[POSITION_CLE], (system.Job) match[POSITION_JOB]);
    } catch(ClassCastException e) {
    	LOGGER.error("Element(s) in array not properly typed!",e);
    	return null;
    }
  }
  
  @Override
  protected ChecklistEntryJobCorrespondenceMatch arrayToMatchMutable(final Object[] match) {
    try {
    	return ChecklistEntryJobCorrespondenceMatch.newMutableMatch((operation.ChecklistEntry) match[POSITION_CLE], (system.Job) match[POSITION_JOB]);
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
  public static IQuerySpecification<ChecklistEntryJobCorrespondenceMatcher> querySpecification() throws IncQueryException {
    return ChecklistEntryJobCorrespondenceQuerySpecification.instance();
  }
}
