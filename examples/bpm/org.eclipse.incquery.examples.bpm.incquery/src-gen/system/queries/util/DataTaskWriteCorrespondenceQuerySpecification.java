package system.queries.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.Equality;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.ExportedParameter;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.TypeConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PParameter;
import org.eclipse.incquery.runtime.matchers.psystem.queries.QueryInitializationException;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import system.queries.DataTaskWriteCorrespondenceMatch;
import system.queries.DataTaskWriteCorrespondenceMatcher;

/**
 * A pattern-specific query specification that can instantiate DataTaskWriteCorrespondenceMatcher in a type-safe way.
 * 
 * @see DataTaskWriteCorrespondenceMatcher
 * @see DataTaskWriteCorrespondenceMatch
 * 
 */
@SuppressWarnings("all")
public final class DataTaskWriteCorrespondenceQuerySpecification extends BaseGeneratedEMFQuerySpecification<DataTaskWriteCorrespondenceMatcher> {
  private DataTaskWriteCorrespondenceQuerySpecification() {
    super(GeneratedPQuery.INSTANCE);
  }
  
  /**
   * @return the singleton instance of the query specification
   * @throws IncQueryException if the pattern definition could not be loaded
   * 
   */
  public static DataTaskWriteCorrespondenceQuerySpecification instance() throws IncQueryException {
    try{
    	return LazyHolder.INSTANCE;
    } catch (ExceptionInInitializerError err) {
    	throw processInitializerError(err);
    }
  }
  
  @Override
  protected DataTaskWriteCorrespondenceMatcher instantiate(final IncQueryEngine engine) throws IncQueryException {
    return DataTaskWriteCorrespondenceMatcher.on(engine);
  }
  
  @Override
  public DataTaskWriteCorrespondenceMatch newEmptyMatch() {
    return DataTaskWriteCorrespondenceMatch.newEmptyMatch();
  }
  
  @Override
  public DataTaskWriteCorrespondenceMatch newMatch(final Object... parameters) {
    return DataTaskWriteCorrespondenceMatch.newMatch((system.Data) parameters[0], (process.Task) parameters[1]);
  }
  
  private static class LazyHolder {
    private final static DataTaskWriteCorrespondenceQuerySpecification INSTANCE = make();
    
    public static DataTaskWriteCorrespondenceQuerySpecification make() {
      return new DataTaskWriteCorrespondenceQuerySpecification();					
    }
  }
  
  private static class GeneratedPQuery extends BaseGeneratedEMFPQuery {
    private final static DataTaskWriteCorrespondenceQuerySpecification.GeneratedPQuery INSTANCE = new GeneratedPQuery();
    
    @Override
    public String getFullyQualifiedName() {
      return "system.queries.DataTaskWriteCorrespondence";
    }
    
    @Override
    public List<String> getParameterNames() {
      return Arrays.asList("Data","Task");
    }
    
    @Override
    public List<PParameter> getParameters() {
      return Arrays.asList(new PParameter("Data", "system.Data"),new PParameter("Task", "process.Task"));
    }
    
    @Override
    public Set<PBody> doGetContainedBodies() throws QueryInitializationException {
      Set<PBody> bodies = Sets.newLinkedHashSet();
      try {
      	{
      		PBody body = new PBody(this);
      		PVariable var_Data = body.getOrCreateVariableByName("Data");
      		PVariable var_Task = body.getOrCreateVariableByName("Task");
      		PVariable var_TaskId = body.getOrCreateVariableByName("TaskId");
      		PVariable var__virtual_0_ = body.getOrCreateVariableByName(".virtual{0}");
      		PVariable var__virtual_1_ = body.getOrCreateVariableByName(".virtual{1}");
      		body.setExportedParameters(Arrays.<ExportedParameter>asList(
      			new ExportedParameter(body, var_Data, "Data"),
      			
      			new ExportedParameter(body, var_Task, "Task")
      		));
      		new TypeConstraint(body, new FlatTuple(var_Data), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://system/1.0", "Data")));
      		new TypeConstraint(body, new FlatTuple(var_Task), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://process/1.0", "Task")));
      		new TypeConstraint(body, new FlatTuple(var_Data), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://system/1.0", "Data")));
      		new TypeConstraint(body, new FlatTuple(var_Data, var__virtual_0_), new EStructuralFeatureInstancesKey(getFeatureLiteral("http://system/1.0", "Data", "writingTaskIds")));
      		new Equality(body, var__virtual_0_, var_TaskId);
      		new TypeConstraint(body, new FlatTuple(var_Task), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://process/1.0", "Task")));
      		new TypeConstraint(body, new FlatTuple(var_Task, var__virtual_1_), new EStructuralFeatureInstancesKey(getFeatureLiteral("http://process/1.0", "ProcessElement", "id")));
      		new Equality(body, var__virtual_1_, var_TaskId);
      		bodies.add(body);
      	}
      	{
      		PAnnotation annotation = new PAnnotation("QueryBasedFeature");
      		annotation.addAttribute("feature", "writingTask");
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
