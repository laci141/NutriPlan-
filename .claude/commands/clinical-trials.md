# Clinical Trials CLI Skill

A multi-source clinical-trials intelligence system built on top of the `clinical-trials-pp-cli` binary from [mvanhorn/printing-press-library](https://github.com/mvanhorn/printing-press-library).

## Telepítés (ha nincs még)

```bash
GOTOOLCHAIN=local go build -o clinical-trials ./cmd/clinical-trials-pp-cli/
# vagy go install:
go install github.com/mvanhorn/printing-press-library/library/health/clinical-trials/cmd/clinical-trials-pp-cli@latest
```

## Legjobb parancsok (kipróbált, működik)

### 1. Betegség kereső — aktív kísérletek
```bash
clinical-trials-pp-cli recruiting "diabetes type 2" --limit 10 --human-friendly
```

### 5. Összehasonlítás — két gyógyszer egymás ellen
```bash
clinical-trials-pp-cli compare "aspirin" "ibuprofen" --human-friendly
# Vagy JSON módban:
clinical-trials-pp-cli compare "Keytruda" "Opdivo" --json
```

### 9. Publikáció / eredmény összefoglaló egy trial-hoz
```bash
clinical-trials-pp-cli evidence NCT04280705 --human-friendly
```

## Egyéb hasznos parancsok

```bash
# Leggyorsabban növekvő kategóriák
clinical-trials-pp-cli emerging "cancer" --human-friendly

# Kik finanszírozzák a legtöbb kísérletet?
clinical-trials-pp-cli sponsors "diabetes" --human-friendly

# Egy trial kockázata
clinical-trials-pp-cli risk NCT07011732 --human-friendly

# Heti összefoglaló riport
clinical-trials-pp-cli report "long covid" --format md

# Egészség check
clinical-trials-pp-cli health --human-friendly
```

## Fontos tudnivalók

- **Nincs szükség API kulcsra** az alap funkcióhoz
- `--human-friendly` = szép emberi olvasható kimenet
- `--json` = agent/script módhoz
- `--agent` = teljesen automatizált mód (json + no-input + no-color)
- `--data-source live` = csak live API, ne local cache
- Az MCP szerver: `clinical-trials-pp-mcp`

## Keresés helyett használd a `recruiting`-ot

A `search` parancs lokális adatbázist keres. Ha nincs szinkronizálva, hibát dob.  
A `recruiting` parancs közvetlenül a live API-t hívja.

## MCP szerver regisztrálás Claude Code-ba

```bash
claude mcp add clinical-trials-pp-mcp -- clinical-trials-pp-mcp
```
