/*******************************************************************************
 * Copyright (c) 2010-2017, Zoltan Ujhelyi, IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.localsearch.matcher.integration;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;
import org.eclipse.viatra.query.runtime.localsearch.MatchingFrame;
import org.eclipse.viatra.query.runtime.localsearch.exceptions.LocalSearchException;
import org.eclipse.viatra.query.runtime.localsearch.matcher.ISearchContext;
import org.eclipse.viatra.query.runtime.localsearch.matcher.LocalSearchMatcher;
import org.eclipse.viatra.query.runtime.localsearch.matcher.MatcherReference;
import org.eclipse.viatra.query.runtime.localsearch.plan.IPlanDescriptor;
import org.eclipse.viatra.query.runtime.localsearch.plan.IPlanProvider;
import org.eclipse.viatra.query.runtime.localsearch.plan.SearchPlan;
import org.eclipse.viatra.query.runtime.localsearch.plan.SearchPlanExecutor;
import org.eclipse.viatra.query.runtime.localsearch.planner.compiler.IOperationCompiler;
import org.eclipse.viatra.query.runtime.localsearch.planner.util.SearchPlanForBody;
import org.eclipse.viatra.query.runtime.matchers.backend.IMatcherCapability;
import org.eclipse.viatra.query.runtime.matchers.backend.IQueryBackend;
import org.eclipse.viatra.query.runtime.matchers.backend.IQueryResultProvider;
import org.eclipse.viatra.query.runtime.matchers.backend.IUpdateable;
import org.eclipse.viatra.query.runtime.matchers.backend.QueryEvaluationHint;
import org.eclipse.viatra.query.runtime.matchers.backend.QueryHintOption;
import org.eclipse.viatra.query.runtime.matchers.context.IInputKey;
import org.eclipse.viatra.query.runtime.matchers.context.IQueryBackendContext;
import org.eclipse.viatra.query.runtime.matchers.context.IQueryRuntimeContext;
import org.eclipse.viatra.query.runtime.matchers.context.IndexingService;
import org.eclipse.viatra.query.runtime.matchers.planning.QueryProcessingException;
import org.eclipse.viatra.query.runtime.matchers.psystem.PBody;
import org.eclipse.viatra.query.runtime.matchers.psystem.PConstraint;
import org.eclipse.viatra.query.runtime.matchers.psystem.basicenumerables.PositivePatternCall;
import org.eclipse.viatra.query.runtime.matchers.psystem.basicenumerables.TypeConstraint;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PParameter;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PQuery;
import org.eclipse.viatra.query.runtime.matchers.psystem.rewriters.IFlattenCallPredicate;
import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Zoltan Ujhelyi
 * @since 1.7
 *
 */
public abstract class AbstractLocalSearchResultProvider implements IQueryResultProvider {

    protected final LocalSearchBackend backend;
    protected final IQueryBackendContext backendContext;
    protected final IQueryRuntimeContext runtimeContext;
    protected final PQuery query;
    protected final QueryEvaluationHint userHints;
    protected final IPlanProvider planProvider;
    protected final ISearchContext searchContext;

    /**
     * @since 1.5
     */
    public AbstractLocalSearchResultProvider(LocalSearchBackend backend, IQueryBackendContext context, PQuery query,
            IPlanProvider planProvider, QueryEvaluationHint userHints) {
        this.backend = backend;
        this.backendContext = context;
        this.query = query;

        this.planProvider = planProvider;
        this.userHints = userHints;
        this.runtimeContext = context.getRuntimeContext();
        this.searchContext = new ISearchContext.SearchContext(backendContext, userHints, backend.getCache());
    }
    
    protected abstract IOperationCompiler getOperationCompiler(IQueryBackendContext backendContext, LocalSearchHints configuration);
    
    private IQueryRuntimeContext getRuntimeContext() {
        return backend.getRuntimeContext();
    }

    private LocalSearchMatcher createMatcher(IPlanDescriptor plan, final ISearchContext searchContext) {
        Collection<SearchPlanForBody> compiledPlans = Lists.newArrayList(plan.getPlan());
    
        Collection<SearchPlanExecutor> executors = Collections2.transform(compiledPlans,
                new Function<SearchPlanForBody, SearchPlanExecutor>() {
    
                    @Override
                    public SearchPlanExecutor apply(SearchPlanForBody input) {
                        final SearchPlan plan = new SearchPlan();
                        plan.addOperations(input.getCompiledOperations());
    
                        return new SearchPlanExecutor(plan, searchContext, input.getVariableKeys());
                    }
                });
    
        final Collection<Integer> parameterSizes = Collections2.transform(compiledPlans,
                new Function<SearchPlanForBody, Integer>() {
    
                    @Override
                    public Integer apply(SearchPlanForBody input) {
                        PBody body = input.getBody();
                        return body.getUniqueVariables().size();
                        // return Math.max(input.getSymbolicParameters().size(), input.getUniqueVariables().size());
                    }
                });
    
        return new LocalSearchMatcher(plan, executors,
                Collections.max(parameterSizes));
    }

