/*
 * /**********************************************************************
 * Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * **********************************************************************/
package com.google.appengine.datanucleus.jdo;

import java.util.Collection;

import org.datanucleus.util.NucleusLogger;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.Utils;
import com.google.appengine.datanucleus.test.jdo.EmbeddedArrayOwner;
import com.google.appengine.datanucleus.test.jdo.EmbeddedChildPC;
import com.google.appengine.datanucleus.test.jdo.EmbeddedCollectionOwner;
import com.google.appengine.datanucleus.test.jdo.EmbeddedParentPC;
import com.google.appengine.datanucleus.test.jdo.EmbeddedRelatedBase;
import com.google.appengine.datanucleus.test.jdo.EmbeddedRelatedSub;
import com.google.appengine.datanucleus.test.jdo.Flight;
import com.google.appengine.datanucleus.test.jdo.HasEmbeddedJDO;
import com.google.appengine.datanucleus.test.jdo.HasEmbeddedPc;
import com.google.appengine.datanucleus.test.jdo.HasEmbeddedWithKeyPkJDO;
import com.google.appengine.datanucleus.test.jdo.HasKeyPkJDO;

/**
 * @author Max Ross <maxr@google.com>
 */
public class JDOEmbeddedTest extends JDOTestCase {

  public void testEmbeddedWithGeneratedId() throws EntityNotFoundException {
    HasEmbeddedJDO pojo = new HasEmbeddedJDO();
    Flight f = new Flight();
    f.setId("yarg");
    f.setFlightNumber(23);
    f.setName("harold");
    f.setOrigin("bos");
    f.setDest("mia");
    f.setYou(24);
    f.setMe(25);
    pojo.setFlight(f);

    Flight f2 = new Flight();
    f2.setId("blarg");
    f2.setFlightNumber(26);
    f2.setName("jimmy");
    f2.setOrigin("jfk");
    f2.setDest("sea");
    f2.setYou(28);
    f2.setMe(29);
    pojo.setAnotherFlight(f2);

    HasEmbeddedJDO.Embedded1 embedded1 = new HasEmbeddedJDO.Embedded1();
    pojo.setEmbedded1(embedded1);
    embedded1.setVal1("v1");
    embedded1.setMultiVal1(Utils.newArrayList("yar1", "yar2"));
    HasEmbeddedJDO.Embedded2 embedded2 = new HasEmbeddedJDO.Embedded2();
    embedded2.setVal2("v2");
    embedded2.setMultiVal2(Utils.newArrayList("bar1", "bar2"));
    embedded1.setEmbedded2(embedded2);
    beginTxn();
    pm.makePersistent(pojo);
    commitTxn();

    Entity e = ds.get(KeyFactory.createKey(kindForClass(pojo.getClass()), pojo.getId()));
    assertTrue(e.hasProperty("flightId")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("origin")); // Uses column names from Flight class since not overridden
    assertTrue(e.hasProperty("dest")); // Uses column names from Flight class since not overridden
    assertTrue(e.hasProperty("name")); // Uses column names from Flight class since not overridden
    assertTrue(e.hasProperty("you")); // Uses column names from Flight class since not overridden
    assertTrue(e.hasProperty("me")); // Uses column names from Flight class since not overridden
    assertTrue(e.hasProperty("flight_number")); // Uses column names from Flight class since not overridden
    assertTrue(e.hasProperty("ID")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("ORIGIN")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("DEST")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("NAME")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("YOU")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("ME")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("FLIGHTNUMBER")); // Uses column names from embedded mapping
    assertTrue(e.hasProperty("val1"));
    assertTrue(e.hasProperty("multiVal1"));
    assertTrue(e.hasProperty("val2"));
    assertTrue(e.hasProperty("multiVal2"));
    assertEquals(18, e.getProperties().size());

    assertEquals(1, countForClass(HasEmbeddedJDO.class));
    assertEquals(0, countForClass(Flight.class));
    switchDatasource(PersistenceManagerFactoryName.transactional);
    beginTxn();
    pojo = pm.getObjectById(HasEmbeddedJDO.class, pojo.getId());
    assertNotNull(pojo.getFlight());
    // it's weird but flight doesn't have an equals() method
    assertTrue(f.customEquals(pojo.getFlight()));
    assertNotNull(pojo.getAnotherFlight());
    assertTrue(f2.customEquals(pojo.getAnotherFlight()));
    
    assertNotNull(pojo.getEmbedded1());
    assertEquals("v1", pojo.getEmbedded1().getVal1());
    assertEquals(Utils.newArrayList("yar1", "yar2"), pojo.getEmbedded1().getMultiVal1());
    assertNotNull(pojo.getEmbedded1().getEmbedded2());
    assertEquals("v2", pojo.getEmbedded1().getEmbedded2().getVal2());
    assertEquals(Utils.newArrayList("bar1", "bar2"), pojo.getEmbedded1().getEmbedded2().getMultiVal2());
    commitTxn();
  }

