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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The Graph Traversal that maps {@link org.apache.tinkerpop.gremlin.structure.Edge}.
 * This Traversal is lazy, in other words, that just run after the
 */
public interface EdgeTraversal extends EdgeConditionTraversal {


    /**
     * Does a filter predicate based
     * @param predicate a predicate to apply to each element to determine if it should be included
     * @return a {@link EdgeTraversal} with the Vertex predicate
     * @throws NullPointerException when predicate is null
     */
     EdgeTraversal filter(Predicate<EdgeEntity> predicate);

    /**
     * Filter the objects in the traversal by the number of them to pass through the next, where only the first
     * {@code n} objects are allowed as defined by the {@code limit} argument.
     *
     * @param limit the number at which to end the next
     * @return a {@link EdgeTraversal} with the limit
     */
    EdgeTraversal limit(long limit);

    /**
     * Returns an EdgeTraversal with range defined
     *
     * @param start the start inclusive
     * @param end   the end exclusive
     * @return a {@link EdgeTraversal} with the range set
     */
    EdgeTraversal range(long start, long end);


    /**
     * Starts the loop traversal graph
     *
     * @return a {@link EdgeRepeatTraversal}
     */
    EdgeRepeatTraversal repeat();


    /**
     * Returns the next elements in the traversal.
     * If the traversal is empty, then an {@link Optional#empty()} is returned.
     *
     * @return the EdgeEntity result otherwise {@link Optional#empty()}
     */
    Optional<EdgeEntity> next();

    /**
     * Concludes the traversal that returns a single {@link EdgeEntity} result
     * @return the EdgeEntity result otherwise {@link Optional#empty()}
     * @throws org.jnosql.diana.api.NonUniqueResultException when there is more than one result
     */
    Optional<EdgeEntity> getSingleResult();

    /**
     * Concludes the traversal then returns the result as list.
     * @return the entities result
     */
    List<EdgeEntity> getResultList();


    /**
     * Converts to vertex traversal taking the incoming Vertex
     *
     * @return {@link VertexTraversal}
     */
    VertexTraversal inV();

    /**
     * Converts to vertex traversal taking the outgoing Vertex
     *
     * @return {@link VertexTraversal}
     */
    VertexTraversal outV();

    /**
     * Converts to vertex traversal taking both incoming and outgoing Vertex
     *
     * @return {@link VertexTraversal}
     */
    VertexTraversal bothV();

    /**
     * Get all the result in the traversal as Stream
     *
     * @return the entity result as {@link Stream}
     */
     Stream<EdgeEntity> stream();

    /**
     * Get the next n elements result as next, the number of elements is limit based
     *
     * @param limit the limit to result
     * @return the entity result as {@link Stream}
     */
     Stream<EdgeEntity> next(int limit);

    /**
     * Map the {@link org.apache.tinkerpop.gremlin.structure.Element} to a {@link java.util.Map} of the properties key'd according
     * to their {@link org.apache.tinkerpop.gremlin.structure.Property#key}.
     * If no property keys are provided, then all properties are retrieved.
     *
     * @param propertyKeys the properties to retrieve
     * @return a {@link ValueMapTraversal} instance
     */
    ValueMapTraversal valueMap(final String... propertyKeys);

    /**
     * Defines the order of the Edge, the property must have in all elements.
     * Otherwise, it'll return an exception. As recommendation use
     * {@link EdgeTraversal#has(String)} before this method
     *
     * @param property the property to be order
     * @return the {@link EdgeTraversalOrder} to define the order way
     * @throws NullPointerException  when the property is null
     * @throws IllegalStateException when there any Edge that does not have the property
     */
    EdgeTraversalOrder orderBy(String property);

    /**
     * Map the traversal next to its reduction as a sum of the elements
     *
     * @return the sum
     */
    long count();
}
