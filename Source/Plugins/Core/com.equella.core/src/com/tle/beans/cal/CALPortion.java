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

package com.tle.beans.cal;

import com.tle.beans.item.Item;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

@Entity
@AccessType("field")
@Table(name = "cal_portion")
public class CALPortion implements Portion {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @JoinColumn(nullable = false)
  @Index(name = "portionItemIndex")
  @ManyToOne(fetch = FetchType.LAZY)
  private Item item;

  @Index(name = "holdingIndex")
  @ManyToOne(fetch = FetchType.LAZY)
  private CALHolding holding;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "portion")
  private List<CALSection> sections;

  @ElementCollection
  @Column(name = "element")
  @CollectionTable(name = "cal_portion_authors", joinColumns = @JoinColumn(name = "cal_portion_id"))
  private List<String> authors;

  @ElementCollection
  @Column(name = "element")
  @CollectionTable(name = "cal_portion_topics", joinColumns = @JoinColumn(name = "cal_portion_id"))
  private List<String> topics;

  @Lob private String authorList;

  @Column(length = 200)
  private String chapter;

  @Lob private String title;

  @Override
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public Item getItem() {
    return item;
  }

  @Override
  public void setItem(Item item) {
    this.item = item;
  }

  @Override
  public CALHolding getHolding() {
    return holding;
  }

  public void setHolding(CALHolding holding) {
    this.holding = holding;
  }

  @Override
  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  @Override
  public List<? extends Section> getSections() {
    return sections;
  }

  public void setSections(List<CALSection> sections) {
    this.sections = sections;
  }

  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public List<String> getTopics() {
    return topics;
  }

  public void setTopics(List<String> topics) {
    this.topics = topics;
  }

  @Override
  public String getAuthorList() {
    return authorList;
  }

  public void setAuthorList(String authorList) {
    this.authorList = authorList;
  }

  @Override
  public String getChapter() {
    return chapter;
  }

  public void setChapter(String chapter) {
    if (chapter.length() > 200) {
      chapter = chapter.substring(0, 200);
    }
    chapter = chapter.trim().toLowerCase();
    if (chapter.length() == 0) {
      chapter = null;
    }
    this.chapter = chapter;
  }

  public List<CALSection> getCALSections() {
    return sections;
  }

  @Override
  public void setHolding(Holding holding) {
    this.holding = (CALHolding) holding;
  }
}
