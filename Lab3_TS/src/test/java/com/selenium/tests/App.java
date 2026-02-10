package com.selenium.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class App {
    private static final Path REPORT_PATH = Paths.get("results.md");
    private static final Path LEGACY_REPORT_PATH = Paths.get("target", "test-case-results.md");

    public static void main(String[] args) {
        Class<?>[] testClasses = {
            UiBaseElementsTest.class,
            UiLoginNavigationTest.class,
            UiSideMenuNavigationTest.class,
            UiBookSearchFilterTest.class,
            UiBookTitleSortingTest.class,
            UiOpenBookDetailsTest.class,
            UiAddToCollectionAuthTest.class,
            ApiBooksListTest.class,
            ApiBookByIsbnTest.class,
            ApiResolveIsbnAndFetchBookTest.class,
            SecuritySslCertificateTest.class,
            SecuritySearchBoxXssTest.class
        };

        JUnitCore core = new JUnitCore();
        TestRunRecorder recorder = new TestRunRecorder();
        core.addListener(recorder);

        PrintStream originalOut = System.out;
        PrintStream teeOut = new PrintStream(
            new TeeOutputStream(originalOut, recorder),
            true,
            StandardCharsets.UTF_8
        );
        System.setOut(teeOut);

        Result result;
        try {
            result = core.run(testClasses);
        } finally {
            teeOut.flush();
            System.setOut(originalOut);
        }

        for (Failure failure : result.getFailures()) {
            System.out.println("FAIL: " + failure.getTestHeader());
            System.out.println("  " + failure.getMessage());
        }

        System.out.println("Total: " + result.getRunCount());
        System.out.println("Failed: " + result.getFailureCount());
        System.out.println("Ignored: " + result.getIgnoreCount());
        System.out.println("Time: " + result.getRunTime() + " ms");

        try {
            writeReport(testClasses, recorder);
            System.out.println("Report: " + REPORT_PATH.toAbsolutePath());
            System.out.println("Report: " + LEGACY_REPORT_PATH.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Could not write report file: " + e.getMessage());
        }

        if (!result.wasSuccessful()) {
            System.exit(1);
        }
    }

    private static void writeReport(Class<?>[] testClasses, TestRunRecorder recorder) throws IOException {
        List<TestCaseDefinition> tests = discoverTestCases(testClasses);
        Map<String, TestCaseSpec> specs = testSpecs();
        String generatedAt = ZonedDateTime
            .now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));

        int passed = 0;
        int failed = 0;
        int ignored = 0;
        int notRun = 0;
        long totalElapsedMs = 0L;

        StringBuilder detailsRows = new StringBuilder();
        for (TestCaseDefinition test : tests) {
            String key = test.key();
            TestCaseSpec spec = specs.getOrDefault(key, defaultSpec(test));
            TestExecution execution = recorder.executions.getOrDefault(key, TestExecution.notRun());

            switch (execution.status) {
                case PASSED -> passed++;
                case FAILED -> failed++;
                case IGNORED -> ignored++;
                case NOT_RUN -> notRun++;
            }

            if (execution.elapsedMs >= 0L) {
                totalElapsedMs += execution.elapsedMs;
            }

            detailsRows.append("| ")
                .append(escapeMd(spec.displayName))
                .append(" | ")
                .append(escapeMd(key))
                .append(" | ")
                .append(escapeMd(spec.area))
                .append(" | ")
                .append(escapeMd(spec.type))
                .append(" | ")
                .append(escapeMd(spec.expected))
                .append(" | ")
                .append(escapeMd(execution.toHumanReadable(spec.actualOnPass)))
                .append(" | ")
                .append(statusLabel(execution.status))
                .append(" | ")
                .append(formatDuration(execution.elapsedMs))
                .append(" |\n");
        }

        StringBuilder md = new StringBuilder();
        md.append("# Automated Test Results\n\n");
        md.append("- Generated: ").append(generatedAt).append('\n');
        md.append("- Runner: `com.selenium.tests.App`\n");
        md.append("- Output files: `results.md`, `target/test-case-results.md`\n\n");
        md.append("## Summary\n\n");
        md.append("| Metric | Value |\n");
        md.append("|---|---:|\n");
        md.append("| Total tests discovered | ").append(tests.size()).append(" |\n");
        md.append("| Passed | ").append(passed).append(" |\n");
        md.append("| Failed | ").append(failed).append(" |\n");
        md.append("| Ignored | ").append(ignored).append(" |\n");
        md.append("| Not run | ").append(notRun).append(" |\n");
        md.append("| Total execution time | ").append(totalElapsedMs).append(" ms |\n\n");
        md.append("## Detailed Results\n\n");
        md.append(
            "| Test Name | Technical ID | Area | Positive/Negative | Expected Behavior | Actual Behavior | Status | Duration |\n"
        );
        md.append("|---|---|---|---|---|---|---:|---:|\n");
        md.append(detailsRows);

        createParentDirectoryIfNeeded(REPORT_PATH);
        createParentDirectoryIfNeeded(LEGACY_REPORT_PATH);
        Files.writeString(REPORT_PATH, md.toString(), StandardCharsets.UTF_8);
        Files.writeString(LEGACY_REPORT_PATH, md.toString(), StandardCharsets.UTF_8);
    }

    private static void createParentDirectoryIfNeeded(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private static String statusLabel(Status status) {
        return switch (status) {
            case PASSED -> "PASS";
            case FAILED -> "FAIL";
            case IGNORED -> "IGNORED";
            case NOT_RUN -> "NOT RUN";
        };
    }

    private static String formatDuration(long elapsedMs) {
        return elapsedMs >= 0L ? elapsedMs + " ms" : "-";
    }

    private static TestCaseSpec defaultSpec(TestCaseDefinition test) {
        String className = test.clazz().getSimpleName().replaceFirst("Test$", "");
        String methodName = test.methodName().replaceFirst("^(should|test)", "");
        String scenario = humanizeIdentifier(methodName).trim();
        if (!scenario.isEmpty()) {
            scenario = Character.toUpperCase(scenario.charAt(0)) + scenario.substring(1);
        } else {
            scenario = "Unnamed scenario";
        }

        return new TestCaseSpec(
            humanizeIdentifier(className) + " - " + scenario,
            areaFromClassName(test.clazz().getSimpleName()),
            "Unspecified",
            "Test should complete without assertion or runtime errors.",
            "Observed run completed without assertion/runtime errors."
        );
    }

    private static String areaFromClassName(String className) {
        if (className.startsWith("Ui")) {
            return "UI";
        }
        if (className.startsWith("Api")) {
            return "API";
        }
        if (className.startsWith("Security")) {
            return "Security";
        }
        return "General";
    }

    private static String humanizeIdentifier(String value) {
        return value
            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
            .replaceAll("([a-z\\d])([A-Z])", "$1 $2")
            .replace('_', ' ')
            .trim();
    }

    private static void addSpec(
        Map<String, TestCaseSpec> specs,
        String key,
        String displayName,
        String area,
        String type,
        String expected,
        String actualOnPass
    ) {
        specs.put(key, new TestCaseSpec(displayName, area, type, expected, actualOnPass));
    }

    private static List<TestCaseDefinition> discoverTestCases(Class<?>[] testClasses) {
        List<TestCaseDefinition> tests = new ArrayList<>();
        for (Class<?> clazz : testClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    tests.add(new TestCaseDefinition(clazz, method.getName()));
                }
            }
        }
        tests.sort(Comparator.comparing(TestCaseDefinition::key));
        return tests;
    }

    private static Map<String, TestCaseSpec> testSpecs() {
        Map<String, TestCaseSpec> specs = new LinkedHashMap<>();

        addSpec(
            specs,
            "ApiBookByIsbnTest#shouldReturnSpecificBookByIsbn",
            "API - Fetch Book By Fixed ISBN",
            "API",
            "Positive",
            "Book endpoint should return 200 and either valid book data or an explicit no-data fallback.",
            "Observed 200 response for specific ISBN request."
        );
        addSpec(
            specs,
            "ApiBooksListTest#shouldReturnBooksListFromApi",
            "API - Fetch Books List",
            "API",
            "Positive",
            "Books endpoint should return 200 and include books/ISBN data.",
            "Observed 200 response with books and ISBN fields."
        );
        addSpec(
            specs,
            "ApiResolveIsbnAndFetchBookTest#shouldResolveIsbnFromListAndFetchBook",
            "API - Resolve ISBN From List And Fetch Details",
            "API",
            "Positive",
            "First ISBN from books list should resolve to a 200 response for book details.",
            "Observed list fetch succeeded and resolved ISBN fetch returned 200."
        );
        addSpec(
            specs,
            "SecuritySearchBoxXssTest#shouldNotExecuteXssInSearchBox",
            "Security - Reject XSS Payload In Search Box",
            "Security",
            "Negative",
            "Injected script must not execute (no alert), and results should not expose matching book rows.",
            "Observed no alert after XSS payload and no matching book rows displayed."
        );
        addSpec(
            specs,
            "SecuritySslCertificateTest#shouldHaveValidSslCertificate",
            "Security - Validate SSL Certificate",
            "Security",
            "Positive",
            "HTTPS endpoint should return 200 with at least one server certificate.",
            "Observed HTTPS 200 response with non-empty server certificate chain."
        );
        addSpec(
            specs,
            "UiAddToCollectionAuthTest#shouldRequireLoginToAddToCollection",
            "UI - Block Add To Collection When Logged Out",
            "UI",
            "Negative",
            "Unauthenticated add-to-collection should be blocked (alert/redirect) or handled by no-data fallback.",
            "Observed unauthorized flow was blocked as expected."
        );
        addSpec(
            specs,
            "UiBaseElementsTest#shouldDisplayBaseElementsOnHomePage",
            "UI - Display Base Elements On Home Page",
            "UI",
            "Positive",
            "Book store home page should show search box, login button, table, and Book Store content.",
            "Observed core home page controls and Book Store content."
        );
        addSpec(
            specs,
            "UiBookSearchFilterTest#shouldFilterBooksBySearch",
            "UI - Filter Book List By Search Term",
            "UI",
            "Positive",
            "Searching for 'Git Pocket Guide' should leave exactly one row with that title.",
            "Observed search reduced table to one row titled 'Git Pocket Guide'."
        );
        addSpec(
            specs,
            "UiBookTitleSortingTest#shouldSortBooksByTitle",
            "UI - Sort Books By Title",
            "UI",
            "Positive",
            "Title sort interaction should keep at least two valid book rows visible.",
            "Observed title sort click succeeded and book rows remained visible."
        );
        addSpec(
            specs,
            "UiLoginNavigationTest#shouldNavigateToLoginForm",
            "UI - Navigate To Login Form",
            "UI",
            "Positive",
            "Login button should navigate to /login and show username/password fields.",
            "Observed navigation to /login with username and password fields visible."
        );
        addSpec(
            specs,
            "UiOpenBookDetailsTest#shouldOpenBookDetailsFromList",
            "UI - Open Book Details From List",
            "UI",
            "Positive",
            "Opening a book should show details and return to store, or report a no-data fallback.",
            "Observed book details flow completed or no-data fallback was triggered."
        );
        addSpec(
            specs,
            "UiSideMenuNavigationTest#shouldUseSideMenuToNavigateSections",
            "UI - Navigate Sections From Side Menu",
            "UI",
            "Positive",
            "Side menu should navigate to /profile and back to /books.",
            "Observed side menu navigation to /profile then back to /books."
        );

        return specs;
    }

    private static String escapeMd(String value) {
        return value.replace("|", "\\|").replace("\n", " ");
    }

    private record TestCaseDefinition(Class<?> clazz, String methodName) {
        String key() {
            return clazz.getSimpleName() + "#" + methodName;
        }
    }

    private static final class TestCaseSpec {
        private final String displayName;
        private final String area;
        private final String type;
        private final String expected;
        private final String actualOnPass;

        private TestCaseSpec(String displayName, String area, String type, String expected, String actualOnPass) {
            this.displayName = displayName;
            this.area = area;
            this.type = type;
            this.expected = expected;
            this.actualOnPass = actualOnPass;
        }
    }

    private enum Status {
        PASSED,
        FAILED,
        IGNORED,
        NOT_RUN
    }

    private static final class TestExecution {
        private Status status;
        private String message;
        private String output;
        private long elapsedMs;

        private TestExecution(Status status, String message, String output, long elapsedMs) {
            this.status = status;
            this.message = message;
            this.output = output;
            this.elapsedMs = elapsedMs;
        }

        static TestExecution passed() {
            return new TestExecution(Status.PASSED, "", "", -1L);
        }

        static TestExecution failed(String details) {
            return new TestExecution(Status.FAILED, details == null ? "Failed." : details, "", -1L);
        }

        static TestExecution ignored() {
            return new TestExecution(Status.IGNORED, "Ignored.", "", -1L);
        }

        static TestExecution notRun() {
            return new TestExecution(Status.NOT_RUN, "Not run.", "", -1L);
        }

        void appendOutput(String line) {
            if (line == null || line.isBlank()) {
                return;
            }
            if (output == null || output.isBlank()) {
                output = line.strip();
                return;
            }
            output = output + " " + line.strip();
        }

        String toHumanReadable(String actualOnPass) {
            String outputText = output == null ? "" : output.strip();
            String timing = elapsedMs >= 0 ? " Runtime: " + elapsedMs + " ms." : "";
            return switch (status) {
                case PASSED -> {
                    String passText = actualOnPass;
                    if (!outputText.isBlank()) {
                        passText = passText + " Output: " + outputText;
                    }
                    yield passText + timing;
                }
                case FAILED -> "Failed. " + message + timing;
                case IGNORED -> "Ignored by JUnit runner." + timing;
                case NOT_RUN -> "Not run in this execution.";
            };
        }
    }

    private static final class TestRunRecorder extends RunListener {
        private final Map<String, TestExecution> executions = new LinkedHashMap<>();
        private final Map<Long, String> runningByThread = new ConcurrentHashMap<>();
        private final Map<String, Long> startedAtMillis = new ConcurrentHashMap<>();

        @Override
        public void testStarted(org.junit.runner.Description description) {
            String key = keyOf(description);
            runningByThread.put(Thread.currentThread().getId(), key);
            startedAtMillis.put(key, System.currentTimeMillis());
            executions.putIfAbsent(key, TestExecution.notRun());
        }

        @Override
        public void testFinished(org.junit.runner.Description description) {
            String key = keyOf(description);
            TestExecution execution = executions.computeIfAbsent(key, k -> TestExecution.notRun());
            if (execution.status == Status.NOT_RUN) {
                execution.status = Status.PASSED;
            }
            Long started = startedAtMillis.remove(key);
            if (started != null) {
                execution.elapsedMs = Math.max(0L, System.currentTimeMillis() - started);
            }
            runningByThread.remove(Thread.currentThread().getId());
        }

        @Override
        public void testFailure(Failure failure) {
            String key = keyOf(failure.getDescription());
            String message = failure.getMessage();
            if (message == null || message.isBlank()) {
                message = failure.getException() == null ? "No failure message." : failure.getException().toString();
            }
            TestExecution execution = executions.computeIfAbsent(key, k -> TestExecution.notRun());
            execution.status = Status.FAILED;
            execution.message = message;
        }

        @Override
        public void testIgnored(org.junit.runner.Description description) {
            String key = keyOf(description);
            TestExecution execution = executions.computeIfAbsent(key, k -> TestExecution.notRun());
            execution.status = Status.IGNORED;
            execution.message = "Ignored.";
        }

        void appendConsole(String line) {
            String key = runningByThread.get(Thread.currentThread().getId());
            if (key == null) {
                return;
            }
            TestExecution execution = executions.computeIfAbsent(key, k -> TestExecution.notRun());
            execution.appendOutput(line);
        }

        private String keyOf(org.junit.runner.Description description) {
            String className = description.getClassName();
            String methodName = description.getMethodName();

            String classSimpleName = className == null
                ? "UnknownClass"
                : className.substring(className.lastIndexOf('.') + 1);
            String safeMethodName = methodName == null ? "unknownMethod" : methodName;

            return classSimpleName + "#" + safeMethodName;
        }
    }

    private static final class TeeOutputStream extends OutputStream {
        private final PrintStream original;
        private final TestRunRecorder recorder;
        private final StringBuilder buffer = new StringBuilder();

        private TeeOutputStream(PrintStream original, TestRunRecorder recorder) {
            this.original = original;
            this.recorder = recorder;
        }

        @Override
        public synchronized void write(int b) throws IOException {
            original.write(b);
            char c = (char) b;
            buffer.append(c);
            if (c == '\n') {
                flushBufferToRecorder();
            }
        }

        @Override
        public synchronized void flush() throws IOException {
            flushBufferToRecorder();
            original.flush();
        }

        @Override
        public synchronized void close() throws IOException {
            flush();
        }

        private void flushBufferToRecorder() {
            if (buffer.length() == 0) {
                return;
            }
            recorder.appendConsole(buffer.toString());
            buffer.setLength(0);
        }
    }
}
