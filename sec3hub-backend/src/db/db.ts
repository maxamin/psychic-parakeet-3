import postgres from "postgres";
import { load } from "std/dotenv/mod.ts";

const env = await load();
let DATABASE_URL = env["DATABASE_URL"];

if (!DATABASE_URL) DATABASE_URL = Deno.env.get("DATABASE_URL") || "";

// should not be empty
if (DATABASE_URL === "") console.log("DATABASE_URL is empty!!");

const sql = postgres(DATABASE_URL);

export { sql };

//check if connection is successful

// const testConnection = async () => {
//   try {
//     await sql`SELECT 1 + 1`;
//     console.log("Connection to db successful");
//     return true;
//   } catch {
//     console.log("Connection to db failed");
//     return false;
//   }
// };
