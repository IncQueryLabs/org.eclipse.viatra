/*******************************************************************************
 * Copyright (c) 2010-2014, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *   Denes Harmath - support for multiple scope roots
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.emf;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.scope.IBaseIndex;
import org.eclipse.viatra.query.runtime.api.scope.IEngineContext;
import org.eclipse.viatra.query.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.viatra.query.runtime.base.api.IEMFBaseIndex;
import org.eclipse.viatra.query.runtime.base.api.ISurrogateServiceFactory;
import org.eclipse.viatra.query.runtime.base.api.ViatraBaseFactory;
import org.eclipse.viatra.query.runtime.base.exception.ViatraBaseException;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;
import org.eclipse.viatra.query.runtime.matchers.context.IQueryRuntimeContext;

/**
 * Implements an engine context on EMF models.
 * @author Bergmann Gabor
 *
 */
class EMFEngineContext<Surrogate> implements IEngineContext {

    private final EMFScope emfScope;
    private ISurrogateServiceFactory<Surrogate> surrogateServiceFactory;
    ViatraQueryEngine engine;
    Logger logger;
    IEMFBaseIndex<Surrogate> baseIndex;
    IBaseIndex baseIndexWrapper;
    IIndexingErrorListener taintListener;
    private EMFQueryRuntimeContext<Surrogate> runtimeContext;
    
    
    public static <Surrogate> EMFEngineContext<Surrogate> create(
            EMFScope emfScope, 
            ISurrogateServiceFactory<Surrogate> surrogateServiceFactory, 
            ViatraQueryEngine engine, 
            IIndexingErrorListener taintListener, 
            Logger logger) 
    {
        return new EMFEngineContext<Surrogate>(emfScope, surrogateServiceFactory, engine, taintListener, logger);
    }
    
    public EMFEngineContext(
            EMFScope emfScope, 
            ISurrogateServiceFactory<Surrogate> surrogateServiceFactory, 
            ViatraQueryEngine engine, 
            IIndexingErrorListener taintListener, 
            Logger logger) 
    {
        this.emfScope = emfScope;
        this.surrogateServiceFactory = surrogateServiceFactory;
        this.engine = engine;
        this.logger = logger;
        this.taintListener = taintListener;
    }
    
    public IEMFBaseIndex<Surrogate> getBaseIndexCore() throws ViatraQueryException {
        return getBaseIndexCore(true);
    }
    private IEMFBaseIndex<Surrogate> getBaseIndexCore(boolean ensureInitialized) throws ViatraQueryException {
        if (baseIndex == null) {
            try {
                // sync to avoid crazy compiler reordering which would matter if derived features use VIATRA and call this
                // reentrantly
                synchronized (this) {
                    baseIndex = ViatraBaseFactory.getInstance().createBaseIndex(
                            surrogateServiceFactory, 
                            this.emfScope.getOptions(),
                            logger);
                    getBaseIndex().addIndexingErrorListener(taintListener);
                }
            } catch (ViatraBaseException e) {
                throw new ViatraQueryException("Could not create VIATRA Base Index", "Could not create base index",
                        e);
            }

            if (ensureInitialized) {
                ensureIndexLoaded();
            }

        }
        return baseIndex;
    }

    private void ensureIndexLoaded() throws ViatraQueryException {
        try {
            for (Notifier scopeRoot : this.emfScope.getScopeRoots()) {
                baseIndex.addRoot(scopeRoot);
            }
        } catch (ViatraBaseException e) {
            throw new ViatraQueryException("Could not initialize VIATRA Base Index",
                    "Could not initialize base index", e);
        }
    }

    @Override
    public IQueryRuntimeContext getQueryRuntimeContext() throws ViatraQueryException {
        IEMFBaseIndex<Surrogate> nh = getBaseIndexCore(false);
        if (runtimeContext == null) {
            runtimeContext = 
                    emfScope.getOptions().isDynamicEMFMode() ?
                     new DynamicEMFQueryRuntimeContext<Surrogate>(nh, logger, emfScope) :
                     new EMFQueryRuntimeContext<Surrogate>(nh, logger, emfScope);
                     
             ensureIndexLoaded();
        }
        
        return runtimeContext;
    }
    
    @Override
    public void dispose() {
        if (runtimeContext != null) runtimeContext.dispose();
        if (baseIndex != null) baseIndex.dispose();
        
        this.baseIndexWrapper = null;
        this.engine = null;
        this.logger = null;
        this.baseIndex = null;
    }
    
    
    @Override
    public IBaseIndex getBaseIndex() throws ViatraQueryException {
        if (baseIndexWrapper == null) {
            final IEMFBaseIndex<Surrogate> navigationHelper = getBaseIndexCore();
            baseIndexWrapper = new EMFBaseIndexWrapper<Surrogate>(navigationHelper);
        }
        return baseIndexWrapper;
    }
}