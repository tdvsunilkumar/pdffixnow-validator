package com.pdffixnow.desktop.service;

import com.pdffixnow.desktop.model.FileResult;
import com.pdffixnow.desktop.model.Outcome;
import com.pdffixnow.desktop.model.RuleFailure;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

/**
 * Wraps the veraPDF validation library. Everything runs locally; no network access.
 * Safe to call from several worker threads (each call uses its own parser/validator).
 */
public class ValidationService {

    private static final Logger LOG = Logger.getLogger(ValidationService.class.getName());
    /** Maximum number of individual failures kept per file (the error COUNT is always exact). */
    private static final int MAX_STORED_FAILURES = 1000;

    static {
        VeraGreenfieldFoundryProvider.initialise();
    }

    /**
     * @param profileId "1a", "1b", "2b" ... or null for auto-detect
     */
    public Outcome validate(Path file, String profileId) {
        long start = System.currentTimeMillis();
        try {
            if (!Files.isRegularFile(file)) {
                return Outcome.error("File not found", 0);
            }
            if (Files.size(file) == 0) {
                return Outcome.error("File is empty (0 bytes)", 0);
            }

            if (profileId != null) {
                PDFAFlavour flavour = PDFAFlavour.fromString(profileId);
                return run(file, flavour, false, start);
            }

            // Auto-detect: parse once with NO_FLAVOUR to read the declared flavour from XMP.
            try (InputStream in = new BufferedInputStream(Files.newInputStream(file));
                 PDFAParser parser = Foundries.defaultInstance().createParser(in, PDFAFlavour.NO_FLAVOUR)) {
                PDFAFlavour detected = parser.getFlavour();
                if (detected != null && detected != PDFAFlavour.NO_FLAVOUR) {
                    return validateWith(parser, detected, false, start);
                }
            }
            // File does not declare PDF/A conformance: check it against PDF/A-1b so the
            // user still sees what is missing.
            return run(file, PDFAFlavour.PDFA_1_B, true, start);

        } catch (EncryptedPdfException e) {
            return Outcome.error("File is encrypted or password protected", elapsed(start));
        } catch (ModelParsingException e) {
            return Outcome.error("Not a valid PDF or file is damaged: " + safe(e), elapsed(start));
        } catch (OutOfMemoryError e) {
            return Outcome.error("Not enough memory to process this file", elapsed(start));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Validation failed for " + file, e);
            return Outcome.error("Could not validate: " + safe(e), elapsed(start));
        }
    }

    private Outcome run(Path file, PDFAFlavour flavour, boolean undeclared, long start) throws Exception {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file));
             PDFAParser parser = Foundries.defaultInstance().createParser(in, flavour)) {
            return validateWith(parser, flavour, undeclared, start);
        }
    }

    private Outcome validateWith(PDFAParser parser, PDFAFlavour flavour, boolean undeclared, long start)
            throws Exception {
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
        ValidationResult result = validator.validate(parser);

        List<RuleFailure> failures = new ArrayList<>();
        int errorCount = 0;
        for (TestAssertion a : result.getTestAssertions()) {
            if (a.getStatus() == TestAssertion.Status.FAILED) {
                errorCount++;
                if (failures.size() < MAX_STORED_FAILURES) {
                    String clause = a.getRuleId() == null ? "" : String.valueOf(a.getRuleId().getClause());
                    String test = a.getRuleId() == null ? "" : String.valueOf(a.getRuleId().getTestNumber());
                    String ctx = a.getLocation() == null ? "" : String.valueOf(a.getLocation().getContext());
                    failures.add(new RuleFailure(clause, test, String.valueOf(a.getMessage()), ctx));
                }
            }
        }

        String label = "PDF/A-" + flavour.getId();
        boolean compliant = result.isCompliant();
        long ms = elapsed(start);

        if (undeclared) {
            String msg = "This file does not declare PDF/A conformance. Checked against " + label
                    + (compliant ? " and no errors were found." : ": " + errorCount + " error(s).");
            return new Outcome(compliant ? FileResult.VALID : FileResult.INVALID,
                    "Not declared", msg, errorCount, ms, failures);
        }
        if (compliant) {
            return new Outcome(FileResult.VALID, label, "Valid " + label + " document", 0, ms, failures);
        }
        return new Outcome(FileResult.INVALID, label,
                "Not a valid " + label + " document (" + errorCount + " error(s))",
                errorCount, ms, failures);
    }

    private static long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    private static String safe(Throwable t) {
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }
}
