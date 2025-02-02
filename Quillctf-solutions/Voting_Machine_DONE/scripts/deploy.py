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
