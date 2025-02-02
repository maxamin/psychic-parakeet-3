import { load, app } from "./deps.ts";

// first try to load .env file if fails try to load from process env
const env = await load();
let port = env["PORT"];
if (!port) port = Deno.env.get("PORT") || "8080";

console.log(`Listening on port ${port}...`);

await app.listen({
  hostname: "::",
  port: +port,
});
