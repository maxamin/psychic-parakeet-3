## Fallback & Receive

#### Overview
- By default, Solidity smart contracts reject any eth sent to them.
- Conditionally, fallback() vs receive() will be called.
- Which is called is dependant on the conditions met/unmet.
- Without these functions, a contract cannot accept eth.
