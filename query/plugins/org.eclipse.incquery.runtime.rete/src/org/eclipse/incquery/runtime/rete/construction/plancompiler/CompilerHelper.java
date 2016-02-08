/*******************************************************************************
 * Copyright (c) 2010-2014, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.rete.construction.plancompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.incquery.runtime.matchers.planning.SubPlan;
import org.eclipse.incquery.runtime.matchers.psystem.EnumerablePConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.matchers.tuple.TupleMask;
import org.eclipse.incquery.runtime.rete.recipes.AggregatorRecipe;
import org.eclipse.incquery.runtime.rete.recipes.EqualityFilterRecipe;
import org.eclipse.incquery.runtime.rete.recipes.IndexerRecipe;
import org.eclipse.incquery.runtime.rete.recipes.JoinRecipe;
import org.eclipse.incquery.runtime.rete.recipes.Mask;
import org.eclipse.incquery.runtime.rete.recipes.ProductionRecipe;
import org.eclipse.incquery.runtime.rete.recipes.ProjectionIndexerRecipe;
import org.eclipse.incquery.runtime.rete.recipes.RecipesFactory;
import org.eclipse.incquery.runtime.rete.recipes.ReteNodeRecipe;
import org.eclipse.incquery.runtime.rete.recipes.TrimmerRecipe;
import org.eclipse.incquery.runtime.rete.recipes.helper.RecipesHelper;
import org.eclipse.incquery.runtime.rete.traceability.CompiledQuery;
import org.eclipse.incquery.runtime.rete.traceability.CompiledSubPlan;
import org.eclipse.incquery.runtime.rete.traceability.PlanningTrace;
import org.eclipse.incquery.runtime.rete.traceability.RecipeTraceInfo;

/**
 * @author Bergmann Gabor
 *
 */
public class CompilerHelper {
	
	final static RecipesFactory FACTORY = RecipesFactory.eINSTANCE;
		
