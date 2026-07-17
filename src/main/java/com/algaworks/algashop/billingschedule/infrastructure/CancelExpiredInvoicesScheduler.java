package com.algaworks.algashop.billingschedule.infrastructure;

import com.algaworks.algashop.billingschedule.application.CancelExpireInvoicesApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CancelExpiredInvoicesScheduler {

    private final CancelExpireInvoicesApplicationService applicationService;

    @Scheduled(fixedRate = 5000)
    public void runTaks() {
        log.info("tasks started - cancelling expired invoices.");
        applicationService.cancelExpiredInvoices();

        log.info("tasks ended - expired invoices.");
    }
}
