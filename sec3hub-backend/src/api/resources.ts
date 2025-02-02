import { Router, ResourceService } from "../deps.ts";

const resourcesRouter = new Router();

resourcesRouter.get("/", async (ctx) => {
  // get the query string parameters for page and limit
  const page = ctx.request.url.searchParams.get("page");
  const limit = ctx.request.url.searchParams.get("limit");
  const sort = ctx.request.url.searchParams.get("sort");
  const keyword = ctx.request.url.searchParams.get("keyword");
  const order = ctx.request.url.searchParams.get("order");

  // sort allowed values whitelist
  const sortAllowedValues = ["id", "created_at", "up_votes", "name"];

  // allowed orders
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

  // get the rekts from the database
  const resources = await ResourceService.getResources(
    page ? parseInt(page) : 1,
    limit ? parseInt(limit) : 10,
    sort ? sort : "id",
    keyword ? keyword.toLowerCase() : "",
    order ? order : "asc"
  );

  const resourceCount = await ResourceService.getTotalResources();

  // set the response headers
  const total = resourceCount[0].count.toString();
  ctx.response.headers.set("X-Row-Count", total);

  if (keyword) {
    const resources = await ResourceService.getTotalKeywordHits(keyword);
    const total = resources[0].count.toString();
    ctx.response.headers.set("X-Keyword-Count", total);
  }

  // set the response body
  ctx.response.body = resources;
});

// stats
resourcesRouter.get("/stats", async (ctx) => {
  const stats = await ResourceService.getStats();
  ctx.response.body = stats;
});

export { resourcesRouter };
