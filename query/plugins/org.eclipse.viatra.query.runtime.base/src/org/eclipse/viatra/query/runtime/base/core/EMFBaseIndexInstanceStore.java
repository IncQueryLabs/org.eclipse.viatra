/*******************************************************************************
 * Copyright (c) 2010-2016, "Gabor Bergmann", Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   "Gabor Bergmann" - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.base.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.viatra.query.runtime.base.api.ISurrogateObjectService;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.incquerylabs.vhci.collections.helper.BaseIndexCollectionsHelper;

/**
 * Stores the indexed contents of an EMF model (includes instance model information).
 * 
 * @author Gabor Bergmann
 *
 */
public class EMFBaseIndexInstanceStore<Surrogate> extends AbstractBaseIndexStore {

    private ISurrogateObjectService<Surrogate> surrogateObjectService;
    private EMFBaseIndexMetaStore metaStore;
    private EMFBaseIndexSubscriptions<Surrogate> subscriptions;

    public EMFBaseIndexInstanceStore(NavigationHelperImpl<Surrogate> navigationHelper, Logger logger,
            ISurrogateObjectService<Surrogate> surrogateObjectService) {
        super(navigationHelper, logger);
        this.surrogateObjectService = surrogateObjectService;
        this.metaStore = navigationHelper.metaStore;
        this.subscriptions = navigationHelper.subscriptions;
    }

    /**
     * since last run of after-update callbacks
     */
    boolean isDirty = false;

    /**
     * value -> feature (EAttribute or EReference) -> holder(s)
     * <p>
     * holder(s) are stored as
     * <ul>
     * <li>{@link Set} if feature is unique,
     * <li>{@link Multiset} if the feature is non-unique (a single holder is contained multiple times, once for each
     * time it lists the specific value in its feature vale list)
     * <ul>
     * <p>
     * Duplicates of non-unique features are stored in this map only; other index structures consider unique values
     * only.
     */
    private final Table<Object, Object, Collection<Surrogate>> valueToFeatureToHolderMap = HashBasedTable.create();
    private final Table<Object, Object, Collection<Surrogate>> valueToFeatureToHolderMapHC = BaseIndexCollectionsHelper
            .getValueToFeatureToHolderStore();

    /**
     * feature ((String id or EStructuralFeature) -> holder(s) constructed on-demand
     */
    private Map<Object, Multiset<Surrogate>> featureToHolderMap;
    private Map<Object, Multiset<Surrogate>> featureToHolderMapHC = BaseIndexCollectionsHelper
            .getFeatureToHolderStore();

    /**
     * holder -> feature (String id or EStructuralFeature) -> value(s) constructed on-demand
     */
    private Table<Surrogate, Object, Set<Object>> holderToFeatureToValueMap;
    private Table<Surrogate, Object, Set<Object>> holderToFeatureToValueMapHC = BaseIndexCollectionsHelper
            .getHolderToFeatureToValueStore();

    /**
     * key (String id or EClass instance) -> instance(s)
     */
    private final Map<Object, Set<Surrogate>> instanceMap = new HashMap<Object, Set<Surrogate>>();
    private final Map<Object, Set<Surrogate>> instanceMapHC = BaseIndexCollectionsHelper.getInstanceStore();

    /**
     * key (String id or EDataType instance) -> multiset of value(s)
     */
    private final Map<Object, Map<Object, Integer>> dataTypeMap = new HashMap<Object, Map<Object, Integer>>();
    private final Map<Object, Map<Object, Integer>> dataTypeMapHC = BaseIndexCollectionsHelper.getDataTypeStore();

