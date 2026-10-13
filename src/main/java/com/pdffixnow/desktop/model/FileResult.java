package com.pdffixnow.desktop.model;

import java.nio.file.Path;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Observable row model for the results table. Only touch from the JavaFX thread. */
public class FileResult {
    public static final String PENDING = "Pending";
    public static final String RUNNING = "Running";
    public static final String VALID = "Valid";
    public static final String INVALID = "Invalid";
    public static final String ERROR = "Error";
    public static final String CANCELLED = "Cancelled";

    private final Path path;
    private final StringProperty status = new SimpleStringProperty(PENDING);
    private final StringProperty profile = new SimpleStringProperty("-");
    private final StringProperty message = new SimpleStringProperty("");
    private final IntegerProperty errors = new SimpleIntegerProperty(0);
    private final StringProperty time = new SimpleStringProperty("");
    private final ObjectProperty<List<RuleFailure>> failures = new SimpleObjectProperty<>(List.of());

    public FileResult(Path path) {
        this.path = path;
    }

    public void reset() {
        status.set(PENDING);
        profile.set("-");
        message.set("");
        errors.set(0);
        time.set("");
        failures.set(List.of());
    }

    public void apply(Outcome o) {
        profile.set(o.detectedProfile());
        message.set(o.message());
        errors.set(o.errorCount());
        time.set(o.millis() > 0 ? String.format("%.1f s", o.millis() / 1000.0) : "");
        failures.set(o.failures());
        status.set(o.status());
    }

    public Path getPath() { return path; }
    public String getFileName() { return path.getFileName().toString(); }

    public StringProperty statusProperty() { return status; }
    public StringProperty profileProperty() { return profile; }
    public StringProperty messageProperty() { return message; }
    public IntegerProperty errorsProperty() { return errors; }
    public StringProperty timeProperty() { return time; }
    public ObjectProperty<List<RuleFailure>> failuresProperty() { return failures; }

    public String getStatus() { return status.get(); }
    public void setStatus(String s) { status.set(s); }
    public String getProfile() { return profile.get(); }
    public String getMessage() { return message.get(); }
    public int getErrors() { return errors.get(); }
    public String getTime() { return time.get(); }
    public List<RuleFailure> getFailures() { return failures.get(); }
}
