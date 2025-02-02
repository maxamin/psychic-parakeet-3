## Self destruct

#### Overview
- Is a keyword in Solidity that will destroy/delete a contract.
- Any eth inside of a given contract will be pushed into the address specified as an argument to the keyword.
```
contract Example {
  selfdestruct(addr);
}
```
- If there is no receive or fallback function in a given contract, selfdestruct can potentially be used to force eth into a contract. 
