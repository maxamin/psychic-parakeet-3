## Access Control
- Access control in a given contract constitutes who is allowed to perform a given action.
- The most common form of access control is the concept of ownership wherein an account is defined as the owner of a contract and can perform administrative tasks on it.
- By default, the owner of an OZep Ownable contract is the account that deployed it.
- OZep Ownable enables ownership transfers and the renunciation of ownership.
- Contracts can also own another contract, enabling composability to add layers of access control complexity.
- By not placing restrictions on who can call a sensitive function, like withdrawals or ownership changes, can make a contract vulnerable to exploitation.  
