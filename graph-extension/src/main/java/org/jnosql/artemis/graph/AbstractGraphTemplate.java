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

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.apache.tinkerpop.gremlin.structure.T.id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jnosql.artemis.EntityNotFoundException;
import org.jnosql.artemis.IdNotFoundException;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.reflection.FieldRepresentation;
import org.jnosql.artemis.reflection.Reflections;

public abstract class AbstractGraphTemplate implements GraphTemplate {
    private static final Function<GraphTraversal<?, ?>, GraphTraversal<Vertex, Vertex>> INITIAL_VERTEX =
            g -> (GraphTraversal<Vertex, Vertex>) g;

    private static final Function<GraphTraversal<?, ?>, GraphTraversal<Vertex, Edge>> INITIAL_EDGE =
            g -> (GraphTraversal<Vertex, Edge>) g;


    protected abstract GraphTraversalSource getGraphTraversal();

    protected abstract ClassRepresentations getClassRepresentations();

    protected abstract GraphConverter getConverter();

    protected abstract GraphWorkflow getFlow();

    protected abstract Reflections getReflections();

    @Override
    public <T> T insert(T entity) {
        requireNonNull(entity, "entity is required");
        checkId(entity);
        UnaryOperator<Vertex> save = v -> v;

        return getFlow().flow(entity, save);
    }

    @Override
    public <T> T update(T entity) {
        requireNonNull(entity, "entity is required");
        checkId(entity);
        if (isIdNull(entity)) {
            throw new NullPointerException("to update a graph id cannot be null");
        }
        getVertex(entity).orElseThrow(() -> new EntityNotFoundException("Entity does not find in the update"));

        UnaryOperator<Vertex> update = e -> getConverter().toVertex(entity);
        return getFlow().flow(entity, update);
    }

    @Override
    public <T> void delete(T idValue) {
        requireNonNull(idValue, "id is required");
        List<Vertex> vertices = getGraphTraversal().V(idValue).toList();
        vertices.forEach(Vertex::remove);

    }

    @Override
    public <T> void deleteEdge(T idEdge) {
        requireNonNull(idEdge, "idEdge is required");
        List<Edge> edges = getGraphTraversal().E(idEdge).toList();
        edges.forEach(Edge::remove);
    }

    @Override
    public <T, ID> Optional<T> find(ID idValue) {
        requireNonNull(idValue, "id is required");
        Optional<Vertex> vertex = getGraphTraversal().V(idValue).tryNext();
        return vertex.map(getConverter()::toEntity);
    }

    @Override
    public <OUT, IN> EdgeEntity edge(OUT outbound, String label, IN incoming) {

        requireNonNull(incoming, "inbound is required");
        requireNonNull(label, "label is required");
        requireNonNull(outbound, "outbound is required");

        checkId(outbound);
        checkId(incoming);

        if (isIdNull(outbound)) {
            throw new NullPointerException("outbound Id field is required");
        }

        if (isIdNull(incoming)) {
            throw new NullPointerException("inbound Id field is required");
        }


        Vertex outVertex = getVertex(outbound).orElseThrow(() -> new EntityNotFoundException("Outbound entity does not found"));
        Vertex inVertex = getVertex(incoming).orElseThrow(() -> new EntityNotFoundException("Incoming entity does not found"));

        final Predicate<Traverser<Edge>> predicate = t -> {
            Edge e = t.get();
            return e.inVertex().id().equals(inVertex.id())
                    && e.outVertex().id().equals(outVertex.id());
        };

        Optional<Edge> edge = getGraphTraversal()
                .V(outVertex.id())
                .out(label).has(id, inVertex.id()).inE(label).filter(predicate).tryNext();

        return edge.<EdgeEntity>map(edge1 -> new DefaultEdgeEntity<>(edge1, incoming, outbound))
                .orElseGet(() -> new DefaultEdgeEntity<>(outVertex.addEdge(label, inVertex), incoming, outbound));


    }

    @Override
    public <E> Optional<EdgeEntity> edge(E edgeId) {
        requireNonNull(edgeId, "edgeId is required");

        Optional<Edge> edgeOptional = getGraphTraversal().E(edgeId).tryNext();

        if (edgeOptional.isPresent()) {
            Edge edge = edgeOptional.get();
            return Optional.of(getConverter().toEdgeEntity(edge));
        }

        return Optional.empty();
    }


