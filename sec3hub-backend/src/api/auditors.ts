import { Router, AuditorsService } from "../deps.ts";

const auditorsRounter = new Router();

auditorsRounter.get("/", async (ctx) => {
  // get the query string parameters for page and limit
  const page = ctx.request.url.searchParams.get("page");
  const limit = ctx.request.url.searchParams.get("limit");
  const sort = ctx.request.url.searchParams.get("sort");
  const keyword = ctx.request.url.searchParams.get("keyword");
  const order = ctx.request.url.searchParams.get("order");

  // sort allowed values whitelist
  const sortAllowedValues = [
    "id",
    "name",
    "type",
    "total_audits",
    "up_votes",
    "location",
    "start_year",
    "created_at",
  ];

  const allowedOrders = ["asc", "desc"];

  // validate order value
  if (order && !allowedOrders.includes(order)) {
    ctx.response.status = 400;
    ctx.response.body = {
      error: "invalid order value",
    };
    return;
  }

  // validate sort value
  if (sort && !sortAllowedValues.includes(sort)) {
    ctx.response.status = 400;
    ctx.response.body = {
      error: "invalid sort value",
    };
    return;
  }

  // validate page and limit
  try {
    if (page && isNaN(parseInt(page))) throw new Error("invalid page");
    if (limit && isNaN(parseInt(limit))) throw new Error("invalid limit");
  } catch {
    ctx.response.status = 400;
    ctx.response.body = "invalid page or limit";
    return;
  }

  // get the auditors from the database
  const auditors = await AuditorsService.getAuditors(
    page ? parseInt(page) : 1,
    limit ? parseInt(limit) : 10,
    sort ? sort : "id",
    keyword ? keyword.toLowerCase() : "",
    order ? order : "asc"
  );

  const auditorsCount = await AuditorsService.getTotalAuditors();
  const total = auditorsCount[0].count.toString();
  ctx.response.headers.set("X-Row-Count", total);

  if (keyword) {
    const auditors = await AuditorsService.getTotalKeywordHits(keyword);
    const total = auditors[0].count.toString();
    ctx.response.headers.set("X-Keyword-Count", total);
  }

  // patch for now better approach is to map to type in the database
  const auditorsWithIntUpVotes = auditors.map((auditor) => {
    return {
      ...auditor,
      up_votes: parseInt(auditor.up_votes),
    };
  });

  ctx.response.body = auditorsWithIntUpVotes;
});

auditorsRounter.get("/stats", async (ctx) => {
  const stats = await AuditorsService.getStats();

  ctx.response.body = stats;
});

export { auditorsRounter };
