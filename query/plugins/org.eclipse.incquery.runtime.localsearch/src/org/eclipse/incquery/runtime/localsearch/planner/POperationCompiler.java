/*******************************************************************************
 * Copyright (c) 2010-2014, Marton Bur, Akos Horvath, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marton Bur - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.localsearch.planner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.incquery.runtime.emf.EMFQueryRuntimeContext;
import org.eclipse.incquery.runtime.emf.EMFScope;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.localsearch.matcher.MatcherReference;
import org.eclipse.incquery.runtime.localsearch.operations.ISearchOperation;
import org.eclipse.incquery.runtime.localsearch.operations.check.BinaryTransitiveClosureCheck;
import org.eclipse.incquery.runtime.localsearch.operations.check.CheckConstant;
import org.eclipse.incquery.runtime.localsearch.operations.check.CountCheck;
import org.eclipse.incquery.runtime.localsearch.operations.check.ExpressionCheck;
import org.eclipse.incquery.runtime.localsearch.operations.check.InequalityCheck;
import org.eclipse.incquery.runtime.localsearch.operations.check.InstanceOfClassCheck;
import org.eclipse.incquery.runtime.localsearch.operations.check.InstanceOfDataTypeCheck;
import org.eclipse.incquery.runtime.localsearch.operations.check.NACOperation;
import org.eclipse.incquery.runtime.localsearch.operations.check.StructuralFeatureCheck;
import org.eclipse.incquery.runtime.localsearch.operations.check.nobase.ScopeCheck;
import org.eclipse.incquery.runtime.localsearch.operations.extend.CountOperation;
import org.eclipse.incquery.runtime.localsearch.operations.extend.ExpressionEval;
import org.eclipse.incquery.runtime.localsearch.operations.extend.ExtendConstant;
import org.eclipse.incquery.runtime.localsearch.operations.extend.ExtendToEStructuralFeatureSource;
import org.eclipse.incquery.runtime.localsearch.operations.extend.ExtendToEStructuralFeatureTarget;
import org.eclipse.incquery.runtime.localsearch.operations.extend.IterateOverEClassInstances;
import org.eclipse.incquery.runtime.localsearch.operations.extend.IterateOverEDatatypeInstances;
import org.eclipse.incquery.runtime.localsearch.planner.util.CompilerHelper;
import org.eclipse.incquery.runtime.matchers.backend.IQueryBackend;
import org.eclipse.incquery.runtime.matchers.context.IInputKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContext;
import org.eclipse.incquery.runtime.matchers.planning.QueryProcessingException;
import org.eclipse.incquery.runtime.matchers.planning.SubPlan;
import org.eclipse.incquery.runtime.matchers.planning.operations.PApply;
import org.eclipse.incquery.runtime.matchers.planning.operations.POperation;
import org.eclipse.incquery.runtime.matchers.planning.operations.PProject;
import org.eclipse.incquery.runtime.matchers.planning.operations.PStart;
import org.eclipse.incquery.runtime.matchers.psystem.PConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.ExportedParameter;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.ExpressionEvaluation;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.Inequality;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.NegativePatternCall;
import org.eclipse.incquery.runtime.matchers.psystem.basicdeferred.PatternMatchCounter;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.BinaryTransitiveClosure;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.ConstantValue;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.PositivePatternCall;
import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.TypeConstraint;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * An EMF specific plan compiler for the local search-based pattern matcher
 * 
 * @author Marton Bur
 *
 */
public class POperationCompiler {

    private List<ISearchOperation> operations;
    private Set<MatcherReference> dependencies = Sets.newHashSet();
    private Map<PConstraint, Set<Integer>> variableBindings;
	private Map<PVariable, Integer> variableMappings;
    private boolean baseIndexAvailable;
    private EMFQueryRuntimeContext runtimeContext;
    private Set<EObject> allModelContents;
    private IQueryBackend backend;

    public POperationCompiler(IQueryRuntimeContext runtimeContext, IQueryBackend backend) {
        this(runtimeContext, backend, false);
    }

