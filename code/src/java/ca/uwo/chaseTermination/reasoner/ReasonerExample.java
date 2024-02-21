package ca.uwo.chaseTermination.reasoner;

import ca.uwo.chaseTermination.owlapi.ElkReasonerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ReasonerExample {

    private static final Logger log = LogManager.getLogger(ReasonerExample.class);

    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager inputOntologyManager = OWLManager.createOWLOntologyManager();
        OWLOntologyManager outputOntologyManager = OWLManager.createOWLOntologyManager();

        // Load your ontology
        OWLOntology ont = inputOntologyManager.loadOntologyFromOntologyDocument(new File("owl-test.owl"));

        // Create an ELK reasoner.
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ont);

        // Classify the ontology.
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//        getSameIndividuals
//        getUnsatisfiableClasses
        //HOW TO GET INSTANCES BY CLASS
        for (OWLClass clazz : ont.getClassesInSignature()) {
            for (Node<OWLNamedIndividual> individual : reasoner.getInstances(
                    clazz, true)) {
                System.out.println(clazz + "("
                        + individual.getRepresentativeElement() + ")");
            }
        }

        System.out.println("--------------------------------------------");

        for (OWLClass clazz : ont.getClassesInSignature()) {
            for (OWLClass owlClass : reasoner.getEquivalentClasses(clazz)) {
                System.out.println(clazz + " + " + owlClass);
            }
        }

        System.out.println("--------------------------------------------");

        for (OWLClass clazz : ont.getClassesInSignature()) {
            for (Node<OWLClass> owlClass : reasoner.getSubClasses(clazz, true)) {
                System.out.println(clazz + " + " + owlClass);
            }
        }

        System.out.println("--------------------------------------------");

        for (OWLClass clazz : ont.getClassesInSignature()) {
            for (Node<OWLClass> owlClass : reasoner.getSuperClasses(clazz, true)) {
                System.out.println(clazz + " + " + owlClass);
            }
        }

        System.out.println("--------------------------------------------");

        for(OWLNamedIndividual individual : ont.getIndividualsInSignature()) {
            System.out.println(individual + " + " + reasoner.getTypes(individual, true));
        }

        System.out.println("--------------------------------------------");

        System.out.println(reasoner.getUnsatisfiableClasses());
//        reasoner.isSatisfiable(class)
        System.out.println(reasoner.isConsistent());
        System.out.println("--------------------------------------------");

        // To generate an inferred ontology we use implementations of
        // inferred axiom generators
        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        //Tells about the classes and their relationship
        gens.add(new InferredSubClassAxiomGenerator());
        //Tells which classes the individuals belong to
        gens.add(new InferredClassAssertionAxiomGenerator());
        //Gives us equivalent classes
        gens.add(new InferredEquivalentClassAxiomGenerator());

        gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());

//        TODO: NOT IMPLEMENTED
//        gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
//        gens.add(new InferredEquivalentDataPropertiesAxiomGenerator());
//        gens.add(new InferredSubDataPropertyAxiomGenerator());
//        gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
//        gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
//        gens.add(new InferredSubObjectPropertyAxiomGenerator());
//        gens.add(new InferredDisjointClassesAxiomGenerator());
//        gens.add(new InferredPropertyAssertionGenerator());
        // Put the inferred axioms into a fresh empty ontology.
        OWLOntology infOnt = outputOntologyManager.createOntology();
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        iog.fillOntology(outputOntologyManager.getOWLDataFactory(), infOnt);

        for (OWLOntology owlOntology : outputOntologyManager.getOntologies()) {
            System.out.println("class in signature: " + owlOntology.getClassesInSignature());
            System.out.println("axioms: " + owlOntology.getAxioms());
            System.out.println("annotations: " + owlOntology.getAnnotations());
            System.out.println("class Axioms: " + owlOntology.getGeneralClassAxioms());
            System.out.println("anonymous individuals: " + owlOntology.getAnonymousIndividuals());
            System.out.println("data types in signature: " + owlOntology.getDatatypesInSignature());
            System.out.println("individuals in signature: " + owlOntology.getIndividualsInSignature());
            System.out.println(owlOntology.getDirectImports());
            System.out.println(owlOntology.getObjectPropertiesInSignature());
            System.out.println(owlOntology.getNestedClassExpressions());
            System.out.println(owlOntology.getImportsDeclarations());
            System.out.println(owlOntology.getDataPropertiesInSignature());
        }

        // Save the inferred ontology.
        outputOntologyManager.saveOntology(infOnt,
                new FunctionalSyntaxDocumentFormat(),
                IRI.create((new File("output.owl").toURI())));

//         Terminate the worker threads used by the reasoner.
        reasoner.dispose();
    }
}