    private IPlanDescriptor createPlan(MatcherReference key, IPlanProvider planProvider) throws QueryProcessingException {
        LocalSearchHints configuration = overrideDefaultHints(key.getQuery());
        IOperationCompiler compiler = getOperationCompiler(backendContext, configuration);
        IPlanDescriptor plan = planProvider.getPlan(backendContext, compiler, configuration, key);
        return plan;
    }

    private LocalSearchHints overrideDefaultHints(PQuery pQuery) {
        return LocalSearchHints.getDefaultOverriddenBy(
                computeOverridingHints(pQuery));
    }

    /** 
     * Combine with {@link QueryHintOption#getValueOrDefault(QueryEvaluationHint)} to access 
     *  hint settings not covered by {@link LocalSearchHints} 
     */
    private QueryEvaluationHint computeOverridingHints(PQuery pQuery) {
        return backendContext.getHintProvider().getQueryEvaluationHint(pQuery).overrideBy(userHints);
    }

    private Iterator<MatcherReference> computeExpectedAdornments() {
        return Iterators.transform(overrideDefaultHints(query).getAdornmentProvider().getAdornments(query).iterator(), new Function<Set<PParameter>, MatcherReference>() {
    
            @Override
            public MatcherReference apply(Set<PParameter> input) {
                return new MatcherReference(query, input, userHints);
            }
        });
    }

