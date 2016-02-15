package system.queries;

import java.util.Arrays;
import java.util.List;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.impl.BasePatternMatch;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import process.Task;
import system.queries.util.TransitiveAffectedTasksThroughDataQuerySpecification;

/**
 * Pattern-specific match representation of the system.queries.TransitiveAffectedTasksThroughData pattern,
 * to be used in conjunction with {@link TransitiveAffectedTasksThroughDataMatcher}.
 * 
 * <p>Class fields correspond to parameters of the pattern. Fields with value null are considered unassigned.
 * Each instance is a (possibly partial) substitution of pattern parameters,
 * usable to represent a match of the pattern in the result of a query,
 * or to specify the bound (fixed) input parameters when issuing a query.
 * 
 * @see TransitiveAffectedTasksThroughDataMatcher
 * @see TransitiveAffectedTasksThroughDataProcessor
 * 
 */
@SuppressWarnings("all")
public abstract class TransitiveAffectedTasksThroughDataMatch extends BasePatternMatch {
  private Task fSourceTask;
  
  private Task fAffectedTask;
  
  private static List<String> parameterNames = makeImmutableList("SourceTask", "AffectedTask");
  
  private TransitiveAffectedTasksThroughDataMatch(final Task pSourceTask, final Task pAffectedTask) {
    this.fSourceTask = pSourceTask;
    this.fAffectedTask = pAffectedTask;
  }
  
  @Override
  public Object get(final String parameterName) {
    if ("SourceTask".equals(parameterName)) return this.fSourceTask;
    if ("AffectedTask".equals(parameterName)) return this.fAffectedTask;
    return null;
  }
  
  public Task getSourceTask() {
    return this.fSourceTask;
  }
  
  public Task getAffectedTask() {
    return this.fAffectedTask;
  }
  
  @Override
  public boolean set(final String parameterName, final Object newValue) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    if ("SourceTask".equals(parameterName) ) {
    	this.fSourceTask = (process.Task) newValue;
    	return true;
    }
    if ("AffectedTask".equals(parameterName) ) {
    	this.fAffectedTask = (process.Task) newValue;
    	return true;
    }
    return false;
  }
  
  public void setSourceTask(final Task pSourceTask) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    this.fSourceTask = pSourceTask;
  }
  
  public void setAffectedTask(final Task pAffectedTask) {
    if (!isMutable()) throw new java.lang.UnsupportedOperationException();
    this.fAffectedTask = pAffectedTask;
  }
  
  @Override
  public String patternName() {
    return "system.queries.TransitiveAffectedTasksThroughData";
  }
  
  @Override
  public List<String> parameterNames() {
    return TransitiveAffectedTasksThroughDataMatch.parameterNames;
  }
  
  @Override
  public Object[] toArray() {
    return new Object[]{fSourceTask, fAffectedTask};
  }
  
  @Override
  public TransitiveAffectedTasksThroughDataMatch toImmutable() {
    return isMutable() ? newMatch(fSourceTask, fAffectedTask) : this;
  }
  
  @Override
  public String prettyPrint() {
    StringBuilder result = new StringBuilder();
    result.append("\"SourceTask\"=" + prettyPrintValue(fSourceTask) + ", ");
    
    result.append("\"AffectedTask\"=" + prettyPrintValue(fAffectedTask)
    );
    return result.toString();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fSourceTask == null) ? 0 : fSourceTask.hashCode());
    result = prime * result + ((fAffectedTask == null) ? 0 : fAffectedTask.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
    	return true;
    if (!(obj instanceof TransitiveAffectedTasksThroughDataMatch)) { // this should be infrequent
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
    TransitiveAffectedTasksThroughDataMatch other = (TransitiveAffectedTasksThroughDataMatch) obj;
    if (fSourceTask == null) {if (other.fSourceTask != null) return false;}
    else if (!fSourceTask.equals(other.fSourceTask)) return false;
    if (fAffectedTask == null) {if (other.fAffectedTask != null) return false;}
    else if (!fAffectedTask.equals(other.fAffectedTask)) return false;
    return true;
  }
  
  @Override
  public TransitiveAffectedTasksThroughDataQuerySpecification specification() {
    try {
    	return TransitiveAffectedTasksThroughDataQuerySpecification.instance();
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
  public static TransitiveAffectedTasksThroughDataMatch newEmptyMatch() {
    return new Mutable(null, null);
  }
  
  /**
   * Returns a mutable (partial) match.
   * Fields of the mutable match can be filled to create a partial match, usable as matcher input.
   * 
   * @param pSourceTask the fixed value of pattern parameter SourceTask, or null if not bound.
   * @param pAffectedTask the fixed value of pattern parameter AffectedTask, or null if not bound.
   * @return the new, mutable (partial) match object.
   * 
   */
  public static TransitiveAffectedTasksThroughDataMatch newMutableMatch(final Task pSourceTask, final Task pAffectedTask) {
    return new Mutable(pSourceTask, pAffectedTask);
  }
  
  /**
   * Returns a new (partial) match.
   * This can be used e.g. to call the matcher with a partial match.
   * <p>The returned match will be immutable. Use {@link #newEmptyMatch()} to obtain a mutable match object.
   * @param pSourceTask the fixed value of pattern parameter SourceTask, or null if not bound.
   * @param pAffectedTask the fixed value of pattern parameter AffectedTask, or null if not bound.
   * @return the (partial) match object.
   * 
   */
  public static TransitiveAffectedTasksThroughDataMatch newMatch(final Task pSourceTask, final Task pAffectedTask) {
    return new Immutable(pSourceTask, pAffectedTask);
  }
  
  private static final class Mutable extends TransitiveAffectedTasksThroughDataMatch {
    Mutable(final Task pSourceTask, final Task pAffectedTask) {
      super(pSourceTask, pAffectedTask);
    }
    
    @Override
    public boolean isMutable() {
      return true;
    }
  }
  
  private static final class Immutable extends TransitiveAffectedTasksThroughDataMatch {
    Immutable(final Task pSourceTask, final Task pAffectedTask) {
      super(pSourceTask, pAffectedTask);
    }
    
    @Override
    public boolean isMutable() {
      return false;
    }
  }
}