    public void loadToCluster() {
        
        // Local index collection initialization
        if (featureToHolderMap == null) {
            featureToHolderMap = new HashMap<Object, Multiset<Surrogate>>();
            initReversedFeatureMap();
        }

        if (holderToFeatureToValueMap == null) {
            holderToFeatureToValueMap = HashBasedTable.create();
            initDirectFeatureMap();
        }
        
        // Move collections' content to cluster side
        for (Cell<Object, Object, Collection<Surrogate>> c : valueToFeatureToHolderMap.cellSet())
            valueToFeatureToHolderMapHC.put(c.getRowKey(), c.getColumnKey(), c.getValue());
        valueToFeatureToHolderMap.clear();

        for (Entry<Object, Set<Surrogate>> e : instanceMap.entrySet())
            instanceMapHC.put(e.getKey(), e.getValue());
        instanceMap.clear();

        for (Entry<Object, Map<Object, Integer>> entry : dataTypeMap.entrySet())
            dataTypeMapHC.put(entry.getKey(), entry.getValue());
        dataTypeMap.clear();

        for (Entry<Object, Multiset<Surrogate>> e : featureToHolderMap.entrySet())
            featureToHolderMapHC.put(e.getKey(), e.getValue());
        featureToHolderMap.clear();

        for (Cell<Surrogate, Object, Set<Object>> c : holderToFeatureToValueMap.cellSet())
            holderToFeatureToValueMapHC.put(c.getRowKey(), c.getColumnKey(), c.getValue());
        holderToFeatureToValueMap.clear();
        
    }

    /**
     * @return the valueToFeatureToHolderMap
     */
    protected Table<Object, Object, Collection<Surrogate>> getValueToFeatureToHolderMap() {
        loadToCluster();
        return valueToFeatureToHolderMapHC;
    }

    /**
     * This method uses the original {@link EStructuralFeature} instance or the String id.
     * 
     * @return true if this was the first time the value was added to this feature of this holder (false is only
     *         possible for non-unique features)
     */
    private boolean addToFeatureMap(final Object featureKey, boolean unique, final Object value,
            final Surrogate holderSurrogate) {
        Collection<Surrogate> setVal = valueToFeatureToHolderMap.get(value, featureKey);

        if (setVal == null) {
            setVal = unique ? new HashSet<Surrogate>() : HashMultiset.<Surrogate> create();
            valueToFeatureToHolderMap.put(value, featureKey, setVal);
        }
        boolean changed = unique || !setVal.contains(holderSurrogate);
        boolean success = setVal.add(holderSurrogate);
        if (unique && !success) {
            String msg = String.format(
                    "Error adding feature %s to the index with type %s. This indicates some errors in underlying model representation.",
                    value, featureKey);
            logNotificationHandlingError(msg);
        }
        return changed;
    }

    /**
     * This method uses either the original {@link EStructuralFeature} instance or the String id.
     */
    private void addToReversedFeatureMap(final Object feature, final Surrogate holderSurrogate) {
        Multiset<Surrogate> setVal = featureToHolderMap.get(feature);

        if (setVal == null) {
            setVal = HashMultiset.create();
            featureToHolderMap.put(feature, setVal);
        }
        setVal.add(holderSurrogate);
    }

    /**
     * This method uses either the original {@link EStructuralFeature} instance or the String id.
     */
    private void addToDirectFeatureMap(final Surrogate holderSurrogate, final Object feature, final Object value) {
        Set<Object> setVal = holderToFeatureToValueMap.get(holderSurrogate, feature);

        if (setVal == null) {
            setVal = new HashSet<Object>();
            holderToFeatureToValueMap.put(holderSurrogate, feature, setVal);
        }
        setVal.add(value);
    }

    /**
     * This method uses either the original {@link EStructuralFeature} instance or the String id.
     */
    private void removeFromReversedFeatureMap(final Object feature, final Surrogate holderSurrogate) {
        final Multiset<Surrogate> setVal = featureToHolderMap.get(feature);
        if (setVal != null) {
            setVal.remove(holderSurrogate);

            if (setVal.isEmpty()) {
                featureToHolderMap.remove(feature);
            }
        }
    }

    /**
     * This method uses the original {@link EStructuralFeature} instance.
     */
    private boolean removeFromFeatureMap(final Object featureKey, boolean unique, final Object value,
            final Surrogate holderSurrogate) {
        final Collection<Surrogate> setHolder = valueToFeatureToHolderMap.get(value, featureKey);
        if (setHolder != null) {
            boolean removed = setHolder.remove(holderSurrogate);

            if (setHolder.isEmpty()) {
                valueToFeatureToHolderMap.remove(value, featureKey);
            }
            if (!removed) {
                String msg = String.format(
                        "Notification received to remove value %s from feature %s of object %s, but feature value is missing from the index. This indicates some errors in underlying model representation.",
                        value, featureKey, holderSurrogate);
                logNotificationHandlingError(msg);
            }
            return removed && (unique || (!setHolder.contains(holderSurrogate)));
        }
        return false;
    }

