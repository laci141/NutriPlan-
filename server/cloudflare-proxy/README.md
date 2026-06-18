# NutriPlan AI Proxy (Cloudflare Worker)

Telefon → **ez a Worker** → OpenAI / Gemini / Anthropic.
A valódi szolgáltató-kulcsok **csak itt** élnek (titkos env változóban), az
alkalmazásban soha. Az app csak a Worker **URL-jét** és egy **app-tokent** tárol
(titkosítva, app-zár mögött).

Cloudflare Workers ingyenes csomag: napi 100 000 kérés – személyes használatra
gyakorlatilag **0 Ft**.

## Telepítés (egyszer, ~5 perc)

1. **Node.js** telepítése (ha még nincs), majd a Wrangler CLI:
   ```bash
   npm install -g wrangler
   ```

2. Belépés a Cloudflare-fiókodba (böngészőben megnyílik):
   ```bash
   wrangler login
   ```

3. Ebben a mappában telepítsd a Workert:
   ```bash
   cd server/cloudflare-proxy
   wrangler deploy
   ```
   A végén kapsz egy URL-t, pl.:
   `https://nutriplan-ai-proxy.<felhasznalonev>.workers.dev`

4. **Titkok beállítása** (ezeket a parancs bekéri, és NEM kerülnek a kódba):
   ```bash
   # Az app-token: generálj egy hosszú, véletlen szöveget (ezt írod majd az appba is)
   wrangler secret put APP_TOKEN

   # Legalább az egyik szolgáltató kulcsa kell:
   wrangler secret put OPENAI_API_KEY
   # opcionális továbbiak:
   wrangler secret put GEMINI_API_KEY
   wrangler secret put ANTHROPIC_API_KEY
   ```

5. **Az appban** (Beállítások → AI szolgáltatók):
   - Proxy URL: a 3. lépésben kapott cím
   - App-token: a 4. lépésben megadott `APP_TOKEN`
   - Kapcsold be a használni kívánt szolgáltató(ka)t.

## Kapcsolat-teszt

```bash
curl -X POST https://<a-te-worker-url> \
  -H "Authorization: Bearer <APP_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"ping":true}'
# Válasz: {"ok":true,"providers":["openai", ...]}
```

## Kérés formátuma (amit az app küld)

```json
{
  "provider": "openai",
  "model": "gpt-4o-mini",
  "max_tokens": 800,
  "messages": [
    {"role": "system", "content": "..."},
    {"role": "user", "content": "..."}
  ]
}
```
Válasz: `{ "text": "..." }`

## Biztonsági tudnivalók

- A szolgáltató-kulcsok **csak a Cloudflare titkos tárolójában** vannak.
- Minden kérés **app-tokent** igényel (időállandó összehasonlítás).
- **Modell-allowlist** providerenként; ismeretlen modell elutasítva.
- A Worker **nem naplózza** a promptokat.
- Csak **HTTPS** és csak **POST**.
- **Token-csere:** ha a token kiszivároghatott, futtasd újra a
  `wrangler secret put APP_TOKEN` parancsot egy új értékkel, és frissítsd az appban.
- **Költség-védelem (ajánlott):** állíts be napi limitet a szolgáltatónál
  (pl. OpenAI „Usage limits"), hogy egy esetleges visszaélés se okozzon nagy számlát.
