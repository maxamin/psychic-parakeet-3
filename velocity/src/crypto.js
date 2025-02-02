const crypto = require('crypto');

const createHash = (...data) => {
    console.log(data.sort().join(''));
    return crypto.createHash('sha256').update(data.sort().join('')).digest('hex');
}

module.exports = createHash;