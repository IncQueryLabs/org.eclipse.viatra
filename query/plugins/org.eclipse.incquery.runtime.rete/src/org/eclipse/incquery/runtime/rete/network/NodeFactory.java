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
package org.eclipse.incquery.runtime.rete.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.incquery.runtime.matchers.psystem.IExpressionEvaluator;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.eclipse.incquery.runtime.matchers.tuple.TupleMask;
import org.eclipse.incquery.runtime.rete.boundary.ExternalInputEnumeratorNode;
import org.eclipse.incquery.runtime.rete.boundary.ExternalInputStatelessFilterNode;
import org.eclipse.incquery.runtime.rete.eval.CachedFunctionEvaluatorNode;
import org.eclipse.incquery.runtime.rete.eval.CachedPredicateEvaluatorNode;
import org.eclipse.incquery.runtime.rete.index.AggregatorNode;
import org.eclipse.incquery.runtime.rete.index.CountNode;
import org.eclipse.incquery.runtime.rete.index.ExistenceNode;
import org.eclipse.incquery.runtime.rete.index.Indexer;
import org.eclipse.incquery.runtime.rete.index.JoinNode;
import org.eclipse.incquery.runtime.rete.misc.ConstantNode;
import org.eclipse.incquery.runtime.rete.recipes.AggregatorIndexerRecipe;
import org.eclipse.incquery.runtime.rete.recipes.AntiJoinRecipe;
import org.eclipse.incquery.runtime.rete.recipes.CheckRecipe;
import org.eclipse.incquery.runtime.rete.recipes.ConstantRecipe;
import org.eclipse.incquery.runtime.rete.recipes.CountAggregatorRecipe;
import org.eclipse.incquery.runtime.rete.recipes.EqualityFilterRecipe;
import org.eclipse.incquery.runtime.rete.recipes.EvalRecipe;
import org.eclipse.incquery.runtime.rete.recipes.ExpressionDefinition;
import org.eclipse.incquery.runtime.rete.recipes.IndexerRecipe;
import org.eclipse.incquery.runtime.rete.recipes.InequalityFilterRecipe;
import org.eclipse.incquery.runtime.rete.recipes.InputFilterRecipe;
import org.eclipse.incquery.runtime.rete.recipes.InputRecipe;
import org.eclipse.incquery.runtime.rete.recipes.JoinRecipe;
import org.eclipse.incquery.runtime.rete.recipes.Mask;
import org.eclipse.incquery.runtime.rete.recipes.ProductionRecipe;
import org.eclipse.incquery.runtime.rete.recipes.ProjectionIndexerRecipe;
import org.eclipse.incquery.runtime.rete.recipes.ReteNodeRecipe;
import org.eclipse.incquery.runtime.rete.recipes.SemiJoinRecipe;
import org.eclipse.incquery.runtime.rete.recipes.TransitiveClosureRecipe;
import org.eclipse.incquery.runtime.rete.recipes.TransparentRecipe;
import org.eclipse.incquery.runtime.rete.recipes.TrimmerRecipe;
import org.eclipse.incquery.runtime.rete.recipes.UniquenessEnforcerRecipe;
import org.eclipse.incquery.runtime.rete.single.DefaultProductionNode;
import org.eclipse.incquery.runtime.rete.single.EqualityFilterNode;
import org.eclipse.incquery.runtime.rete.single.InequalityFilterNode;
import org.eclipse.incquery.runtime.rete.single.TransitiveClosureNode;
import org.eclipse.incquery.runtime.rete.single.TransparentNode;
import org.eclipse.incquery.runtime.rete.single.TrimmerNode;
import org.eclipse.incquery.runtime.rete.single.UniquenessEnforcerNode;
import org.eclipse.incquery.runtime.rete.traceability.TraceInfo;

/**
 * Factory for instantiating Rete nodes. The created nodes are not connected to the network yet. 
 * 
 * @author Bergmann Gabor
 *
 */
class NodeFactory {
	Logger logger;
	
	public NodeFactory(Logger logger) {
		super();
		this.logger = logger;
	}
	
	/**
	 * PRE: parent node must already be created 
	 */
	public Indexer createIndexer(ReteContainer reteContainer, 
			IndexerRecipe recipe,
			Supplier parentNode, TraceInfo... traces) {
		
		if (recipe instanceof ProjectionIndexerRecipe) {
			return parentNode.constructIndex(toMask(recipe.getMask()), traces);
			// already traced
		} else if (recipe instanceof AggregatorIndexerRecipe) {
			final Indexer result = ((AggregatorNode)parentNode).getAggregatorOuterIndexer();
			for (TraceInfo traceInfo : traces) 
				result.assignTraceInfo(traceInfo);
			return result;
		} else 
			throw new IllegalArgumentException("Unkown Indexer recipe: " + recipe);
	}


