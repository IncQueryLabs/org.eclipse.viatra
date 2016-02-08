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

package org.eclipse.incquery.querybasedfeatures.tooling

import java.io.IOException
import java.util.ArrayList
import java.util.List
import org.eclipse.emf.codegen.ecore.genmodel.GenClass
import org.eclipse.emf.codegen.ecore.genmodel.GenFeature
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage
import org.eclipse.emf.common.util.BasicEMap
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.incquery.patternlanguage.emf.util.IErrorFeedback
import org.eclipse.incquery.patternlanguage.patternLanguage.Annotation
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern
import org.eclipse.incquery.querybasedfeatures.runtime.handler.QueryBasedFeatures
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.Block
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.ImportDeclaration
import org.eclipse.jdt.core.dom.Javadoc
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.Modifier
import org.eclipse.jdt.core.dom.TagElement
import org.eclipse.jdt.core.dom.TextElement
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclarationFragment
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import org.eclipse.jdt.core.dom.rewrite.ListRewrite
import org.eclipse.jface.text.Document
import org.eclipse.xtext.diagnostics.Severity

import static extension org.eclipse.incquery.patternlanguage.helper.CorePatternLanguageHelper.*

class ModelCodeBasedGenerator {

	extension QueryBasedFeatureGenerator gen
	
	//@Inject extension EMFPatternLanguageJvmModelInferrerUtil

	/* usage: @DerivedFeature(
	 * 			feature="featureName", (default: patten name)
	 * 			source="Src" (default: first parameter),
	 * 			target="Trg" (default: second parameter),
	 * 			kind="single/many/counter/sum/iteration" (default: feature.isMany?many:single)
	 * 			keepCache="true/false" (default: true)
	 * 		  )
	 */
	private static String IMPORT_QUALIFIER 			= "org.eclipse.incquery.querybasedfeatures.runtime"
	private static String FEATUREKIND_IMPORT		= "QueryBasedFeatureKind"
	private static String HELPER_IMPORT 			= "QueryBasedFeatureHelper"
	private static String HANDLER_NAME 				= "IQueryBasedFeatureHandler"
	private static String HANDLER_FIELD_SUFFIX 		= "Handler"

  new(QueryBasedFeatureGenerator generator) {
    gen = generator
  }
	
