package system.queries;

import java.util.Arrays;
import java.util.List;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.impl.BasePatternMatch;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import process.Task;
import system.Data;
import system.queries.util.DataTaskReadCorrespondenceQuerySpecification;

/**
 * Pattern-specific match representation of the system.queries.DataTaskReadCorrespondence pattern,
 * to be used in conjunction with {@link DataTaskReadCorrespondenceMatcher}.
 * 
 * <p>Class fields correspond to parameters of the pattern. Fields with value null are considered unassigned.
 * Each instance is a (possibly partial) substitution of pattern parameters,
 * usable to represent a match of the pattern in the result of a query,
 * or to specify the bound (fixed) input parameters when issuing a query.
 * 
 * @see DataTaskReadCorrespondenceMatcher
 * @see DataTaskReadCorrespondenceProcessor
 * 
 */
@SuppressWarnings("all")
public abstract class DataTaskReadCorrespondenceMatch extends BasePatternMatch {
  private Data fData;
  
  private Task fTask;
  
  private static List<String> parameterNames = makeImmutableList("Data", "Task");
  
  private DataTaskReadCorrespondenceMatch(final Data pData, final Task pTask) {
    this.fData = pData;
    this.fTask = pTask;
  }
  
  @Override
  public Object get(final String parameterName) {
    if ("Data".equals(parameterName)) return this.fData;
    if ("Task".equals(parameterName)) return this.fTask;
    return null;
  }
  
  public Data getData() {
    return this.fData;
  }
  
  public Task getTask() {
    return this.fTask;
  }
  
  @Override
  public boolean set(final String parameterName, final Object newValue) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    if ("Data".equals(parameterName) ) {
    	this.fData = (system.Data) newValue;
    	return true;
    }
    if ("Task".equals(parameterName) ) {
    	this.fTask = (process.Task) newValue;
    	return true;
    }
    return false;
  }
  
  public void setData(final Data pData) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    this.fData = pData;
  }
  
  public void setTask(final Task pTask) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    this.fTask = pTask;
  }
  
  @Override
  public String patternName() {
    return "system.queries.DataTaskReadCorrespondence";
  }
  
  @Override
  public List<String> parameterNames() {
    return DataTaskReadCorrespondenceMatch.parameterNames;
  }
  
  @Override
  public Object[] toArray() {
    return new Object[]{fData, fTask};
  }
  
  @Override
  public DataTaskReadCorrespondenceMatch toImmutable() {
    return isMutable() ? newMatch(fData, fTask) : this;
  }
  
  @Override
  public String prettyPrint() {
    StringBuilder result = new StringBuilder();
    result.append("\"Data\"=" + prettyPrintValue(fData) + ", ");
    
    result.append("\"Task\"=" + prettyPrintValue(fTask)
    );
    return result.toString();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fData == null) ? 0 : fData.hashCode());
    result = prime * result + ((fTask == null) ? 0 : fTask.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
    	return true;
    if (!(obj instanceof DataTaskReadCorrespondenceMatch)) { // this should be infrequent
    	if (obj == null) {
    		return false;
    	}
    	if (!(obj instanceof IPatternMatch)) {
    		return false;
    	}
    	IPatternMatch otherSig  = (IPatternMatch) obj;
    	if (!specification().equals(otherSig.specification()))
    		return false;
    	return Arrays.deepEquals(toArray(), otherSig.toArray());
    }
    DataTaskReadCorrespondenceMatch other = (DataTaskReadCorrespondenceMatch) obj;
    if (fData == null) {if (other.fData != null) return false;}
    else if (!fData.equals(other.fData)) return false;
    if (fTask == null) {if (other.fTask != null) return false;}
    else if (!fTask.equals(other.fTask)) return false;
    return true;
  }
  
  @Override
  public DataTaskReadCorrespondenceQuerySpecification specification() {
    try {
    	return DataTaskReadCorrespondenceQuerySpecification.instance();
    } catch (IncQueryException ex) {
     	// This cannot happen, as the match object can only be instantiated if the query specification exists
     	throw new IllegalStateException (ex);
    }
  }
  
  /**
   * Returns an empty, mutable match.
   * Fields of the mutable match can be filled to create a partial match, usable as matcher input.
   * 
   * @return the empty match.
   * 
   */
  public static DataTaskReadCorrespondenceMatch newEmptyMatch() {
    return new Mutable(null, null);
  }
  
  /**
   * Returns a mutable (partial) match.
   * Fields of the mutable match can be filled to create a partial match, usable as matcher input.
   * 
   * @param pData the fixed value of pattern parameter Data, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the new, mutable (partial) match object.
   * 
   */
  public static DataTaskReadCorrespondenceMatch newMutableMatch(final Data pData, final Task pTask) {
    return new Mutable(pData, pTask);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pData the fixed value of pattern parameter Data, or null if not bound.
   * @param pTask the fixed value of pattern parameter Task, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public static DataTaskReadCorrespondenceMatch newMatch(final Data pData, final Task pTask) {
    return new Immutable(pData, pTask);
  }
  
  private static final class Mutable extends DataTaskReadCorrespondenceMatch {
    Mutable(final Data pData, final Task pTask) {
      super(pData, pTask);
    }
    
    @Override
    public boolean isMutable() {
      return true;
    }
  }
  
  private static final class Immutable extends DataTaskReadCorrespondenceMatch {
    Immutable(final Data pData, final Task pTask) {
      super(pData, pTask);
    }
    
    @Override
    public boolean isMutable() {
      return false;
    }
  }
}
