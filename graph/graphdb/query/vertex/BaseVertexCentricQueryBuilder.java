/*
 * GRAKN.AI - THE KNOWLEDGE GRAPH
 * Copyright (C) 2019 Grakn Labs Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.core.graph.graphdb.query.vertex;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import grakn.core.graph.core.BaseVertexQuery;
import grakn.core.graph.core.JanusGraphRelation;
import grakn.core.graph.core.JanusGraphVertex;
import grakn.core.graph.core.PropertyKey;
import grakn.core.graph.core.RelationType;
import grakn.core.graph.core.attribute.Cmp;
import grakn.core.graph.core.schema.SchemaInspector;
import grakn.core.graph.graphdb.internal.Order;
import grakn.core.graph.graphdb.internal.OrderList;
import grakn.core.graph.graphdb.internal.RelationCategory;
import grakn.core.graph.graphdb.query.JanusGraphPredicate;
import grakn.core.graph.graphdb.query.Query;
import grakn.core.graph.graphdb.query.condition.PredicateCondition;
import grakn.core.graph.graphdb.relations.RelationIdentifier;
import grakn.core.graph.graphdb.tinkerpop.ElementUtils;
import grakn.core.graph.graphdb.types.system.ImplicitKey;
import grakn.core.graph.graphdb.types.system.SystemRelationType;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link BaseVertexQuery}, optimizes the query and compiles the result into a {@link BaseVertexCentricQuery} which
 * is then executed by one of the extending classes.
 */
public abstract class BaseVertexCentricQueryBuilder<Q extends BaseVertexQuery<Q>> implements BaseVertexQuery<Q> {

    private static final String[] NO_TYPES = new String[0];
    private static final List<PredicateCondition<String, JanusGraphRelation>> NO_CONSTRAINTS = ImmutableList.of();

    /**
     * The direction of this query. BOTH by default
     */
    protected Direction dir = Direction.BOTH;
    /**
     * The relation types (labels or keys) to query for. None by default which means query for any relation type.
     */
    protected String[] types = NO_TYPES;
    /**
     * The constraints added to this query. None by default.
     */
    protected List<PredicateCondition<String, JanusGraphRelation>> constraints = NO_CONSTRAINTS;
    /**
     * The vertex to be used for the adjacent vertex constraint. If null, that means no such constraint. Null by default.
     */
    protected JanusGraphVertex adjacentVertex = null;
    /**
     * The order in which the relations should be returned. None by default.
     */
    protected final OrderList orders = new OrderList();
    /**
     * The limit of this query. No limit by default.
     */
    protected int limit = Query.NO_LIMIT;

    private final SchemaInspector schemaInspector;

    protected BaseVertexCentricQueryBuilder(SchemaInspector schemaInspector) {
        this.schemaInspector = schemaInspector;
    }

    protected abstract Q getThis();

    protected abstract JanusGraphVertex getVertex(long vertexId);


    /* ---------------------------------------------------------------
     * Query Construction
     * ---------------------------------------------------------------
     */

    @Override
    public Q adjacent(Vertex vertex) {
        Preconditions.checkArgument(vertex instanceof JanusGraphVertex, "Not a valid vertex provided for adjacency constraint");
        this.adjacentVertex = (JanusGraphVertex) vertex;
        return getThis();
    }

