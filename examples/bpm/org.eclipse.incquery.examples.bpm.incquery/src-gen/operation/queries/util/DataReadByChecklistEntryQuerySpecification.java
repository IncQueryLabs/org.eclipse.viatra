package operation.queries.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import operation.queries.DataReadByChecklistEntryMatch;
import operation.queries.DataReadByChecklistEntryMatcher;
import operation.queries.util.ChecklistEntryTaskCorrespondenceQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedEMFPQuery;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedEMFQuerySpecification;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.matchers.psystem.PBody;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.annotations.PAnnotation;
import org.eclipse.incquery.runtime.matchers.psystem.annotations.ParameterReference;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.ExportedParameter;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.PositivePatternCall;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PParameter;
import org.eclipse.incquery.runtime.matchers.psystem.queries.QueryInitializationException;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import system.queries.util.DataTaskReadCorrespondenceQuerySpecification;

/**
 * A pattern-specific query specification that can instantiate DataReadByChecklistEntryMatcher in a type-safe way.
 * 
 * @see DataReadByChecklistEntryMatcher
 * @see DataReadByChecklistEntryMatch
 * 
 */
@SuppressWarnings("all")
public final class DataReadByChecklistEntryQuerySpecification extends BaseGeneratedEMFQuerySpecification<DataReadByChecklistEntryMatcher> {
  private DataReadByChecklistEntryQuerySpecification() {
    super(GeneratedPQuery.INSTANCE);
  }
  
  /**
   * @return the singleton instance of the query specification
   * @throws IncQueryException if the pattern definition could not be loaded
   * 
   */
  public static DataReadByChecklistEntryQuerySpecification instance() throws IncQueryException {
    try{
    	return LazyHolder.INSTANCE;
    } catch (ExceptionInInitializerError err) {
    	throw processInitializerError(err);
    }
  }
  
  @Override
  protected DataReadByChecklistEntryMatcher instantiate(final IncQueryEngine engine) throws IncQueryException {
    return DataReadByChecklistEntryMatcher.on(engine);
  }
  
  @Override
  public DataReadByChecklistEntryMatch newEmptyMatch() {
    return DataReadByChecklistEntryMatch.newEmptyMatch();
  }
  
  @Override
  public DataReadByChecklistEntryMatch newMatch(final Object... parameters) {
    return DataReadByChecklistEntryMatch.newMatch((operation.ChecklistEntry) parameters[0], (process.Task) parameters[1], (system.Data) parameters[2]);
  }
  
  private static class LazyHolder {
    private final static DataReadByChecklistEntryQuerySpecification INSTANCE = make();
    
    public static DataReadByChecklistEntryQuerySpecification make() {
      return new DataReadByChecklistEntryQuerySpecification();					
    }
  }
  
  private static class GeneratedPQuery extends BaseGeneratedEMFPQuery {
    private final static DataReadByChecklistEntryQuerySpecification.GeneratedPQuery INSTANCE = new GeneratedPQuery();
    
    @Override
    public String getFullyQualifiedName() {
      return "operation.queries.DataReadByChecklistEntry";
    }
    
    @Override
    public List<String> getParameterNames() {
      return Arrays.asList("CLE","Task","Data");
    }
    
    @Override
    public List<PParameter> getParameters() {
      return Arrays.asList(new PParameter("CLE", "operation.ChecklistEntry"),new PParameter("Task", "process.Task"),new PParameter("Data", "system.Data"));
    }
    
    @Override
    public Set<PBody> doGetContainedBodies() throws QueryInitializationException {
      Set<PBody> bodies = Sets.newLinkedHashSet();
      try {
      	{
      		PBody body = new PBody(this);
      		PVariable var_CLE = body.getOrCreateVariableByName("CLE");
      		PVariable var_Task = body.getOrCreateVariableByName("Task");
      		PVariable var_Data = body.getOrCreateVariableByName("Data");
      		body.setExportedParameters(Arrays.<ExportedParameter>asList(
      			new ExportedParameter(body, var_CLE, "CLE"),
      			
      			new ExportedParameter(body, var_Task, "Task"),
      			
      			new ExportedParameter(body, var_Data, "Data")
      		));
      		new PositivePatternCall(body, new FlatTuple(var_CLE, var_Task), ChecklistEntryTaskCorrespondenceQuerySpecification.instance().getInternalQueryRepresentation());
      		new PositivePatternCall(body, new FlatTuple(var_Data, var_Task), DataTaskReadCorrespondenceQuerySpecification.instance().getInternalQueryRepresentation());
      		bodies.add(body);
      	}
      	{
      		PAnnotation annotation = new PAnnotation("Constraint");
      		annotation.addAttribute("severity", "warning");
      		annotation.addAttribute("location", new ParameterReference("CLE"));
      		annotation.addAttribute("message", "Entry $CLE.name$ connected to $Data.name$ through $Task.name$");
      		addAnnotation(annotation);
      	}
      	// to silence compiler error
      	if (false) throw new IncQueryException("Never", "happens");
      } catch (IncQueryException ex) {
      	throw processDependencyException(ex);
      }
      return bodies;
    }
  }
}