	/**
	 * Makes sure that all variables in the tuple are different so that it can be used as {@link CompiledSubPlan}.
	 * If a variable occurs multiple times, equality checks are applied and then the results are trimmed so that duplicates are hidden.
	 * If no manipulation is necessary, the original trace is returned.  
	 * 
	 * <p> to be used whenever a constraint introduces new variables.
	 */
	public static PlanningTrace checkAndTrimEqualVariables(
			SubPlan plan, final PlanningTrace coreTrace) 
	{
		// are variables in the constraint all different?
		final List<PVariable> coreVariablesTuple = coreTrace.getVariablesTuple();
		final int constraintArity = coreVariablesTuple.size();
		final int distinctVariables = coreTrace.getPosMapping().size();
		if (constraintArity == distinctVariables) {
			// all variables occur exactly once in tuple
			return coreTrace;
		} else { // apply equality checks and trim
			
			// find the positions in the tuple for each variable
			Map<PVariable, SortedSet<Integer>> posMultimap = new HashMap<PVariable, SortedSet<Integer>>();
			List<PVariable> trimmedVariablesTuple = new ArrayList<PVariable>(distinctVariables);
			int[] trimIndices = new int[distinctVariables]; 
			for (int i = 0; i < constraintArity; ++i) {
				final PVariable variable = coreVariablesTuple.get(i);
				SortedSet<Integer> indexSet = posMultimap.get(variable);
				if (indexSet == null) { // first occurrence of variable
					indexSet = new TreeSet<Integer>();
					posMultimap.put(variable, indexSet);
					
					// this is the first occurrence, set up trimming
					trimIndices[trimmedVariablesTuple.size()] = i;
					trimmedVariablesTuple.add(variable);
				}
				indexSet.add(i);
			}

			// construct equality checks for each variable occurring multiple times
			PlanningTrace lastTrace = coreTrace;
			for (Entry<PVariable, SortedSet<Integer>> entry : posMultimap.entrySet()) {
				if (entry.getValue().size() > 1) {
					EqualityFilterRecipe equalityFilterRecipe = FACTORY.createEqualityFilterRecipe();
					equalityFilterRecipe.setParent(lastTrace.getRecipe());
					equalityFilterRecipe.getIndices().addAll(entry.getValue());
					lastTrace = new PlanningTrace(plan, 
							coreVariablesTuple, equalityFilterRecipe, lastTrace);
				}
			}
				
			// trim so that each variable occurs only once
			TrimmerRecipe trimmerRecipe = FACTORY.createTrimmerRecipe();
			trimmerRecipe.setParent(lastTrace.getRecipe());
			trimmerRecipe.setMask(RecipesHelper.mask(constraintArity, trimIndices));
			return new PlanningTrace(plan, 
					trimmedVariablesTuple, trimmerRecipe, lastTrace);
		}
	}
	
	
	/**
	 * Extracts the variable list representation of the variables tuple. 
	 */
	public static List<PVariable> convertVariablesTuple(EnumerablePConstraint constraint) {
		return convertVariablesTuple(constraint.getVariablesTuple());
	}
	/**
	 * Extracts the variable list representation of the variables tuple. 
	 */
	public static List<PVariable> convertVariablesTuple(Tuple variablesTuple) {
		List<PVariable> result = new ArrayList<PVariable>();
		for (Object o : variablesTuple.getElements())
			result.add((PVariable) o);
		return result;
	}
	
	
	/**
	 * Returns a compiled indexer trace according to a mask 
	 */
    public static RecipeTraceInfo makeIndexerTrace(SubPlan planToCompile, PlanningTrace parentTrace, TupleMask mask) {
		final ReteNodeRecipe parentRecipe = parentTrace.getRecipe();
		if (parentRecipe instanceof AggregatorRecipe) 
			throw new IllegalArgumentException("Cannot take projection indexer of aggregator node at plan " + planToCompile);
		IndexerRecipe recipe = RecipesHelper.projectionIndexerRecipe(parentRecipe, 
				RecipesHelper.mask(mask.sourceWidth, mask.indices));
		// final List<PVariable> maskedVariables = mask.transform(parentTrace.getVariablesTuple());
		return new PlanningTrace(planToCompile, 
				/*maskedVariables*/ parentTrace.getVariablesTuple(), recipe, parentTrace);
		// TODO add specialized indexer trace info?
    }

    /**
     * Creates a trimmer that keeps selected variables only.
     */
	protected static TrimmerRecipe makeTrimmerRecipe(
			final PlanningTrace compiledParent,
			List<PVariable> projectedVariables) {
		final Mask projectionMask = makeProjectionMask(compiledParent, projectedVariables);
		final TrimmerRecipe trimmerRecipe = ReteRecipeCompiler.FACTORY.createTrimmerRecipe();
		trimmerRecipe.setParent(compiledParent.getRecipe());
		trimmerRecipe.setMask(projectionMask);
		return trimmerRecipe;
	}


	public static Mask makeProjectionMask(final PlanningTrace compiledParent, Iterable<PVariable> projectedVariables) {
		List<Integer> projectionSourceIndices = new ArrayList<Integer>();
		for (PVariable pVariable : projectedVariables) {
			projectionSourceIndices.add(compiledParent.getPosMapping().get(pVariable));
		}
		final Mask projectionMask = RecipesHelper.mask(compiledParent.getRecipe().getArity(), projectionSourceIndices);
		return projectionMask;
	}


    /**
     * Creates a recipe for a production node and the corresponding trace.
     */
	public static CompiledQuery makeQueryTrace(PQuery query,
			Collection<RecipeTraceInfo> bodyFinalTraces,
			Collection<ReteNodeRecipe> bodyFinalRecipes) 
	{
		final ProductionRecipe recipe = ReteRecipeCompiler.FACTORY.createProductionRecipe();
		recipe.setPattern(query);
		recipe.setPatternFQN(query.getFullyQualifiedName());
		recipe.setTraceInfo(recipe.getPatternFQN());
		recipe.getParents().addAll(bodyFinalRecipes);
		for (int i = 0; i < query.getParameterNames().size(); ++i)
			recipe.getMappedIndices().put(query.getParameterNames().get(i), i);
		
		return new CompiledQuery(recipe, bodyFinalTraces, query);
	}    
    
