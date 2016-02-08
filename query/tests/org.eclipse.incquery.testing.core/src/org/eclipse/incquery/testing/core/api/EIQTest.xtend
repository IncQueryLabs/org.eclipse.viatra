/** 
 * Copyright (c) 2010-2015, Grill Bal�zs, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Grill Bal�zs - initial API and implementation
 */
package org.eclipse.incquery.testing.core.api

import java.util.LinkedList
import java.util.List
import org.eclipse.emf.common.util.URI
import org.eclipse.incquery.runtime.api.IPatternMatch
import org.eclipse.incquery.runtime.api.IQueryGroup
import org.eclipse.incquery.runtime.api.IQuerySpecification
import org.eclipse.incquery.runtime.api.IncQueryMatcher
import org.eclipse.incquery.runtime.extensibility.QuerySpecificationRegistry
import org.eclipse.incquery.runtime.matchers.backend.IQueryBackend
import org.eclipse.incquery.runtime.matchers.backend.QueryEvaluationHint
import org.eclipse.incquery.testing.core.EIQTestCase
import org.eclipse.incquery.testing.core.PatternBasedMatchSetModelProvider
import org.eclipse.incquery.testing.core.SnapshotMatchSetModelProvider
import org.eclipse.incquery.testing.core.XmiModelUtil
import org.eclipse.incquery.testing.core.XmiModelUtil.XmiModelUtilRunningOptionEnum

/**
 * This class defines an API to easily construct test cases. The base conception is to provide
 * a set of match set sources (from a snapshot or from the execution of a pattern) and make assertions
 * on them being equal.
 */
class EIQTest {

	val EIQTestCase testCase;

	/**
	 * Test the specified query
	 */
	static def <Match extends IPatternMatch> test(IQuerySpecification<? extends IncQueryMatcher<Match>> pattern) {
		new EIQTest().and(pattern)
	}

	static def test(IQueryGroup patterns) {
		new EIQTest().and(patterns)
	}

	static def test() {
		new EIQTest
	}

	/**
	 * Test the specified query
	 */
	static def <Match extends IPatternMatch> test(String pattern) {
		test()
	}

	def and(IQueryGroup patterns) {
		patterns.specifications.forEach [
			this.patterns += it as IQuerySpecification<IncQueryMatcher<IPatternMatch>>
		]
		this
	}

	def and(IQuerySpecification<? extends IncQueryMatcher<? extends IPatternMatch>> pattern) {
		patterns.add(pattern)
		this
	}

	def and(String pattern) {
		and(
			QuerySpecificationRegistry.
				getQuerySpecification(pattern) as IQuerySpecification<IncQueryMatcher<IPatternMatch>>)
	}

	private val List<IQuerySpecification<? extends IncQueryMatcher<? extends IPatternMatch>>> patterns = new LinkedList;

	private new() {
		testCase = new EIQTestCase
	}

	private new(IQuerySpecification<? extends IncQueryMatcher<? extends IPatternMatch>> pattern) {
		this()
		this.patterns.add(pattern);
	}

	/**
	 * Add match result set with a query initialized using the given hints
	 */
	def with(QueryEvaluationHint hint) {
		val modelProvider = new PatternBasedMatchSetModelProvider(hint);
		testCase.addMatchSetModelProvider(modelProvider)
		this
	}

	/**
	 * Add match result set with a query initialized using the given query backend
	 */
	def with(Class<? extends IQueryBackend> queryBackendClass) {
		val QueryEvaluationHint hint = new QueryEvaluationHint(queryBackendClass, emptyMap);
		with(hint)
	}

	/**
	 * Add match result set loaded from the given snapshot
	 */
	def with(URI snapshotURI) {
		testCase.addMatchSetModelProvider(new SnapshotMatchSetModelProvider(snapshotURI))
		this
	}

	/**
	 * Add match result set loaded from the given snapshot
	 */
	def with(String snapshotURI) {
		with(XmiModelUtil::resolvePlatformURI(XmiModelUtilRunningOptionEnum.BOTH, snapshotURI))
	}

	/**
	 * Load input model
	 */
	def on(URI inputURI) {
		testCase.loadModel(inputURI)
		this
	}

	/**
	 * Execute all queries and check that the result sets are equal
	 */
	def assertEquals() {
		patterns.forEach [
			testCase.assertMatchSetsEqual(it as IQuerySpecification<IncQueryMatcher<IPatternMatch>>)
		]
	}

}
