/*******************************************************************************
 * Copyright (c) 2010-2017, Gabor Bergmann, IncQueryLabs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Gabor Bergmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.base.api;

import javax.inject.Provider;

/**
 * Factory for {@link ISurrogateObjectService}.
 * @author Gabor Bergmann
 * @since 1.7
 *
 */
public interface ISurrogateServiceFactory<Surrogate> {
    
    /**
     * @return a surrogate service
     */
    public ISurrogateObjectService<Surrogate> get();

}
