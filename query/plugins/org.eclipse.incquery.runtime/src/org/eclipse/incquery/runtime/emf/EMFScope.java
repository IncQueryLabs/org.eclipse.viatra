/*******************************************************************************
 * Copyright (c) 2010-2014, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *   Denes Harmath - support for multiple scope roots
 *******************************************************************************/
package org.eclipse.incquery.runtime.emf;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.incquery.runtime.api.AdvancedIncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.api.scope.IncQueryScope;
import org.eclipse.incquery.runtime.base.api.BaseIndexOptions;
import org.eclipse.incquery.runtime.base.api.NavigationHelper;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

/**
 * An IncQuery scope consisting of EMF objects contained in multiple {@link ResourceSet}s, a single {@link ResourceSet}, {@link Resource} or a containment subtree below a given {@link EObject}.
 * 
 * <p> The scope is characterized by a root and some options (see {@link BaseIndexOptions}) such as dynamic EMF mode, subtree filtering etc.
 * <p>
 * The scope of pattern matching will be the given EMF model root(s) and below (see FAQ for more precise definition).
 * @author Bergmann Gabor
 *
 */
public class EMFScope extends IncQueryScope {
    
    private Set<? extends Notifier> scopeRoots;
    private BaseIndexOptions options;
    
    /**
     * Creates an EMF scope at the given root, with default options (recommended for most users).
     * @param scopeRoot the root of the EMF scope
     * @throws IncQueryException if scopeRoot is not an EMF ResourceSet, Resource or EObject
     */
    public EMFScope(Notifier scopeRoot) throws IncQueryException {
        this(ImmutableSet.of(scopeRoot), new BaseIndexOptions());
    }

    /**
     * Creates an EMF scope at the given root, with customizable options.
     * <p> Most users should consider {@link #EMFScope(Notifier)} instead.
     * @param scopeRoot the root of the EMF scope
     * @param options the base index building settings
     * @throws IncQueryException if scopeRoot is not an EMF ResourceSet, Resource or EObject
     */
    public EMFScope(Notifier scopeRoot, BaseIndexOptions options) throws IncQueryException {
        this(ImmutableSet.of(scopeRoot), options);
    }

    /**
     * Creates an EMF scope at the given roots, with default options (recommended for most users).
     * @param scopeRoots the roots of the EMF scope, must be {@link ResourceSet}s
     * @throws IncQueryException if not all scopeRoots are {@link ResourceSet}s
     */
    public EMFScope(Set<? extends ResourceSet> scopeRoots) throws IncQueryException {
        this(scopeRoots, new BaseIndexOptions());
    }

    /**
     * Creates an EMF scope at the given roots, with customizable options.
     * <p> Most users should consider {@link #EMFScope(Set)} instead.
     * @param scopeRoots the roots of the EMF scope, must be {@link ResourceSet}s
     * @param options the base index building settings
     * @throws IncQueryException if not all scopeRoots are {@link ResourceSet}s
     */
    public EMFScope(Set<? extends Notifier> scopeRoots, BaseIndexOptions options) throws IncQueryException {
        super();
        if (scopeRoots.isEmpty()) {
            throw new IllegalArgumentException("No scope roots given");
        } else if (scopeRoots.size() == 1) {
            checkScopeRoots(scopeRoots, Predicates.or(ImmutableSet.of(Predicates.instanceOf(EObject.class), Predicates.instanceOf(Resource.class), Predicates.instanceOf(ResourceSet.class))));
        } else {
            checkScopeRoots(scopeRoots, Predicates.instanceOf(ResourceSet.class));
        }
        this.scopeRoots = ImmutableSet.copyOf(scopeRoots);
        this.options = options.copy();
    }

    private void checkScopeRoots(Set<? extends Notifier> scopeRoots, Predicate<Object> predicate) throws IncQueryException {
        for (Notifier scopeRoot : scopeRoots) {
            if (!predicate.apply(scopeRoot))
                throw new IncQueryException(IncQueryException.INVALID_EMFROOT
                        + (scopeRoot == null ? "(null)" : scopeRoot.getClass().getName()),
                        IncQueryException.INVALID_EMFROOT_SHORT);
        }
    }

    /**
     * @return the scope roots ({@link ResourceSet}s) containing the model
     */
    public Set<? extends Notifier> getScopeRoots() {
        return scopeRoots;
    }
    
    /**
     * @return the scope root ({@link ResourceSet}, {@link Resource} or {@link EObject}) containing the model, if there is only 1
     * @throws IllegalStateException if there is more than 1 root
     * @deprecated use {@link #getScopeRoots()} instead
     */
    @Deprecated
    public Notifier getScopeRoot() {
        if (scopeRoots.size() == 1) {
            return scopeRoots.iterator().next();
        } else {
            throw new IllegalStateException("This is a multi-root scope");
        }
    }
    
    /**
     * @return the options
     */
    public BaseIndexOptions getOptions() {
        return options.copy();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        result = prime * result
                + ((scopeRoots == null) ? 0 : scopeRoots.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EMFScope))
            return false;
        EMFScope other = (EMFScope) obj;
        if (options == null) {
            if (other.options != null)
                return false;
        } else if (!options.equals(other.options))
            return false;
        if (scopeRoots == null) {
            if (other.scopeRoots != null)
                return false;
        } else if (!scopeRoots.equals(other.scopeRoots))
            return false;
        return true;
    }   
    

    @Override
    public String toString() {
        return String.format("EMFScope(%s):%s", options, scopeRoots);
    }

    @Override
    protected IEngineContext createEngineContext(IncQueryEngine engine, IIndexingErrorListener errorListener, Logger logger) {
        return new EMFEngineContext(this, engine, errorListener, logger);
    }

    /**
     * Provides access to the underlying EMF model index ({@link NavigationHelper}) from an IncQuery engine instantiated on an EMFScope
     * 
     * @param engine an already existing EMF-IncQuery engine instantiated on an EMFScope
     * @return the underlying EMF base index that indexes the contents of the EMF model
     * @throws IncQueryException if base index initialization fails
     */
    public static NavigationHelper extractUnderlyingEMFIndex(IncQueryEngine engine) throws IncQueryException {
        final IncQueryScope scope = engine.getScope();
         if (scope instanceof EMFScope)
             return ((EMFBaseIndexWrapper)AdvancedIncQueryEngine.from(engine).getBaseIndex()).getNavigationHelper();
         else throw new IllegalArgumentException("Cannot extract EMF base index from IncQuery engine instantiated on non-EMF scope " + scope);
    }
    
}
