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
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.viatra.query.runtime.base.api.BaseIndexOptions;
import org.eclipse.viatra.query.runtime.base.api.InstanceSurrogateListener;
import org.eclipse.viatra.query.runtime.base.exception.ViatraBaseException;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * Stores the indexed metamodel information.
 * 
 * @author Gabor Bergmann
 */
public class EMFBaseIndexMetaStore<Surrogate> {
    
    private static final EClass EOBJECT_CLASS = EcorePackage.eINSTANCE.getEObject();
    private final boolean isDynamicModel;
    private NavigationHelperImpl<Surrogate> navigationHelper;
    
    /**
     * 
     */
    public EMFBaseIndexMetaStore(final NavigationHelperImpl<Surrogate> navigationHelper) {
        this.navigationHelper = navigationHelper;
        final BaseIndexOptions options = navigationHelper.getBaseIndexOptions();
        this.isDynamicModel = options.isDynamicEMFMode();
    }

    /**
     * Supports collision detection and EEnum canonicalization. Used for all EPackages that have types whose instances
     * were encountered at least once.
     */
    private final Set<EPackage> knownPackages = new HashSet<EPackage>();

    /**
     * Field variable because it is needed for collision detection. Used for all EClasses whose instances were
     * encountered at least once.
     */
    private final Set<EClassifier> knownClassifiers = new HashSet<EClassifier>();
    /**
     * Field variable because it is needed for collision detection. Used for all EStructuralFeatures whose instances
     * were encountered at least once.
     */
    private final Set<EStructuralFeature> knownFeatures = new HashSet<EStructuralFeature>();

    /**
     * (EClass or String ID) -> all subtypes in knownClasses
     */
    private final Map<Object, Set<Object>> subTypeMap = new HashMap<Object, Set<Object>>();
    /**
     * (EClass or String ID) -> all supertypes in knownClasses
     */
    private final Map<Object, Set<Object>> superTypeMap = new HashMap<Object, Set<Object>>();

    /**
     * EPacakge NsURI -> EPackage instances; this is instance-level to detect collisions
     */
    private final Multimap<String, EPackage> uniqueIDToPackage = HashMultimap.create();

    /**
     * static maps between metamodel elements and their unique IDs
     */
    private final Map<EClassifier, String> uniqueIDFromClassifier = new HashMap<EClassifier, String>();
    private final Map<ETypedElement, String> uniqueIDFromTypedElement = new HashMap<ETypedElement, String>();
    private final Map<Enumerator, String> uniqueIDFromEnumerator = new HashMap<Enumerator, String>();
    private final Multimap<String, EClassifier> uniqueIDToClassifier = HashMultimap.create(100, 1);
    private final Multimap<String, ETypedElement> uniqueIDToTypedElement = HashMultimap.create(100, 1);
    private final Multimap<String, Enumerator> uniqueIDToEnumerator = HashMultimap.create(100, 1);
    private final Map<String, Enumerator> uniqueIDToCanonicalEnumerator = new HashMap<String, Enumerator>();

    /**
     * Map from enum classes generated for {@link EEnum}s to the actual EEnum.
     */
    Map<Class<?>, EEnum> generatedEENumClasses = new HashMap<Class<?>, EEnum>();
    
    /**
     * @return the eObjectClassKey
     */
    public Object getEObjectClassKey() {
        if (eObjectClassKey == null) {
            eObjectClassKey = toKey(EOBJECT_CLASS);
        }
        return eObjectClassKey;
    }
    private Object eObjectClassKey = null;

    protected Object toKey(final EClassifier classifier) {
        if (isDynamicModel) {
            return toKeyDynamicInternal(classifier);
        } else {
            maintainMetamodel(classifier);
            return classifier;
        }
    }

    private String toKeyDynamicInternal(final EClassifier classifier) {
        String id = uniqueIDFromClassifier.get(classifier);
        if (id == null) {
            Preconditions.checkArgument(!classifier.eIsProxy(),
                    "Classifier %s is an unresolved proxy", classifier);
            id = classifier.getEPackage().getNsURI() + "##" + classifier.getName();
            uniqueIDFromClassifier.put(classifier, id);
            uniqueIDToClassifier.put(id, classifier);
            // metamodel maintenance will call back toKey(), but now the ID maps are already filled
            maintainMetamodel(classifier);
        }
        return id;
    }

