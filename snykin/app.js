const express = require("express");
const app = express();
const port = 3000;

app.get("/", (req, res) => res.send("Hello World!"));

app.listen(port, () => console.log(`Example app listening on port ${port}!`));

exitOnSignal("SIGINT");
exitOnSignal("SIGTERM");

function exitOnSignal(signal) {
  process.on(signal, function() {
    process.exit(1);
  });
}
