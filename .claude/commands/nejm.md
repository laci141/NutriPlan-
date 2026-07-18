# NEJM CLI Skill

A New England Journal of Medicine terminal interface — browse current issues, search articles, track specialties, manage a reading list. No API key required; syncs from public RSS feeds.

## Installation (Termux/Android)

```bash
cd ~/printing-press-library/library/health/nejm
GOTOOLCHAIN=local go build -o nejm ./cmd/nejm-pp-cli/
```

## Branch

```
laci/feat/nejm  (in printing-press-library repo)
```

## Sync First

```bash
./nejm sync --feed etoc    # current issue table of contents
./nejm sync --feed axatoc  # ahead-of-print articles
```
Syncs ~135 articles into a local SQLite database (`~/.local/share/nejm-pp-cli/data.db`, ~602 KB, schema v4).

## All 24 Commands

### CURRENT — Latest issue articles
```bash
./nejm current --human-friendly
./nejm current --limit 5 --human-friendly
./nejm current --json
```

### RECENT — Recently published articles
```bash
./nejm recent --human-friendly
./nejm recent --limit 10 --human-friendly
```

### SEARCH — Full-text search in synced corpus
```bash
./nejm search "cardiovascular" --human-friendly
./nejm search "cancer immunotherapy" --human-friendly
./nejm search "diabetes GLP-1" --human-friendly
./nejm search "alzheimer" --limit 5 --human-friendly
```

### ARTICLE — Fetch single article by DOI
```bash
./nejm article 10.1056/NEJMicm2600597 --human-friendly
./nejm article 10.1056/NEJMp2605694 --human-friendly
./nejm article 10.1056/NEJMoa2600597 --enrich --human-friendly
```

### ARTICLES — Browse all synced articles
```bash
./nejm articles --human-friendly
./nejm articles --limit 20 --human-friendly
./nejm articles --json
```

### SINCE — Articles published since a date
```bash
./nejm since 2026-06-01 --human-friendly
./nejm since 2026-05-01 --human-friendly
./nejm since 2026-01-01 --limit 20 --human-friendly
```

### SPECIALTY — Browse by medical specialty
```bash
./nejm specialty "Cardiology" --human-friendly
./nejm specialty "Endocrinology" --human-friendly
./nejm specialty "Clinical Medicine" --human-friendly
./nejm specialty "Oncology" --human-friendly
```

### DIGEST — Weekly digest summary
```bash
./nejm digest --human-friendly
./nejm digest --json
```

### TRENDS — Publication trend analysis (requires --enrich)
```bash
./nejm trends --enrich --human-friendly
./nejm trends --enrich --json
```
Note: `--enrich` fetches metadata per article. Run `sync` first, then use `--enrich` for enriched analysis.

### OPEN-ACCESS — Open access filter (requires --enrich)
```bash
./nejm open-access --enrich --human-friendly
./nejm open-access --enrich --json
```

### READING-LIST — Personal reading list management
```bash
./nejm reading-list --human-friendly
./nejm reading-list add 10.1056/NEJMicm2600597
./nejm reading-list remove 10.1056/NEJMicm2600597
```

### WORKFLOW — Compound multi-step operations
```bash
./nejm workflow "sync and show current issue" --human-friendly
./nejm workflow "search cancer and summarize" --human-friendly
```

### SQL — Raw SQL query on local database
```bash
./nejm sql "SELECT title, published_at FROM articles ORDER BY published_at DESC LIMIT 5" --human-friendly
./nejm sql "SELECT COUNT(*) FROM articles" --human-friendly
```

### DOCTOR — System diagnostics
```bash
./nejm doctor
./nejm doctor --json
```
(Note: there is no `health` command — use `doctor` instead.)

### WHICH — Natural language to command resolver
```bash
./nejm which "show me latest heart articles"
./nejm which "find cancer research"
```

### IMPORT — Import from file
```bash
./nejm import articles.jsonl
```

### PROFILE — Named flag sets
```bash
./nejm profile list
./nejm profile create cardiology --specialty Cardiology
```

### API / AGENT-CONTEXT / VERSION / FEEDBACK / COMPLETION
```bash
./nejm api --human-friendly
./nejm agent-context --json
./nejm version
./nejm feedback "great tool"
./nejm completion bash >> ~/.bashrc
```

## All Available Commands (24 total)

| Command | Type | Description |
|---|---|---|
| current | local DB | Current issue articles |
| recent | local DB | Recently published |
| search | local DB | Full-text search |
| article | live + local | Single article by DOI |
| articles | local DB | Browse all articles |
| since | local DB | Articles since date |
| specialty | local DB | Browse by specialty |
| digest | local DB | Weekly digest |
| trends | local DB + fetch | Trend analysis (needs --enrich) |
| open-access | local DB + fetch | Open access filter (needs --enrich) |
| reading-list | local | Personal reading list |
| sync | live RSS | Download to SQLite |
| workflow | local | Multi-step compound operations |
| sql | local DB | Raw SQL query |
| import | local | Import from JSONL |
| doctor | info | Connectivity + DB diagnostics |
| which | helper | NL to command resolver |
| profile | config | Named flag sets |
| api | info | Browse API endpoints |
| agent-context | agent | JSON description for agents |
| version | info | Print version |
| feedback | local | Record feedback |
| completion | shell | Shell autocompletion |
| help | info | Help |

## Key Rules

- `--human-friendly` = colored terminal output
- `--json` = machine/agent output
- `--agent` = json + no-color + no-input (CI/scripts)
- `search`, `current`, `recent`, `specialty` all require `sync` first
- `trends` and `open-access` additionally require `--enrich`
- `article <doi>` works without sync (fetches live)
- No `health` command — use `doctor` instead
- No API key needed
- Read-only CLI — never modifies NEJM data

## Database Info

- Location: `~/.local/share/nejm-pp-cli/data.db`
- Size: ~602 KB
- Schema: v4
- Default feeds: `etoc` (current issue) + `axatoc` (ahead-of-print)
- Typical corpus: ~135 articles after full sync

## MCP Server (Claude Desktop)

```bash
claude mcp add nejm-pp-mcp -- nejm-pp-mcp
```

## HTML Demo Files

- English mobile-friendly: `nejm-demo.html` (NutriPlan- repo, claude/clinical-trials-cli-vu4v7j branch)
