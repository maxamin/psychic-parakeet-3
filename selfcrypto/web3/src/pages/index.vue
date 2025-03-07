<template>
    <div>
        <div v-show="!showSpin">
            <WalletPanel ref="walletPanel" :onAccountChanged="onAccountChanged" />
            <TOTPPanel v-if="showTOTP" ref="totpPanel" :getSelf="getSelf"/>
            <HomePanel v-show="showHomePanel && !showTOTP" ref="privatePanel" :getSelf="getSelf"/>
            <CryptoPanel v-show="showPanels['cryptoPanel'] && !showTOTP" ref="cryptoPanel" :getSelf="getSelf"/>
        </div>
        <Spin size="large" fix v-if="showSpin"></Spin>
    </div>
</template>
<script>
import Web3 from "web3";
import TOTPPanel from './totp.vue';
import HomePanel from './home.vue';
import CryptoPanel from './crypto.vue';
import WalletPanel from './wallet.vue';
export default {
    components: {
        TOTPPanel,
        WalletPanel,
        HomePanel,
        CryptoPanel
    },
    inject: ["reload"],
    data() {
        return {
            connect: false,
            publicKey: '',
            walletAddress: '',

            showTOTP: false,
            justVerify: false,
            showHomePanel: true,
            showPanels: {},

            panelName: '',
            backendPublicKey: '',
            afterVerifyFunc: null,

            apiPrefix: '',
            loadRandom: '',
            loadSignature: '',
            showSpin: false
        }
    },
    methods: {
        getSelf() {
            return this;
        },
        enableSpin(status) {
            this.showSpin = status;
        },
        init() {
            let self = this;
            const go = new Go();
            this.enableSpin(true);
            WebAssembly.instantiateStreaming(fetch("selfcrypto.wasm"), go.importObject)
            .then(function(result) {
                console.log('load wasm successed: ', result)
                go.run(result.instance);
                self.loadRandom = self.generatekey(32, false);
                // self.sign(Web3.utils.soliditySha3("\x19Ethereum Signed Message:\n32", self.loadRandom), function(sig) {
                self.signTypedData(self.loadRandom, function(sig) {
                    console.log('sign successed: ', sig)
                    self.loadSignature = sig;
                    self.load();
                })
            })
        },
        onAccountChanged(action, network, address) {
            let self = this;
            if (action === 'connect') {
                this.connect = true;
                this.modelAuthID = address;
                this.walletAddress = address;
                this.init();
            } else if (action === 'disconnect') {
                this.connect = false;
                this.walletAddress = '';
            } else {
                window.location.reload();
            }
        },
        load() {
            let self = this;
            let initBackend = function(recoverID, backendKey, web3Key, web3PublicKey) {
                // wasm
                let response = {};
                Load(self.walletAddress, web3PublicKey, backendKey, function(wasmResponse) {
                    response['data'] = JSON.parse(wasmResponse);
                    if (response.data['Data'] !== null && response.data['Data'] !== undefined && response.data['Data'] !== {}) {
                        // self.wasmCallback("Load", '', false);
                        self.enableSpin(false);
                        self.backendPublicKey = response.data['Data'];
                        console.log('selfcrypto load from backend successed: ', response.data['Data']);
                        self.$refs.privatePanel.init(recoverID, web3Key, web3PublicKey);
                    } else {
                        self.wasmCallback("Load", response.data['Error'], false);
                    }
                });
            }

            var loadParams = [];
            loadParams.push(this.loadSignature);
            loadParams.push(Web3.utils.asciiToHex(this.loadRandom));
            self.$refs.walletPanel.Execute("call", "Load", self.walletAddress, 0, loadParams, function (result) {
                console.log('selfcrypto load from contract successed: ', result);
                self.$refs.privatePanel.hasRegisted = true;
                let web3Key = Web3.utils.hexToAscii(result['web3Key']);
                let recoverID = Web3.utils.hexToAscii(result['recoverID']);
                let backendKey = Web3.utils.hexToAscii(result['backendKey']);
                let web3PublicKey = Web3.utils.hexToAscii(result['web3PublicKey']);
                initBackend(recoverID, backendKey, web3Key, web3PublicKey);
            }, function (err) {
                self.$Message.error('selfCrypto load from contract failed');
                initBackend('', '', '', '');
            });
        },
        getWalletAddress() {
            return this.walletAddress;
        },
        getWallet() {
            return this.$refs.walletPanel;
        },
        switchPanel(action, panelName, panelInitParam, afterVerifyFunc) {
            if (action === 'back' || action === '') {
                this.showPanels[panelName] = false;
                this.showHomePanel = !this.showHomePanel;
                // this.reload();
                return;
            }
            this.panelName = panelName;
            this.afterVerifyFunc = afterVerifyFunc;

            let self = this;
            this.showTOTP = true;
            this.$nextTick(function(){
                self.$refs.totpPanel.init(action, self.backendPublicKey, panelInitParam);
            });
        },
        afterVerify(hasVerified, panelInitParam, optionPanelName) {
            this.showTOTP = false;
            if (hasVerified === true) {
                console.log('verify successed: ', this.panelName, optionPanelName);
                if (this.afterVerifyFunc !== null && this.afterVerifyFunc !== undefined) {
                    this.afterVerifyFunc(panelInitParam);
                    return;
                }
                if (this.panelName === '' && optionPanelName !== undefined) this.panelName = optionPanelName;
                this.showHomePanel = !this.showHomePanel;
                this.showPanels[this.panelName] = true;
                this.$refs[this.panelName].init(panelInitParam);
            }
        },
        signTypedData(msg, callback) {
            var msgParams = [
                {
                    type: 'string',
                    name: 'Message',
                    value: msg
                }
            ]
            
            let self = this;
            let from = this.getWalletAddress();
            var params = [msgParams, from];
            var method = 'eth_signTypedData';
            this.$refs.walletPanel.getWeb3().currentProvider.sendAsync({
                method,
                params,
                from,
            }, function (error, result) {
                if (error || result.error) {
                    self.$Message.error('sign message failed at web3: ', msg, error);
                    console.log('sign message failed at web3: ', msg, error)
                    self.enableSpin(false);
                    return
                }
                if (callback !== null && callback !== undefined) callback(result.result);
            })
        },
        generatekey(num, needNO) {
            let library = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            if (needNO === true) library = "0123456789";
            let key = "";
            for (var i = 0; i < num; i++) {
                let randomPoz = Math.floor(Math.random() * library.length);
                key += library.substring(randomPoz, randomPoz + 1);
            }
            return key;
        },
        wasmCallback(method, err, spinStatus) {
            if (spinStatus !== undefined) this.enableSpin(spinStatus);
            if (err === undefined || err === '') {
                this.$Message.success('exec wasm method successed: ' + method);                
            } else {
                console.log('exec wasm method failed: ', method + ", ", err);
                this.$Message.error('exec wasm method failed: ' + method + ", " + err);    
            }
        },
        httpGet(url, onResponse, onPanic) {
            this.$axios.get(this.apiPrefix + url)
                .then(function(response) {
                    if (onResponse !== undefined && onResponse !== null) onResponse(response);
                })
                .catch(function(e) {
                    console.log(e);
                });
        },
        httpPost(url, formdata, onResponse, onPanic) {
            this.$axios.post(this.apiPrefix + url, formdata)
                .then(function(response) {
                    if (onResponse !== undefined && onResponse !== null) onResponse(response);
                })
                .catch(function(e) {
                    console.log(e);
                });
        }
    }
}
</script>