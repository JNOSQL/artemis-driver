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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

class DefaultEdgeEntity<OUT, IN> implements EdgeEntity {

    private final OUT outgoing;

    private final Edge edge;

    private final IN incoming;

    DefaultEdgeEntity(Edge edge, IN incoming, OUT outgoing) {
        this.edge = edge;
        this.incoming = incoming;
        this.outgoing = outgoing;
    }

    @Override
    public Value getId() {
        return Value.of(edge.id());
    }

    @Override
    public String getLabel() {
        return edge.label();
    }

    @Override
    public IN getIncoming() {
        return incoming;
    }

    @Override
    public OUT getOutgoing() {
        return outgoing;
    }

    @Override
    public List<Property> getProperties() {
        return edge.keys()
                .stream()
                .map(k -> DefaultProperty.of(k, edge.value(k)))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Override
    public void add(String key, Object value) {
        throw new UnsupportedOperationException("add operation is not supported!");
        
//        requireNonNull(key, "key is required");
//        requireNonNull(value, "value is required");
//        edge.property(key, value);

    }

    @Override
    public void add(String key, Value value) {
        throw new UnsupportedOperationException("add operation is not supported!");
        
//        requireNonNull(key, "key is required");
//        requireNonNull(value, "value is required");
//        edge.property(key, value.get());
    }

    @Override
    public void remove(String key) {
        throw new UnsupportedOperationException("remove operation is not supported!");

//        requireNonNull(key, "key is required");
//        org.apache.tinkerpop.gremlin.structure.Property property = edge.property(key);
//        property.ifPresent(o -> property.remove());
    }

    @Override
    public Optional<Value> get(String key) {
        requireNonNull(key, "key is required");
        org.apache.tinkerpop.gremlin.structure.Property property = edge.property(key);
        if (property.isPresent()) {
            return Optional.of(Value.of(property.value()));
        }
        return Optional.empty();
    }

    @Override
    public boolean isEmpty() {
        return edge.keys().isEmpty();
    }

    @Override
    public int size() {
        return edge.keys().size();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("remove operation is not supported!");
        //edge.remove();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultEdgeEntity)) {
            return false;
        }
        DefaultEdgeEntity<?, ?> that = (DefaultEdgeEntity<?, ?>) o;
        return Objects.equals(edge, that.edge) &&
                Objects.equals(incoming, that.incoming) &&
                Objects.equals(outgoing, that.outgoing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, incoming, outgoing);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(outgoing);
        sb.append("---").append(edge.label());
        sb.append(" --->").append(incoming);
        return sb.toString();
    }

}
