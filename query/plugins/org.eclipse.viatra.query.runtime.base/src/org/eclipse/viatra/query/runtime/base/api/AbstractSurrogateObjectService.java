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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Base implementation for surrogate services.
 * @author Gabor Bergmann
 * @since 1.7
 *
 */
public abstract class AbstractSurrogateObjectService<Surrogate> implements ISurrogateObjectService<Surrogate> {

    @Override
    public Set<EObject> fromExistingSurrogateSet(Set<Surrogate> surrogates) {
        Set<EObject> result = new HashSet<>();
        for (Surrogate surrogate : surrogates) {
            result.add(fromExistingSurrogate(surrogate));
        }
        return result;
    }
    
    @Override
    public <T> Map<EObject, T> fromExistingSurrogateKeyedMap(Map<Surrogate, T> valMap) {
        Map<EObject, T> result = new HashMap<>();
        for (Entry<Surrogate, T> entry : valMap.entrySet()) {
            result.put(fromExistingSurrogate(entry.getKey()), entry.getValue());
        }
        return result;
    }
    
    @Override
    public EClass eClassOfExistingSurrogate(Surrogate surrogate) {
        return fromExistingSurrogate(surrogate).eClass();
    }
}