    public POperationCompiler(IQueryRuntimeContext runtimeContext, IQueryBackend backend, boolean baseIndexAvailable) {
        this.backend = backend;
        this.runtimeContext = (EMFQueryRuntimeContext) runtimeContext;
        this.baseIndexAvailable = baseIndexAvailable;
        if(!baseIndexAvailable){
            // TODO obtain here the contents of the scope and pass it to operations that need it
            allModelContents = Sets.newHashSet();
            EMFScope emfScope = this.runtimeContext.getEmfScope();
            Set<? extends Notifier> scopeRoots = emfScope.getScopeRoots();
            for (Notifier notifier : scopeRoots) {
                if(notifier instanceof ResourceSet){
                    EList<Resource> allResources = ((ResourceSet) notifier).getResources();
                    for (Resource resource : allResources) {
                           storeAllEObjects(resource);
                    }
                }
                else if(notifier instanceof Resource){
                    storeAllEObjects((Resource) notifier);
                }
            }
        }
    }

    private void storeAllEObjects(Resource resource) {
        TreeIterator<EObject> allContents = resource.getAllContents();
        this.allModelContents.addAll(Sets.newHashSet(allContents));
    }

	/**
     * Compiles a plan of <code>POperation</code>s to a list of type <code>List&ltISearchOperation></code>
     * 
     * @param plan
     * @param boundVariableIndexes 
	 * @return an ordered list of POperations that make up the compiled search plan
     * @throws QueryProcessingException 
     */
    public List<ISearchOperation> compile(SubPlan plan, Set<Integer> boundVariableIndexes) throws QueryProcessingException {

        variableMappings = CompilerHelper.createVariableMapping(plan);
        variableBindings = CompilerHelper.cacheVariableBindings(plan,variableMappings,boundVariableIndexes);

        operations = Lists.newArrayList();

        List<POperation> operationList = CompilerHelper.createOperationsList(plan);
        for (POperation pOperation : operationList) {
            compile(pOperation, variableMappings);
        }

        return operations;
    }
 
    private void compile(POperation pOperation, Map<PVariable, Integer> variableMapping) throws QueryProcessingException {

        if (pOperation instanceof PApply) {
            PApply pApply = (PApply) pOperation;
            PConstraint pConstraint = pApply.getPConstraint();

            
            Set<PVariable> affectedVariables = pConstraint.getAffectedVariables();
            Set<Integer> varIndices = Sets.newHashSet();
            for (PVariable variable : affectedVariables) {
                varIndices.add(variableMapping.get(variable));
            }
            if (variableBindings.get(pConstraint).containsAll(varIndices)) {
                // check
                createCheckDispatcher(pConstraint, variableMapping);
            } else {
                // extend
                createExtendDispatcher(pConstraint, variableMapping);
            }

        } else if (pOperation instanceof PStart) {
            // nop
        } else if (pOperation instanceof PProject) {
            // nop
        } else {
            throw new QueryProcessingException("PStart, PApply or PProject was expected, received: " + pOperation.getClass(), null,"Unexpected POperation type", null);
        }

    }

    private void createCheckDispatcher(PConstraint pConstraint, Map<PVariable, Integer> variableMapping) {


        // DeferredPConstraint subclasses

        // Equalities are normalized

        if (pConstraint instanceof Inequality) {
            createCheck((Inequality) pConstraint, variableMapping);
        } else if (pConstraint instanceof NegativePatternCall) {
            createCheck((NegativePatternCall) pConstraint,variableMapping);
        } else if (pConstraint instanceof PatternMatchCounter) {
            createCheck((PatternMatchCounter) pConstraint, variableMapping);
        } else if (pConstraint instanceof ExpressionEvaluation) {
            createCheck((ExpressionEvaluation) pConstraint, variableMapping);
        } else if (pConstraint instanceof ExportedParameter) {
            // Nothing to do here
        } 
        
        // EnumerablePConstraint subclasses

        if (pConstraint instanceof BinaryTransitiveClosure) {
            createCheck((BinaryTransitiveClosure) pConstraint, variableMapping);
        } else if (pConstraint instanceof ConstantValue) {
            createCheck((ConstantValue) pConstraint, variableMapping);
        } else if (pConstraint instanceof PositivePatternCall) {
            createCheck((PositivePatternCall) pConstraint, variableMapping);
        } else if (pConstraint instanceof TypeConstraint) {
            createCheck((TypeConstraint) pConstraint,variableMapping);
        }

    }

    private void createCheck(ConstantValue constant, Map<PVariable, Integer> variableMapping) {
        int position = variableMapping.get(constant.getVariablesTuple().get(0));
        operations.add(new CheckConstant(position, constant.getSupplierKey()));
    }