    /**
     * This method uses either the original {@link EStructuralFeature} instance or the String id.
     */
    private void removeFromDirectFeatureMap(final Surrogate holderSurrogate, final Object feature, final Object value) {
        final Set<Object> setVal = holderToFeatureToValueMap.get(holderSurrogate, feature);
        if (setVal != null) {
            setVal.remove(value);

            if (setVal.isEmpty()) {
                holderToFeatureToValueMap.remove(holderSurrogate, feature);
            }
        }
    }

    public void insertReferenceTuple(final Object featureKey, boolean unique, final EObject value,
            final EObject holder) {
        Surrogate valueSurrogate = surrogateObjectService.getAndPinSurrogate(value);
        Surrogate holderSurrogate = surrogateObjectService.getAndPinSurrogate(holder);
        boolean changed = insertFeatureTuple(featureKey, unique, valueSurrogate, holderSurrogate);
        if (changed) {
            subscriptions.notifyFeatureListeners(holder, featureKey, value, true);
            subscriptions.notifyFeatureSurrogateListeners(holderSurrogate, featureKey, valueSurrogate, true);
        }
    }

    /**
     * @param value
     *            external representation
     * @return internal representation of value
     */
    public Object insertAttributeTuple(final Object featureKey, boolean unique, final Object value,
            final EObject holder) {
        Object internalValueRepresentation = metaStore.toInternalValueRepresentation(value);
        Surrogate holderSurrogate = surrogateObjectService.getAndPinSurrogate(holder);
        boolean changed = insertFeatureTuple(featureKey, unique, internalValueRepresentation, holderSurrogate);
        if (changed) {
            subscriptions.notifyFeatureListeners(holder, featureKey, value, true);
            subscriptions.notifyFeatureSurrogateListeners(holderSurrogate, featureKey, value, true);
        }
        return internalValueRepresentation;
    }

    public void removeReferenceTuple(final Object featureKey, boolean unique, final EObject value,
            final EObject holder) {
        Surrogate valueSurrogate = surrogateObjectService.getAndUnpinExistingSurrogate(value);
        Surrogate holderSurrogate = surrogateObjectService.getAndUnpinExistingSurrogate(holder);
        boolean changed = removeFeatureTuple(featureKey, unique, valueSurrogate, holderSurrogate);
        if (changed) {
            subscriptions.notifyFeatureListeners(holder, featureKey, value, false);
            subscriptions.notifyFeatureSurrogateListeners(holderSurrogate, featureKey, valueSurrogate, false);
        }
    }

    /**
     * @param value
     *            external representation
     * @return internal representation of value
     */
    public Object removeAttributeTuple(final Object featureKey, boolean unique, final Object value,
            final EObject holder) {
        Object internalValueRepresentation = metaStore.toInternalValueRepresentation(value);
        Surrogate holderSurrogate = surrogateObjectService.getAndUnpinExistingSurrogate(holder);
        boolean changed = removeFeatureTuple(featureKey, unique, internalValueRepresentation, holderSurrogate);
        if (changed) {
            subscriptions.notifyFeatureListeners(holder, featureKey, value, false);
            subscriptions.notifyFeatureSurrogateListeners(holderSurrogate, featureKey, value, false);
        }
        return internalValueRepresentation;
    }

    private boolean insertFeatureTuple(final Object featureKey, boolean unique, final Object value,
            final Surrogate holderSurrogate) {
        boolean changed = addToFeatureMap(featureKey, unique, value, holderSurrogate);
        if (changed) { // if not duplicated
            if (featureToHolderMap != null) {
                addToReversedFeatureMap(featureKey, holderSurrogate);
            }
            if (holderToFeatureToValueMap != null) {
                addToDirectFeatureMap(holderSurrogate, featureKey, value);
            }

            isDirty = true;
        }
        return changed;
    }

