package com.xiaomi.infra.galaxy.fds.result;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by zhangjunbin on 4/20/15.
 */

@XmlRootElement
public class ListDomainMappingsResult {
  private List<String> domainMappings = new ArrayList<String>();

  public List<String> getDomainMappings() {
    return domainMappings;
  }

  public void setDomainMappings(List<String> domainMappings) {
    this.domainMappings = domainMappings;
  }
}
