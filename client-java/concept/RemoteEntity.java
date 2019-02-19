/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.core.client.concept;

import grakn.core.client.GraknClient;
import grakn.core.graql.concept.Concept;
import grakn.core.graql.concept.ConceptId;
import grakn.core.graql.concept.Entity;
import grakn.core.graql.concept.EntityType;

/**
 * Client implementation of {@link Entity}
 */
public class RemoteEntity extends RemoteThing<Entity, EntityType> implements Entity {

    RemoteEntity(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    static RemoteEntity construct(GraknClient.Transaction tx, ConceptId id) {
        return new RemoteEntity(tx, id);
    }

    @Override
    final EntityType asCurrentType(Concept concept) {
        return concept.asEntityType();
    }

    @Override
    final Entity asCurrentBaseType(Concept other) {
        return other.asEntity();
    }
}