    private boolean removeFeatureTuple(final Object featureKey, boolean unique, final Object value,
            final Surrogate holderSurrogate) {
        boolean changed = removeFromFeatureMap(featureKey, unique, value, holderSurrogate);
        if (changed) { // if not duplicated
            if (featureToHolderMap != null) {
                removeFromReversedFeatureMap(featureKey, holderSurrogate);
            }
            if (holderToFeatureToValueMap != null) {
                removeFromDirectFeatureMap(holderSurrogate, featureKey, value);
            }

            isDirty = true;
        }
        return changed;
    }

    // START ********* InstanceSet *********
    public Set<EObject> getInstanceSet(final Object keyClass) {
        loadToCluster();
        return surrogateObjectService.fromExistingSurrogateSet(instanceMapHC.get(keyClass));
    }

    public Set<Surrogate> getSurrogateInstanceSet(final Object keyClass) {
        loadToCluster();
        return instanceMapHC.get(keyClass);
    }

    public void removeInstanceSet(final Object keyClass) {
        instanceMap.remove(keyClass);
    }

    public void insertIntoInstanceSet(final Object keyClass, final EObject value) {
        Surrogate valueSurrogate = surrogateObjectService.getAndPinSurrogate(value);
        Set<Surrogate> set = instanceMap.get(keyClass);
        if (set == null) {
            set = new HashSet<Surrogate>();
            instanceMap.put(keyClass, set);
        }

        if (!set.add(valueSurrogate)) {
            String msg = String.format(
                    "Notification received to index %s as a %s, but it already exists in the index. This indicates some errors in underlying model representation.",
                    value, keyClass);
            logNotificationHandlingError(msg);
        }

        isDirty = true;
        subscriptions.notifyInstanceSurrogateListeners(keyClass, valueSurrogate, true);
    }

    public void removeFromInstanceSet(final Object keyClass, final EObject value) {
        Surrogate valueSurrogate = surrogateObjectService.getAndUnpinExistingSurrogate(value);
        final Set<Surrogate> set = instanceMap.get(keyClass);
        if (set != null) {
            if (!set.remove(valueSurrogate)) {
                String msg = String.format(
                        "Notification received to remove %s as a %s, but it is missing from the index. This indicates some errors in underlying model representation.",
                        value, keyClass);
                logNotificationHandlingError(msg);
            }

            if (set.isEmpty()) {
                instanceMap.remove(keyClass);
            }
        }

        isDirty = true;
        subscriptions.notifyInstanceSurrogateListeners(keyClass, valueSurrogate, false);
    }

    // END ********* InstanceSet *********

    // START ********* DataTypeMap *********
    public Map<Object, Integer> getDataTypeMap(final Object keyType) {
        loadToCluster();
        return dataTypeMapHC.get(keyType);
    }

    public void removeDataTypeMap(final Object keyType) {
        dataTypeMap.remove(keyType);
    }

    /**
     * @param value
     *            internal representation of value
     */
    public void insertIntoDataTypeMap(final Object keyType, final Object value) {
        Map<Object, Integer> valMap = dataTypeMap.get(keyType);
        if (valMap == null) {
            valMap = new HashMap<Object, Integer>();
            dataTypeMap.put(keyType, valMap);
        }
        final boolean firstOccurrence = (valMap.get(value) == null);
        if (firstOccurrence) {
            valMap.put(value, 1);
        } else {
            Integer count = valMap.get(value);
            valMap.put(value, ++count);
        }

        isDirty = true;
        subscriptions.notifyDataTypeListeners(keyType, value, true, firstOccurrence);
    }

    /**
     * @param value
     *            internal representation of value
     */
    public void removeFromDataTypeMap(final Object keyType, final Object value) {
        final Map<Object, Integer> valMap = dataTypeMap.get(keyType);
        if (valMap != null && valMap.get(value) != null) {
            Integer count = valMap.get(value);
            final boolean lastOccurrence = (--count == 0);
            if (lastOccurrence) {
                valMap.remove(value);
                if (valMap.size() == 0) {
                    dataTypeMap.remove(keyType);
                }
            } else {
                valMap.put(value, count);
            }

            isDirty = true;
            subscriptions.notifyDataTypeListeners(keyType, value, false, lastOccurrence);
        }
        // else: inconsistent deletion? log error?
    }

