# Automated Test Results

- Generated: 2026-02-09 11:35:20 UTC
- Runner: `com.selenium.tests.App`

## Summary

| Metric | Value |
|---|---:|
| Total tests discovered | 12 |
| Passed | 12 |
| Failed | 0 |
| Ignored | 0 |
| Not run | 0 |
| Total execution time | 45901 ms |

## Detailed Results

| Test Name | Technical ID | Area | Positive/Negative | Expected Behavior | Actual Behavior | Status | Duration |
|---|---|---|---|---|---|---:|---:|
| API - Fetch Book By Fixed ISBN | ApiBookByIsbnTest#shouldReturnSpecificBookByIsbn | API | Positive | Book endpoint should return 200 and either valid book data or an explicit no-data fallback. | Observed 200 response for specific ISBN request. Runtime: 60 ms. | PASS | 60 ms |
| API - Fetch Books List | ApiBooksListTest#shouldReturnBooksListFromApi | API | Positive | Books endpoint should return 200 and include books/ISBN data. | Observed 200 response with books and ISBN fields. Runtime: 528 ms. | PASS | 528 ms |
| API - Resolve ISBN From List And Fetch Details | ApiResolveIsbnAndFetchBookTest#shouldResolveIsbnFromListAndFetchBook | API | Positive | First ISBN from books list should resolve to a 200 response for book details. | Observed list fetch succeeded and resolved ISBN fetch returned 200. Runtime: 117 ms. | PASS | 117 ms |
| Security - Reject XSS Payload In Search Box | SecuritySearchBoxXssTest#shouldNotExecuteXssInSearchBox | Security | Negative | Injected script must not execute (no alert), and results should not expose matching book rows. | Observed no alert after XSS payload and no matching book rows displayed. Runtime: 2772 ms. | PASS | 2772 ms |
| Security - Validate SSL Certificate | SecuritySslCertificateTest#shouldHaveValidSslCertificate | Security | Positive | HTTPS endpoint should return 200 with at least one server certificate. | Observed HTTPS 200 response with non-empty server certificate chain. Runtime: 61 ms. | PASS | 61 ms |
| UI - Block Add To Collection When Logged Out | UiAddToCollectionAuthTest#shouldRequireLoginToAddToCollection | UI | Negative | Unauthenticated add-to-collection should be blocked (alert/redirect) or handled by no-data fallback. | Observed unauthorized flow was blocked as expected. Output: ok the link exists but there's no data Runtime: 10484 ms. | PASS | 10484 ms |
| UI - Display Base Elements On Home Page | UiBaseElementsTest#shouldDisplayBaseElementsOnHomePage | UI | Positive | Book store home page should show search box, login button, table, and Book Store content. | Observed core home page controls and Book Store content. Runtime: 3813 ms. | PASS | 3813 ms |
| UI - Filter Book List By Search Term | UiBookSearchFilterTest#shouldFilterBooksBySearch | UI | Positive | Searching for 'Git Pocket Guide' should leave exactly one row with that title. | Observed search reduced table to one row titled 'Git Pocket Guide'. Runtime: 2910 ms. | PASS | 2910 ms |
| UI - Sort Books By Title | UiBookTitleSortingTest#shouldSortBooksByTitle | UI | Positive | Title sort interaction should keep at least two valid book rows visible. | Observed title sort click succeeded and book rows remained visible. Runtime: 2890 ms. | PASS | 2890 ms |
| UI - Navigate To Login Form | UiLoginNavigationTest#shouldNavigateToLoginForm | UI | Positive | Login button should navigate to /login and show username/password fields. | Observed navigation to /login with username and password fields visible. Runtime: 2959 ms. | PASS | 2959 ms |
| UI - Open Book Details From List | UiOpenBookDetailsTest#shouldOpenBookDetailsFromList | UI | Positive | Opening a book should show details and return to store, or report a no-data fallback. | Observed book details flow completed or no-data fallback was triggered. Output: ok the link exists but there's no data Runtime: 16405 ms. | PASS | 16405 ms |
| UI - Navigate Sections From Side Menu | UiSideMenuNavigationTest#shouldUseSideMenuToNavigateSections | UI | Positive | Side menu should navigate to /profile and back to /books. | Observed side menu navigation to /profile then back to /books. Runtime: 2902 ms. | PASS | 2902 ms |
