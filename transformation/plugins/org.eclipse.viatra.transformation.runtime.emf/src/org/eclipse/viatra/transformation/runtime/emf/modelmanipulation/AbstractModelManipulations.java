/*******************************************************************************
 * Copyright (c) 2004-2013, Zoltan Ujhelyi and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.transformation.runtime.emf.modelmanipulation;

import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.base.api.IEMFBaseIndex;
import org.eclipse.viatra.query.runtime.base.api.NavigationHelper;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Abstract base class for model manipulation implementation. It checks for the preconditions of the operations, and the
 * subclasses should override the do* methods.
 * 
 * @author Zoltan Ujhelyi
 * 
 */
public abstract class AbstractModelManipulations implements IModelManipulations {

    protected final ViatraQueryEngine engine;
    private IEMFBaseIndex<?> baseEMFIndex;

    public AbstractModelManipulations(ViatraQueryEngine engine) {
        super();
        this.engine = engine;
    }

    /**
     * @since 1.7
     */
    protected IEMFBaseIndex<?> getBaseEMFIndex() throws ViatraQueryException {
        if (baseEMFIndex == null) {
            baseEMFIndex = EMFScope.extractUnderlyingEMFBaseIndex(engine);
        }
        return baseEMFIndex;
    }

    protected void doMoveTo(Collection<EObject> what, EObject newContainer, EReference reference)
            throws ModelManipulationException {
        for (EObject obj : what) {
            doMoveTo(obj, newContainer, reference);
        }
    }

    protected abstract void doMoveTo(EObject what, Resource newContainer) throws ModelManipulationException;

    protected abstract void doMoveTo(EObject what, Resource newContainer, int index) throws ModelManipulationException;

    protected abstract void doMoveTo(EObject what, EObject newContainer, EReference reference)
            throws ModelManipulationException;

    protected abstract void doMoveTo(EObject what, EObject newContainer, EReference reference, int index)
            throws ModelManipulationException;
    
    /**
     * Remove a non-containment reference value
     */
    protected abstract void doRemove(EObject container, EStructuralFeature feature, Object element)
            throws ModelManipulationException;

    protected abstract void doRemove(EObject container, EStructuralFeature feature, int index)
            throws ModelManipulationException;

    /**
     * Removes an element from the containment hierarchy
     */
    protected abstract void doRemove(EObject object) throws ModelManipulationException;

    protected abstract void doRemove(EObject container, EStructuralFeature reference) throws ModelManipulationException;

    protected abstract void doAdd(EObject container, EStructuralFeature reference, Collection<? extends Object> element)
            throws ModelManipulationException;

    protected abstract void doAdd(EObject container, EStructuralFeature reference, Object object, int index)
            throws ModelManipulationException;

    protected abstract void doSet(EObject container, EStructuralFeature feature, Object value)
            throws ModelManipulationException;

    protected abstract EObject doCreate(EObject container, EReference reference, EClass clazz)
            throws ModelManipulationException;

    protected abstract EObject doCreate(Resource res, EClass clazz) throws ModelManipulationException;

    protected abstract void doChangeIndex(EObject container, EStructuralFeature feature, int oldIndex, int newIndex)
            throws ModelManipulationException;

    @Override
    public EObject create(Resource res, EClass clazz) throws ModelManipulationException {
        return doCreate(res, clazz);
    }

    @Override
    public EObject createChild(EObject container, EReference reference, EClass clazz)
            throws ModelManipulationException {
        EClass containerClass = container.eClass();
        Preconditions.checkArgument(!(containerClass.getEAllReferences().contains(container)),
                "The container of EClass %s does neither define or inherit an EReference %s.", containerClass.getName(),
                reference.getName());
        Preconditions.checkArgument(reference.isContainment(),
                "Created elements must be inserted directly into the containment hierarchy.");
        Preconditions.checkArgument(!clazz.isAbstract(), "Cannot instantiate abstract EClass %s.", clazz.getName());

        return doCreate(container, reference, clazz);
    }

    @Override
    public void add(EObject container, EStructuralFeature feature, Object element) throws ModelManipulationException {
        addTo(container, feature, element);
    }

    @Override
    public void addTo(EObject container, EStructuralFeature feature, Object element) throws ModelManipulationException {
        addTo(container, feature, ImmutableList.of(element));
    }
    
    @Override
    public void add(EObject container, EStructuralFeature feature, Object element, int index)
            throws ModelManipulationException {
        addTo(container, feature, element, index);
    }

    @Override
    public void add(EObject container, EStructuralFeature feature, Collection<? extends Object> elements)
            throws ModelManipulationException {
        addTo(container, feature, elements);
    }

