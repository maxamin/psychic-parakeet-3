## reNFT

#### Overview
- The protocols purpose is the facilitation of generalized collateral-free NFT rentals built on top of Gnosis Safe and Seaport.

#### Illustrative example
- Imagine user X has gaming NFT's, X signs a seaport order typed data, signalling that they are willing to lend out the NFT's.  
- User Y wants to use the NFT's, so they find X's listing and rent one.
- Following, a gnosis safe is created for Y where the rented assets are sent.
- There exists a gnosis module that prevents Y from moving the assets out of their smart contract wallet.
- As a result of these established priors, Y can use the NFT's in their desired fashion e.g. in a game.

#### Architecture
- The protocols primary architecure is based on the default framework.
- https://github.com/fullyallocated/Default
- The contracts in scope can be categorized into the following four groups: modules, policies, packages, general.
- Modules are internal-facing contracts that store shared state across the protocol.
- Policies are external-facing contracts that receive inbound calls to the protocol, routing all the necessary updates to data models via modules.
- Packages are small helper contracts imported by core protocol contracts and are dedicated to performing a single task.
- General are gen-purpose contracts agnostic to the protocol's core functionality. 
