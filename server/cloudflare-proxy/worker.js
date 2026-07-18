/**
 * NutriPlan AI Proxy – Cloudflare Worker
 *
 * Telefon → (HTTPS + app-token) → ez a Worker → OpenAI / Gemini / Anthropic
 *
 * Biztonság:
 *  - A valódi szolgáltató-kulcsok CSAK itt, titkos env változókban élnek
 *    (OPENAI_API_KEY, GEMINI_API_KEY, ANTHROPIC_API_KEY). Az appban soha.
 *  - Minden kérés kötelező app-tokent igényel (Authorization: Bearer ...),
 *    amit időállandó módon hasonlítunk össze (APP_TOKEN secret).
 *  - Modell-allowlist providerenként; ismeretlen modellt elutasít.
 *  - A promptokat NEM naplózzuk.
 *  - Csak POST; csak az engedélyezett (kulccsal rendelkező) providerek hívhatók.
 */

const MODEL_ALLOWLIST = {
  openai: ["gpt-4o-mini", "gpt-4o", "gpt-4.1-mini"],
  gemini: ["gemini-1.5-flash", "gemini-1.5-pro"],
  anthropic: ["claude-3-5-haiku-latest", "claude-3-5-sonnet-latest"],
};
const DEFAULT_MODEL = {
  openai: "gpt-4o-mini",
  gemini: "gemini-1.5-flash",
  anthropic: "claude-3-5-haiku-latest",
};

export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return json({ error: "method_not_allowed" }, 405);
    }

    // --- App-token ellenőrzés (időállandó) ---
    const auth = request.headers.get("Authorization") || "";
    const token = auth.startsWith("Bearer ") ? auth.slice(7) : "";
    if (!env.APP_TOKEN || !timingSafeEqual(token, env.APP_TOKEN)) {
      return json({ error: "unauthorized" }, 401);
    }

    let body;
    try {
      body = await request.json();
    } catch {
      return json({ error: "bad_json" }, 400);
    }

    // Egyszerű "ping" a kapcsolat-teszthez
    if (body.ping === true) {
      return json({ ok: true, providers: availableProviders(env) });
    }

    const provider = String(body.provider || "openai").toLowerCase();
    const messages = Array.isArray(body.messages) ? body.messages : null;
    if (!messages) return json({ error: "messages_required" }, 400);

    const allowed = MODEL_ALLOWLIST[provider];
    if (!allowed) return json({ error: "unknown_provider" }, 400);
    const model = body.model && allowed.includes(body.model) ? body.model : DEFAULT_MODEL[provider];
    const maxTokens = clampInt(body.max_tokens, 1, 2000, 800);

    try {
      let text;
      if (provider === "openai") text = await callOpenAI(env, model, messages, maxTokens);
      else if (provider === "gemini") text = await callGemini(env, model, messages, maxTokens);
      else if (provider === "anthropic") text = await callAnthropic(env, model, messages, maxTokens);
      else return json({ error: "unknown_provider" }, 400);
      return json({ text });
    } catch (e) {
      return json({ error: "upstream_error", detail: String((e && e.message) || e) }, 502);
    }
  },
};

// ---- Providerek ----

async function callOpenAI(env, model, messages, maxTokens) {
  if (!env.OPENAI_API_KEY) throw new Error("openai_not_configured");
  const res = await fetch("https://api.openai.com/v1/chat/completions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${env.OPENAI_API_KEY}`,
    },
    body: JSON.stringify({ model, messages, max_tokens: maxTokens, temperature: 0.7 }),
  });
  if (!res.ok) throw new Error(`openai_${res.status}`);
  const data = await res.json();
  return data.choices?.[0]?.message?.content ?? "";
}

