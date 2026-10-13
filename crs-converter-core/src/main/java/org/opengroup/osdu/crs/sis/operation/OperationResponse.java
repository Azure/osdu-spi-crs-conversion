package org.opengroup.osdu.crs.sis.operation;

import java.util.List;

public class OperationResponse {

    private final List<String> operationsApplied;
    private final int successCount;

    public OperationResponse(List<String> operationsApplied, int successCount) {
        this.operationsApplied = operationsApplied;
        this.successCount = successCount;
    }

    public List<String> getOperationsApplied() {
        return operationsApplied;
    }

    public int getSuccessCount() {
        return successCount;
    }
}
