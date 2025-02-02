import { Router, ProductsService } from "../deps.ts";

const productsRouter = new Router();

productsRouter.get("/", async (ctx) => {
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
    "up_votes",
    "is_commercial",
    "is_open_source",
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

  const products = await ProductsService.getProducts(
    page ? parseInt(page) : 1,
    limit ? parseInt(limit) : 10,
    sort ? sort : "id",
    keyword ? keyword.toLowerCase() : "",
    order ? order : "asc"
  );

  const productsCount = await ProductsService.getTotalProducts();
  const total = productsCount[0].count.toString();
  ctx.response.headers.set("X-Row-Count", total);

  if (keyword) {
    const products = await ProductsService.getTotalKeywordHits(keyword);
    const total = products[0].count.toString();
    ctx.response.headers.set("X-Keyword-Count", total);
  }

  // patch for now better approach is to map to type in the database
  const productsWithTypes = products.map((product) => {
    return {
      ...product,
      up_votes: parseInt(product.up_votes),
    };
  });

  // set the response body
  ctx.response.body = productsWithTypes;
});

// stats
productsRouter.get("/stats", async (ctx) => {
  const stats = await ProductsService.getStats();
  ctx.response.body = stats;
});

export { productsRouter };
