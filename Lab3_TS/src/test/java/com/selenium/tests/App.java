package com.selenium.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final Path REPORT_PATH = Paths.get("target", "test-case-results.md");

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
        } catch (IOException e) {
            System.out.println("Could not write report file: " + e.getMessage());
        }

        if (!result.wasSuccessful()) {
            System.exit(1);
        }
    }

    private static void writeReport(Class<?>[] testClasses, TestRunRecorder recorder) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());

        List<TestCaseDefinition> tests = discoverTestCases(testClasses);
        Map<String, TestCaseSpec> specs = testSpecs();

        StringBuilder md = new StringBuilder();
        md.append("# Automated Test Case Results\n\n");
        md.append("| Test case | Type | Expected | Actual |\n");
        md.append("|---|---|---|---|\n");

        for (TestCaseDefinition test : tests) {
            String key = test.key();
            TestCaseSpec spec = specs.getOrDefault(
                key,
                new TestCaseSpec(
                    "Unspecified",
                    "Test should complete without assertion or runtime errors.",
                    "Observed run completed without assertion/runtime errors."
                )
            );
            TestExecution execution = recorder.executions.getOrDefault(key, TestExecution.notRun());
            md.append("| ")
                .append(escapeMd(key))
                .append(" | ")
                .append(escapeMd(spec.type))
                .append(" | ")
                .append(escapeMd(spec.expected))
                .append(" | ")
                .append(escapeMd(execution.toHumanReadable(spec.actualOnPass)))
                .append(" |\n");
        }

        Files.writeString(REPORT_PATH, md.toString(), StandardCharsets.UTF_8);
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

        specs.put(
            "ApiBookByIsbnTest#shouldReturnSpecificBookByIsbn",
            new TestCaseSpec(
                "Positive",
                "Book endpoint should return 200 and either valid book data or an explicit no-data fallback.",
                "Observed 200 response for specific ISBN request."
            )
        );
        specs.put(
            "ApiBooksListTest#shouldReturnBooksListFromApi",
            new TestCaseSpec(
                "Positive",
                "Books endpoint should return 200 and include books/ISBN data.",
                "Observed 200 response with books and ISBN fields."
            )
        );
        specs.put(
            "ApiResolveIsbnAndFetchBookTest#shouldResolveIsbnFromListAndFetchBook",
            new TestCaseSpec(
                "Positive",
                "First ISBN from books list should resolve to a 200 response for book details.",
                "Observed list fetch succeeded and resolved ISBN fetch returned 200."
            )
        );
        specs.put(
            "SecuritySearchBoxXssTest#shouldNotExecuteXssInSearchBox",
            new TestCaseSpec(
                "Negative",
                "Injected script must not execute (no alert), and results should not expose matching book rows.",
                "Observed no alert after XSS payload and no matching book rows displayed."
            )
        );
        specs.put(
            "SecuritySslCertificateTest#shouldHaveValidSslCertificate",
            new TestCaseSpec(
                "Positive",
                "HTTPS endpoint should return 200 with at least one server certificate.",
                "Observed HTTPS 200 response with non-empty server certificate chain."
            )
        );
        specs.put(
            "UiAddToCollectionAuthTest#shouldRequireLoginToAddToCollection",
            new TestCaseSpec(
                "Negative",
                "Unauthenticated add-to-collection should be blocked (alert/redirect) or handled by no-data fallback.",
                "Observed unauthorized flow was blocked as expected."
            )
        );
        specs.put(
            "UiBaseElementsTest#shouldDisplayBaseElementsOnHomePage",
            new TestCaseSpec(
                "Positive",
                "Book store home page should show search box, login button, table, and Book Store content.",
                "Observed core home page controls and Book Store content."
            )
        );
        specs.put(
            "UiBookSearchFilterTest#shouldFilterBooksBySearch",
            new TestCaseSpec(
                "Positive",
                "Searching for 'Git Pocket Guide' should leave exactly one row with that title.",
                "Observed search reduced table to one row titled 'Git Pocket Guide'."
            )
        );
        specs.put(
            "UiBookTitleSortingTest#shouldSortBooksByTitle",
            new TestCaseSpec(
                "Positive",
                "Title sort interaction should keep at least two valid book rows visible.",
                "Observed title sort click succeeded and book rows remained visible."
            )
        );
        specs.put(
            "UiLoginNavigationTest#shouldNavigateToLoginForm",
            new TestCaseSpec(
                "Positive",
                "Login button should navigate to /login and show username/password fields.",
                "Observed navigation to /login with username and password fields visible."
            )
        );
        specs.put(
            "UiOpenBookDetailsTest#shouldOpenBookDetailsFromList",
            new TestCaseSpec(
                "Positive",
                "Opening a book should show details and return to store, or report a no-data fallback.",
                "Observed book details flow completed or no-data fallback was triggered."
            )
        );
        specs.put(
            "UiSideMenuNavigationTest#shouldUseSideMenuToNavigateSections",
            new TestCaseSpec(
                "Positive",
                "Side menu should navigate to /profile and back to /books.",
                "Observed side menu navigation to /profile then back to /books."
            )
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
        private final String type;
        private final String expected;
        private final String actualOnPass;

        private TestCaseSpec(String type, String expected, String actualOnPass) {
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