  public void testEmbeddedWithKeyPk_NullEmbedded() {
    HasEmbeddedWithKeyPkJDO pojo = new HasEmbeddedWithKeyPkJDO();
    beginTxn();
    pm.makePersistent(pojo);
    commitTxn();
    pm.evictAll();
    pmf.getDataStoreCache().evictAll();

    // Failed on GAE v1.x
    beginTxn();
    pojo = pm.getObjectById(HasEmbeddedWithKeyPkJDO.class, pojo.getId());
    assertNull(pojo.getEmbedded());
    commitTxn();
  }

  public void testEmbeddedWithKeyPk_NotNullEmbedded() {
    HasEmbeddedWithKeyPkJDO pojo = new HasEmbeddedWithKeyPkJDO();
    HasKeyPkJDO embedded = new HasKeyPkJDO();
    embedded.setStr("yar");
    pojo.setEmbedded(embedded);
    beginTxn();
    pm.makePersistent(pojo);
    commitTxn();
    pm.evictAll();
    pmf.getDataStoreCache().evictAll();

    beginTxn();
    pojo = pm.getObjectById(HasEmbeddedWithKeyPkJDO.class, pojo.getId());
    assertNotNull(pojo.getEmbedded());
    assertEquals("yar", pojo.getEmbedded().getStr());
    commitTxn();
  }

  public void testEmbeddedWithKeyPk_AddEmbeddedToExistingParent() {
    HasEmbeddedWithKeyPkJDO pojo = new HasEmbeddedWithKeyPkJDO();
    beginTxn();
    pm.makePersistent(pojo);
    commitTxn();

    HasKeyPkJDO embedded = new HasKeyPkJDO();
    embedded.setStr("yar");
    beginTxn();
    pojo.setEmbedded(embedded);
    pojo = pm.getObjectById(HasEmbeddedWithKeyPkJDO.class, pojo.getId());
    pojo.setEmbedded(embedded);
    commitTxn();
  }

  public void testEmbeddingPC() throws EntityNotFoundException {
    HasEmbeddedPc parent = new HasEmbeddedPc();
    HasKeyPkJDO embedded = new HasKeyPkJDO();
    embedded.setKey(KeyFactory.createKey("blar", 43L));
    parent.setEmbedded(embedded);
    beginTxn();
    pm.makePersistent(parent);
    commitTxn();
    Entity e = ds.get(parent.getKey());
    assertTrue(e.hasProperty("key"));
  }

  public void testEmbeddingPCWithIdField() {
    EmbeddedParentPC pi = new EmbeddedParentPC();
    pi.setChild(new EmbeddedChildPC(1, "Hi"));

    // Failed on GAE v1.x
    pm.currentTransaction().begin();
    pm.makePersistent(pi);
    pm.currentTransaction().commit();
  }

