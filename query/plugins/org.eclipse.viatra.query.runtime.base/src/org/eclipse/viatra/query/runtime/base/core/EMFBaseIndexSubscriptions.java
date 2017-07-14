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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.viatra.query.runtime.base.api.DataTypeListener;
import org.eclipse.viatra.query.runtime.base.api.EMFBaseIndexChangeListener;
import org.eclipse.viatra.query.runtime.base.api.FeatureListener;
import org.eclipse.viatra.query.runtime.base.api.FeatureSurrogateListener;
import org.eclipse.viatra.query.runtime.base.api.IEMFIndexingErrorListener;
import org.eclipse.viatra.query.runtime.base.api.InstanceSurrogateListener;
import org.eclipse.viatra.query.runtime.base.api.LightweightEObjectObserver;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Manages listener subscriptions for the base index
 * @author Gabor Bergmann
 *
 */
public class EMFBaseIndexSubscriptions<Surrogate> {
    
    private EMFBaseIndexMetaStore<Surrogate> metaStore;
    
    /**
     * These global listeners will be called after updates.
     */
    // private final Set<Runnable> afterUpdateCallbacks;
    private final Set<EMFBaseIndexChangeListener> baseIndexChangeListeners;
    private final Map<LightweightEObjectObserver, Collection<EObject>> lightweightObservers;

    // These are the user subscriptions to notifications
    private final Map<InstanceSurrogateListener<Surrogate>, Set<EClass>> subscribedInstanceSurrogateListeners;
    private final Map<FeatureListener, Set<EStructuralFeature>> subscribedFeatureListeners;
    private final Map<FeatureSurrogateListener<Surrogate>, Set<EStructuralFeature>> subscribedFeatureSurrogateListeners;
    private final Map<DataTypeListener, Set<EDataType>> subscribedDataTypeListeners;

    // these are the internal notification tables
    // (element Type or String id) -> listener -> (subscription types)
    // if null, must be recomputed from subscriptions
    // potentially multiple subscription types for each element type because (a) nsURI collisions, (b) multiple
    // supertypes
    private Table<Object, InstanceSurrogateListener<Surrogate>, Set<EClass>> InstanceSurrogateListeners;
    private Table<Object, FeatureListener, Set<EStructuralFeature>> featureListeners;
    private Table<Object, FeatureSurrogateListener<Surrogate>, Set<EStructuralFeature>> featureSurrogateListeners;
    private Table<Object, DataTypeListener, Set<EDataType>> dataTypeListeners;

    private final Set<IEMFIndexingErrorListener> errorListeners;
    private Logger logger;
    
    
    public EMFBaseIndexSubscriptions(EMFBaseIndexMetaStore<Surrogate> metaStore, Logger logger) {
        this.metaStore = metaStore;
        this.logger = logger;
        this.subscribedInstanceSurrogateListeners = new HashMap<InstanceSurrogateListener<Surrogate>, Set<EClass>>();
        this.subscribedFeatureListeners = new HashMap<FeatureListener, Set<EStructuralFeature>>();
        this.subscribedFeatureSurrogateListeners = new HashMap<FeatureSurrogateListener<Surrogate>, Set<EStructuralFeature>>();
        this.subscribedDataTypeListeners = new HashMap<DataTypeListener, Set<EDataType>>();
        this.lightweightObservers = new HashMap<LightweightEObjectObserver, Collection<EObject>>();
        this.baseIndexChangeListeners = new HashSet<EMFBaseIndexChangeListener>();
        this.errorListeners = new LinkedHashSet<IEMFIndexingErrorListener>();
    }
    
    private Object toKey(EClassifier eClassifier) {
        return metaStore.toKey(eClassifier);
    }

    private Object toKey(EStructuralFeature feature) {
        return metaStore.toKey(feature);
    }
    
    <T> Set<T> setMinus(Collection<? extends T> a, Collection<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.removeAll(b);
        return result;
    }

    
    
