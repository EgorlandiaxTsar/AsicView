package com.egorgoncharov.asicview.service.fetching;

import com.egorgoncharov.asicview.appdata.xml.Asic;
import com.egorgoncharov.asicview.service.parsing.DataParsingService;
import com.egorgoncharov.asicview.service.parsing.PageDownloader;
import com.egorgoncharov.asicview.service.parsing.Response;
import com.egorgoncharov.asicview.service.parsing.exception.MachineDataParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataFetchingServiceWorker extends Thread {
    private static final Logger logger = LogManager.getLogger(DataFetchingServiceWorker.class);
    private final Asic asic;
    private int fetchingInterval;
    private Result fetchingResult = null;
    private boolean running = true;

    public DataFetchingServiceWorker(Asic asic, int fetchingInterval) {
        this.asic = asic;
        this.fetchingInterval = fetchingInterval;
    }

    @Override
    public void run() {
        this.setName("DataFetchingServiceWorker-" + asic.getIp().replaceAll("\\.", ""));
        logger.info("'" + getName() + "' started");
        while (running) {
            Response res = new PageDownloader(asic.getUsername(), asic.getPassword()).getDocument(asic.getIp(), asic.getRefreshInterval());
            if (!res.isSuccessful()) {
                fetchingResult = new Result(null, res);
            } else {
                try {
                    fetchingResult = new Result(DataParsingService.fromDoc(res.getContent()).parse(asic.getIp()), res);
                } catch (MachineDataParseException | NullPointerException e) {
                    logger.error("Exception has been thrown, (target IP: '" + asic.getIp() + "')", e);
                    fetchingResult = null;
                }
            }
            try {
                sleep(fetchingInterval);
            } catch (InterruptedException e) {
                logger.error("Interrupted exception has been thrown", e);
            }
        }
        logger.info("'DataFetchingServiceWorker' terminated");
        interrupt();
    }

    public Asic getAsic() {
        return asic;
    }

    public int getFetchingInterval() {
        return fetchingInterval;
    }

    public void setFetchingInterval(int fetchingInterval) {
        this.fetchingInterval = fetchingInterval;
    }

    public Result getFetchingResult() {
        return fetchingResult;
    }

    public void terminate() {
        logger.info("Safe 'DataFetchingServiceWorker-'" + asic.getIp().replaceAll("\\.", "") + " termination was required");
        running = false;
    }
}
