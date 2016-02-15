package operation.queries.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import operation.queries.IncorrectEntryInChecklistMatch;
import operation.queries.IncorrectEntryInChecklistMatcher;
import operation.queries.util.ChecklistEntryTaskCorrespondenceQuerySpecification;
import operation.queries.util.ChecklistProcessCorrespondenceQuerySpecification;
import operation.queries.util.TaskInProcessQuerySpecification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedEMFPQuery;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedEMFQuerySpecification;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.matchers.psystem.PBody;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.annotations.PAnnotation;
import org.eclipse.incquery.runtime.matchers.psystem.annotations.ParameterReference;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.Equality;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.ExportedParameter;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.NegativePatternCall;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.PositivePatternCall;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.TypeConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PParameter;
import org.eclipse.incquery.runtime.matchers.psystem.queries.QueryInitializationException;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;

/**
 * A pattern-specific query specification that can instantiate IncorrectEntryInChecklistMatcher in a type-safe way.
 * 
 * @see IncorrectEntryInChecklistMatcher
 * @see IncorrectEntryInChecklistMatch
 * 
 */
@SuppressWarnings("all")
public final class IncorrectEntryInChecklistQuerySpecification extends BaseGeneratedEMFQuerySpecification<IncorrectEntryInChecklistMatcher> {
  private IncorrectEntryInChecklistQuerySpecification() {
    super(GeneratedPQuery.INSTANCE);
  }
  
  /**
   * @return the singleton instance of the query specification
   * @throws IncQueryException if the pattern definition could not be loaded
   * 
   */
  public static IncorrectEntryInChecklistQuerySpecification instance() throws IncQueryException {
    try{
    	return LazyHolder.INSTANCE;
    } catch (ExceptionInInitializerError err) {
    	throw processInitializerError(err);
    }
  }
  
  @Override
  protected IncorrectEntryInChecklistMatcher instantiate(final IncQueryEngine engine) throws IncQueryException {
    return IncorrectEntryInChecklistMatcher.on(engine);
  }
  
  @Override
  public IncorrectEntryInChecklistMatch newEmptyMatch() {
    return IncorrectEntryInChecklistMatch.newEmptyMatch();
  }
  
  @Override
  public IncorrectEntryInChecklistMatch newMatch(final Object... parameters) {
    return IncorrectEntryInChecklistMatch.newMatch((operation.ChecklistEntry) parameters[0], (process.Task) parameters[1], (process.Process) parameters[2]);
  }
  
  private static class LazyHolder {
    private final static IncorrectEntryInChecklistQuerySpecification INSTANCE = make();
    
    public static IncorrectEntryInChecklistQuerySpecification make() {
      return new IncorrectEntryInChecklistQuerySpecification();					
    }
  }
  
  private static class GeneratedPQuery extends BaseGeneratedEMFPQuery {
    private final static IncorrectEntryInChecklistQuerySpecification.GeneratedPQuery INSTANCE = new GeneratedPQuery();
    
    @Override
    public String getFullyQualifiedName() {
      return "operation.queries.IncorrectEntryInChecklist";
    }
    
    @Override
    public List<String> getParameterNames() {
      return Arrays.asList("ChecklistEntry","Task","Process");
    }
    
    @Override
    public List<PParameter> getParameters() {
      return Arrays.asList(new PParameter("ChecklistEntry", "operation.ChecklistEntry"),new PParameter("Task", "process.Task"),new PParameter("Process", "process.Process"));
    }
    
    @Override
    public Set<PBody> doGetContainedBodies() throws QueryInitializationException {
      Set<PBody> bodies = Sets.newLinkedHashSet();
      try {
      	{
      		PBody body = new PBody(this);
      		PVariable var_ChecklistEntry = body.getOrCreateVariableByName("ChecklistEntry");
      		PVariable var_Task = body.getOrCreateVariableByName("Task");
      		PVariable var_Process = body.getOrCreateVariableByName("Process");
      		PVariable var_Checklist = body.getOrCreateVariableByName("Checklist");
      		PVariable var__virtual_0_ = body.getOrCreateVariableByName(".virtual{0}");
      		body.setExportedParameters(Arrays.<ExportedParameter>asList(
      			new ExportedParameter(body, var_ChecklistEntry, "ChecklistEntry"),
      			
      			new ExportedParameter(body, var_Task, "Task"),
      			
      			new ExportedParameter(body, var_Process, "Process")
      		));
      		new TypeConstraint(body, new FlatTuple(var_Checklist), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://operation/1.0", "Checklist")));
      		new TypeConstraint(body, new FlatTuple(var_Checklist, var__virtual_0_), new EStructuralFeatureInstancesKey(getFeatureLiteral("http://operation/1.0", "Checklist", "entries")));
      		new Equality(body, var__virtual_0_, var_ChecklistEntry);
      		new PositivePatternCall(body, new FlatTuple(var_Checklist, var_Process), ChecklistProcessCorrespondenceQuerySpecification.instance().getInternalQueryRepresentation());
      		new PositivePatternCall(body, new FlatTuple(var_ChecklistEntry, var_Task), ChecklistEntryTaskCorrespondenceQuerySpecification.instance().getInternalQueryRepresentation());
      		new NegativePatternCall(body, new FlatTuple(var_Task, var_Process), TaskInProcessQuerySpecification.instance().getInternalQueryRepresentation());
      		bodies.add(body);
      	}
      	{
      		PAnnotation annotation = new PAnnotation("Constraint");
      		annotation.addAttribute("severity", "error");
      		annotation.addAttribute("location", new ParameterReference("ChecklistEntry"));
      		annotation.addAttribute("message", "Entry $ChecklistEntry.name$ corresponds to Task $Task.name$ outside of process $Process.name$ defined for the checklist!");
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