    public void addInstanceSurrogateListener(Collection<EClass> classes, InstanceSurrogateListener<Surrogate> listener) {
        Set<EClass> registered = this.subscribedInstanceSurrogateListeners.get(listener);
        if (registered == null) {
            registered = new HashSet<EClass>();
            this.subscribedInstanceSurrogateListeners.put(listener, registered);
        }
        Set<EClass> delta = setMinus(classes, registered);
        if (!delta.isEmpty()) {
            registered.addAll(delta);
            if (InstanceSurrogateListeners != null) { // if already computed
                for (EClass subscriptionType : delta) {
                    final Object superElementTypeKey = toKey(subscriptionType);
                    addInstanceSurrogateListenerInternal(listener, subscriptionType, superElementTypeKey);
                    final Set<Object> subTypeKeys = metaStore.getSubTypeMap().get(superElementTypeKey);
                    if (subTypeKeys != null)
                        for (Object subTypeKey : subTypeKeys) {
                            addInstanceSurrogateListenerInternal(listener, subscriptionType, subTypeKey);
                        }
                }
            }
        }
    }

    public void removeInstanceSurrogateListener(Collection<EClass> classes, InstanceSurrogateListener<Surrogate> listener) {
        Set<EClass> restriction = this.subscribedInstanceSurrogateListeners.get(listener);
        if (restriction != null) {
            boolean changed = restriction.removeAll(classes);
            if (restriction.size() == 0) {
                this.subscribedInstanceSurrogateListeners.remove(listener);
            }
            if (changed)
                InstanceSurrogateListeners = null; // recompute later on demand
        }
    }

    public void addFeatureListener(Collection<? extends EStructuralFeature> features, FeatureListener listener) {
        Set<EStructuralFeature> registered = this.subscribedFeatureListeners.get(listener);
        if (registered == null) {
            registered = new HashSet<EStructuralFeature>();
            this.subscribedFeatureListeners.put(listener, registered);
        }
        Set<EStructuralFeature> delta = setMinus(features, registered);
        if (!delta.isEmpty()) {
            registered.addAll(delta);
            if (featureListeners != null) { // if already computed
                for (EStructuralFeature subscriptionType : delta) {
                    addFeatureListenerInternal(listener, subscriptionType, toKey(subscriptionType));
                }
            }
        }
    }

    public void removeFeatureListener(Collection<? extends EStructuralFeature> features, FeatureListener listener) {
        Collection<EStructuralFeature> restriction = this.subscribedFeatureListeners.get(listener);
        if (restriction != null) {
            boolean changed = restriction.removeAll(features);
            if (restriction.size() == 0) {
                this.subscribedFeatureListeners.remove(listener);
            }
            if (changed)
                featureListeners = null; // recompute later on demand
        }
    }

    public void addFeatureSurrogateListener(Collection<? extends EStructuralFeature> features, FeatureSurrogateListener<Surrogate> listener) {
        Set<EStructuralFeature> registered = this.subscribedFeatureSurrogateListeners.get(listener);
        if (registered == null) {
            registered = new HashSet<EStructuralFeature>();
            this.subscribedFeatureSurrogateListeners.put(listener, registered);
        }
        Set<EStructuralFeature> delta = setMinus(features, registered);
        if (!delta.isEmpty()) {
            registered.addAll(delta);
            if (featureListeners != null) { // if already computed
                for (EStructuralFeature subscriptionType : delta) {
                    addFeatureSurrogateListenerInternal(listener, subscriptionType, toKey(subscriptionType));
                }
            }
        }
    }

    public void removeFeatureSurrogateListener(Collection<? extends EStructuralFeature> features, FeatureSurrogateListener<Surrogate> listener) {
        Collection<EStructuralFeature> restriction = this.subscribedFeatureSurrogateListeners.get(listener);
        if (restriction != null) {
            boolean changed = restriction.removeAll(features);
            if (restriction.size() == 0) {
                this.subscribedFeatureSurrogateListeners.remove(listener);
            }
            if (changed)
                featureSurrogateListeners = null; // recompute later on demand
        }
    }

