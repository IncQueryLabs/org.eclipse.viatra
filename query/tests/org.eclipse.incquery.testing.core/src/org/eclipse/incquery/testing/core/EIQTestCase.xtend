/** 
 * Copyright (c) 2010-2015, Grill Balázs, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Grill Balázs - initial API and implementation
 */
package org.eclipse.incquery.testing.core

import java.util.LinkedList
import java.util.List
import junit.framework.AssertionFailedError
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.incquery.runtime.api.IPatternMatch
import org.eclipse.incquery.runtime.api.IQueryGroup
import org.eclipse.incquery.runtime.api.IQuerySpecification
import org.eclipse.incquery.runtime.api.IncQueryEngine
import org.eclipse.incquery.runtime.api.IncQueryMatcher
import org.eclipse.incquery.runtime.emf.EMFScope
import org.eclipse.incquery.testing.queries.UnexpectedMatchRecordMatcher

/** 
 * @author Grill Balázs
 */
class EIQTestCase {

	public static val String UNEXPECTED_MATCH = "Unexpected match"
	public static val String EXPECTED_NOT_FOUND = "Expected match not found"

	final ResourceSet resourceSet

	final List<IMatchSetModelProvider> modelProviders
	extension SnapshotHelper = new SnapshotHelper

	new() {
		this.resourceSet = new ResourceSetImpl
		this.modelProviders = new LinkedList
	}

	def loadModel(URI uri) {
		resourceSet.getResource(uri, true)
	}

	def addMatchSetModelProvider(IMatchSetModelProvider matchSetModelProvider) {
		modelProviders.add(matchSetModelProvider);
	}

	def <Match extends IPatternMatch> assertMatchSetsEqual(
		IQuerySpecification<? extends IncQueryMatcher<Match>> querySpecification) {
		if (modelProviders.size < 2) {
			throw new IllegalArgumentException("At least two model providers shall be set")
		}

		val reference = modelProviders.head

		modelProviders.tail.forEach [
			val diff = compareMatchSets(querySpecification, reference, it)
			if (!diff.empty) {
				throw new AssertionFailedError(diff.toString)
			}
		]
	}

	def assertMatchSetsEqual(IQueryGroup queryGroup) {
		queryGroup.specifications.forEach [
			assertMatchSetsEqual(it as IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>)
		]
	}

	private def <Match extends IPatternMatch> compareMatchSets(
		IQuerySpecification<? extends IncQueryMatcher<Match>> querySpecification,
		IMatchSetModelProvider expectedProvider, IMatchSetModelProvider actualProvider) {
		val engine = IncQueryEngine::on(new EMFScope(resourceSet))
		val unexpectedMatcher = UnexpectedMatchRecordMatcher::querySpecification().getMatcher(engine)

		val diff = newHashSet

		var Match filter = null;

		var expected = expectedProvider.getMatchSetRecord(resourceSet, querySpecification, filter)
		if (expected.filter != null) {
			filter = createMatchForMatchRecord(querySpecification, expected.filter)
		}

		val actual = actualProvider.getMatchSetRecord(resourceSet, querySpecification, filter)
		if (actual.filter != null) {
			if (filter != null) {
				throw new IllegalArgumentException(
					"Filter is provided by more than one sources: " + expectedProvider + ", " + actualProvider)
			} else {
				filter = createMatchForMatchRecord(querySpecification, actual.filter)
				// Reexecute expected to have the same filtering
				expected = expectedProvider.getMatchSetRecord(resourceSet, querySpecification, filter)
			}
		}

		unexpectedMatcher.forEachMatch(actual, expected, null) [
			diff.add(UNEXPECTED_MATCH + " (" + it.prettyPrint + ")")
		]
		unexpectedMatcher.forEachMatch(expected, actual, null) [
			diff.add(EXPECTED_NOT_FOUND + " (" + it.prettyPrint + ")")
		]
		return diff
	}
}
