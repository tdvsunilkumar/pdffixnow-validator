package com.pdffixnow.desktop.service;

import com.pdffixnow.desktop.App;
import com.pdffixnow.desktop.model.FileResult;
import com.pdffixnow.desktop.model.RuleFailure;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Writes CSV and HTML reports. */
public class ReportService {

    public void writeCsv(Path target, List<FileResult> results) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            w.write('\uFEFF'); // BOM so Excel opens UTF-8 correctly
            w.write("File,Folder,Status,Profile,Errors,Message\r\n");
            for (FileResult r : results) {
                w.write(csv(r.getFileName()) + "," + csv(String.valueOf(r.getPath().getParent())) + ","
                        + csv(r.getStatus()) + "," + csv(r.getProfile()) + "," + r.getErrors() + ","
                        + csv(r.getMessage()) + "\r\n");
            }
        }
    }

    public void writeHtml(Path target, List<FileResult> results) throws IOException {
        long valid = results.stream().filter(r -> FileResult.VALID.equals(r.getStatus())).count();
        long invalid = results.stream().filter(r -> FileResult.INVALID.equals(r.getStatus())).count();
        long errors = results.stream().filter(r -> FileResult.ERROR.equals(r.getStatus())).count();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>PDFFixNow Report</title><style>")
          .append("body{font-family:Segoe UI,Arial,sans-serif;margin:32px;color:#1f2937}")
          .append("h1{margin-bottom:0}.sub{color:#6b7280;margin-top:4px}")
          .append("table{border-collapse:collapse;width:100%;margin:16px 0}")
          .append("th,td{border:1px solid #e5e7eb;padding:8px 10px;text-align:left;font-size:14px;vertical-align:top}")
          .append("th{background:#f3f4f6}.Valid{color:#15803d;font-weight:600}")
          .append(".Invalid{color:#b91c1c;font-weight:600}.Error{color:#b45309;font-weight:600}")
          .append("details{margin:6px 0}summary{cursor:pointer}</style></head><body>")
          .append("<h1>PDFFixNow - PDF/A Validation Report</h1>")
          .append("<p class=\"sub\">Generated ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
          .append(" &middot; PDFFixNow ").append(App.VERSION).append(" &middot; powered by veraPDF</p>")
          .append("<p><b>").append(results.size()).append("</b> file(s): ")
          .append(valid).append(" valid, ").append(invalid).append(" invalid, ")
          .append(errors).append(" could not be processed.</p>")
          .append("<table><tr><th>File</th><th>Status</th><th>Profile</th><th>Errors</th><th>Details</th></tr>");

        for (FileResult r : results) {
            sb.append("<tr><td>").append(esc(r.getFileName())).append("<br><small>")
              .append(esc(String.valueOf(r.getPath().getParent()))).append("</small></td>")
              .append("<td class=\"").append(esc(r.getStatus())).append("\">").append(esc(r.getStatus())).append("</td>")
              .append("<td>").append(esc(r.getProfile())).append("</td>")
              .append("<td>").append(r.getErrors()).append("</td><td>").append(esc(r.getMessage()));
            if (!r.getFailures().isEmpty()) {
                sb.append("<details><summary>Failed rules (").append(r.getFailures().size()).append(")</summary><ul>");
                for (RuleFailure f : r.getFailures()) {
                    sb.append("<li><b>").append(esc(f.clause())).append("-").append(esc(f.testNumber()))
                      .append("</b>: ").append(esc(f.message()));
                    if (f.context() != null && !f.context().isBlank() && !"null".equals(f.context())) {
                        sb.append("<br><small>").append(esc(f.context())).append("</small>");
                    }
                    sb.append("</li>");
                }
                sb.append("</ul></details>");
            }
            sb.append("</td></tr>");
        }
        sb.append("</table></body></html>");
        Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String csv(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
