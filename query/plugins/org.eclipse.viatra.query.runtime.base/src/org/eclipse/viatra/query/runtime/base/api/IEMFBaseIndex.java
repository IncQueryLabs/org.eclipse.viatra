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

import java.util.Collection;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.viatra.query.runtime.base.api.IEClassifierProcessor.IEClassSurrogateProcessor;

/**
 * The new API of the EMF Base Index.
 * 
 * <p>
 * Using an index of the EMF model, this interface exposes useful query functionality, such as:
 * <ul>
 * <li>
 * Getting all the (direct or descendant) instances of a given {@link EClass}, or their count.
 * <li>
 * Querying instances of given data types, or structural features, or their count.
 * <li>
 * Change notifications of the above.
 * <li>
 * "Surrogate" representations of {@link EObject}s.
 * </ul>
 * As queries are served from an index, results are always instantaneous. 
 * </p>
 * 
 * <p>
 * Such indices will be built on an EMF model rooted at an {@link EObject}, {@link Resource} or {@link ResourceSet}. 
 * The boundaries of the model are defined by the containment (sub)tree.
 * The indices will be <strong>maintained incrementally</strong> on changes to the model; these updates can also be
 * observed by registering listeners.
 * </p>
 * 
 * <p>
 * One of the options is to build indices in <em>wildcard mode</em>, meaning that all EClasses, EDataTypes, EReferences
 * and EAttributes are indexed. This is convenient, but comes at a high memory cost. To save memory, one can disable
 * <em>wildcard mode</em> and manually register those EClasses, EDataTypes, EReferences and EAttributes that should be
 * indexed.
 * </p>
 * 
 * <p>
 * Another choice is whether to build indices in <em>dynamic EMF mode</em>, meaning that types are identified by the String IDs 
 * that are ultimately derived from the nsURI of the EPackage. Multiple types with the same ID are treated as the same.
 * This is useful if dynamic EMF is used, where there can be multiple copies (instantiations) of the same EPackage, 
 * representing essentially the same metamodel. If one disables <em>dynamic EMF mode</em>, an error is logged if 
 * duplicate EPackages with the same nsURI are encountered.
 * </p>
 *  
 * <p>
 * Note that none of the defined query methods return null upon empty result sets. All query methods return either a copy of
 * the result sets or an unmodifiable collection of the result view.
 * 
 * <p>
 * Instantiate using {@link ViatraBaseFactory}
 * 
 * @author Tamas Szabo
 * @author Gabor Bergmann
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 * @param <Surrogate> surrogate substitute of {@link EObject}s provided by a {@link ISurrogateObjectService}
 * 
 * @since 1.7
 */
public interface IEMFBaseIndex<Surrogate> extends IEMFBaseIndexCommon {
    
    
    /**
     */
    public int countFeatureTargetsFromSurrogate(Surrogate seedSource, EStructuralFeature feature);

    
    /**
     * Find all {@link Object}s that are the target of the EStructuralFeature <code>feature</code> from the given
     * surrogate <code>source</code> object.
     * 
     * <p>
     * Unset / null-valued features are not indexed, and will not be included in the results.
     * 
     * <p>
     * <strong>Precondition:</strong> Results will be returned only if either (a) the feature has already been
     * registered, or (b) running in <em>wildcard mode</em> (see
     * {@link #isInWildcardMode()}).
     * 
     * @param source the host object
     * @param feature an EStructuralFeature of the host object
     * @return the set of values that the given feature takes at the given source object 
     * (surrogate values in case of references)
     * 
     */
    public Set<Object> getFeatureSurrogateTargets(Surrogate source, EStructuralFeature feature);

    /**
     * Get the all surrogate instances of the given {@link EClass}. 
     * This includes instances of subclasses.
     * 
     * <p>
     * <strong>Precondition:</strong> Results will be returned only if either (a) the EClass (or any superclass) has
     * already been registered using {@link #registerEClasses(Set)}, or (b) running in <em>wildcard mode</em> (see
     * {@link #isInWildcardMode()}).
     * 
     * @param clazz
     *            an EClass
     * @return the collection of surrogate instances of the given EClass and any of its subclasses
     * 
     * @see #getDirectInstances(EClass)
     */
    public Set<Surrogate> getAllSurrogateInstances(EClass clazz);
    
    /**
     * Checks whether the given object is a valid surrogate and specifically the surrogate of an instance of the given {@link EClass}. 
     * This includes instances of subclasses.
     * <p> Special note: this method does not check whether the object is indexed in the scope, 
     * and will return true for out-of-scope objects as well (as long as they are instances of the class). 
     * <p> The given class does not have to be indexed.
     * <p> Even if no surrogate service is used, there is a difference between this method and {@link EClassifier#isInstance(Object)}: 
     * in dynamic EMF mode, EPackage equivalence is taken into account. 
     */
    public boolean isSurrogateInstanceOfUnscoped(Surrogate object, EClass clazz);
    
