package org.opengroup.osdu.crs.osducoreserviceclient.storage;

import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;

import java.util.Collection;

public interface IStorageClient {
    Record getRecord(String record);

    MultiRecordInfo getRecords(Collection<String> records);
}
