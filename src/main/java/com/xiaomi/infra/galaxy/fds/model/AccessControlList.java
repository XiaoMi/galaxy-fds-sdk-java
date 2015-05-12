package com.xiaomi.infra.galaxy.fds.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

public class AccessControlList {

  public enum Permission {
    // The READ permission: when it applies to buckets it means
    // allow the grantee to list the objects in the bucket; when
    // it applies to objects it means allow the grantee to read
    // the object data and metadata.
    READ(0x01),

    // The WRITE permission: when it applies to buckets it means
    // allow the grantee to create, overwrite and delete any
    // object in the bucket; it is not applicable for objects.
    WRITE(0x02),

    // The FULL_CONTROL permission: allows the grantee the READ
    // and WRITE permission on the bucket/object.
    FULL_CONTROL(0xff);

    private final int value;

    private Permission(int value) {
      this.value = value;
    }

    public int getValue() {
      return this.value;
    }
  }

  // Predefined user groups
  public enum UserGroups {
    ALL_USERS,
    AUTHENTICATED_USERS
  }

  public enum GrantType {
    USER,
    GROUP
  }

  public static class Grant {

    private String granteeId;
    private Permission permission;
    private GrantType type;

    public Grant(String granteeId, Permission permission) {
      this(granteeId, permission, GrantType.USER);
    }

    public Grant(String granteeId, Permission permission,
        GrantType type) {
      this.granteeId = granteeId;
      this.permission = permission;
      this.type = type;
    }

    public String getGranteeId() {
      return granteeId;
    }
    public void setGranteeId(String granteeId) {
      this.granteeId = granteeId;
    }

    public Permission getPermission() {
      return permission;
    }
    public void setPermission(Permission permission) {
      this.permission = permission;
    }

    public GrantType getType() {
      return type;
    }

    public void setType(GrantType type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return granteeId + ":" + type.name()  + ":" + permission.name();
    }

    public static Grant fromString(String string) {
      String[] tokens = string.split(":");
      Preconditions.checkState(tokens.length == 3);
      String granteeId = tokens[0];
      GrantType type = GrantType.valueOf(tokens[1]);
      Permission permission = Permission.valueOf(tokens[2]);
      return new Grant(granteeId, permission, type);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Grant grant = (Grant) o;

      if (granteeId != null ? !granteeId.equals(grant.granteeId) :
          grant.granteeId != null)
        return false;

      if (permission != grant.permission) return false;

      if (type != grant.type) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = granteeId != null ? granteeId.hashCode() : 0;
      result = 31 * result + (permission != null ? permission.hashCode() : 0);
      result = 31 * result + (type != null ? type.hashCode() : 0);
      return result;
    }
  }

  private final Map<String, Integer> acls = new HashMap<String, Integer>();

  public void addGrant(Grant grant) {
    String key = grant.getGranteeId() + ":" + grant.getType().name();
    Integer perm = acls.get(key);

    if (perm == null) {
      acls.put(key, grant.getPermission().getValue());
    } else {
      perm = perm.intValue() | grant.getPermission().getValue();
      acls.put(key, perm);
    }
  }

  public boolean checkPermission(String grantee, GrantType type,
      Permission permission) {
    Integer perm = acls.get(grantee + ":" + type.name());
    System.out.println("checkPermission2: permission=" + permission);
    System.out.println("checkPermission2: perm=" + perm);
    if (perm != null) {
      return (permission.getValue() & perm.intValue()) > 0;
    }
    return false;
  }

  public boolean checkUserReadPermission(String grantee) {
    return checkPermission(grantee, GrantType.USER, Permission.READ);
  }

  public boolean checkUserWritePermission(String grantee) {
    return checkPermission(grantee, GrantType.USER, Permission.WRITE);
  }

  public boolean checkGroupReadPermission(String grantee) {
    return checkPermission(grantee, GrantType.GROUP, Permission.READ);
  }

  public boolean checkGroupWritePermission(String grantee) {
    return checkPermission(grantee, GrantType.GROUP, Permission.WRITE);
  }

  public List<Grant> getGrantList() {
    List<Grant> grants = new LinkedList<Grant>();
    for (Map.Entry<String, Integer> entry : acls.entrySet()) {
      String[] tokens = entry.getKey().split(":");
      if (entry.getValue().intValue() == Permission.FULL_CONTROL.getValue()) {
        grants.add(new Grant(tokens[0], Permission.FULL_CONTROL,
            GrantType.valueOf(tokens[1])));
      } else {
        for (Permission p : Permission.values()) {
          if (p.getValue() != Permission.FULL_CONTROL.getValue() &&
              (p.getValue() & entry.getValue().intValue()) > 0) {
            grants.add(new Grant(tokens[0], p, GrantType.valueOf(tokens[1])));
          }
        }
      }
    }
    return grants;
  }
}