    private void createCheck(TypeConstraint typeConstraint, Map<PVariable, Integer> variableMapping) {
    	final IInputKey inputKey = typeConstraint.getSupplierKey();
		if (inputKey instanceof EClassTransitiveInstancesKey) {
	        operations.add(new InstanceOfClassCheck(variableMapping.get(typeConstraint.getVariablesTuple().get(0)), ((EClassTransitiveInstancesKey) inputKey).getEmfKey()));
	    } else if (inputKey instanceof EStructuralFeatureInstancesKey) {
            int sourcePosition = variableMapping.get(typeConstraint.getVariablesTuple().get(0));
            int targetPosition = variableMapping.get(typeConstraint.getVariablesTuple().get(1));
            operations.add(new StructuralFeatureCheck(sourcePosition, targetPosition,
                    ((EStructuralFeatureInstancesKey) inputKey).getEmfKey()));
        } else if (inputKey instanceof EDataTypeInSlotsKey) {
            operations.add(new InstanceOfDataTypeCheck(variableMapping.get(typeConstraint.getVariablesTuple().get(0)),
                    ((EDataTypeInSlotsKey) inputKey).getEmfKey()));
        } else {
	    	throw new IllegalArgumentException("Unsupported type: " + inputKey);
	    }
    }
    private void createCheck(PositivePatternCall positivePatternCall, Map<PVariable, Integer> variableMapping) {
        throw new UnsupportedOperationException("Pattern call not supported");
    }

    private void createCheck(BinaryTransitiveClosure binaryTransitiveColsure, Map<PVariable, Integer> variableMapping) {
        int sourcePosition = variableMapping.get(binaryTransitiveColsure.getVariablesTuple().get(0));
        int targetPosition = variableMapping.get(binaryTransitiveColsure.getVariablesTuple().get(1));
        
        PQuery referredQuery = binaryTransitiveColsure.getReferredQuery();
        
        operations.add(new BinaryTransitiveClosureCheck(referredQuery, sourcePosition, targetPosition));
        //The second parameter is NOT bound during execution!
        Set<Integer> adornment = ImmutableSet.of(0);
        dependencies.add(new MatcherReference(referredQuery, adornment));
    }


    private void createCheck(ExpressionEvaluation expressionEvaluation, Map<PVariable, Integer> variableMapping) {
        // Technically same as extend
        createExtend(expressionEvaluation, variableMapping);
    }    
    
    private void createCheck(PatternMatchCounter patternMatchCounter, Map<PVariable, Integer> variableMapping) {
        // Fill unbound variables with null; simply copy all variables. Unbound variables will be null anyway
        // Create frame mapping
        Map<Integer, Integer> frameMapping = Maps.newHashMap();
        Set<Integer> adornment = Sets.newHashSet();
        final Set<Integer> bindings = variableBindings.get(patternMatchCounter);
        int keySize = patternMatchCounter.getActualParametersTuple().getSize();
        for (int i = 0; i < keySize; i++) {
            PVariable parameter = (PVariable) patternMatchCounter.getActualParametersTuple().get(i);
            frameMapping.put(variableMapping.get(parameter), i);
            if (bindings.contains(variableMapping.get(parameter))) {
                adornment.add(i);
            }
        }
        
        PQuery referredQuery = patternMatchCounter.getReferredQuery();
        operations.add(new CountCheck(referredQuery, frameMapping, variableMapping.get(patternMatchCounter.getResultVariable())));
        dependencies.add(new MatcherReference(referredQuery, adornment));
    }
    
    private void createCheck(NegativePatternCall negativePatternCall, Map<PVariable, Integer> variableMapping) {
        // Technically same as extend
        createExtend(negativePatternCall, variableMapping);
    }
    
    private void createCheck(Inequality inequality, Map<PVariable, Integer> variableMapping) {
        operations.add(new InequalityCheck(variableMapping.get(inequality.getWho()), variableMapping.get(inequality.getWithWhom())));
    }

    
    private void createExtendDispatcher(PConstraint pConstraint, Map<PVariable, Integer> variableMapping) {

        // DeferredPConstraint subclasses
        
        // Equalities are normalized

        if (pConstraint instanceof Inequality) {
            createExtend((Inequality) pConstraint, variableMapping);
        } else if (pConstraint instanceof NegativePatternCall) {
            createExtend((NegativePatternCall) pConstraint,variableMapping);
        }  else if (pConstraint instanceof PatternMatchCounter) {
            createExtend((PatternMatchCounter) pConstraint, variableMapping);
        } else if (pConstraint instanceof ExpressionEvaluation) {
            createExtend((ExpressionEvaluation) pConstraint, variableMapping);
        } else if (pConstraint instanceof ExportedParameter) {
            createExtend((ExportedParameter) pConstraint, variableMapping);
        }
        
        // EnumerablePConstraint subclasses

        if (pConstraint instanceof BinaryTransitiveClosure) {
            createExtend((BinaryTransitiveClosure) pConstraint, variableMapping);
        } else if (pConstraint instanceof ConstantValue) {
            createExtend((ConstantValue) pConstraint, variableMapping);
        } else if (pConstraint instanceof TypeConstraint) {
            createExtend((TypeConstraint) pConstraint, variableMapping);
        }
    }

