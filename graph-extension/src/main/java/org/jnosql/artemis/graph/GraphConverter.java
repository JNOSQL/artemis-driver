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
import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface GraphConverter {
    
    /**
     * add a new vertex from entity
     * 
     * @param entity entity
     * @param <T> entity type
     * @return return a new aeed vertex
     */
     <T> Vertex toNewVertex(T entity);
  
     /**
      * update vertex property from entity
      * 
      * @param entity entity
      * @param <T> entity type
      * @param vertex vertex
      * @return updated vertex
      */
      <T> Vertex toVertex(T entity, Vertex vertex);
     
    /**
     * Converts entity object to  TinkerPop Vertex
     *
     * @param entity the entity
     * @param <T>    the entity type
     * @return the ThinkerPop Vertex with the entity values
     * @throws NullPointerException when entity is null
     * @deprecated
     */
    <T> Vertex toVertex(T entity);

    /**
     * Converts vertex to an entity
     *
     * @param vertex the vertex
     * @param <T>    the entity type
     * @return a entity instance
     * @throws NullPointerException when vertex is null
     */
    <T> T toEntity(Vertex vertex);

    /**
     * Converts vertex to an entity
     *
     * @param entityClass the entity class
     * @param vertex      the vertex
     * @param <T>         the entity type
     * @return a entity instance
     * @throws NullPointerException when vertex or entityClass is null
     */
    <T> T toEntity(Class<T> entityClass, Vertex vertex);

    /**
     * Converts vertex to an entity
     * Instead of creating a new object is uses the instance used in this parameters
     *
     * @param entityInstance the entity class
     * @param vertex         the vertex
     * @param <T>            the entity type
     * @return a entity instance
     * @throws NullPointerException when vertex or entityInstance is null
     */
    <T> T toEntity(T entityInstance, Vertex vertex);

    /**
     * Converts {@link EdgeEntity} from {@link Edge} Thinkerpop
     *
     * @param edge the ThinkerPop edge
     * @return an EdgeEntity instance
     * @throws NullPointerException when Edge is null
     */
    EdgeEntity toEdgeEntity(Edge edge);

    /**
     * Converts {@link Edge} from {@link EdgeEntity}
     *
     * @param edge the EdgeEntity instance
     * @return a Edge instance
     * @throws NullPointerException when edge entity is null
     */
    Edge toEdge(EdgeEntity edge);


}
