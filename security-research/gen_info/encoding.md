## Encoding

#### Overview
- abi.encodePacked enables encoding and decoding of entities like strings into and from machine readble binary/hex/bytecode.
- EVM chains are looking for the bytecode of contracts.
- Transactions are compiled down to binary.
- Data piece in transactions is what to send to a given address.
- Using encoding, the data field of a transaction call can be manually designed and sent.
- In other words, the data field can be populated with bytecode that defines the exact instructions we wish to execute.
- Because of this, you don't need an abi etc.
- Additionally, very complex executions can be requested in a transaction.
- Call allows us to call functions and as a result, change the state of the blockchain.
- Staticall is a low-level way to perform view or pure function calls. 
