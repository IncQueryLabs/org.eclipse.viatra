/*******************************************************************************
 * Copyright (c) 2010-2014, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.rete.matcher;

import org.apache.log4j.Logger;
import org.eclipse.incquery.runtime.matchers.backend.IQueryBackend;
import org.eclipse.incquery.runtime.matchers.backend.IQueryBackendFactory;
import org.eclipse.incquery.runtime.matchers.backend.IQueryBackendHintProvider;
import org.eclipse.incquery.runtime.matchers.context.IQueryCacheContext;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContext;
import org.eclipse.incquery.runtime.rete.construction.plancompiler.ReteRecipeCompiler;
import org.eclipse.incquery.runtime.rete.util.Options;

public class ReteBackendFactory implements IQueryBackendFactory {
    /**
     * EXPERIMENTAL
     */
    private final static int reteThreads = 0;
    
    @Override
    public IQueryBackend create(Logger logger,
    		IQueryRuntimeContext runtimeContext,
    		IQueryCacheContext queryCacheContext,
    		IQueryBackendHintProvider hintProvider) 
    {
	    ReteEngine engine;
	    engine = new ReteEngine(logger, runtimeContext, reteThreads);
	    ReteRecipeCompiler compiler = 
	    		new ReteRecipeCompiler(
	    				Options.builderMethod.layoutStrategy(), 
	    				logger,
	    				runtimeContext.getMetaContext(),
	    				queryCacheContext,
	    				hintProvider);
	    //EPMBuilder builder = new EPMBuilder(buildable, context);
	    engine.setCompiler(compiler);
	    return engine;
	}
    
    @Override
    public Class<? extends IQueryBackend> getBackendClass() {
    	return ReteEngine.class;
    }

	@Override
	public int hashCode() {
		return ReteBackendFactory.class.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ReteBackendFactory)) {
			return false;
		}
		return true;
	}
    
    
}