    private void createExtend(ConstantValue constant, Map<PVariable, Integer> variableMapping) {
        int position = variableMapping.get(constant.getVariablesTuple().get(0));
        operations.add(new ExtendConstant(position, constant.getSupplierKey()));        
    }

    private void createExtend(TypeConstraint typeConstraint, Map<PVariable, Integer> variableMapping) {
    	final IInputKey inputKey = typeConstraint.getSupplierKey();
    	if (inputKey instanceof EDataTypeInSlotsKey) {
    	    if(baseIndexAvailable){
    	        operations.add(new IterateOverEDatatypeInstances(variableMapping.get(typeConstraint.getVariableInTuple(0)), ((EDataTypeInSlotsKey) inputKey).getEmfKey()));		        
            } else {
                int position = variableMapping.get(typeConstraint.getVariableInTuple(0));
                operations
                        .add(new org.eclipse.incquery.runtime.localsearch.operations.extend.nobase.IterateOverEDatatypeInstances(position,
                                ((EDataTypeInSlotsKey) inputKey).getEmfKey(), allModelContents, backend));
                operations.add(new ScopeCheck(position, runtimeContext.getEmfScope()));
            }
    	} else if (inputKey instanceof EClassTransitiveInstancesKey) {
		    if(baseIndexAvailable){
                operations.add(new IterateOverEClassInstances(variableMapping.get(typeConstraint.getVariableInTuple(0)),
                        ((EClassTransitiveInstancesKey) inputKey).getEmfKey()));
            } else {
                int position = variableMapping.get(typeConstraint.getVariableInTuple(0));
                operations
                        .add(new org.eclipse.incquery.runtime.localsearch.operations.extend.nobase.IterateOverEClassInstances(
                                position,
                                ((EClassTransitiveInstancesKey) inputKey).getEmfKey(), allModelContents));
                operations.add(new ScopeCheck(position, runtimeContext.getEmfScope()));
            }
	    } else if (inputKey instanceof EStructuralFeatureInstancesKey) {
	    	final EStructuralFeature feature = ((EStructuralFeatureInstancesKey) inputKey).getEmfKey();
	    	
	        int sourcePosition = variableMapping.get(typeConstraint.getVariablesTuple().get(0));
	        int targetPosition = variableMapping.get(typeConstraint.getVariablesTuple().get(1));

	        boolean fromBound = variableBindings.get(typeConstraint).contains(sourcePosition);
	        boolean toBound = variableBindings.get(typeConstraint).contains(targetPosition);

            if (fromBound && !toBound) {
                if (baseIndexAvailable) {
                    operations.add(new ExtendToEStructuralFeatureTarget(sourcePosition, targetPosition, feature));
                } else {
                    operations
                            .add(new org.eclipse.incquery.runtime.localsearch.operations.extend.nobase.ExtendToEStructuralFeatureTarget(
                                    sourcePosition, targetPosition, feature));
                    operations.add(new ScopeCheck(targetPosition, runtimeContext.getEmfScope()));
                }
            }
	        else if(!fromBound && toBound){
	            if(baseIndexAvailable){
	                operations.add(new ExtendToEStructuralFeatureSource(sourcePosition, targetPosition, feature));	                
	            } else {
                    operations
                            .add(new org.eclipse.incquery.runtime.localsearch.operations.extend.nobase.ExtendToEStructuralFeatureSource(
                                    sourcePosition, targetPosition, feature));
                    operations.add(new ScopeCheck(sourcePosition, runtimeContext.getEmfScope()));
                }
	        } else {
	            // TODO Elaborate solution based on the navigability of edges
	            // As of now a static solution is implemented
                if (baseIndexAvailable) {
                    operations.add(new IterateOverEClassInstances(sourcePosition, feature.getEContainingClass()));
                    operations.add(new ExtendToEStructuralFeatureTarget(sourcePosition, targetPosition, feature));
                } else {
                    operations
                            .add(new org.eclipse.incquery.runtime.localsearch.operations.extend.nobase.IterateOverEClassInstances(
                                    sourcePosition, feature.getEContainingClass(), allModelContents));
                    operations.add(new ScopeCheck(sourcePosition, runtimeContext.getEmfScope()));
                    operations
                            .add(new org.eclipse.incquery.runtime.localsearch.operations.extend.nobase.ExtendToEStructuralFeatureTarget(
                                    sourcePosition, targetPosition, feature));
                    operations.add(new ScopeCheck(targetPosition, runtimeContext.getEmfScope()));
                }
            }

	    } else {
	    	throw new IllegalArgumentException("Unsupported type: " + inputKey);
	    }        
    }

