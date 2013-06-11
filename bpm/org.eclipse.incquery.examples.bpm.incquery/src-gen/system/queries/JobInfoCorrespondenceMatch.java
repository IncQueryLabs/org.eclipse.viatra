package system.queries;

import java.util.Arrays;
import java.util.List;
import operation.RuntimeInformation;
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.impl.BasePatternMatch;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import system.Job;

/**
 * Pattern-specific match representation of the system.queries.JobInfoCorrespondence pattern, 
 * to be used in conjunction with {@link JobInfoCorrespondenceMatcher}.
 * 
 * <p>Class fields correspond to parameters of the pattern. Fields with value null are considered unassigned.
 * Each instance is a (possibly partial) substitution of pattern parameters, 
 * usable to represent a match of the pattern in the result of a query, 
 * or to specify the bound (fixed) input parameters when issuing a query.
 * 
 * @see JobInfoCorrespondenceMatcher
 * @see JobInfoCorrespondenceProcessor
 * 
 */
public abstract class JobInfoCorrespondenceMatch extends BasePatternMatch {
  private Job fJob;
  
  private RuntimeInformation fInfo;
  
  private static List<String> parameterNames = makeImmutableList("Job", "Info");
  
  private JobInfoCorrespondenceMatch(final Job pJob, final RuntimeInformation pInfo) {
    this.fJob = pJob;
    this.fInfo = pInfo;
    
  }
  
  @Override
  public Object get(final String parameterName) {
    if ("Job".equals(parameterName)) return this.fJob;
    if ("Info".equals(parameterName)) return this.fInfo;
    return null;
    
  }
  
  public Job getJob() {
    return this.fJob;
    
  }
  
  public RuntimeInformation getInfo() {
    return this.fInfo;
    
  }
  
  @Override
  public boolean set(final String parameterName, final Object newValue) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    if ("Job".equals(parameterName) ) {
    	this.fJob = (system.Job) newValue;
    	return true;
    }
    if ("Info".equals(parameterName) ) {
    	this.fInfo = (operation.RuntimeInformation) newValue;
    	return true;
    }
    return false;
    
  }
  
  public void setJob(final Job pJob) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    this.fJob = pJob;
    
  }
  
  public void setInfo(final RuntimeInformation pInfo) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    this.fInfo = pInfo;
    
  }
  
  @Override
  public String patternName() {
    return "system.queries.JobInfoCorrespondence";
    
  }
  
  @Override
  public List<String> parameterNames() {
    return JobInfoCorrespondenceMatch.parameterNames;
    
  }
  
  @Override
  public Object[] toArray() {
    return new Object[]{fJob, fInfo};
    
  }
  
  @Override
  public String prettyPrint() {
    StringBuilder result = new StringBuilder();
    result.append("\"Job\"=" + prettyPrintValue(fJob) + ", ");
    result.append("\"Info\"=" + prettyPrintValue(fInfo));
    return result.toString();
    
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fJob == null) ? 0 : fJob.hashCode()); 
    result = prime * result + ((fInfo == null) ? 0 : fInfo.hashCode()); 
    return result; 
    
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
    	return true;
    if (!(obj instanceof JobInfoCorrespondenceMatch)) { // this should be infrequent				
    	if (obj == null)
    		return false;
    	if (!(obj instanceof IPatternMatch))
    		return false;
    	IPatternMatch otherSig  = (IPatternMatch) obj;
    	if (!pattern().equals(otherSig.pattern()))
    		return false;
    	return Arrays.deepEquals(toArray(), otherSig.toArray());
    }
    JobInfoCorrespondenceMatch other = (JobInfoCorrespondenceMatch) obj;
    if (fJob == null) {if (other.fJob != null) return false;}
    else if (!fJob.equals(other.fJob)) return false;
    if (fInfo == null) {if (other.fInfo != null) return false;}
    else if (!fInfo.equals(other.fInfo)) return false;
    return true;
  }
  
  @Override
  public Pattern pattern() {
    try {
    	return JobInfoCorrespondenceMatcher.querySpecification().getPattern();
    } catch (IncQueryException ex) {
     	// This cannot happen, as the match object can only be instantiated if the query specification exists
     	throw new IllegalStateException	(ex);
    }
    
  }
  static final class Mutable extends JobInfoCorrespondenceMatch {
    Mutable(final Job pJob, final RuntimeInformation pInfo) {
      super(pJob, pInfo);
      
    }
    
    @Override
    public boolean isMutable() {
      return true;
    }
  }
  
  static final class Immutable extends JobInfoCorrespondenceMatch {
    Immutable(final Job pJob, final RuntimeInformation pInfo) {
      super(pJob, pInfo);
      
    }
    
    @Override
    public boolean isMutable() {
      return false;
    }
  }
  
}
