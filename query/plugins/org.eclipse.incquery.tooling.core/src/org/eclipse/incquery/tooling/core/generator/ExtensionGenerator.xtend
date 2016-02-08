/*******************************************************************************
 * Copyright (c) 2010-2012, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.tooling.core.generator

import org.eclipse.incquery.tooling.core.project.XmlDocumentHelper
import org.w3c.dom.Document
import org.w3c.dom.Element

class ExtensionGenerator {
	
	Document document = XmlDocumentHelper.emptyXmlDocument
	
	def contribExtension(String id, String point, (Element) => void initializer) {
		val ex = document.createElement("extension")
		ex.setAttribute("id", id)
		
		ex.setAttribute("point", point)
		ex.init(initializer)
		ex.normalize
		new ExtensionData(ex)
	}
	
	def contribElement(Element parent, String name, (Element) => void initializer) {
		val el = document.createElement(name)
		parent.appendChild(el)
		el.init(initializer)
	}
	
	def contribAttribute(Element element, String name, String value) {
		element.setAttribute(name, value)
	}
	
	def private <T> T init (T obj, (T)=>void init) {
		init.apply(obj)
		return obj
	}
}