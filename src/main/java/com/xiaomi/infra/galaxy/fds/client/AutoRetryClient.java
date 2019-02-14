package com.xiaomi.infra.galaxy.fds.client;

import com.google.common.base.Preconditions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Created by yepeng on 18-7-10.
 */
public class AutoRetryClient {

  private static class AutoRetryHandler implements InvocationHandler {
    private static final Log LOG = LogFactory.getLog(AutoRetryHandler.class);
    private GalaxyFDS fdsClient;
    private int maxRetry;
    private Set<String> retryMethodSet;

    private AutoRetryHandler(GalaxyFDS fdsClient, int maxRetry){
      this.fdsClient = fdsClient;
      this.maxRetry = maxRetry;
      this.retryMethodSet = fdsClient.getRetryMethodSet();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if(!retryMethodSet.contains(method.getName())) {
        try {
          return method.invoke(fdsClient, args);
        } catch (InvocationTargetException e){
          throw e.getCause();
        }
      } else {
        int retry = 0;
        do {
          try {
            Object ret = method.invoke(fdsClient, args);
            return ret;
          } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            Integer errorCode = Utils.getErrorCode(cause.getMessage());
            if(errorCode != null && errorCode != 429 && errorCode < 500){
              throw cause;
            }

            if (maxRetry < 0 || retry >= maxRetry) {
              LOG.error("reach max retry number, retry: " + retry);
              throw cause;
            }
            ++retry;
            LOG.info("Auto retrying RPC call " + method.getName() + " for " + retry + " time");
          }
        } while (true);
      }
    }
  }

  public static GalaxyFDS getAutoRetryClient(GalaxyFDS fdsClient, int maxRetry) {
    Preconditions.checkArgument(maxRetry >= 1, "Expected maxRetry > 1");
    return (GalaxyFDS) Proxy.newProxyInstance(GalaxyFDSClient.class.getClassLoader(),
        GalaxyFDSClient.class.getInterfaces(),
        new AutoRetryHandler(fdsClient, maxRetry));
  }

  public static GalaxyFDS getAutoRetryClient(GalaxyFDS fdsClient){
    return getAutoRetryClient(fdsClient, 1);
  }
}
