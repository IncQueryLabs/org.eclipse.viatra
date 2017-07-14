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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.viatra.query.runtime.base.api.IEClassifierProcessor.IEDataTypeProcessor;
import org.eclipse.viatra.query.runtime.base.exception.ViatraBaseException;

/**
 * Common API methods of the base index, independent from the surrogate object mechanism.
 * @author Gabor Bergmann
 * @author Tamas Szabo
 * @since 1.7
 *
 */
public interface IEMFBaseIndexCommon {
    
    /**
     * @return access to the same index through the NavigationHelper interface
     */
    public NavigationHelper getNavigationHelper();
    
    /**
     * Call this method to dispose the base index. 
     * 
     * <p>After its disposal, the base index will no longer listen to EMF change notifications, 
     *   and it will be possible to GC it even if the model is retained in memory.
     * 
     * <dt><b>Precondition:</b><dd> no listeners can be registered at all.
     * @throws IllegalStateException if there are any active listeners
     * 
     */
    public void dispose();

    
    
    /**
     * Indicates whether indexing is performed in <em>wildcard mode</em> for a selected indexing level
     * 
     * @return true if everything is indexed, false if manual registration of interesting EClassifiers and
     *         EStructuralFeatures is required.
     */
    public boolean isInWildcardMode(IndexingLevel level);
    
    /**
     * Returns the current {@link IndexingLevel} applied to all model elements. For specific types it is possible to request a higher indexing levels, but cannot be lowered.
     * @return the current level of index specified
     */
    public IndexingLevel getWildcardLevel();
    
    /**
     * Starts wildcard indexing at the given level. After this call, no registration is required for this {@link IndexingLevel}.
     * a previously set wildcard level cannot be lowered, only extended.
     * 
     */
    public void setWildcardLevel(IndexingLevel level);

    /**
     */
    IndexingLevel getIndexingLevel(EClass type);
    
    /**
     */
    IndexingLevel getIndexingLevel(EDataType type);
    
    /**
     */
    IndexingLevel getIndexingLevel(EStructuralFeature feature);
    
    /**
     * Get the total number of instances of the given {@link EClass} and all of its subclasses.
     * 
     */
    public int countAllInstances(EClass clazz);

    /**
     */
    public int countDataTypeInstances(EDataType dataType);
    
    /**
     */
    public int countFeatures(EStructuralFeature feature);
    
    /**
     * Returns the set of instances for the given {@link EDataType} that can be found in the model.
     * 
     * <p>
     * <strong>Precondition:</strong> Results will be returned only if either (a) the EDataType has already been
     * registered using {@link #registerEDataTypes(Set)}, or (b) running in <em>wildcard mode</em> (see
     * {@link #isInWildcardMode()}).
     * 
     * @param type
     *            the data type
     * @return the set of all attribute values found in the model that are of the given data type
     */
    public Set<Object> getDataTypeInstances(EDataType type);

    /**
     * The given <code>listener</code> will be notified from now on whenever instances the given {@link EDataType}s 
     * are added to or removed from the model.  
     * 
     * @param types
     *            the collection of types associated to the listener
     * @param listener
     *            the listener instance
     */
    public void addDataTypeListener(Collection<EDataType> types, DataTypeListener listener);

    /**
     * Unregisters a data type listener for the given types.
     * 
     * @param types
     *            the collection of data types
     * @param listener
     *            the listener instance
     */
    public void removeDataTypeListener(Collection<EDataType> types, DataTypeListener listener);
    
    /**
     * Register a lightweight observer that is notified if the value of any feature of the given EObject changes.
     * 
     * @param observer the listener instance
     * @param observedObject the observed EObject
     * @return false if the observer was already attached to the object (call has no effect), true otherwise
     */
    public boolean addLightweightEObjectObserver(LightweightEObjectObserver observer, EObject observedObject);
    
    /**
     * Unregisters a lightweight observer for the given EObject.
     * 
     * @param observer the listener instance
     * @param observedObject the observed EObject
     * @return false if the observer has not been previously attached to the object (call has no effect), true otherwise
     */
    public boolean removeLightweightEObjectObserver(LightweightEObjectObserver observer, EObject observedObject);
    
    
    /**
     * Manually turns on indexing for the given types (indexing of others are unaffected). Note that
     * registering new types will result in a single iteration through the whole attached model.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * @param classes
     *            the set of classes to observe (null okay)
     * @param dataTypes
     *            the set of data types to observe (null okay)
     * @param features
     *            the set of features to observe (null okay)
     * @throws IllegalStateException if in wildcard mode
     */
    public void registerObservedTypes(Set<EClass> classes, Set<EDataType> dataTypes, Set<? extends EStructuralFeature> features, IndexingLevel level);
    