	def protected processJavaFiles(Pattern pattern, Annotation annotation, boolean generate) {
			try{
				val parameters = pattern.processDerivedFeatureAnnotation(annotation, generate)

				val pckg = provider.findGenPackage(pattern, parameters.ePackage)
				if(pckg == null){
          if(generate)
            errorFeedback.reportError(pattern,"GenPackage not found!", QueryBasedFeatureGenerator::DERIVED_ERROR_CODE, Severity::ERROR, IErrorFeedback::FRAGMENT_ERROR_TYPE)
          throw new IllegalArgumentException("Query-based feature pattern "+pattern.fullyQualifiedName+": GenPackage not found!")
        }
				val source =  parameters.source
				val feature = parameters.feature

				val genSourceClass = pckg.findGenClassForSource(source, pattern)
				val genFeature = genSourceClass.findGenFeatureForFeature(feature, pattern)

				val javaProject = pckg.findJavaProject
				if(javaProject == null){
				  errorFeedback.reportError(pattern,"Model project for GenPackage " + pckg.NSURI + " not found!", QueryBasedFeatureGenerator::DERIVED_ERROR_CODE, Severity::ERROR, IErrorFeedback::FRAGMENT_ERROR_TYPE)
          throw new IllegalArgumentException("Derived feature pattern "+pattern.fullyQualifiedName+": Model project for GenPackage " + pckg.NSURI + " not found!")
				}
				val compunit = pckg.findJavaFile(genSourceClass, javaProject)

				val docSource = compunit.source
				val parser = ASTParser::newParser(AST::JLS3)
				val document = new Document(docSource)
				parser.setSource(compunit)
				val astNode = parser.createAST(null) as CompilationUnit
				val ast = astNode.AST
				val rewrite = ASTRewrite::create(ast)
				val types = astNode.types as List<AbstractTypeDeclaration>
				val type = types.findFirst[
					val type = it as AbstractTypeDeclaration
					type.name.identifier == genSourceClass.className
				] as TypeDeclaration

				val bodyDeclListRewrite = rewrite.getListRewrite(type, TypeDeclaration::BODY_DECLARATIONS_PROPERTY)

				val feat = genFeature.ecoreFeature
				if(generate){
					ast.ensureImports(rewrite, astNode, type)
					ast.ensureHandlerField(bodyDeclListRewrite, type, genFeature)
					ast.ensureGetterMethod(document, type, rewrite, bodyDeclListRewrite, genSourceClass, genFeature, pattern, parameters)

					try{
  					val annotations = new ArrayList(feat.EAnnotations)
            annotations.forEach[
              if(it.source == QueryBasedFeatures::ANNOTATION_SOURCE){
                feat.EAnnotations.remove(it)
              }
            ]
  					val ecoreAnnotation = EcoreFactory::eINSTANCE.createEAnnotation
  					ecoreAnnotation.source = QueryBasedFeatures::ANNOTATION_SOURCE
            feat.EAnnotations.add(ecoreAnnotation)

  				  // add entry ("patternFQN", pattern.fullyQualifiedName)
  			    val entry = EcoreFactory::eINSTANCE.create(EcorePackage::eINSTANCE.getEStringToStringMapEntry()) as BasicEMap.Entry<String,String>
  			    entry.key = QueryBasedFeatures::PATTERN_FQN_KEY
  			    entry.value = pattern.fullyQualifiedName
  			    ecoreAnnotation.details.add(entry)
					} catch(Exception e){
					  logger.warn("Error happened when trying to edit Ecore file!", e)
					}
				} else {
					ast.removeHandlerField(bodyDeclListRewrite, type, genFeature.name)
					ast.restoreGetterMethod(document, compunit, type, rewrite, bodyDeclListRewrite, genSourceClass, genFeature)

					try{
					  val annotations = new ArrayList(feat.EAnnotations)
  					annotations.forEach[
              if(it.source == QueryBasedFeatures::ANNOTATION_SOURCE){
                feat.EAnnotations.remove(it)
              }
            ]
          } catch(Exception e){
            logger.warn("Error happened when trying to edit Ecore file!", e)
          }
				}
				try{
          feat.eResource.save(null)
				} catch(IOException e){
				  logger.warn("Error happened when trying to save Ecore file!", e)
				}

				val edits = rewrite.rewriteAST(document, javaProject.getOptions(true));
				edits.apply(document);
				val newSource = document.get
				compunit.buffer.setContents(newSource)
				// save!
				compunit.buffer.save(null, false)

			} catch(IllegalArgumentException e){
			  if(generate){
				  logger.error(e.message,e);
				}
			}
	}

	def private findGenClassForSource(GenPackage pckg, EClass source, Pattern pattern){
		val genSourceClass = pckg.genClasses.findFirst[
		  val cls = it.ecoreClass
		  cls.name == source.name
		]
		if(genSourceClass == null){
			throw new IllegalArgumentException("Derived feature pattern "+pattern.fullyQualifiedName+": Source EClass " + source.name + " not found in GenPackage " + pckg + "!")
		}
		return genSourceClass
	}

	def private findGenFeatureForFeature(GenClass genSourceClass, EStructuralFeature feature, Pattern pattern){
		val genFeature = genSourceClass.genFeatures.findFirst[
      val feat = it.ecoreFeature
      feat.name == feature.name
    ]
		if(genFeature == null){
			throw new IllegalArgumentException("Derived feature pattern "+pattern.fullyQualifiedName+": Feature " + feature.name + " not found in GenClass " + genSourceClass.name + "!")
		}
		return genFeature
	}

	def private findJavaProject(GenPackage pckg){
		// find java project
		val projectDir = pckg.genModel.modelProjectDirectory
		ProjectLocator::locateProject(projectDir,logger)
	}

	def private findJavaFile(GenPackage pckg, GenClass genSourceClass, IJavaProject javaProject){
		val prefix = pckg.getEcorePackage.name
		val suffix = pckg.classPackageSuffix
		val base = pckg.basePackage
		var packageNameTmp = ""
		if(base != null && base != ""){
			packageNameTmp = base
		}
		if(prefix != null && prefix != ""){
			if(packageNameTmp != ""){
				packageNameTmp = packageNameTmp + "." + prefix
			} else {
				packageNameTmp = prefix
			}
		}
		if(suffix != null && suffix != ""){
			if(packageNameTmp != ""){
				packageNameTmp = packageNameTmp + "." + suffix
			} else {
				packageNameTmp = suffix
			}
		}
		val packageName = packageNameTmp
		val implPackage = javaProject.packageFragments.findFirst[it.elementName == packageName]
		if(implPackage == null){
		  throw new IllegalArgumentException("Derived feature generation: Implementation package "+packageName+" not found!")
		}
		implPackage.compilationUnits.findFirst[it.elementName == genSourceClass.className+".java"]
	}

