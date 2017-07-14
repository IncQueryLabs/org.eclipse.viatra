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

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Replaces EObjects with surrogates.
 * 
 * <p> The API explicitly indicates (via "pinning") when the client stores or ceases to store a reference to a surrogate value, so that 
 * the service may employ a reference counting mechanism. If a surrogate receives as many unpinnings as pinnings, 
 * then the client promises that it does not store the surrogate anymore, 
 * and it can be cleaned up from data structures of the service upon the next call to {@link ISurrogateObjectService#collectGarbage()}.
 * 
 * @author Gabor Bergmann
 * @since 1.7
 *
 *
 */
public interface ISurrogateObjectService<Surrogate> {
    
    /**
     * Returns a surrogate representation of the object. 
     * Creates and pins a new one if it does not exist yet.
     */
    public Surrogate getAndPinSurrogate(EObject object);
    /**
     * Returns the already existing surrogate representation of the object.
     * @throws IllegalStateException if no surrogate exists for the given object
     */
    public Surrogate getExistingSurrogate(EObject object);
    
    /**
     * Returns a surrogate representation of the object, if it already exists, or null otherwise.
     */
    public Surrogate peekSurrogate(EObject object);
    
    /**
     * Returns the EObject that corresponds to this surrogate, if it exists, or null otherwise.
     */
    public EObject fromSurrogate(Surrogate surrogate);
    
    /**
     * Returns the EObject that corresponds to this existing surrogate.
     * @throws IllegalStateException if not a valid surrogate
     */
    public EObject fromExistingSurrogate(Surrogate surrogate);
    
    /**
     * Returns the EClass of the EObject that corresponds to this existing surrogate.
     */
    public EClass eClassOfExistingSurrogate(Surrogate surrogate);

    /**
     * Returns the EObjects that corresponds to these existing surrogates.
     * @throws IllegalStateException if argument contains an invalid valid surrogate
     */
    public Set<EObject> fromExistingSurrogateSet(Set<Surrogate> surrogates);
    /**
     * Returns a map keyed by EObjects that corresponds to these existing surrogates.
     * @throws IllegalStateException if argument contains an invalid valid surrogate
     */
    public <T> Map<EObject, T> fromExistingSurrogateKeyedMap(Map<Surrogate, T> valMap);
    
    /**
     * Returns the already existing surrogate representation of the object.
     * The existing surrogate will be unpinned.
     * 
     * @throws IllegalStateException if no surrogate exists for the given object
     */
    public Surrogate getAndUnpinExistingSurrogate(EObject object);
    
    /**
     * Indicates that all notifications have been handled, and unused surrogates can be cleaned.
     */
    public void collectGarbage();
}
