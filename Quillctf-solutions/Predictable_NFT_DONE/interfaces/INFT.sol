// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

interface INFT {
    function mint() external payable;

    function tokens(uint256 _id) external view returns (uint256);

    function id() external view returns (uint256);
}
