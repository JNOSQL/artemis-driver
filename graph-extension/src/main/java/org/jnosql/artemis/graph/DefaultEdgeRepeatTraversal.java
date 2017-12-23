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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

class DefaultEdgeRepeatTraversal extends AbstractEdgeTraversal implements EdgeRepeatTraversal {


    DefaultEdgeRepeatTraversal(Supplier<GraphTraversal<?, ?>> supplier,
                               Function<GraphTraversal<?, ?>, GraphTraversal<Vertex, Edge>> flow,
                               VertexConverter converter) {
        super(supplier, flow, converter);
    }

    @Override
    public EdgeRepeatStepTraversal has(String propertyKey) throws NullPointerException {
        requireNonNull(propertyKey, "propertyKey is required");
        return new DefaultEdgeRepeatStepTraversal(supplier, flow.andThen(g -> g.has(propertyKey)), converter);
    }

    @Override
    public EdgeRepeatStepTraversal has(String propertyKey, Object value) throws NullPointerException {
        requireNonNull(propertyKey, "propertyKey is required");
        requireNonNull(value, "value is required");
        return new DefaultEdgeRepeatStepTraversal(supplier, flow.andThen(g -> g.has(propertyKey, value)), converter);
    }

    @Override
    public EdgeRepeatStepTraversal has(String propertyKey, P<?> predicate) throws NullPointerException {
        requireNonNull(propertyKey, "propertyKey is required");
        requireNonNull(predicate, "predicate is required");
        return new DefaultEdgeRepeatStepTraversal(supplier, flow.andThen(g -> g.has(propertyKey, predicate)), converter);
    }

    @Override
    public EdgeRepeatStepTraversal has(T accessor, Object value) throws NullPointerException {
        requireNonNull(accessor, "accessor is required");
        requireNonNull(value, "value is required");
        return new DefaultEdgeRepeatStepTraversal(supplier, flow.andThen(g -> g.has(accessor, value)), converter);
    }

    @Override
    public EdgeRepeatStepTraversal has(T accessor, P<?> predicate) throws NullPointerException {
        requireNonNull(accessor, "accessor is required");
        requireNonNull(predicate, "predicate is required");
        return new DefaultEdgeRepeatStepTraversal(supplier, flow.andThen(g -> g.has(accessor, predicate)), converter);
    }

    @Override
    public EdgeRepeatStepTraversal hasNot(String propertyKey) throws NullPointerException {
        requireNonNull(propertyKey, "propertyKey is required");
        return new DefaultEdgeRepeatStepTraversal(supplier, flow.andThen(g -> g.hasNot(propertyKey)), converter);
    }
}

