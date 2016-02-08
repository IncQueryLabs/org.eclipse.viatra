/*******************************************************************************
 * Copyright (c) 2010-2013, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Szabo, Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.evm.specific.scheduler;

import org.eclipse.incquery.runtime.evm.api.Executor;
import org.eclipse.incquery.runtime.evm.api.Scheduler;

/**
 * A timed scheduler is similar to the {@link UpdateCompleteBasedScheduler} but it schedules in a periodic manner.
 *  One must define the interval between two consecutive scheduling calls.
 * 
 * @author Tamas Szabo
 * 
 */
public class TimedScheduler extends Scheduler {
    private long interval;
    private volatile boolean interrupted = false;
    
    /**
     * Creates a timed scheduler for the given executor and interval.
     * 
     * @param executor
     * @param interval
     */
    protected TimedScheduler(final Executor executor, final long interval) {
        super(executor);
        this.interval = interval;
        new FiringThread().start();
    }

    /**
     * Internal thread class for scheduling at predefined intervals.
     * 
     * @author Abel Hegedus
     *
     */
    private class FiringThread extends Thread {

        public FiringThread() {
            this.setName("TimedFiringStrategy [interval: " + interval + "]");
        }

        @Override
        public void run() {
            while (!interrupted) {
                schedule();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.incquery.runtime.evm.api.Scheduler#dispose()
     */
    @Override
    public void dispose() {
        interrupted = true;
    }
    
    /**
     * Scheduler factory implementation for preparing timed schedulers.
     * 
     * @author Abel Hegedus
     *
     */
    public static class TimedSchedulerFactory implements ISchedulerFactory{
        
        private long interval;
        
        /**
         * @param interval the interval to set
         */
        public void setInterval(final long interval) {
            this.interval = interval;
        }
        
        /**
         * @return the interval
         */
        public long getInterval() {
            return interval;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.incquery.runtime.evm.api.Scheduler.ISchedulerFactory#prepareScheduler()
         */
        @Override
        public Scheduler prepareScheduler(final Executor engine) {
            return new TimedScheduler(engine, interval);
        }
        
        /**
         * Creates a scheduler factory with the given interval.
         * 
         * @param interval
         */
        public TimedSchedulerFactory(final long interval) {
            this.interval = interval;
        }
        
    }
}
