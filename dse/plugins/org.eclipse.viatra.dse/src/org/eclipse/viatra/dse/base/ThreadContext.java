/*******************************************************************************
 * Copyright (c) 2010-2014, Miklos Foldenyi, Andras Szabolcs Nagy, Abel Hegedus, Akos Horvath, Zoltan Ujhelyi and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *   Miklos Foldenyi - initial API and implementation
 *   Andras Szabolcs Nagy - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.dse.base;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.viatra.dse.api.DSEException;
import org.eclipse.viatra.dse.api.strategy.interfaces.IStrategy;
import org.eclipse.viatra.dse.api.strategy.interfaces.IStrategyFactory;
import org.eclipse.viatra.dse.designspace.api.IDesignSpace;
import org.eclipse.viatra.dse.designspace.api.TrajectoryInfo;
import org.eclipse.viatra.dse.objectives.Fitness;
import org.eclipse.viatra.dse.objectives.IGlobalConstraint;
import org.eclipse.viatra.dse.objectives.IObjective;
import org.eclipse.viatra.dse.objectives.ObjectiveComparatorHelper;
import org.eclipse.viatra.dse.solutionstore.SolutionStore;
import org.eclipse.viatra.dse.statecode.IStateCoder;
import org.eclipse.viatra.dse.util.EMFHelper;
import org.eclipse.viatra.query.runtime.api.IPatternMatch;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;
import org.eclipse.viatra.transformation.evm.api.Activation;
import org.eclipse.viatra.transformation.evm.api.RuleEngine;
import org.eclipse.viatra.transformation.evm.api.RuleSpecification;
import org.eclipse.viatra.transformation.evm.api.event.EventFilter;
import org.eclipse.viatra.transformation.evm.api.resolver.ConflictResolver;
import org.eclipse.viatra.transformation.evm.api.resolver.ConflictSet;
import org.eclipse.viatra.transformation.evm.specific.RuleEngines;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRule;

/**
 * This class holds all the information that is related to a single processing thread of the DesignSpaceExploration
 * process. For any attributes related to the Design Space Exploration process as a whole, see {@link GlobalContext}.
 * 
 * @author Miklos Foldenyi
 * 
 */
public class ThreadContext implements IDseStrategyContext{

    private final GlobalContext globalContext;
    private final IStrategy strategy;
    private ExplorerThread explorerThread;
    private RuleEngine ruleEngine;
    private ViatraQueryEngine queryEngine;
    private EditingDomain domain;
    private Notifier model;
    private DesignSpaceManager designSpaceManager;
    private List<IObjective> objectives;
    private List<IGlobalConstraint> globalConstraints;
    private Fitness lastFitness;
    private ObjectiveComparatorHelper objectiveComparatorHelper;

    /**
     * This value is true after the {@link ThreadContext} has been initialized in it's own thread.
     */
    private AtomicBoolean inited = new AtomicBoolean(false);

    private boolean isFirstThread = false;
    private IObjective[][] leveledObjectives;
    private boolean isThereHardObjective;
    
    private static class GetRuleExecutionsImpl implements DseIdPoolHelper.IGetRuleExecutions {

        private List<BatchTransformationRule<?, ?>> executedRules;

        public GetRuleExecutionsImpl(List<BatchTransformationRule<?, ?>> executedRulesView) {
            this.executedRules = executedRulesView;
        }
        
        @Override
        public int getRuleExecutions(BatchTransformationRule<?, ?> rule) {
            int nextId = 0;
            for (BatchTransformationRule<?, ?> r : executedRules) {
                if (r.equals(rule)) {
                    nextId ++;
                }
            }
            return nextId;
        }
        
    }
    
    private SingletonSetConflictResolver conflictResolver;
    
    public SingletonSetConflictResolver getConflictResolver() {
        return conflictResolver;
    }

    public ConflictSet getConflictSet() {
        return conflictResolver.conflictSet;
    }

