package com.algaworks.algashop.billingschedule.infrastructure;


import com.algaworks.algashop.billingschedule.application.CancelExpireInvoicesApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelExpireInvoicesApplicationServiceJdbcImpl implements CancelExpireInvoicesApplicationService {

    private final JdbcClient jdbcClient;
    private final JdbcOperations jdbcOperations;
    private final TransactionTemplate transactionTemplate;
    private final FastpayPaymentAPIClient fastpayPaymentAPIClient;

    private static final Duration EXPIRED_SINCE = Duration.ofDays(1);
    private static final String CANCELED_STATUS = "CANCELED";
    private static final String UNPAID_STATUS = "UNPAID";
    private static final String CANCEL_REASON = "Invoice expired";
    private static final int BATCH_LIMIT = 50;

    private static final String SELECT_EXPIRED_INVOICES_SQL = """
            select i.id, ps.gateway_code
            from invoice i 
                inner join payment_settings ps 
                    on i.payment_settings_id = ps.id
            where i.expires_at  <= now() - interval '%d days'
              and i.status  = :status
            order by i.expires_at asc
                limit :limit
                for update 
                skip locked 
            """.formatted(EXPIRED_SINCE.toDays());

    private static final String UPDATE_INVOICE_STATUS_SQL = """
            update invoice 
                set status = ?, 
                    canceled_at = now(), 
                    cancel_reason = ?
             where id = ?
            """;

    @Override
    public void cancelExpiredInvoices() {
        transactionTemplate.execute(_ -> {
            var projections = fetchExpiredInvoices();
            log.info("tasks - total invoices fetched: {}", projections.size());
            if (projections.isEmpty()) {
                log.info("task - no expired invoices found for cancellatoion");
                return true;
            }

            var total = cancelInvoice(projections);
            log.info("tasks - total invoices canceled: {}", total);
            return true;
        });

    }

    private List<InvoiceProjection> fetchExpiredInvoices() {
        return jdbcClient.sql(SELECT_EXPIRED_INVOICES_SQL)
                .param("status", UNPAID_STATUS)
                .param("limit", BATCH_LIMIT)
                .query((rs, rowNum) ->
                    new InvoiceProjection(UUID.fromString(rs.getString("id")),
                            rs.getString("gateway_code"))
                )
                .list();
    }

    public int cancelInvoice(List<InvoiceProjection> projections) {
        var cancelledInvoices = projections.stream().filter(projection ->{
            try {
                fastpayPaymentAPIClient.cancel(projection.getPaymentGatewayCode());
                log.info("task - invoice {} has the payment {} cancelled on gateway", projection.getId(), projection.getPaymentGatewayCode());
                return true;
            } catch (Exception e) {
                log.error("task - failed to cancel invoice {} payment {} on the gateway, detail {}", projection.getId(), projection.getPaymentGatewayCode()
                ,e.getMessage());
                return false;
            }
        }).toList();

        try {
            jdbcOperations.batchUpdate(UPDATE_INVOICE_STATUS_SQL,
                    cancelledInvoices,
                    cancelledInvoices.size(),
                    (ps, projection) -> {
                        ps.setString(1, CANCELED_STATUS);
                        ps.setString(2, CANCEL_REASON);
                        ps.setObject(3, projection.getId());
                    });

            log.info("task - invoice canceled IDs {}", cancelledInvoices.size());
            return cancelledInvoices.size();
        } catch (Exception e) {
            log.error("tasl = failed to cancel invoices {} details {}", projections.size(), e.getMessage());
            return 0;
        }
    }
}