    /**
     * Calculated index mappings for a join, based on the common variables of the two parent subplans.
     * 
     * @author Gabor Bergmann
     * 
     */
    public static class JoinHelper {
        private TupleMask primaryMask;
        private TupleMask secondaryMask;
        private TupleMask complementerMask;
		private RecipeTraceInfo primaryIndexer;
		private RecipeTraceInfo secondaryIndexer;
		private JoinRecipe naturalJoinRecipe;
		private List<PVariable> naturalJoinVariablesTuple;

        /**
         * @pre enforceVariableCoincidences() has been called on both sides.
         */
        public JoinHelper(SubPlan planToCompile, 
        		PlanningTrace primaryCompiled, 
        		PlanningTrace callTrace) 
        {
            super();

            Set<PVariable> primaryVariables = new HashSet<PVariable>(primaryCompiled.getVariablesTuple());
            Set<PVariable> secondaryVariables = new HashSet<PVariable>(callTrace.getVariablesTuple());
            int oldNodes = 0;
            Set<Integer> introducingSecondaryIndices = new TreeSet<Integer>();
            for (PVariable var : secondaryVariables) {
                if (primaryVariables.contains(var))
                    oldNodes++;
                else
                    introducingSecondaryIndices.add(callTrace.getPosMapping().get(var));
            }
            int[] primaryIndices = new int[oldNodes];
            final int[] secondaryIndices = new int[oldNodes];
            int k = 0;
            for (PVariable var : secondaryVariables) {
                if (primaryVariables.contains(var)) {
                    primaryIndices[k] = primaryCompiled.getPosMapping().get(var);
                    secondaryIndices[k] = callTrace.getPosMapping().get(var);
                    k++;
                }
            }
            int[] complementerIndices = new int[introducingSecondaryIndices.size()];
            int l = 0;
            for (Integer integer : introducingSecondaryIndices) {
                complementerIndices[l++] = integer;
            }
            primaryMask = new TupleMask(primaryIndices, primaryCompiled.getVariablesTuple().size());
            secondaryMask = new TupleMask(secondaryIndices, callTrace.getVariablesTuple().size());
            complementerMask = new TupleMask(complementerIndices, callTrace.getVariablesTuple().size());
            
        	primaryIndexer = makeIndexerTrace(planToCompile, primaryCompiled, primaryMask);
        	secondaryIndexer = makeIndexerTrace(planToCompile, callTrace, secondaryMask);
        	
        	naturalJoinRecipe = FACTORY.createJoinRecipe();
        	naturalJoinRecipe.setLeftParent((ProjectionIndexerRecipe) primaryIndexer.getRecipe());
        	naturalJoinRecipe.setRightParent((IndexerRecipe) secondaryIndexer.getRecipe());
    		naturalJoinRecipe.setRightParentComplementaryMask(RecipesHelper.mask(complementerMask.sourceWidth, complementerMask.indices));
        	
            naturalJoinVariablesTuple = new ArrayList<PVariable>(primaryCompiled.getVariablesTuple());
            for (int complementerIndex : complementerMask.indices)
            	naturalJoinVariablesTuple.add(callTrace.getVariablesTuple().get(complementerIndex));
        }
        

        public TupleMask getPrimaryMask() {
            return primaryMask;
        }

        public TupleMask getSecondaryMask() {
            return secondaryMask;
        }

        public TupleMask getComplementerMask() {
            return complementerMask;
        }


		public RecipeTraceInfo getPrimaryIndexer() {
			return primaryIndexer;
		}


		public RecipeTraceInfo getSecondaryIndexer() {
			return secondaryIndexer;
		}


		public JoinRecipe getNaturalJoinRecipe() {
			return naturalJoinRecipe;
		}


		public List<PVariable> getNaturalJoinVariablesTuple() {
			return naturalJoinVariablesTuple;
		}
        
    }
    
}
