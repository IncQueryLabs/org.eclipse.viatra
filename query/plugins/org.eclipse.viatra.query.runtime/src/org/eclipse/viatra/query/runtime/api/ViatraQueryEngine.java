/*******************************************************************************
 * Copyright (c) 2004-2010 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra.query.runtime.api;

import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.viatra.query.runtime.api.scope.IBaseIndex;
import org.eclipse.viatra.query.runtime.api.scope.QueryScope;
import org.eclipse.viatra.query.runtime.base.api.BaseIndexOptions;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

/**
 * A Viatra Query (incremental) evaluation engine, attached to a model such as an EMF resource. The engine hosts pattern matchers, and
 * will listen on model update notifications stemming from the given model in order to maintain live results.
 * 
 * <p>
 * By default, ViatraQueryEngines do not need to be separately disposed; they will be garbage collected along with the model. 
 * Advanced users: see {@link AdvancedViatraQueryEngine} if you want fine control over the lifecycle of an engine.
 * 
 * <p>
 * Pattern matchers within this engine may be instantiated in the following ways:
 * <ul>
 * <li>Recommended: instantiate the specific matcher class generated for the pattern by e.g. MyPatternMatcher.on(engine).
 * <li>Use {@link #getMatcher(IQuerySpecification)} if the pattern-specific generated matcher API is not available.
 * <li>Advanced: use the query specification associated with the generated matcher class to achieve the same.
 * </ul>
 * Additionally, a group of patterns (see {@link IQueryGroup}) can be initialized together before usage; this may improve
 * the performance of pattern matcher construction by trying to gather all necessary information from the model in one go. 
 * Note that no such improvement is to be expected if the engine is specifically constructed in wildcard mode, 
 * an option available in some scope implementations  
 * (see {@link EMFScope#EMFScope(Notifier, BaseIndexOptions)} and {@link BaseIndexOptions#withWildcardMode(boolean)}).
 * 
 * 
 * @author Bergmann Gábor
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ViatraQueryEngine {
    
   
    /**
     * Obtain a (managed) {@link ViatraQueryEngine} to evaluate queries over a given scope specified by an {@link QueryScope}.
     * 
     * <p> For a given matcher scope, the same engine will be returned to any client. 
     * This facilitates the reuse of internal caches of the engine, greatly improving performance.  
     * 
     * <p> The lifecycle of this engine is centrally managed, and will not be disposed as long as the model is retained in memory. 
     * The engine will be garbage collected along with the model. 
     * 
     * <p>
     * Advanced users: see {@link AdvancedViatraQueryEngine#createUnmanagedEngine(QueryScope)} to obtain a private, 
     * unmanaged engine that is not shared with other clients and allows tight control over its lifecycle. 
     * 
     * @param scope 
     * 		the scope of query evaluation; the definition of the set of model elements that this engine is operates on. 
     * 		Provide e.g. a {@link EMFScope} for evaluating queries on an EMF model.
     * @return a (managed) {@link ViatraQueryEngine} instance
     * @throws ViatraQueryException on initialization errors.
     */
    public static ViatraQueryEngine on(QueryScope scope) throws ViatraQueryException {
        return ViatraQueryEngineManager.getInstance().getQueryEngine(scope);
    }
    
    /**
     * Obtain a (managed) {@link ViatraQueryEngine} to evaluate queries over a given scope specified by an {@link QueryScope}.
     * 
     * <p> For a given matcher scope, the same engine will be returned to any client. 
     * This facilitates the reuse of internal caches of the engine, greatly improving performance.  
     * 
     * <p> The lifecycle of this engine is centrally managed, and will not be disposed as long as the model is retained in memory. 
     * The engine will be garbage collected along with the model. 
     * 
     * <p>
     * Advanced users: see {@link AdvancedViatraQueryEngine#createUnmanagedEngine(QueryScope)} to obtain a private, 
     * unmanaged engine that is not shared with other clients and allows tight control over its lifecycle. 
     * 
     * @param scope 
     *      the scope of query evaluation; the definition of the set of model elements that this engine is operates on. 
     *      Provide e.g. a {@link EMFScope} for evaluating queries on an EMF model.
     * @return a (managed) {@link ViatraQueryEngine} instance
     * @throws ViatraQueryException on initialization errors.
     * @since 1.4
     */
    public static ViatraQueryEngine on(QueryScope scope, ViatraQueryEngineOptions options) throws ViatraQueryException {
        return ViatraQueryEngineManager.getInstance().getQueryEngine(scope, options);
    }

    /**
     * Provides access to the internal base index component of the engine, responsible for keeping track of basic
     * contents of the model.
     * 
     * <p>If using an {@link EMFScope}, 
     *  consider {@link EMFScope#extractUnderlyingEMFBaseIndex(ViatraQueryEngine)} instead to access EMF-specific details.
     * 
     * @return the baseIndex the NavigationHelper maintaining the base index
     * @throws ViatraQueryException
     *             if the base index could not be constructed
     */
    public abstract IBaseIndex getBaseIndex() throws ViatraQueryException;

    /**
     * Access a pattern matcher based on a {@link IQuerySpecification}. 
     * Multiple calls will return the same matcher.
     * @param querySpecification a {@link IQuerySpecification} that describes a VIATRA query specification
     * @return a pattern matcher corresponding to the specification
     * @throws ViatraQueryException if the matcher could not be initialized
     */
    public abstract <Matcher extends ViatraQueryMatcher<? extends IPatternMatch>> Matcher getMatcher(IQuerySpecification<Matcher> querySpecification) throws ViatraQueryException;

    /**
     * Access a pattern matcher for the graph pattern with the given fully qualified name. 
     * Will succeed only if a query specification for this fully qualified name has been generated and registered. 
     * Multiple calls will return the same matcher unless the registered specification changes. 
     * 
     * @param patternFQN the fully qualified name of a VIATRA query specification
     * @return a pattern matcher corresponding to the specification
     * @throws ViatraQueryException if the matcher could not be initialized
     */
    public abstract ViatraQueryMatcher<? extends IPatternMatch> getMatcher(String patternFQN) throws ViatraQueryException;
    
    /**
     * Access an existing pattern matcher based on a {@link IQuerySpecification}.
     * @param querySpecification a {@link IQuerySpecification} that describes a VIATRA query specification
     * @return a pattern matcher corresponding to the specification, <code>null</code> if a matcher does not exist yet.
     */
    public abstract <Matcher extends ViatraQueryMatcher<? extends IPatternMatch>> Matcher getExistingMatcher(IQuerySpecification<Matcher> querySpecification);

    
    /**
     * Access a copy of available {@link ViatraQueryMatcher} pattern matchers.
     * @return a copy of the set of currently available pattern matchers registered on this engine instance
     */
    public abstract Set<? extends ViatraQueryMatcher<? extends IPatternMatch>> getCurrentMatchers();
    
    public Set<IQuerySpecification<? extends ViatraQueryMatcher<? extends IPatternMatch>>> getRegisteredQuerySpecifications() {
        return Sets.newHashSet(Collections2.transform(getCurrentMatchers(), new Function<ViatraQueryMatcher<?>, IQuerySpecification<?>>() {

            @Override
            public IQuerySpecification<?> apply(ViatraQueryMatcher<?> arg0) {
                return arg0.getSpecification();
            }
        }));
    }

    /**
     * @return the scope of query evaluation; the definition of the set of model elements that this engine is operates on.
     */
    public abstract QueryScope getScope();
        
}
