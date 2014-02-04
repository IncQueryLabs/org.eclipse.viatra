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

package org.eclipse.incquery.runtime.rete.construction.quasitree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.incquery.runtime.matchers.IPatternMatcherContext;
import org.eclipse.incquery.runtime.matchers.planning.IOperationCompiler;
import org.eclipse.incquery.runtime.matchers.planning.IQueryPlannerStrategy;
import org.eclipse.incquery.runtime.matchers.planning.QueryPlannerException;
import org.eclipse.incquery.runtime.matchers.planning.SubPlan;
import org.eclipse.incquery.runtime.matchers.planning.SubPlanProcessor;
import org.eclipse.incquery.runtime.matchers.planning.helpers.BuildHelper;
import org.eclipse.incquery.runtime.matchers.psystem.DeferredPConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.EnumerablePConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.PBody;
import org.eclipse.incquery.runtime.matchers.psystem.PBodyNormalizer;
import org.eclipse.incquery.runtime.matchers.psystem.PQuery;
import org.eclipse.incquery.runtime.rete.construction.RetePatternBuildException;
import org.eclipse.incquery.runtime.rete.util.Options;

/**
 * Layout ideas: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=398763
 * 
 * @author Gabor Bergmann
 * 
 */
public class QuasiTreeLayout implements IQueryPlannerStrategy {

    @Override
    public SubPlan layout(PBody pSystem, IOperationCompiler<?> compiler, IPatternMatcherContext context)
            throws QueryPlannerException {
        return new Scaffold(pSystem, compiler, context).run();
    }

	public class Scaffold {
        PBody pSystem;
        PQuery query;
        IPatternMatcherContext context;
        IOperationCompiler<?> compiler;
        SubPlanProcessor planProcessor = new SubPlanProcessor();

        Set<DeferredPConstraint> deferredConstraints = null;
        Set<EnumerablePConstraint> enumerableConstraints = null;
        Set<SubPlan> forefront = new LinkedHashSet<SubPlan>();

        Scaffold(PBody pSystem, IOperationCompiler<?> compiler, IPatternMatcherContext context) {
            this.pSystem = pSystem;
            this.context = context;
            query = pSystem.getPattern();
            this.compiler = compiler;
            planProcessor.setCompiler(compiler);
        }

        /**
         * @return
         */
        public SubPlan run() throws QueryPlannerException {
            try {
                context.logDebug(String.format(
                		"%s: patternbody build started for %s",
                		getClass().getSimpleName(), 
                		query.getFullyQualifiedName()));

                // PROCESS CONSTRAINTS
                deferredConstraints = pSystem.getConstraintsOfType(DeferredPConstraint.class);
                enumerableConstraints = pSystem.getConstraintsOfType(EnumerablePConstraint.class);
                for (EnumerablePConstraint enumerable : enumerableConstraints) {
                    SubPlan plan = planProcessor.processEnumerableConstraint(enumerable);
                    admitSubPlan(plan);
                }
                if (enumerableConstraints.isEmpty()) { // EXTREME CASE
                    SubPlan plan = compiler.buildStartingPlan(new Object[] {}, new Object[] {});
                    admitSubPlan(plan);
                }

                // JOIN FOREFRONT PLANS WHILE POSSIBLE
                while (forefront.size() > 1) {
                    // TODO QUASI-TREE TRIVIAL JOINS?

                    List<JoinCandidate> candidates = generateJoinCandidates();
                    JoinOrderingHeuristics ordering = new JoinOrderingHeuristics();
                    JoinCandidate selectedJoin = Collections.min(candidates, ordering);
                    doJoin(selectedJoin.getPrimary(), selectedJoin.getSecondary());
                }

                // FINAL CHECK, whether all exported variables are present
                assert (forefront.size() == 1);
                SubPlan finalPlan = forefront.iterator().next();
                BuildHelper.finalCheck(pSystem, finalPlan, context);

                context.logDebug(String.format(
                		"%s: patternbody build concluded for %s",
                		getClass().getSimpleName(), 
                		query.getFullyQualifiedName()));
               return finalPlan;
            } catch (RetePatternBuildException ex) {
                ex.setPatternDescription(query);
                throw ex;
            }
        }

        public List<JoinCandidate> generateJoinCandidates() {
            List<JoinCandidate> candidates = new ArrayList<JoinCandidate>();
            int bIndex = 0;
            for (SubPlan b : forefront) {
                int aIndex = 0;
                for (SubPlan a : forefront) {
                    if (aIndex++ >= bIndex)
                        break;
                    candidates.add(new JoinCandidate(a, b));
                }
                bIndex++;
            }
            return candidates;
        }

        private void admitSubPlan(SubPlan plan) throws QueryPlannerException {
        	// are there any variables that will not be needed anymore and are worth trimming?
        	// (check only if there are unenforced enumerables, so that there are still upcoming joins)
        	if (Options.planTrimOption != Options.PlanTrimOption.OFF &&
        			!plan.getAllEnforcedConstraints().containsAll(enumerableConstraints)) {
        		final SubPlan trimmed = BuildHelper.trimUnneccessaryVariables(compiler, plan, true);
				plan = trimmed;
        	}        	
        	// are there any checkable constraints?
            for (DeferredPConstraint deferred : deferredConstraints) {
                if (!plan.getAllEnforcedConstraints().contains(deferred)) {
                    if (deferred.isReadyAt(plan, context)) {
                        admitSubPlan(planProcessor.processDeferredConstraint(deferred, plan));
                        return;
                    }
                }
            }
            // if no checkable constraints and no unused variables
            forefront.add(plan);
        }

        private void doJoin(SubPlan primaryPlan, SubPlan secondaryPlan)
                throws QueryPlannerException {
            SubPlan joinedPlan = BuildHelper.naturalJoin(compiler, primaryPlan, secondaryPlan);
            forefront.remove(primaryPlan);
            forefront.remove(secondaryPlan);
            admitSubPlan(joinedPlan);
        }

    }

}
