package com.pdffixnow.desktop.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optional update check. The app works fully offline; if the check fails for any
 * reason it is silently ignored.
 *
 * Expected JSON at UPDATE_URL:
 *   { "version": "1.0.1", "url": "https://www.pdffixnow.com/downloads/PDFFixNow-Setup-1.0.1.exe" }
 */
public class UpdateService {

    public static final String UPDATE_URL = "https://www.pdffixnow.com/downloads/latest.json";

    public record UpdateInfo(String version, String url) {}

    public Optional<UpdateInfo> check(String currentVersion) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(UPDATE_URL))
                    .timeout(Duration.ofSeconds(8)).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return Optional.empty();

            String version = field(resp.body(), "version");
            String url = field(resp.body(), "url");
            if (version != null && url != null && url.startsWith("https://") && newer(version, currentVersion)) {
                return Optional.of(new UpdateInfo(version, url));
            }
        } catch (Exception ignored) {
            // offline or server unavailable: not an error
        }
        return Optional.empty();
    }

    private static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static boolean newer(String candidate, String current) {
        String[] a = candidate.split("\\.");
        String[] b = current.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? num(a[i]) : 0;
            int y = i < b.length ? num(b[i]) : 0;
            if (x != y) return x > y;
        }
        return false;
    }

    private static int num(String s) {
        try { return Integer.parseInt(s.replaceAll("\\D", "")); } catch (Exception e) { return 0; }
    }
}
