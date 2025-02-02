pragma solidity ^0.8.19;

contract NFT {
    uint256 private _id; // STORAGE[0x0]
    mapping(uint256 => uint256) private _tokens; // STORAGE[0x1]

    receive() external payable {
        revert();
    }

    function mint() public payable {
        require(1 ether == msg.value, "show me the money");
        require(_id <= 1 + _id, "Panic(17)"); // arithmetic overflow or underflow
        _id += 1;
        require(100 != 0, "Panic(18)"); // division by zero
        uint256 v0;
        uint256 v1;
        uint256 v2;
        uint256 v3;
        if (
            uint256(
                keccak256(abi.encodePacked(_id, msg.sender, block.number))
            ) %
                100 <=
            90
        ) {
            if (
                uint256(
                    keccak256(abi.encodePacked(_id, msg.sender, block.number))
                ) %
                    100 <=
                80
            ) {
                v0 = v1 = 1;
            } else {
                v0 = v2 = 2;
            }
        } else {
            v0 = v3 = 3;
        }
        _tokens[_id] = v0;
    }

    function tokens(uint256 _id) public view returns (uint256) {
        require(msg.data.length - 4 >= 32);
        return _tokens[_id];
    }

    function id() public view returns (uint256) {
        return _id;
    }
}
