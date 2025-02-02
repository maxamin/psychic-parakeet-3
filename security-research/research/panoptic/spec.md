## Panoptic

#### Overview
- "SemiFungiblePositionManager" a gas-efficient alternative to Uniswap's NFPM. 
- Manages complex, multi-leg swap positions.
- Positions are encoded in ERC1155 tokenids.
- Performs swaps allowing users to mint positions with a single token type.
- Critically, it supports minting of both typical LP positions.
- Standard LP positions where liquidity is added to Uniswap and long positions where liquidity is burned.
- Scoped code represents a subsection of the POp v1 protocol, but also serves as a standalone liquidity manager open for use by any entity.

#### General info
- Multi-leg strat: A strat that involves more than one trading action or order. e.g. instead of a singular transaction, a multi-leg position combines several trades into one composite strategy.
- In multi-leg strats, each trade//leg plays a unique role.
- The combined effect of the trades is designed to achieve a specific goal. e.g. Long straddle.
- Long straddle: Buy a call option, and buy a put option in one composite execution. This is a multi-leg strat because it involves two distinct operations being combined and executed as a singular entity.
- ERC-1155: Can represent both fungible tokens and NFT's, enables batch transfers of multiple token types in a single transaction, tokens are identified by a unique ID and stored together in a single contract unlike ERC-721 where each NFT is a separate contract, can embody conditional fungibility.
- LP positions (adding liquidity): Providing liquidity to Uniswap by minting a new ERC-1155 representing a share of the LP pool.
- Long positions (burning liquidity): taking a long position in expectation of asset value increase. Uniswap liquidity is burned?
- SFPM: engine that manages all USv3 positions.

#### Function descs
- F1/F2: Reentrancy lock.
- F3: Initializes USv3 AMM pool in contract. Builds USv3 pool SFPM contract reverting if already init.
- F4: USv3 mint callback. Called after minting liquidity to a position.
- F5: USv3 swap callback. Called by pool after executing a swap during an ITM option mint/burn.
- F6: Burn tokenized position. Burns a new position containing up to four legs wrapped in an ERC-1155.
- F7: Mint tokenized position. Creates a new position tokenID containing up to four legs.
- F8: After token transfer. Called after batch transfers. Not mints or burns.
- F9: After token transfer 2. Called after single transfers. Also not mints or burns.
- F10: Register token transfer. Updates user position data after a token transfer.
- F11: Validate and forward to AMM. Checks proposed options positions and size and forwards the minting and potential swapping tasks. Mint & Burn tokenized positions feed into validate and forward to AMM.
- F12: Swap in AMM. When a position is minted or burned in-the-money (ITM) status is 'not' 100% token0 nor 100% token1. Status is a mix of both. Swapping for ITM options is needed because only one of the tokens are 'borrowed' by a user to create the position.
- F13: Create position in AMM. Create the position in the AMM given the tokenId.
- F14: Create leg in AMM. Create the position in the AMM for a specific leg in the tokenId.
- F15: Update stored permia. Caches/stores the accumulated premia values for the specified position.
- F16: Get base fees. Computes the feesGrowth by reading from USv3 pool positions.
- F17: Mint liquidity. Mints a chunk og liquidity in the USv3 pool.
- F18: Burn liquidity. Burns a chunk of liquidity in the USv3 pool and sends to msg.sender.
- F19: Collect and write position data. Helper to collect amounts between msg.sender and USwp and also to update USwp fees collected to date from the AMM.
- F20: Get premia deltas. Updates gas owed and gross account liquidities.
- F21: Get account liquidity. Return the liquidity associated with a given position.
- F22: Get account premium. Return the premium associated with a given position, where premium is an accumulator of feeGrowth for the touched position.
- F23: Get account fees base. Return the feesBase associated with a given position.
- F24: Get USv3 pool from id. Returns the USv3 pool for a given poolId.
- F25: Get pool id. Returns the poolId for a given USv3 pool.

#### Flow
- Options buyer/seller(mintOption/burnOption)(deposit/withdraw), Panoptic LP(deposit/withdraw)(delegate/revoke), Liquidator(forceExercise/liquidateAccount) -> POp pool(Deployed by: PanopticFactory.sol)(Collat tracking: ReceiptBase.sol). 
- POp pool(mintTokenizedPosition/burnTokenizedPosition) -> SFPM(ERC-1155).
- SFPM(mint/burn/collect) -> USv3 pool.
- SFPM <- UniV3 LP(mintTokenizedPosition/burnTok...).

#### Flow p2
- UniV3 LP(mint/collect, increaseLiquidity/decrLiq...) -> Uni NFPM(ERC-721).
- Uni NFPM(mint, burn, collect) -> USv3 pool.
