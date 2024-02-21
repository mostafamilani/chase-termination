/*
 * #%L
 * ELK OWL API Binding
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ca.uwo.chaseTermination.owlapi;

import org.semanticweb.elk.owl.interfaces.*;
import org.semanticweb.elk.owl.visitors.ElkEntityVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/**
 * Converter from ElkEntities to OWL API entities.
 * 
 * @author Markus Kroetzsch
 * 
 */
public final class ElkEntityConverter implements
		ElkEntityVisitor<OWLEntity> {
	
	final OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
	
	private static ElkEntityConverter INSTANCE_ = new ElkEntityConverter();
	
	private ElkEntityConverter() {
	}
	
	public static ElkEntityConverter getInstance() {
		return INSTANCE_;
	}
	

	@Override
	public OWLEntity visit(ElkAnnotationProperty elkAnnotationProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClass visit(ElkClass elkClass) {
		String iri = elkClass.getIri().getFullIriAsString();
		return owlDataFactory.getOWLClass(IRI.create(iri));
	}

	@Override
	public OWLEntity visit(ElkDataProperty elkDataProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLEntity visit(ElkDatatype elkDatatype) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLNamedIndividual visit(ElkNamedIndividual elkNamedIndividual) {
		String iri = elkNamedIndividual.getIri().getFullIriAsString();
		return owlDataFactory.getOWLNamedIndividual(IRI.create(iri));
	}

	@Override
	public OWLObjectProperty visit(ElkObjectProperty elkObjectProperty) {
		String iri = elkObjectProperty.getIri().getFullIriAsString();
		return owlDataFactory.getOWLObjectProperty(IRI.create(iri));
	}

}