    /**
     * Creates a {@link ThreadContext} and sets it up to be initialized on the given {@link TransactionalEditingDomain}
     * 
     * @param globalContext
     * @param strategyBase
     * @param domain
     * @param trajectoryInfoToClone
     * @param parentGuidance
     */
    public ThreadContext(final GlobalContext globalContext, IStrategy strategy, Notifier model) {
        checkArgument(model != null, "Cannot initialize ThreadContext on a null model.");
        this.globalContext = globalContext;
        this.strategy = strategy;
        this.model = model;
    }

    /**
     * Initializes the {@link ThreadContext} by initializing the underlying {@link ViatraQueryEngine} and
     * {@link RuleEngine}. {@link Guidance} initialization is also happening within this method.
     * 
     * @throws ViatraQueryException
     */
    public void init() throws DSEException {

        AtomicBoolean isFirst = globalContext.getFirstThreadContextIniting();
        AtomicBoolean isFirstReady = globalContext.getFirstThreadContextInited();
        if (!isFirstReady.get()) {
            if (!isFirst.compareAndSet(false, true)) {
                try {
                    do {
                        Thread.sleep(5);
                    } while (!isFirstReady.get());
                } catch (InterruptedException e) {
                }
            } else {
                isFirstThread = true;
            }
        }
        // prohibit re-initialization
        checkArgument(!inited.getAndSet(true), "This Thread context has been initialized already!");

        try {
            // initialize query engine
            final EMFScope scope = new EMFScope(model);
            queryEngine = ViatraQueryEngine.on(scope);
            ruleEngine = RuleEngines.createViatraQueryRuleEngine(queryEngine);
            ConflictResolver conflictResolverToWrap = globalContext.getConflictResolver();
            conflictResolver = new SingletonSetConflictResolver(conflictResolverToWrap);
            ruleEngine.setConflictResolver(conflictResolver);
            for (BatchTransformationRule<?, ?> tr : globalContext.getTransformations()) {
                ruleEngine.addRule(tr.getRuleSpecification(), (EventFilter<IPatternMatch>) tr.getFilter());
            }
        } catch (ViatraQueryException e) {
            throw new DSEException("Failed to create unmanaged ViatraQueryEngine on the model.", e);
        }

        if (isFirstThread) {

            objectives = globalContext.getObjectives();
            leveledObjectives = globalContext.getLeveledObjectives();
            globalConstraints = globalContext.getGlobalConstraints();

        } else {
            objectives = new ArrayList<IObjective>();

            IObjective[][] leveledObjectivesToCopy = globalContext.getLeveledObjectives();
            leveledObjectives = new IObjective[leveledObjectivesToCopy.length][];
            for (int i = 0; i < leveledObjectivesToCopy.length; i++) {
                leveledObjectives[i] = new IObjective[leveledObjectivesToCopy[i].length];
                for (int j = 0; j < leveledObjectivesToCopy[i].length; j++) {
                    leveledObjectives[i][j] = leveledObjectivesToCopy[i][j].createNew();
                    objectives.add(leveledObjectives[i][j]);
                }
            }

            globalConstraints = new ArrayList<IGlobalConstraint>();
            for (IGlobalConstraint globalConstraint : globalContext.getGlobalConstraints()) {
                globalConstraints.add(globalConstraint.createNew());
            }

        }
        // create the thread specific DesignSpaceManager
        this.domain = EMFHelper.createEditingDomain(model);
        designSpaceManager = new DesignSpaceManager(this, model, domain, globalContext.getStateCoderFactory(),
                globalContext.getDesignSpace(), queryEngine);

        for (IObjective objective : objectives) {
            objective.init(this);
            if (objective.isHardObjective()) {
                isThereHardObjective = true;
            }
        }
        for (IGlobalConstraint globalConstraint : globalConstraints) {
            globalConstraint.init(this);
        }

        DseIdPoolHelper.INSTANCE.registerRules(new GetRuleExecutionsImpl(getDesignSpaceManager().getTrajectoryInfo().getRules()), getRules());

        globalContext.initVisualizersForThread(this);

        if (isFirstThread) {
            isFirstReady.set(true);
        }

    }

