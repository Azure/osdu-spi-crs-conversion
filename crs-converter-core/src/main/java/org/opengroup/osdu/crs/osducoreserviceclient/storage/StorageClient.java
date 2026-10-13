package org.opengroup.osdu.crs.osducoreserviceclient.storage;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.model.storage.StorageException;
import org.opengroup.osdu.core.common.storage.IStorageFactory;
import org.opengroup.osdu.core.common.storage.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class StorageClient implements IStorageClient{

    @Autowired
    private DpsHeaders headers;
    @Autowired
    IStorageFactory factory;

    @Override
    public Record getRecord(String record) {
        try {
            IStorageService service = this.factory.create(headers);
            Record upsertRecords = service.getRecord(record);
            return upsertRecords;
        } catch (StorageException e) {
            /*Catch block is executed when record param is bad request. Eg: fromCRS = ';:' in v3/convert API.
                Trying to keep the error generic*/
            String error = e.getHttpResponse().getBody();
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Error getting record",
                    "Please check the input type and format and try again.");
        }
    }

    @Override
    public MultiRecordInfo getRecords(Collection<String> records) {
        try {
            IStorageService service = this.factory.create(headers);
            MultiRecordInfo upsertRecords = service.getRecords(records);
            return upsertRecords;
        } catch (StorageException e) {
            /*Catch block is executed when record param is bad request. Eg: fromCRS = ';:' in v3/convert API.
                Trying to keep the error generic*/
            String error = e.getHttpResponse().getBody();
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Error getting record",
                    "Please check the input type and format and try again.");
        }
    }
}

