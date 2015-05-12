package com.xiaomi.infra.galaxy.fds.client.metrics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.model.ClientMetrics;
import com.xiaomi.infra.galaxy.fds.model.MetricData;


/**
 * Created by zhangjunbin on 3/16/15.
 */
public class MetricsCollector {

  private static final Log LOG = LogFactory.getLog(MetricsCollector.class);

  private GalaxyFDSClient fdsClient;

  private BlockingQueue<MetricData> queue;

  private MetricUploaderThread metricUploaderThread;

  public MetricsCollector(GalaxyFDSClient fdsClient) {
    this.fdsClient = fdsClient;
    queue = new LinkedBlockingDeque<MetricData>();
    metricUploaderThread = new MetricUploaderThread(queue);
    metricUploaderThread.setDaemon(true);
    metricUploaderThread.start();
  }

  public void collect(RequestMetrics requestMetrics) {
    queue.addAll(requestMetrics.toClientMetrics().getMetrics());
  }

  private class MetricUploaderThread extends Thread {

    private static final String THREAD_NAME = "fds-java-sdk-metrics-uploader";

    private final long timeoutNano = TimeUnit.MINUTES.toNanos(1);

    private final BlockingQueue<MetricData> queue;

    public MetricUploaderThread(BlockingQueue<MetricData> queue) {
      super(THREAD_NAME);
      this.queue = queue;
    }

    @Override
    public void run() {
      while (true) {
        try {
          ClientMetrics clientMetrics = nextUploadUnits();
          fdsClient.putClientMetrics(clientMetrics);
          LOG.info("Pushed " + clientMetrics.getMetrics().size() + " client metrics.");
        } catch (Exception ex) {
          LOG.warn("Unexpected exception, ignored, ", ex);
        }
      }
    }

    private ClientMetrics nextUploadUnits() throws InterruptedException {
      ClientMetrics clientMetrics = new ClientMetrics();
      long startNano = System.nanoTime();
      while (true) {
        long elapsedNano = System.nanoTime() - startNano;
        if (elapsedNano > timeoutNano) {
          return clientMetrics;
        }
        MetricData metricData = queue.poll(timeoutNano - elapsedNano,
            TimeUnit.NANOSECONDS);
        if (metricData == null) {
          // time out
          return clientMetrics;
        }
        clientMetrics.add(metricData);
      }
    }
  }
}
