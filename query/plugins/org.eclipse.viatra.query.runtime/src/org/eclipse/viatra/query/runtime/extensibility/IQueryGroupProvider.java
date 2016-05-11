/*******************************************************************************
 * Copyright (c) 2010-2016, Abel Hegedus, IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.extensibility;

import java.util.Set;

import org.eclipse.viatra.query.runtime.api.IQueryGroup;
import org.eclipse.viatra.query.runtime.matchers.util.IProvider;

/**
 * Provider interface for {@link IQueryGroup} instances with added method for
 * requesting the set of FQNs for the query specifications in the group.
 * 
 * @author Abel Hegedus
 * @since 1.3
 *
 */
public interface IQueryGroupProvider extends IProvider<IQueryGroup> {

    /**
     * Note that the provider should load the query group class only if the FQNs can not be computed in other ways.
     * 
     * @return the set of query specification FQNs in the group
     */
    Set<String> getQuerySpecificationFQNs();

    /**
     * Note that the provider should load the query group class only if the FQNs can not be computed in other ways.
     * 
     * @return a set of providers for query specifications in the group
     */
    Set<IQuerySpecificationProvider> getQuerySpecificationProviders();
    
}
