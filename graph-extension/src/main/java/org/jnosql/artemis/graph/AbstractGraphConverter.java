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

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jnosql.artemis.reflection.FieldType.EMBEDDED;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jnosql.artemis.AttributeConverter;
import org.jnosql.artemis.Converters;
import org.jnosql.artemis.EntityNotFoundException;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.reflection.FieldRepresentation;
import org.jnosql.artemis.reflection.Reflections;
import org.jnosql.diana.api.Value;

abstract class AbstractGraphConverter implements GraphConverter {


    protected abstract ClassRepresentations getClassRepresentations();

    protected abstract Reflections getReflections();

    protected abstract Converters getConverters();

    protected abstract GraphTraversalSource getTraversalSource();

    /**
     * add a new vertex from entity
     * 
     * @param entity entity
     * @param <T> entity type
     * @return return a new aeed vertex
     */
    @Override
    public <T> Vertex toNewVertex(T entity) {
        requireNonNull(entity, "entity is required");
        
        final ClassRepresentation representation = getClassRepresentations().get(entity.getClass());
        final String label = representation.getName();
        
        final List<FieldRepresentation> fieldsR = representation.getFields();
        
        final Optional<FieldGraph> id = fieldsR.stream()
                .map(f -> to(f, entity))
                .filter(FieldGraph::isNotEmpty)
                .filter(FieldGraph::isId)
                .findFirst();
        
        final GraphTraversal<Vertex,Vertex> t = 
                id.map( i -> i.toElement(getConverters()) )
                .map( i -> getTraversalSource().addV(label).property( org.apache.tinkerpop.gremlin.structure.T.id, i.value() ))
                .orElseGet(() -> getTraversalSource().addV(label) );     
          
        fieldsR.stream()
        .map(f -> to(f, entity))
        .filter(FieldGraph::isNotEmpty)
        .filter(FieldGraph::isNotId)
        .flatMap(f -> f.toElements(this, getConverters()).stream())
        .forEach(p -> t.property(p.key(), p.value()));

        return t.next();
      
    }
    
    /**
     * update vertex property from entity
     * 
     * @param entity entity
     * @param <T> entity type
     * @param vertex vertex
     * @return updated vertex
     */
    @Override
    public <T> Vertex toVertex(T entity, Vertex vertex) {
        requireNonNull(entity, "entity is required");
        requireNonNull(vertex, "vertex is required");
        
        final ClassRepresentation representation = getClassRepresentations().get(entity.getClass());        

        final GraphTraversal<Vertex,Vertex> t = getTraversalSource().V( vertex.id() );
        
        representation.getFields().stream()
        .map(f -> to(f, entity))
        .filter(FieldGraph::isNotEmpty)
        .filter(FieldGraph::isNotId)
        .flatMap(f -> f.toElements(this, getConverters()).stream())
        .forEach(p -> t.property(p.key(), p.value()));

        return t.next();

        
    }

    /**
     * 
     */
    @Override
    public <T> Vertex toVertex(T entity) {
        requireNonNull(entity, "entity is required");

        ClassRepresentation representation = getClassRepresentations().get(entity.getClass());
        String label = representation.getName();

        List<FieldGraph> fields = representation.getFields().stream()
                .map(f -> to(f, entity))
                .filter(FieldGraph::isNotEmpty).collect(toList());

        Optional<FieldGraph> id = fields.stream().filter(FieldGraph::isId).findFirst();
        
        final Function<Property, GraphTraversal<Vertex,Vertex>> findVertexOrCreateWithId = p -> {
            final GraphTraversal<Vertex,Vertex> vertices = getTraversalSource().V(p.value());
            return vertices.hasNext() ? 
                    vertices : 
                    getTraversalSource().addV(label).property( org.apache.tinkerpop.gremlin.structure.T.id, p.value() ) ;
        };

        final GraphTraversal<Vertex, Vertex> vertex = 
                id.map(i -> i.toElement(getConverters()))
                .map(findVertexOrCreateWithId)
                .orElseGet(() -> getTraversalSource().addV(label) );
        
        fields.stream().filter(FieldGraph::isNotId)
                .flatMap(f -> f.toElements(this, getConverters()).stream())
                .forEach(p -> vertex.property(p.key(), p.value()));

        return vertex.next();
    }

    @Override
    public <T> T toEntity(Vertex vertex) {
        requireNonNull(vertex, "vertex is required");
        ClassRepresentation representation = getClassRepresentations().findByName(vertex.label());

        List<Property> properties = vertex.keys().stream().map(k -> DefaultProperty.of(k, vertex.value(k))).collect(toList());
        T entity = toEntity((Class<T>) representation.getClassInstance(), properties);
        feedId(vertex, entity);
        return entity;
    }

    @Override
    public <T> T toEntity(Class<T> entityClass, Vertex vertex) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(vertex, "vertex is required");

