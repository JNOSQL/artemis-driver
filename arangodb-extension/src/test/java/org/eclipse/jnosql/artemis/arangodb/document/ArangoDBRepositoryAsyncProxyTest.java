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
package org.eclipse.jnosql.artemis.arangodb.document;

import jakarta.nosql.mapping.document.DocumentRepositoryAsyncProducer;
import org.eclipse.jnosql.artemis.test.CDIExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@CDIExtension
public class ArangoDBRepositoryAsyncProxyTest {


    private ArangoDBTemplateAsync template;

    @Inject
    private DocumentRepositoryAsyncProducer producer;

    private PersonAsyncRepository personRepository;


    @BeforeEach
    public void setUp() {
        this.template = Mockito.mock(ArangoDBTemplateAsync.class);
        PersonAsyncRepository personAsyncRepository = producer.get(PersonAsyncRepository.class, template);
        ArangoDBRepositoryAsyncProxy handler = new ArangoDBRepositoryAsyncProxy(template, personAsyncRepository);


        personRepository = (PersonAsyncRepository) Proxy.newProxyInstance(PersonAsyncRepository.class.getClassLoader(),
                new Class[]{PersonAsyncRepository.class},
                handler);
    }


    @Test
    public void shouldUpdate() {
        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = new Person("Ada", 12);
        template.update(person);
        verify(template).update(captor.capture());
        Person value = captor.getValue();
        assertEquals(person, value);
    }


    @Test
    public void shouldFindNoCallback() {
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        Map params = Collections.singletonMap("name", "Ada");
        personRepository.queryName("Ada");
        verify(template).aql(Mockito.eq("FOR p IN Person RETURN p"), captor.capture(),
                any(Consumer.class));

        Map value = captor.getValue();
        assertEquals("Ada", value.get("name"));
    }

    @Test
    public void shouldFindByNameFromAQL() {
        Consumer<Stream<Person>> callBack = p -> {
        };

        Map params = Collections.singletonMap("name", "Ada");
        personRepository.queryName("Ada", callBack);

        verify(template).aql(Mockito.eq("FOR p IN Person FILTER p.name = @name RETURN p"), Mockito.eq(params),
                Mockito.eq(callBack));

    }

    interface PersonAsyncRepository extends ArangoDBRepositoryAsync<Person, String> {

        Person findByName(String name);


        @AQL("FOR p IN Person RETURN p")
        void queryName(@Param("name") String name);

        @AQL("FOR p IN Person FILTER p.name = @name RETURN p")
        void queryName(@Param("name") String name, Consumer<Stream<Person>> callBack);
    }
}