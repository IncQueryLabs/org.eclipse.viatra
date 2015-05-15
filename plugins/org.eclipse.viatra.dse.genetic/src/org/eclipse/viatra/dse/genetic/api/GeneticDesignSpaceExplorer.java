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
package org.eclipse.viatra.dse.genetic.api;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.viatra.dse.api.DSETransformationRule;
import org.eclipse.viatra.dse.api.DesignSpaceExplorer;
import org.eclipse.viatra.dse.api.SolutionTrajectory;
import org.eclipse.viatra.dse.base.GlobalContext;
import org.eclipse.viatra.dse.genetic.core.GeneticSharedObject;
import org.eclipse.viatra.dse.genetic.core.InstanceData;
import org.eclipse.viatra.dse.genetic.core.MainGeneticStrategy;
import org.eclipse.viatra.dse.genetic.debug.GeneticDebugger;
import org.eclipse.viatra.dse.genetic.interfaces.ICrossoverTrajectories;
import org.eclipse.viatra.dse.genetic.interfaces.IInitialPopulationSelector;
import org.eclipse.viatra.dse.genetic.interfaces.IMutateTrajectory;
import org.eclipse.viatra.dse.genetic.interfaces.ISelectNextPopulation;
import org.eclipse.viatra.dse.objectives.IGlobalConstraint;
import org.eclipse.viatra.dse.objectives.IObjective;
import org.eclipse.viatra.dse.solutionstore.DummySolutionStore;
import org.eclipse.viatra.dse.statecode.IStateCoderFactory;

public class GeneticDesignSpaceExplorer {

    private final MainGeneticStrategy MAIN_GENETIC_STRATEGY = new MainGeneticStrategy();
    private DesignSpaceExplorer dse;
    private GeneticSharedObject configuration;

    public GeneticDesignSpaceExplorer() {
        dse = new DesignSpaceExplorer();
        configuration = new GeneticSharedObject();
        dse.getGlobalContext().setSharedObject(configuration);
        dse.setSolutionStore(new DummySolutionStore());
    }

    public void setStartingModel(EObject root) {
        dse.setInitialModel(root, true);
    }

    public void setStateCoderFactory(IStateCoderFactory stateCoderFactory) {
        dse.setStateCoderFactory(stateCoderFactory);
    }

    public void addTransformationRule(DSETransformationRule<?, ?> rule) {
        dse.addTransformationRule(rule);
    }

    public void addTransformationRule(DSETransformationRule<?, ?> rule, int priority) {
        dse.addTransformationRule(rule);
        configuration.priorities.put(rule, priority);
    }

    public void setInitialPopulationSelector(IInitialPopulationSelector selector) {
        configuration.initialPopulationSelector = selector;
    }

    public void setSizeOfPopulation(int sizeOfPopulation) {
        configuration.sizeOfPopulation = sizeOfPopulation;
    }

    public void setStopCondition(StopCondition stopCondition, int stopConditionNumber) {
        configuration.stopCondition = stopCondition;
        configuration.stopConditionNumber = stopConditionNumber;
    }

    public void addObjective(IObjective objective) {
        dse.addObjective(objective);
    }

    public GlobalContext getGlobalContext() {
        return dse.getGlobalContext();
    }

    public void addGlobalConstraint(IGlobalConstraint constraint) {
        dse.addGlobalConstraint(constraint);
    }

    public void setMutationChanceAtCrossover(float baseChance) {
        setMutationChanceAtCrossover(baseChance, 0.0f);
    }

    public void setMutationChanceAtCrossover(float baseChance, float multiplierForAdaptivity) {
        configuration.chanceOfMutationInsteadOfCrossover = baseChance;
        configuration.mutationChanceMultiplier = multiplierForAdaptivity;
    }

    public void addMutatitor(IMutateTrajectory mutatior) {
        addMutatitor(mutatior, 1);
    }

    public void addMutatitor(IMutateTrajectory mutatior, int weight) {
        configuration.mutationApplications.put(mutatior, 0);
        for (int i = 0; i < weight; ++i) {
            configuration.mutatiors.add(mutatior);
        }
    }

    public void addCrossover(ICrossoverTrajectories crossover) {
        addCrossover(crossover, 1);
    }

    public void addCrossover(ICrossoverTrajectories crossover, int weight) {
        configuration.crossoverApplications.put(crossover, 0);
        for (int i = 0; i < weight; ++i) {
            configuration.crossovers.add(crossover);
        }
    }

    public void setSelector(ISelectNextPopulation selector) {
        configuration.selector = selector;
    }

    public void setNumberOfWorkerThreads(int workerThreads) {
        configuration.workerThreads = workerThreads;
    }

    public void startExploration() {
        this.startExploration(true);
    }

    public void startExploration(boolean waitForTermination) {
        dse.startExploration(MAIN_GENETIC_STRATEGY, waitForTermination, -1);
    }

    public boolean startExploration(long timeOutInMiliSec) {
        dse.startExploration(MAIN_GENETIC_STRATEGY, false, -1);

        double start = System.nanoTime() / 1000000;
        do {
            try {
                Thread.sleep(10);
                if (dse.getGlobalContext().isDone()) {
                    return false;
                }
            } catch (InterruptedException e) {
            }
        } while ((System.nanoTime() / 1000000) - start < timeOutInMiliSec);

        dse.getGlobalContext().stopAllThreads();

        // wait until all threads exit
        do {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            if (dse.getGlobalContext().isDone()) {
                return true;
            }
        } while (true);

    }

    public String getSolutionsInString() {
        StringBuilder sb = new StringBuilder();
        Set<InstanceData> solutions = getSolutions().keySet();

        sb.append("Best solutions (" + solutions.size() + ")");
        int i = 1;
        for (InstanceData instanceData : solutions) {
            sb.append("\n" + i++ + ". solution\n");
            instanceData.prettyPrint(sb);
            String state = instanceData.trajectory.get(instanceData.trajectory.size() - 1).getResultsIn().getId()
                    .toString();
            int runningApps = state.split("RUNNING").length - 1;
            int apps = state.split("\\{").length - 1;
            int unallocated = state.split("\\(\\)").length - 1;
            int unusedHosts = state.substring(state.lastIndexOf('}')).split(",").length;
            sb.append("running/unallocated/all: " + runningApps + "/" + unallocated + "/" + apps);
            sb.append(", unusedHosts: " + unusedHosts);
        }

        return sb.toString();
    }

    public DesignSpaceExplorer getDseEngine() {
        return dse;
    }

    public GeneticSharedObject getGeneticSharedObject() {
        return configuration;
    }

    public Map<InstanceData, SolutionTrajectory> getSolutions() {
        return configuration.bestSolutions;
    }

    public void setDebugger(GeneticDebugger geneticDebugger) {
        MAIN_GENETIC_STRATEGY.setGeneticDebugger(geneticDebugger);
    }
}
