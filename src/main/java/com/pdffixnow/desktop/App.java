package com.pdffixnow.desktop;

import com.pdffixnow.desktop.model.FileResult;
import com.pdffixnow.desktop.model.Outcome;
import com.pdffixnow.desktop.model.ProfileOption;
import com.pdffixnow.desktop.model.RuleFailure;
import com.pdffixnow.desktop.service.ReportService;
import com.pdffixnow.desktop.service.UpdateService;
import com.pdffixnow.desktop.service.ValidationService;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {

    public static final String VERSION = "1.0.0";
    public static final String WEBSITE = "https://www.pdffixnow.com";

    private final ValidationService validationService = new ValidationService();
    private final ReportService reportService = new ReportService();
    private final Preferences prefs = Preferences.userNodeForPackage(App.class);

    private final ObservableList<FileResult> results = FXCollections.observableArrayList();
    private final ObservableList<RuleFailure> failureRows = FXCollections.observableArrayList();

    private Stage stage;
    private TableView<FileResult> table;
    private TableView<RuleFailure> detailTable;
    private Label detailHeader;
    private Label summaryLabel;
    private Label hintLabel;
    private ProgressBar progressBar;
    private ComboBox<ProfileOption> profileBox;
    private Spinner<Integer> threadSpinner;
    private Button addFilesBtn, addFolderBtn, clearBtn, validateBtn, cancelBtn, exportBtn;
    private HBox updateBox;

    private volatile boolean running = false;
    private AtomicBoolean cancelFlag = new AtomicBoolean(false);
    private ExecutorService pool;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1100, 720);
        var css = App.class.getResource("style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        scene.setOnDragOver(this::onDragOver);
        scene.setOnDragDropped(this::onDragDropped);

        stage.setTitle("PDFFixNow - PDF/A Validator");
        var icon = App.class.getResourceAsStream("icon.png");
        if (icon != null) stage.getIcons().add(new javafx.scene.image.Image(icon));
        stage.setMinWidth(860);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            savePrefs();
            cancelFlag.set(true);
            if (pool != null) pool.shutdownNow();
            Platform.exit();
            System.exit(0);
        });
        stage.show();

        updateButtons();
        checkForUpdatesInBackground();
    }

    // ------------------------------------------------------------------ UI building

    private Region buildHeader() {
        // Logo image in header
        ImageView logoView = null;
        var logoStream = App.class.getResourceAsStream("icon.png");
        if (logoStream != null) {
            Image logoImg = new Image(logoStream, 40, 40, true, true);
            logoView = new ImageView(logoImg);
            logoView.getStyleClass().add("header-logo");
        }

        Label title = new Label("PDFFixNow");
        title.getStyleClass().add("app-title");
        Label sub = new Label("PDF/A validator - works offline, your files never leave this PC");
        sub.getStyleClass().add("app-subtitle");
        VBox titles = new VBox(2, title, sub);

        HBox brand = logoView != null
                ? new HBox(10, logoView, titles)
                : new HBox(titles);
        brand.setAlignment(Pos.CENTER_LEFT);

        Button about = new Button("About");
        about.getStyleClass().add("ghost-button");
        about.setOnAction(e -> showAbout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(12, brand, spacer, about);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getStyleClass().add("header");

        addFilesBtn = new Button("Add files...");
        addFolderBtn = new Button("Add folder...");
        clearBtn = new Button("Clear list");
        addFilesBtn.setOnAction(e -> chooseFiles());
        addFolderBtn.setOnAction(e -> chooseFolder());
        clearBtn.setOnAction(e -> clearList());

        profileBox = new ComboBox<>(FXCollections.observableArrayList(ProfileOption.ALL));
        int savedIdx = Math.max(0, Math.min(prefs.getInt("profileIndex", 0), ProfileOption.ALL.size() - 1));
        profileBox.getSelectionModel().select(savedIdx);
        Tooltip.install(profileBox, new Tooltip("Choose which PDF/A level to validate against"));

        int cores = Runtime.getRuntime().availableProcessors();
        int defThreads = Math.max(1, Math.min(prefs.getInt("threads", Math.min(2, cores)), 8));
        threadSpinner = new Spinner<>(1, Math.max(2, Math.min(8, cores)), defThreads);
        threadSpinner.setPrefWidth(70);
        Tooltip.install(threadSpinner, new Tooltip("Files validated at the same time"));

        validateBtn = new Button("Validate");
        validateBtn.getStyleClass().add("primary-button");
        validateBtn.setOnAction(e -> startValidation());
        cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> cancelFlag.set(true));
        exportBtn = new Button("Export report...");
        exportBtn.setOnAction(e -> exportReport());

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox bar = new HBox(8, addFilesBtn, addFolderBtn, clearBtn,
                new Label("Validate against:"), profileBox,
                new Label("Threads:"), threadSpinner,
                spacer2, exportBtn, cancelBtn, validateBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("toolbar");

        return new VBox(top, bar);
    }

    private Region buildCenter() {
        table = new TableView<>(results);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        hintLabel = new Label("Drag and drop PDF files or folders here\nor use \"Add files...\"");
        hintLabel.getStyleClass().add("hint");
        table.setPlaceholder(hintLabel);

        TableColumn<FileResult, String> nameCol = new TableColumn<>("File");
        nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFileName()));
        nameCol.setPrefWidth(300);

        TableColumn<FileResult, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("st-valid", "st-invalid", "st-error", "st-running");
                if (empty || item == null) { setText(null); return; }
                setText(item);
                switch (item) {
                    case FileResult.VALID -> getStyleClass().add("st-valid");
                    case FileResult.INVALID -> getStyleClass().add("st-invalid");
                    case FileResult.ERROR -> getStyleClass().add("st-error");
                    case FileResult.RUNNING -> getStyleClass().add("st-running");
                    default -> { }
                }
            }
        });

        TableColumn<FileResult, String> profileCol = new TableColumn<>("Profile");
        profileCol.setCellValueFactory(c -> c.getValue().profileProperty());
        profileCol.setPrefWidth(110);

        TableColumn<FileResult, Number> errCol = new TableColumn<>("Errors");
        errCol.setCellValueFactory(c -> c.getValue().errorsProperty());
        errCol.setPrefWidth(70);

        TableColumn<FileResult, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(c -> c.getValue().timeProperty());
        timeCol.setPrefWidth(70);

        TableColumn<FileResult, String> msgCol = new TableColumn<>("Result");
        msgCol.setCellValueFactory(c -> c.getValue().messageProperty());
        msgCol.setPrefWidth(400);

        table.getColumns().addAll(nameCol, statusCol, profileCol, errCol, timeCol, msgCol);

        MenuItem remove = new MenuItem("Remove from list");
        remove.setOnAction(e -> {
            if (running) return;
            results.removeAll(new ArrayList<>(table.getSelectionModel().getSelectedItems()));
            updateButtons();
        });
        MenuItem openFolder = new MenuItem("Show in folder");
        openFolder.setOnAction(e -> {
            FileResult sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) openInExplorer(sel.getPath());
        });
        table.setContextMenu(new ContextMenu(openFolder, remove));

        // Detail panel
        detailHeader = new Label("Select a file to see the failed PDF/A rules");
        detailHeader.getStyleClass().add("detail-header");

        detailTable = new TableView<>(failureRows);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        detailTable.setPlaceholder(new Label("No failed rules to show"));
        TableColumn<RuleFailure, String> clauseCol = new TableColumn<>("Clause");
        clauseCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().clause()));
        clauseCol.setPrefWidth(90);
        TableColumn<RuleFailure, String> testCol = new TableColumn<>("Test");
        testCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().testNumber()));
        testCol.setPrefWidth(60);
        TableColumn<RuleFailure, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().message()));
        descCol.setPrefWidth(520);
        TableColumn<RuleFailure, String> ctxCol = new TableColumn<>("Location in PDF");
        ctxCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().context()));
        ctxCol.setPrefWidth(300);
        detailTable.getColumns().addAll(clauseCol, testCol, descCol, ctxCol);
        VBox.setVgrow(detailTable, Priority.ALWAYS);
        VBox detail = new VBox(6, detailHeader, detailTable);
        detail.setPadding(new Insets(8, 0, 0, 0));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> showDetail(sel));

        SplitPane split = new SplitPane(table, detail);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.58);
        BorderPane.setMargin(split, new Insets(0, 12, 0, 12));
        return split;
    }

    private Region buildStatusBar() {
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(220);
        summaryLabel = new Label("Ready");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        updateBox = new HBox(6);
        updateBox.setAlignment(Pos.CENTER_RIGHT);
        HBox bar = new HBox(12, progressBar, summaryLabel, spacer, updateBox);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    // ------------------------------------------------------------------ adding files

    private void chooseFiles() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select PDF files");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf", "*.PDF"));
        String last = prefs.get("lastDir", null);
        if (last != null && new File(last).isDirectory()) fc.setInitialDirectory(new File(last));
        List<File> files = fc.showOpenMultipleDialog(stage);
        if (files != null && !files.isEmpty()) {
            prefs.put("lastDir", files.get(0).getParent());
            addPaths(files.stream().map(File::toPath).toList());
        }
    }

    private void chooseFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select a folder containing PDF files");
        String last = prefs.get("lastDir", null);
        if (last != null && new File(last).isDirectory()) dc.setInitialDirectory(new File(last));
        File dir = dc.showDialog(stage);
        if (dir != null) {
            prefs.put("lastDir", dir.getAbsolutePath());
            addPaths(List.of(dir.toPath()));
        }
    }

    private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles() && !running) e.acceptTransferModes(TransferMode.COPY);
        e.consume();
    }

    private void onDragDropped(DragEvent e) {
        boolean ok = false;
        if (e.getDragboard().hasFiles() && !running) {
            addPaths(e.getDragboard().getFiles().stream().map(File::toPath).toList());
            ok = true;
        }
        e.setDropCompleted(ok);
        e.consume();
    }

    /** Adds PDFs; folders are scanned recursively. Directory scanning happens off the UI thread. */
    private void addPaths(List<Path> inputs) {
        if (running) return;
        Thread t = new Thread(() -> {
            List<Path> found = new ArrayList<>();
            for (Path p : inputs) {
                try {
                    if (Files.isDirectory(p)) {
                        try (Stream<Path> walk = Files.walk(p)) {
                            walk.filter(Files::isRegularFile).filter(App::isPdf).forEach(found::add);
                        }
                    } else if (isPdf(p)) {
                        found.add(p);
                    }
                } catch (Exception ignored) {
                    // unreadable folder: skip
                }
            }
            Platform.runLater(() -> {
                Set<Path> existing = new HashSet<>();
                results.forEach(r -> existing.add(r.getPath().toAbsolutePath().normalize()));
                int added = 0;
                for (Path p : found) {
                    if (existing.add(p.toAbsolutePath().normalize())) {
                        results.add(new FileResult(p));
                        added++;
                    }
                }
                summaryLabel.setText(added + " file(s) added, " + results.size() + " in list");
                updateButtons();
            });
        }, "scan-files");
        t.setDaemon(true);
        t.start();
    }

    private static boolean isPdf(Path p) {
        String n = p.getFileName().toString().toLowerCase();
        return n.endsWith(".pdf");
    }

    private void clearList() {
        if (running) return;
        results.clear();
        failureRows.clear();
        detailHeader.setText("Select a file to see the failed PDF/A rules");
        progressBar.setProgress(0);
        summaryLabel.setText("Ready");
        updateButtons();
    }

    // ------------------------------------------------------------------ validation

    private void startValidation() {
        if (running || results.isEmpty()) return;
        running = true;
        cancelFlag = new AtomicBoolean(false);
        final AtomicBoolean cancel = cancelFlag;

        String profile = profileBox.getValue().id();
        int threads = threadSpinner.getValue();
        List<FileResult> batch = new ArrayList<>(results);
        batch.forEach(FileResult::reset);
        failureRows.clear();
        progressBar.setProgress(0);
        summaryLabel.setText("Validating 0 / " + batch.size() + " ...");
        updateButtons();

        final int total = batch.size();
        final AtomicInteger done = new AtomicInteger();
        pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "validator");
            t.setDaemon(true);
            return t;
        });

        for (FileResult fr : batch) {
            pool.submit(() -> {
                Outcome outcome;
                if (cancel.get()) {
                    outcome = Outcome.cancelled();
                } else {
                    Platform.runLater(() -> fr.setStatus(FileResult.RUNNING));
                    outcome = validationService.validate(fr.getPath(), profile);
                }
                Platform.runLater(() -> {
                    fr.apply(outcome);
                    int n = done.incrementAndGet();
                    progressBar.setProgress((double) n / total);
                    summaryLabel.setText("Validating " + n + " / " + total + " ...");
                    if (table.getSelectionModel().getSelectedItem() == fr) showDetail(fr);
                    if (n == total) finishBatch(cancel.get());
                });
            });
        }
        pool.shutdown(); // no new tasks; existing ones still run
    }

    private void finishBatch(boolean wasCancelled) {
        running = false;
        long valid = results.stream().filter(r -> FileResult.VALID.equals(r.getStatus())).count();
        long invalid = results.stream().filter(r -> FileResult.INVALID.equals(r.getStatus())).count();
        long errors = results.stream().filter(r -> FileResult.ERROR.equals(r.getStatus())).count();
        summaryLabel.setText((wasCancelled ? "Cancelled. " : "Done. ")
                + valid + " valid, " + invalid + " invalid, " + errors + " error(s)");
        updateButtons();
    }

    private void showDetail(FileResult r) {
        failureRows.clear();
        if (r == null) {
            detailHeader.setText("Select a file to see the failed PDF/A rules");
            return;
        }
        detailHeader.setText(r.getFileName() + " - " + (r.getMessage().isEmpty() ? r.getStatus() : r.getMessage()));
        failureRows.addAll(r.getFailures());
    }

    // ------------------------------------------------------------------ export, misc

    private void exportReport() {
        if (results.isEmpty()) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Export report");
        fc.setInitialFileName("PDFFixNow-report");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML report (*.html)", "*.html"),
                new FileChooser.ExtensionFilter("CSV spreadsheet (*.csv)", "*.csv"));
        File f = fc.showSaveDialog(stage);
        if (f == null) return;
        try {
            Path p = f.toPath();
            if (f.getName().toLowerCase().endsWith(".csv")) {
                reportService.writeCsv(p, new ArrayList<>(results));
            } else {
                if (!f.getName().toLowerCase().endsWith(".html")) p = Path.of(f.getPath() + ".html");
                reportService.writeHtml(p, new ArrayList<>(results));
            }
            summaryLabel.setText("Report saved: " + p.getFileName());
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(p.toFile());
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Could not save report", ex.getMessage());
        }
    }

    private void openInExplorer(Path p) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("explorer.exe", "/select,", p.toAbsolutePath().toString()).start();
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(p.getParent().toFile());
            }
        } catch (Exception ignored) {
            // nothing sensible to do
        }
    }

    private void showAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("About PDFFixNow");
        a.setHeaderText("PDFFixNow " + VERSION);
        a.setContentText("""
                Offline PDF/A validator for Windows.
                Your files are checked on this computer and are never uploaded.

                PDF/A validation is powered by veraPDF (https://verapdf.org),
                used under the Mozilla Public License 2.0.

                Website: %s
                """.formatted(WEBSITE));
        a.initOwner(stage);
        // Inherit the app icon on the About dialog
        var iconStream = App.class.getResourceAsStream("icon.png");
        if (iconStream != null) {
            a.getDialogPane().getScene().getWindow()
             .getClass(); // ensure window exists before icon set
            javafx.stage.Stage dlgStage =
                    (javafx.stage.Stage) a.getDialogPane().getScene().getWindow();
            dlgStage.getIcons().add(new Image(iconStream));
        }
        a.showAndWait();
    }

    private void alert(Alert.AlertType type, String header, String text) {
        Alert a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(text == null ? "" : text);
        a.initOwner(stage);
        a.showAndWait();
    }

    private void updateButtons() {
        boolean empty = results.isEmpty();
        addFilesBtn.setDisable(running);
        addFolderBtn.setDisable(running);
        clearBtn.setDisable(running || empty);
        profileBox.setDisable(running);
        threadSpinner.setDisable(running);
        validateBtn.setDisable(running || empty);
        cancelBtn.setDisable(!running);
        exportBtn.setDisable(running || empty);
    }

    private void savePrefs() {
        prefs.putInt("profileIndex", profileBox.getSelectionModel().getSelectedIndex());
        prefs.putInt("threads", threadSpinner.getValue());
    }

    private void checkForUpdatesInBackground() {
        Thread t = new Thread(() -> new UpdateService().check(VERSION).ifPresent(info ->
                Platform.runLater(() -> {
                    Hyperlink link = new Hyperlink("Version " + info.version() + " is available - download");
                    link.setOnAction(e -> getHostServices().showDocument(info.url()));
                    updateBox.getChildren().setAll(link);
                })), "update-check");
        t.setDaemon(true);
        t.start();
    }
}
