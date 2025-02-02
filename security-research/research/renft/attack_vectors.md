## Attack Vectors

#### Potential attacks
- Rental wallet security: assets should be able to be securely rented out by ensuring rental wallets cannot move rented assets freely.
- Improper rental creation: when a rental is created, it is first transferred to the rental wallet during its seaport processing, then the rental is handled by the protocol and logged in storage. Consider if the rental identifier can be prevented from being stored, resulting in the protocol not knowing of the rental, and as a result, not imposing contraints on it.
- Improper rental termination: when a rental has expired, any address can initiate the reclaim process to direct the assets back to the lender. Consider if invariants can be broken to result in lenders not receiving their expected assets or payments are sent to the wrong address or incorrect amounts.

