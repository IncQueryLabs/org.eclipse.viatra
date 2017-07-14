/*******************************************************************************
 * Copyright (c) 2010-2012, Tamas Szabo, Gabor Bergmann, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Szabo, Gabor Bergmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.base.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.query.runtime.base.api.IndexingLevel;
import org.eclipse.viatra.query.runtime.base.comprehension.EMFModelComprehension;
import org.eclipse.viatra.query.runtime.base.comprehension.EMFVisitor;

import com.google.common.collect.ImmutableSet;

public abstract class NavigationHelperVisitor<Surrogate> extends EMFVisitor {

    /**
     * A visitor for processing a single change event. Does not traverse the model. Uses all the observed types.
     */
    public static class ChangeVisitor<Surrogate> extends NavigationHelperVisitor<Surrogate> {
        // local copies to save actual state, in case visitor has to be saved for later due unresolvable proxies
        private final IndexingLevel wildcardMode;
        private final Map<Object, IndexingLevel> allObservedClasses;
        private final Map<Object, IndexingLevel> observedDataTypes;
        private final Map<Object, IndexingLevel> observedFeatures;
        private final Map<Object, Boolean> sampledClasses;

        public ChangeVisitor(NavigationHelperImpl<Surrogate> navigationHelper, boolean isInsertion) {
            super(navigationHelper, isInsertion, false);
            wildcardMode = navigationHelper.getWildcardLevel();
            allObservedClasses = navigationHelper.getAllObservedClassesInternal(); // new HashSet<EClass>();
            observedDataTypes = navigationHelper.getObservedDataTypesInternal(); // new HashSet<EDataType>();
            observedFeatures = navigationHelper.getObservedFeaturesInternal(); // new HashSet<EStructuralFeature>();
            sampledClasses = new HashMap<Object, Boolean>();
        }

        @Override
        protected boolean observesClass(Object eClass) {
            return wildcardMode.hasInstances() || (IndexingLevel.FULL == allObservedClasses.get(eClass)) || registerSampledClass(eClass);
        }

        private boolean registerSampledClass(Object eClass) {
            Boolean classAlreadyChecked = sampledClasses.get(eClass);
            if (classAlreadyChecked != null) {
                return classAlreadyChecked;
            }
            boolean isSampledClass = isSampledClass(eClass);
            sampledClasses.put(eClass, isSampledClass);
            // do not modify observation configuration during traversal
            return false;
        }

        @Override
        protected boolean observesDataType(Object type) {
            return wildcardMode.hasInstances() || (IndexingLevel.FULL == observedDataTypes.get(type));
        }

        @Override
        protected boolean observesFeature(Object feature) {
            return wildcardMode.hasInstances() || (IndexingLevel.FULL == observedFeatures.get(feature));
        }

        @Override
        protected boolean countsFeature(Object feature) {
            return wildcardMode.hasStatistics() || observedFeatures.containsKey(feature) && observedFeatures.get(feature).hasStatistics();
        }

        @Override
        protected boolean countsDataType(Object type) {
            return wildcardMode.hasStatistics() || observedDataTypes.containsKey(type) && observedDataTypes.get(type).hasStatistics();
        }

        @Override
        protected boolean countsClass(Object eClass) {
            return wildcardMode.hasStatistics() || allObservedClasses.containsKey(eClass) && allObservedClasses.get(eClass).hasStatistics();
        }
    }

    /**
     * A visitor for a single-pass traversal of the whole model, processing only the given types and inserting them.
     */
    public static class TraversingVisitor<Surrogate> extends NavigationHelperVisitor<Surrogate> {
        private final IndexingLevel wildcardMode;
        Map<Object, IndexingLevel> features;
        Map<Object, IndexingLevel> newClasses;
        Map<Object, IndexingLevel> oldClasses; // if decends from an old class, no need to add!
        Map<Object, IndexingLevel> classObservationMap; // true for a class even if only a supertype is included in classes;
        Map<Object, IndexingLevel> dataTypes;

        public TraversingVisitor(NavigationHelperImpl<Surrogate> navigationHelper, Map<Object, IndexingLevel> features, Map<Object, IndexingLevel> newClasses,
                Map<Object, IndexingLevel> oldClasses, Map<Object, IndexingLevel> dataTypes) {
            super(navigationHelper, true, true);
            wildcardMode = navigationHelper.getWildcardLevel();
            this.features = features;
            this.newClasses = newClasses;
            this.oldClasses = oldClasses;
            this.classObservationMap = new HashMap<Object, IndexingLevel>();
            this.dataTypes = dataTypes;
        }

        private IndexingLevel getExistingIndexingLevel(Object eClass){
            IndexingLevel result = IndexingLevel.NONE;
            result = result.merge(oldClasses.get(eClass));
            result = result.merge(oldClasses.get(metaStore.getEObjectClassKey()));
            if (IndexingLevel.FULL == result) return result;
            Set<Object> superTypes = metaStore.getSuperTypeMap().get(eClass);
            if (superTypes != null){
                for(Object superType: superTypes){
                    result = result.merge(oldClasses.get(superType));
                    if (IndexingLevel.FULL == result) return result;
                }
            }
            return result;
        }
        
        private IndexingLevel getRequestedIndexingLevel(Object eClass){
            IndexingLevel result = IndexingLevel.NONE;
            result = result.merge(newClasses.get(eClass));
            result = result.merge(newClasses.get(metaStore.getEObjectClassKey()));
            if (IndexingLevel.FULL == result) return result;
            Set<Object> superTypes = metaStore.getSuperTypeMap().get(eClass);
            if (superTypes != null){
                for(Object superType: superTypes){
                    result = result.merge(newClasses.get(superType));
                    if (IndexingLevel.FULL == result) return result;
                }
            }
            return result;
        }
        
        private IndexingLevel getTraversalIndexing(Object eClass){
            IndexingLevel level = classObservationMap.get(eClass);
            if (level == null){
                IndexingLevel existing = getExistingIndexingLevel(eClass);
                IndexingLevel requested = getRequestedIndexingLevel(eClass);
                
                // Calculate the type of indexing which needs to be executed to reach requested indexing state
                // Considering indexes which are already available
                if (existing == requested || existing == IndexingLevel.FULL) return IndexingLevel.NONE;
                if (requested == IndexingLevel.FULL) return IndexingLevel.FULL;
                if (requested.hasStatistics() == existing.hasStatistics()) return IndexingLevel.NONE;
                if (requested.hasStatistics()) return IndexingLevel.STATISTICS;
                return IndexingLevel.NONE;
            }
            return level;
        }
        
        @Override
        protected boolean observesClass(Object eClass) {
            if (wildcardMode.hasInstances()) {
                return true;
            }
            return IndexingLevel.FULL == getTraversalIndexing(eClass);
        }
        
        @Override
        protected boolean countsClass(Object eClass) {
            return wildcardMode.hasStatistics() || getTraversalIndexing(eClass).hasStatistics();
        }

        @Override
        protected boolean observesDataType(Object type) {
            return wildcardMode.hasInstances() || (IndexingLevel.FULL == dataTypes.get(type));
        }

        @Override
        protected boolean observesFeature(Object feature) {
            return wildcardMode.hasInstances() || (IndexingLevel.FULL == features.get(feature));
        }
        
        @Override
        protected boolean countsDataType(Object type) {
            return wildcardMode.hasStatistics() || dataTypes.containsKey(type) && dataTypes.get(type).hasStatistics();
        }
        
        @Override
        protected boolean countsFeature(Object feature) {
            return wildcardMode.hasStatistics() || features.containsKey(feature) && features.get(feature).hasStatistics();
        }

        @Override
        public boolean avoidTransientContainmentLink(EObject source, EReference reference, EObject targetObject) {
            return !targetObject.eAdapters().contains(navigationHelper.contentAdapter);
        }
    }

    protected NavigationHelperImpl<Surrogate> navigationHelper;
    boolean isInsertion;
    boolean descendHierarchy;
    boolean traverseOnlyWellBehavingDerivedFeatures;
    EMFBaseIndexInstanceStore<Surrogate> instanceStore;
    EMFBaseIndexStatisticsStore statsStore;
    EMFBaseIndexMetaStore<Surrogate> metaStore;

    NavigationHelperVisitor(NavigationHelperImpl<Surrogate> navigationHelper, boolean isInsertion, boolean descendHierarchy) {
        super(isInsertion /* preOrder iff insertion */);
        this.navigationHelper = navigationHelper;
        instanceStore = navigationHelper.instanceStore;
        metaStore = navigationHelper.metaStore;
        statsStore = navigationHelper.statsStore;
        this.isInsertion = isInsertion;
        this.descendHierarchy = descendHierarchy;
        this.traverseOnlyWellBehavingDerivedFeatures = navigationHelper.getBaseIndexOptions()
                .isTraverseOnlyWellBehavingDerivedFeatures();
    }

    @Override
    public boolean pruneSubtrees(EObject source) {
        return !descendHierarchy;
    }

    @Override
    public boolean pruneSubtrees(Resource source) {
        return !descendHierarchy;
    }

    @Override
    public boolean pruneFeature(EStructuralFeature feature) {
        Object featureKey = toKey(feature);
        if (observesFeature(featureKey) || countsFeature(featureKey)) {
            return false;
        }
        if (feature instanceof EAttribute){
            Object dataTypeKey = toKey(((EAttribute) feature).getEAttributeType());
            if (observesDataType(dataTypeKey) || countsDataType(dataTypeKey)) {
                return false;
            }
        }
        return !(isInsertion && navigationHelper.isExpansionAllowed() && feature instanceof EReference
                && !((EReference) feature).isContainment());
    }

    /**
     * @param feature
     *            key of feature (EStructuralFeature or String id)
     */
    protected abstract boolean observesFeature(Object feature);

    /**
     * @param feature
     *            key of data type (EDatatype or String id)
     */
    protected abstract boolean observesDataType(Object type);

    /**
     * @param feature
     *            key of class (EClass or String id)
     */
    protected abstract boolean observesClass(Object eClass);

    protected abstract boolean countsFeature(Object feature);
    
    protected abstract boolean countsDataType(Object type);
    
    protected abstract boolean countsClass(Object eClass);
    
    @Override
    public void visitElement(EObject source) {
        EClass eClass = source.eClass();
        if (eClass.eIsProxy()) {
            eClass = (EClass) EcoreUtil.resolve(eClass, source);
        }

        final Object classKey = toKey(eClass);
        if (observesClass(classKey)) {
            if (isInsertion) {
                instanceStore.insertIntoInstanceSet(classKey, source);
            } else {
                instanceStore.removeFromInstanceSet(classKey, source);
            }
        }
        if (countsClass(classKey)){
            if (isInsertion){
                statsStore.addInstance(classKey);
            } else {
                statsStore.removeInstance(classKey);
            }
        }
    }

    @Override
    public void visitAttribute(EObject source, EAttribute feature, Object target) {
        Object featureKey = toKey(feature);
        final Object eAttributeType = toKey(feature.getEAttributeType());
        Object internalValueRepresentation = null;
        if (observesFeature(featureKey)) {
            // if (internalValueRepresentation == null) // always true
            boolean unique = feature.isUnique();
            if (isInsertion) {
                internalValueRepresentation = 
                        instanceStore.insertAttributeTuple(featureKey, unique, target, source);
            } else {
                internalValueRepresentation = 
                        instanceStore.removeAttributeTuple(featureKey, unique, target, source);
            }
        }
        if (countsFeature(featureKey)){
            if (isInsertion) {
                statsStore.addFeature(source, featureKey);
            }else{
                statsStore.removeFeature(source, featureKey);
            }
        }
        if (observesDataType(eAttributeType)) {
            if (internalValueRepresentation == null)
                internalValueRepresentation = metaStore.toInternalValueRepresentation(target);
            if (isInsertion) {
                instanceStore.insertIntoDataTypeMap(eAttributeType, internalValueRepresentation);
            } else {
                instanceStore.removeFromDataTypeMap(eAttributeType, internalValueRepresentation);
            }
        }
        if (countsDataType(eAttributeType)){
            if (isInsertion){
                statsStore.addInstance(eAttributeType);
            } else {
                statsStore.removeInstance(eAttributeType);
            }
        }
    }

    @Override
    public void visitInternalContainment(EObject source, EReference feature, EObject target) {
        visitReference(source, feature, target);
    }

    @Override
    public void visitNonContainmentReference(EObject source, EReference feature, EObject target) {
        visitReference(source, feature, target);
        if (isInsertion) {
            navigationHelper.considerForExpansion(target);
        }
    }

    private void visitReference(EObject source, EReference feature, EObject target) {
        Object featureKey = toKey(feature);
        if (observesFeature(featureKey)) {
            boolean unique = feature.isUnique();
            if (isInsertion) {
                instanceStore.insertReferenceTuple(featureKey, unique, target, source);
            } else {
                instanceStore.removeReferenceTuple(featureKey, unique, target, source);
            }
        }
        if (countsFeature(featureKey)){
            if (isInsertion){
                statsStore.addFeature(source, featureKey);
            } else {
                statsStore.removeFeature(source, featureKey);
            }
        }
    }
    
    @Override
    // do not attempt to resolve proxies referenced from resources that are still being loaded
    public boolean attemptProxyResolutions(EObject source, EReference feature) {
        // emptyness is checked first to avoid costly resource lookup in most cases
        if (navigationHelper.resolutionDelayingResources.isEmpty())
            return true;
        else 
            return ! navigationHelper.resolutionDelayingResources.contains(source.eResource());
    }

    @Override
    public void visitProxyReference(EObject source, EReference reference, EObject targetObject, Integer position) {
        if (isInsertion) { // only attempt to resolve proxies if they are inserted
            // final Object result = source.eGet(reference, true);
            // if (reference.isMany()) {
            // // no idea which element to get, have to iterate through
            // for (EObject touch : (Iterable<EObject>) result);
            // }
            if (navigationHelper.isFeatureResolveIgnored(reference))
                return; // skip resolution; would be ignored anyways
            if (position != null && reference.isMany() && attemptProxyResolutions(source, reference)) {
                // there is added value in doing the resolution now, when we know the position
                // this may save an iteration through the EList if successful
                @SuppressWarnings("unchecked")
                EObject touch = ((java.util.List<EObject>) source.eGet(reference, true)).get(position);
                // if resolution successful, no further action needed
                if (!touch.eIsProxy())
                    return;
            }
            // otherwise, attempt resolution later, at the end of the coalesced traversal block
            navigationHelper.delayedProxyResolutions.put(source, reference);
        }
    }

    protected Object toKey(EStructuralFeature feature) {
        return metaStore.toKey(feature);
    }

    protected Object toKey(EClassifier eClassifier) {
        return metaStore.toKey(eClassifier);
    }

    /**
     * Decides whether the type must be observed in order to allow re-sampling of any of its features. If not
     * well-behaving features are traversed and there is such a feature for this class, the class will be registered
     * into the navigation helper, which may cause a re-traversal.
     * 
     */
    protected boolean isSampledClass(Object eClass) {
        if (!traverseOnlyWellBehavingDerivedFeatures) {
            // TODO we could save this reverse lookup if the calling method would have the EClass, not just the key
            EClass knownClass = (EClass) metaStore.getKnownClassifierForKey(eClass);
            // check features that are traversed, and whether there is any that must be sampled
            for (EStructuralFeature feature : knownClass.getEAllStructuralFeatures()) {
                EMFModelComprehension comprehension = navigationHelper.getComprehension();
                if (comprehension.untraversableDirectly(feature))
                    continue;
                final boolean visitorPrunes = pruneFeature(feature);
                if (visitorPrunes)
                    continue;
                // we found a feature to be visited
                if (comprehension.onlySamplingFeature(feature)) {
                    // we found a feature that must be sampled
                    navigationHelper.registerEClasses(ImmutableSet.of(feature.getEContainingClass()));
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean descendAlongCrossResourceContainments() {
        return this.navigationHelper.traversalDescendsAlongCrossResourceContainment();
    }
}
