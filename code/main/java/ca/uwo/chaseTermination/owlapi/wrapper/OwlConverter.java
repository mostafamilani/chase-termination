/*
 * #%L
 * ELK OWL API Binding
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
package ca.uwo.chaseTermination.owlapi.wrapper;

import org.semanticweb.elk.owl.interfaces.*;
import org.semanticweb.elk.owl.iris.ElkFullIri;
import org.semanticweb.elk.owl.iris.ElkIri;
import org.semanticweb.owlapi.model.*;

/**
 * 
 * @author Yevgeny Kazakov
 * @author Pavel Klinov 
 *
 * pavel.klinov@uni-ulm.de
 */
public class OwlConverter {

	private static OwlConverter INSTANCE_ = new OwlConverter();

	private OwlConverter() {
	}

	public static OwlConverter getInstance() {
		return INSTANCE_;
	}

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlCorrespondingObjectConverterVisitor CONVERTER = OwlCorrespondingObjectConverterVisitor
			.getInstance();

	protected static OwlAxiomConverterVisitor OWL_AXIOM_CONVERTER = OwlAxiomConverterVisitor
			.getInstance();

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlAnnotationAxiomConverterVisitor OWL_ANNOTATION_AXIOM_CONVERTER = OwlAnnotationAxiomConverterVisitor
			.getInstance();

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlClassAxiomConverterVisitor OWL_CLASS_AXIOM_CONVERTER = OwlClassAxiomConverterVisitor
			.getInstance();

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlObjectPropertyAxiomConverterVisitor OWL_OBJECT_PROPERTY_AXIOM_CONVERTER = OwlObjectPropertyAxiomConverterVisitor
			.getInstance();

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlDataPropertyAxiomConverterVisitor OWL_DATA_PROPERTY_AXIOM_CONVERTER = OwlDataPropertyAxiomConverterVisitor
			.getInstance();

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlIndividualAxiomConverterVisitor OWL_INDIVIDUAL_AXIOM_CONVERTER = OwlIndividualAxiomConverterVisitor
			.getInstance();

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlObjectPropertyExpressionConverterVisitor OWL_OBJECT_PROPERTY_EXPRESSION_CONVERTER = OwlObjectPropertyExpressionConverterVisitor
			.getInstance();

	protected static ca.uwo.chaseTermination.owlapi.wrapper.OwlAnnotationSubjectValueVisitor OWL_ANNOTATION_CONVERTER = OwlAnnotationSubjectValueVisitor
			.getInstance();

	@SuppressWarnings("static-method")
	public ElkAnnotationProperty convert(
			OWLAnnotationProperty owlAnnotationProperty) {
		return new ElkAnnotationPropertyWrap<OWLAnnotationProperty>(
				owlAnnotationProperty);
	}

	@SuppressWarnings("static-method")
	public ElkAnonymousIndividual convert(
			OWLAnonymousIndividual owlAnonymousIndividual) {
		return new ElkAnonymousIndividualWrap<OWLAnonymousIndividual>(
				owlAnonymousIndividual);
	}

