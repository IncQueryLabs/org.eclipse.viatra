/*******************************************************************************
 * Copyright (c) 2010-2015, Marton Bur, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marton Bur, Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.localsearch.matcher.integration;

import java.util.Set;

import com.google.common.collect.*;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.viatra.query.runtime.localsearch.plan.CachingPlanProvider;
import org.eclipse.viatra.query.runtime.localsearch.plan.IPlanProvider;
import org.eclipse.viatra.query.runtime.matchers.backend.IQueryBackend;
import org.eclipse.viatra.query.runtime.matchers.backend.IQueryBackendHintProvider;
import org.eclipse.viatra.query.runtime.matchers.backend.IQueryResultProvider;
import org.eclipse.viatra.query.runtime.matchers.context.IQueryCacheContext;
import org.eclipse.viatra.query.runtime.matchers.context.IQueryRuntimeContext;
import org.eclipse.viatra.query.runtime.matchers.planning.QueryProcessingException;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PQuery;

import com.google.common.collect.Table;

/**
 * @author Marton Bur, Zoltan Ujhelyi
 *
 */
public class LocalSearchBackend implements IQueryBackend {

    IQueryBackendHintProvider hintProvider;
	IQueryRuntimeContext runtimeContext;
	IQueryCacheContext queryCacheContext;
	Logger logger;
	IPlanProvider planProvider;
	
	// Cache
	Table<EDataType, EClass, Set<EAttribute>> eAttributesByTypeForEClass;
    

    public LocalSearchBackend(Logger logger, IQueryRuntimeContext runtimeContext, IQueryCacheContext queryCacheContext, IQueryBackendHintProvider hintProvider) {
        super();
		this.logger = logger;
		this.runtimeContext = runtimeContext;
		this.queryCacheContext = queryCacheContext;
        this.hintProvider = hintProvider;
        this.eAttributesByTypeForEClass = HashBasedTable.create();
        this.planProvider = new CachingPlanProvider(logger);
    }


    @Override
    public IQueryResultProvider getResultProvider(PQuery query) throws QueryProcessingException {
        return new LocalSearchResultProvider(this, logger, runtimeContext, queryCacheContext, hintProvider, query, planProvider);
    }

    @Override
    public void dispose() {        
    }

	@Override
	public boolean isCaching() {
		return false;
	}

	@Override
	public IQueryResultProvider peekExistingResultProvider(PQuery query) {
		return null;
	}

	public Table<EDataType, EClass, Set<EAttribute>> geteAttributesByTypeForEClass() {
	    return eAttributesByTypeForEClass;
	}

	/**
	 * @since 1.4
     */
    public IQueryRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
    
    /**
     * @since 1.4
     */
    public IQueryBackendHintProvider getHintProvider() {
        return hintProvider;
    }
	
}
