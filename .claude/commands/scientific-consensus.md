# Scientific Consensus CLI Skill

A multi-source scientific evidence aggregator — combines OpenAlex, PubMed, Crossref, Europe PMC, and Semantic Scholar to answer "what does the evidence say about X?" No API key required for core functionality.

## Installation (Termux/Android)

```bash
cd ~/printing-press-library/library/health/scientific-consensus
GOTOOLCHAIN=local go build -o scientific-consensus ./cmd/scientific-consensus-pp-cli/
```

## Branch

```
feat/scientific-consensus  (in printing-press-library repo)
```

## Sources

| Source | Coverage |
|---|---|
| OpenAlex | Primary — 250M+ works |
| PubMed | Biomedical literature |
| Crossref | DOI metadata + citations |
| Europe PMC | Open access full text |
| Semantic Scholar | AI-enriched, citation graphs |

No API key needed. Optional: OpenAI / Anthropic / Gemini key for AI-enhanced stance detection.

## All 37 Commands

### CONSENSUS — Evidence stance score
```bash
./scientific-consensus consensus "vaccines cause autism" --human-friendly
./scientific-consensus consensus "mediterranean diet prevents heart disease" --human-friendly
./scientific-consensus consensus "statins reduce cardiovascular risk" --human-friendly
```
Returns: score (0–100), confidence level, evidence strength, stance (supports/refutes/mixed).

### EVIDENCE — Study design pyramid
```bash
./scientific-consensus evidence "aspirin stroke prevention" --human-friendly
./scientific-consensus evidence "vitamin D supplementation" --human-friendly
./scientific-consensus evidence "intermittent fasting weight loss" --human-friendly
```
Displays pyramid from meta-analysis → RCT → cohort → case report.

### COMPARE — Side-by-side analysis
```bash
./scientific-consensus compare "aspirin" "ibuprofen" --human-friendly
./scientific-consensus compare "ozempic weight loss" "lifestyle intervention weight loss" --human-friendly
```

### GAPS — Under-researched areas
```bash
./scientific-consensus gaps "long covid treatment" --human-friendly
./scientific-consensus gaps "alzheimer prevention" --human-friendly
./scientific-consensus gaps "childhood obesity intervention" --human-friendly
```

### CONTROVERSIES — Conflicting findings
```bash
./scientific-consensus controversies "coffee health effects" --human-friendly
./scientific-consensus controversies "low-carb vs low-fat diet" --human-friendly
./scientific-consensus controversies "antidepressants efficacy" --human-friendly
```

### EMERGING — Fastest-growing research areas
```bash
./scientific-consensus emerging --human-friendly
./scientific-consensus emerging --limit 10 --human-friendly
./scientific-consensus emerging --json
```

### DRIFT — Topic distribution over time
```bash
./scientific-consensus drift "cancer immunotherapy" --from 2018 --to 2024 --human-friendly
./scientific-consensus drift "covid-19 long term effects" --from 2020 --to 2025 --human-friendly
```
Note: requires minimum 2-year span between --from and --to.

### REPRODUCIBILITY — Replication quality signals
```bash
./scientific-consensus reproducibility "social priming effects" --human-friendly
./scientific-consensus reproducibility "gut microbiome mental health" --human-friendly
```

### QUALITY — Study quality estimation
```bash
./scientific-consensus quality "omega-3 depression" --human-friendly
./scientific-consensus quality "exercise dementia prevention" --human-friendly
```

### FUNDING — Funding pattern analysis
```bash
./scientific-consensus funding "cancer" --human-friendly
./scientific-consensus funding "diabetes drug trials" --human-friendly
./scientific-consensus funding "alzheimer" --human-friendly
```

### TIMELINE — Publication history with milestones
```bash
./scientific-consensus timeline "mRNA vaccines" --human-friendly
./scientific-consensus timeline "CRISPR gene editing" --human-friendly
```

### TRENDS — Year-over-year growth
```bash
./scientific-consensus trends "GLP-1 obesity" --human-friendly
./scientific-consensus trends "artificial intelligence radiology" --human-friendly
```

### WATCH — Monitor for new publications
```bash
./scientific-consensus watch "long covid" --human-friendly
./scientific-consensus watch "alzheimer amyloid" --human-friendly
```

### SEARCH — Full-text search (cross-source)
```bash
./scientific-consensus search "CRISPR off-target effects" --human-friendly
./scientific-consensus search "metformin longevity" --limit 10 --human-friendly
./scientific-consensus search "cancer immunotherapy" --sort citations --human-friendly
```

### LANDMARK — Most influential papers
```bash
./scientific-consensus landmark "deep learning medical imaging" --human-friendly
./scientific-consensus landmark "covid-19 vaccine efficacy" --human-friendly
```

### RANK-AUTHORS — Top researchers by topic
```bash
./scientific-consensus rank-authors "alzheimer disease" --human-friendly
./scientific-consensus rank-authors "climate change health" --human-friendly
```

### RANK-INSTITUTIONS — Top institutions by output
```bash
./scientific-consensus rank-institutions "cancer research" --human-friendly
./scientific-consensus rank-institutions "neuroscience" --human-friendly
```

### RANK-JOURNALS — Leading publication venues
```bash
./scientific-consensus rank-journals "cardiology" --human-friendly
./scientific-consensus rank-journals "oncology clinical trials" --human-friendly
```

