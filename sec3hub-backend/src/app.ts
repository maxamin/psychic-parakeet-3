import {
  Application,
  Router,
  auditorsRounter,
  rektsRouter,
  productsRouter,
  resourcesRouter,
  votesRouter,
  oakCors,
} from "./deps.ts";

const app = new Application();

// Base route
const router = new Router();
router.get("/", (ctx) => {
  ctx.response.body = "Sec3hub.xyz Backbone!";
});

// Auditors route
router.use("/auditors", auditorsRounter.routes());
// Rekts route
router.use("/rekts", rektsRouter.routes());
// Products route
router.use("/products", productsRouter.routes());
// Resources route
router.use("/resources", resourcesRouter.routes());
// Vote route
router.use("/vote", votesRouter.routes());
app.use(votesRouter.allowedMethods());

// set up CORS to allow requests
app.use(oakCors());

// for x-row-count header
app.use((ctx, next) => {
  // Keyword-Count and X-Row-Count
  ctx.response.headers.set(
    "Access-Control-Expose-Headers",
    "X-Row-Count, X-Keyword-Count"
  );
  return next();
});

app.use(router.routes());
app.use(router.allowedMethods());

export default app;
