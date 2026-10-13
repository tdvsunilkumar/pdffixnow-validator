package com.pdffixnow.desktop.model;

import java.util.List;

/** An entry in the "validate against" drop-down. id == null means auto-detect. */
public record ProfileOption(String id, String label) {

    public static final List<ProfileOption> ALL = List.of(
            new ProfileOption(null, "Auto-detect"),
            new ProfileOption("1a", "PDF/A-1a"),
            new ProfileOption("1b", "PDF/A-1b"),
            new ProfileOption("2a", "PDF/A-2a"),
            new ProfileOption("2b", "PDF/A-2b"),
            new ProfileOption("2u", "PDF/A-2u"),
            new ProfileOption("3a", "PDF/A-3a"),
            new ProfileOption("3b", "PDF/A-3b"),
            new ProfileOption("3u", "PDF/A-3u"),
            new ProfileOption("4", "PDF/A-4"),
            new ProfileOption("4e", "PDF/A-4e"),
            new ProfileOption("4f", "PDF/A-4f"));

    @Override
    public String toString() {
        return label;
    }
}