### CITED-BY — Citation resolution
```bash
./scientific-consensus cited-by 10.1056/NEJMoa2100473 --human-friendly
./scientific-consensus cited-by pmid:33301246 --human-friendly
```

### AUTHORS — Author discovery and details
```bash
./scientific-consensus authors search "Jennifer Doudna" --human-friendly
./scientific-consensus authors get A2208157607 --human-friendly
```

### INSTITUTIONS — Institution information
```bash
./scientific-consensus institutions search "Harvard Medical School" --human-friendly
./scientific-consensus institutions get I136199984 --human-friendly
```

### WORKS — Publication details
```bash
./scientific-consensus works search "CRISPR cancer" --human-friendly
./scientific-consensus works get W2741809807 --human-friendly
```

### ANALYTICS — Faceted data aggregation
```bash
./scientific-consensus analytics "diabetes" --human-friendly
./scientific-consensus analytics "obesity intervention" --json
```

### EXPORT — Multi-format output
```bash
./scientific-consensus export "alzheimer prevention" --format md
./scientific-consensus export "cancer immunotherapy" --format bibtex
./scientific-consensus export "covid long term" --format csv
./scientific-consensus export "diabetes diet" --format html
```

### CURATE — Ranked reading list
```bash
./scientific-consensus curate "sleep deprivation cognitive function" --human-friendly
./scientific-consensus curate "microbiome mental health" --limit 20 --human-friendly
```

### SYNC — Populate local SQLite store
```bash
./scientific-consensus sync
./scientific-consensus sync --query "cardiovascular"
```

### SQL — Raw FTS5 query on local corpus
```bash
./scientific-consensus sql "SELECT title, year FROM works WHERE year > 2020 LIMIT 10"
./scientific-consensus sql "SELECT COUNT(*) FROM works"
```

### SOURCES — Source status and coverage
```bash
./scientific-consensus sources --human-friendly
./scientific-consensus sources --json
```

### DOCTOR — Health check and API key detection
```bash
./scientific-consensus doctor
./scientific-consensus doctor --json
```

### OTHER COMMANDS
```bash
./scientific-consensus profile list
./scientific-consensus profile create myprofile --limit 20
./scientific-consensus which "what does evidence say about coffee"
./scientific-consensus agent-context --json
./scientific-consensus api --human-friendly
./scientific-consensus feedback "great tool"
./scientific-consensus completion bash >> ~/.bashrc
./scientific-consensus version
./scientific-consensus help
```

## All Available Commands (37 total)

| Command | Type | Description |
|---|---|---|
| consensus | live API | Evidence stance score + confidence |
| evidence | live API | Study design pyramid |
| compare | live API | Side-by-side claim analysis |
| gaps | live API | Under-researched populations/designs |
| controversies | live API | Conflicting findings |
| emerging | live API | Fastest-growing research areas |
| drift | live API | Topic distribution over time |
| reproducibility | live API | Replication quality signals |
| quality | live API | Study quality estimation |
| funding | live API | Funder patterns and concentration |
| timeline | live API | Publication history + milestones |
| trends | live API | Year-over-year growth |
| watch | live API | Monitor for new publications |
| search | live API | Cross-source full-text search |
| landmark | live API | Most influential papers |
| rank-authors | live API | Top researchers by topic |
| rank-institutions | live API | Top institutions |
| rank-journals | live API | Leading publication venues |
| cited-by | live API | Cross-source citation resolution |
| authors | live API | Author search and details |
| institutions | live API | Institution search and details |
| works | live API | Publication search and details |
| analytics | live API | Faceted aggregation |
| export | live API | Multi-format output (md/json/csv/html/bibtex) |
| curate | live API | Ranked deduplicated reading list |
| sync | local DB | Populate SQLite from all sources |
| sql | local DB | Raw FTS5 query on synced corpus |
| sources | info | Source status + rate limits |
| doctor | info | Health check + API key detection |
| profile | config | Named flag sets |
| which | helper | NL to command resolver |
| agent-context | agent | JSON description for agents |
| api | info | Browse API endpoints |
| feedback | local | Record feedback |
| completion | shell | Shell autocompletion |
| version | info | Print version |
| help | info | Help |

## Key Rules

- `--human-friendly` = colored terminal output
- `--json` = machine/agent output
- `--agent` = json + no-color + no-input (CI/scripts)
- `--summarize` = AI-enhanced summaries (uses S2 TLDR without key)
- `--sort citations` = rank by impact
- `--limit N` = cap results
- `--select field1,field2` = field filtering
- `drift` requires `--from` and `--to` with min 2-year gap
- No API key needed; optional OpenAI/Anthropic/Gemini for enhanced stance detection
- Read-only CLI — never modifies source data

## Export Formats

| Flag | Output |
|---|---|
| `--format md` | Markdown report |
| `--format json` | Machine-readable |
| `--format csv` | Spreadsheet |
| `--format html` | Web page |
| `--format bibtex` | Citation manager |

## MCP Server (Claude Desktop)

```bash
claude mcp add scientific-consensus-pp-mcp -- scientific-consensus-pp-mcp
```

## HTML Demo Files

- English mobile-friendly: `scientific-consensus-demo.html` (NutriPlan- repo, claude/clinical-trials-cli-vu4v7j branch)