	@SuppressWarnings("static-method")
	public ElkAnnotationAxiom convert(OWLAnnotationAxiom owlAnnotationAxiom) {
		return owlAnnotationAxiom.accept(OWL_ANNOTATION_AXIOM_CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkAssertionAxiom convert(OWLIndividualAxiom owlIndividualAxiom) {
		return owlIndividualAxiom.accept(OWL_INDIVIDUAL_AXIOM_CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkAsymmetricObjectPropertyAxiom convert(
			OWLAsymmetricObjectPropertyAxiom owlAsymmetricObjectPropertyAxiom) {
		return new ElkAsymmetricObjectPropertyAxiomWrap<OWLAsymmetricObjectPropertyAxiom>(
				owlAsymmetricObjectPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkAxiom convert(OWLAxiom owlAxiom) {
		return owlAxiom.accept(OWL_AXIOM_CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkClassAssertionAxiom convert(
			OWLClassAssertionAxiom owlClassAssertionAxiom) {
		return new ElkClassAssertionAxiomWrap<OWLClassAssertionAxiom>(
				owlClassAssertionAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkClassAxiom convert(OWLClassAxiom owlClassAxiom) {
		return owlClassAxiom.accept(OWL_CLASS_AXIOM_CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkClassExpression convert(OWLClassExpression owlClassExpression) {
		return owlClassExpression.accept(CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkClass convert(OWLClass owlClass) {
		return new ElkClassWrap<OWLClass>(owlClass);
	}

	@SuppressWarnings("static-method")
	public ElkDataAllValuesFrom convert(
			OWLDataAllValuesFrom owlDataAllValuesFrom) {
		return new ElkDataAllValuesFromWrap<OWLDataAllValuesFrom>(
				owlDataAllValuesFrom);
	}

	@SuppressWarnings("static-method")
	public ElkDataComplementOf convert(OWLDataComplementOf owlDataComplementOf) {
		return new ElkDataComplementOfWrap<OWLDataComplementOf>(
				owlDataComplementOf);
	}

	@SuppressWarnings("static-method")
	public ElkDataExactCardinality convert(
			OWLDataExactCardinality owlDataExactCardinality) {
		if (owlDataExactCardinality.isQualified())
			return new ElkDataExactCardinalityQualifiedWrap<OWLDataExactCardinality>(
					owlDataExactCardinality);
		else
			return new ElkDataExactCardinalityWrap<OWLDataExactCardinality>(
					owlDataExactCardinality);
	}

	@SuppressWarnings("static-method")
	public ElkDataHasValue convert(OWLDataHasValue owlDataHasValue) {
		return new ElkDataHasValueWrap<OWLDataHasValue>(owlDataHasValue);
	}

	@SuppressWarnings("static-method")
	public ElkDataIntersectionOf convert(
			OWLDataIntersectionOf owlDataIntersectionOf) {
		return new ElkDataIntersectionOfWrap<OWLDataIntersectionOf>(
				owlDataIntersectionOf);
	}

	@SuppressWarnings("static-method")
	public ElkDataMaxCardinality convert(
			OWLDataMaxCardinality owlDataMaxCardinality) {
		if (owlDataMaxCardinality.isQualified())
			return new ElkDataMaxCardinalityQualifiedWrap<OWLDataMaxCardinality>(
					owlDataMaxCardinality);
		else
			return new ElkDataMaxCardinalityWrap<OWLDataMaxCardinality>(
					owlDataMaxCardinality);
	}

	@SuppressWarnings("static-method")
	public ElkDataMinCardinality convert(
			OWLDataMinCardinality owlDataMinCardinality) {
		if (owlDataMinCardinality.isQualified())
			return new ElkDataMinCardinalityQualifiedWrap<OWLDataMinCardinality>(
					owlDataMinCardinality);
		else
			return new ElkDataMinCardinalityWrap<OWLDataMinCardinality>(
					owlDataMinCardinality);
	}

	@SuppressWarnings("static-method")
	public ElkDataOneOf convert(OWLDataOneOf owlDataOneOf) {
		return new ElkDataOneOfWrap<OWLDataOneOf>(owlDataOneOf);
	}

	@SuppressWarnings("static-method")
	public ElkDataPropertyAssertionAxiom convert(
			OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom) {
		return new ElkDataPropertyAssertionAxiomWrap<OWLDataPropertyAssertionAxiom>(
				owlDataPropertyAssertionAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkDataPropertyAxiom convert(
			OWLDataPropertyAxiom owlDataPropertyAxiom) {
		return owlDataPropertyAxiom.accept(OWL_DATA_PROPERTY_AXIOM_CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkDataPropertyDomainAxiom convert(
			OWLDataPropertyDomainAxiom owlDataPropertyDomainAxiom) {
		return new ElkDataPropertyDomainAxiomWrap<OWLDataPropertyDomainAxiom>(
				owlDataPropertyDomainAxiom);
	}

	public ElkDataPropertyExpression convert(
			OWLDataPropertyExpression owlDataPropertyExpression) {
		return this.convert(owlDataPropertyExpression.asOWLDataProperty());
	}

	@SuppressWarnings("static-method")
	public ElkDataPropertyRangeAxiom convert(
			OWLDataPropertyRangeAxiom owlDataPropertyRangeAxiom) {
		return new ElkDataPropertyRangeAxiomWrap<OWLDataPropertyRangeAxiom>(
				owlDataPropertyRangeAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkDataProperty convert(OWLDataProperty owlDataProperty) {
		return new ElkDataPropertyWrap<OWLDataProperty>(owlDataProperty);
	}

	@SuppressWarnings("static-method")
	public ElkDataRange convert(OWLDataRange owlDataRange) {
		return owlDataRange.accept(CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkDataSomeValuesFrom convert(
			OWLDataSomeValuesFrom owlDataSomeValuesFrom) {
		return new ElkDataSomeValuesFromWrap<OWLDataSomeValuesFrom>(
				owlDataSomeValuesFrom);
	}

	@SuppressWarnings("static-method")
	public ElkDatatypeRestriction convert(
			OWLDatatypeRestriction owlDatatypeRestriction) {
		return new ElkDatatypeRestrictionWrap<OWLDatatypeRestriction>(
				owlDatatypeRestriction);
	}

	@SuppressWarnings("static-method")
	public ElkDatatypeDefinitionAxiom convert(
			OWLDatatypeDefinitionAxiom owlDatatypeDefinition) {
		return new ElkDatatypeDefinitionAxiomWrap<OWLDatatypeDefinitionAxiom>(
				owlDatatypeDefinition);
	}

	@SuppressWarnings("static-method")
	public ElkDatatype convert(OWLDatatype owlDatatype) {
		return new ElkDatatypeWrap<OWLDatatype>(owlDatatype);
	}

	@SuppressWarnings("static-method")
	public ElkDataUnionOf convert(OWLDataUnionOf owlDataUnionOf) {
		return new ElkDataUnionOfWrap<OWLDataUnionOf>(owlDataUnionOf);
	}

	@SuppressWarnings("static-method")
	public ElkDeclarationAxiom convert(OWLDeclarationAxiom owlDeclarationAxiom) {
		return new ElkDeclarationAxiomWrap<OWLDeclarationAxiom>(
				owlDeclarationAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkDifferentIndividualsAxiom convert(
			OWLDifferentIndividualsAxiom owlDifferentIndividualsAxiom) {
		return new ElkDifferentIndividualsAxiomWrap<OWLDifferentIndividualsAxiom>(
				owlDifferentIndividualsAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkDisjointClassesAxiom convert(
			OWLDisjointClassesAxiom owlDisjointClassesAxiom) {
		return new ElkDisjointClassesAxiomWrap<OWLDisjointClassesAxiom>(
				owlDisjointClassesAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkDisjointDataPropertiesAxiom convert(
			OWLDisjointDataPropertiesAxiom owlDisjointDataPropertiesAxiom) {
		return new ElkDisjointDataPropertiesAxiomWrap<OWLDisjointDataPropertiesAxiom>(
				owlDisjointDataPropertiesAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkDisjointObjectPropertiesAxiom convert(
			OWLDisjointObjectPropertiesAxiom owlDisjointObjectPropertiesAxiom) {
		return new ElkDisjointObjectPropertiesAxiomWrap<OWLDisjointObjectPropertiesAxiom>(
				owlDisjointObjectPropertiesAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkDisjointUnionAxiom convert(
			OWLDisjointUnionAxiom owlDisjointUnionAxiom) {
		return new ElkDisjointUnionAxiomWrap<OWLDisjointUnionAxiom>(
				owlDisjointUnionAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkEntity convert(OWLEntity owlEntity) {
		return owlEntity.accept(CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkEquivalentClassesAxiom convert(
			OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
		return new ElkEquivalentClassesAxiomWrap<OWLEquivalentClassesAxiom>(
				owlEquivalentClassesAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkEquivalentDataPropertiesAxiom convert(
			OWLEquivalentDataPropertiesAxiom owlEquivalentDataPropertiesAxiom) {
		return new ElkEquivalentDataPropertiesAxiomWrap<OWLEquivalentDataPropertiesAxiom>(
				owlEquivalentDataPropertiesAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkEquivalentObjectPropertiesAxiom convert(
			OWLEquivalentObjectPropertiesAxiom owlEquivalentObjectPropertiesAxiom) {
		return new ElkEquivalentObjectPropertiesAxiomWrap<OWLEquivalentObjectPropertiesAxiom>(
				owlEquivalentObjectPropertiesAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkFacetRestriction convert(OWLFacetRestriction owlFacetRestriction) {
		return new ElkFacetRestrictionWrap<OWLFacetRestriction>(
				owlFacetRestriction);
	}

	@SuppressWarnings("static-method")
	public ElkFunctionalDataPropertyAxiom convert(
			OWLFunctionalDataPropertyAxiom owlFunctionalDataPropertyAxiom) {
		return new ElkFunctionalDataPropertyAxiomWrap<OWLFunctionalDataPropertyAxiom>(
				owlFunctionalDataPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkFunctionalObjectPropertyAxiom convert(
			OWLFunctionalObjectPropertyAxiom owlFunctionalObjectPropertyAxiom) {
		return new ElkFunctionalObjectPropertyAxiomWrap<OWLFunctionalObjectPropertyAxiom>(
				owlFunctionalObjectPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkIndividual convert(OWLIndividual owlIndividual) {
		return owlIndividual.accept(CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkInverseFunctionalObjectPropertyAxiom convert(
			OWLInverseFunctionalObjectPropertyAxiom owlInverseFunctionalObjectPropertyAxiom) {
		return new ElkInverseFunctionalObjectPropertyAxiomWrap<OWLInverseFunctionalObjectPropertyAxiom>(
				owlInverseFunctionalObjectPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkInverseObjectPropertiesAxiom convert(
			OWLInverseObjectPropertiesAxiom owlInverseObjectPropertiesAxiom) {
		return new ElkInverseObjectPropertiesAxiomWrap<OWLInverseObjectPropertiesAxiom>(
				owlInverseObjectPropertiesAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkIrreflexiveObjectPropertyAxiom convert(
			OWLIrreflexiveObjectPropertyAxiom owlIrreflexiveObjectPropertyAxiom) {
		return new ElkIrreflexiveObjectPropertyAxiomWrap<OWLIrreflexiveObjectPropertyAxiom>(
				owlIrreflexiveObjectPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkLiteral convert(OWLLiteral owlLiteral) {
		return new ElkLiteralWrap<OWLLiteral>(owlLiteral);
	}

	@SuppressWarnings("static-method")
	public ElkNamedIndividual convert(OWLNamedIndividual owlNamedIndividual) {
		return new ElkNamedIndividualWrap<OWLNamedIndividual>(
				owlNamedIndividual);
	}

	@SuppressWarnings("static-method")
	public ElkNegativeDataPropertyAssertionAxiom convert(
			OWLNegativeDataPropertyAssertionAxiom owlNegativeDataPropertyAssertionAxiom) {
		return new ElkNegativeDataPropertyAssertionAxiomWrap<OWLNegativeDataPropertyAssertionAxiom>(
				owlNegativeDataPropertyAssertionAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkNegativeObjectPropertyAssertionAxiom convert(
			OWLNegativeObjectPropertyAssertionAxiom owlNegativeObjectPropertyAssertionAxiom) {
		return new ElkNegativeObjectPropertyAssertionAxiomWrap<OWLNegativeObjectPropertyAssertionAxiom>(
				owlNegativeObjectPropertyAssertionAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkObjectAllValuesFrom convert(
			OWLObjectAllValuesFrom owlObjectAllValuesFrom) {
		return new ElkObjectAllValuesFromWrap<OWLObjectAllValuesFrom>(
				owlObjectAllValuesFrom);
	}

	@SuppressWarnings("static-method")
	public ElkObjectComplementOf convert(
			OWLObjectComplementOf owlObjectComplementOf) {
		return new ElkObjectComplementOfWrap<OWLObjectComplementOf>(
				owlObjectComplementOf);
	}

	@SuppressWarnings("static-method")
	public ElkObjectExactCardinality convert(
			OWLObjectExactCardinality owlObjectExactCardinality) {
		if (owlObjectExactCardinality.isQualified())
			return new ElkObjectExactCardinalityQualifiedWrap<OWLObjectExactCardinality>(
					owlObjectExactCardinality);
		else
			return new ElkObjectExactCardinalityWrap<OWLObjectExactCardinality>(
					owlObjectExactCardinality);
	}

	@SuppressWarnings("static-method")
	public ElkObjectHasSelf convert(OWLObjectHasSelf owlObjectHasSelf) {
		return new ElkObjectHasSelfWrap<OWLObjectHasSelf>(owlObjectHasSelf);
	}

	@SuppressWarnings("static-method")
	public ElkObjectHasValue convert(OWLObjectHasValue owlObjectHasValue) {
		return new ElkObjectHasValueWrap<OWLObjectHasValue>(owlObjectHasValue);
	}

	@SuppressWarnings("static-method")
	public ElkObjectIntersectionOf convert(
			OWLObjectIntersectionOf owlObjectIntersectionOf) {
		return new ElkObjectIntersectionOfWrap<OWLObjectIntersectionOf>(
				owlObjectIntersectionOf);
	}

	@SuppressWarnings("static-method")
	public ElkObjectMaxCardinality convert(
			OWLObjectMaxCardinality owlObjectMaxCardinality) {
		if (owlObjectMaxCardinality.isQualified())
			return new ElkObjectMaxCardinalityQualifiedWrap<OWLObjectMaxCardinality>(
					owlObjectMaxCardinality);
		else
			return new ElkObjectMaxCardinalityWrap<OWLObjectMaxCardinality>(
					owlObjectMaxCardinality);
	}

	@SuppressWarnings("static-method")
	public ElkObjectMinCardinality convert(
			OWLObjectMinCardinality owlObjectMaxCardinality) {
		if (owlObjectMaxCardinality.isQualified())
			return new ElkObjectMinCardinalityQualifiedWrap<OWLObjectMinCardinality>(
					owlObjectMaxCardinality);
		else
			return new ElkObjectMinCardinalityWrap<OWLObjectMinCardinality>(
					owlObjectMaxCardinality);
	}

	@SuppressWarnings("static-method")
	public ElkObjectOneOf convert(OWLObjectOneOf owlObjectOneOf) {
		return new ElkObjectOneOfWrap<OWLObjectOneOf>(owlObjectOneOf);
	}

	@SuppressWarnings("static-method")
	public ElkObjectPropertyAssertionAxiom convert(
			OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom) {
		return new ElkObjectPropertyAssertionAxiomWrap<OWLObjectPropertyAssertionAxiom>(
				owlObjectPropertyAssertionAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkObjectPropertyAxiom convert(
			OWLObjectPropertyAxiom owlObjectPropertyAxiom) {
		return owlObjectPropertyAxiom
				.accept(OWL_OBJECT_PROPERTY_AXIOM_CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkObjectPropertyChain convert(
			OWLSubPropertyChainOfAxiom owlSubPropertyChainOfAxiom) {
		return new ElkObjectPropertyChainWrap<OWLSubPropertyChainOfAxiom>(
				owlSubPropertyChainOfAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkObjectPropertyDomainAxiom convert(
			OWLObjectPropertyDomainAxiom owlObjectPropertyDomainAxiom) {
		return new ElkObjectPropertyDomainAxiomWrap<OWLObjectPropertyDomainAxiom>(
				owlObjectPropertyDomainAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkObjectPropertyExpression convert(
			OWLObjectPropertyExpression objectPropertyExpression) {
		return objectPropertyExpression
				.accept(OWL_OBJECT_PROPERTY_EXPRESSION_CONVERTER);
	}

	@SuppressWarnings("static-method")
	public ElkObjectPropertyRangeAxiom convert(
			OWLObjectPropertyRangeAxiom owlObjectPropertyRangeAxiom) {
		return new ElkObjectPropertyRangeAxiomWrap<OWLObjectPropertyRangeAxiom>(
				owlObjectPropertyRangeAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkObjectProperty convert(OWLObjectProperty owlObjectProperty) {
		return new ElkObjectPropertyWrap<OWLObjectProperty>(owlObjectProperty);
	}

	@SuppressWarnings("static-method")
	public ElkObjectSomeValuesFrom convert(
			OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {
		return new ElkObjectSomeValuesFromWrap<OWLObjectSomeValuesFrom>(
				owlObjectSomeValuesFrom);
	}

	@SuppressWarnings("static-method")
	public ElkObjectUnionOf convert(OWLObjectUnionOf owlObjectUnionOf) {
		return new ElkObjectUnionOfWrap<OWLObjectUnionOf>(owlObjectUnionOf);
	}

	@SuppressWarnings("static-method")
	public ElkReflexiveObjectPropertyAxiom convert(
			OWLReflexiveObjectPropertyAxiom owlReflexiveObjectPropertyAxiom) {
		return new ElkReflexiveObjectPropertyAxiomWrap<OWLReflexiveObjectPropertyAxiom>(
				owlReflexiveObjectPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkSameIndividualAxiom convert(
			OWLSameIndividualAxiom owlSameIndividualAxiom) {
		return new ElkSameIndividualAxiomWrap<OWLSameIndividualAxiom>(
				owlSameIndividualAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkSubClassOfAxiom convert(OWLSubClassOfAxiom owlSubClassOfAxiom) {
		return new ElkSubClassOfAxiomWrap<OWLSubClassOfAxiom>(
				owlSubClassOfAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkSubDataPropertyOfAxiom convert(
			OWLSubDataPropertyOfAxiom owlSubDataPropertyOfAxiom) {
		return new ElkSubDataPropertyOfAxiomWrap<OWLSubDataPropertyOfAxiom>(
				owlSubDataPropertyOfAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkSubObjectPropertyOfAxiom convert(
			OWLSubObjectPropertyOfAxiom owlSubObjectPropertyOfAxiom) {
		return new ElkSubObjectPropertyOfAxiomWrap<OWLSubObjectPropertyOfAxiom>(
				owlSubObjectPropertyOfAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkSymmetricObjectPropertyAxiom convert(
			OWLSymmetricObjectPropertyAxiom owlSymmetricObjectPropertyAxiom) {
		return new ElkSymmetricObjectPropertyAxiomWrap<OWLSymmetricObjectPropertyAxiom>(
				owlSymmetricObjectPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkTransitiveObjectPropertyAxiom convert(
			OWLTransitiveObjectPropertyAxiom owlTransitiveObjectPropertyAxiom) {
		return new ElkTransitiveObjectPropertyAxiomWrap<OWLTransitiveObjectPropertyAxiom>(
				owlTransitiveObjectPropertyAxiom);
	}

	@SuppressWarnings("static-method")
	public ElkHasKeyAxiom convert(OWLHasKeyAxiom owlHasKey) {
		return new ElkHasKeyAxiomWrap<OWLHasKeyAxiom>(owlHasKey);
	}

	@SuppressWarnings("static-method")
	public ElkAnnotationSubject convert(OWLAnnotationSubject subject) {
		return OWL_ANNOTATION_CONVERTER.visit(subject);
	}

	@SuppressWarnings("static-method")
	public ElkAnnotationValue convert(OWLAnnotationValue value) {
		return OWL_ANNOTATION_CONVERTER.visit(value);
	}

	@SuppressWarnings("static-method")
	public ElkIri convert(IRI iri) {
		return new ElkFullIri(iri.toString());
	}

	/**
	 * @param axiom
	 *            the owl axiom to test
	 * @return {@code true} if the owl axiom can be converted to ELK axiom
	 */
	@SuppressWarnings("static-method")
	public boolean isRelevantAxiom(OWLAxiom axiom) {
		return axiom.isLogicalAxiom() || axiom.isOfType(AxiomType.DECLARATION);
	}

	public ElkSWRLRule convert(SWRLRule rule) {
		return new ElkSWRLRuleWrap();
	}
}