    private String enumToKeyDynamicInternal(Enumerator enumerator) {
        String id = uniqueIDFromEnumerator.get(enumerator);
        if (id == null) {
            if (enumerator instanceof EEnumLiteral) {
                EEnumLiteral enumLiteral = (EEnumLiteral) enumerator;
                final EEnum eEnum = enumLiteral.getEEnum();
                maintainMetamodel(eEnum);

                id = constructEnumID(eEnum.getEPackage().getNsURI(), eEnum.getName(), enumLiteral.getLiteral());

                // there might be a generated enum for this enum literal!
                // generated enum should pre-empt the ecore enum literal as canonical enumerator
                Enumerator instanceEnum = enumLiteral.getInstance();
                if (instanceEnum != null && !uniqueIDToCanonicalEnumerator.containsKey(id)) {
                    uniqueIDToCanonicalEnumerator.put(id, instanceEnum);
                }
                // if generated enum not found... delay selection of canonical enumerator
            } else { // generated enum
                final EEnum eEnum = generatedEENumClasses.get(enumerator.getClass());
                if (eEnum != null)
                    id = constructEnumID(eEnum.getEPackage().getNsURI(), eEnum.getName(), enumerator.getLiteral());
                else
                    id = constructEnumID("unkownPackage URI", enumerator.getClass().getSimpleName(),
                            enumerator.getLiteral());

                // generated enum should pre-empt the ecore enum literal as canonical enumerator
                if (!uniqueIDToCanonicalEnumerator.containsKey(id)) {
                    uniqueIDToCanonicalEnumerator.put(id, enumerator);
                }
            }
            uniqueIDFromEnumerator.put(enumerator, id);
            uniqueIDToEnumerator.put(id, enumerator);
        }
        return id;
    }

    private String constructEnumID(String nsURI, String name, String literal) {
        return String.format("%s##%s##%s", nsURI, name, literal);
    }

    protected Object toKey(final EStructuralFeature feature) {
        if (isDynamicModel) {
            String id = uniqueIDFromTypedElement.get(feature);
            if (id == null) {
                Preconditions.checkArgument(!feature.eIsProxy(),
                        "Element %s is an unresolved proxy", feature);
                id = toKeyDynamicInternal((EClassifier) feature.eContainer()) + "##" + feature.getEType().getName()
                        + "##" + feature.getName();
                uniqueIDFromTypedElement.put(feature, id);
                uniqueIDToTypedElement.put(id, feature);
                // metamodel maintenance will call back toKey(), but now the ID maps are already filled
                maintainMetamodel(feature);
            }
            return id;
        } else {
            maintainMetamodel(feature);
            return feature;
        }
    }

    private Enumerator enumToCanonicalDynamicInternal(final Enumerator value) {
        final String key = enumToKeyDynamicInternal(value);
        Enumerator canonicalEnumerator = uniqueIDToCanonicalEnumerator.get(key);
        if (canonicalEnumerator == null) { // if no canonical version appointed yet, appoint first version
            canonicalEnumerator = uniqueIDToEnumerator.get(key).iterator().next();
            uniqueIDToCanonicalEnumerator.put(key, canonicalEnumerator);
        }
        return canonicalEnumerator;
    }

    /**
     * If in dynamic EMF mode, substitutes enum literals with a canonical version of the enum literal.
     */
    protected Object toInternalValueRepresentation(final Object value) {
        if (isDynamicModel) {
            if (value instanceof Enumerator)
                return enumToCanonicalDynamicInternal((Enumerator) value);
            else
                return value;
        } else {
            return value;
        }
    }

    /**
     * Checks the {@link EStructuralFeature}'s source and target {@link EPackage} for NsURI collision. An error message
     * will be logged if a model element from an other {@link EPackage} instance with the same NsURI has been already
     * processed. The error message will be logged only for the first time for a given {@link EPackage} instance.
     *
     * @param classifier
     *            the classifier instance
     */
    protected void maintainMetamodel(final EStructuralFeature feature) {
        if (!knownFeatures.contains(feature)) {
            knownFeatures.add(feature);
            maintainMetamodel(feature.getEContainingClass());
            maintainMetamodel(feature.getEType());
        }
    }

    /**
     * put subtype information into cache
     */
    protected void maintainMetamodel(final EClassifier classifier) {
        if (!knownClassifiers.contains(classifier)) {
            checkEPackage(classifier);
            knownClassifiers.add(classifier);

            if (classifier instanceof EClass) {
                final EClass clazz = (EClass) classifier;
                final Object clazzKey = toKey(clazz);
                for (final EClass superType : clazz.getEAllSuperTypes()) {
                    maintainTypeHierarhyInternal(clazzKey, toKey(superType));
                }
                maintainTypeHierarhyInternal(clazzKey, getEObjectClassKey());
            } else if (classifier instanceof EEnum) {
                EEnum eEnum = (EEnum) classifier;

                if (isDynamicModel) {
                    // if there is a generated enum class, save this model element for describing that class
                    if (eEnum.getInstanceClass() != null)
                        generatedEENumClasses.put(eEnum.getInstanceClass(), eEnum);

                    for (EEnumLiteral eEnumLiteral : eEnum.getELiterals()) {
                        // create string ID; register generated enum values
                        enumToKeyDynamicInternal(eEnumLiteral);
                    }
                }
            }
        }
    }

