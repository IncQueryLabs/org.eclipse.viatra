package org.eclipse.incquery.examples.bpm.queries;

import org.eclipse.incquery.examples.bpm.queries.EntryTaskMatcher;
import org.eclipse.incquery.examples.bpm.queries.JobTasksMatcher;
import org.eclipse.incquery.examples.bpm.queries.NextActivityMatcher;
import org.eclipse.incquery.examples.bpm.queries.ProcessTasksMatcher;
import org.eclipse.incquery.examples.bpm.queries.util.EntryTaskQuerySpecification;
import org.eclipse.incquery.examples.bpm.queries.util.JobTasksQuerySpecification;
import org.eclipse.incquery.examples.bpm.queries.util.NextActivityQuerySpecification;
import org.eclipse.incquery.examples.bpm.queries.util.ProcessTasksQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedPatternGroup;
import org.eclipse.incquery.runtime.exception.IncQueryException;

/**
 * A pattern group formed of all patterns defined in bpmTestQueries.eiq.
 * 
 * <p>Use the static instance as any {@link org.eclipse.incquery.runtime.api.IPatternGroup}, to conveniently prepare
 * an EMF-IncQuery engine for matching all patterns originally defined in file bpmTestQueries.eiq,
 * in order to achieve better performance than one-by-one on-demand matcher initialization.
 * 
 * <p> From package org.eclipse.incquery.examples.bpm.queries, the group contains the definition of the following patterns: <ul>
 * <li>processTasks</li>
 * <li>nextActivity</li>
 * <li>jobTasks</li>
 * <li>entryTask</li>
 * </ul>
 * 
 * @see IPatternGroup
 * 
 */
@SuppressWarnings("all")
public final class BpmTestQueries extends BaseGeneratedPatternGroup {
  /**
   * Access the pattern group.
   * 
   * @return the singleton instance of the group
   * @throws IncQueryException if there was an error loading the generated code of pattern specifications
   * 
   */
  public static BpmTestQueries instance() throws IncQueryException {
    if (INSTANCE == null) {
    	INSTANCE = new BpmTestQueries();
    }
    return INSTANCE;
  }
  
  private static BpmTestQueries INSTANCE;
  
  private BpmTestQueries() throws IncQueryException {
    querySpecifications.add(ProcessTasksQuerySpecification.instance());
    querySpecifications.add(NextActivityQuerySpecification.instance());
    querySpecifications.add(JobTasksQuerySpecification.instance());
    querySpecifications.add(EntryTaskQuerySpecification.instance());
  }
  
  public ProcessTasksQuerySpecification getProcessTasks() throws IncQueryException {
    return ProcessTasksQuerySpecification.instance();
  }
  
  public ProcessTasksMatcher getProcessTasks(final IncQueryEngine engine) throws IncQueryException {
    return ProcessTasksMatcher.on(engine);
  }
  
  public NextActivityQuerySpecification getNextActivity() throws IncQueryException {
    return NextActivityQuerySpecification.instance();
  }
  
  public NextActivityMatcher getNextActivity(final IncQueryEngine engine) throws IncQueryException {
    return NextActivityMatcher.on(engine);
  }
  
  public JobTasksQuerySpecification getJobTasks() throws IncQueryException {
    return JobTasksQuerySpecification.instance();
  }
  
  public JobTasksMatcher getJobTasks(final IncQueryEngine engine) throws IncQueryException {
    return JobTasksMatcher.on(engine);
  }
  
  public EntryTaskQuerySpecification getEntryTask() throws IncQueryException {
    return EntryTaskQuerySpecification.instance();
  }
  
  public EntryTaskMatcher getEntryTask(final IncQueryEngine engine) throws IncQueryException {
    return EntryTaskMatcher.on(engine);
  }
}
