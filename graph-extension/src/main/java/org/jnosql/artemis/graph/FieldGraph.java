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

import org.apache.tinkerpop.gremlin.structure.Property;
import org.jnosql.artemis.AttributeConverter;
import org.jnosql.artemis.Converters;
import org.jnosql.artemis.reflection.FieldRepresentation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.jnosql.artemis.reflection.FieldType.EMBEDDED;

class FieldGraph {

    private final Object value;

    private final FieldRepresentation field;

    private FieldGraph(Object value, FieldRepresentation field) {
        this.value = value;
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public FieldRepresentation getField() {
        return field;
    }

    public boolean isNotEmpty() {
        return value != null;
    }

    public boolean isId() {
        return field.isId();
    }

    public boolean isNotId() {
        return !isId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldGraph)) {
            return false;
        }
        FieldGraph that = (FieldGraph) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, field);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldGraph{");
        sb.append("value=").append(value);
        sb.append(", field=").append(field);
        sb.append('}');
        return sb.toString();
    }

    public static FieldGraph of(Object value, FieldRepresentation field) {
        return new FieldGraph(value, field);
    }

    public List<Property<?>> toElements(GraphConverter converter, Converters converters) {
        if (EMBEDDED.equals(field.getType())) {
            return converter.getProperties(value);
        }

        Optional<Class<? extends AttributeConverter>> optionalConverter = field.getConverter();
        if (optionalConverter.isPresent()) {
            AttributeConverter attributeConverter = converters.get(optionalConverter.get());
            return singletonList(DefaultProperty.of(field.getName(), attributeConverter.convertToDatabaseColumn(value)));
        }
        return singletonList(DefaultProperty.of(field.getName(), value));
    }

    public Property toElement(Converters converters) {
        Optional<Class<? extends AttributeConverter>> optionalConverter = field.getConverter();
        if (optionalConverter.isPresent()) {
            AttributeConverter attributeConverter = converters.get(optionalConverter.get());
            return DefaultProperty.of(field.getName(), attributeConverter.convertToDatabaseColumn(value));
        }
        return DefaultProperty.of(field.getName(), value);
    }

}