    /**
     * Checks whether the given object is a valid surrogate and specifically the surrogate of an instance of the given {@link EClass}. 
     * This includes instances of subclasses.
     * <p> Special note: this method does check whether the object is indexed in the scope, 
     * and will return false for out-of-scope objects as well (as long as they are instances of the class). 
     * <p> The given class does have to be indexed. 
     * @since 1.7
     */
    public boolean isSurrogateInstanceOfScoped(Surrogate object, EClass clazz);
    
    /**
     * Decides whether the given non-null source and target objects are connected via a specific, indexed EStructuralFeature instance.
     * 
     * <p>
     * Unset / null-valued features are not indexed, and will not be included in the results.
     * 
     * <p>
     * <strong>Precondition:</strong> Result will be true only if either (a) the feature has already been
     * registered, or (b) running in <em>wildcard mode</em> (see
     * {@link #isInWildcardMode()}).
     * @since 1.7
     */
    public boolean isSurrogateFeatureInstance(Surrogate source, Object target, EStructuralFeature feature);
    
    /**
     * Returns whether an object is a set of instance for the given {@link EDataType} that can be found in the model.
     * <p>
     * <strong>Precondition:</strong> Result will be true only if either (a) the EDataType has already been registered
     * using {@link #registerEDataTypes(Set)}, or (b) running in <em>wildcard mode</em> (see
     * {@link #isInWildcardMode()}).
     * 
     * @param value a non-null value to decide whether it is available as an EDataType instance
     * @param type a non-null EDataType
     * @return true, if a corresponding instance was found
     * @since 1.7
     */
    public boolean isInstanceOfDatatype(Object value, EDataType type);
    
    /**
     * Find all source surrogate objects for which the given <code>feature</code> points to / takes the given (surrogate) <code>value</code>.
     * 
     * <p>
     * <strong>Precondition:</strong> Unset / null-valued features are not indexed, so <code>value!=null</code>
     * 
     * <p>
     * <strong>Precondition:</strong> Results will be returned only if either (a) the feature has already been
     * registered using {@link #registerEStructuralFeatures(Set)}, or (b) running in <em>wildcard mode</em> (see
     * {@link #isInWildcardMode()}).
     * 
     * @param value
     *            the value of the feature
     * @param feature
     *            the feature instance
     * @return the collection of {@link EObject} instances
     */
    public Set<Surrogate> findSurrogateByFeatureValue(Object value, EStructuralFeature feature);

    /**
     * The given <code>listener</code> will be notified from now on whenever instances the given {@link EClass}es 
     * (and any of their subtypes) are added to or removed from the model.  
     *  
     * @param classes
     *            the collection of classes whose instances the listener should be notified of
     * @param listener
     *            the listener instance
     */
    public void addInstanceSurrogateListener(Collection<EClass> classes, InstanceSurrogateListener<Surrogate> listener);

    /**
     * Unregisters an instance listener for the given classes.
     * 
     * @param classes
     *            the collection of classes
     * @param listener
     *            the listener instance
     */
    public void removeInstanceSurrogateListener(Collection<EClass> classes, InstanceSurrogateListener<Surrogate> listener);


    /**
     * The given <code>listener</code> will be notified from now on whenever instances the given {@link EStructuralFeature}s 
     * are added to or removed from the model.  
     * 
     * @param features
     *            the collection of features associated to the listener
     * @param listener
     *            the listener instance
     */
    public void addFeatureSurrogateListener(Collection<? extends EStructuralFeature> features, FeatureSurrogateListener<Surrogate> listener);

    /**
     * Unregisters a feature listener for the given features.
     * 
     * @param listener
     *            the listener instance
     * @param features
     *            the collection of features
     */
    public void removeFeatureSurrogateListener(Collection<? extends EStructuralFeature> features, FeatureSurrogateListener<Surrogate> listener);
 
    /**
     * Traverses all direct instances of a selected class stored in the base index, and allows executing a custom function on
     * it. There is no guaranteed order in which the processor will be called with the selected features.
     * 
     * @param type
     * @param processor
     */
    void processAllSurrogateInstances(EClass type, IEClassSurrogateProcessor<Surrogate> processor);

    /**
     * Traverses all direct instances of a selected class stored in the base index, and allows executing a custom function on
     * it. There is no guaranteed order in which the processor will be called with the selected features.
     * 
     * @param type
     * @param processor
     */
    void processDirectSurrogateInstances(EClass type, IEClassSurrogateProcessor<Surrogate> processor);
    
    /**
     * Traverses all instances of a selected feature stored in the base index, and allows executing a custom function on
     * it. There is no guaranteed order in which the processor will be called with the selected features. 
     * The surrogates of sources and reference targets will be processed.
     * 
     * <p>
     * <strong>Precondition:</strong> Will only find those EAttributes that have already been registered using
     * {@link #registerEStructuralFeatures(Set)}, unless running in <em>wildcard mode</em> (see
     * {@link #isInWildcardMode()}).
     * 
     * @param feature
     * @param processor
     */
    public void processAllFeatureInstancesWithSurrogates(EStructuralFeature feature, IEStructuralFeatureSurrogateProcessor<Surrogate> processor);
    

}
