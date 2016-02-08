/*******************************************************************************
 * Copyright (c) 2010-2012, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.testing.core

import java.util.ArrayList
import java.util.Date
import org.eclipse.emf.ecore.EEnumLiteral
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.incquery.runtime.api.IPatternMatch
import org.eclipse.incquery.runtime.api.IQuerySpecification
import org.eclipse.incquery.runtime.api.IncQueryEngine
import org.eclipse.incquery.runtime.api.IncQueryMatcher
import org.eclipse.incquery.runtime.emf.EMFScope
import org.eclipse.incquery.snapshot.EIQSnapshot.EIQSnapshotFactory
import org.eclipse.incquery.snapshot.EIQSnapshot.IncQuerySnapshot
import org.eclipse.incquery.snapshot.EIQSnapshot.InputSpecification
import org.eclipse.incquery.snapshot.EIQSnapshot.MatchRecord
import org.eclipse.incquery.snapshot.EIQSnapshot.MatchSetRecord

/**
 * Helper methods for dealing with snapshots and match set records.
 */
class SnapshotHelper {
	/**
	 * Returns the EMF root that was used by the matchers recorded into the given snapshot,
	 *  based on the input specification and the model roots.
	 */
	def getEMFRootForSnapshot(IncQuerySnapshot snapshot){
		if(snapshot.inputSpecification == InputSpecification::EOBJECT){
			if(snapshot.modelRoots.size > 0){
				snapshot.modelRoots.get(0)
			}
		} else if(snapshot.inputSpecification == InputSpecification::RESOURCE){
			if(snapshot.modelRoots.size > 0){
				snapshot.modelRoots.get(0).eResource
			}
		} else if(snapshot.inputSpecification == InputSpecification::RESOURCE_SET){
			snapshot.eResource.resourceSet
		}
	}

	/**
	 * Returns the model roots that were used by the given IncQueryEngine.
	 */
	def getModelRootsForEngine(IncQueryEngine engine){
        switch scope : engine.scope {
            EMFScope: {
        		scope.scopeRoots.map[
        		    switch it {
        		        EObject: #[it]
        		        Resource: contents
        		        ResourceSet: resources.map[contents].flatten.toList
        		    }
        		].flatten.toList
       		}
       		default: #[]
       	}
	}

	/**
	 * Returns the input specification for the given matcher.
	 */
	def getInputSpecificationForMatcher(IncQueryMatcher matcher){
		switch scope : matcher.engine.scope {
		    EMFScope: {
		        switch scope.scopeRoots.head {
		            EObject: InputSpecification::EOBJECT
		            Resource: InputSpecification::RESOURCE
		            ResourceSet: InputSpecification::RESOURCE_SET 
		        }
		    }
		    default: InputSpecification::UNSET
		}
	}

	/**
	 * Saves the matches of the given matcher (using the partial match) into the given snapshot.
	 * If the input specification is not yet filled, it is now filled based on the engine of the matcher.
	 */
	def saveMatchesToSnapshot(IncQueryMatcher matcher, IPatternMatch partialMatch, IncQuerySnapshot snapshot){
		val patternFQN = matcher.patternName
		val actualRecord = EIQSnapshotFactory::eINSTANCE.createMatchSetRecord
		actualRecord.patternQualifiedName = patternFQN
		// 1. put actual match set record in the same model with the expected
		snapshot.matchSetRecords.add(actualRecord)
		// 2. store model roots
		if(snapshot.inputSpecification == InputSpecification::UNSET){
			snapshot.modelRoots.addAll(matcher.engine.modelRootsForEngine)
			snapshot.modelRoots.remove(snapshot)
			snapshot.inputSpecification = matcher.inputSpecificationForMatcher
		}
		actualRecord.filter = partialMatch.createMatchRecordForMatch

		// 3. create match set records
		matcher.forEachMatch(partialMatch)[match |
			actualRecord.matches.add(match.createMatchRecordForMatch)
		]
		return actualRecord
	}

	/**
	 * Creates a match record that corresponds to the given match.
	 *  Each parameter with a value is saved as a substitution.
	 */
	def createMatchRecordForMatch(IPatternMatch match){
		val matchRecord = EIQSnapshotFactory::eINSTANCE.createMatchRecord
		match.parameterNames.forEach()[param |
			if(match.get(param) != null){
				matchRecord.substitutions.add(param.createSubstitution(match.get(param)))
			}
		]
		return matchRecord
	}
	
	/**
	 * Creates a match set record which holds the snapshot of a single matcher instance. It is also possible to enter
	 * a filter for the matcher.
	 */
	def <Match extends IPatternMatch> createMatchSetRecordForMatcher(IncQueryMatcher<Match> matcher, Match filter){
		val matchSetRecord = EIQSnapshotFactory::eINSTANCE.createMatchSetRecord
		matcher.forEachMatch(filter,[ match |
			matchSetRecord.matches.add(createMatchRecordForMatch(match))
		] );
		return matchSetRecord
	}

