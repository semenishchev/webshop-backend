package cc.olek.webshop.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.*;

@ApplicationScoped
public class TransactionHelper {

    @Inject
    TransactionManager transactionManager;

    public void registerRollbackAction(Runnable action) {
        try {
            Transaction transaction = transactionManager.getTransaction();
            if (transaction == null) return;
            transaction.registerSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {}

                @Override
                public void afterCompletion(int status) {
                    if (status != jakarta.transaction.Status.STATUS_ROLLEDBACK) return;
                    action.run();
                }
            });
        } catch (SystemException | RollbackException e) {
            throw new RuntimeException("Failed to register rollback action", e);
        }
    }
}
