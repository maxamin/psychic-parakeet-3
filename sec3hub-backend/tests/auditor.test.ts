import app from "../src/app.ts";
import { superoak } from "superoak";
import * as sinon from "sinon";
import { AuditorsService } from "../src/deps.ts";

Deno.test("Can get auditors", async () => {
  const testClient = await superoak(app);

  // mock database call in auditors.ts
  const stubbedAuditorsService = sinon.stub(AuditorsService, "getAuditors");
  stubbedAuditorsService.returns([
    {
      id: 1,
      name: "test",
      type: "test",
      total_audits: 1,
      up_votes: 1,
      location: "test",
      start_year: 1,
      created_at: "test",
      services: ["test"],
    },
  ]);

  // mock total auditors count
  const getTotalAuditors = sinon.stub(AuditorsService, "getTotalAuditors");
  getTotalAuditors.returns([{ count: 1 }]);

  // check that the response is correct
  await testClient
    .get("/auditors")
    .expect(200)
    .expect([
      {
        id: 1,
        name: "test",
        type: "test",
        total_audits: 1,
        up_votes: 1,
        location: "test",
        start_year: 1,
        created_at: "test",
        services: ["test"],
      },
    ]);

  // restore the stubbed functions
  stubbedAuditorsService.restore();
  getTotalAuditors.restore();
});

Deno.test("Can get auditors with page and limit", async () => {
  const testClient = await superoak(app);

  // mock database call in auditors.ts
  const stubbedAuditorsService = sinon.stub(AuditorsService, "getAuditors");
  stubbedAuditorsService.returns([
    {
      id: 1,
      name: "test",
      type: "test",
      total_audits: 1,
      up_votes: 1,
      location: "test",
      start_year: 1,
      created_at: "test",
      services: ["test"],
    },
  ]);

  // mock total auditors count
  const getTotalAuditors = sinon.stub(AuditorsService, "getTotalAuditors");
  getTotalAuditors.returns([{ count: 1 }]);

  // check that the response is correct
  await testClient
    .get("/auditors?page=1&limit=10")
    .expect(200)
    .expect([
      {
        id: 1,
        name: "test",
        type: "test",
        total_audits: 1,
        up_votes: 1,
        location: "test",
        start_year: 1,
        created_at: "test",
        services: ["test"],
      },
    ]);

  // restore the stubbed functions
  stubbedAuditorsService.restore();
  getTotalAuditors.restore();
});

Deno.test("Can get auditors search by keyword", async () => {
  const testClient = await superoak(app);

  // mock database call in auditors.ts
  const stubbedAuditorsService = sinon.stub(AuditorsService, "getAuditors");
  stubbedAuditorsService.returns([
    {
      id: 1,
      name: "test",
      type: "test",
      total_audits: 1,
      up_votes: 1,
      location: "test",
      start_year: 1,
      created_at: "test",
      services: ["test"],
    },
  ]);

  // mock total keyword hits
  const getTotalKeywordHits = sinon.stub(
    AuditorsService,
    "getTotalKeywordHits"
  );
  getTotalKeywordHits.returns([{ count: 1 }]);

  // mock total auditors count
  const getTotalAuditors = sinon.stub(AuditorsService, "getTotalAuditors");
  getTotalAuditors.returns([{ count: 1 }]);

  // check that the response is correct with a X-Keyword-Count header set to 1
  await testClient
    .get("/auditors?keyword=test")
    .expect(200)
    .expect([
      {
        id: 1,
        name: "test",
        type: "test",
        total_audits: 1,
        up_votes: 1,
        location: "test",
        start_year: 1,
        created_at: "test",
        services: ["test"],
      },
    ])
    .expect("X-Keyword-Count", "1");

  // restore the stubbed functions
  stubbedAuditorsService.restore();
  getTotalAuditors.restore();
});
