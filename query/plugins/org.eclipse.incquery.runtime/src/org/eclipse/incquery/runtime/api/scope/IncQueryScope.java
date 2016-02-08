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
package org.eclipse.incquery.runtime.api.scope;

import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.internal.apiimpl.EngineContextFactory;

/**
 * Defines a scope for the IncQuery engine, which determines the set of model elements that query evaluation operates on.
 * 
 * @author Bergmann Gabor
 *
 */
public abstract class IncQueryScope extends EngineContextFactory {
	
	/**
	 * Determines whether a query engine initialized on this scope can evaluate queries formulated against the given scope type.
	 * <p> Every query scope class is compatible with a query engine initialized on a scope of the same class or a subclass.
	 * @param queryScopeClass the scope class returned by invoking {@link IQuerySpecification#getPreferredScopeClass()} on a query specification
	 * @return true if an {@link IncQueryEngine} initialized on this scope can consume an {@link IQuerySpecification}
	 */
	public boolean isCompatibleWithQueryScope(Class<? extends IncQueryScope> queryScopeClass) {
		return queryScopeClass.isAssignableFrom(this.getClass());
	}

}