	def private ensureImports(AST ast, ASTRewrite rewrite, CompilationUnit astNode, TypeDeclaration type){
		val importListRewrite = rewrite.getListRewrite(astNode, CompilationUnit::IMPORTS_PROPERTY)
		// check import
		val imports = astNode.imports as List<ImportDeclaration>
		val handlerImport = imports.findFirst[
			it.name.fullyQualifiedName == IMPORT_QUALIFIER + "." + HANDLER_NAME
		]
		if(handlerImport == null){
			val handlerImportNew = ast.newImportDeclaration
			handlerImportNew.setName(ast.newQualifiedName(ast.newName(IMPORT_QUALIFIER), ast.newSimpleName(HANDLER_NAME)))
			importListRewrite.insertLast(handlerImportNew, null)
		}
		val kindImport = imports.findFirst[
			it.name.fullyQualifiedName == IMPORT_QUALIFIER + "."+ FEATUREKIND_IMPORT
		]
		if(kindImport == null){
			val kindImportNew = ast.newImportDeclaration
			kindImportNew.setName(ast.newQualifiedName(ast.newName(IMPORT_QUALIFIER),ast.newSimpleName(FEATUREKIND_IMPORT)))
			importListRewrite.insertLast(kindImportNew, null)
		}
		val helperImport = imports.findFirst[
			it.name.fullyQualifiedName == IMPORT_QUALIFIER + "." + HELPER_IMPORT
		]
		if(helperImport == null){
			val helperImportNew = ast.newImportDeclaration
			helperImportNew.setName(ast.newQualifiedName(ast.newName(IMPORT_QUALIFIER), ast.newSimpleName(HELPER_IMPORT)))
			importListRewrite.insertLast(helperImportNew, null)
		}
	}

	def private ensureHandlerField(AST ast, ListRewrite bodyDeclListRewrite, TypeDeclaration type, GenFeature feature){
		val handler = type.fields.findFirst[
			val fragments = it.fragments as List<VariableDeclarationFragment>
			fragments.exists()[
				it.name.identifier == feature.name+HANDLER_FIELD_SUFFIX
			]
		]

		if(handler == null){
			// insert handler
			val handlerFragment = ast.newVariableDeclarationFragment
			handlerFragment.setName(ast.newSimpleName(feature.name+HANDLER_FIELD_SUFFIX))
			val handlerField = ast.newFieldDeclaration(handlerFragment)
			val handlerType = ast.newSimpleType(ast.newSimpleName(HANDLER_NAME))
			// NOTE code for parameterized feature, ensure proper imports...
			//val handlerType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName(HANDLER_NAME)))
			//handlerType.typeArguments.add(ast.newSimpleType(ast.newSimpleName(feature.ecoreFeature.EType.name)))
			handlerField.setType(handlerType)
			handlerField.modifiers().add(ast.newModifier(Modifier.ModifierKeyword::PRIVATE_KEYWORD))
			val handlerTag = ast.newTagElement
			val tagText = ast.newTextElement
			tagText.text = "EMF-IncQuery handler for query-based feature "+feature.name
			handlerTag.fragments.add(tagText)
			val javaDoc = ast.newJavadoc
			javaDoc.tags.add(handlerTag)
			handlerField.javadoc = javaDoc
			bodyDeclListRewrite.insertLast(handlerField, null)
		}
	}

	def private removeHandlerField(AST ast, ListRewrite bodyDeclListRewrite, TypeDeclaration type, String featureName){
		val handler = type.fields.findFirst[
			val fragments = it.fragments as List<VariableDeclarationFragment>
			fragments.exists()[
				it.name.identifier == featureName+HANDLER_FIELD_SUFFIX
			]
		]

		if(handler != null){
			// remove handler
			bodyDeclListRewrite.remove(handler, null)
		}
	}

