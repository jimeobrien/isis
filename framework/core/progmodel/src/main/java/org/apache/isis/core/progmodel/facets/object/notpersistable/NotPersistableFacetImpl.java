/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.progmodel.facets.object.notpersistable;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.notpersistable.InitiatedBy;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacetAbstract;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;

public class NotPersistableFacetImpl extends NotPersistableFacetAbstract {

    public NotPersistableFacetImpl(final InitiatedBy value, final FacetHolder holder) {
        super(value, holder);
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        final InitiatedBy initiatedBy = value();
        if (initiatedBy == InitiatedBy.USER_OR_PROGRAM) {
            // never persistable
            return "Not persistable";
        } else {
            if (ic.isProgrammatic()) {
                // enabled only in this case
                return null;
            } else {
                // not programmatic, ie by user, so disabled
                return "Not persistable";
            }
        }
    }

}
