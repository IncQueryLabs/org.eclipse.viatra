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

package org.eclipse.viatra.query.runtime.api.impl;

import java.util.List;

import org.eclipse.viatra.query.runtime.api.IPatternMatch;
import org.eclipse.viatra.query.runtime.api.IQuerySpecification;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.ViatraQueryMatcher;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;
import org.eclipse.viatra.query.runtime.matchers.psystem.annotations.PAnnotation;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PParameter;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PProblem;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PQuery;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.QueryInitializationException;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PQuery.PQueryStatus;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * Base implementation of IQuerySpecification.
 *
 * @author Gabor Bergmann
 *
 */
public abstract class BaseQuerySpecification<Matcher extends ViatraQueryMatcher<? extends IPatternMatch>> implements
        IQuerySpecification<Matcher> {

    /**
     * @since 1.6
     */
    protected static ViatraQueryException processInitializerError(ExceptionInInitializerError err) {
        Throwable cause1 = err.getCause();
        if (cause1 instanceof RuntimeException) {
            Throwable cause2 = ((RuntimeException) cause1).getCause();
            if (cause2 instanceof ViatraQueryException) {
                return (ViatraQueryException) cause2;
            } else if (cause2 instanceof QueryInitializationException) {
                return new ViatraQueryException((QueryInitializationException) cause2);
            } 
        }
        throw err;
    }
    protected final PQuery wrappedPQuery;
    
    protected abstract Matcher instantiate(ViatraQueryEngine engine) throws ViatraQueryException;

    /**
     * For backward compatibility of code generated with previous versions of viatra query, this method has a default
     * implementation returning null, indicating that a matcher can only be created using the old method, which ignores
     * the hints provided by the user.
     * 
     * @since 1.4
     */
    @Override
    public Matcher instantiate() throws ViatraQueryException{
        return null;
    }
    
    
    /**
     * Instantiates query specification for the given internal query representation.
     */
    public BaseQuerySpecification(PQuery wrappedPQuery) {
        super();
        this.wrappedPQuery = wrappedPQuery;
        wrappedPQuery.publishedAs().add(this);
    }


    @Override
    public PQuery getInternalQueryRepresentation() {
        return wrappedPQuery;
    }

    @Override
    public Matcher getMatcher(ViatraQueryEngine engine) throws ViatraQueryException {
        ensureInitializedInternal();
        if (wrappedPQuery.getStatus() == PQueryStatus.ERROR) {

            String errorMessages = Joiner.on("\n")
                    .join(Iterables.transform(wrappedPQuery.getPProblems(), new Function<PProblem, String>() {

                        @Override
                        public String apply(PProblem input) {
                            return (input == null) ? "" : input.getShortMessage();
                        }

                    }));

            throw new ViatraQueryException(String.format("Erroneous query specification: %s %n %s", getFullyQualifiedName(), errorMessages),
                    "Cannot initialize matchers on erroneous query specifications.");
        } else if (!engine.getScope().isCompatibleWithQueryScope(this.getPreferredScopeClass())) {
            throw new ViatraQueryException(
                    String.format(
                            "Scope class incompatibility: the query %s is formulated over query scopes of class %s, "
                                    + " thus the query engine formulated over scope %s of class %s cannot evaluate it.",
                            this.getFullyQualifiedName(), this.getPreferredScopeClass().getCanonicalName(),
                            engine.getScope(), engine.getScope().getClass().getCanonicalName()),
                    "Incompatible scope classes of engine and query.");
        }
        return instantiate(engine);
    }

    protected void ensureInitializedInternal() throws ViatraQueryException {
        try {
            wrappedPQuery.ensureInitialized();
        } catch (QueryInitializationException e) {
            throw new ViatraQueryException(e);
        }
    }
    protected void ensureInitializedInternalSneaky() {
        try {
            wrappedPQuery.ensureInitialized();
        } catch (QueryInitializationException e) {
            throw new RuntimeException(e);
        }
    }
    
    

    // // EXPERIMENTAL
    //
    // @Override
    // public Matcher getMatcher(TransactionalEditingDomain trDomain) throws ViatraQueryException {
    // return getMatcher(trDomain, 0);
    // }
    //
    // @Override
    // public Matcher getMatcher(TransactionalEditingDomain trDomain, int numThreads) throws ViatraQueryException {
    // try {
    // ViatraQueryEngine engine = EngineManager.getInstance().getReteEngine(trDomain, numThreads);
    // return instantiate(engine);
    // } catch (RetePatternBuildException e) {
    // throw new ViatraQueryException(e);
    // }
    // }
    
    
    // // DELEGATIONS
    
    @Override
    public List<PAnnotation> getAllAnnotations() {
        return wrappedPQuery.getAllAnnotations();
    }
    @Override
    public List<PAnnotation> getAnnotationsByName(String annotationName) {
        return wrappedPQuery.getAnnotationsByName(annotationName);
    }
    @Override
    public PAnnotation getFirstAnnotationByName(String annotationName) {
        return wrappedPQuery.getFirstAnnotationByName(annotationName);
    }
    @Override
    public String getFullyQualifiedName() {
        return wrappedPQuery.getFullyQualifiedName();
    }
    @Override
    public List<String> getParameterNames() {
        return wrappedPQuery.getParameterNames();
    }
    @Override
    public List<PParameter> getParameters() {
        return wrappedPQuery.getParameters();
    }
    @Override
    public Integer getPositionOfParameter(String parameterName) {
        return wrappedPQuery.getPositionOfParameter(parameterName);
    }

}