  public void testEmbeddedCollection() {
    Object id = null;
    Key ownerKey = null;
    try {
      EmbeddedCollectionOwner owner = new EmbeddedCollectionOwner();
      EmbeddedRelatedBase baseRel1 = new EmbeddedRelatedBase("First Base", 100);
      owner.addChild(baseRel1);
      EmbeddedRelatedSub subRel2 = new EmbeddedRelatedSub("Second Base", 200, "Other Type");
      owner.addChild(subRel2);

      pm.currentTransaction().begin();
      pm.makePersistent(owner);
      pm.currentTransaction().commit();
      id = pm.getObjectId(owner);
      ownerKey = owner.getKey();
    } catch (Exception e) {
      NucleusLogger.PERSISTENCE.error("Exception on persist of embedded collection", e);
      fail("Exception occurred on persist of embedded collection : " + e.getMessage());
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
      pm.close();
    }
    pmf.getDataStoreCache().evictAll();

    // Check datastore values direct
    try {
      Entity entity = ds.get(ownerKey);
      assertTrue(entity.hasProperty("children.size"));
      Object propVal = entity.getProperty("children.size");
      assertNotNull(propVal);
      long numChildren = (Long)entity.getProperty("children.size");
      assertEquals(2, numChildren);

      assertTrue(entity.hasProperty("name.0"));
      assertTrue(entity.hasProperty("value.0"));
      assertTrue(entity.hasProperty("name.1"));
      assertTrue(entity.hasProperty("value.1"));
    } catch (EntityNotFoundException enfe) {
      fail("Failure to retrieve Entity for persisted owner with embedded collection");
    }

    // Check retrieval
    pm = pmf.getPersistenceManager();
    try {
      pm.currentTransaction().begin();
      EmbeddedCollectionOwner owner = (EmbeddedCollectionOwner)pm.getObjectById(id);
      Collection<EmbeddedRelatedBase> children = owner.getChildren();
      assertEquals(2, children.size());
      boolean firstPresent = false;
      boolean secondPresent = false;
      for (EmbeddedRelatedBase elem : children) {
        if (elem.getName().equals("First Base") && elem.getValue() == 100 &&
            elem.getClass().getName().equals(EmbeddedRelatedBase.class.getName())) {
          firstPresent = true;
        } else if (elem.getName().equals("Second Base") && elem.getValue() == 200 &&
            elem.getClass().getName().equals(EmbeddedRelatedSub.class.getName())) {
          secondPresent = true;
        }
      }
      assertTrue(firstPresent);
      assertTrue(secondPresent);
      pm.currentTransaction().commit();
    } catch (Exception e) {
      NucleusLogger.PERSISTENCE.error("Exception on retrieve of embedded collection", e);
      fail("Exception occurred on retrieve of embedded collection : " + e.getMessage());
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
    }
  }

  public void testEmbeddedArray() {
    Object id = null;
    Key ownerKey = null;
    try {
      EmbeddedArrayOwner owner = new EmbeddedArrayOwner();
      EmbeddedRelatedBase baseRel1 = new EmbeddedRelatedBase("First Base", 100);
      EmbeddedRelatedSub subRel2 = new EmbeddedRelatedSub("Second Base", 200, "Other Type");
      EmbeddedRelatedBase[] array = new EmbeddedRelatedBase[]{baseRel1, subRel2};
      owner.setArray(array);

      pm.currentTransaction().begin();
      pm.makePersistent(owner);
      pm.currentTransaction().commit();
      id = pm.getObjectId(owner);
      ownerKey = owner.getKey();
    } catch (Exception e) {
      NucleusLogger.PERSISTENCE.error("Exception on persist of embedded array", e);
      fail("Exception occurred on persist of embedded array : " + e.getMessage());
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
      pm.close();
    }
    pmf.getDataStoreCache().evictAll();

    // Check datastore values direct
    try {
      Entity entity = ds.get(ownerKey);
      assertTrue(entity.hasProperty("array.size"));
      Object propVal = entity.getProperty("array.size");
      assertNotNull(propVal);
      long numChildren = (Long)entity.getProperty("array.size");
      assertEquals(2, numChildren);

      assertTrue(entity.hasProperty("name.0"));
      assertTrue(entity.hasProperty("value.0"));
      assertTrue(entity.hasProperty("name.1"));
      assertTrue(entity.hasProperty("value.1"));
    } catch (EntityNotFoundException enfe) {
      fail("Failure to retrieve Entity for persisted owner with embedded array");
    }

    // Check retrieval
    pm = pmf.getPersistenceManager();
    try {
      pm.currentTransaction().begin();
      EmbeddedArrayOwner owner = (EmbeddedArrayOwner)pm.getObjectById(id);
      EmbeddedRelatedBase[] array = owner.getArray();
      assertEquals(2, array.length);
      for (int i=0;i<array.length;i++) {
        if (i == 0) {
          assertTrue("First element incorrect",
              array[i].getName().equals("First Base") && array[i].getValue() == 100 &&
              array[i].getClass().getName().equals(EmbeddedRelatedBase.class.getName()));
        } else if (i == 1) {
          assertTrue("Second element incorrect",
              array[i].getName().equals("Second Base") && array[i].getValue() == 200 &&
              array[i].getClass().getName().equals(EmbeddedRelatedSub.class.getName()));
        }
      }
      pm.currentTransaction().commit();
    } catch (Exception e) {
      NucleusLogger.PERSISTENCE.error("Exception on retrieve of embedded array", e);
      fail("Exception occurred on retrieve of embedded array : " + e.getMessage());
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
    }
  }
}
