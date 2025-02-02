#!/usr/bin/python3
from brownie import web3
from eth_abi import encode

encoded_data = encode(
    ["uint", "string", "address"],
    [1, "test1", "0x5B38Da6a701c568545dCfcB03FcB875f56beddC4"],
)

# 0x94822408407fb535100a4152f8709ed58507e1b3dfd32798d25051065f680d64
# 0x94822408407fb535100a4152f8709ed58507e1b3dfd32798d25051065f680d64

encoded_data = encode(
    ["uint256", "address", "uint256"],
    [2, "0x0000000000000000000000000000000000001337", 8948253],
)

print(web3.toInt(web3.keccak(encoded_data)))
print(web3.sha3(encoded_data).hex())
