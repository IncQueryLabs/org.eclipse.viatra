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
package org.eclipse.viatra.query.runtime.emf;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.scope.IEngineContext;
import org.eclipse.viatra.query.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.viatra.query.runtime.api.scope.QueryScope;
import org.eclipse.viatra.query.runtime.base.api.BaseIndexOptions;
import org.eclipse.viatra.query.runtime.base.api.IEMFBaseIndex;
import org.eclipse.viatra.query.runtime.base.api.NavigationHelper;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

/**
 * An {@link QueryScope} consisting of EMF objects contained in multiple {@link ResourceSet}s, a single {@link ResourceSet}, {@link Resource} or a containment subtree below a given {@link EObject}.
 * 
 * <p> The scope is characterized by a root and some options (see {@link BaseIndexOptions}) such as dynamic EMF mode, subtree filtering etc.
 * <p>
 * The scope of pattern matching will be the given EMF model root(s) and below (see FAQ for more precise definition).
 * 
 * <p> Note on <i>cross-resource containment</i>: in case of {@link EObject} scopes, cross-resource containments will be considered part of the scope.
 * The same goes for {@link ResourceSet} scopes, provided that the resource of the contained element is successfully loaded into the resource set. 
 * In case of a {@link Resource} scope, containments pointing out from the resource will be excluded from the scope. 
 * Thus the boundaries of {@link EObject} scopes conform to the notion of EMF logical containment, while {@link Resource} and {@link ResourceSet} scopes adhere to persistence boundaries.
 * 
 * @author Bergmann Gabor
 *
 */
public class EMFScope extends QueryScope {
    
    private Set<? extends Notifier> scopeRoots;
    private BaseIndexOptions options;
    
    /**
     * Creates an EMF scope at the given root, with default options (recommended for most users).
     * @param scopeRoot the root of the EMF scope
     * @throws ViatraQueryException- if scopeRoot is not an EMF ResourceSet, Resource or EObject
     */
    public EMFScope(Notifier scopeRoot) throws ViatraQueryException {
        this(ImmutableSet.of(scopeRoot), new BaseIndexOptions());
    }

    /**
     * Creates an EMF scope at the given root, with customizable options.
     * <p> Most users should consider {@link #EMFScope(Notifier)} instead.
     * @param scopeRoot the root of the EMF scope
     * @param options the base index building settings
     * @throws ViatraQueryException if scopeRoot is not an EMF ResourceSet, Resource or EObject
     */
    public EMFScope(Notifier scopeRoot, BaseIndexOptions options) throws ViatraQueryException {
        this(ImmutableSet.of(scopeRoot), options);
    }

    /**
     * Creates an EMF scope at the given roots, with default options (recommended for most users).
     * @param scopeRoots the roots of the EMF scope, must be {@link ResourceSet}s
     * @throws ViatraQueryException if not all scopeRoots are {@link ResourceSet}s
     */
    public EMFScope(Set<? extends ResourceSet> scopeRoots) throws ViatraQueryException {
        this(scopeRoots, new BaseIndexOptions());
    }

    /**
     * Creates an EMF scope at the given roots, with customizable options.
     * <p> Most users should consider {@link #EMFScope(Set)} instead.
     * @param scopeRoots the roots of the EMF scope, must be {@link ResourceSet}s
     * @param options the base index building settings
     * @throws ViatraQueryException if not all scopeRoots are {@link ResourceSet}s
     */
    public EMFScope(Set<? extends Notifier> scopeRoots, BaseIndexOptions options) throws ViatraQueryException {
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

    private void checkScopeRoots(Set<? extends Notifier> scopeRoots, Predicate<Object> predicate) throws ViatraQueryException {
        for (Notifier scopeRoot : scopeRoots) {
            if (!predicate.apply(scopeRoot))
                throw new ViatraQueryException(ViatraQueryException.INVALID_EMFROOT
                        + (scopeRoot == null ? "(null)" : scopeRoot.getClass().getName()),
                        ViatraQueryException.INVALID_EMFROOT_SHORT);
        }
    }

    /**
     * @return the scope roots ({@link ResourceSet}s) containing the model
     */
    public Set<? extends Notifier> getScopeRoots() {
        return scopeRoots;
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
    protected IEngineContext createEngineContext(ViatraQueryEngine engine, IIndexingErrorListener errorListener, Logger logger) {
        return EMFEngineContext.create(this, options.getSurrogateServiceFactory(), engine, errorListener, logger);
    }

    /**
     * Provides access to the underlying EMF model index ({@link NavigationHelper}) from a VIATRA Query engine instantiated on an EMFScope
     * 
     * @param engine an already existing VIATRA Query engine instantiated on an EMFScope
     * @return the underlying EMF base index that indexes the contents of the EMF model
     * @throws ViatraQueryException if base index initialization fails
     * @deprecated use {@link #extractUnderlyingEMFBaseIndex(ViatraQueryEngine)} instead
     */
    public static NavigationHelper extractUnderlyingEMFIndex(ViatraQueryEngine engine) throws ViatraQueryException {
        return extractUnderlyingEMFBaseIndex(engine).getNavigationHelper();
    }
    /**
     * Provides access to the underlying EMF model index ({@link IEMFBaseIndex}) from a VIATRA Query engine instantiated on an EMFScope
     * 
     * @param engine an already existing VIATRA Query engine instantiated on an EMFScope
     * @return the underlying EMF base index that indexes the contents of the EMF model
     * @throws ViatraQueryException if base index initialization fails
     * @since 1.7
     */
    public static IEMFBaseIndex<?> extractUnderlyingEMFBaseIndex(ViatraQueryEngine engine) throws ViatraQueryException {
        final QueryScope scope = engine.getScope();
         if (scope instanceof EMFScope)
             return ((EMFBaseIndexWrapper<?>)AdvancedViatraQueryEngine.from(engine).getBaseIndex()).getWrapped();
         else throw new IllegalArgumentException("Cannot extract EMF base index from VIATRA Query engine instantiated on non-EMF scope " + scope);
    }
    
}