    public void addDataTypeListener(Collection<EDataType> types, DataTypeListener listener) {
        Set<EDataType> registered = this.subscribedDataTypeListeners.get(listener);
        if (registered == null) {
            registered = new HashSet<EDataType>();
            this.subscribedDataTypeListeners.put(listener, registered);
        }
        Set<EDataType> delta = setMinus(types, registered);
        if (!delta.isEmpty()) {
            registered.addAll(delta);
            if (dataTypeListeners != null) { // if already computed
                for (EDataType subscriptionType : delta) {
                    addDatatypeListenerInternal(listener, subscriptionType, toKey(subscriptionType));
                }
            }
        }
    }

    public void removeDataTypeListener(Collection<EDataType> types, DataTypeListener listener) {
        Collection<EDataType> restriction = this.subscribedDataTypeListeners.get(listener);
        if (restriction != null) {
            boolean changed = restriction.removeAll(types);
            if (restriction.size() == 0) {
                this.subscribedDataTypeListeners.remove(listener);
            }
            if (changed)
                dataTypeListeners = null; // recompute later on demand
        }
    }

    public boolean addLightweightEObjectObserver(LightweightEObjectObserver observer, EObject observedObject) {
        Collection<EObject> observedObjects = lightweightObservers.get(observer);
        if (observedObjects == null) {
            observedObjects = new HashSet<EObject>();
            lightweightObservers.put(observer, observedObjects);
        }
        return observedObjects.add(observedObject);
    }

    public boolean removeLightweightEObjectObserver(LightweightEObjectObserver observer, EObject observedObject) {
        boolean result = false;
        Collection<EObject> observedObjects = lightweightObservers.get(observer);
        if (observedObjects != null) {
            result = observedObjects.remove(observedObject);
            if (observedObjects.isEmpty()) {
                lightweightObservers.remove(observer);
            }
        }
        return result;
    }

    /**
     * @return the lightweightObservers
     */
    public Map<LightweightEObjectObserver, Collection<EObject>> getLightweightObservers() {
        return lightweightObservers;
    }

    /**
     * This will run after updates.
     */
    protected void notifyBaseIndexChangeListeners(boolean baseIndexChanged) {
        if (!baseIndexChangeListeners.isEmpty()) {
            for (EMFBaseIndexChangeListener listener : new ArrayList<EMFBaseIndexChangeListener>(
                    baseIndexChangeListeners)) {
                try {
                    if (!listener.onlyOnIndexChange() || baseIndexChanged) {
                        listener.notifyChanged(baseIndexChanged);
                    }
                } catch (Exception ex) {
                    notifyFatalListener("VIATRA Base encountered an error in delivering notifications about changes. ",
                            ex);
                }
            }
        }
    }

    void notifyDataTypeListeners(final Object typeKey, final Object value, final boolean isInsertion,
            final boolean firstOrLastOccurrence) {
        for (final Entry<DataTypeListener, Set<EDataType>> entry : getDataTypeListeners().row(typeKey).entrySet()) {
            final DataTypeListener listener = entry.getKey();
            for (final EDataType subscriptionType : entry.getValue()) {
                if (isInsertion) {
                    listener.dataTypeInstanceInserted(subscriptionType, value, firstOrLastOccurrence);
                } else {
                    listener.dataTypeInstanceDeleted(subscriptionType, value, firstOrLastOccurrence);
                }
            }
        }
    }

    void notifyFeatureListeners(final EObject host, final Object featureKey, final Object value,
            final boolean isInsertion) {
        for (final Entry<FeatureListener, Set<EStructuralFeature>> entry : getFeatureListeners().row(featureKey)
                .entrySet()) {
            final FeatureListener listener = entry.getKey();
            for (final EStructuralFeature subscriptionType : entry.getValue()) {
                if (isInsertion) {
                    listener.featureInserted(host, subscriptionType, value);
                } else {
                    listener.featureDeleted(host, subscriptionType, value);
                }
            }
        }
    }

    void notifyFeatureSurrogateListeners(final Surrogate host, final Object featureKey, final Object value,
            final boolean isInsertion) {
        for (final Entry<FeatureSurrogateListener<Surrogate>, Set<EStructuralFeature>> entry : 
                getFeatureSurrogateListeners().row(featureKey).entrySet()) 
        {
            final FeatureSurrogateListener<Surrogate> listener = entry.getKey();
            for (final EStructuralFeature subscriptionType : entry.getValue()) {
                if (isInsertion) {
                    listener.featureInserted(host, subscriptionType, value);
                } else {
                    listener.featureDeleted(host, subscriptionType, value);
                }
            }
        }
    }

