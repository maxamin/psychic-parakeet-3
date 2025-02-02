import app from "../src/app.ts";
import { superoak } from "superoak";

Deno.test("GET request to / should return 'backbone!'", async () => {
  const testClient = await superoak(app);
  await testClient.get("/").expect("Sec3hub.xyz Backbone!");
});
