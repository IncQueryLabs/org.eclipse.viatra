/*******************************************************************************
 * Copyright (c) 2010-2015, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.emf;

import org.apache.log4j.Logger;
import org.eclipse.viatra.query.runtime.base.api.IEMFBaseIndex;
import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;
import org.eclipse.viatra.query.runtime.matchers.tuple.Tuples;

/**
 * In dynamic EMF mode, we need to make sure that EEnum literal constants and values returned by eval() expressions 
 * 	are canonicalized in the same way as enum literals from the EMF model.
 * 
 * <p> This canonicalization is a one-way mapping, so 
 * 	{@link #unwrapElement(Object)} and {@link #unwrapTuple(Object)} remain NOPs.
 * 
 * @author Bergmann Gabor
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 */
public class DynamicEMFQueryRuntimeContext<Surrogate> extends EMFQueryRuntimeContext<Surrogate> {

    /**
     * @since 1.7
     */
    public DynamicEMFQueryRuntimeContext(IEMFBaseIndex<Surrogate> baseIndex, Logger logger, EMFScope emfScope) {
        super(baseIndex, logger, emfScope);
    }	
    
    @Override
    public Object wrapElement(Object externalElement) {
        return baseIndex.toCanonicalValueRepresentation(externalElement);
    }
    
    @Override
    public Tuple wrapTuple(Tuple externalElements) {
        Object[] elements = externalElements.getElements();
        for (int i=0; i< elements.length; ++i)
            elements[i] = wrapElement(elements[i]);
        return Tuples.flatTupleOf(elements);
    }
    
    
    
}
