/*******************************************************************************
 * Copyright (c) 2010-2015, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.matchers.util;

import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;

import com.google.common.base.Function;

/**
 * A provider interface useful in various registry instances.
 * 
 * @author Zoltan Ujhelyi
 *
 */
public interface IProvider<T> {

	T get();
	
	public final class ProvidedValueFunction implements Function<IProvider<PQuery>, PQuery> {
		@Override
		public PQuery apply(IProvider<PQuery> input) {
			return input.get();
		}
	}
}
