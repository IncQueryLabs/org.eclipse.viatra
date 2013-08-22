/*******************************************************************************
 * Copyright (c) 2010-2013, istvanrath, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   istvanrath - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.viewers.runtime.model;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.incquery.databinding.runtime.observables.ObservableLabelFeature;
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.viewers.runtime.model.listeners.IViewerLabelListener;
import org.eclipse.incquery.viewers.runtime.model.listeners.IViewerStateListener;

import com.google.common.collect.Multimap;

/**
 * @author istvanrath
 *
 */
public abstract class ViewerState {

	
	/* factory method */
	
	// here you can easily switch between list and set-based implementations
	private static boolean setMode = false;
	// at the moment, the set-based one is very buggy
	
	public static ViewerState newInstance(ResourceSet set, IncQueryEngine engine,
			Collection<Pattern> patterns, ViewerDataFilter filter,
			Collection<ViewerStateFeature> features) {
		if (setMode)
			return new ViewerStateSet(set, engine, patterns, filter, features);
		else
			return new ViewerStateList(set, engine, patterns, filter, features);		
	}
	
	public static ViewerState newInstance(ViewerDataModel model, ViewerDataFilter filter,
			Collection<ViewerStateFeature> features)
	{
		if (setMode)
			return new ViewerStateSet(model, filter, features);
		else
			return new ViewerStateList(model, filter, features);
	}
	
	
	
	/**
	 * Maps low-xlevel model objects to their corresponding items.
	 */
	protected Multimap<Object, Item> itemMap;
	
	/**
	 * Maps parent-child relationships in the viewer model.
	 */
	protected Multimap<Item, Item> childrenMap;
	
	/**
     * Maps child-parent relationships in the viewer model.
     */
	protected Map<Item, Item> parentMap;
	
	public Collection<Item> getChildren(Item parent) {
		return childrenMap.get(parent);
	}

	public Item getParent(Item child) {
		return parentMap.get(child);
	}
	
	protected ViewerDataModel model;

	public ViewerDataModel getModel() {
		return model;
	}

	public enum ViewerStateFeature {
		EDGE, CONTAINMENT
	}
	
	
	/*
	 * Listener management
	 */


	protected ListenerList stateListeners = new ListenerList();
	
	protected ListenerList labelListeners = new ListenerList();
	protected IChangeListener labelChangeListener = new IChangeListener() {
		@Override
        public void handleChange(ChangeEvent event) {
            Object element = ((ObservableLabelFeature) event.getSource()).getContainer();
            for (Object _listener : labelListeners.getListeners()) {
            	IViewerLabelListener listener = (IViewerLabelListener) _listener;
            	if (element instanceof Item) {
            		Item item = (Item) element;
					listener.labelUpdated(item, event.getObservable().toString());
            	} else if (element instanceof Edge) {
					Edge edge = (Edge) element;
            		listener.labelUpdated(edge, event.getObservable().toString());
            	}
            }
		}
	};
	
	/**
	 * Adds a new state Listener to the Viewer State
	 */
	public void addStateListener(IViewerStateListener listener) {
		stateListeners.add(listener);
	}

	/**
	 * Removes a state Listener from the Viewer State
	 */
	public void removeStateListener(IViewerStateListener listener) {
		stateListeners.remove(listener);
	}
	
	public void addLabelListener(IViewerLabelListener listener) {
		labelListeners.add(listener);
	}
	
	public void removeLabelListener(IViewerLabelListener listener) {
		labelListeners.remove(listener);
	}
	
	/**
	 * Exposes EObject -> Item* traceability information.
	 * 
	 * Access the Set of Items mapped to an EObject.
	 */
	public Collection<Item> getItemsFor(Object target) {
		return itemMap.get(target);
	}
	
	/* Hooks into concrete implementations */
	
	public abstract IObservableCollection getItems();
	
	public abstract IObservableCollection getEdges();
	
	public abstract IObservableCollection getContainments();
	
	
}
