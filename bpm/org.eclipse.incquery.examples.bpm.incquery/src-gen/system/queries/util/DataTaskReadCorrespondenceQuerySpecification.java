package system.queries.util;

import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedQuerySpecification;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.extensibility.IQuerySpecificationProvider;
import system.queries.DataTaskReadCorrespondenceMatcher;

/**
 * A pattern-specific query specification that can instantiate DataTaskReadCorrespondenceMatcher in a type-safe way.
 * 
 * @see DataTaskReadCorrespondenceMatcher
 * @see DataTaskReadCorrespondenceMatch
 * 
 */
@SuppressWarnings("all")
public final class DataTaskReadCorrespondenceQuerySpecification extends BaseGeneratedQuerySpecification<DataTaskReadCorrespondenceMatcher> {
  /**
   * @return the singleton instance of the query specification
   * @throws IncQueryException if the pattern definition could not be loaded
   * 
   */
  public static DataTaskReadCorrespondenceQuerySpecification instance() throws IncQueryException {
    try {
    	return LazyHolder.INSTANCE;
    } catch (ExceptionInInitializerError err) {
    	processInitializerError(err);
    	throw err;
    }
    
  }
  
  @Override
  protected DataTaskReadCorrespondenceMatcher instantiate(final IncQueryEngine engine) throws IncQueryException {
    return DataTaskReadCorrespondenceMatcher.on(engine);
    
  }
  
  @Override
  protected String getBundleName() {
    return "org.eclipse.incquery.examples.bpm.incquery";
    
  }
  
  @Override
  protected String patternName() {
    return "system.queries.DataTaskReadCorrespondence";
    
  }
  
  private DataTaskReadCorrespondenceQuerySpecification() throws IncQueryException {
    super();
  }
  
  @SuppressWarnings("all")
  public static class Provider implements IQuerySpecificationProvider<DataTaskReadCorrespondenceQuerySpecification> {
    @Override
    public DataTaskReadCorrespondenceQuerySpecification get() throws IncQueryException {
      return instance();
    }
  }
  
  
  @SuppressWarnings("all")
  private static class LazyHolder {
    private final static DataTaskReadCorrespondenceQuerySpecification INSTANCE = make();
    
    public static DataTaskReadCorrespondenceQuerySpecification make() {
      try {
      	return new DataTaskReadCorrespondenceQuerySpecification();
      } catch (IncQueryException ex) {
      	throw new RuntimeException	(ex);
      }
      
    }
  }
  
}
