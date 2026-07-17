package com.algaworks.algashop.billingschedule.infrastructure;


import com.algaworks.algashop.billingschedule.application.CancelExpireInvoicesApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelExpireInvoicesApplicationServiceJdbcImpl implements CancelExpireInvoicesApplicationService {

    private final JdbcClient jdbcClient;

    private static final Duration EXPIRED_SINCE = Duration.ofDays(1);
    private static final String CANCELED_STATUS = "CANCELED";
    private static final String UNPAID_STATUS = "UNPAID";
    private static final String CANCEL_REASON = "Invoice expired";

    private static final String SELECT_EXPIRED_INVOICES_SQL = """
            select id from invoice i
            where i.expires_at  <= now() - interval '%d days'
              and i.status  = :status
            """.formatted(EXPIRED_SINCE.toDays());

    private static final String UPDATE_INVOICE_STATUS_SQL = """
            update invoice 
                set status = :status, 
                    canceled_at = now(), 
                    cancel_reason = :reason
             where id = :id
            """;

    @Override
    public void cancelExpiredInvoices() {
        var invoiceIds = fetchExpiredInvoices();
        log.info("tasks - total invoices fetched: {}", invoiceIds.size());

        var total = cancelInvoice(invoiceIds);
        log.info("tasks - total invoices canceled: {}", total);
    }

    private List<UUID> fetchExpiredInvoices() {
        return jdbcClient.sql(SELECT_EXPIRED_INVOICES_SQL)
                .param("status", UNPAID_STATUS)
                .query(UUID.class)
                .list();
    }

    public int cancelInvoice(List<UUID> invoicesIds) {
        AtomicInteger updatedInvoices = new AtomicInteger();
        invoicesIds.forEach(id -> {
            try {
                updatedInvoices.addAndGet(jdbcClient.sql(UPDATE_INVOICE_STATUS_SQL)
                        .param("status", CANCELED_STATUS)
                        .param("reason", CANCEL_REASON)
                        .param("id", id)
                        .update());

                log.info("task - invoice canceled ID {}", id);
            } catch (Exception e) {
                log.error("tasl = failed to cancel invoice with id {}", e.getMessage());
            }
        });

        return updatedInvoices.get();
    }
}
