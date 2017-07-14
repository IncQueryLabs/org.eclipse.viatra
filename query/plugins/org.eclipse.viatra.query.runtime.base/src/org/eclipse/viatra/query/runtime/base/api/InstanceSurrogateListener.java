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

import org.eclipse.emf.ecore.EClass;

/**
 * Interface for observing insertion / deletion of instances of EClass.
 * 
 * <p> Surrogate values will be used instead of the original model contents for feature sources and reference targets. 
 * For attribute values, the internal canonical representation will be used. 
 * 
 * @author Tamas Szabo
 * @author Gabor Bergmann
 * @since 1.7
 */
public interface InstanceSurrogateListener<Surrogate> {
    /**
     * Called when the given instance was added to the model.
     * 
     * @param clazz
     *            an EClass registered for this listener, for which a new instance (possibly an instance of a subclass) was inserted into the model
     * @param instance
     *            an EObject instance that was inserted into the model
     */
    public void instanceInserted(EClass clazz, Surrogate instance);

    /**
     * Called when the given instance was removed from the model.
     * 
     * @param clazz
     *            an EClass registered for this listener, for which an instance (possibly an instance of a subclass) was removed from the model
     * @param instance
     *            an EObject instance that was removed from the model
     */
    public void instanceDeleted(EClass clazz, Surrogate instance);
}