        List<Property> properties = vertex.keys().stream().map(k -> DefaultProperty.of(k, vertex.value(k))).collect(toList());
        T entity = toEntity(entityClass, properties);
        feedId(vertex, entity);
        return entity;
    }

    @Override
    public <T> T toEntity(T entityInstance, Vertex vertex) {
        requireNonNull(entityInstance, "entityInstance is required");
        requireNonNull(vertex, "vertex is required");

        List<Property> properties = vertex.keys().stream().map(k -> DefaultProperty.of(k, vertex.value(k))).collect(toList());

        ClassRepresentation representation = getClassRepresentations().get(entityInstance.getClass());
        convertEntity(properties, representation, entityInstance);
        feedId(vertex, entityInstance);
        return entityInstance;

    }

    @Override
    public EdgeEntity toEdgeEntity(Edge edge) {
        requireNonNull(edge, "vertex is required");
        Object out = toEntity(edge.outVertex());
        Object in = toEntity(edge.inVertex());
        return EdgeEntity.of(out, edge, in);
    }

    @Override
    public Edge toEdge(EdgeEntity edge) {
        requireNonNull(edge, "vertex is required");
        Object id = edge.getId().get();
        //Iterator<Edge> edges = getGraph().edges(id);
        final Iterator<Edge> edges = getTraversalSource().E(id);
        if (edges.hasNext()) {
            return edges.next();
        }
        throw new EntityNotFoundException("Edge does not found in the database with id: " + id);
    }

    private <T> void feedId(Vertex vertex, T entity) {
        ClassRepresentation representation = getClassRepresentations().get(entity.getClass());
        Optional<FieldRepresentation> id = representation.getId();


        Object vertexId = vertex.id();
        if (Objects.nonNull(vertexId) && id.isPresent()) {
            FieldRepresentation fieldRepresentation = id.get();
            Field fieldId = fieldRepresentation.getNativeField();
            if (fieldRepresentation.getConverter().isPresent()) {
                AttributeConverter attributeConverter = getConverters().get(fieldRepresentation.getConverter().get());
                Object attributeConverted = attributeConverter.convertToEntityAttribute(vertexId);
                getReflections().setValue(entity, fieldId, fieldRepresentation.getValue(Value.of(attributeConverted)));
            } else {
                getReflections().setValue(entity, fieldId, fieldRepresentation.getValue(Value.of(vertexId)));
            }

        }
    }

    private <T> T toEntity(Class<T> entityClass, List<Property> properties) {
        ClassRepresentation representation = getClassRepresentations().get(entityClass);
        T instance = getReflections().newInstance(representation.getConstructor());
        return convertEntity(properties, representation, instance);
    }

    private <T> T convertEntity(List<Property> elements, ClassRepresentation representation, T instance) {

        Map<String, FieldRepresentation> fieldsGroupByName = representation.getFieldsGroupByName();
        List<String> names = elements.stream()
                .map(Property::key)
                .sorted()
                .collect(toList());
        Predicate<String> existField = k -> Collections.binarySearch(names, k) >= 0;

        fieldsGroupByName.keySet().stream()
                .filter(existField.or(k -> EMBEDDED.equals(fieldsGroupByName.get(k).getType())))
                .forEach(feedObject(instance, elements, fieldsGroupByName));

        return instance;
    }

    private <T> Consumer<String> feedObject(T instance, List<Property> elements,
                                            Map<String, FieldRepresentation> fieldsGroupByName) {
        return k -> {
            Optional<Property> element = elements
                    .stream()
                    .filter(c -> c.key().equals(k))
                    .findFirst();

            FieldRepresentation field = fieldsGroupByName.get(k);
            if (EMBEDDED.equals(field.getType())) {
                setEmbeddedField(instance, elements, field);
            } else {
                setSingleField(instance, element, field);
            }
        };
    }

    private <T> void setSingleField(T instance, Optional<Property> element, FieldRepresentation field) {
        Object value = element.get().value();
        Optional<Class<? extends AttributeConverter>> converter = field.getConverter();
        if (converter.isPresent()) {
            AttributeConverter attributeConverter = getConverters().get(converter.get());
            Object attributeConverted = attributeConverter.convertToEntityAttribute(value);
            getReflections().setValue(instance, field.getNativeField(), field.getValue(Value.of(attributeConverted)));
        } else {
            getReflections().setValue(instance, field.getNativeField(), field.getValue(Value.of(value)));
        }
    }

    private <T> void setEmbeddedField(T instance, List<Property> elements,
                                      FieldRepresentation field) {
        getReflections().setValue(instance, field.getNativeField(), toEntity(field.getNativeField().getType(), elements));
    }

    private FieldGraph to(FieldRepresentation field, Object entityInstance) {
        Object value = getReflections().getValue(entityInstance, field.getNativeField());
        return FieldGraph.of(value, field);
    }
}
