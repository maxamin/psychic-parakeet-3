const { GENESIS_DATA } = require('./config.js');
const createHash = require('./crypto.js')

class Block {
    constructor({ timestamp, previousHash, hash, data }) {
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.hash = hash;
        this.data = data;
    }

    // to create genesis block at start of the blockchain
    static createGenesisBlock() {
        return new this(GENESIS_DATA);
    }

    // to create new block in the blockchain
    static mineBlock({ previousBlock, data }) {
        const timestamp = Date.now();
        const previousHash = previousBlock.hash;
        const block = new this({
            timestamp,
            previousHash,
            hash: createHash(timestamp, previousHash, data),
            data
        });
        return block;
    }
}
// console.log(Block.genesisBlock());
// console.log(Block.mineBlock({previousBlock: Block.genesisBlock(), data: "second block"}));

module.exports = Block;