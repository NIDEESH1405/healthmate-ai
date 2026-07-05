# HealthMate AI

A production-ready **Spring Boot 3.x (Java 17, Maven)** multi-agent healthcare assistant. HealthMate AI is **informational only** — it never diagnoses conditions or replaces professional medical care, and every agent surfaces that disclaimer in its UI.

## The 5 agents

| # | Agent | Accent color | What it does |
|---|-------|---------------|---------------|
| 1 | Appointment Scheduling | Amber `#D97706` | Books a department/doctor/date/time slot, validated against a hospital knowledge base, with an AI-generated confirmation message. Persisted to MySQL. |
| 2 | Symptom Checker | Orange `#F4511E` | Session-based chat: body-area checkboxes + severity slider → up to 3 AI clarifying questions → structured specialist + urgency assessment. |
| 3 | Medical Report Summarizer | Blue `#2563EB` | Upload a PDF → text extracted via Apache PDFBox → AI produces a plain-language summary + labeled findings (Normal/High/Low/Critical). Only the summary metadata is stored — never the raw report text. |
| 4 | Prescription Reminder | Violet `#7C3AED` | AI explains a medicine, cross-checked against a local reference DB; lets you set simple in-app reminders (no real notifications are sent). |
| 5 | Hospital Info | Emerald `#059669` | KB-grounded Q&A (keyword-overlap retrieval, top-3 sources shown) plus a full knowledge-base directory browser. |

## Tech stack

- **Backend:** Spring Boot 3.3, Spring Web (Thymeleaf views), Spring Data JPA
- **Database:** MySQL (`mysql-connector-j`) in production; H2 in-memory profile available for quick local verification
- **AI:** Groq API (`llama-3.3-70b-versatile`) via Spring WebFlux `WebClient`
- **PDF parsing:** Apache PDFBox 3
- **JSON:** Jackson
- **Frontend:** Server-rendered Thymeleaf + vanilla CSS/JS (no frontend framework)

## Project layout

```
src/main/java/com/healthmate/ai/
  config/       # WebClient, ObjectMapper, global exception handling
  controller/   # One controller per agent + HomeController
  service/      # GroqService, PdfExtractService, RetrievalService, per-agent services
  repository/   # Spring Data JPA repositories
  entity/       # JPA entities (Appointment, Reminder, ReportHistory)
  model/        # Non-persisted DTOs/state (ChatMessage, KbTopic, SymptomSession, ...)
  dto/          # Form-binding request objects
src/main/resources/
  data/hospital_kb.json      # Hospital knowledge base (7 topics)
  data/medication_db.json    # Local medication reference (8 medicines)
  templates/                 # Thymeleaf views (layout fragments + one page per agent)
  static/css/theme.css        # Design system
  static/js/                  # Per-page vanilla JS
```

## Prerequisites

- JDK 17+
- Maven 3.9+ (or use your IDE's bundled Maven)
- A MySQL 8.x server (local install, Docker, or a managed instance)
- A [Groq API key](https://console.groq.com) (free tier works)

## Running locally with MySQL

1. **Create the database** (the app will auto-create it too, via `createDatabaseIfNotExist=true`, but you can do it explicitly):

   ```sql
   CREATE DATABASE healthmate_ai;
   ```

2. **Set environment variables** (copy `.env.example` to `.env` or export them in your shell):

   ```bash
   export MYSQL_HOST=localhost
   export MYSQL_PORT=3306
   export MYSQL_DATABASE=healthmate_ai
   export MYSQL_USER=root
   export MYSQL_PASSWORD=your_mysql_password
   export GROQ_API_KEY=your_groq_api_key_here
   ```

3. **Build and run:**

   ```bash
   mvn clean package
   mvn spring-boot:run
   ```

   or run the packaged jar directly:

   ```bash
   java -jar target/healthmate-ai.jar
   ```

4. Open **http://localhost:8080**.

Hibernate is configured with `ddl-auto=update`, so the `appointments`, `reminders`, and `report_history` tables are created automatically on first run — no manual schema/migration step needed.

## Quick local verification with H2 (no MySQL required)

For a fast sanity check without setting up MySQL, run with the `h2` profile:

```bash
export GROQ_API_KEY=your_groq_api_key_here
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

This uses an in-memory H2 database (MySQL-compatible mode) so you can exercise every agent end-to-end. Data is lost on restart. **Use MySQL for anything beyond quick verification.**

If `GROQ_API_KEY` isn't set, the app still starts and every page still renders — the AI-dependent features (confirmations, symptom triage, report summaries, medicine explanations, KB Q&A) will show a clear fallback message instead of failing.

## Deploying to Railway

Railway auto-detects this as a Java/Maven project via **Nixpacks** — no Dockerfile needed.

1. **Push this repository to GitHub** (or connect your existing repo).
2. In Railway, create a **New Project → Deploy from GitHub repo** and select this repository.
3. **Add a MySQL plugin** to the project (Railway → *+ New* → *Database* → *MySQL*). Railway automatically injects `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` into your app's environment — `application.properties` in this project already reads those exact variable names as its first fallback, so **no extra wiring is required**.
4. In your app service's **Variables** tab, add:
   ```
   GROQ_API_KEY=your_groq_api_key_here
   ```
5. Railway sets `PORT` automatically; `application.properties` already binds `server.port` to it.
6. Deploy. Railway runs `mvn -B package` (via Nixpacks' Java provider) and then starts the jar. First boot will create the schema automatically via `ddl-auto=update`.

### Notes for production

- Consider setting `spring.jpa.hibernate.ddl-auto=validate` once your schema has stabilized, and manage migrations with Flyway/Liquibase for a real production rollout.
- Rotate your `GROQ_API_KEY` via Railway's variable UI rather than committing it anywhere.
- The `spring-boot-devtools` dependency is `optional` and marked `runtime`-scope only for local dev convenience; it has no effect on the packaged production jar's behavior beyond faster local restarts.

## Design notes

- **No diagnosis, ever.** Every agent's system prompt explicitly forbids diagnosis or prescription; the UI repeats this via a persistent disclaimer strip and per-agent callouts.
- **Minimal data retention.** Agent 3 never stores the uploaded PDF or its extracted text — only the AI-generated summary and a flagged-value count go into `report_history`. Agent 2's conversation and Agent 3's follow-up Q&A live only in the HTTP session, never the database.
- **Fail-soft AI calls.** `GroqService` never throws out to the UI: on any network/parsing failure it returns a clear, descriptive fallback string so pages always render.
- **Grounded, not hallucinated.** Agent 5's Q&A restricts the LLM to the top-3 retrieved KB topics via prompt context; Agent 1 cross-checks doctor availability against the same KB.