	/**
	 * PRE: recipe is not an indexer recipe.
	 */
	public Supplier createNode(ReteContainer reteContainer, ReteNodeRecipe recipe, TraceInfo... traces) {
		if (recipe instanceof IndexerRecipe)
			throw new IllegalArgumentException("Indexers are not created by NodeFactory: " + recipe);
		
		Supplier result = instantiateNodeDispatch(reteContainer, recipe);
		for (TraceInfo traceInfo : traces) 
			result.assignTraceInfo(traceInfo);
        return result;	
	}

	private Supplier instantiateNodeDispatch(ReteContainer reteContainer, ReteNodeRecipe recipe) {
		
		// Parentless
		
		if (recipe instanceof ConstantRecipe) 
			return instantiateNode(reteContainer, (ConstantRecipe)recipe);	
		if (recipe instanceof InputRecipe) 
			return instantiateNode(reteContainer, (InputRecipe)recipe);	
		
		// SingleParentNodeRecipe
		
//		if (recipe instanceof ProjectionIndexer) 
//			return instantiateNode((ProjectionIndexer)recipe);
		if (recipe instanceof InputFilterRecipe) 
			return instantiateNode(reteContainer, (InputFilterRecipe)recipe);
		if (recipe instanceof InequalityFilterRecipe) 
			return instantiateNode(reteContainer, (InequalityFilterRecipe)recipe);
		if (recipe instanceof EqualityFilterRecipe) 
			return instantiateNode(reteContainer, (EqualityFilterRecipe)recipe);
		if (recipe instanceof TransparentRecipe) 
			return instantiateNode(reteContainer, (TransparentRecipe)recipe);
		if (recipe instanceof TrimmerRecipe) 
			return instantiateNode(reteContainer, (TrimmerRecipe)recipe);
		if (recipe instanceof TransitiveClosureRecipe) 
			return instantiateNode(reteContainer, (TransitiveClosureRecipe)recipe);
		if (recipe instanceof CheckRecipe) 
			return instantiateNode(reteContainer, (CheckRecipe)recipe);
		if (recipe instanceof EvalRecipe) 
			return instantiateNode(reteContainer, (EvalRecipe)recipe);
		if (recipe instanceof CountAggregatorRecipe) 
			return instantiateNode(reteContainer, (CountAggregatorRecipe)recipe);
		
		// MultiParentNodeRecipe
		if (recipe instanceof UniquenessEnforcerRecipe) 
			return instantiateNode(reteContainer, (UniquenessEnforcerRecipe)recipe);
		if (recipe instanceof ProductionRecipe) 
			return instantiateNode(reteContainer, (ProductionRecipe)recipe);
		
		// BetaNodeRecipe
		if (recipe instanceof JoinRecipe) 
			return instantiateNode(reteContainer, (JoinRecipe)recipe);
		if (recipe instanceof SemiJoinRecipe) 
			return instantiateNode(reteContainer, (SemiJoinRecipe)recipe);
		if (recipe instanceof AntiJoinRecipe) 
			return instantiateNode(reteContainer, (AntiJoinRecipe)recipe);
		
		// ... TODO	
		throw new IllegalArgumentException("Unsupported recipe type: " + recipe);
	}


	// INSTANTIATION for recipe types

	private Supplier instantiateNode(ReteContainer reteContainer, InputRecipe recipe) {
		return new ExternalInputEnumeratorNode(reteContainer);
	}
	private Supplier instantiateNode(ReteContainer reteContainer, InputFilterRecipe recipe) {
		return new ExternalInputStatelessFilterNode(reteContainer, toMaskOrNull(recipe.getMask()));
	}
//	private Supplier instantiateNode(ReteContainer reteContainer, BinaryInputRecipe recipe) {
//		return new InputNode(reteContainer, 2, recipe.getTypeKey());
//	}
//
//	private Supplier instantiateNode(ReteContainer reteContainer, UnaryInputRecipe recipe) {
//		return new InputNode(reteContainer, 1, recipe.getTypeKey());
//	}

	private Supplier instantiateNode(ReteContainer reteContainer, CountAggregatorRecipe recipe) {
		return new CountNode(reteContainer);
	}

