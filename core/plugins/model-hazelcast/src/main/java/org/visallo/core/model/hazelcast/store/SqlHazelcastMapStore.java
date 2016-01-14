package org.visallo.core.model.hazelcast.store;

import com.google.inject.Inject;
import com.hazelcast.core.MapStore;
import org.skife.jdbi.v2.Update;
import org.visallo.core.exception.VisalloException;
import org.visallo.core.model.hazelcast.HazelcastConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.util.Collection;
import java.util.Map;

public class SqlHazelcastMapStore extends SqlHazelcastStoreBase<String, Object> implements MapStore<String, Object> {
    @Inject
    public SqlHazelcastMapStore(String name, HazelcastConfiguration hazelcastConfiguration) {
        super(name, StoreType.MAP, hazelcastConfiguration);
    }

    @Override
    protected Object getDataFromQueryRow(Map<String, Object> row) {
        byte[] bytes;
        try {
            Object columnValue = row.get(HazelcastConfiguration.SQL_DATA_COLUMN);
            if (columnValue instanceof Blob) {
                Blob blob = (Blob) columnValue;
                bytes = blob.getBytes(0, (int) blob.length());
            } else if (columnValue instanceof byte[]) {
                bytes = (byte[]) columnValue;
            } else {
                throw new VisalloException("Unsupported result object: " + columnValue);
            }
        } catch (Throwable ex) {
            throw new VisalloException("Could not get data from column", ex);
        }
        return deserializeValue(bytes);
    }

    @Override
    protected void setDataInPreparedStatement(Update stmt, Object value) {
        stmt.bind(HazelcastConfiguration.SQL_DATA_COLUMN, serializeValue(value));
    }

    private Object deserializeValue(byte[] bytes) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return in.readObject();
        } catch (Exception ex) {
            throw new VisalloException("Could not deserialize value", ex);
        }
    }

    private byte[] serializeValue(Object value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(value);
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new VisalloException("Could not serialize value", ex);
        }
    }

    @Override
    public void store(String key, Object value) {
        super.store(key, value);
    }

    @Override
    public void storeAll(Map<String, Object> map) {
        super.storeAll(map);
    }

    @Override
    public void delete(String key) {
        super.delete(key);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        super.deleteAll(keys);
    }

    @Override
    public Object load(String key) {
        return super.load(key);
    }

    @Override
    public Map<String, Object> loadAll(Collection<String> keys) {
        return super.loadAll(keys);
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return super.loadAllKeysIterable();
    }
}