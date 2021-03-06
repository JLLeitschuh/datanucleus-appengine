package com.google.appengine.datanucleus.bugs.test;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Issue228Child {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  Key id;

  String name;

  Issue228Parent parent;

  public String getName() {
    return name;
  }

  public void setName(String str) {
    this.name = str;
  }

  public void setParent(Issue228Parent o) {
    this.parent = o;
  }

  public Issue228Parent getParent() {
    return parent;
  }
}