    private Q addConstraint(String type, JanusGraphPredicate rel, Object value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(type));
        Preconditions.checkNotNull(rel);
        //Treat special cases
        if (type.equals(ImplicitKey.ADJACENT_ID.name())) {
            Preconditions.checkArgument(rel == Cmp.EQUAL, "Only equality constraints are supported for %s", type);
            long vertexId = ElementUtils.getVertexId(value);
            return adjacent(getVertex(vertexId));
        } else if (type.equals(ImplicitKey.ID.name())) {
            RelationIdentifier rid = ElementUtils.getEdgeId(value);
            Preconditions.checkNotNull(rid, "Expected valid relation id: %s", value);
            return addConstraint(ImplicitKey.JANUSGRAPHID.name(), rel, rid.getRelationId());
        } else {
            Preconditions.checkArgument(rel.isValidCondition(value), "Invalid condition provided: " + value);
        }
        if (constraints == NO_CONSTRAINTS) constraints = new ArrayList<>(5);
        constraints.add(new PredicateCondition<>(type, rel, value));
        return getThis();
    }

    @Override
    public Q has(String type, Object value) {
        return addConstraint(type, Cmp.EQUAL, value);
    }

    @Override
    public Q hasNot(String key, Object value) {
        return has(key, Cmp.NOT_EQUAL, value);
    }

    @Override
    public Q has(String key) {
        return has(key, Cmp.NOT_EQUAL, null);
    }

    @Override
    public Q hasNot(String key) {
        return has(key, Cmp.EQUAL, null);
    }

    @Override
    public Q has(String key, JanusGraphPredicate predicate, Object value) {
        return addConstraint(key, predicate, value);
    }

    @Override
    public <T extends Comparable<?>> Q interval(String key, T start, T end) {
        addConstraint(key, Cmp.GREATER_THAN_EQUAL, start);
        return addConstraint(key, Cmp.LESS_THAN, end);
    }

    @Override
    public Q types(RelationType... types) {
        String[] ts = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ts[i] = types[i].name();
        }
        return types(ts);
    }


    @Override
    public Q labels(String... labels) {
        return types(labels);
    }

    @Override
    public Q keys(String... keys) {
        return types(keys);
    }

    public Q type(RelationType type) {
        return types(type.name());
    }

    @Override
    public Q types(String... types) {
        if (types == null) types = NO_TYPES;
        for (String type : types) Preconditions.checkArgument(StringUtils.isNotBlank(type), "Invalid type: %s", type);
        this.types = types;
        return getThis();
    }

    @Override
    public Q direction(Direction d) {
        Preconditions.checkNotNull(d);
        dir = d;
        return getThis();
    }

    @Override
    public Q limit(int limit) {
        Preconditions.checkArgument(limit >= 0);
        this.limit = limit;
        return getThis();
    }

    @Override
    public Q orderBy(String keyName, org.apache.tinkerpop.gremlin.process.traversal.Order order) {
        Preconditions.checkArgument(schemaInspector.containsPropertyKey(keyName), "Provided key does not exist: %s", keyName);
        PropertyKey key = schemaInspector.getPropertyKey(keyName);
        Preconditions.checkArgument(key != null && order != null, "Need to specify and key and an order");
        Preconditions.checkArgument(Comparable.class.isAssignableFrom(key.dataType()), "Can only order on keys with comparable data type. [%s] has datatype [%s]", key.name(), key.dataType());
        Preconditions.checkArgument(!(key instanceof SystemRelationType), "Cannot use system types in ordering: %s", key);
        Preconditions.checkArgument(!orders.containsKey(key));
        Preconditions.checkArgument(orders.isEmpty(), "Only a single sort order is supported on vertex queries");
        orders.add(key, Order.convert(order));
        return getThis();
    }


    /* ---------------------------------------------------------------
     * Inspection Methods
     * ---------------------------------------------------------------
     */

    protected final boolean hasTypes() {
        return types.length > 0;
    }

    protected final boolean hasSingleType() {
        return types.length == 1 && schemaInspector.getRelationType(types[0]) != null;
    }

    protected final RelationType getSingleType() {
        Preconditions.checkArgument(hasSingleType());
        return schemaInspector.getRelationType(types[0]);
    }

    /**
     * Whether this query is asking for the value of an {@link ImplicitKey}.
     * <p>
     * Handling of implicit keys is completely distinct from "normal" query execution and handled extra
     * for completeness reasons.
     */
    protected final boolean isImplicitKeyQuery(RelationCategory returnType) {
        return returnType != RelationCategory.EDGE && types.length == 1 && constraints.isEmpty() && schemaInspector.getRelationType(types[0]) instanceof ImplicitKey;
    }


}
