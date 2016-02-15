package org.eclipse.incquery.examples.bpm.queries.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.incquery.examples.bpm.queries.ProcessTasksMatch;
import org.eclipse.incquery.examples.bpm.queries.ProcessTasksMatcher;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedEMFPQuery;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedEMFQuerySpecification;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.matchers.psystem.PBody;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.Equality;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.ExportedParameter;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.TypeConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PParameter;
import org.eclipse.incquery.runtime.matchers.psystem.queries.QueryInitializationException;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;

/**
 * A pattern-specific query specification that can instantiate ProcessTasksMatcher in a type-safe way.
 * 
 * @see ProcessTasksMatcher
 * @see ProcessTasksMatch
 * 
 */
@SuppressWarnings("all")
public final class ProcessTasksQuerySpecification extends BaseGeneratedEMFQuerySpecification<ProcessTasksMatcher> {
  private ProcessTasksQuerySpecification() {
    super(GeneratedPQuery.INSTANCE);
  }
  
  /**
   * @return the singleton instance of the query specification
   * @throws IncQueryException if the pattern definition could not be loaded
   * 
   */
  public static ProcessTasksQuerySpecification instance() throws IncQueryException {
    try{
    	return LazyHolder.INSTANCE;
    } catch (ExceptionInInitializerError err) {
    	throw processInitializerError(err);
    }
  }
  
  @Override
  protected ProcessTasksMatcher instantiate(final IncQueryEngine engine) throws IncQueryException {
    return ProcessTasksMatcher.on(engine);
  }
  
  @Override
  public ProcessTasksMatch newEmptyMatch() {
    return ProcessTasksMatch.newEmptyMatch();
  }
  
  @Override
  public ProcessTasksMatch newMatch(final Object... parameters) {
    return ProcessTasksMatch.newMatch((process.Process) parameters[0], (process.Activity) parameters[1]);
  }
  
  private static class LazyHolder {
    private final static ProcessTasksQuerySpecification INSTANCE = make();
    
    public static ProcessTasksQuerySpecification make() {
      return new ProcessTasksQuerySpecification();					
    }
  }
  
  private static class GeneratedPQuery extends BaseGeneratedEMFPQuery {
    private final static ProcessTasksQuerySpecification.GeneratedPQuery INSTANCE = new GeneratedPQuery();
    
    @Override
    public String getFullyQualifiedName() {
      return "org.eclipse.incquery.examples.bpm.queries.processTasks";
    }
    
    @Override
    public List<String> getParameterNames() {
      return Arrays.asList("Proc","Task");
    }
    
    @Override
    public List<PParameter> getParameters() {
      return Arrays.asList(new PParameter("Proc", "process.Process"),new PParameter("Task", "process.Activity"));
    }
    
    @Override
    public Set<PBody> doGetContainedBodies() throws QueryInitializationException {
      Set<PBody> bodies = Sets.newLinkedHashSet();
      try {
      	{
      		PBody body = new PBody(this);
      		PVariable var_Proc = body.getOrCreateVariableByName("Proc");
      		PVariable var_Task = body.getOrCreateVariableByName("Task");
      		PVariable var__virtual_0_ = body.getOrCreateVariableByName(".virtual{0}");
      		body.setExportedParameters(Arrays.<ExportedParameter>asList(
      			new ExportedParameter(body, var_Proc, "Proc"),
      			
      			new ExportedParameter(body, var_Task, "Task")
      		));
      		new TypeConstraint(body, new FlatTuple(var_Proc), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://process/1.0", "Process")));
      		new TypeConstraint(body, new FlatTuple(var_Proc, var__virtual_0_), new EStructuralFeatureInstancesKey(getFeatureLiteral("http://process/1.0", "Process", "contents")));
      		new Equality(body, var__virtual_0_, var_Task);
      		bodies.add(body);
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
