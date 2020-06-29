/*********************************************************************
* Copyright (c) 2020 The University of York.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.picto.transformers.elements;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.epsilon.picto.ViewContent;
import org.eclipse.epsilon.picto.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class ReplacingElementTransformer extends AbstractHtmlElementTransformer {
	
	protected XmlHelper xmlHelper = new XmlHelper();
	
	protected void replace(Element element, ViewContent viewContent, boolean svgContent) {
		if (svgContent) {
			try {
				Document document = xmlHelper.parse(viewContent.getText());
				Element svg = document.getDocumentElement();
				element.getOwnerDocument().importNode(svg, true);
				element.getOwnerDocument().adoptNode(svg);
				element.getParentNode().replaceChild(svg, element);
				
				NamedNodeMap attributes = element.getAttributes();
				for (int i=0;i<attributes.getLength();i++) {
					Node node = attributes.item(i);
					svg.setAttribute(node.getNodeName(), node.getNodeValue());
				}
				
				return;
			} catch (Exception e) {
				element.getOwnerDocument().renameNode(element, element.getNamespaceURI(), "b");
				element.setTextContent(e.getMessage());
				return;
			}
		} else {
			try {
				File temp = Files.createTempFile("picto-iframe", ".html").toFile();
				Files.write(Paths.get(temp.toURI()), viewContent.getText().getBytes());
				element.getOwnerDocument().renameNode(element, element.getNamespaceURI(), "iframe");
				element.setAttribute("src", temp.getAbsolutePath());
			} catch (Exception ex) {}
		}
	}
	
}
