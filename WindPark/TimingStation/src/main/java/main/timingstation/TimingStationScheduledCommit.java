package main.timingstation;

import lombok.extern.slf4j.Slf4j;
import main.model.TimingstationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimingStationScheduledCommit {
    @Autowired
    TimingstationService timingstationService;

    @Scheduled(fixedRate = 30000)
    public void commitData() {
        TimingstationData data = timingstationService.getTimingstationData();
        log.info("Committing Data: " + data.getTimingstationID());
        timingstationService.commitData(data);
    }
}
