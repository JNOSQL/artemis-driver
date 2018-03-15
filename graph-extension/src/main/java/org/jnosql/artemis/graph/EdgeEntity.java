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

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.jnosql.diana.api.Value;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The representation of {@link org.apache.tinkerpop.gremlin.structure.Edge} that links two Entity.
 * Along with its Property objects, an Edge has both a Direction and a label.
 * Any Change at the Edge is automatically continued in the database. However, any, change in the Entity will be ignored.
 * {@link GraphTemplate#update(Object)}
 * 
 * <pre>outVertex ---label---&#62; inVertex.</pre>
 * 
 */
public interface EdgeEntity {

    /**
     * Returns the id
     *
     * @return the id
     */
    Value getId();

    /**
     * Returns the label of the vertex
     *
     * @return the label
     */
    String getLabel();

    /**
     * Gets the inbound entity
     *
     * @param <T> the type
     * @return the inbound entity
     */
    <T> T getInbound();

    /**
     * Gets the outbound entity
     *
     * @param <T> the type
     * @return the outbound entity
     */
    <T> T getOutbound();

    /**
     * Returns the properties of this vertex
     *
     * @return the properties
     */
    List<Property> getProperties();


    /**
     * Add a new element in the Vertex
     *
     * @param key   the key
     * @param value the information
     * @throws NullPointerException when either key or value are null
     */
    void add(String key, Object value);

    /**
     * Add a new element in the Vertex
     *
     * @param key   the key
     * @param value the information
     * @throws NullPointerException when either key or value are null
     */
    void add(String key, Value value);

    /**
     * Removes an property
     *
     * @param key the key
     * @throws NullPointerException whe key is null
     */
    void remove(String key);

    /**
     * Returns the property from the key
     *
     * @param key the key to find the property
     * @return the property to the respective key otherwise {@link Optional#empty()}
     * @throws NullPointerException when key is null
     */
    Optional<Value> get(String key);

    /**
     * Returns true if this Edge contains no elements.
     *
     * @return true if this collection contains no elements
     */
    boolean isEmpty();

    /**
     * Returns the number of property in Edge
     *
     * @return the number of elements in this Edge
     */
    int size();

    /**
     * Deletes the Edge from the database, after this operation, any write operation
     * such as add a property will an illegal state.
     */
    void delete();


    /**
     * Creates a new {@link EdgeEntity} instance
     *
     * @param outgoing the outgoing
     * @param edge     the Tinkerpop edge representation
     * @param incoming the incoming object
     * @param <OUT>    the outgoing type
     * @param <IN>     the incoming type
     * @return an {@link EdgeEntity} instance
     * @throws NullPointerException if any parameters is null
     */
    static <OUT, IN> EdgeEntity of(OUT outgoing, Edge edge, IN incoming) {
        requireNonNull(outgoing, "outgoing is required");
        requireNonNull(edge, "edge is required");
        requireNonNull(incoming, "incoming is required");
        return new DefaultEdgeEntity<>(edge, incoming, outgoing);
    }
}
