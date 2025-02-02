## Storage

#### Overview
- Some Solidity variables are placed in storage. 
```
contract StorageExample {
  uint256 x; // Gets stored in first available storage slot.

  constructor() {
    x = 1; // Value gets stored in slot as its hex representation.
  }
}
```
- In storage, each slot is 32 bytes long and represents the bytes version of the object being stored.
- Storage is best though of as an array with slots numbering from 0, 1, 2...
- Items are placed in storage at the first available slot e.g. 0th slot, 1st slot etc.
- Anytime a !storage! variable is saved, it takes up another slot in storage.
- Global variables are an example of a variable type that take up a slot in storage.
- For dynamic values like mappings and dynamic arrays, the elements are stored using a hashing function.
- For arrays, a sequential slot is taken for the length of the array.
- For mappings, a sequential slot is taken, but left blank.
- More simply, when an array is placed into storage, it takes a slot, but only the length of the array is saved in the storage slot.
- Constants and immutable variables do not take up a storage slot.
- This is because constant variables are part of the contracts bytecode itself.
- The constant variable name can be thought of as a direct pointer to the value itself.
- Function variables are not persistent, they are impermanent, they only exist for the duration of the function itself.
- As such, function vars are not placed in storage, instead, they are placed into their own memory data structure and deleted post-function run.