    /**
     * Manually turns off indexing for the given types (indexing of others are unaffected). Note that if the
     * unregistered types are re-registered later, the whole attached model needs to be visited again.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * <dt><b>Precondition:</b><dd> no listeners can be registered for the given types.
     * @param classes
     *            the set of classes that will be ignored again from now on (null okay)
     * @param dataTypes
     *            the set of data types that will be ignored again from now on (null okay)
     * @param features
     *            the set of features that will be ignored again from now on (null okay)
     * @throws IllegalStateException if in wildcard mode, or if there are listeners registered for the given types
     */
    public void unregisterObservedTypes(Set<EClass> classes, Set<EDataType> dataTypes, Set<? extends EStructuralFeature> features);  
    /**
     * Manually turns on indexing for the given features (indexing of other features are unaffected). Note that
     * registering new features will result in a single iteration through the whole attached model.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * @param features
     *            the set of features to observe
     * @throws IllegalStateException if in wildcard mode
     */
    public void registerEStructuralFeatures(Set<? extends EStructuralFeature> features, IndexingLevel level);

    /**
     * Manually turns off indexing for the given features (indexing of other features are unaffected). Note that if the
     * unregistered features are re-registered later, the whole attached model needs to be visited again.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * <dt><b>Precondition:</b><dd> no listeners can be registered for the given features.
     * 
     * @param features
     *            the set of features that will be ignored again from now on
     * @throws IllegalStateException if in wildcard mode, or if there are listeners registered for the given types
     */
    public void unregisterEStructuralFeatures(Set<? extends EStructuralFeature> features);
    /**
     * Manually turns on indexing for the given classes (indexing of other classes are unaffected). Instances of
     * subclasses will also be indexed. Note that registering new classes will result in a single iteration through the whole
     * attached model.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * @param classes
     *            the set of classes to observe
     * @throws IllegalStateException if in wildcard mode
     */
    public void registerEClasses(Set<EClass> classes, IndexingLevel level);

    /**
     * Manually turns off indexing for the given classes (indexing of other classes are unaffected). Note that if the
     * unregistered classes are re-registered later, the whole attached model needs to be visited again.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * <dt><b>Precondition:</b><dd> no listeners can be registered for the given classes.
     * @param classes
     *            the set of classes that will be ignored again from now on
     * @throws IllegalStateException if in wildcard mode, or if there are listeners registered for the given types
     */
    public void unregisterEClasses(Set<EClass> classes);

    /**
     * Manually turns on indexing for the given data types (indexing of other features are unaffected). Note that
     * registering new data types will result in a single iteration through the whole attached model.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * @param dataTypes
     *            the set of data types to observe
     * @throws IllegalStateException if in wildcard mode
     */
    public void registerEDataTypes(Set<EDataType> dataTypes, IndexingLevel level);
    
    /**
     * Manually turns off indexing for the given data types (indexing of other data types are unaffected). Note that if
     * the unregistered data types are re-registered later, the whole attached model needs to be visited again.
     * <b> Not usable in <em>wildcard mode</em>.</b>
     * 
     * <dt><b>Precondition:</b><dd> no listeners can be registered for the given datatypes.
     * 
     * @param dataTypes
     *            the set of data types that will be ignored again from now on
     * @throws IllegalStateException if in wildcard mode, or if there are listeners registered for the given types
     */
    public void unregisterEDataTypes(Set<EDataType> dataTypes);

    /**
     * The given callback will be executed, and all model traversals and index registrations will be delayed until the
     * execution is done. If there are any outstanding feature, class or datatype registrations, a single coalesced model
     * traversal will initialize the caches and deliver the notifications.
     * 
     * @param callable
     */
    public <V> V coalesceTraversals(Callable<V> callable) throws InvocationTargetException;

    /**
     * Execute the given runnable after traversal. It is guaranteed that the runnable is executed as soon as
     * the indexing is finished. The callback is executed only once, then is removed from the callback queue.
     * @param traversalCallback
     * @throws InvocationTargetException 
     */
    public void executeAfterTraversal(Runnable traversalCallback) throws InvocationTargetException;
    
