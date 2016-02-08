/*******************************************************************************
 * Copyright (c) 2010-2015, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *   Zoltan Ujhelyi - provided lazy loading support
 *******************************************************************************/
package org.eclipse.incquery.runtime.matchers.context.surrogate;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.incquery.runtime.matchers.context.IInputKey;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;
import org.eclipse.incquery.runtime.matchers.util.IProvider;
import org.eclipse.incquery.runtime.matchers.util.IProvider.ProvidedValueFunction;
import org.eclipse.incquery.runtime.matchers.util.SingletonInstanceProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Abel Hegedus
 *
 */
public class SurrogateQueryRegistry {
    
	private Map<IInputKey, IProvider<PQuery>> registeredSurrogateQueryMap = Maps.newHashMap();
    private Map<IInputKey, IProvider<PQuery>> dynamicSurrogateQueryMap = Maps.newHashMap();
    
    /**
     * Hidden constructor
     */
    private SurrogateQueryRegistry() {
    }

    private static final SurrogateQueryRegistry INSTANCE = new SurrogateQueryRegistry();
    
    public static SurrogateQueryRegistry instance() {
        return INSTANCE;
    }
    
    /**
     * 
     * @param feature
     * @param surrogateQuery
     * @return the previous surrogate query associated with feature, or null if there was no such query FQN registered
     * @throws IllegalArgumentException if feature or surrogateQuery is null 
     */
    public IProvider<PQuery> registerSurrogateQueryForFeature(IInputKey feature, PQuery surrogateQuery) {
    	Preconditions.checkArgument(surrogateQuery != null, "Surrogate query must not be null!");
    	return registerSurrogateQueryForFeature(feature, new SingletonInstanceProvider<PQuery>(surrogateQuery));
	}

	/**
	 * 
	 * @param feature
	 * @param surrogateQuery
	 * @return the previous surrogate query associated with feature, or null
	 *         if there was no such query registered
	 * @throws IllegalArgumentException
	 *             if feature or surrogateQuery is null
	 */
	public IProvider<PQuery> registerSurrogateQueryForFeature(IInputKey feature, IProvider<PQuery> surrogateQueryProvider) {
	    Preconditions.checkArgument(feature != null, "Feature must not be null!");
	    Preconditions.checkArgument(surrogateQueryProvider != null, "Surrogate query must not be null!");
		return registeredSurrogateQueryMap.put(feature, surrogateQueryProvider);
    }
	
	public IProvider<PQuery> addDynamicSurrogateQueryForFeature(IInputKey feature, PQuery surrogateQuery) {
		Preconditions.checkArgument(surrogateQuery != null, "Surrogate query FQN must not be null!");
		return addDynamicSurrogateQueryForFeature(feature, new SingletonInstanceProvider<PQuery>(surrogateQuery));
	}
    
    public IProvider<PQuery> addDynamicSurrogateQueryForFeature(IInputKey feature, IProvider<PQuery> surrogateQuery) {
        Preconditions.checkArgument(feature != null, "Feature must not be null!");
        Preconditions.checkArgument(surrogateQuery != null, "Surrogate query FQN must not be null!");
        return dynamicSurrogateQueryMap.put(feature, surrogateQuery);
    }

    public IProvider<PQuery> removeDynamicSurrogateQueryForFeature(IInputKey feature) {
        Preconditions.checkArgument(feature != null, "Feature must not be null!");
        return dynamicSurrogateQueryMap.remove(feature);
    }
    
    /**
     * 
     * @param feature that may have surrogate query defined, null not allowed
     * @return true if the feature has a surrogate query defined
     * @throws IllegalArgumentException if feature is null
     */
    public boolean hasSurrogateQueryFQN(IInputKey feature) {
        Preconditions.checkArgument(feature != null, "Feature must not be null!");
        boolean surrogateExists = dynamicSurrogateQueryMap.containsKey(feature);
        if(!surrogateExists){
            surrogateExists = registeredSurrogateQueryMap.containsKey(feature);
        }
        return surrogateExists;
    }
    
    /**
     * 
     * @param feature for which the surrogate query FQN should be returned
     * @return the surrogate query FQN defined for the feature
     * @throws IllegalArgumentException if feature is null
     * @throws NoSuchElementException if the feature has no surrogate query defined, use {@link #hasSurrogateQueryFQN} to check
     */
    public PQuery getSurrogateQuery(IInputKey feature) {
        Preconditions.checkArgument(feature != null, "Feature must not be null!");
        IProvider<PQuery> surrogate = dynamicSurrogateQueryMap.get(feature);
        if(surrogate == null) {
            surrogate = registeredSurrogateQueryMap.get(feature);
        }
        if(surrogate != null) {
            return surrogate.get();
        } else {
            throw new NoSuchElementException(String.format("Feature %s has no surrogate query defined! Use #hasSurrogateQueryFQN to check existence.", feature));
        }
    }

    /**
     * @return the map of features with registered surrogate query FQNs
     * @deprecated use {@link #getRegisteredSurrogateQueries()} instead
     */
    public Map<IInputKey, PQuery> getRegisteredSurrogateQueryFQNMap() {
        return Maps.transformValues(registeredSurrogateQueryMap, new ProvidedValueFunction());
    }
    
    /**
     * @return the map of features with dynamically added surrogate query FQNs
     * @deprecated use {@link #getDynamicSurrogateQueries()} instead
     */
    public Map<IInputKey, PQuery> getDynamicSurrogateQueryFQNMap() {
        return Maps.transformValues(dynamicSurrogateQueryMap, new ProvidedValueFunction());
    }
    
    /**
     * Returns a copy of the map that contains all features with surrogate queries.
     * Dynamically added surrogates override registered surrogates and each feature will only appear once in the map. 
     * 
     * @return a new copy of the map that contains all features with surrogate queries.
     * @deprecated use {@link #getAllSurrogateQueries()} instead
     */
    @Deprecated
    public Map<IInputKey, PQuery> getAllSurrogateQueryFQNMap() {
    	Map<IInputKey, IProvider<PQuery>> allSurrogateQueries = 
        		Maps.newHashMap(registeredSurrogateQueryMap);
        allSurrogateQueries.putAll(dynamicSurrogateQueryMap);
        return Maps.transformValues(allSurrogateQueries, new ProvidedValueFunction());
    }
    
    /**
     * @return an unmodifiable set of features with registered surrogate queries
     */
    public Set<IInputKey> getRegisteredSurrogateQueries() {
        return ImmutableSet.<IInputKey>builder().addAll(getRegisteredSurrogateQueriesInternal()).build();
    }
    
    private Set<IInputKey> getRegisteredSurrogateQueriesInternal() {
    	return registeredSurrogateQueryMap.keySet();
    }
    
    /**
     * @return an unmodifiable set of features with dynamically added surrogate queries
     */
    public Set<IInputKey> getDynamicSurrogateQueries() {
    	return ImmutableSet.<IInputKey>builder().addAll(getDynamicSurrogateQueriesInternal()).build();
    }
    
    private Set<IInputKey> getDynamicSurrogateQueriesInternal() {
    	return dynamicSurrogateQueryMap.keySet();    	
    }
    
    /**
     * @return an unmodifiable set that contains all features with surrogate queries.
     */
    public Set<IInputKey> getAllSurrogateQueries() {
        return Sets.union(getRegisteredSurrogateQueriesInternal(), getDynamicSurrogateQueriesInternal());
    }
}
