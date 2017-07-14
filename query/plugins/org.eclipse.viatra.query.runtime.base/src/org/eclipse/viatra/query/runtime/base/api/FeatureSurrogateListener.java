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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Interface for observing insertion and deletion of structural feature values ("settings"). (Works both for
 * single-valued and many-valued features.) 
 * 
 * <p> Surrogate values will be used instead of the original model contents for feature sources and reference targets. 
 * For attribute values, the internal canonical representation will be used. 
 * 
 * @author Tamas Szabo
 * @author Gabor Bergmann
 * @since 1.7
 * 
 */
public interface FeatureSurrogateListener<Surrogate> {
    /**
     * Called when the given value is inserted into the given feature of the given host EObject.
     * 
     * @param host
     *            the surrogate representation of the host (holder) of the feature
     * @param feature
     *            the {@link EAttribute} or {@link EReference} instance
     * @param value
     *            the target of the feature
     */
    public void featureInserted(Surrogate host, EStructuralFeature feature, Object value);

    /**
     * Called when the given value is removed from the given feature of the given host EObject.
     * 
     * @param host
     *            the surrogate representation of the host (holder) of the feature
     * @param feature
     *            the {@link EAttribute} or {@link EReference} instance
     * @param value
     *            the target of the feature
     */
    public void featureDeleted(Surrogate host, EStructuralFeature feature, Object value);
}