	private Supplier instantiateNode(ReteContainer reteContainer, TransparentRecipe recipe) {
		return new TransparentNode(reteContainer);
	}

	private Supplier instantiateNode(ReteContainer reteContainer, EvalRecipe recipe) {
		final IExpressionEvaluator evaluator = toIExpressionEvaluator(recipe.getExpression());
		final Map<String, Integer> posMapping = toStringIndexMap(recipe.getMappedIndices());
		return new CachedFunctionEvaluatorNode(reteContainer, logger, evaluator, posMapping, recipe.getParent().getArity());
	}

	private Supplier instantiateNode(ReteContainer reteContainer, CheckRecipe recipe) {
		final IExpressionEvaluator evaluator = toIExpressionEvaluator(recipe.getExpression());
		final Map<String, Integer> posMapping = toStringIndexMap(recipe.getMappedIndices());
		return new CachedPredicateEvaluatorNode(reteContainer, logger, evaluator, posMapping, recipe.getParent().getArity());
	}


	private Supplier instantiateNode(ReteContainer reteContainer, TransitiveClosureRecipe recipe) {
		return new TransitiveClosureNode(reteContainer);
	}

	private Supplier instantiateNode(ReteContainer reteContainer, ProductionRecipe recipe) {
		return new DefaultProductionNode(reteContainer, toStringIndexMap(recipe.getMappedIndices()));
	}

	private Supplier instantiateNode(ReteContainer reteContainer, UniquenessEnforcerRecipe recipe) {
		return new UniquenessEnforcerNode(reteContainer, recipe.getArity());
	}
	private Supplier instantiateNode(ReteContainer reteContainer, ConstantRecipe recipe) {
		final List<Object> constantValues = recipe.getConstantValues();
		final Object[] constantArray = constantValues.toArray(new Object[constantValues.size()]);
		return new ConstantNode(reteContainer, new FlatTuple(constantArray));
	}

	private Supplier instantiateNode(ReteContainer reteContainer, TrimmerRecipe recipe) {
		return new TrimmerNode(reteContainer, toMask(recipe.getMask()));
	}

	private Supplier instantiateNode(ReteContainer reteContainer, InequalityFilterRecipe recipe) {
        final int[] inequalIndices = integersToIntArray(recipe.getInequals());
		Tunnel result = new InequalityFilterNode(reteContainer, recipe.getSubject(), new TupleMask(inequalIndices, recipe.getParent().getArity()));
		return result;        
	}
	private Supplier instantiateNode(ReteContainer reteContainer, EqualityFilterRecipe recipe) {
		final int[] equalIndices = integersToIntArray(recipe.getIndices());
		return new EqualityFilterNode(reteContainer, equalIndices);
	}
	
	private Supplier instantiateNode(ReteContainer reteContainer, AntiJoinRecipe recipe) {
		return new ExistenceNode(reteContainer, true);
	}
	
	private Supplier instantiateNode(ReteContainer reteContainer, SemiJoinRecipe recipe) {
		return new ExistenceNode(reteContainer, false);
	}
	
	private Supplier instantiateNode(ReteContainer reteContainer, JoinRecipe recipe) {
		return new JoinNode(reteContainer, toMask(recipe.getRightParentComplementaryMask()));
	}
	
	// HELPERS
	
	private IExpressionEvaluator toIExpressionEvaluator(ExpressionDefinition expressionDefinition) {
		final Object evaluator = expressionDefinition.getEvaluator();
		if (evaluator instanceof IExpressionEvaluator) {
			return (IExpressionEvaluator) evaluator;
		}
		throw new IllegalArgumentException("No runtime support for expression definition: " + evaluator);
	}
	private Map<String, Integer> toStringIndexMap(final EMap<String, Integer> mappedIndices) {
		final HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (java.util.Map.Entry<String, Integer> entry : mappedIndices) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/** Mask can be null */
	private TupleMask toMaskOrNull(Mask mask) {
		if (mask == null) 
			return null;
		else 
			return toMask(mask);
	}
	/** Mask is non-null. */
	private TupleMask toMask(Mask mask) {
		return new TupleMask(integersToIntArray(mask.getSourceIndices()), mask.getSourceArity());
	}
	private int[] integersToIntArray(final List<Integer> integers) {
		int[] result = new int[integers.size()]; 
		int k=0; 
		for (Integer index : integers) 
			result[k++] = index;
		return result;
	}

	
}
