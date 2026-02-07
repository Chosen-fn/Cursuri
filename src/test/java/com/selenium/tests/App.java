package com.selenium.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses = loadTestClasses(
            "com.selenium.tests.BookStoreTests",
            "com.selenium.tests.BookStoreDatabaseTests",
            "com.selenium.tests.BookStoreSecurityTests"
        );

        Result result = JUnitCore.runClasses(testClasses);

        for (Failure failure : result.getFailures()) {
            System.out.println("FAIL: " + failure.getTestHeader());
            System.out.println("  " + failure.getMessage());
        }

        System.out.println("Total: " + result.getRunCount());
        System.out.println("Failed: " + result.getFailureCount());
        System.out.println("Ignored: " + result.getIgnoreCount());
        System.out.println("Time: " + result.getRunTime() + " ms");

        if (!result.wasSuccessful()) {
            System.exit(1);
        }
    }

    private static Class<?>[] loadTestClasses(String... classNames) {
        Class<?>[] classes = new Class<?>[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            try {
                classes[i] = Class.forName(classNames[i]);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                    "Test class not found on classpath: " + classNames[i],
                    e
                );
            }
        }
        return classes;
    }
}