    /**
     * Checks the {@link EClassifier}'s {@link EPackage} for NsURI collision. An error message will be logged if a model
     * element from an other {@link EPackage} instance with the same NsURI has been already processed. The error message
     * will be logged only for the first time for a given {@link EPackage} instance.
     *
     * @param classifier
     *            the classifier instance
     */
    private void checkEPackage(final EClassifier classifier) {
        final EPackage ePackage = classifier.getEPackage();
        if (knownPackages.add(ePackage)) { // this is a new EPackage
            final String nsURI = ePackage.getNsURI();
            final Collection<EPackage> packagesOfURI = uniqueIDToPackage.get(nsURI);
            if (!packagesOfURI.contains(ePackage)) { // this should be true
                uniqueIDToPackage.put(nsURI, ePackage);
                // collision detection between EPackages (disabled in dynamic model mode)
                if (!isDynamicModel && packagesOfURI.size() == 2) { // only report the issue if the new EPackage
                                                                    // instance is the second for the same URI
                    navigationHelper.processingError(
                            new ViatraBaseException("NsURI (" + nsURI
                                    + ") collision detected between different instances of EPackages. If this is normal, try using dynamic EMF mode."),
                            "process new metamodel elements.");
                }
            }
        }
    }

    /**
     * Maintains subtype hierarchy
     * 
     * @param subClassKey
     *            EClass or String id of subclass
     * @param superClassKey
     *            EClass or String id of superclass
     */
    private void maintainTypeHierarhyInternal(final Object subClassKey, final Object superClassKey) {
        // update observed class and instance listener tables according to new subtype information
        if (navigationHelper.directlyObservedClasses.containsKey(superClassKey)) {
            navigationHelper.getAllObservedClassesInternal().put(subClassKey, navigationHelper.directlyObservedClasses.get(superClassKey));
        }
        final Table<Object, InstanceSurrogateListener<Surrogate>, Set<EClass>> instanceListeners = navigationHelper.subscriptions.peekInstanceSurrogateListeners();
        if (instanceListeners != null) { // table already constructed
            for (final Entry<InstanceSurrogateListener<Surrogate>, Set<EClass>> entry : instanceListeners.row(superClassKey).entrySet()) {
                final InstanceSurrogateListener<Surrogate> listener = entry.getKey();
                for (final EClass subscriptionType : entry.getValue()) {
                    navigationHelper.subscriptions.addInstanceSurrogateListenerInternal(listener, subscriptionType, subClassKey);
                }
            }
        }

        // update subtype maps
        Set<Object> subTypes = subTypeMap.get(superClassKey);
        if (subTypes == null) {
            subTypes = new HashSet<Object>();
            subTypeMap.put(superClassKey, subTypes);
        }
        subTypes.add(subClassKey);
        Set<Object> superTypes = superTypeMap.get(subClassKey);
        if (superTypes == null) {
            superTypes = new HashSet<Object>();
            superTypeMap.put(subClassKey, superTypes);
        }
        superTypes.add(superClassKey);
    }
    
    /**
     * @return the subTypeMap
     */
    protected Map<Object, Set<Object>> getSubTypeMap() {
        return subTypeMap;
    }

    protected Map<Object, Set<Object>> getSuperTypeMap() {
        return superTypeMap;
    }

    /**
     * Returns the corresponding {@link EStructuralFeature} instance for the id.
     *
     * @param featureId
     *            the id of the feature
     * @return the {@link EStructuralFeature} instance
     */
    public EStructuralFeature getKnownFeature(final String featureId) {
        final Collection<ETypedElement> features = uniqueIDToTypedElement.get(featureId);
        if (features != null && !features.isEmpty()) {
            final ETypedElement next = features.iterator().next();
            if (next instanceof EStructuralFeature) {
                return (EStructuralFeature) next;
            }
        }
        return null;

    }

    public EStructuralFeature getKnownFeatureForKey(Object featureKey) {
        EStructuralFeature feature;
        if (isDynamicModel) {
            feature = getKnownFeature((String) featureKey);
        } else {
            feature = (EStructuralFeature) featureKey;
        }
        return feature;
    }

    /**
     * Returns the corresponding {@link EClassifier} instance for the id.
     */
    public EClassifier getKnownClassifier(final String key) {
        final Collection<EClassifier> classifiersOfThisID = uniqueIDToClassifier.get(key);
        if (classifiersOfThisID != null && !classifiersOfThisID.isEmpty()) {
            return classifiersOfThisID.iterator().next();
        } else {
            return null;
        }
    }

    public EClassifier getKnownClassifierForKey(Object classifierKey) {
        EClassifier cls;
        if (isDynamicModel) {
            cls = getKnownClassifier((String) classifierKey);
        } else {
            cls = (EClassifier) classifierKey;
        }
        return cls;
    }
    
    
}