	def private ensureGetterMethod(AST ast, Document document, TypeDeclaration type, ASTRewrite rewrite, ListRewrite bodyDeclListRewrite,
		 GenClass sourceClass, GenFeature genFeature, Pattern pattern, QueryBasedFeatureParameters parameters){
		val sourceName =  parameters.sourceVar
		val targetName = parameters.targetVar
		val kind = parameters.kind
		val keepCache = parameters.keepCache

		val getMethod = findFeatureMethod(type, genFeature, "")
		val getGenMethod = findFeatureMethod(type, genFeature, "Gen")

		var methodSource = codeGen.methodBody(sourceClass, genFeature, pattern, sourceName, targetName, kind, keepCache)
		var dummyMethod = processDummyComputationUnit(methodSource.toString)

		if(getMethod != null){
			val javadoc = getMethod.javadoc
			var generatedBody = false
			if(javadoc != null){
				val tags = javadoc.tags as List<TagElement>
				val generatedTag = tags.findFirst[
					(it as TagElement).tagName == "@generated"
				]
				val derivedTag = tags.findFirst[
					(it as TagElement).tagName == "@query-based"
				]
				if(derivedTag == null && generatedTag != null && (generatedTag.fragments.size == 0)){
					// generated tag intact
					generatedBody = true
					// rename method to name+"Gen"
					val methodName = getMethod.name.identifier
					// create method
					val method = ASTNode::copySubtree(ast, getMethod) as MethodDeclaration
					rewrite.replace(method.name, ast.newSimpleName(methodName), null)
					rewrite.replace(method.body, dummyMethod.body, null)

					val methodtags = method.javadoc.tags as List<TagElement>
					val oldTag = methodtags.findFirst[
						(it as TagElement).tagName == "@generated"
					]
					//oldTag.tagName = "@derived"
					rewrite.set(oldTag,TagElement::TAG_NAME_PROPERTY,"@query-based",null)
					val tagsRewrite = rewrite.getListRewrite(oldTag,TagElement::FRAGMENTS_PROPERTY)
					val tagText = ast.newTextElement
					tagText.text = "getter created by EMF-IncQuery for query-based feature "+genFeature.name
					tagsRewrite.insertLast(tagText, null)
					bodyDeclListRewrite.insertLast(method, null)
					if(getGenMethod == null){
						rewrite.replace(getMethod.name,ast.newSimpleName(getMethod.name.identifier+"Gen"),null)
					} else {
						rewrite.replace(getMethod.name,ast.newSimpleName("_"+getMethod.name.identifier),null)
					}
				}
				if(derivedTag != null){
					generatedBody = true
					replaceMethodBody(ast, rewrite, getMethod.body, dummyMethod.body,
						javadoc, document, false, null,	null, null)
				}
			}


			if(!generatedBody){
				// generated tag dirty or javadoc null
				replaceMethodBody(ast, rewrite, getMethod.body, dummyMethod.body,
					javadoc, document, true, "@query-based",
					"getter created by EMF-IncQuery for query-based feature "+genFeature.name, null
				)
			}

		}
	}

	def private restoreGetterMethod(AST ast, Document document, ICompilationUnit compunit, TypeDeclaration type, ASTRewrite rewrite, ListRewrite bodyDeclListRewrite,
		 GenClass sourceClass, GenFeature genFeature){
		val getMethod = findFeatureMethod(type, genFeature, "")
		val getGenMethod = findFeatureMethod(type, genFeature, "Gen")

		if(getGenMethod != null){
			// there is a gen method, we can use that and remove the other
			if(getMethod != null){
				rewrite.replace(getGenMethod.name,ast.newSimpleName(getMethod.name.identifier),null)
				bodyDeclListRewrite.remove(getMethod, null)
			}
		} else {
			var methodSource = codeGen.defaultMethod(genFeature.ecoreFeature.many)
			var dummyMethod = processDummyComputationUnit(methodSource.toString)

			if(getMethod != null){
				val javadoc = getMethod.javadoc
				if(javadoc != null){
					val tags = javadoc.tags as List<TagElement>
					val derivedTag = tags.findFirst[
						(it as TagElement).tagName == "@query-basedd"
					]

					val originalTag = tags.findFirst[
						(it as TagElement).tagName == "@original"
					]

					val generatedTag = tags.findFirst[
						(it as TagElement).tagName == "@generated"
					]
					if(generatedTag != null && (generatedTag.fragments.size == 0)){
						// generated tag intact
						return
					}
					if(derivedTag != null && originalTag != null){
						if(originalTag.fragments.size != 0){
							// original tag found

							//rewrite.set(derivedTag,TagElement::TAG_NAME_PROPERTY,"@generated",null)
							val tagsRewrite = rewrite.getListRewrite(javadoc, Javadoc::TAGS_PROPERTY)
							tagsRewrite.remove(derivedTag, null)

							val tagFragments = originalTag.fragments
							val oldBody = new StringBuilder
							for(Object o : tagFragments){
								if(o instanceof TextElement){
									oldBody.append((o as TextElement).text)
								}
							}
							val oldMethod = compunit.prepareOriginalMethod(type, getMethod, oldBody)
							rewrite.replace(getMethod.body,oldMethod.body,null)
							tagsRewrite.remove(originalTag, null)
							return
						}
					}
					if(generatedTag != null){
						return
					}
				}
				ast.replaceMethodBody(rewrite, getMethod.body, dummyMethod.body, javadoc,
					document, false, "@generated","", "@query-based"
				)
			}
		}
	}

