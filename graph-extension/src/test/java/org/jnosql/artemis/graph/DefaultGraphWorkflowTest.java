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
package org.jnosql.artemis.graph;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jnosql.artemis.graph.cdi.MockitoExtension;
import org.jnosql.artemis.graph.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
public class DefaultGraphWorkflowTest {


    @InjectMocks
    private DefaultGraphWorkflow subject;

    @Mock
    private GraphEventPersistManager graphEventPersistManager;

    @Mock
    private GraphConverter converter;

    @Mock
    private Vertex vertex;


    @BeforeEach
    public void setUp() {
        when(converter.toVertex(any(Object.class)))
                .thenReturn(vertex);
        when(converter.toEntity(Mockito.eq(Person.class), any(Vertex.class)))
                .thenReturn(Person.builder().build());
        when(converter.toEntity(Mockito.any(Person.class), any(Vertex.class)))
                .thenReturn(Person.builder().build());

    }

    @Test
    public void shouldReturnErrorWhenEntityIsNull() {
        assertThrows(NullPointerException.class, () -> {
            UnaryOperator<Vertex> action = t -> t;
            subject.flow(null, Optional.empty(), action);
        });
    }

    @Test
    public void shouldReturnErrorWhenActionIsNull() {
        assertThrows(NullPointerException.class, () -> subject.flow("", Optional.empty(), null));
    }

    @Test
    public void shouldFollowWorkflow() {
        UnaryOperator<Vertex> action = t -> t;
        subject.flow(Person.builder().withId(1L).withAge().withName("Ada").build(), Optional.empty(), action);

        verify(graphEventPersistManager).firePreGraph(any(Vertex.class));
        verify(graphEventPersistManager).firePostGraph(any(Vertex.class));
        verify(graphEventPersistManager).firePreEntity(any(Person.class));
        verify(graphEventPersistManager).firePostEntity(any(Person.class));

        verify(graphEventPersistManager).firePreGraphEntity(any(Person.class));
        verify(graphEventPersistManager).firePostGraphEntity(any(Person.class));
        verify(converter).toVertex(any(Object.class));
    }

}