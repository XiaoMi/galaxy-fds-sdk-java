package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by zhangjunbin on 4/20/15.
 */

public class ListDomainMappingsResult {
  private List<String> domainMappings = new ArrayList<String>();

  public List<String> getDomainMappings() {
    return domainMappings;
  }

  public void setDomainMappings(List<String> domainMappings) {
    this.domainMappings = domainMappings;
  }
}
