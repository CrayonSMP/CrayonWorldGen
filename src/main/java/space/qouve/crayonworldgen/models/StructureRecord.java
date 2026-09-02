package space.qouve.crayonworldgen.models;

import java.util.UUID;

/**
 * Represents persistent metadata for a generated structure instance.
 */
public record StructureRecord(
        UUID id,
        String key,
        long timestamp,
        int x,
        int y,
        int z
) {
    private static final String DELIMITER = ";";

    /**
     * Serializes record fields to a delimited string format.
     */
    public String serialize() {
        return id + DELIMITER + key + DELIMITER + timestamp + DELIMITER + x + DELIMITER + y + DELIMITER + z;
    }

    /**
     * Parses a record instance from a serialized string format.
     */
    public static StructureRecord deserialize(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        String[] parts = data.split(DELIMITER);
        if (parts.length < 6) {
            return null;
        }

        try {
            return new StructureRecord(
                    UUID.fromString(parts[0]),
                    parts[1],
                    Long.parseLong(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5])
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
