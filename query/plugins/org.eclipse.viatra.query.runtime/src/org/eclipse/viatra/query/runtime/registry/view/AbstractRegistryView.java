/*******************************************************************************
 * Copyright (c) 2010-2016, Abel Hegedus, IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.registry.view;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Set;

import org.eclipse.viatra.query.runtime.registry.IQuerySpecificationRegistry;
import org.eclipse.viatra.query.runtime.registry.IQuerySpecificationRegistryEntry;
import org.eclipse.viatra.query.runtime.registry.IQuerySpecificationRegistryChangeListener;
import org.eclipse.viatra.query.runtime.registry.IRegistryView;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * An abstract {@link IRegistryView} implementation that stores the registry, the set of listeners added to the view and
 * the FQN to entry map of the view itself. The only responsibility of subclasses is to decide whether an entry received
 * as an addition or removal notification is relevant to the view.
 * 
 * @author Abel Hegedus
 * @since 1.3
 *
 */
public abstract class AbstractRegistryView implements IRegistryView {

    protected final IQuerySpecificationRegistry registry;
    protected final SetMultimap<String, IQuerySpecificationRegistryEntry> fqnToEntryMap;
    protected final Set<IQuerySpecificationRegistryChangeListener> listeners;
    protected final boolean allowDuplicateFQNs;

    /**
     * This method is called both when an addition or removal notification is received from the registry. Subclasses can
     * implement view filtering by returning false for those specifications that are not relevant for this view.
     * 
     * @param entry
     *            that is added or removed in the registry
     * @return true if the entry should be added to or removed from the view, false otherwise
     */
    protected abstract boolean isEntryRelevant(IQuerySpecificationRegistryEntry entry);

    /**
     * Creates a new view instance for the given registry. Note that views are created by the registry and the view
     * update mechanisms are also set up by the registry.
     * 
     * @param registry
     */
    public AbstractRegistryView(IQuerySpecificationRegistry registry, boolean allowDuplicateFQNs) {
        this.registry = registry;
        this.allowDuplicateFQNs = allowDuplicateFQNs;
        this.fqnToEntryMap = Multimaps.newSetMultimap(
                Maps.<String, Collection<IQuerySpecificationRegistryEntry>> newTreeMap(),
                new Supplier<Set<IQuerySpecificationRegistryEntry>>() {
                    @Override
                    public Set<IQuerySpecificationRegistryEntry> get() {
                        return Sets.newHashSet();
                    }
                });
        this.listeners = Sets.newHashSet();
    }

    @Override
    public IQuerySpecificationRegistry getRegistry() {
        return registry;
    }

    @Override
    public Iterable<IQuerySpecificationRegistryEntry> getEntries() {
        ImmutableSet<IQuerySpecificationRegistryEntry> entrySet = ImmutableSet.copyOf(fqnToEntryMap.values());
        return entrySet;
    }

    @Override
    public Set<String> getQuerySpecificationFQNs() {
        ImmutableSet<String> fqns = ImmutableSet.copyOf(fqnToEntryMap.keySet());
        return fqns;
    }

    @Override
    public boolean hasQuerySpecificationFQN(String fullyQualifiedName) {
        checkArgument(fullyQualifiedName != null, "FQN must not be null!");
        return fqnToEntryMap.containsKey(fullyQualifiedName);
    }

    @Override
    public Set<IQuerySpecificationRegistryEntry> getEntries(String fullyQualifiedName) {
        checkArgument(fullyQualifiedName != null, "FQN must not be null!");
        Set<IQuerySpecificationRegistryEntry> entries = fqnToEntryMap.get(fullyQualifiedName);
        return ImmutableSet.copyOf(entries);
    }

    @Override
    public void addViewListener(IQuerySpecificationRegistryChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeViewListener(IQuerySpecificationRegistryChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void entryAdded(IQuerySpecificationRegistryEntry entry) {
        if (isEntryRelevant(entry)) {
            String fullyQualifiedName = entry.getFullyQualifiedName();
            if(!allowDuplicateFQNs && fqnToEntryMap.containsKey(fullyQualifiedName)){
                Set<IQuerySpecificationRegistryEntry> removed = fqnToEntryMap.removeAll(fullyQualifiedName);
                for (IQuerySpecificationRegistryEntry e : removed) {
                    for (IQuerySpecificationRegistryChangeListener listener : listeners) {
                        listener.entryRemoved(e);
                    }
                }
            }
            fqnToEntryMap.put(fullyQualifiedName, entry);
            for (IQuerySpecificationRegistryChangeListener listener : listeners) {
                listener.entryAdded(entry);
            }
        }
    }

    @Override
    public void entryRemoved(IQuerySpecificationRegistryEntry entry) {
        if (isEntryRelevant(entry)) {
            String fullyQualifiedName = entry.getFullyQualifiedName();
            fqnToEntryMap.remove(fullyQualifiedName, entry);
            for (IQuerySpecificationRegistryChangeListener listener : listeners) {
                listener.entryRemoved(entry);
            }
        }
    }

}