    private void createExtend(BinaryTransitiveClosure binaryTransitiveClosure, Map<PVariable, Integer> variableMapping) {
        throw new UnsupportedOperationException("Binary transitive closures must be checks");
    }

    private void createExtend(ExportedParameter exportedParameter, Map<PVariable, Integer> variableMapping) {
        // Such PConstraints are only metadata
    }

    private void createExtend(ExpressionEvaluation expressionEvaluation, Map<PVariable, Integer> variableMapping) {
        // Fill unbound variables with null; simply copy all variables. Unbound variables will be null anyway
        Iterable<String> inputParameterNames = expressionEvaluation.getEvaluator().getInputParameterNames();
        Map<String, Integer> nameMap = Maps.newHashMap();
        
        for (String pVariableName : inputParameterNames) {
            PVariable pVariable = expressionEvaluation.getPSystem().getVariableByNameChecked(pVariableName);
            nameMap.put(pVariableName, variableMapping.get(pVariable));
        }
        
        // output variable can be null; if null it is an ExpressionCheck
        if(expressionEvaluation.getOutputVariable() == null){
            operations.add(new ExpressionCheck(expressionEvaluation.getEvaluator(), nameMap));
        } else {
            operations.add(new ExpressionEval(expressionEvaluation.getEvaluator(), nameMap, variableMapping.get(expressionEvaluation.getOutputVariable())));
        }
    }
    
    private void createExtend(PatternMatchCounter patternMatchCounter, Map<PVariable, Integer> variableMapping) {
        // Fill unbound variables with null; simply copy all variables. Unbound variables will be null anyway
        // Create frame mapping
        Map<Integer, Integer> frameMapping = Maps.newHashMap();
        Set<Integer> adornment = Sets.newHashSet();
        final Set<Integer> bindings = variableBindings.get(patternMatchCounter);
        int keySize = patternMatchCounter.getActualParametersTuple().getSize();
        for (int i = 0; i < keySize; i++) {
            PVariable parameter = (PVariable) patternMatchCounter.getActualParametersTuple().get(i);
            frameMapping.put(variableMapping.get(parameter), i);
            if (bindings.contains(variableMapping.get(parameter))) {
                adornment.add(i);
            }
        }
        
        PQuery referredQuery = patternMatchCounter.getReferredQuery();
        operations.add(new CountOperation(referredQuery, frameMapping, variableMapping.get(patternMatchCounter.getResultVariable())));
        dependencies.add(new MatcherReference(referredQuery, adornment));
    }
    
    private void createExtend(NegativePatternCall negativePatternCall, Map<PVariable, Integer> variableMapping) {
        // Fill unbound variables with null; simply copy all variables. Unbound variables will be null anyway
        // Create frame mapping
        Map<Integer,Integer> frameMapping = Maps.newHashMap();
        int keySize = negativePatternCall.getActualParametersTuple().getSize();
        Set<Integer> adornment = Sets.newHashSet();
        final Set<Integer> bindings = variableBindings.get(negativePatternCall);
        for (int i = 0; i < keySize; i++) {
            PVariable parameter = (PVariable) negativePatternCall.getActualParametersTuple().get(i);
            frameMapping.put(variableMapping.get(parameter), i);
            if (bindings.contains(variableMapping.get(parameter))) {
                adornment.add(i);
            }
            
        }
        PQuery referredQuery = negativePatternCall.getReferredQuery();
        operations.add(new NACOperation(referredQuery, frameMapping));
        dependencies.add(new MatcherReference(referredQuery, adornment));
    }

    private void createExtend(Inequality pConstraint, Map<PVariable, Integer> variableMapping) {
        throw new UnsupportedOperationException("Unsafe operation. Requires iteration over the entire domain");
    }

    public Set<MatcherReference> getDependencies() {
        return dependencies;
    }
    /**
	 * @return the cached variable bindings for the previously created plan
	 */
	public Map<PVariable, Integer> getVariableMappings() {
		return variableMappings;
	}
}
