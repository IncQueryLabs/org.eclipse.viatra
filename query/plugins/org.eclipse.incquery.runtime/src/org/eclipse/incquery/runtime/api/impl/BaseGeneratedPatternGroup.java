/*******************************************************************************
 * Copyright (c) 2010-2012, Mark Czotter, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mark Czotter - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.api.impl;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.incquery.runtime.api.IQuerySpecification;

/**
 * @author Mark Czotter
 * 
 */
public abstract class BaseGeneratedPatternGroup extends BasePatternGroup {

    @Override
    public Set<IQuerySpecification<?>> getSpecifications() {
        return querySpecifications;
    }

    /**
     * Returns {@link IQuerySpecification} objects for handling them as a group. To be filled by constructors of subclasses.
     */
    protected Set<IQuerySpecification<?>> querySpecifications = new HashSet<IQuerySpecification<?>>();
}
