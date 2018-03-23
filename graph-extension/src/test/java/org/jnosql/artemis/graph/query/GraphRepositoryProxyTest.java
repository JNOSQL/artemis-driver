/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.graph.query;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jnosql.artemis.Repository;
import org.jnosql.artemis.graph.GraphConverter;
import org.jnosql.artemis.graph.GraphTemplate;
import org.jnosql.artemis.graph.cdi.CDIExtension;
import org.jnosql.artemis.graph.model.Person;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.reflection.Reflections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@ExtendWith(CDIExtension.class)
public class GraphRepositoryProxyTest {


    private GraphTemplate template;

    @Inject
    private ClassRepresentations classRepresentations;

    @Inject
    private Reflections reflections;

    private PersonRepository personRepository;

    @Inject
    private GraphTraversalSource graph;

    @Inject
    private GraphConverter converter;

    @BeforeEach
    public void setUp() {

        graph.V().toList().forEach(Vertex::remove);
        graph.E().toList().forEach(Edge::remove);


        this.template = Mockito.mock(GraphTemplate.class);

        GraphRepositoryProxy handler = new GraphRepositoryProxy(template,
                classRepresentations, PersonRepository.class, reflections, converter);

        when(template.insert(any(Person.class))).thenReturn(Person.builder().build());
        when(template.update(any(Person.class))).thenReturn(Person.builder().build());
        personRepository = (PersonRepository) Proxy.newProxyInstance(PersonRepository.class.getClassLoader(),
                new Class[]{PersonRepository.class},
                handler);
    }

    @AfterEach
    public void after() {
        graph.V().toList().forEach(Vertex::remove);
        graph.E().toList().forEach(Edge::remove);

    }


    @Test
    public void shouldSaveUsingInsertWhenDataDoesNotExist() {
        when(template.find(Mockito.any(Long.class))).thenReturn(Optional.empty());

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().withName("Ada")
                .withId(10L)
                .withPhones(singletonList("123123"))
                .build();
        assertNotNull(personRepository.save(person));
        verify(template).insert(captor.capture());
        Person value = captor.getValue();
        assertEquals(person, value);
    }

    @Test
    public void shouldSaveUsingUpdateWhenDataExists() {
        when(template.find(Mockito.any(Long.class))).thenReturn(Optional.of(Person.builder().build()));

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().withName("Ada")
                .withId(10L)
                .withPhones(singletonList("123123"))
                .build();
        assertNotNull(personRepository.save(person));
        verify(template).update(captor.capture());
        Person value = captor.getValue();
        assertEquals(person, value);
    }


    @Test
    public void shouldSaveIterable() {
        when(personRepository.findById(10L)).thenReturn(Optional.empty());

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().withName("Ada")
                .withId(10L)
                .withPhones(singletonList("123123"))
                .build();

        personRepository.save(singletonList(person));
        verify(template).insert(captor.capture());
        Person personCapture = captor.getValue();
        assertEquals(person, personCapture);
    }


    @Test
    public void shouldFindByNameInstance() {

        graph.addV("Person").property("name", "name").property("age", 20).next();    
        //graph.addVertex(T.label, "Person", "name", "name", "age", 20);

        Person person = personRepository.findByName("name");
        assertNotNull(person);
        assertNull(personRepository.findByName("name2"));

    }

    @Test
    public void shouldFindByNameAndAge() {

        graph.addV("Person").property("name", "name").property("age", 20).next();    
        graph.addV("Person").property("name", "name").property("age", 20).next();    
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);

        List<Person> people = personRepository.findByNameAndAge("name", 20);
        assertEquals(2, people.size());

    }

    @Test
    public void shouldFindByAgeAndName() {

        graph.addV("Person").property("name", "name").property("age", 20).next();    
        graph.addV("Person").property("name", "name").property("age", 20).next();    
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);

        Set<Person> people = personRepository.findByAgeAndName(20, "name");
        assertEquals(2, people.size());

    }

    @Test
    public void shouldFindByAge() {

        graph.addV("Person").property("name", "name").property("age", 20).next();    
        graph.addV("Person").property("name", "name").property("age", 20).next();    
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);

        Optional<Person> person = personRepository.findByAge(20);
        assertTrue(person.isPresent());

    }

    @Test
    public void shouldDeleteByName() {
        Vertex vertex = graph.addV("Person").property("name", "Ada").property("age", 20).next();    
        //Vertex vertex = graph.addVertex(T.label, "Person", "name", "Ada", "age", 20);

        personRepository.deleteByName("Ada");
        assertFalse(graph.V(vertex.id()).tryNext().isPresent());

    }

    @Test
    public void shouldFindByNameAndAgeGreaterThanEqual() {

        graph.addV("Person").property("name", "name").property("age", 20).next();    
        graph.addV("Person").property("name", "name").property("age", 20).next();    
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);

        Set<Person> people = personRepository.findByNameAndAgeGreaterThanEqual("name", 20);
        assertEquals(2, people.size());

    }

    @Test
    public void shouldFindById() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        personRepository.findById(10L);
        verify(template).find(captor.capture());

        Object id = captor.getValue();

        assertEquals(10L, id);
    }

    @Test
    public void shouldFindByIds() {

        when(template.find(any(Object.class))).thenReturn(Optional.empty());
        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);
        personRepository.findById(singletonList(10L));
        verify(template).find(captor.capture());

        personRepository.findById(asList(1L, 2L, 3L));
        verify(template, times(4)).find(any(Long.class));
    }

    @Test
    public void shouldDeleteById() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        personRepository.deleteById(10L);
        verify(template).delete(captor.capture());

        assertEquals(captor.getValue(), 10L);
    }

    @Test
    public void shouldDeleteByIds() {
        personRepository.deleteById(singletonList(10L));
        verify(template).delete(10L);

        personRepository.deleteById(asList(1L, 2L, 3L));
        verify(template, times(4)).delete(any(Long.class));
    }


    @Test
    public void shouldContainsById() {
        when(template.find(any(Long.class))).thenReturn(Optional.of(Person.builder().build()));

        assertTrue(personRepository.existsById(10L));
        Mockito.verify(template).find(any(Long.class));

        when(template.find(any(Long.class))).thenReturn(Optional.empty());
        assertFalse(personRepository.existsById(10L));

    }

    @Test
    public void shouldFindAll() {
        graph.addV("Person").property("name", "name").property("age", 20).next();    
        graph.addV("Person").property("name", "name").property("age", 20).next();    
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
//        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        List<Person> people = personRepository.findAll();
        assertFalse(people.isEmpty());
    }

    @Test
    public void shouldReturnToString() {
        assertNotNull(personRepository.toString());
    }

    @Test
    public void shouldReturnHasCode() {
        assertNotNull(personRepository.hashCode());
        assertEquals(personRepository.hashCode(), personRepository.hashCode());
    }



    interface PersonRepository extends Repository<Person, Long> {

        Person findByName(String name);

        void deleteByName(String name);

        List<Person> findAll();

        Optional<Person> findByAge(Integer age);

        List<Person> findByNameAndAge(String name, Integer age);

        Set<Person> findByAgeAndName(Integer age, String name);

        Set<Person> findByNameAndAgeGreaterThanEqual(String name, Integer age);


    }
}
