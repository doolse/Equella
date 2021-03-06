/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.lti.consumers.entity;

import com.tle.beans.IdCloneable;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
public class LtiConsumerCustomRole implements Serializable, IdCloneable {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(length = 255)
  private String ltiRole;

  @Column(length = 255)
  private String equellaRole;

  @JoinColumn(name = "lti_consumer_id", insertable = false, updatable = false, nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private LtiConsumer consumer;

  @Override
  public void setId(long id) {
    this.id = id;
  }

  @Override
  public long getId() {
    return id;
  }

  public LtiConsumer getConsumer() {
    return consumer;
  }

  public void setConsumer(LtiConsumer consumer) {
    this.consumer = consumer;
  }

  public String getEquellaRole() {
    return equellaRole;
  }

  public void setEquellaRole(String equellaRole) {
    this.equellaRole = equellaRole;
  }

  public String getLtiRole() {
    return ltiRole;
  }

  public void setLtiRole(String ltiRole) {
    this.ltiRole = ltiRole;
  }
}
