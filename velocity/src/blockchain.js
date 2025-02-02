const Block = require('./block');
const createHash = require('./crypto');

class Blockchain {
    constructor() {
        this.chain = [Block.createGenesisBlock()]; // initial block will be GenesisBlock
    }

    getLatestBlock() {
        return this.chain[this.chain.length - 1];
    }

    // to add new block in the chain connecting previous block
    addBlock({ data }) {
        // newBlock.previousHash = this.getLatestBlock().hash;
        // newBlock.hash = newBlock.createHash();
        const newBlock = Block.mineBlock({
            previousBlock: this.getLatestBlock(),
            data: data
        })
        this.chain.push(newBlock);
    }

    // to check if the chain is valid or not
    static isChainValid(chain) {
        if (chain.length < 1) {
            return false;
        }

        // if the genesis block within itself and in chain is valid or not
        const genesisBlock = Block.createGenesisBlock();
        const firstBlockInChain = chain[0];
        if (JSON.stringify(genesisBlock) !== JSON.stringify(firstBlockInChain)) {
            return false;
        }


        // check for rest of the blockchain
        for (let i = 1; i < chain.length; i++) {
            const currentBlock = chain[i];
            const previousBlock = chain[i - 1];

            // checking if blocks are connected 
            if (currentBlock.previousHash !== previousBlock.hash) {
                return false;
            }

            // checking if the block in itself is valid or not
            if (currentBlock.hash !== (createHash(currentBlock.timestamp, currentBlock.previousHash, currentBlock.data))) {
                return false;
            }

        }
        return true;
    }

    // to validate and replace the chain with incoming chain
    replaceChain(chain) {
        if (chain.length <= this.chain.length) {
            console.error("Incoming chain is not longer.");
            return;
        }

        if (!Blockchain.isChainValid(chain)) {
            console.error("Incoming chain is not valid.");
            return;
        }

        console.log("Replacing chain with incoming chain.");
        this.chain = chain;
    }
}

// const blockchain = new Blockchain();
// blockchain.addBlock({data:"blockchain data 1"})
// blockchain.addBlock({data:"blockchain data 2"})

// console.log(Blockchain.isChainValid(blockchain.chain));
// console.log(blockchain);
module.exports = Blockchain;