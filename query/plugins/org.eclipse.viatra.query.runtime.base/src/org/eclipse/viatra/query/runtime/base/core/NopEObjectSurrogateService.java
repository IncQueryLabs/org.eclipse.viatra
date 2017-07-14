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
package org.eclipse.viatra.query.runtime.base.core;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.viatra.query.runtime.base.api.AbstractSurrogateObjectService;
import org.eclipse.viatra.query.runtime.base.api.ISurrogateObjectService;
import org.eclipse.viatra.query.runtime.base.api.ISurrogateServiceFactory;

/**
 * Provides a nop-implementation of surrogates; the EObjects themselves will be stored in the base index.
 * @author Gabor Bergmann
 *
 */
public class NopEObjectSurrogateService extends AbstractSurrogateObjectService<EObject> {

    public static final Factory FACTORY = new Factory();
    
    public static class Factory implements ISurrogateServiceFactory<EObject> {
        @Override
        public ISurrogateObjectService<EObject> get() {
            return new NopEObjectSurrogateService();
        }
    }
    
    @Override
    public EObject getAndPinSurrogate(EObject object) {
        return object;
    }

    @Override
    public EObject peekSurrogate(EObject object) {
        return object;
    }

    @Override
    public EObject fromSurrogate(EObject surrogate) {
        return surrogate;
    }

    @Override
    public EObject getExistingSurrogate(EObject object) {
        return object;
    }
    
    @Override
    public EObject getAndUnpinExistingSurrogate(EObject object) {
        return object;
    }

    @Override
    public void collectGarbage() {
        // NOP
    }

    @Override
    public EObject fromExistingSurrogate(EObject surrogate) {
        return surrogate;
    }

    @Override
    public Set<EObject> fromExistingSurrogateSet(Set<EObject> surrogates) {
        // simple, performant override
        return surrogates;
    }
    // simple, performant override
    @Override
    public <T> Map<EObject, T> fromExistingSurrogateKeyedMap(Map<EObject, T> valMap) {
        // simple, performant override
        return valMap;
    }

}