    public Fitness calculateFitness() {
        Fitness result = new Fitness();

        boolean satisifiesHardObjectives = true;

        for (IObjective objective : objectives) {
            Double fitness = objective.getFitness(this);
            result.put(objective.getName(), fitness);
            if (objective.isHardObjective() && !objective.satisifiesHardObjective(fitness)) {
                satisifiesHardObjectives = false;
            }
        }

        if (isThereHardObjective) {
            result.setSatisifiesHardObjectives(satisifiesHardObjectives);
        }
        else {
            result.setSatisifiesHardObjectives(false);
        }

        lastFitness = result;

        return result;
    }

    public boolean checkGlobalConstraints() {
        for (IGlobalConstraint globalConstraint : globalContext.getGlobalConstraints()) {
            if (!globalConstraint.checkGlobalConstraint(this)) {
                return false;
            }
        }
        return true;
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }

    public GlobalContext getGlobalContext() {
        return globalContext;
    }

    public DesignSpaceManager getDesignSpaceManager() {
        return designSpaceManager;
    }

    public EditingDomain getEditingDomain() {
        return domain;
    }

    public Notifier getModel() {
        return model;
    }

    public ViatraQueryEngine getQueryEngine() {
        return queryEngine;
    }

    public IStrategy getStrategy() {
        return strategy;
    }

    public ExplorerThread getExplorerThread() {
        return explorerThread;
    }

    public void setExplorerThread(ExplorerThread explorerThread) {
        this.explorerThread = explorerThread;
    }

    public Fitness getLastFitness() {
        return lastFitness;
    }

    public ObjectiveComparatorHelper getObjectiveComparatorHelper() {
        if (objectiveComparatorHelper == null) {
            objectiveComparatorHelper = new ObjectiveComparatorHelper(leveledObjectives);
        }
        return objectiveComparatorHelper;
    }

    public IObjective[][] getLeveledObjectives() {
        return leveledObjectives;
    }
    
    public List<IObjective> getObjectives() {
        return objectives;
    }
    
    public List<IGlobalConstraint> getGlobalConstraints() {
        return globalConstraints;
    }

    @Override
    public SolutionStore getSolutionStore() {
        return globalContext.getSolutionStore();
    }

    @Override
    public void newSolution() {
        globalContext.getSolutionStore().newSolution(this);
    }

    @Override
    public Object getSharedObject() {
        return globalContext.getSharedObject();
    }

    @Override
    public void setSharedObject(Object sharedObject) {
        globalContext.setSharedObject(sharedObject);
    }

    @Override
    public Set<BatchTransformationRule<?, ?>> getRules() {
        return globalContext.getTransformations();
    }

    @Override
    public BatchTransformationRule<?, ?> getRuleByRuleSpecification(RuleSpecification<?> ruleSpecification) {
        return globalContext.getSpecificationRuleMap().get(ruleSpecification);
    }

    @Override
    public ExplorerThread tryStartNewThread(IStrategy strategy) {
        return globalContext.tryStartNewThread(this, strategy);
    }

    @Override
    public ExplorerThread tryStartNewThreadWithoutModelClone(IStrategy strategy) {
        return globalContext.tryStartNewThreadWithoutModelClone(this, strategy);
    }

    @Override
    public void startAllThreads(IStrategyFactory strategyFactory) {
        globalContext.startAllThreads(this, strategyFactory);
    }

    @Override
    public IStateCoder getStateCoder() {
        return designSpaceManager.getStateCoder();
    }

    @Override
    public IDesignSpace getDesignSpace() {
        return globalContext.getDesignSpace();
    }

