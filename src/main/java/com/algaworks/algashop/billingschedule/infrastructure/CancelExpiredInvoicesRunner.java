package com.algaworks.algashop.billingschedule.infrastructure;

import com.algaworks.algashop.billingschedule.application.CancelExpireInvoicesApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CancelExpiredInvoicesRunner implements ApplicationRunner {

    private final CancelExpireInvoicesApplicationService applicationService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("tasks started - cancelling expired invoices.");
        applicationService.cancelExpiredInvoices();

        log.info("tasks ended - expired invoices.");
    }
}