    // END ********* DataTypeMap *********

    /**
     * Decodes the collection of holders (potentially non-unique) to a unique set
     */
    protected Set<EObject> holderCollectionToUniqueSet(Collection<Surrogate> holders) {
        return surrogateObjectService.fromExistingSurrogateSet(holderCollectionToUniqueSurrogateSet(holders));
    }

    /**
     * Decodes the collection of holders (potentially non-unique) to a unique set of surrogates
     */
    protected Set<Surrogate> holderCollectionToUniqueSurrogateSet(Collection<Surrogate> holders) {
        if (holders instanceof Set<?>) {
            return (Set<Surrogate>) holders;
        } else if (holders instanceof Multiset<?>) {
            Multiset<Surrogate> multiSet = (Multiset<Surrogate>) holders;
            return multiSet.elementSet();
        } else
            throw new IllegalStateException("Neither Set nor Multiset: " + holders);
    }

    /**
     * @return the featureToHolderMap
     */
    protected Map<Object, Multiset<Surrogate>> getFeatureToHolderMap() {
        loadToCluster();
        return featureToHolderMapHC;
    }

    protected Map<Object, Multiset<Surrogate>> peekFeatureToHolderMap() {
        loadToCluster();
        return featureToHolderMapHC;
    }

    /**
     * Calling this method will construct the map for all holders and features, consuming significant memory!
     * 
     * @return the holderToFeatureToValeMap
     */
    protected Table<Surrogate, Object, Set<Object>> getHolderToFeatureToValueMap() {
        loadToCluster();
        return holderToFeatureToValueMapHC;
    }

    protected Table<Surrogate, Object, Set<Object>> peekHolderToFeatureToValueMap() {
        loadToCluster();
        return holderToFeatureToValueMapHC;
    }

    private void initReversedFeatureMap() {
        for (final Cell<Object, Object, Collection<Surrogate>> entry : valueToFeatureToHolderMap.cellSet()) {
            final Object feature = entry.getColumnKey();
            for (final Surrogate holder : holderCollectionToUniqueSurrogateSet(entry.getValue())) {
                addToReversedFeatureMap(feature, holder);
            }
        }
    }

    private void initDirectFeatureMap() {
        for (final Cell<Object, Object, Collection<Surrogate>> entry : valueToFeatureToHolderMap.cellSet()) {
            final Object value = entry.getRowKey();
            final Object feature = entry.getColumnKey();
            for (final Surrogate holder : holderCollectionToUniqueSurrogateSet(entry.getValue())) {
                addToDirectFeatureMap(holder, feature, value);
            }
        }
    }

    /**
     * Returns all EClasses that currently have direct instances cached by the index.
     * <p>
     * Supertypes will not be returned, unless they have direct instances in the model as well. If not in <em>wildcard
     * mode</em>, only registered EClasses and their subtypes will be returned.
     * <p>
     * Note for advanced users: if a type is represented by multiple EClass objects, one of them is chosen as
     * representative and returned.
     */
    public Set<EClass> getAllCurrentClasses() {
        final Set<EClass> result = Sets.newHashSet();
        final Set<Object> classifierKeys = instanceMap.keySet();
        for (final Object classifierKey : classifierKeys) {
            final EClassifier knownClassifier = metaStore.getKnownClassifierForKey(classifierKey);
            if (knownClassifier instanceof EClass) {
                result.add((EClass) knownClassifier);
            }
        }
        return result;
    }

    Set<Object> getOldValuesForHolderAndFeature(Surrogate source, EStructuralFeature feature) {
        // while this is slower than using the holderToFeatureToValueMap, we do not want to construct that to avoid
        // memory overhead
        Map<Object, Collection<Surrogate>> oldValuesToHolders = valueToFeatureToHolderMap.column(feature);
        Set<Object> oldValues = new HashSet<Object>();
        for (Entry<Object, Collection<Surrogate>> entry : oldValuesToHolders.entrySet()) {
            if (entry.getValue().contains(source)) {
                oldValues.add(entry.getKey());
            }
        }
        return oldValues;
    }

}