    /**
     * Examines whether execution is currently in the callable 
     *  block of an invocation of {#link {@link #coalesceTraversals(Callable)}}. 
     */
    public boolean isCoalescing();
    /**
     * Adds a coarse-grained listener that will be invoked after the NavigationHelper index or the underlying model is changed. Can be used
     * e.g. to check model contents. Not intended for general use.
     * 
     * <p/> See {@link #removeBaseIndexChangeListener(EMFBaseIndexChangeListener)}
     * @param listener
     */
    public void addBaseIndexChangeListener(EMFBaseIndexChangeListener listener);
    
    /**
     * Removes a registered listener.
     * 
     * <p/> See {@link #addBaseIndexChangeListener(EMFBaseIndexChangeListener)}
     * 
     * @param listener
     */
    public void removeBaseIndexChangeListener(EMFBaseIndexChangeListener listener);
    
    /**
     * Adds an additional EMF model root.
     * 
     * @param emfRoot
     */
    public void addRoot(Notifier emfRoot) throws ViatraBaseException;
    
    /**
     * Moves an EObject (along with its entire containment subtree) within the containment hierarchy of the EMF model. 
     *   The object will be relocated from the original parent object to a different parent, or a different containment 
     *   list of the same parent. 
     *   
     * <p> When indexing is enabled, such a relocation is costly if performed through normal getters/setters, as the index 
     * for the entire subtree is pruned at the old location and reconstructed at the new one. 
     * This method provides a workaround to keep the operation cheap.
     * 
     * <p> This method is experimental. Re-entrancy not supported.
     * 
     * @param element the eObject to be moved
     * @param targetContainmentReferenceList containment list of the new parent object into which the element has to be moved
     * 
     */
    public <T extends EObject> void cheapMoveTo(T element, EList<T> targetContainmentReferenceList);
    
    /**
     * Moves an EObject (along with its entire containment subtree) within the containment hierarchy of the EMF model. 
     *   The object will be relocated from the original parent object to a different parent, or a different containment 
     *   list of the same parent. 
     *   
     * <p> When indexing is enabled, such a relocation is costly if performed through normal getters/setters, as the index 
     * for the entire subtree is pruned at the old location and reconstructed at the new one. 
     * This method provides a workaround to keep the operation cheap.
     * 
     * <p> This method is experimental. Re-entrancy not supported.
     * 
     * @param element the eObject to be moved
     * @param parent  the new parent object under which the element has to be moved
     * @param containmentFeature the kind of containment reference that should be established between the new parent and the element
     * 
     */
    public void cheapMoveTo(EObject element, EObject parent, EReference containmentFeature);
   
    /**
     * Traverses all instances of a selected data type stored in the base index, and allows executing a custom function on
     * it. There is no guaranteed order in which the processor will be called with the selected features.
     * 
     * @param type
     * @param processor
     */
    void processDataTypeInstances(EDataType type, IEDataTypeProcessor processor);
    /**
     * Updates the value of indexed derived features that are not well-behaving.
     */
    void resampleDerivedFeatures();

    /**
     * Adds a listener for internal errors in the index. A listener can only be added once.
     * @param listener
     * @returns true if the listener was not already added
     */
    boolean addIndexingErrorListener(IEMFIndexingErrorListener listener);
    /**
     * Removes a listener for internal errors in the index
     * @param listener
     * @returns true if the listener was successfully removed (e.g. it did exist)
     */
    boolean removeIndexingErrorListener(IEMFIndexingErrorListener listener);

    /**
     * Returns the internal, canonicalized implementation of an attribute value.
     * 
     * <p> Behaviour: when in dynamic EMF mode, substitutes enum literals with a canonical version of the enum literal.
     * Otherwise, returns the input. 
     * 
     * <p> The canonical enum literal will be guaranteed to be a valid EMF enum literal ({@link Enumerator}), 
     *  and the best effort is made to ensure that it will be the same for all versions of the {@link EEnum}, 
     *  including {@link EEnumLiteral}s in different versions of ecore packages, as well as Java enums generated from them..
     * 
     * <p> Usage is not required when simply querying the indexed model through the {@link NavigationHelper} API, 
     *  as both method inputs and the results returned are automatically canonicalized in dynamic EMF mode. 
     * Using this method is required only if the client wants to do querying/filtering on the results returned, and wants to know what to look for.   
     */
    Object toCanonicalValueRepresentation(Object value);
 
}
