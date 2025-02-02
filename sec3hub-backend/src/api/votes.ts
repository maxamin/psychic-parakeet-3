import { Router, verifyMessage, VotesService } from "../deps.ts";

const votesRouter = new Router();

votesRouter.post("/", async (ctx) => {
  const result = await ctx.request.body().value;

  const { pub, msg, sig, id, type } = result;

  // type can be only product or auditor
  if (type !== "product" && type !== "auditor" && type !== "resource") {
    ctx.response.status = 400;
    ctx.response.body = {
      error: "invalid type",
    };
    return;
  }

  // validate auditor/product id to be a number
  if (isNaN(parseInt(id))) {
    ctx.response.status = 400;
    ctx.response.body = {
      error: "invalid auditor id",
    };
    return;
  }

  // validate the signature
  const isVerified = await verifyMessage({
    address: pub,
    message: msg,
    signature: sig,
  });

  if (!isVerified) {
    ctx.response.status = 400;
    ctx.response.body = {
      error: "invalid signature",
    };
    return;
  }

  // check if the user has already voted for this auditor
  const hasVoted = await VotesService.hasVoted(pub, parseInt(id), type);

  if (hasVoted) {
    ctx.response.status = 409;
    ctx.response.body = {
      error: "user has already voted for this auditor",
    };
    return;
  }

  // add the vote to the database
  const res = await VotesService.castVote(pub, parseInt(id), type);

  if (res) {
    ctx.response.body = {
      message: "vote added successfully",
    };
    return;
  }

  ctx.response.status = 500;
  ctx.response.body = {
    error: "insert failed",
  };
});

export { votesRouter };