    void notifyInstanceSurrogateListeners(final Object clazzKey, final Surrogate instance, final boolean isInsertion) {
        for (final Entry<InstanceSurrogateListener<Surrogate>, Set<EClass>> entry : getInstanceSurrogateListeners().row(clazzKey).entrySet()) {
            final InstanceSurrogateListener<Surrogate> listener = entry.getKey();
            for (final EClass subscriptionType : entry.getValue()) {
                if (isInsertion) {
                    listener.instanceInserted(subscriptionType, instance);
                } else {
                    listener.instanceDeleted(subscriptionType, instance);
                }
            }
        }
    }

    void notifyLightweightObservers(final EObject host, final EStructuralFeature feature,
            final Notification notification) {
        for (final Entry<LightweightEObjectObserver, Collection<EObject>> entry : getLightweightObservers()
                .entrySet()) {
            if (entry.getValue().contains(host)) {
                entry.getKey().notifyFeatureChanged(host, feature, notification);
            }
        }
    }

    public void addBaseIndexChangeListener(EMFBaseIndexChangeListener listener) {
        checkArgument(listener != null, "Cannot add null listener!");
        baseIndexChangeListeners.add(listener);
    }

    public void removeBaseIndexChangeListener(EMFBaseIndexChangeListener listener) {
        checkArgument(listener != null, "Cannot remove null listener!");
        baseIndexChangeListeners.remove(listener);
    }

    public boolean addIndexingErrorListener(IEMFIndexingErrorListener listener) {
        return errorListeners.add(listener);
    }

    public boolean removeIndexingErrorListener(IEMFIndexingErrorListener listener) {
        return errorListeners.remove(listener);
    }

    public void notifyErrorListener(String message, Throwable t) {
        logger.error(message, t);
        for (IEMFIndexingErrorListener listener : errorListeners) {
            listener.error(message, t);
        }
    }

    public void notifyFatalListener(String message, Throwable t) {
        logger.fatal(message, t);
        for (IEMFIndexingErrorListener listener : errorListeners) {
            listener.fatal(message, t);
        }
    }
    
    /**
     * @return the InstanceSurrogateListeners
     */
    Table<Object, InstanceSurrogateListener<Surrogate>, Set<EClass>> getInstanceSurrogateListeners() {
        if (InstanceSurrogateListeners == null) {
            InstanceSurrogateListeners = HashBasedTable.create(100, 1);
            for (Entry<InstanceSurrogateListener<Surrogate>, Set<EClass>> subscription : subscribedInstanceSurrogateListeners.entrySet()) {
                final InstanceSurrogateListener<Surrogate> listener = subscription.getKey();
                for (EClass subscriptionType : subscription.getValue()) {
                    final Object superElementTypeKey = toKey(subscriptionType);
                    addInstanceSurrogateListenerInternal(listener, subscriptionType, superElementTypeKey);
                    final Set<Object> subTypeKeys = metaStore.getSubTypeMap().get(superElementTypeKey);
                    if (subTypeKeys != null)
                        for (Object subTypeKey : subTypeKeys) {
                            addInstanceSurrogateListenerInternal(listener, subscriptionType, subTypeKey);
                        }
                }
            }
        }
        return InstanceSurrogateListeners;
    }

    Table<Object, InstanceSurrogateListener<Surrogate>, Set<EClass>> peekInstanceSurrogateListeners() {
        return InstanceSurrogateListeners;
    }

    void addInstanceSurrogateListenerInternal(final InstanceSurrogateListener<Surrogate> listener, EClass subscriptionType,
            final Object elementTypeKey) {
        Set<EClass> subscriptionTypes = InstanceSurrogateListeners.get(elementTypeKey, listener);
        if (subscriptionTypes == null) {
            subscriptionTypes = new HashSet<EClass>(1);
            InstanceSurrogateListeners.put(elementTypeKey, listener, subscriptionTypes);
        }
        subscriptionTypes.add(subscriptionType);
    }