    @Override
    public void addTo(EObject container, EStructuralFeature feature, Object element, int index)
            throws ModelManipulationException {
        EClass containerClass = container.eClass();
        Preconditions.checkArgument(containerClass.getEAllStructuralFeatures().contains(feature),
                "The container of EClass %s does neither define or inherit an EReference or EAttribute named %s.",
                containerClass.getName(), feature.getName());
        Preconditions.checkArgument(feature.isMany(),
                "The EStructuralFeature %s must have an upper bound larger than 1.", feature.getName());
        Preconditions.checkArgument(!(feature instanceof EReference && ((EReference) feature).isContainment()),
                "Adding existing elements into the containment reference %s is not supported.", feature.getName());
        doAdd(container, feature, element, index);
    }
    
    @Override
    public void addTo(EObject container, EStructuralFeature feature, Collection<? extends Object> elements)
            throws ModelManipulationException {
        EClass containerClass = container.eClass();
        Preconditions.checkArgument(containerClass.getEAllStructuralFeatures().contains(feature),
                "The container of EClass %s does neither define or inherit an EReference or EAttribute named %s.",
                containerClass.getName(), feature.getName());
        Preconditions.checkArgument(feature.isMany(),
                "The EStructuralFeature %s must have an upper bound larger than 1.", feature.getName());
        Preconditions.checkArgument(!(feature instanceof EReference && ((EReference) feature).isContainment()),
                "Adding existing elements into the containment reference %s is not supported.", feature.getName());
        doAdd(container, feature, elements);
    }

    @Override
    public void set(EObject container, EStructuralFeature feature, Object value) throws ModelManipulationException {
        EClass containerClass = container.eClass();
        Preconditions.checkArgument(containerClass.getEAllStructuralFeatures().contains(feature),
                "The container of EClass %s does neither define or inherit an EAttribute or EReference %s.",
                containerClass.getName(), feature.getName());
        Preconditions.checkArgument(!feature.isMany(), "The EStructuralFeature %s must have an upper bound of 1.",
                feature.getName());
        doSet(container, feature, value);
    }

    @Override
    public void remove(EObject object) throws ModelManipulationException {
        doRemove(object);
    }

    @Override
    public void remove(EObject container, EStructuralFeature feature, Object element)
            throws ModelManipulationException {
        EClass containerClass = container.eClass();
        Preconditions.checkArgument(containerClass.getEAllStructuralFeatures().contains(feature),
                "The container of EClass %s does neither define or inherit an EAttribute or EReference %s.",
                containerClass.getName(), feature.getName());
        Preconditions.checkArgument(feature.isMany(),
                "Remove only works on EStructuralFeatures with 'many' multiplicity.");
        doRemove(container, feature, element);
    }

    @Override
    public void remove(EObject container, EStructuralFeature feature, int index) throws ModelManipulationException {
        EClass containerClass = container.eClass();
        Preconditions.checkArgument(containerClass.getEAllStructuralFeatures().contains(feature),
                "The container of EClass %s does neither define or inherit an EAttribute or EReference %s.",
                containerClass.getName(), feature.getName());
        Preconditions.checkArgument(feature.isMany(), "Remove only works on features with 'many' multiplicity.");
        doRemove(container, feature, index);
    }
    
    @Override
    public void remove(EObject container, EStructuralFeature feature) throws ModelManipulationException {
        EClass containerClass = container.eClass();
        Preconditions.checkArgument(containerClass.getEAllStructuralFeatures().contains(feature),
                "The container of EClass %s does neither define or inherit an EAttribute or EReference %s.",
                containerClass.getName(), feature.getName());
        Preconditions.checkArgument(feature.isMany(), "Remove only works on references with 'many' multiplicity.");
        doRemove(container, feature);
    }

    @Override
    public void moveTo(EObject what, Resource newContainer) throws ModelManipulationException {
        doMoveTo(what, newContainer);
    }

    @Override
    public void moveTo(EObject what, Resource newContainer, int index) throws ModelManipulationException {
        doMoveTo(what, newContainer, index);
    }

    @Override
    public void moveTo(EObject what, EObject newContainer, EReference reference) throws ModelManipulationException {
        doMoveTo(what, newContainer, reference);
    }

    @Override
    public void moveTo(EObject what, EObject newContainer, EReference reference, int index)
            throws ModelManipulationException {
        doMoveTo(what, newContainer, reference, index);
    }

    @Override
    public void moveTo(Collection<EObject> what, EObject newContainer, EReference reference)
            throws ModelManipulationException {
        doMoveTo(what, newContainer, reference);
    }

    @Override
    public void changeIndex(EObject container, EStructuralFeature feature, int oldIndex, int newIndex)
            throws ModelManipulationException {
        doChangeIndex(container, feature, oldIndex, newIndex);
    }
}
