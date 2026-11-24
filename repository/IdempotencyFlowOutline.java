package repository;

import api.ApiResponse;

import java.util.HashMap;
import java.util.Map;

public class IdempotencyFlowOutline {

    /** What the Map stores for one key. */
    static class Record {
        final String fingerprint;
        final ApiResponse response;

        Record(String fingerprint, ApiResponse response) {
            this.fingerprint = fingerprint;
            this.response = response;
        }
    }

    // key -> record. Students: this lives inside the IdempotencyStore singleton.
    private final Map<String, Record> records = new HashMap<>();

    Record find(String key) {
        
        throw new UnsupportedOperationException("not implemented yet");
    }

    void save(String key, String fingerprint, ApiResponse response) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