    /**
     * @return the featureListeners
     */
    Table<Object, FeatureListener, Set<EStructuralFeature>> getFeatureListeners() {
        if (featureListeners == null) {
            featureListeners = HashBasedTable.create(100, 1);
            for (Entry<FeatureListener, Set<EStructuralFeature>> subscription : subscribedFeatureListeners.entrySet()) {
                final FeatureListener listener = subscription.getKey();
                for (EStructuralFeature subscriptionType : subscription.getValue()) {
                    final Object elementTypeKey = toKey(subscriptionType);
                    addFeatureListenerInternal(listener, subscriptionType, elementTypeKey);
                }
            }
        }
        return featureListeners;
    }
    Table<Object, FeatureSurrogateListener<Surrogate>, Set<EStructuralFeature>> getFeatureSurrogateListeners() {
        if (featureSurrogateListeners == null) {
            featureSurrogateListeners = HashBasedTable.create(100, 1);
            for (Entry<FeatureSurrogateListener<Surrogate>, Set<EStructuralFeature>> subscription : subscribedFeatureSurrogateListeners.entrySet()) {
                final FeatureSurrogateListener<Surrogate> listener = subscription.getKey();
                for (EStructuralFeature subscriptionType : subscription.getValue()) {
                    final Object elementTypeKey = toKey(subscriptionType);
                    addFeatureSurrogateListenerInternal(listener, subscriptionType, elementTypeKey);
                }
            }
        }
        return featureSurrogateListeners;
    }

    void addFeatureListenerInternal(final FeatureListener listener, EStructuralFeature subscriptionType,
            final Object elementTypeKey) {
        Set<EStructuralFeature> subscriptionTypes = featureListeners.get(elementTypeKey, listener);
        if (subscriptionTypes == null) {
            subscriptionTypes = new HashSet<EStructuralFeature>(1);
            featureListeners.put(elementTypeKey, listener, subscriptionTypes);
        }
        subscriptionTypes.add(subscriptionType);
    }

    void addFeatureSurrogateListenerInternal(final FeatureSurrogateListener<Surrogate> listener, 
            EStructuralFeature subscriptionType,
            final Object elementTypeKey) {
        Set<EStructuralFeature> subscriptionTypes = featureSurrogateListeners.get(elementTypeKey, listener);
        if (subscriptionTypes == null) {
            subscriptionTypes = new HashSet<EStructuralFeature>(1);
            featureSurrogateListeners.put(elementTypeKey, listener, subscriptionTypes);
        }
        subscriptionTypes.add(subscriptionType);
    }

    /**
     * @return the dataTypeListeners
     */
    Table<Object, DataTypeListener, Set<EDataType>> getDataTypeListeners() {
        if (dataTypeListeners == null) {
            dataTypeListeners = HashBasedTable.create(100, 1);
            for (Entry<DataTypeListener, Set<EDataType>> subscription : subscribedDataTypeListeners.entrySet()) {
                final DataTypeListener listener = subscription.getKey();
                for (EDataType subscriptionType : subscription.getValue()) {
                    final Object elementTypeKey = toKey(subscriptionType);
                    addDatatypeListenerInternal(listener, subscriptionType, elementTypeKey);
                }
            }
        }
        return dataTypeListeners;
    }

    void addDatatypeListenerInternal(final DataTypeListener listener, EDataType subscriptionType,
            final Object elementTypeKey) {
        Set<EDataType> subscriptionTypes = dataTypeListeners.get(elementTypeKey, listener);
        if (subscriptionTypes == null) {
            subscriptionTypes = new HashSet<EDataType>(1);
            dataTypeListeners.put(elementTypeKey, listener, subscriptionTypes);
        }
        subscriptionTypes.add(subscriptionType);
    }
    
    void ensureNoListenersForDispose() {
        if (!(baseIndexChangeListeners.isEmpty()
                && subscribedFeatureListeners.isEmpty()
                && subscribedFeatureSurrogateListeners.isEmpty()
                && subscribedDataTypeListeners.isEmpty() 
                && subscribedInstanceSurrogateListeners.isEmpty()))
            throw new IllegalStateException("Cannot dispose while there are active listeners");
    }
    
}
