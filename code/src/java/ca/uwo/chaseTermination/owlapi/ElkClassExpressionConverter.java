/*
 * #%L
 * ELK OWL API
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
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
/**
 * @author Yevgeny Kazakov, Jul 1, 2011
 */
package ca.uwo.chaseTermination.owlapi;

import org.semanticweb.elk.owl.interfaces.*;
import org.semanticweb.elk.owl.visitors.ElkClassExpressionVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/**
 * Converter of ElkClassExpressions to OWL API class expressions.
 * 
 * @author Yevgeny Kazakov
 * @author Markus Kroetzsch
 */
public final class ElkClassExpressionConverter implements
		ElkClassExpressionVisitor<OWLClassExpression> {
	
	final OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();

	private static ElkClassExpressionConverter INSTANCE_ = new ElkClassExpressionConverter();

	private ElkClassExpressionConverter() {
	}

	public static ElkClassExpressionConverter getInstance() {
		return INSTANCE_;
	}

	@Override
	public OWLClass visit(ElkClass elkClass) {
		return ElkEntityConverter.getInstance().visit(elkClass);
	}

	@Override
	public OWLClassExpression visit(ElkDataAllValuesFrom elkDataAllValuesFrom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkDataExactCardinality elkDataExactCardinality) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkDataExactCardinalityQualified elkDataExactCardinalityQualified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkDataHasValue elkDataHasValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkDataMaxCardinality elkDataMaxCardinality) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkDataMaxCardinalityQualified elkDataMaxCardinalityQualified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkDataMinCardinality elkDataMinCardinality) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkDataMinCardinalityQualified elkDataMinCardinalityQualified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkDataSomeValuesFrom elkDataSomeValuesFrom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkObjectAllValuesFrom elkObjectAllValuesFrom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkObjectComplementOf elkObjectComplementOf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkObjectExactCardinality elkObjectExactCardinality) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkObjectExactCardinalityQualified elkObjectExactCardinalityQualified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkObjectHasSelf elkObjectHasSelf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkObjectHasValue elkObjectHasValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLObjectIntersectionOf visit(
			ElkObjectIntersectionOf elkObjectIntersectionOf) {
		// TODO Support this constructor
		throw new ConverterException("Not yet implemented.");
	}

	@Override
	public OWLClassExpression visit(
			ElkObjectMaxCardinality elkObjectMaxCardinality) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkObjectMaxCardinalityQualified elkObjectMaxCardinalityQualified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkObjectMinCardinality elkObjectMaxCardinality) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(
			ElkObjectMinCardinalityQualified elkObjectMinCardinalityQualified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(ElkObjectOneOf elkObjectOneOf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLObjectSomeValuesFrom visit(
			ElkObjectSomeValuesFrom elkObjectSomeValuesFrom) {
		// TODO Support this constructor
		throw new ConverterException("Not yet implemented.");
	}

	@Override
	public OWLClassExpression visit(ElkObjectUnionOf elkObjectUnionOf) {
		// TODO Auto-generated method stub
		return null;
	}

}
