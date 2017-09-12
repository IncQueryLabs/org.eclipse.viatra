/*******************************************************************************
 * Copyright (c) 2010-2012, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Szabo, Gabor Bergmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.base.core;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.NotifyingList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.query.runtime.base.api.BaseIndexOptions;
import org.eclipse.viatra.query.runtime.base.api.DataTypeListener;
import org.eclipse.viatra.query.runtime.base.api.EMFBaseIndexChangeListener;
import org.eclipse.viatra.query.runtime.base.api.FeatureListener;
import org.eclipse.viatra.query.runtime.base.api.IEClassifierProcessor.IEClassProcessor;
import org.eclipse.viatra.query.runtime.base.api.IEClassifierProcessor.IEDataTypeProcessor;
import org.eclipse.viatra.query.runtime.base.api.IEMFIndexingErrorListener;
import org.eclipse.viatra.query.runtime.base.api.IEStructuralFeatureProcessor;
import org.eclipse.viatra.query.runtime.base.api.IndexingLevel;
import org.eclipse.viatra.query.runtime.base.api.InstanceListener;
import org.eclipse.viatra.query.runtime.base.api.LightweightEObjectObserver;
import org.eclipse.viatra.query.runtime.base.api.NavigationHelper;
import org.eclipse.viatra.query.runtime.base.api.filters.IBaseIndexObjectFilter;
import org.eclipse.viatra.query.runtime.base.api.filters.IBaseIndexResourceFilter;
import org.eclipse.viatra.query.runtime.base.comprehension.EMFModelComprehension;
import org.eclipse.viatra.query.runtime.base.comprehension.EMFVisitor;
import org.eclipse.viatra.query.runtime.base.exception.ViatraBaseException;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

public class NavigationHelperImpl implements NavigationHelper {

    /**
     * This is never null.
     */
    protected IndexingLevel wildcardMode;

    protected Notifier notifier;
    protected Set<Notifier> modelRoots;
    private boolean expansionAllowed;
    private boolean traversalDescendsAlongCrossResourceContainment;
    // protected NavigationHelperVisitor visitor;
    protected NavigationHelperContentAdapter contentAdapter;

    private final Logger logger;

    // type object or String id
    protected Map<Object, IndexingLevel> directlyObservedClasses = new HashMap<Object, IndexingLevel>();
    // including subclasses; if null, must be recomputed
    protected Map<Object, IndexingLevel> allObservedClasses = null;
    protected Map<Object, IndexingLevel> observedDataTypes;
    protected Map<Object, IndexingLevel> observedFeatures;
    // ignore RESOLVE for these features, as they are just starting to be observed - see [428458]
    protected Set<Object> ignoreResolveNotificationFeatures;

    /**
     * Feature registration and model traversal is delayed while true
     */
    protected boolean delayTraversals = false;
    /**
     * Classes (or String ID in dynamic mode) to be registered once the coalescing period is over
     */
    protected Map<Object, IndexingLevel> delayedClasses;
    /**
     * EStructuralFeatures (or String ID in dynamic mode) to be registered once the coalescing period is over
     */
    protected Map<Object, IndexingLevel> delayedFeatures;
    /**
     * EDataTypes (or String ID in dynamic mode) to be registered once the coalescing period is over
     */
    protected Map<Object, IndexingLevel> delayedDataTypes;

    /**
     * Features per EObject to be resolved later (towards the end of a coalescing period when no Resources are loading)
     */
    protected Multimap<EObject, EReference> delayedProxyResolutions = LinkedHashMultimap.create();
    /**
     * Reasources that are currently loading, implying the proxy resolution attempts should be delayed
     */
    protected Set<Resource> resolutionDelayingResources = new HashSet<Resource>();
    
    protected Queue<Runnable> traversalCallbacks = new LinkedList<Runnable>();

    /**
     * These global listeners will be called after updates.
     */
    // private final Set<Runnable> afterUpdateCallbacks;
    private final Set<EMFBaseIndexChangeListener> baseIndexChangeListeners;
    private final Map<LightweightEObjectObserver, Collection<EObject>> lightweightObservers;

    // These are the user subscriptions to notifications
    private final Map<InstanceListener, Set<EClass>> subscribedInstanceListeners;
    private final Map<FeatureListener, Set<EStructuralFeature>> subscribedFeatureListeners;
    private final Map<DataTypeListener, Set<EDataType>> subscribedDataTypeListeners;

    // these are the internal notification tables
    // (element Type or String id) -> listener -> (subscription types)
    // if null, must be recomputed from subscriptions
    // potentially multiple subscription types for each element type because (a) nsURI collisions, (b) multiple
    // supertypes
    private Table<Object, InstanceListener, Set<EClass>> instanceListeners;
    private Table<Object, FeatureListener, Set<EStructuralFeature>> featureListeners;
    private Table<Object, DataTypeListener, Set<EDataType>> dataTypeListeners;

    private final Set<IEMFIndexingErrorListener> errorListeners;
    private final BaseIndexOptions baseIndexOptions;

    private EMFModelComprehension comprehension;
    
    private boolean loggedRegistrationMessage = false;

    EMFBaseIndexMetaStore metaStore;
    EMFBaseIndexInstanceStore instanceStore;
    EMFBaseIndexStatisticsStore statsStore;

    <T> Set<T> setMinus(Collection<? extends T> a, Collection<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.removeAll(b);
        return result;
    }

    @SuppressWarnings("unchecked")
    <T extends EObject> Set<T> resolveAllInternal(Set<? extends T> a) {
        if (a == null)
            a = Collections.emptySet();
        Set<T> result = new HashSet<T>();
        for (T t : a) {
            if (t.eIsProxy()) {
                result.add((T) EcoreUtil.resolve(t, (ResourceSet) null));
            } else {
                result.add(t);
            }
        }
        return result;
    }

    Set<Object> resolveClassifiersToKey(Set<? extends EClassifier> classes) {
        Set<? extends EClassifier> resolveds = resolveAllInternal(classes);
        Set<Object> result = new HashSet<Object>();
        for (EClassifier resolved : resolveds) {
            result.add(toKey(resolved));
        }
        return result;
    }

    Set<Object> resolveFeaturesToKey(Set<? extends EStructuralFeature> features) {
        Set<EStructuralFeature> resolveds = resolveAllInternal(features);
        Set<Object> result = new HashSet<Object>();
        for (EStructuralFeature resolved : resolveds) {
            result.add(toKey(resolved));
        }
        return result;
    }

    @Override
    public boolean isInWildcardMode() {
        return isInWildcardMode(IndexingLevel.FULL);
    }
  
    @Override
    public boolean isInWildcardMode(IndexingLevel level) {
        return wildcardMode.providesLevel(level);
    }

    @Override
    public boolean isInDynamicEMFMode() {
        return baseIndexOptions.isDynamicEMFMode();
    }

    /**
     * @return the baseIndexOptions
     */
    public BaseIndexOptions getBaseIndexOptions() {
        return baseIndexOptions.copy();
    }

    /**
     * @return the comprehension
     */
    public EMFModelComprehension getComprehension() {
        return comprehension;
    }

    public NavigationHelperImpl(Notifier emfRoot, BaseIndexOptions options, Logger logger) throws ViatraBaseException {
        this.baseIndexOptions = options.copy();
        this.logger = logger;
        assert (logger != null);

        this.comprehension = new EMFModelComprehension(baseIndexOptions);
        this.wildcardMode = baseIndexOptions.getWildcardLevel();
        this.subscribedInstanceListeners = new HashMap<InstanceListener, Set<EClass>>();
        this.subscribedFeatureListeners = new HashMap<FeatureListener, Set<EStructuralFeature>>();
        this.subscribedDataTypeListeners = new HashMap<DataTypeListener, Set<EDataType>>();
        this.lightweightObservers = new HashMap<LightweightEObjectObserver, Collection<EObject>>();
        this.observedFeatures = new HashMap<Object, IndexingLevel>();
        this.ignoreResolveNotificationFeatures = new HashSet<Object>();
        this.observedDataTypes = new HashMap<Object, IndexingLevel>();

        metaStore = new EMFBaseIndexMetaStore(this);
        instanceStore = new EMFBaseIndexInstanceStore(this, logger);
        statsStore = new EMFBaseIndexStatisticsStore(this, logger);

        this.contentAdapter = new NavigationHelperContentAdapter(this);
        this.baseIndexChangeListeners = new HashSet<EMFBaseIndexChangeListener>();
        this.errorListeners = new LinkedHashSet<IEMFIndexingErrorListener>();

        this.notifier = emfRoot;
        this.modelRoots = new HashSet<Notifier>();
        this.expansionAllowed = false;
        this.traversalDescendsAlongCrossResourceContainment = false;

        if (emfRoot != null) {
            addRootInternal(emfRoot);
        }
              
    }
    
    @Override
    public IndexingLevel getWildcardLevel() {
        return wildcardMode;
    }
    
    @Override
    public void setWildcardLevel(final IndexingLevel level) {
        try{
            IndexingLevel mergedLevel = NavigationHelperImpl.this.wildcardMode.merge(level);
            if (mergedLevel != NavigationHelperImpl.this.wildcardMode){
                NavigationHelperImpl.this.wildcardMode = mergedLevel;

                // force traversal upon change of wildcard level
                final NavigationHelperVisitor visitor = new NavigationHelperVisitor.TraversingVisitor(this,
                        Collections.<Object, IndexingLevel>emptyMap(), Collections.<Object, IndexingLevel>emptyMap(), Collections.<Object, IndexingLevel>emptyMap(), Collections.<Object, IndexingLevel>emptyMap());
                coalesceTraversals(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        traverse(visitor);
                        return null;
                    }

                }); 
            }
        } catch (InvocationTargetException ex) {
            processingFatal(ex.getCause(), "Setting wildcard level: " + level);
        } catch (Exception ex) {
            processingFatal(ex, "Setting wildcard level: " + level);
        }
    }

    public NavigationHelperContentAdapter getContentAdapter() {
        return contentAdapter;
    }

    public Map<Object, IndexingLevel> getObservedFeaturesInternal() {
        return observedFeatures;
    }

    public boolean isFeatureResolveIgnored(EStructuralFeature feature) {
        return ignoreResolveNotificationFeatures.contains(toKey(feature));
    }

    @Override
    public void dispose() {
        ensureNoListenersForDispose();
        for (Notifier root : modelRoots) {
            contentAdapter.removeAdapter(root);
        }
    }

    @Override
    public Set<Object> getDataTypeInstances(EDataType type) {
        Object typeKey = toKey(type);
        Map<Object, Integer> valMap = instanceStore.getDataTypeMap(typeKey);
        if (valMap != null) {
            return Collections.unmodifiableSet(valMap.keySet());
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean isInstanceOfDatatype(Object value, EDataType type) {
        Object typeKey = toKey(type);
        Map<Object, Integer> valMap = instanceStore.getDataTypeMap(typeKey);
        return valMap != null && valMap.containsKey(value);
    }

    @Override
    public Set<Setting> findByAttributeValue(Object value_) {
        Object value = toCanonicalValueRepresentation(value_);
        Set<Setting> retSet = new HashSet<Setting>();
        Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().row(value);

        for (Entry<Object, Collection<EObject>> entry : valMap.entrySet()) {
            final Collection<EObject> holders = entry.getValue();
            EStructuralFeature feature = metaStore.getKnownFeatureForKey(entry.getKey());
            for (EObject holder : EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders)) {
                retSet.add(new NavigationHelperSetting(feature, holder, value));
            }
        }

        return retSet;
    }

    @Override
    public Set<Setting> findByAttributeValue(Object value_, Collection<EAttribute> attributes) {
        Object value = toCanonicalValueRepresentation(value_);
        Set<Setting> retSet = new HashSet<Setting>();
        Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().row(value);

        for (EAttribute attr : attributes) {
            Object feature = toKey(attr);
            final Collection<EObject> holders = valMap.get(feature);
            if (holders != null) {
                for (EObject holder : EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders)) {
                    retSet.add(new NavigationHelperSetting(attr, holder, value));
                }
            }
        }

        return retSet;
    }

    @Override
    public Set<EObject> findByAttributeValue(Object value_, EAttribute attribute) {
        Object value = toCanonicalValueRepresentation(value_);
        Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().row(value);
        Object feature = toKey(attribute);
        final Collection<EObject> holders = valMap.get(feature);
        if (holders == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders));
        }
    }

    @Override
    public void processAllFeatureInstances(EStructuralFeature feature, IEStructuralFeatureProcessor processor) {
        final Map<Object, Collection<EObject>> instanceMap = instanceStore.getValueToFeatureToHolderMap()
                .column(toKey(feature));
        for (Entry<Object, Collection<EObject>> entry : instanceMap.entrySet()) {
            final Collection<EObject> holders = entry.getValue();
            for (EObject src : EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders)) {
                processor.process(feature, src, entry.getKey());
            }
        }
    }

    @Override
    public void processDirectInstances(EClass type, IEClassProcessor processor) {
        Object typeKey = toKey(type);
        processDirectInstancesInternal(type, processor, typeKey);
    }

    @Override
    public void processAllInstances(EClass type, IEClassProcessor processor) {
        Object typeKey = toKey(type);
        Set<Object> subTypes = metaStore.getSubTypeMap().get(typeKey);
        if (subTypes != null) {
            for (Object subTypeKey : subTypes) {
                processDirectInstancesInternal(type, processor, subTypeKey);
            }
        }
        processDirectInstancesInternal(type, processor, typeKey);
    }

    @Override
    public void processDataTypeInstances(EDataType type, IEDataTypeProcessor processor) {
        Object typeKey = toKey(type);
        Map<Object, Integer> valMap = instanceStore.getDataTypeMap(typeKey);
        if (valMap == null) {
            return;
        }
        for (Object value : valMap.keySet()) {
            processor.process(type, value);
        }
    }

    private void processDirectInstancesInternal(EClass type, IEClassProcessor processor, Object typeKey) {
        final Set<EObject> instances = instanceStore.getInstanceSet(typeKey);
        if (instances != null) {
            for (EObject eObject : instances) {
                processor.process(type, eObject);
            }
        }
    }

    @Override
    public Set<Setting> getInverseReferences(EObject target) {
        Set<Setting> retSet = new HashSet<Setting>();
        Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().row(target);

        for (Entry<Object, Collection<EObject>> entry : valMap.entrySet()) {
            final Collection<EObject> holders = entry.getValue();
            for (EObject source : EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders)) {
                EStructuralFeature feature = metaStore.getKnownFeatureForKey(entry.getKey());
                retSet.add(new NavigationHelperSetting(feature, source, target));
            }
        }

        return retSet;
    }

    @Override
    public Set<Setting> getInverseReferences(EObject target, Collection<EReference> references) {
        Set<Setting> retSet = new HashSet<Setting>();
        Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().row(target);

        for (EReference ref : references) {
            Object feature = toKey(ref);
            final Collection<EObject> holders = valMap.get(feature);
            if (holders != null) {
                for (EObject source : EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders)) {
                    retSet.add(new NavigationHelperSetting(ref, source, target));
                }
            }
        }

        return retSet;
    }

    @Override
    public Set<EObject> getInverseReferences(EObject target, EReference reference) {
        Object feature = toKey(reference);
        Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().row(target);
        final Collection<EObject> holders = valMap.get(feature);
        if (holders == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<EObject> getReferenceValues(EObject source, EReference reference) {
        Set<Object> targets = getFeatureTargets(source, reference);
        return (Set<EObject>) (Set<?>) targets; // this is known to be safe, as EReferences can only point to EObjects
    }

    @Override
    public Set<Object> getFeatureTargets(EObject source, EStructuralFeature _feature) {
        Object feature = toKey(_feature);
        final Set<Object> valSet = instanceStore.getHolderToFeatureToValueMap().get(source, feature);
        if (valSet == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(valSet);
        }
    }

    @Override
    public Map<EObject, Set<Object>> getFeatureInstances(EStructuralFeature _feature) {
        Object feature = toKey(_feature);
        final Map<EObject, Set<Object>> valMap = instanceStore.getHolderToFeatureToValueMap().column(feature);
        if (valMap == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(valMap);
        }
    }

    @Override
    public boolean isFeatureInstance(EObject source, Object target, EStructuralFeature _feature) {
        Object feature = toKey(_feature);
        final Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().column(feature);
        return valMap != null && valMap.containsKey(target) && valMap.get(target).contains(source);
    }

    @Override
    public Set<EObject> getDirectInstances(EClass type) {
        Object typeKey = toKey(type);
        Set<EObject> valSet = instanceStore.getInstanceSet(typeKey);
        if (valSet == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(valSet);
        }
    }

    private Object toKey(EClassifier eClassifier) {
        return metaStore.toKey(eClassifier);
    }

    private Object toKey(EStructuralFeature feature) {
        return metaStore.toKey(feature);
    }

    @Override
    public Object toCanonicalValueRepresentation(Object value) {
        return metaStore.toInternalValueRepresentation(value);
    }

    @Override
    public Set<EObject> getAllInstances(EClass type) {
        Set<EObject> retSet = new HashSet<EObject>();

        Object typeKey = toKey(type);
        Set<Object> subTypes = metaStore.getSubTypeMap().get(typeKey);
        if (subTypes != null) {
            for (Object subTypeKey : subTypes) {
                final Set<EObject> instances = instanceStore.getInstanceSet(subTypeKey);
                if (instances != null) {
                    retSet.addAll(instances);
                }
            }
        }
        final Set<EObject> instances = instanceStore.getInstanceSet(typeKey);
        if (instances != null) {
            retSet.addAll(instances);
        }

        return retSet;
    }
    
    @Override
    public boolean isInstanceOfUnscoped(EObject object, EClass clazz) {
        Object candidateTypeKey = toKey(clazz);
        Object typeKey = toKey(object.eClass()); 
        
        if (candidateTypeKey.equals(typeKey)) return true;
        if (metaStore.getEObjectClassKey().equals(candidateTypeKey)) return true;
                    
        Set<Object> superTypes = metaStore.getSuperTypeMap().get(typeKey);
        return superTypes.contains(candidateTypeKey);            
    }
    
    @Override
    public boolean isInstanceOfScoped(EObject object, EClass clazz) {
        Object typeKey = toKey(clazz);
        Set<Object> subTypes = metaStore.getSubTypeMap().get(typeKey);
        if (subTypes != null) {
            for (Object subTypeKey : subTypes) {
                final Set<EObject> instances = instanceStore.getInstanceSet(subTypeKey);
                if (instances != null && instances.contains(object)) {
                    return true;
                }
            }
        }
        final Set<EObject> instances = instanceStore.getInstanceSet(typeKey);
        return instances != null && instances.contains(object);
    }

    @Override
    public Set<EObject> findByFeatureValue(Object value_, EStructuralFeature _feature) {
        Object value = toCanonicalValueRepresentation(value_);
        Object feature = toKey(_feature);
        Set<EObject> retSet = new HashSet<EObject>();
        Map<Object, Collection<EObject>> valMap = instanceStore.getValueToFeatureToHolderMap().row(value);
        final Collection<EObject> holders = valMap.get(feature);
        if (holders != null) {
            retSet.addAll(EMFBaseIndexInstanceStore.holderCollectionToUniqueSet(holders));
        }
        return retSet;
    }

    @Override
    public Set<EObject> getHoldersOfFeature(EStructuralFeature _feature) {
        Object feature = toKey(_feature);
        Multiset<EObject> holders = instanceStore.getFeatureToHolderMap().get(feature);
        if (holders == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(holders.elementSet());
        }
    }

    @Override
    public void addInstanceListener(Collection<EClass> classes, InstanceListener listener) {
        Set<EClass> registered = this.subscribedInstanceListeners.get(listener);
        if (registered == null) {
            registered = new HashSet<EClass>();
            this.subscribedInstanceListeners.put(listener, registered);
        }
        Set<EClass> delta = setMinus(classes, registered);
        if (!delta.isEmpty()) {
            registered.addAll(delta);
            if (instanceListeners != null) { // if already computed
                for (EClass subscriptionType : delta) {
                    final Object superElementTypeKey = toKey(subscriptionType);
                    addInstanceListenerInternal(listener, subscriptionType, superElementTypeKey);
                    final Set<Object> subTypeKeys = metaStore.getSubTypeMap().get(superElementTypeKey);
                    if (subTypeKeys != null)
                        for (Object subTypeKey : subTypeKeys) {
                            addInstanceListenerInternal(listener, subscriptionType, subTypeKey);
                        }
                }
            }
        }
    }

    @Override
    public void removeInstanceListener(Collection<EClass> classes, InstanceListener listener) {
        Set<EClass> restriction = this.subscribedInstanceListeners.get(listener);
        if (restriction != null) {
            boolean changed = restriction.removeAll(classes);
            if (restriction.size() == 0) {
                this.subscribedInstanceListeners.remove(listener);
            }
            if (changed)
                instanceListeners = null; // recompute later on demand
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    /**
     * @return the observedDataTypes
     */
    public Map<Object, IndexingLevel> getObservedDataTypesInternal() {
        return observedDataTypes;
    }

    @Override
    public boolean addLightweightEObjectObserver(LightweightEObjectObserver observer, EObject observedObject) {
        Collection<EObject> observedObjects = lightweightObservers.get(observer);
        if (observedObjects == null) {
            observedObjects = new HashSet<EObject>();
            lightweightObservers.put(observer, observedObjects);
        }
        return observedObjects.add(observedObject);
    }

    @Override
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

    public void notifyBaseIndexChangeListeners() {
        notifyBaseIndexChangeListeners(instanceStore.isDirty);
        if (instanceStore.isDirty) {
            instanceStore.isDirty = false;
        }
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

    void notifyInstanceListeners(final Object clazzKey, final EObject instance, final boolean isInsertion) {
        for (final Entry<InstanceListener, Set<EClass>> entry : getInstanceListeners().row(clazzKey).entrySet()) {
            final InstanceListener listener = entry.getKey();
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

    @Override
    public void addBaseIndexChangeListener(EMFBaseIndexChangeListener listener) {
        checkArgument(listener != null, "Cannot add null listener!");
        baseIndexChangeListeners.add(listener);
    }

    @Override
    public void removeBaseIndexChangeListener(EMFBaseIndexChangeListener listener) {
        checkArgument(listener != null, "Cannot remove null listener!");
        baseIndexChangeListeners.remove(listener);
    }

    @Override
    public boolean addIndexingErrorListener(IEMFIndexingErrorListener listener) {
        return errorListeners.add(listener);
    }

    @Override
    public boolean removeIndexingErrorListener(IEMFIndexingErrorListener listener) {
        return errorListeners.remove(listener);
    }

    protected void processingFatal(final Throwable ex, final String task) {
        notifyFatalListener(logTaskFormat(task), ex);
    }

    protected void processingError(final Throwable ex, final String task) {
        notifyErrorListener(logTaskFormat(task), ex);
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

    private String logTaskFormat(final String task) {
        return "VIATRA Query encountered an error in processing the EMF model. " + "This happened while trying to "
                + task;
    }

    protected void considerForExpansion(EObject obj) {
        if (expansionAllowed) {
            Resource eResource = obj.eResource();
            if (eResource != null && eResource.getResourceSet() == null) {
                expandToAdditionalRoot(eResource);
            }
        }
    }

    protected void expandToAdditionalRoot(Notifier root) {
        if (modelRoots.contains(root))
            return;

        if (root instanceof ResourceSet) {
            expansionAllowed = true;
        } else if (root instanceof Resource) {
            IBaseIndexResourceFilter resourceFilter = baseIndexOptions.getResourceFilterConfiguration();
            if (resourceFilter != null && resourceFilter.isResourceFiltered((Resource) root))
                return;
        } else { // root instanceof EObject
            traversalDescendsAlongCrossResourceContainment = true;
        }
        final IBaseIndexObjectFilter objectFilter = baseIndexOptions.getObjectFilterConfiguration();
        if (objectFilter != null && objectFilter.isFiltered(root))
            return;

        // no veto by filters
        modelRoots.add(root);
        contentAdapter.addAdapter(root);
        notifyBaseIndexChangeListeners();
    }

    /**
     * @return the expansionAllowed
     */
    public boolean isExpansionAllowed() {
        return expansionAllowed;
    }

    public boolean traversalDescendsAlongCrossResourceContainment() {
        return traversalDescendsAlongCrossResourceContainment;
    }

    /**
     * @return the directlyObservedClasses
     */
    public Set<Object> getDirectlyObservedClassesInternal() {
        return directlyObservedClasses.keySet();
    }

    boolean isObservedInternal(Object clazzKey) {
        return isInWildcardMode() || getAllObservedClassesInternal().containsKey(clazzKey);
    }

    /**
     * Add the given item the map with the given indexing level if it wasn't already added with a higher level.
     * 
     * @param map
     * @param key
     * @param level
     */
    private static <V> void putIntoMapIfHigherLevel(Map<V, IndexingLevel> map, V key, IndexingLevel level) {
        IndexingLevel l = map.get(key);
        if (l == null || level.compareTo(l) > 0) {
            map.put(key, level);
        }
    }

    private void addObservedClassesInternal(Object eClassKey, IndexingLevel level) {
        putIntoMapIfHigherLevel(allObservedClasses, eClassKey, level);
        final Set<Object> subTypes = metaStore.getSubTypeMap().get(eClassKey);
        if (subTypes != null) {
            for (Object subType : subTypes) {
                /*
                 * It is necessary to check if the class has already been added with a higher indexing level as in case
                 * of multiple inheritance, a subclass may be registered for statistics only but full indexing may be
                 * required via one of its super classes.
                 */
                putIntoMapIfHigherLevel(allObservedClasses, subType, level);
            }
        }
    }

    /**
     * not just the directly observed classes, but also their known subtypes
     */
    public Map<Object, IndexingLevel> getAllObservedClassesInternal() {
        if (allObservedClasses == null) {
            allObservedClasses = new HashMap<Object, IndexingLevel>();
            for (Entry<Object, IndexingLevel> entry : directlyObservedClasses.entrySet()) {
                Object eClassKey = entry.getKey();
                IndexingLevel level = entry.getValue();
                addObservedClassesInternal(eClassKey, level);
            }
        }
        return allObservedClasses;
    }

    /**
     * @return the instanceListeners
     */
    Table<Object, InstanceListener, Set<EClass>> getInstanceListeners() {
        if (instanceListeners == null) {
            instanceListeners = HashBasedTable.create(100, 1);
            for (Entry<InstanceListener, Set<EClass>> subscription : subscribedInstanceListeners.entrySet()) {
                final InstanceListener listener = subscription.getKey();
                for (EClass subscriptionType : subscription.getValue()) {
                    final Object superElementTypeKey = toKey(subscriptionType);
                    addInstanceListenerInternal(listener, subscriptionType, superElementTypeKey);
                    final Set<Object> subTypeKeys = metaStore.getSubTypeMap().get(superElementTypeKey);
                    if (subTypeKeys != null)
                        for (Object subTypeKey : subTypeKeys) {
                            addInstanceListenerInternal(listener, subscriptionType, subTypeKey);
                        }
                }
            }
        }
        return instanceListeners;
    }

    Table<Object, InstanceListener, Set<EClass>> peekInstanceListeners() {
        return instanceListeners;
    }

    void addInstanceListenerInternal(final InstanceListener listener, EClass subscriptionType,
            final Object elementTypeKey) {
        Set<EClass> subscriptionTypes = instanceListeners.get(elementTypeKey, listener);
        if (subscriptionTypes == null) {
            subscriptionTypes = new HashSet<EClass>();
            instanceListeners.put(elementTypeKey, listener, subscriptionTypes);
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

    void addFeatureListenerInternal(final FeatureListener listener, EStructuralFeature subscriptionType,
            final Object elementTypeKey) {
        Set<EStructuralFeature> subscriptionTypes = featureListeners.get(elementTypeKey, listener);
        if (subscriptionTypes == null) {
            subscriptionTypes = new HashSet<EStructuralFeature>();
            featureListeners.put(elementTypeKey, listener, subscriptionTypes);
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
            subscriptionTypes = new HashSet<EDataType>();
            dataTypeListeners.put(elementTypeKey, listener, subscriptionTypes);
        }
        subscriptionTypes.add(subscriptionType);
    }

    public void registerObservedTypes(Set<EClass> classes, Set<EDataType> dataTypes,
            Set<? extends EStructuralFeature> features) {
        registerObservedTypes(classes, dataTypes, features, IndexingLevel.FULL);
    }

    @Override
    public void registerObservedTypes(Set<EClass> classes, Set<EDataType> dataTypes,
            Set<? extends EStructuralFeature> features, final IndexingLevel level) {
        if (isRegistrationNecessary(level) && (classes != null || features != null || dataTypes != null)) {
            final Set<Object> resolvedFeatures = resolveFeaturesToKey(features);
            final Set<Object> resolvedClasses = resolveClassifiersToKey(classes);
            final Set<Object> resolvedDatatypes = resolveClassifiersToKey(dataTypes);

            try {
                coalesceTraversals(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        Function<Object, IndexingLevel> f = new Function<Object, IndexingLevel>() {

                            @Override
                            public IndexingLevel apply(Object input) {
                                return level;
                            }
                        };
                        delayedFeatures.putAll(Maps.asMap(resolvedFeatures, f));
                        delayedDataTypes.putAll(Maps.asMap(resolvedDatatypes, f));
                        delayedClasses.putAll(Maps.asMap(resolvedClasses, f));
                        return null;
                    }
                });
            } catch (InvocationTargetException ex) {
                processingFatal(ex.getCause(), "register en masse the observed EClasses " + resolvedClasses
                        + " and EDatatypes " + resolvedDatatypes + " and EStructuralFeatures " + resolvedFeatures);
            } catch (Exception ex) {
                processingFatal(ex, "register en masse the observed EClasses " + resolvedClasses + " and EDatatypes "
                        + resolvedDatatypes + " and EStructuralFeatures " + resolvedFeatures);
            }
        }
    }

    @Override
    public void unregisterObservedTypes(Set<EClass> classes, Set<EDataType> dataTypes,
            Set<? extends EStructuralFeature> features) {
        unregisterEClasses(classes);
        unregisterEDataTypes(dataTypes);
        unregisterEStructuralFeatures(features);
    }

    @Override
    public void registerEStructuralFeatures(Set<? extends EStructuralFeature> features, final IndexingLevel level) {
        if (isRegistrationNecessary(level) && features != null) {
            final Set<Object> resolved = resolveFeaturesToKey(features);

            try {
                coalesceTraversals(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (Object o : resolved) {
                            delayedFeatures.put(o, level);
                        }
                        return null;
                    }
                });
            } catch (InvocationTargetException ex) {
                processingFatal(ex.getCause(), "register the observed EStructuralFeatures: " + resolved);
            } catch (Exception ex) {
                processingFatal(ex, "register the observed EStructuralFeatures: " + resolved);
            }
        }
    }

    @Override
    public void registerEStructuralFeatures(Set<? extends EStructuralFeature> features) {
        registerEStructuralFeatures(features, IndexingLevel.FULL);
    }

    @Override
    public void unregisterEStructuralFeatures(Set<? extends EStructuralFeature> features) {
        if (isRegistrationNecessary(IndexingLevel.FULL) && features != null) {
            final Set<Object> resolved = resolveFeaturesToKey(features);
            ensureNoListeners(resolved, getFeatureListeners());
            observedFeatures.keySet().removeAll(resolved);
            delayedFeatures.keySet().removeAll(resolved);
            for (Object f : resolved) {
                instanceStore.getValueToFeatureToHolderMap().column(f).clear();
                if (instanceStore.peekFeatureToHolderMap() != null) {
                    instanceStore.peekFeatureToHolderMap().remove(f);
                }
                if (instanceStore.peekHolderToFeatureToValueMap() != null) {
                    instanceStore.peekHolderToFeatureToValueMap().column(f).clear();
                }
                statsStore.removeType(f);
            }
        }
    }

    @Override
    public void registerEClasses(Set<EClass> classes, final IndexingLevel level) {
        if (isRegistrationNecessary(level) && classes != null) {
            final Set<Object> resolvedClasses = resolveClassifiersToKey(classes);

            try {
                coalesceTraversals(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (Object o : resolvedClasses) {
                            delayedClasses.put(o, level);
                        }
                        return null;
                    }
                });
            } catch (InvocationTargetException ex) {
                processingFatal(ex.getCause(), "register the observed EClasses: " + resolvedClasses);
            } catch (Exception ex) {
                processingFatal(ex, "register the observed EClasses: " + resolvedClasses);
            }
        }
    }

    @Override
    public void registerEClasses(Set<EClass> classes) {
        registerEClasses(classes, IndexingLevel.FULL);
    }

    /**
     * @param classes
     */
    protected void startObservingClasses(Map<Object, IndexingLevel> classObservations) {
        for (Entry<Object, IndexingLevel> classObservation : classObservations.entrySet()) {
            putIntoMapIfHigherLevel(directlyObservedClasses, classObservation.getKey(), classObservation.getValue());
            addObservedClassesInternal(classObservation.getKey(), classObservation.getValue());
        }
    }

    @Override
    public void unregisterEClasses(Set<EClass> classes) {
        if (isRegistrationNecessary(IndexingLevel.FULL) && classes != null) {
            final Set<Object> resolved = resolveClassifiersToKey(classes);
            ensureNoListeners(resolved, getInstanceListeners());
            directlyObservedClasses.keySet().removeAll(resolved);
            allObservedClasses = null;
            delayedClasses.keySet().removeAll(resolved);
            for (Object c : resolved) {
                instanceStore.removeInstanceSet(c);
                statsStore.removeType(c);
            }
        }
    }

    @Override
    public void registerEDataTypes(Set<EDataType> dataTypes, final IndexingLevel level) {
        if (isRegistrationNecessary(level) && dataTypes != null) {
            final Set<Object> resolved = resolveClassifiersToKey(dataTypes);

            try {
                coalesceTraversals(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (Object o : resolved) {
                            delayedDataTypes.put(o, level);
                        }
                        return null;
                    }
                });
            } catch (InvocationTargetException ex) {
                processingFatal(ex.getCause(), "register the observed EDataTypes: " + resolved);
            } catch (Exception ex) {
                processingFatal(ex, "register the observed EDataTypes: " + resolved);
            }
        }
    }

    @Override
    public void registerEDataTypes(Set<EDataType> dataTypes) {
        registerEDataTypes(dataTypes, IndexingLevel.FULL);
    }

    @Override
    public void unregisterEDataTypes(Set<EDataType> dataTypes) {
        if (isRegistrationNecessary(IndexingLevel.FULL) && dataTypes != null) {
            final Set<Object> resolved = resolveClassifiersToKey(dataTypes);
            ensureNoListeners(resolved, getDataTypeListeners());
            observedDataTypes.keySet().removeAll(resolved);
            delayedDataTypes.keySet().removeAll(resolved);
            for (Object dataType : resolved) {
                instanceStore.removeDataTypeMap(dataType);
                statsStore.removeType(dataType);
            }
        }
    }

    @Override
    public boolean isCoalescing() {
        return delayTraversals;
    }

    @Override
    public <V> V coalesceTraversals(Callable<V> callable) throws InvocationTargetException {
        V finalResult = null;

        if (delayTraversals) { // reentrant case, no special action needed
            try {
                finalResult = callable.call();
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
            return finalResult;
        }

        boolean firstRun = true;
        while (callable != null) { // repeat if post-processing needed
            delayedClasses = new HashMap<Object, IndexingLevel>();
            delayedFeatures = new HashMap<Object, IndexingLevel>();
            delayedDataTypes = new HashMap<Object, IndexingLevel>();

            try {
                try {
                    delayTraversals = true;

                    V result = callable.call();
                    if (firstRun) {
                        firstRun = false;
                        finalResult = result;
                    }

                    // are there proxies left to be resolved? are we allowed to resolve them now?
                    while ((!delayedProxyResolutions.isEmpty()) && resolutionDelayingResources.isEmpty()) {
                        // pop first entry
                        final Collection<Entry<EObject, EReference>> entries = delayedProxyResolutions.entries();
                        final Entry<EObject, EReference> toResolve = entries.iterator().next();
                        entries.remove(toResolve);

                        // see if we can resolve proxies
                        comprehension.tryResolveReference(toResolve.getKey(), toResolve.getValue());
                    }

                } finally {
                    delayTraversals = false;
                    callable = null;

                    for(Entry<Object, IndexingLevel> entry: observedFeatures.entrySet()){
                        IndexingLevel requested = delayedFeatures.get(entry.getKey());
                        if (requested != null){
                            IndexingLevel old = entry.getValue();
                            IndexingLevel merged = requested.merge(old);
                            if (merged == old){
                                delayedFeatures.remove(entry.getKey());
                            }else{
                                delayedFeatures.put(entry.getKey(), merged);
                            }
                        }
                    }
                    for(Entry<Object, IndexingLevel> entry: directlyObservedClasses.entrySet()){
                        IndexingLevel requested = delayedClasses.get(entry.getKey());
                        if (requested != null){
                            IndexingLevel old = entry.getValue();
                            IndexingLevel merged = requested.merge(old);
                            if (merged == old){
                                delayedClasses.remove(entry.getKey());
                            } else{
                                delayedClasses.put(entry.getKey(), merged);
                            }
                        }
                    }
                    for(Entry<Object, IndexingLevel> entry: observedDataTypes.entrySet()){
                        IndexingLevel requested = delayedDataTypes.get(entry.getKey());
                        if (requested != null){
                            IndexingLevel old = entry.getValue();
                            IndexingLevel merged = requested.merge(old);
                            if (merged == old){
                                delayedDataTypes.remove(entry.getKey());
                            } else {
                                delayedDataTypes.put(entry.getKey(), merged);
                            }
                        }
                    }

                    boolean classesWarrantTraversal = !Maps
                            .difference(delayedClasses, getAllObservedClassesInternal()).areEqual();

                    if (!delayedClasses.isEmpty() || !delayedFeatures.isEmpty() || !delayedDataTypes.isEmpty()) {
                        final HashMap<Object, IndexingLevel> oldClasses = new HashMap<Object, IndexingLevel>(
                                directlyObservedClasses);
                        startObservingClasses(delayedClasses);
                        observedDataTypes.putAll(delayedDataTypes);
                        observedFeatures.putAll(delayedFeatures);

                        // make copies so that original accumulators can be cleaned for the next cycle
                        // or for the rare case that a coalesced traversal is invoked during visitation,
                        // e.g. by a derived feature implementation
                        final Map<Object, IndexingLevel> toGatherClasses = new HashMap<Object, IndexingLevel>(
                                delayedClasses);
                        final Map<Object, IndexingLevel> toGatherFeatures = new HashMap<Object, IndexingLevel>(
                                delayedFeatures);
                        final Map<Object, IndexingLevel> toGatherDataTypes = new HashMap<Object, IndexingLevel>(
                                delayedDataTypes);
                        
                        // Instance indexing would add extra entries to the statistics store, so we have to clean the
                        // appropriate entries. If no re-traversal is required, it is detected earlier; at this point we
                        // only have to consider the target indexing level. See bug
                        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=518356 for more details.
                        
                        // Technically, the statsStore cleanup seems only necessary for EDataTypes; otherwise everything
                        // works as expected, but it seems a better idea to do the cleanup for all types in the same way
                        for (Entry<Object, IndexingLevel> entry : Iterables.concat(toGatherClasses.entrySet(),
                                toGatherFeatures.entrySet(), toGatherDataTypes.entrySet())) {
                            if (entry.getValue().hasInstances()) {
                                statsStore.removeType(entry.getKey());
                            }
                        }

                        if (classesWarrantTraversal || !toGatherFeatures.isEmpty() || !toGatherDataTypes.isEmpty()) {
                            // repeat the cycle with this visit
                            final NavigationHelperVisitor visitor = new NavigationHelperVisitor.TraversingVisitor(this,
                                    toGatherFeatures, toGatherClasses, oldClasses, toGatherDataTypes);

                            callable = new Callable<V>() {
                                @Override
                                public V call() throws Exception {
                                    // temporarily ignoring RESOLVE on these features, as they were not observed before
                                    ignoreResolveNotificationFeatures.addAll(toGatherFeatures.keySet());
                                    try {
                                        traverse(visitor);
                                    } finally {
                                        ignoreResolveNotificationFeatures.removeAll(toGatherFeatures.keySet());
                                    }
                                    return null;
                                }
                            };

                        }
                    }
                }
            } catch (Exception e) {
                notifyFatalListener(
                        "VIATRA Base encountered an error while traversing the EMF model to gather new information. ",
                        e);
                throw new InvocationTargetException(e);
            }
        }
        executeTraversalCallbacks();
        return finalResult;
    }
    
    private void executeTraversalCallbacks() throws InvocationTargetException{
        final Runnable[] callbacks = traversalCallbacks.toArray(new Runnable[traversalCallbacks.size()]);
        traversalCallbacks.clear();
        if (callbacks.length > 0){
            coalesceTraversals(new Callable<Void>() {

                @Override
                public Void call() throws Exception {

                    for(Runnable callback : callbacks){
                        callback.run();
                    }
                    return null;
                }
            });
        }
    }

    private void traverse(final NavigationHelperVisitor visitor) {
        // Cloning model roots avoids a concurrent modification exception
        for (Notifier root : new HashSet<Notifier>(modelRoots)) {
            comprehension.traverseModel(visitor, root);
        }
        notifyBaseIndexChangeListeners();
    }

    @Override
    public void addRoot(Notifier emfRoot) throws ViatraBaseException {
        addRootInternal(emfRoot);
    }

    @Override
    public <T extends EObject> void cheapMoveTo(T element, EList<T> targetContainmentReferenceList) {
        if (element.eAdapters().contains(contentAdapter)
                && targetContainmentReferenceList instanceof NotifyingList<?>) {
            final Object listNotifier = ((NotifyingList<?>) targetContainmentReferenceList).getNotifier();
            if (listNotifier instanceof Notifier && ((Notifier) listNotifier).eAdapters().contains(contentAdapter)) {
                contentAdapter.ignoreInsertionAndDeletion = element;
                try {
                    targetContainmentReferenceList.add(element);
                } finally {
                    contentAdapter.ignoreInsertionAndDeletion = null;
                }
            } else {
                targetContainmentReferenceList.add(element);
            }
        } else {
            targetContainmentReferenceList.add(element);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void cheapMoveTo(EObject element, EObject parent, EReference containmentFeature) {
        metaStore.maintainMetamodel(containmentFeature);
        if (containmentFeature.isMany())
            cheapMoveTo(element, (EList) parent.eGet(containmentFeature));
        else if (element.eAdapters().contains(contentAdapter) && parent.eAdapters().contains(contentAdapter)) {
            contentAdapter.ignoreInsertionAndDeletion = element;
            try {
                parent.eSet(containmentFeature, element);
            } finally {
                contentAdapter.ignoreInsertionAndDeletion = null;
            }
        } else {
            parent.eSet(containmentFeature, element);
        }
    }

    /**
     * @param emfRoot
     * @throws ViatraBaseException
     */
    private void addRootInternal(Notifier emfRoot) throws ViatraBaseException {
        if (!((emfRoot instanceof EObject) || (emfRoot instanceof Resource) || (emfRoot instanceof ResourceSet))) {
            throw new ViatraBaseException(ViatraBaseException.INVALID_EMFROOT);
        }
        expandToAdditionalRoot(emfRoot);
    }

    @Override
    public Set<EClass> getAllCurrentClasses() {
        return instanceStore.getAllCurrentClasses();
    }
    
    private boolean isRegistrationNecessary(IndexingLevel level) {
        boolean inWildcardMode = isInWildcardMode(level);
        if (inWildcardMode && !loggedRegistrationMessage) {
            loggedRegistrationMessage = true;
            logger.warn("Type registration/unregistration not required in wildcard mode. This message will not be repeated for future occurences.");
        }
        return !inWildcardMode;
    }

    private <X, Y> void ensureNoListeners(Set<Object> unobservedTypes,
            final Table<Object, X, Set<Y>> listenerRegistry) {
        if (!Collections.disjoint(unobservedTypes, listenerRegistry.rowKeySet()))
            throw new IllegalStateException("Cannot unregister observed types for which there are active listeners");
    }

    private void ensureNoListenersForDispose() {
        if (!(baseIndexChangeListeners.isEmpty() && subscribedFeatureListeners.isEmpty()
                && subscribedDataTypeListeners.isEmpty() && subscribedInstanceListeners.isEmpty()))
            throw new IllegalStateException("Cannot dispose while there are active listeners");
    }

    /**
     * Resamples the values of not well-behaving derived features if those features are also indexed.
     */
    public void resampleDerivedFeatures() {
        // otherwise notifications are delivered anyway
        if (!baseIndexOptions.isTraverseOnlyWellBehavingDerivedFeatures()) {
            // get all required classes
            Set<EClass> allCurrentClasses = instanceStore.getAllCurrentClasses();
            Set<EStructuralFeature> featuresToSample = Sets.newHashSet();
            // collect features to sample
            for (EClass cls : allCurrentClasses) {
                EList<EStructuralFeature> features = cls.getEAllStructuralFeatures();
                for (EStructuralFeature f : features) {
                    // is feature only sampled?
                    if (comprehension.onlySamplingFeature(f)) {
                        featuresToSample.add(f);
                    }
                }
            }

            final EMFVisitor removalVisitor = contentAdapter.getVisitorForChange(false);
            final EMFVisitor insertionVisitor = contentAdapter.getVisitorForChange(true);

            // iterate on instances
            for (final EStructuralFeature f : featuresToSample) {
                EClass containingClass = f.getEContainingClass();
                processAllInstances(containingClass, new IEClassProcessor() {
                    @Override
                    public void process(EClass type, EObject instance) {
                        resampleFeatureValueForHolder(instance, f, insertionVisitor, removalVisitor);
                    }
                });
            }
            notifyBaseIndexChangeListeners();
        }
    }

    protected void resampleFeatureValueForHolder(EObject source, EStructuralFeature feature,
            EMFVisitor insertionVisitor, EMFVisitor removalVisitor) {
        // traverse features and update value
        Object newValue = source.eGet(feature);
        Set<Object> oldValues = instanceStore.getOldValuesForHolderAndFeature(source, feature);
        if (feature.isMany()) {
            resampleManyFeatureValueForHolder(source, feature, newValue, oldValues, insertionVisitor, removalVisitor);
        } else {
            resampleSingleFeatureValueForHolder(source, feature, newValue, oldValues, insertionVisitor, removalVisitor);
        }

    }

    private void resampleManyFeatureValueForHolder(EObject source, EStructuralFeature feature, Object newValue,
            Set<Object> oldValues, EMFVisitor insertionVisitor, EMFVisitor removalVisitor) {
        InternalEObject internalEObject = (InternalEObject) source;
        Collection<?> newValues = (Collection<?>) newValue;
        // add those that are in new but not in old
        Set<Object> newValueSet = new HashSet<Object>(newValues);
        newValueSet.removeAll(oldValues);
        // remove those that are in old but not in new
        oldValues.removeAll(newValues);
        if (!oldValues.isEmpty()) {
            for (Object ov : oldValues) {
                comprehension.traverseFeature(removalVisitor, source, feature, ov, null);
            }
            ENotificationImpl removeNotification = new ENotificationImpl(internalEObject, Notification.REMOVE_MANY,
                    feature, oldValues, null);
            notifyLightweightObservers(source, feature, removeNotification);
        }
        if (!newValueSet.isEmpty()) {
            for (Object nv : newValueSet) {
                comprehension.traverseFeature(insertionVisitor, source, feature, nv, null);
            }
            ENotificationImpl addNotification = new ENotificationImpl(internalEObject, Notification.ADD_MANY, feature,
                    null, newValueSet);
            notifyLightweightObservers(source, feature, addNotification);
        }
    }

    private void resampleSingleFeatureValueForHolder(EObject source, EStructuralFeature feature, Object newValue,
            Set<Object> oldValues, EMFVisitor insertionVisitor, EMFVisitor removalVisitor) {
        InternalEObject internalEObject = (InternalEObject) source;
        Object oldValue = Iterables.getFirst(oldValues, null);
        if (!Objects.equal(oldValue, newValue)) {
            // value changed
            comprehension.traverseFeature(removalVisitor, source, feature, oldValue, null);
            comprehension.traverseFeature(insertionVisitor, source, feature, newValue, null);
            ENotificationImpl notification = new ENotificationImpl(internalEObject, Notification.SET, feature, oldValue,
                    newValue);
            notifyLightweightObservers(source, feature, notification);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.viatra.query.runtime.base.api.NavigationHelper#countAllInstances(org.eclipse.emf.ecore.EClass)
     */
    @Override
    public int countAllInstances(EClass type) {
        int result = 0;

        Object typeKey = toKey(type);
        Set<Object> subTypes = metaStore.getSubTypeMap().get(typeKey);
        if (subTypes != null) {
            for (Object subTypeKey : subTypes) {
                result += statsStore.countInstances(subTypeKey);
            }
        }
        result += statsStore.countInstances(typeKey);

        return result;
    }

    @Override
    public int countDataTypeInstances(EDataType dataType) {
        return statsStore.countInstances(toKey(dataType));
    }

    @Override
    public int countFeatureTargets(EObject seedSource, EStructuralFeature feature) {
        return instanceStore.getHolderToFeatureToValueMap().get(seedSource, toKey(feature)).size();
    }

    @Override
    public int countFeatures(EStructuralFeature feature) {
        return statsStore.countFeatures(toKey(feature));
    }

    @Override
    public IndexingLevel getIndexingLevel(EClass type) {
        Object key = toKey(type);
        IndexingLevel level = directlyObservedClasses.get(key);
        if (level == null) {
            level = delayedClasses.get(key);
        }
        // Wildcard mode is never null
        return wildcardMode.merge(level);
    }

    @Override
    public IndexingLevel getIndexingLevel(EDataType type) {
        Object key = toKey(type);
        IndexingLevel level = observedDataTypes.get(key);
        if (level == null) {
            level = delayedDataTypes.get(key);
        }
        // Wildcard mode is never null
        return wildcardMode.merge(level);
    }

    @Override
    public IndexingLevel getIndexingLevel(EStructuralFeature feature) {
        Object key = toKey(feature);
        IndexingLevel level = observedFeatures.get(key);
        if (level == null) {
            level = delayedFeatures.get(key);
        }
        // Wildcard mode is never null
        return wildcardMode.merge(level);
    }
    
    @Override
    public void executeAfterTraversal(final Runnable traversalCallback) throws InvocationTargetException {
        coalesceTraversals(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                traversalCallbacks.add(traversalCallback);
                return null;
            }
        });
    }

}
