# Capabilities

A detailed inventory of what the Enterprise Hybrid Automation Framework does today. For the
high-level pitch and setup instructions, see the [README](README.md).

## Test coverage

**UI — [automationexercise.com](https://automationexercise.com)** (10+ distinct pages, via a full Page Object Model):

| Flow | Pages exercised |
|---|---|
| Registration → login → logout → re-login → delete | Home, Login/Signup, Account Information, Account Created, Account Deleted |
| Invalid login | Login/Signup (negative path) |
| Product search | Home, Products |
| Add to cart → view cart | Home, Products, Cart |
| Full checkout | Home, Products, Cart, Checkout, Payment, Order Placed, Account Deleted (cleanup) |
| Contact form with file upload | Home, Contact Us |

**API — [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com)** (`/posts`): GET single,
GET 404, GET filtered list, POST create (201 + generated id), PUT update, DELETE — each validated
against status code, response body fields, and (for GET single) a JSON Schema.

## Self-healing locators

- Every element is defined as an **ordered list of candidate locators** (`locators.json`), not a
  single selector — `id`, `css`, `xpath`, `name`, `linkText`, etc., in priority order.
- `SelfHealingLocator` walks the list; the first match wins. If the primary locator fails but a
  fallback still matches, the element resolves anyway, a `HealingEvent` is recorded, and the test
  keeps passing instead of failing on a cosmetic DOM change.
- Every healing event (page, element key, which fallback fired, timestamp) is written to
  `target/self-healing-report.json` at suite end — a reviewable artifact, not just a log line.
- **Not theoretical**: during development this framework caught two of its own wrong primary
  locators (a bad `id` on a file-upload field, a slightly-off XPath on a confirmation heading) —
  both healed automatically via fallback candidates, then were corrected for accuracy.

## Execution backends

One config property (`execution.mode`) switches between three interchangeable backends, no code
changes required:

- **`local`** — Selenium 4's built-in Selenium Manager auto-resolves the driver binary; no
  WebDriverManager dependency needed.
- **`grid`** — a self-hosted Selenium Grid via `docker/docker-compose.yml` (hub + Chrome node +
  Firefox node).
- **`browserstack`** — BrowserStack Automate cloud, authenticated purely via
  `BROWSERSTACK_USERNAME` / `BROWSERSTACK_ACCESS_KEY` environment variables (never hardcoded,
  never logged).

Cross-browser: Chrome, Firefox, and Edge are all supported locally and on Grid.

## Parallel execution

- `testng-crossbrowser.xml` runs the smoke suite against Chrome, Firefox, and Edge **concurrently**
  as separate TestNG `<test>` tags.
- `DriverManager` (ThreadLocal `WebDriver`) and `BrowserContext` (ThreadLocal browser override)
  make this genuinely thread-safe — no shared System-property races between parallel threads, a
  common bug in naive parallel Selenium setups.
- `testng-regression.xml` runs `parallel="classes"` across all UI + API test classes.

## Reliability

- **Retry-on-failure**: `RetryTransformer` (`IAnnotationTransformer`) auto-applies `RetryAnalyzer`
  to every `@Test` method — no `retryAnalyzer = ...` boilerplate per test — absorbing transient
  flakiness (`retry.max.count` config) without masking real regressions.
- **Ad-domain blocking**: headless Chrome runs with `--host-resolver-rules` blocking known
  ad-serving domains, preventing full-page interstitial ads on the public site under test from
  intercepting clicks mid-navigation — a real flakiness source discovered and fixed during
  development, not a hypothetical.
- **Explicit waits everywhere**: `WebDriverWait`/`FluentWait`-style polling in `BasePage`, no
  blind `Thread.sleep`.

## Reporting & observability

Two complementary HTML reports plus a step-by-step trail, all driven by one `TestListener`:

- **ExtentReports** — a single self-contained HTML file (`target/extent-report/ExtentReport.html`),
  no server or CLI needed.
- **Allure** — richer, multi-run trend reporting via `allure serve target/allure-results`.
- **Step-level logging** (`ReportLogger`) — every UI click/type and every API request/response is
  logged automatically to *both* reports plus the console/file log, with password fields masked.
  Test methods add their own Given/When/Then narration on top.
- **Screenshots on failure** — captured once, saved to `target/screenshots/`, and attached
  (base64-embedded) to both Allure and Extent.
- **Self-healing report** — `target/self-healing-report.json`.
- **Logs** — `target/automation.log` (Log4j2, console + file).

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`) runs three jobs on every push/PR:

1. **`smoke`** — local headless Chrome + API smoke suite.
2. **`grid-execution`** — the same suite against a *real* Selenium Grid, spun up as GitHub Actions
   `services:` (hub + Chrome node) — no Docker Compose needed in CI, and it proves the Grid
   execution path actually works rather than existing as unverified YAML.
3. **`browserstack`** (optional) — runs only when `RUN_BROWSERSTACK_CI=true` and BrowserStack
   secrets are configured, so the workflow never fails on forks/repos without BrowserStack access.

Each job uploads Allure results, Surefire reports, and the self-healing report as artifacts.

## Framework engineering

- **Centralized config** (`ConfigManager`): `-D` system property → `config.properties` → default,
  in that precedence order.
- **Jackson POJOs** (Java `record`s) for API request/response models, including JSON Schema
  validation via `rest-assured-json-schema-validator`.
- **TestNG suite structure**: smoke / regression / api / cross-browser suites, filtered by TestNG
  groups (`smoke`, `regression`, `api`).
- **Test data factory**: unique emails/names per run (timestamp-based) so signup-based tests never
  collide with leftover state from previous runs.
- **File upload testing** and **native `confirm()` dialog handling** (Contact Us form).
- Maven profiles per suite (`-Psmoke`, `-Pregression`, `-Papi`, `-Pcrossbrowser`) plus an
  OS-activated profile that transparently works around a JDK 21+/Windows NIO bug some local dev
  environments hit.

## Verified locally, not just claimed

Every flow above was run against the real, live public sites before being committed — see the
[README's verification notes](README.md#running-locally). Concretely: the full API suite passes
6/6 against jsonplaceholder.typicode.com via a real `mvn test` run, and every UI flow (signup/
login/delete, product search, cart, full checkout+payment, contact form) was driven end-to-end
against automationexercise.com with the actual production `Page Object`/`DriverFactory` code —
including catching and fixing two real self-healing locator bugs along the way.

## Tech stack

Java 17 · Maven · Selenium 4 (Selenium Manager) · TestNG 7 · REST Assured 5 · Allure 2 ·
ExtentReports 5 · Jackson · Log4j2 · Docker (Selenium Grid) · BrowserStack Automate ·
GitHub Actions