    /**
     * Prepare this result provider. This phase is separated from the constructor to allow the backend to cache its instance before
     * requesting preparation for its dependencies.
     * @since 1.5
     */
    public void prepare() throws QueryProcessingException {
        try {
            runtimeContext.coalesceTraversals(new Callable<Void>() {
    
                @Override
                public Void call() throws Exception {
                    indexInitializationBeforePlanning();
                    prepareDirectDependencies();
                    runtimeContext.executeAfterTraversal(new Runnable() {
                        
                        @Override
                        public void run() {
                            try {
                                preparePlansForExpectedAdornments();
                            } catch (QueryProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    return null;
                }
            });
        } catch (InvocationTargetException e) {
            throw new QueryProcessingException("Error while building required indexes: %s", new String[]{e.getTargetException().getMessage()}, "Error while building required indexes.", query, e);
        }
    }

    protected void preparePlansForExpectedAdornments() throws QueryProcessingException {
     // Plan for possible adornments
        Iterator<MatcherReference> iterator = computeExpectedAdornments();
        while(iterator.hasNext()){
            LocalSearchHints configuration = overrideDefaultHints(query);
            IOperationCompiler compiler = getOperationCompiler(backendContext, configuration);
            IPlanDescriptor plan = planProvider.getPlan(backendContext, compiler, configuration, iterator.next());
            // Index keys
            try {
                indexKeys(plan.getIteratedKeys());
            } catch (InvocationTargetException e) {
                throw new QueryProcessingException(e.getMessage(), null, e.getMessage(), query, e);
            }
            //Prepare dependencies
            for(SearchPlanForBody body: plan.getPlan()){
                for(MatcherReference dependency : body.getDependencies()){
                    try {
                        searchContext.getMatcher(dependency);
                    } catch (LocalSearchException e) {
                        throw new QueryProcessingException("Could not prepare dependency {1}", new String[]{dependency.toString()}, e.getMessage(), query, e);
                    }
                }
            }
        }
    
    }

    protected void prepareDirectDependencies() throws QueryProcessingException {
        // Do not prepare for any adornment at this point
        IAdornmentProvider adornmentProvider = new IAdornmentProvider() {
            
            @Override
            public Iterable<Set<PParameter>> getAdornments(PQuery query) {
                return Collections.emptySet();
            }
        };
        @SuppressWarnings("rawtypes")
        QueryEvaluationHint hints = new QueryEvaluationHint(Collections.<QueryHintOption, Object>singletonMap(LocalSearchHintOptions.ADORNMENT_PROVIDER, adornmentProvider), null);
        for(PQuery dep : getDirectPositiveDependencies()){
            backendContext.getResultProviderAccess().getResultProvider(dep, hints);
        }
    }

    /**
     * This method is called before planning start to allow indexing. It is important to note that this method is called
     * inside a coalesceTraversals block, meaning (1) it is safe to add multiple registration requests as necessary, but
     * (2) no value or statistics is available from the index.
     * 
     * @throws QueryProcessingException
     */
    protected void indexInitializationBeforePlanning() throws QueryProcessingException {
        // By default, no indexing is necessary
    }
    
    /**
     * Collects and indexes all types _directly_ referred by the PQuery {@link #query}. Types indirect
     * @param requiredIndexingServices
     */
    protected void indexReferredTypesOfQuery(PQuery query, IndexingService requiredIndexingServices) {
        for (PBody body : query.getDisjunctBodies().getBodies()) {
            for (PConstraint constraint : body.getConstraints()) {
                if (constraint instanceof TypeConstraint) {
                    runtimeContext.ensureIndexed(((TypeConstraint) constraint).getSupplierKey(), requiredIndexingServices);
                }
            }
        }
    }
    
    private Set<PQuery> getDirectPositiveDependencies() {
        IFlattenCallPredicate flattenPredicate = overrideDefaultHints(query).getFlattenCallPredicate();
        Queue<PQuery> queue = new LinkedList<>();
        Set<PQuery> visited = new HashSet<>();
        Set<PQuery> result = new HashSet<>();
        queue.add(query);
        
        while(!queue.isEmpty()){
            PQuery next = queue.poll();
            visited.add(next);
            for(PBody body : next.getDisjunctBodies().getBodies()){
                for(PositivePatternCall ppc : body.getConstraintsOfType(PositivePatternCall.class)){
                    PQuery dep = ppc.getSupplierKey();
                    if (flattenPredicate.shouldFlatten(ppc)){
                        if (!visited.contains(dep)){
                            queue.add(dep);
                        }
                    }else{
                        result.add(dep);
                    }
                }
            }
        }
        return result;
    }

    private LocalSearchMatcher initializeMatcher(Object[] parameters) {
        try {
            return newLocalSearchMatcher(parameters);
        } catch (QueryProcessingException | ViatraQueryException e) {
            throw new RuntimeException(e);
        }
    
    }

    public LocalSearchMatcher newLocalSearchMatcher(Object[] parameters) throws ViatraQueryException, QueryProcessingException {
    
        final Set<PParameter> adornment = Sets.newHashSet();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] != null) {
                adornment.add(query.getParameters().get(i));
            }
        }
    
        final MatcherReference reference = new MatcherReference(query, adornment, userHints);
        
        IPlanDescriptor plan = createPlan(reference, planProvider);
        if (overrideDefaultHints(reference.getQuery()).isUseBase()){
            try {
                indexKeys(plan.getIteratedKeys());
            } catch (InvocationTargetException e) {
                throw new ViatraQueryException("Could not index keys","Could not index keys", e);
            }
        }
        
        LocalSearchMatcher matcher = createMatcher(plan, searchContext);
        matcher.addAdapters(backend.getAdapters());
        return matcher;
    }

    private void indexKeys(final Iterable<IInputKey> keys) throws InvocationTargetException {
        final IQueryRuntimeContext qrc = getRuntimeContext();
        qrc.coalesceTraversals(new Callable<Void>() {
    
            @Override
            public Void call() throws Exception {
                for(IInputKey key : keys){
                    if (key.isEnumerable()) {
                        qrc.ensureIndexed(key, IndexingService.INSTANCES);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Tuple getOneArbitraryMatch(Object[] parameters) {
        final LocalSearchMatcher matcher = initializeMatcher(parameters);
        final MatchingFrame frame = matcher.editableMatchingFrame();
        frame.setParameterValues(parameters);
        return matcher.getOneArbitraryMatch(frame);
    }

    @Override
    public int countMatches(Object[] parameters) {
        final LocalSearchMatcher matcher = initializeMatcher(parameters);
        final MatchingFrame frame = matcher.editableMatchingFrame();
        frame.setParameterValues(parameters);
        return matcher.countMatches(frame);
    }

    @Override
    public Collection<? extends Tuple> getAllMatches(Object[] parameters) {
        final LocalSearchMatcher matcher = initializeMatcher(parameters);
        final MatchingFrame frame = matcher.editableMatchingFrame();
        frame.setParameterValues(parameters);
        return matcher.getAllMatches(frame);
    }

    @Override
    public IQueryBackend getQueryBackend() {
        return backend;
    }

    @Override
    public void addUpdateListener(IUpdateable listener, Object listenerTag, boolean fireNow) {
        // throw new UnsupportedOperationException(UPDATE_LISTENER_NOT_SUPPORTED);
    }

    @Override
    public void removeUpdateListener(Object listenerTag) {
        // throw new UnsupportedOperationException(UPDATE_LISTENER_NOT_SUPPORTED);
    }

    /**
     * @since 1.4
     */
    public IMatcherCapability getCapabilites() {
        LocalSearchHints configuration = overrideDefaultHints(query);
        return configuration;
    }

}