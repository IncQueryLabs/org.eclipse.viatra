/*******************************************************************************
 * Copyright (c) 2010-2013, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.base.api;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;


/**
 * Listener interface for lightweight observation on EObject feature value changes.
 * 
 * @author Abel Hegedus
 *
 */
public interface LightweightEObjectObserver {
    
    /**
     * 
     * @param host
     * @param feature
     * @param notification
     */
    void notifyFeatureChanged(EObject host, EStructuralFeature feature, Notification notification);
    
}