async function callAnthropic(env, model, messages, maxTokens) {
  if (!env.ANTHROPIC_API_KEY) throw new Error("anthropic_not_configured");
  const system = messages.filter((m) => m.role === "system").map((m) => m.content).join("\n");
  const chat = messages
    .filter((m) => m.role === "user" || m.role === "assistant")
    .map((m) => ({ role: m.role, content: toAnthropicContent(m.content) }));
  const res = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "x-api-key": env.ANTHROPIC_API_KEY,
      "anthropic-version": "2023-06-01",
    },
    body: JSON.stringify({ model, max_tokens: maxTokens, system: system || undefined, messages: chat }),
  });
  if (!res.ok) throw new Error(`anthropic_${res.status}`);
  const data = await res.json();
  return data.content?.[0]?.text ?? "";
}

async function callGemini(env, model, messages, maxTokens) {
  if (!env.GEMINI_API_KEY) throw new Error("gemini_not_configured");
  const contents = messages
    .filter((m) => m.role === "user" || m.role === "assistant")
    .map((m) => ({ role: m.role === "assistant" ? "model" : "user", parts: toGeminiParts(m.content) }));
  const sys = messages.filter((m) => m.role === "system").map((m) => m.content).join("\n");
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${env.GEMINI_API_KEY}`;
  const payload = {
    contents,
    generationConfig: { maxOutputTokens: maxTokens, temperature: 0.7 },
  };
  if (sys) payload.systemInstruction = { parts: [{ text: sys }] };
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(`gemini_${res.status}`);
  const data = await res.json();
  return data.candidates?.[0]?.content?.parts?.[0]?.text ?? "";
}

// ---- Konverziós segédfüggvények (vision) ----

/**
 * OpenAI content (string vagy content-array) → Anthropic content-array.
 * image_url block: { type:"image_url", image_url:{ url:"data:image/jpeg;base64,..." } }
 *   → { type:"image", source:{ type:"base64", media_type:"image/jpeg", data:"..." } }
 */
function toAnthropicContent(content) {
  if (typeof content === "string") return content;
  if (!Array.isArray(content)) return String(content);
  return content.map((block) => {
    if (block.type === "text") return { type: "text", text: block.text };
    if (block.type === "image_url") {
      const url = block.image_url?.url ?? "";
      const match = url.match(/^data:([^;]+);base64,(.+)$/s);
      if (match) {
        return { type: "image", source: { type: "base64", media_type: match[1], data: match[2] } };
      }
      // URL (nem base64) → Anthropic nem támogatja URL-alapú képet; küldjük szövegként
      return { type: "text", text: `[image: ${url}]` };
    }
    return block;
  });
}

/**
 * OpenAI content (string vagy content-array) → Gemini parts-array.
 * image_url block → { inlineData:{ mimeType:"image/jpeg", data:"..." } }
 */
function toGeminiParts(content) {
  if (typeof content === "string") return [{ text: content }];
  if (!Array.isArray(content)) return [{ text: String(content) }];
  return content.map((block) => {
    if (block.type === "text") return { text: block.text };
    if (block.type === "image_url") {
      const url = block.image_url?.url ?? "";
      const match = url.match(/^data:([^;]+);base64,(.+)$/s);
      if (match) {
        return { inlineData: { mimeType: match[1], data: match[2] } };
      }
      return { text: `[image: ${url}]` };
    }
    return { text: JSON.stringify(block) };
  });
}

// ---- Segédfüggvények ----

function availableProviders(env) {
  const list = [];
  if (env.OPENAI_API_KEY) list.push("openai");
  if (env.GEMINI_API_KEY) list.push("gemini");
  if (env.ANTHROPIC_API_KEY) list.push("anthropic");
  return list;
}

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json", "Cache-Control": "no-store" },
  });
}

function clampInt(v, min, max, fallback) {
  const n = parseInt(v, 10);
  if (Number.isNaN(n)) return fallback;
  return Math.min(max, Math.max(min, n));
}

/** Időállandó string-összehasonlítás (timing-attack ellen). */
function timingSafeEqual(a, b) {
  if (typeof a !== "string" || typeof b !== "string") return false;
  if (a.length !== b.length) return false;
  let result = 0;
  for (let i = 0; i < a.length; i++) {
    result |= a.charCodeAt(i) ^ b.charCodeAt(i);
  }
  return result === 0;
}
