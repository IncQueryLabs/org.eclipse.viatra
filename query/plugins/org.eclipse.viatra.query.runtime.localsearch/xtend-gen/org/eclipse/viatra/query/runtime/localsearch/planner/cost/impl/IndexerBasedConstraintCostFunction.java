/**
 * Copyright (c) 2010-2016, Grill Balázs, IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Grill Balázs - initial API and implementation
 */
package org.eclipse.viatra.query.runtime.localsearch.planner.cost.impl;

import org.eclipse.viatra.query.runtime.localsearch.planner.cost.IConstraintEvaluationContext;
import org.eclipse.viatra.query.runtime.localsearch.planner.cost.impl.StatisticsBasedConstraintCostFunction;
import org.eclipse.viatra.query.runtime.matchers.context.IInputKey;

/**
 * Cost function which calculates cost based on the cardinality of items in the runtime model, provided by the base indexer
 * 
 * @author Grill Balázs
 * @since 1.4
 */
@SuppressWarnings("all")
public class IndexerBasedConstraintCostFunction extends StatisticsBasedConstraintCostFunction {
  @Override
  public long countTuples(final IConstraintEvaluationContext input, final IInputKey supplierKey) {
    long _xifexpression = (long) 0;
    boolean _isEnumerable = supplierKey.isEnumerable();
    if (_isEnumerable) {
      _xifexpression = input.getRuntimeContext().countTuples(supplierKey, null);
    } else {
      _xifexpression = Math.round(StatisticsBasedConstraintCostFunction.DEFAULT_COST);
    }
    return _xifexpression;
  }
}
