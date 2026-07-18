# Clinical Trials CLI Skill

A multi-source clinical-trials intelligence system — aggregates ClinicalTrials.gov, EU CTIS, OpenAlex. No API key required.

## Installation (Termux/Android)

```bash
cd ~/printing-press-library/library/health/clinical-trials
GOTOOLCHAIN=local go build -o clinical-trials ./cmd/clinical-trials-pp-cli/
```

## GitHub Token (Termux push)

Token saved at `~/.github_token` on user's device.
Push command: `git push https://$(cat ~/.github_token)@github.com/laci141/printing-press-library.git fix-branch:feat/clinical-trials`
Alias: `gitpush` (defined in ~/.bashrc)

## Natural Language Wrapper (~/ct)

`~/ct` script routes natural language queries (English or Hungarian) to the correct CLI command.
Source: `library/health/clinical-trials/ct-wrapper.sh` on feat/clinical-trials branch.

Hungarian input examples (handled automatically):
```bash
~/ct "diabetes type 2 active trials"
~/ct "compare aspirin ibuprofen"
~/ct "cancer who funds"
~/ct "alzheimer trend"
~/ct "lung cancer phase 3"
~/ct "aspirin fda safety"
```

## All 30 Commands / Questions

### RECRUITING — Active recruiting trials
```bash
./clinical-trials recruiting "diabetes type 2" --limit 10 --human-friendly
./clinical-trials recruiting "heart disease" --human-friendly
./clinical-trials recruiting "obesity" --human-friendly
```

### HOTSPOTS — Geographic distribution of research
```bash
./clinical-trials hotspots "alzheimer" --human-friendly
./clinical-trials hotspots "covid-19" --human-friendly
./clinical-trials hotspots "diabetes" --human-friendly
```

### PHASE3 — Phase 3 trials only
```bash
./clinical-trials phase3 "lung cancer" --human-friendly
./clinical-trials phase3 "breast cancer" --human-friendly
./clinical-trials phase3 "hypertension" --human-friendly
```

### COMPARE — Head-to-head drug comparison (RxNorm normalized)
```bash
./clinical-trials compare "aspirin" "ibuprofen" --human-friendly
./clinical-trials compare "metformin" "insulin" --human-friendly
./clinical-trials compare "ozempic" "wegovy" --human-friendly
```

### SPONSORS — Who funds the research
```bash
./clinical-trials sponsors "cancer" --human-friendly
./clinical-trials sponsors "alzheimer" --human-friendly
./clinical-trials sponsors "heart disease" --human-friendly
```

### SAFETY — FDA adverse-event signals (FAERS database)
```bash
./clinical-trials safety "aspirin" --human-friendly
./clinical-trials safety "ibuprofen" --human-friendly
./clinical-trials safety "metformin" --human-friendly
```

### VELOCITY / EMERGING — Trend analysis and growth rate
```bash
./clinical-trials velocity "alzheimer" --human-friendly
./clinical-trials velocity "covid-19" --human-friendly
./clinical-trials emerging "cancer" --human-friendly
```

### RISK — Risk analysis for a specific trial (needs NCT ID)
```bash
./clinical-trials risk NCT07011732 --human-friendly
./clinical-trials risk NCT04280705 --human-friendly
```

### EVIDENCE — Publications and citations (needs NCT ID)
```bash
./clinical-trials evidence NCT04280705 --human-friendly
./clinical-trials evidence NCT06128837 --human-friendly
```

### WATCH — Monitor changes since last run
```bash
./clinical-trials watch "vitamin d" --human-friendly
```

### REPORT — Full weekly intelligence briefing
```bash
./clinical-trials report "long covid" --format md
./clinical-trials report "diabetes" --format md
```

### HEALTH / DOCTOR — System diagnostics
```bash
./clinical-trials health --human-friendly
./clinical-trials doctor
```

## All Available Commands (30 total)

| Command | Type | Description |
|---|---|---|
| recruiting | live API | Active recruiting trials |
| hotspots | live API | Geographic distribution |
| phase3 | live API | Phase 3 filter |
| watch | live API | Monitor changes |
| compare | live API | Head-to-head drug comparison |
| sponsors | live API | Who funds the research |
| safety | live API | FDA adverse-event signals |
| velocity | live API | Growth speed measurement |
| emerging | live API | Fastest-growing categories |
| evidence | live API | Publications + citations (needs NCT ID) |
| risk | live API | Risk analysis (needs NCT ID) |
| report | live API | Full weekly briefing |
| search | local DB | Full-text search (needs sync first) |
| sync | local DB | Download to SQLite offline |
| tail | live API | Real-time stream of changes |
| analytics | local DB | Analytics on synced data |
| workflow | live API | Compound multi-step operations |
| export | local | Export to JSONL/JSON |
| import | local | Import from JSONL |
| api | info | Browse API endpoints |
| profile | config | Named flag sets |
| health | info | Live API status |
| doctor | info | Connectivity diagnostics |
| which | helper | NL to command resolver |
| agent-context | agent | JSON description for agents |
| clinicaltrials-gov-version | info | API version |
| feedback | local | Record feedback |
| completion | shell | Shell autocompletion |
| version | info | Print version |
| help | info | Help |

## Key Rules

- `--human-friendly` = colored terminal output
- `--json` = machine/agent output
- `--agent` = json + no-color + no-input (CI/scripts)
- `recruiting` works live; `search` needs `sync` first
- `risk` and `evidence` require NCT ID (e.g. NCT04280705)
- No API key needed for core functionality
- Read-only CLI — never modifies data

## MCP Server (Claude Desktop)
```bash
claude mcp add clinical-trials-pp-mcp -- clinical-trials-pp-mcp
```

## HTML Demo Files
- Romanian: `clinical-trials-prezentare.html` (NutriPlan- repo)
- English mobile-friendly: `clinical-trials-demo.html` (NutriPlan- repo, claude/clinical-trials-cli-vu4v7j branch)
