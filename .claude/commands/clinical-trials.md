# Clinical Trials CLI Skill

A multi-source clinical-trials intelligence system — aggregates ClinicalTrials.gov, EU CTIS, OpenAlex. Nincs API kulcs szükséges.

## Telepítés

```bash
cd ~/printing-press-library/library/health/clinical-trials
GOTOOLCHAIN=local go build -o clinical-trials ./cmd/clinical-trials-pp-cli/
```

## Mind a 10 funkció — kipróbált parancsok

### 1. Betegség kereső — aktív toborzó kísérletek
```bash
./clinical-trials recruiting "diabetes type 2" --limit 10 --human-friendly
```

### 2. Földrajzi eloszlás — hol folyik a legtöbb kutatás
```bash
./clinical-trials hotspots "alzheimer" --human-friendly
```

### 3. Fázis szűrő — csak Phase 3 kísérletek
```bash
./clinical-trials phase3 "lung cancer" --human-friendly
```

### 4. Értesítő — változások figyelése
```bash
./clinical-trials watch "vitamin d" --human-friendly
```

### 5. Összehasonlítás — két gyógyszer egymás ellen ✓ KIPRÓBÁLT
```bash
./clinical-trials compare "aspirin" "ibuprofen" --human-friendly
```

### 6. Szponzor / ki finanszírozza
```bash
./clinical-trials sponsors "cancer" --human-friendly
```

### 7. Exportálás CSV-be
```bash
./clinical-trials export --format jsonl > trials.jsonl
```

### 8. Kockázatelemzés — egy trial kockázata
```bash
./clinical-trials risk NCT07011732 --human-friendly
```

### 9. Eredmény / publikáció összefoglaló ✓ KIPRÓBÁLT
```bash
./clinical-trials evidence NCT04280705 --human-friendly
```

### 10. Trend elemzés — növekedési sebesség
```bash
./clinical-trials velocity "alzheimer" --human-friendly
# Vagy leggyorsabban növekvő kategóriák:
./clinical-trials emerging "cancer" --human-friendly
```

## Bónusz: Teljes heti riport
```bash
./clinical-trials report "long covid" --format md
```

## Egészség ellenőrzés
```bash
./clinical-trials health --human-friendly
./clinical-trials doctor
```

## Fontos szabályok

- `--human-friendly` = szép emberi kimenet (terminálba)
- `--json` = gépi/agent kimenet
- `--agent` = json + no-color + no-input (CI/script)
- `recruiting` > `search` ha nincs lokális szinkron
- Nincs API kulcs szükséges az alap funkcióhoz
- Read-only CLI — nem módosít adatot

## MCP szerver (Claude Desktop-ba)
```bash
claude mcp add clinical-trials-pp-mcp -- clinical-trials-pp-mcp
```
