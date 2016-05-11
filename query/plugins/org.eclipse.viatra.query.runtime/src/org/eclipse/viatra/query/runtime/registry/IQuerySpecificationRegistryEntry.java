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
package org.eclipse.viatra.query.runtime.registry;

import org.eclipse.viatra.query.runtime.extensibility.IQuerySpecificationProvider;

/**
 * The query specification registry entry interface can return the identifier of the source that added it to the
 * registry. It is provider based and can delay class loading of the wrapped {@link IQuerySpecification} until needed.
 * 
 * @author Abel Hegedus
 * @since 1.3
 *
 */
public interface IQuerySpecificationRegistryEntry extends IQuerySpecificationProvider {

    /**
     * @return the identifier of the registry source that contributed the specification
     */
    String getSourceIdentifier();

}
