package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.Date;

/**
 * Represents an Galaxy FDS bucket.
 */
public class FDSBucket {

  private String name;
  private Date creationDate;
  private Owner owner;

  public FDSBucket(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Owner getOwner() {
    return owner;
  }

  public void setOwner(Owner owner) {
    this.owner = owner;
  }
}