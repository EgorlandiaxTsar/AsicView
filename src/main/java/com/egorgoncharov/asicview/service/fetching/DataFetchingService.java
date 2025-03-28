package com.egorgoncharov.asicview.service.fetching;

import com.egorgoncharov.asicview.appdata.AppDataGlobalManager;
import com.egorgoncharov.asicview.appdata.xml.Asic;
import com.egorgoncharov.asicview.service.parsing.DataParsingService;
import com.egorgoncharov.asicview.service.parsing.PageDownloader;
import com.egorgoncharov.asicview.service.parsing.Response;
import com.egorgoncharov.asicview.service.parsing.exception.MachineDataParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataFetchingService extends Thread {
    private static final Logger logger = LogManager.getLogger(DataFetchingService.class);
    private static final List<Result> fetchedAsics = new CopyOnWriteArrayList<>();
    private static final Map<String, DataFetchingServiceWorker> workers = new TreeMap<>();
    private static boolean running = true;

    public static Result instantFetch(String asicIp, String username, String password, int preferredRequestTime) {
        logger.info("Instant data fetch required (target IP: '" + asicIp + "')");
        Response res = new PageDownloader(username, password).getDocument(asicIp, preferredRequestTime);
        if (!res.isSuccessful()) {
            return new Result(null, res);
        } else {
            try {
                return new Result(DataParsingService.fromDoc(res.getContent()).parse(asicIp), res);
            } catch (MachineDataParseException e) {
                logger.error("Parsing exception has been thrown, (target IP: '" + asicIp + "')", e);
                return new Result(null, res);
            }
        }
    }

    public static void updateWorkerRefreshInterval(String asicIp, int newRefreshInterval) {
        logger.info("Updating 'DataFetchingServiceWorker' object (target IP: '" + asicIp + "')");
        if (workers.containsKey(asicIp)) {
            DataFetchingServiceWorker worker = workers.get(asicIp);
            worker.terminate();
            DataFetchingServiceWorker newWorker = new DataFetchingServiceWorker(worker.getAsic(), newRefreshInterval);
            workers.replace(asicIp, newWorker);
            newWorker.start();
            logger.info("'DataFetchingServiceWorker' object updated (target IP: '" + asicIp + "')");
        }
    }

    @Override
    public void run() {
        logger.info("Starting 'DataFetchingService' service");
        List<DataFetchingServiceWorker> newWorkersQueue = new ArrayList<>();
        List<String> workersToDeleteQueue = new ArrayList<>();
        List<Asic> asics = new ArrayList<>();
        logger.info("'DataFetchingService' started");
        while (running) {
            logger.info("Workers inspection started");
            asics.addAll(AppDataGlobalManager.getAppData().getAsics());
            workers.forEach((ip, worker) -> {
                Asic relatedAsic = null;
                for (Asic asic : asics) {
                    if (asic.getIp().equals(ip)) {
                        relatedAsic = asic;
                        break;
                    }
                }
                if (relatedAsic == null) {
                    worker.terminate();
                    workersToDeleteQueue.add(ip);
                }
            });
            asics.forEach(asic -> {
                if (!workers.containsKey(asic.getIp())) {
                    DataFetchingServiceWorker newWorker = new DataFetchingServiceWorker(asic, asic.getRefreshInterval());
                    newWorkersQueue.add(newWorker);
                }
            });
            workersToDeleteQueue.forEach(workers::remove);
            newWorkersQueue.forEach(worker -> {
                workers.put(worker.getAsic().getIp(), worker);
                worker.start();
            });
            logger.info("Workers inspection ended, deleted " + workersToDeleteQueue.size() + " workers, added " + newWorkersQueue.size() + " workers");
            newWorkersQueue.clear();
            workersToDeleteQueue.clear();
            asics.clear();
            fetchedAsics.clear();
            logger.info("Updating workers results");
            workers.forEach((ip, worker) -> {
                if (worker.getFetchingResult() != null) {
                    fetchedAsics.add(worker.getFetchingResult());
                }
            });
            logger.info("Updated workers results, current size: " + fetchedAsics.size() + " results");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Interrupted exception has been thrown", e);
            }
        }
        logger.info("'DataFetchingService' terminated");
        interrupt();
    }

    public static List<Result> getFetchedAsics() {
        return fetchedAsics;
    }

    public static void shutdown() {
        logger.info("Safe 'DataFetchingService' termination was required");
        running = false;
        workers.forEach((ip, worker) -> worker.terminate());
        workers.clear();
    }
}