	def private findFeatureMethod(TypeDeclaration type, GenFeature genFeature, String suffix){
		type.methods.findFirst[
			if(genFeature.basicGet){
				it.name.identifier == "basic"+genFeature.getAccessor.toFirstUpper + suffix
			} else {
				it.name.identifier == genFeature.getAccessor + suffix
			}
		]
	}

	def private processDummyComputationUnit(String dummySource){
		val methodBodyParser = ASTParser::newParser(AST::JLS3)
		methodBodyParser.setSource(dummySource.toCharArray)
		val options = JavaCore::getOptions();
		JavaCore::setComplianceOptions(JavaCore::VERSION_1_5, options);
		methodBodyParser.setCompilerOptions(options);
		val dummyAST = methodBodyParser.createAST(null)
		val dummyCU = dummyAST as CompilationUnit
		val dummyType = dummyCU.types.get(0) as TypeDeclaration
		dummyType.methods.get(0) as MethodDeclaration
	}

	def private prepareOriginalMethod(ICompilationUnit cu, TypeDeclaration type, MethodDeclaration method, StringBuilder originalBody){
		originalBody.insert(0,'''public class Dummy{public void DummyMethod()''')
		cu.imports.forEach[
			originalBody.insert(0,it.source+"\n")
		]
		originalBody.append("}")
		val dummyCU = originalBody.toString
		dummyCU.processDummyComputationUnit
	}

	def private replaceMethodBody(
		AST ast, ASTRewrite rewrite, Block oldBody, Block newBody,
		Javadoc javadoc, Document document, boolean keepOld,
		String newTagName, String newTagText, String removeTagName
	){
		// generated tag dirty or javadoc null
		// copy method body into block comment with @original fragment
		val tags = javadoc.tags as List<TagElement>
		val tagsRewrite = rewrite.getListRewrite(javadoc, Javadoc::TAGS_PROPERTY)
		val originalTag = tags.findFirst[
			(it as TagElement).tagName == "@original"
		]
		val newTag = tags.findFirst[
			(it as TagElement).tagName == newTagName
		]
		if(removeTagName != null){
			val removeTag = tags.findFirst[
					(it as TagElement).tagName == removeTagName
			]
			if(removeTag != null){
				tagsRewrite.remove(removeTag, null)
			}
		}
		if(originalTag == null){
			if(keepOld){
				val tag = ast.newTagElement
				tag.tagName = "@original"
				val text = ast.newTextElement
				text.setText(oldBody.toString.replace("\n",document.defaultLineDelimiter))
				tag.fragments.add(text)
				//tags.add(tag)
				tagsRewrite.insertLast(tag,null)
			}
		} else {
			if(!keepOld){
				tagsRewrite.remove(originalTag,null)
			}
		}
		if(newTag == null){
			val newTagToInsert = ast.newTagElement
			newTagToInsert.tagName = newTagName
			val tagText = ast.newTextElement
			tagText.text = newTagText
			newTagToInsert.fragments.add(tagText)
			tagsRewrite.insertLast(newTagToInsert,null)
		}
		// overwrite method body
		rewrite.replace(oldBody, newBody, null)
	}

}