    @Override
    public TrajectoryInfo getTrajectoryInfo() {
        return designSpaceManager.getTrajectoryInfo();
    }

    @Override
    public List<Object> getTrajectory() {
        return designSpaceManager.getTrajectoryInfo().getTrajectory();
    }

    @Override
    public List<Object> getTrajectoryCopied() {
        return new ArrayList<Object>(getTrajectory());
    }

    @Override
    public int getDepth() {
        return designSpaceManager.getTrajectoryInfo().getDepth();
    }

    @Override
    public Object getCurrentStateId() {
        return designSpaceManager.getTrajectoryInfo().getCurrentStateId();
    }

    @Override
    public Object getTransitionByActivation(Activation<?> activation) {
        return designSpaceManager.getTransitionByActivation(activation);
    }

    @Override
    public Activation<?> getActivationById(Object activationId) {
        return designSpaceManager.getActivationById(activationId);
    }

    @Override
    public BatchTransformationRule<?, ?> getRuleByActivation(Activation<?> activation) {
        return designSpaceManager.getRuleByActivation(activation);
    }

    @Override
    public BatchTransformationRule<?, ?> getRuleByActivationId(Object activationId) {
        return designSpaceManager.getRuleByActivationId(activationId);
    }

    @Override
    public Collection<Object> getCurrentActivationIds() {
        return designSpaceManager.getTransitionsFromCurrentState();
    }

    @Override
    public Collection<Object> getUntraversedActivationIds() {
        return designSpaceManager.getUntraversedTransitionsFromCurrentState();
    }

    @Override
    public void executeAcitvationId(Object activationId) {
        designSpaceManager.fireActivation(activationId);
    }

    @Override
    public boolean tryExecuteAcitvationId(Object activationId) {
        return designSpaceManager.tryFireActivation(activationId);
    }

    @Override
    public boolean executeRandomActivationId() {
        return designSpaceManager.executeRandomActivationId();
    }

    @Override
    public void executeTrajectory(Object[] activationIds) {
        designSpaceManager.executeTrajectory(activationIds);
    }

    @Override
    public void executeTrajectory(Object[] activationIds, int excludedIndex) {
        designSpaceManager.executeTrajectory(activationIds, excludedIndex);
    }

    @Override
    public int executeTrajectoryByTrying(Object[] activationIds) {
        return designSpaceManager.executeTrajectoryByTrying(activationIds);
    }

    @Override
    public int executeTrajectoryByTrying(Object[] activationIds, int excludedIndex) {
        return designSpaceManager.executeTrajectoryByTrying(activationIds, excludedIndex);
    }

    @Override
    public int executeTrajectoryWithoutStateCoding(Object[] activationIds) {
        return designSpaceManager.executeTrajectoryWithoutStateCoding(activationIds);
    }

    @Override
    public int executeTrajectoryWithoutStateCoding(Object[] activationIds, int excludedIndex) {
        return designSpaceManager.executeTrajectoryWithoutStateCoding(activationIds, excludedIndex);
    }

    @Override
    public int executeTrajectoryByTryingWithoutStateCoding(Object[] activationIds) {
        return designSpaceManager.executeTrajectoryByTryingWithoutStateCoding(activationIds);
    }

    @Override
    public int executeTrajectoryByTryingWithoutStateCoding(Object[] activationIds, int excludedIndex) {
        return designSpaceManager.executeTrajectoryByTryingWithoutStateCoding(activationIds, excludedIndex);
    }

    @Override
    public boolean backtrack() {
        return designSpaceManager.undoLastTransformation();
    }

    @Override
    public void backtrackUntilRoot() {
        designSpaceManager.undoUntilRoot();
    }

    @Override
    public boolean isCurrentStateAlreadyTraversed() {
        return designSpaceManager.isNewModelStateAlreadyTraversed();
    }

    @Override
    public boolean isCurrentStateInTrajectory() {
        return designSpaceManager.isCurentStateInTrajectory();
    }

}