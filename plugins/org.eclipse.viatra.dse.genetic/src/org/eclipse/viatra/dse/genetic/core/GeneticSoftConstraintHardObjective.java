/*******************************************************************************
 * Copyright (c) 2010-2015, Andras Szabolcs Nagy and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *   Andras Szabolcs Nagy - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.dse.genetic.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.viatra.dse.api.DSEException;
import org.eclipse.viatra.dse.base.ThreadContext;
import org.eclipse.viatra.dse.objectives.Comparators;
import org.eclipse.viatra.dse.objectives.IObjective;
import org.eclipse.viatra.dse.objectives.impl.BaseObjective;

public class GeneticSoftConstraintHardObjective extends BaseObjective {

    public static final String DEFAULT_NAME = "SoftConstraints";

    protected List<IQuerySpecification<? extends IncQueryMatcher<? extends IPatternMatch>>> constraints;
    protected List<Double> weights;
    protected List<String> names;

    protected List<IncQueryMatcher<? extends IPatternMatch>> matchers = new ArrayList<IncQueryMatcher<? extends IPatternMatch>>();
    protected List<Integer> matches;

    public GeneticSoftConstraintHardObjective() {
        super(DEFAULT_NAME);

        this.constraints = new ArrayList<IQuerySpecification<? extends IncQueryMatcher<? extends IPatternMatch>>>();
        this.weights = new ArrayList<Double>();
        this.names = new ArrayList<String>();
        
        comparator = Comparators.LOWER_IS_BETTER;
        level = 1;
    }

    public GeneticSoftConstraintHardObjective withConstraint(String name,
            IQuerySpecification<? extends IncQueryMatcher<? extends IPatternMatch>> constraint, double weight) {
        constraints.add(constraint);
        weights.add(new Double(weight));
        names.add(name);
        return this;
    }

    @Override
    public Double getFitness(ThreadContext context) {

        double result = 0;

        for (int i = 0; i < constraints.size(); i++) {
            int countMatches = matchers.get(i).countMatches();
            result += countMatches * weights.get(i);
            matches.set(i, new Integer(countMatches));
        }

        return new Double(result);
    }

    @Override
    public void init(ThreadContext context) {
        matches = new ArrayList<Integer>(constraints.size());
        for (IQuerySpecification<?> qs : constraints) {
            matches.add(0);
        }
        try {
            IncQueryEngine incQueryEngine = context.getIncqueryEngine();

            for (IQuerySpecification<? extends IncQueryMatcher<? extends IPatternMatch>> querySpecification : constraints) {
                IncQueryMatcher<? extends IPatternMatch> matcher = querySpecification.getMatcher(incQueryEngine);
                matchers.add(matcher);
            }

        } catch (IncQueryException e) {
            throw new DSEException("Couldn't initialize the incquery matcher, see inner exception", e);
        }
    }

    @Override
    public IObjective createNew() {
        GeneticSoftConstraintHardObjective result = new GeneticSoftConstraintHardObjective();
        result.constraints = constraints;
        result.names = names;
        result.weights = weights;
        return result;
    }

    @Override
    public boolean isHardObjective() {
        return true;
    }

    @Override
    public boolean satisifiesHardObjective(Double fitness) {
        return fitness < 0.0001 ? true : false;
    }

    public List<Integer> getMatches() {
        return matches;
    }

    public List<String> getNames() {
        return names;
    }
    
    @Override
    public void setComparator(Comparator<Double> comparator) {
        throw new UnsupportedOperationException("");
    }
    
    @Override
    public BaseObjective withComparator(Comparator<Double> comparator) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public BaseObjective withLevel(int level) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void setLevel(int level) {
        throw new UnsupportedOperationException("");
    }
    
}