	/**
	 * Creates a partial match that corresponds to the given match record.
	 *  Each substitution is used as a value for the parameter with the same name.
	 * @deprecated use createMatchForMatchRecord(IQuerySpecification, MatchRecord) instead
	 */
	@Deprecated
	def createMatchForMachRecord(IncQueryMatcher matcher, MatchRecord matchRecord){
		val match = matcher.newEmptyMatch
		matchRecord.substitutions.forEach()[
			var target = derivedValue
			/*if(target instanceof EObject){
				var etarget = target as EObject
				if(etarget.eIsProxy){
					target = EcoreUtil::resolve(etarget, matchRecord)
				}
			}*/
			match.set(parameterName,target)
		]
		return match
	}

	/**
	 * Creates a partial match that corresponds to the given match record.
	 *  Each substitution is used as a value for the parameter with the same name.
	 */
	def <Match extends IPatternMatch> Match createMatchForMatchRecord(IQuerySpecification<? extends IncQueryMatcher<Match>> querySpecification, MatchRecord matchRecord){
		val match = querySpecification.newEmptyMatch as Match
		matchRecord.substitutions.forEach()[
			var target = derivedValue
			match.set(parameterName,target)
		]
		return match
	}

	/**
	 * Saves all matches of the given matcher into the given snapshot.
	 * If the input specification is not yet filled, it is now filled based on the engine of the matcher.
	 */
	def saveMatchesToSnapshot(IncQueryMatcher matcher, IncQuerySnapshot snapshot){
		matcher.saveMatchesToSnapshot(matcher.newEmptyMatch, snapshot)
	}

	/**
	 * Returns the match set record for the given pattern FQN from the snapshot,
	 *  if there is only one such record.
	 */
	def getMatchSetRecordForPattern(IncQuerySnapshot snapshot, String patternFQN){
		val matchsetrecord = snapshot.matchSetRecords.filter[patternQualifiedName.equals(patternFQN)]
		if(matchsetrecord.size == 1){
			return matchsetrecord.iterator.next
		}
	}

	/**
	 * Returns the match set records for the given pattern FQN from the snapshot.
	 */
	def getMatchSetRecordsForPattern(IncQuerySnapshot snapshot, String patternFQN){
		val matchSetRecords = new ArrayList<MatchSetRecord>
		matchSetRecords.addAll(snapshot.matchSetRecords.filter[patternQualifiedName.equals(patternFQN)])
		return matchSetRecords
	}

	/**
	 * Creates a substitution for the given parameter name using the given value.
	 *  The type of the substitution is decided based on the type of the value.
	 */
	def createSubstitution(String parameterName, Object value){
		return switch(value) {
			EEnumLiteral: {
				val sub = EIQSnapshotFactory::eINSTANCE.createEnumSubstitution
				sub.setValueLiteral((value as EEnumLiteral).literal)
				sub.setEnumType((value as EEnumLiteral).EEnum)
				sub.setParameterName(parameterName)
				sub
			}
			EObject : {
				val sub = EIQSnapshotFactory::eINSTANCE.createEMFSubstitution
				sub.setValue(value as EObject)
				sub.setParameterName(parameterName)
				sub
			}
			Integer : {
				val sub = EIQSnapshotFactory::eINSTANCE.createIntSubstitution
				sub.setValue(value as Integer)
				sub.setParameterName(parameterName)
				sub
			}
			Long : {
				val sub = EIQSnapshotFactory::eINSTANCE.createLongSubstitution
				sub.setValue(value as Long)
				sub.setParameterName(parameterName)
				sub
			}
			Double : {
				val sub = EIQSnapshotFactory::eINSTANCE.createDoubleSubstitution
				sub.setValue(value as Double)
				sub.setParameterName(parameterName)
				sub				
			}
			Float : {
				val sub = EIQSnapshotFactory::eINSTANCE.createFloatSubstitution
				sub.setValue(value as Float)
				sub.setParameterName(parameterName)
				sub	
			}
			Boolean : {
				val sub = EIQSnapshotFactory::eINSTANCE.createBooleanSubstitution
				sub.setValue(value as Boolean)
				sub.setParameterName(parameterName)
				sub
			}
			String : {
				val sub = EIQSnapshotFactory::eINSTANCE.createStringSubstitution
				sub.setValue(value as String)
				sub.setParameterName(parameterName)
				sub	
			}
			Date : {
				val sub = EIQSnapshotFactory::eINSTANCE.createDateSubstitution
				sub.setValue(value as Date)
				sub.setParameterName(parameterName)
				sub				
			}
			default : {
				val sub = EIQSnapshotFactory::eINSTANCE.createMiscellaneousSubstitution
				sub.setValue(value)
				sub.setParameterName(parameterName)
				sub	
			}
		}
	}

}