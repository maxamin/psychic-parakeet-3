#!/usr/bin/python3
from brownie import PrivateClub, web3
from scripts.helpful_scripts import get_account
import datetime


def convert(amount):
    return web3.fromWei(amount, "ether")


def deploy():
    owner, ownerFriend, user2, user3, user4, attacker = get_account()
    print(convert(owner.balance()))
    

    # inital eth is 1000. so transfer 990 eth to the owner so that everyone has only 10 ETH each
    user2.transfer(owner, "990 ether")
    user3.transfer(owner, "990 ether")
    user4.transfer(owner, "990 ether")
    attacker.transfer(owner, "990 ether")
    # print(convert(owner.balance()))

    # print(
    #     convert(user2.balance()),
    #     convert(user3.balance()),
    #     convert(user4.balance()),
    #     convert(attacker.balance()),
    # )
    # exit()

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
    # print(pc.balance())

    # user1 becoming member
    _members = [ownerFriend.address]
    pc.becomeMember(_members, {"from": user2, "value": "1 ether"}).wait(1)

    # print(web3.eth.getBlock("latest").gasUsed)

    # user2 becoming member
    _members = [ownerFriend.address, user3.address]
    pc.becomeMember(_members, {"from": user3, "value": "2 ether"}).wait(1)

    # print(web3.eth.getBlock("latest").gasUsed)

    # # user3 becoming member
    # _members = [ownerFriend.address, user1.address, user2.address]
    # pc.becomeMember(_members, {"from": user3, "value": "3 ether"}).wait(1)

    return pc, ownerFriend, user2, user3, user4, attacker


def main():
    deploy()
