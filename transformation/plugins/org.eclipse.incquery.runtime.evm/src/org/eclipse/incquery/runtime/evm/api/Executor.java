/*******************************************************************************
 * Copyright (c) 2010-2013, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.evm.api;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.incquery.runtime.evm.api.event.EventRealm;
import org.eclipse.incquery.runtime.evm.api.resolver.ChangeableConflictSet;

/**
 * The executor is responsible for firing enabled activations of its ruleBase,
 * when its scheduler notifies it. The executor also manages a context that
 * is passed to activations.
 * 
 * @author Abel Hegedus
 *
 */
public class Executor {

    private RuleBase ruleBase;
    private Context context;
    private boolean scheduling = false;
    private final String startMessage = "Executing started in " + this;
    private final String reentrantMessage = "Reentrant schedule call ignored in " + this;
    private final String endMessage = "Executing ended in " + this;
    
    
    /**
     * Creates an executor for the given IncQueryEngine.
     * Executors are usually created as part of an ExecutionSchema 
     * through the EventDrivenVM.createExecutionSchema methods.
     * 
     * @param eventRealm
     */
    public Executor(final EventRealm eventRealm) {
        this(eventRealm, Context.create());
    }

    /**
     * Creates an executor for the given IncQueryEngine and Context.
     * Executors are usually created as part of an ExecutionSchema 
     * through the EventDrivenVM.createExecutionSchema methods.
     * 
     * @param eventRealm
     * @param context
     */
    public Executor(final EventRealm eventRealm, final Context context) {
        this.context = checkNotNull(context, "Cannot create trigger engine with null context!");
        ruleBase = new RuleBase(eventRealm);
    }

    /**
     * This method is called by the scheduler to indicate that the
     * executor should start its firing strategy.
     * 
     * The default implementation uses an as-long-as-possible strategy,
     * where the first enabled activation is fired, as long as there is one.
     * 
     * If firing causes further schedule calls, these reentrant calls are 
     * ignored, since the activations will be fired if they became enabled.
     */
    protected void schedule() {
        
        if(!startScheduling()) {
            return;
        }
        
        Activation<?> nextActivation = null;
        ChangeableConflictSet conflictSet = ruleBase.getAgenda().getConflictSet();
        while((nextActivation = conflictSet.getNextActivation()) != null) {
            ruleBase.getLogger().debug("Executing: " + nextActivation + " in " + this);
            nextActivation.fire(context);
        }
        
        endScheduling();
    }

    /**
     * This method is called from schedule() to indicate that a new call
     * was received. If there is already scheduling in progress, that is 
     * logged and false is returned.
     * 
     * Otherwise, a new scheduling starts, which is logged and stored.
     * 
     * @return true, if the firing strategy can start, false otherwise
     */
    protected synchronized boolean startScheduling() {
        if(scheduling) {
            ruleBase.getLogger().trace(reentrantMessage);
            return false;
        } else {
            scheduling = true;
            ruleBase.getLogger().trace(startMessage);
            return true;
        }
    }
    
    /**
     * This method is called by schedule() to indicate that the firing 
     * strategy is finished its execution. This is logged and the scheduling
     * state is set to false.
     */
    protected synchronized void endScheduling() {
        ruleBase.getLogger().trace(endMessage);
        scheduling = false;
    }

    /**
     * @return the ruleBase
     */
    public RuleBase getRuleBase() {
        return ruleBase;
    }
    
    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * Disposes of the executor by disposing its ruleBase.
     * 
     */
    protected void dispose() {
        ruleBase.dispose();
    }
    
}