    @Override
    public <T> Collection<EdgeEntity> getEdges(T entity, Direction direction) {
        return getEdgesImpl(entity, direction);
    }

    @Override
    public <T> Collection<EdgeEntity> getEdges(T entity, Direction direction, String... labels) {
        return getEdgesImpl(entity, direction, labels);
    }


    @SafeVarargs
    @Override
    public final <T> Collection<EdgeEntity> getEdges(T entity, Direction direction, Supplier<String>... labels) {
        checkLabelsSupplier(labels);
        return getEdgesImpl(entity, direction, Stream.of(labels).map(Supplier::get).toArray(String[]::new));
    }


    @Override
    public <ID> Collection<EdgeEntity> getEdgesById(ID id, Direction direction, String... labels) {
        return getEdgesByIdImpl(id, direction, labels);
    }

    @Override
    public <ID> Collection<EdgeEntity> getEdgesById(ID id, Direction direction) {
        return getEdgesByIdImpl(id, direction);
    }

    @SafeVarargs
    @Override
    public final <ID> Collection<EdgeEntity> getEdgesById(ID id, Direction direction, Supplier<String>... labels) {
        checkLabelsSupplier(labels);
        return getEdgesByIdImpl(id, direction, Stream.of(labels).map(Supplier::get).toArray(String[]::new));
    }


    @Override
    public VertexTraversal getTraversalVertex(Object... vertexIds) {
        if (Stream.of(vertexIds).anyMatch(Objects::isNull)) {
            throw new NullPointerException("No one vertexId element cannot be null");
        }
        return new DefaultVertexTraversal(() -> getGraphTraversal().V(vertexIds), INITIAL_VERTEX, getConverter());
    }

    @Override
    public EdgeTraversal getTraversalEdge(Object... edgeIds) {
        if (Stream.of(edgeIds).anyMatch(Objects::isNull)) {
            throw new NullPointerException("No one edgeId element cannot be null");
        }
        return new DefaultEdgeTraversal(() -> getGraphTraversal().E(edgeIds), INITIAL_EDGE, getConverter());
    }

    private <ID> Collection<EdgeEntity> getEdgesByIdImpl(ID id, Direction direction, String... labels) {

        requireNonNull(id, "id is required");
        requireNonNull(direction, "direction is required");

        //Iterator<Vertex> vertices = getGraph().vertices(id);
        final Iterator<Vertex> vertices = getGraphTraversal().V(id);
        if (vertices.hasNext()) {
            List<Edge> edges = new ArrayList<>();
            vertices.next().edges(direction, labels).forEachRemaining(edges::add);
            return edges.stream().map(getConverter()::toEdgeEntity).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private <T> Collection<EdgeEntity> getEdgesImpl(T entity, Direction direction, String... labels) {
        requireNonNull(entity, "entity is required");

        if (isIdNull(entity)) {
            throw new NullPointerException("Entity id is required");
        }

        if (!getVertex(entity).isPresent()) {
            return Collections.emptyList();
        }
        Object id = getConverter().toVertex(entity).id();
        return getEdgesByIdImpl(id, direction, labels);
    }

    private void checkLabelsSupplier(Supplier<String>[] labels) {
        if (Stream.of(labels).anyMatch(Objects::isNull)) {
            throw new NullPointerException("Item cannot be null");
        }
    }

    private <T> boolean isIdNull(T entity) {
        ClassRepresentation classRepresentation = getClassRepresentations().get(entity.getClass());
        FieldRepresentation field = classRepresentation.getId().get();
        return isNull(getReflections().getValue(entity, field.getNativeField()));

    }

    private <T> Optional<Vertex> getVertex(T entity) {
        ClassRepresentation classRepresentation = getClassRepresentations().get(entity.getClass());
        FieldRepresentation field = classRepresentation.getId().get();
        Object id = getReflections().getValue(entity, field.getNativeField());
        //Iterator<Vertex> vertices = getGraph().vertices(id);
        final Iterator<Vertex> vertices = getGraphTraversal().V(id);
        if (vertices.hasNext()) {
            return Optional.of(vertices.next());
        }
        return Optional.empty();
    }

    private <T> void checkId(T entity) {
        ClassRepresentation classRepresentation = getClassRepresentations().get(entity.getClass());
        classRepresentation.getId().orElseThrow(() -> IdNotFoundException.newInstance(entity.getClass()));
    }
}
