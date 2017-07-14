/*******************************************************************************
 * Copyright (c) 2010-2014, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.emf;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.viatra.query.runtime.api.scope.IBaseIndex;
import org.eclipse.viatra.query.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.viatra.query.runtime.api.scope.IInstanceObserver;
import org.eclipse.viatra.query.runtime.api.scope.ViatraBaseIndexChangeListener;
import org.eclipse.viatra.query.runtime.base.api.EMFBaseIndexChangeListener;
import org.eclipse.viatra.query.runtime.base.api.IEMFBaseIndex;
import org.eclipse.viatra.query.runtime.base.api.IEMFIndexingErrorListener;
import org.eclipse.viatra.query.runtime.base.api.LightweightEObjectObserver;

/**
 * Wraps the EMF base index into the IBaseIndex interface.
 * @author Bergmann Gabor
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 */
public class EMFBaseIndexWrapper<Surrogate> implements IBaseIndex {

    private final IEMFBaseIndex<Surrogate> baseIndex;
    /**
     * @return the underlying index object
     * @since 1.7
     */
    public IEMFBaseIndex<Surrogate> getWrapped() {
        return baseIndex;
    }

    /**
     * @param navigationHelper
     * @since 1.7
     */
    public EMFBaseIndexWrapper(IEMFBaseIndex<Surrogate> navigationHelper) {
        this.baseIndex = navigationHelper;
    }

    @Override
    public void resampleDerivedFeatures() {
        baseIndex.resampleDerivedFeatures();
    }


    @Override
    public <V> V coalesceTraversals(Callable<V> callable) throws InvocationTargetException {
        return baseIndex.coalesceTraversals(callable);
    }

    Map<IIndexingErrorListener, IEMFIndexingErrorListener> indexErrorListeners =
            new HashMap<IIndexingErrorListener, IEMFIndexingErrorListener>();
    @Override
    public boolean addIndexingErrorListener(final IIndexingErrorListener listener) {
        if (indexErrorListeners.containsKey(listener)) return false;
        IEMFIndexingErrorListener emfListener = new IEMFIndexingErrorListener() {
            @Override
            public void fatal(String description, Throwable t) {
                listener.fatal(description, t);
            }
            @Override
            public void error(String description, Throwable t) {
                listener.error(description, t);
            }
        };
        indexErrorListeners.put(listener, emfListener);
        return baseIndex.addIndexingErrorListener(emfListener);
    }
    @Override
    public boolean removeIndexingErrorListener(IIndexingErrorListener listener) {
        if (!indexErrorListeners.containsKey(listener)) return false;
        return baseIndex.removeIndexingErrorListener(indexErrorListeners.remove(listener));
    }
    
    
    Map<ViatraBaseIndexChangeListener, EMFBaseIndexChangeListener> indexChangeListeners = 
            new HashMap<ViatraBaseIndexChangeListener, EMFBaseIndexChangeListener>();
    @Override
    public void addBaseIndexChangeListener(final ViatraBaseIndexChangeListener listener) {
        EMFBaseIndexChangeListener emfListener = new EMFBaseIndexChangeListener() {
            @Override
            public boolean onlyOnIndexChange() {
                return listener.onlyOnIndexChange();
            }
            
            @Override
            public void notifyChanged(boolean indexChanged) {
                listener.notifyChanged(indexChanged);
            }
        };
        indexChangeListeners.put(listener, emfListener);
        baseIndex.addBaseIndexChangeListener(emfListener);
    }
    @Override
    public void removeBaseIndexChangeListener(ViatraBaseIndexChangeListener listener) {
        final EMFBaseIndexChangeListener cListener = indexChangeListeners.remove(listener);
        if (cListener != null) 
            baseIndex.removeBaseIndexChangeListener(cListener);
    }

    Map<IInstanceObserver, EObjectObserver> instanceObservers = 
                new HashMap<IInstanceObserver, EObjectObserver>();
    @Override
    public boolean addInstanceObserver(final IInstanceObserver observer,
            Object observedObject) {
        if (observedObject instanceof EObject) {
            EObjectObserver emfObserver = instanceObservers.get(observer);
            if (emfObserver == null) {		
                emfObserver = new EObjectObserver(observer);
                instanceObservers.put(observer, emfObserver);
            }
            boolean success = 
                    baseIndex.addLightweightEObjectObserver(emfObserver, (EObject) observedObject);
            if (success) emfObserver.usageCount++;
            return success;
        } else return false;
    }
    @Override
    public boolean removeInstanceObserver(IInstanceObserver observer,
            Object observedObject) {
        if (observedObject instanceof EObject) {
            EObjectObserver emfObserver = instanceObservers.get(observer);
            if (emfObserver == null)
                return false;
            boolean success = 
                    baseIndex.removeLightweightEObjectObserver(emfObserver, (EObject)observedObject);
            if (success) 
                if (0 == --emfObserver.usageCount)
                    instanceObservers.remove(observer);
            return success;
        } else return false;
    }
    private static class EObjectObserver implements LightweightEObjectObserver {
        /**
         * 
         */
        private final IInstanceObserver observer;
        int usageCount = 0; 

        /**
         * @param observer
         */
        private EObjectObserver(IInstanceObserver observer) {
            this.observer = observer;
        }

        @Override
        public void notifyFeatureChanged(EObject host,
                EStructuralFeature feature, Notification notification) {
            observer.notifyBinaryChanged(host, feature);
        }
    }

}