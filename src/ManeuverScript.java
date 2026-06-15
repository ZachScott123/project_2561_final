import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManeuverScript {
    private final List<Maneuver> maneuvers;

    private ManeuverScript(List<Maneuver> maneuvers) {
        this.maneuvers = Collections.unmodifiableList(new ArrayList<>(maneuvers));
    }

    public List<Maneuver> getManeuvers() {
        return maneuvers;
    }

    public static ManeuverScript load(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Maneuver> entries = new ArrayList<>();
            String line;
            int lineNumber = 0;
            boolean headerFound = false;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                if (!headerFound) {
                    headerFound = true;
                    validateHeader(trimmed, lineNumber);
                    continue;
                }

                String[] fields = trimmed.split(",", -1);
                if (fields.length != 4) {
                    throw new ScriptFormatException(
                            String.format("Script error on line %d: expected 4 fields but found %d", lineNumber, fields.length));
                }

                double seconds = parseDoubleField(fields[0].trim(), lineNumber, 1, "seconds");
                double roll = parseDoubleField(fields[1].trim(), lineNumber, 2, "roll");
                double pitch = parseDoubleField(fields[2].trim(), lineNumber, 3, "pitch");
                double yaw = parseDoubleField(fields[3].trim(), lineNumber, 4, "yaw");

                if (seconds <= 0) {
                    throw new ScriptFormatException(
                            String.format("Script error on line %d field 1 (\"seconds\"): %s must be a positive number.",
                                    lineNumber, formatNumber(seconds)));
                }
                validateRange(lineNumber, 2, "roll", roll, -180, 180);
                validateRange(lineNumber, 3, "pitch", pitch, -90, 90);
                validateRange(lineNumber, 4, "yaw", yaw, -180, 180);

                entries.add(new Maneuver(seconds, roll, pitch, yaw));
            }

            if (!headerFound) {
                throw new ScriptFormatException("Script error: missing header line \"seconds,roll,pitch,yaw\".");
            }

            return new ManeuverScript(entries);
        }
    }

    private static void validateHeader(String headerLine, int lineNumber) {
        String[] headerFields = headerLine.split(",", -1);
        if (headerFields.length != 4
                || !headerFields[0].trim().equalsIgnoreCase("seconds")
                || !headerFields[1].trim().equalsIgnoreCase("roll")
                || !headerFields[2].trim().equalsIgnoreCase("pitch")
                || !headerFields[3].trim().equalsIgnoreCase("yaw")) {
            throw new ScriptFormatException(
                    String.format("Script error on line %d: expected header \"seconds,roll,pitch,yaw\".", lineNumber));
        }
    }

    private static double parseDoubleField(String value, int lineNumber, int fieldIndex, String fieldName) {
        if (value.isEmpty()) {
            throw new ScriptFormatException(
                    String.format("Script error on line %d field %d (\"%s\"): value is missing.",
                            lineNumber, fieldIndex, fieldName));
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ScriptFormatException(
                    String.format("Script error on line %d field %d (\"%s\"): \"%s\" is not a number.",
                            lineNumber, fieldIndex, fieldName, value));
        }
    }

    private static void validateRange(int lineNumber, int fieldIndex, String fieldName,
                                      double value, double min, double max) {
        if (value < min || value > max) {
            throw new ScriptFormatException(
                    String.format("Script error on line %d field %d (\"%s\"): %s is outside allowed range [%.0f,%.0f].",
                            lineNumber, fieldIndex, fieldName, formatNumber(value), min, max));
        }
    }

    private static String formatNumber(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.format("%s", value);
    }

    public static final class Maneuver {
        private final double seconds;
        private final double roll;
        private final double pitch;
        private final double yaw;

        public Maneuver(double seconds, double roll, double pitch, double yaw) {
            this.seconds = seconds;
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public double getSeconds() {
            return seconds;
        }

        public double getRoll() {
            return roll;
        }

        public double getPitch() {
            return pitch;
        }

        public double getYaw() {
            return yaw;
        }
    }

    public static class ScriptFormatException extends RuntimeException {
        public ScriptFormatException(String message) {
            super(message);
        }
    }
}

