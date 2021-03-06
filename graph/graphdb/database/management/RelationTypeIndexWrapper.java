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

package grakn.core.graph.graphdb.database.management;

import com.google.common.base.Preconditions;
import grakn.core.graph.core.RelationType;
import grakn.core.graph.core.schema.RelationTypeIndex;
import grakn.core.graph.core.schema.SchemaStatus;
import grakn.core.graph.graphdb.internal.InternalRelationType;
import grakn.core.graph.graphdb.transaction.StandardJanusGraphTx;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;


public class RelationTypeIndexWrapper implements RelationTypeIndex {

    public static final char RELATION_INDEX_SEPARATOR = ':';

    private final InternalRelationType type;

    public RelationTypeIndexWrapper(InternalRelationType type) {
        Preconditions.checkArgument(type != null && type.getBaseType() != null);
        this.type = type;
    }

    @Override
    public RelationType getType() {
        return type.getBaseType();
    }

    public InternalRelationType getWrappedType() {
        return type;
    }

    @Override
    public String name() {
        String typeName = type.name();
        int index = typeName.lastIndexOf(RELATION_INDEX_SEPARATOR);
        Preconditions.checkArgument(index > 0 && index < typeName.length() - 1, "Invalid name encountered: %s", typeName);
        return typeName.substring(index + 1);
    }

    @Override
    public Order getSortOrder() {
        return type.getSortOrder().getTP();

    }

    @Override
    public RelationType[] getSortKey() {
        StandardJanusGraphTx tx = type.tx();
        long[] ids = type.getSortKey();
        RelationType[] keys = new RelationType[ids.length];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = tx.getExistingRelationType(ids[i]);
        }
        return keys;
    }

    @Override
    public Direction getDirection() {
        if (type.isUnidirected(Direction.BOTH)) {
            return Direction.BOTH;
        } else if (type.isUnidirected(Direction.OUT)) {
            return Direction.OUT;
        } else if (type.isUnidirected(Direction.IN)) {
            return Direction.IN;
        }
        throw new AssertionError();
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object oth) {
        if (oth == null) {
            return false;
        } else if (oth == this) {
            return true;
        } else if (!getClass().isInstance(oth)) {
            return false;
        }
        return type.equals(((RelationTypeIndexWrapper) oth).type);
    }

    @Override
    public SchemaStatus getIndexStatus() {
        return type.getStatus();
    }

    @Override
    public String toString() {
        return name();
    }
}
