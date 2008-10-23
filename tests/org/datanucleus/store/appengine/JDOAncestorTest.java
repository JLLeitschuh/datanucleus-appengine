// Copyright 2008 Google Inc. All Rights Reserved.
package org.datanucleus.store.appengine;

import com.google.apphosting.api.datastore.Entity;
import com.google.apphosting.api.datastore.Key;
import com.google.apphosting.api.datastore.KeyFactory;
import com.google.apphosting.api.datastore.Query;

import org.datanucleus.test.Flight;
import org.datanucleus.test.HasAncestorJDO;

/**
 * @author Max Ross <maxr@google.com>
 */
public class JDOAncestorTest extends JDOTestCase {

  public void testInsert() {
    Entity flightEntity = Flight.newFlightEntity("max", "bos", "mia", 3, 4);
    ldth.ds.put(flightEntity);
    Key flightKey = flightEntity.getKey();
    HasAncestorJDO ha = new HasAncestorJDO(KeyFactory.encodeKey(flightKey));
    pm.makePersistent(ha);
    Key keyWithParent = KeyFactory.decodeKey(ha.getId());
    assertEquals(flightKey, keyWithParent.getParent());
    // now we'll issue an ancestor query directly against the datastore and see
    // if our object comes back.
    Query q = new Query(ha.getClass().getSimpleName());
    q.setAncestor(flightKey);
    Entity result = ldth.ds.prepare(q).asSingleEntity();
    assertEquals(flightKey, result.getKey().getParent());
  }

  public void testInsertWithNamedKey() {
    Entity flightEntity = Flight.newFlightEntity("parent named key", "max", "bos", "mia", 3, 4);
    ldth.ds.put(flightEntity);
    Key flightKey = flightEntity.getKey();
    HasAncestorJDO ha = new HasAncestorJDO(KeyFactory.encodeKey(flightKey), "named key");
    pm.makePersistent(ha);
    Key keyWithParent = KeyFactory.decodeKey(ha.getId());
    assertEquals(flightKey, keyWithParent.getParent());
    // now we'll issue an ancestor query directly against the datastore and see
    // if our object comes back.
    Query q = new Query(ha.getClass().getSimpleName());
    q.setAncestor(flightKey);
    Entity result = ldth.ds.prepare(q).asSingleEntity();
    assertEquals(flightKey, result.getKey().getParent());
    assertEquals("named key", result.getKey().getName());
    assertEquals("parent named key", result.getKey().getParent().getName());
  }

  public void testFetch() {
    Entity flightEntity = Flight.newFlightEntity("max", "bos", "mia", 3, 4);
    ldth.ds.put(flightEntity);
    Entity hasAncestorEntity = new Entity(HasAncestorJDO.class.getName(), flightEntity.getKey());
    ldth.ds.put(hasAncestorEntity);

    HasAncestorJDO ha = pm.getObjectById(HasAncestorJDO.class, KeyFactory.encodeKey(hasAncestorEntity.getKey()));
    assertEquals(KeyFactory.encodeKey(flightEntity.getKey()), ha.getAncestorId());
  }

  public void testFetchWithNamedKey() {
    Entity flightEntity = Flight.newFlightEntity("parent named key", "max", "bos", "mia", 3, 4);
    ldth.ds.put(flightEntity);
    Entity hasAncestorEntity =
        new Entity(HasAncestorJDO.class.getName(), "named key", flightEntity.getKey());
    ldth.ds.put(hasAncestorEntity);

    HasAncestorJDO ha = pm.getObjectById(HasAncestorJDO.class, KeyFactory.encodeKey(hasAncestorEntity.getKey()));
    assertEquals(KeyFactory.encodeKey(flightEntity.getKey()), ha.getAncestorId());
    assertEquals("named key", KeyFactory.decodeKey(ha.getId()).getName());
    assertEquals("parent named key", KeyFactory.decodeKey(ha.getId()).getParent().getName());
  }

  public void testInsertWithNullAncestor() {
    HasAncestorJDO ha = new HasAncestorJDO(null);
    pm.makePersistent(ha);
    Key keyWithParent = KeyFactory.decodeKey(ha.getId());
    assertNull(keyWithParent.getParent());
  }
}