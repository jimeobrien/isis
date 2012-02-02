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

package org.apache.isis.tck.dom.assocs;

import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.util.TitleBuffer;

public class ChildEntity extends AbstractDomainObject {

    // {{ Identification
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        if (getParent() != null) {
            buf.append(getParent().getName()).append("-");
        }
        buf.append(getName());
        return buf.toString();
    }

    // }}

    // {{ Name
    private String name;

    @MemberOrder(sequence = "1")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ Parent
    private ParentEntity parent;

    @MemberOrder(sequence = "1")
    @Optional
    public ParentEntity getParent() {
        return parent;
    }

    public void setParent(final ParentEntity parent) {
        this.parent = parent;
    }

    // }}

    // {{ moveTo
    @MemberOrder(sequence = "1")
    public ChildEntity moveTo(final ParentEntity newParent) {
        if (newParent == getParent()) {
            // nothing to do
            return this;
        }
        if (getParent() != null) {
            getParent().removeChild(this);
        }
        newParent.getChildren().add(this);
        this.setParent(newParent);
        return this;
    }

    public ParentEntity default0MoveTo() {
        return getParent();
    }

    public List<ParentEntity> choices0MoveTo() {
        return parentEntityRepository.list();
    }

    // }}

    // {{ injected dependencies
    // {{ injected: ParentEntityRepository
    private ParentEntityRepository parentEntityRepository;

    public void setParentEntityRepository(final ParentEntityRepository parentEntityRepository) {
        this.parentEntityRepository = parentEntityRepository;
    }
    // }}

    // }}

}
