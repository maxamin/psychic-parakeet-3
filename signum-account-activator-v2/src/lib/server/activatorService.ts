import { generateMasterKeys, getAccountIdFromPublicKey } from '@signumjs/crypto';
import { Amount } from '@signumjs/util';
import { AttachmentMessage, Address, LedgerClientFactory, type Ledger } from '@signumjs/core';
import { config } from './config';
import { Logger } from './logger';

const WelcomeMessage =
	'Welcome to the Signum Network.👋 A truly decentralized, public, and sustainable blockchain platform. Have a look at https://docs.signum.network/ecosystem to find out more.';

process.env.NODE_TLS_REJECT_UNAUTHORIZED = config.isTestnet ? '0' : '1';

export class ActivatorService {
	constructor(private signumApi: Ledger) {
		this.__validateAddressKeyPair = this.__log(this.__validateAddressKeyPair);
		this.__getSenderCredentials = this.__log(this.__getSenderCredentials);
		this.__validatePendingActivation = this.__log(this.__validatePendingActivation);
		this.__validateAccount = this.__log(this.__validateAccount);
		this.__sendWelcomeMessage = this.__log(this.__sendWelcomeMessage);
		this.__sendWelcomeMessageWithAmount = this.__log(this.__sendWelcomeMessageWithAmount);
		this.activate = this.__log(this.activate);
	}

	// eslint-disable-next-line @typescript-eslint/ban-types
	__log(fn: Function) {
		return function () {
			const ctx = `ActivatorService.${fn.name}`;
			// eslint-disable-next-line prefer-rest-params
			const args = arguments;
			Logger.verbose({
				msg: 'ActivatorService',
				ctx,
				args
			});
			try {
				// @ts-ignore
				return fn.apply(this, args);
			} catch (e: any) {
				Logger.verbose({ msg: e.message, ctx, args, err: e.message });
				throw e;
			}
		};
	}

	__validateAddressKeyPair(accountId: string, publicKey: string) {
		const verifiedAccountId = getAccountIdFromPublicKey(publicKey);
		if (verifiedAccountId !== accountId) {
			throw new Error('Account Id does not match Public Key');
		}
	}

	__getSenderCredentials() {
		const keys = generateMasterKeys(config.accountSecret || '');
		return {
			id: getAccountIdFromPublicKey(keys.publicKey),
			...keys
		};
	}

	async __validatePendingActivation(recipientId: string) {
		const { id: senderId } = this.__getSenderCredentials();
		const { unconfirmedTransactions } =
			await this.signumApi.account.getUnconfirmedAccountTransactions(senderId, false);
		if (unconfirmedTransactions.some(({ recipient }) => recipient === recipientId)) {
			throw new Error('Activation is pending');
		}
	}

	async __validateAccount(accountId: string) {
		try {
			const { publicKey } = await this.signumApi.account.getAccount({ accountId });
			if (publicKey) {
				throw new Error('The account is already active');
			}
		} catch (e: any) {
			if (!e.data) {
				throw e;
			}
			const { errorDescription } = e.data;
			if (errorDescription === 'Unknown account') {
				// ok, ignore this
			} else {
				throw e;
			}
		}
	}

	async __sendWelcomeMessage(accountId: string, publicKey: string) {
		const { signPrivateKey, publicKey: senderPublicKey } = this.__getSenderCredentials();
		const feePlanck = Amount.fromSigna(config.activationFee).getPlanck();
		const sendMessageArgs = {
			message: WelcomeMessage,
			feePlanck,
			recipientId: accountId,
			recipientPublicKey: publicKey,
			senderPrivateKey: signPrivateKey,
			senderPublicKey: senderPublicKey
		};
		await this.signumApi.message.sendMessage(sendMessageArgs);
	}

	async __sendWelcomeMessageWithAmount(accountId: string, publicKey: string, amountPlanck: string) {
		const { signPrivateKey, publicKey: senderPublicKey } = this.__getSenderCredentials();
		const feePlanck = Amount.fromSigna(config.activationFee).getPlanck();
		const attachment = new AttachmentMessage({
			messageIsText: true,
			message: WelcomeMessage
		});

		const args = {
			amountPlanck,
			attachment,
			feePlanck,
			recipientId: accountId,
			recipientPublicKey: publicKey,
			senderPrivateKey: signPrivateKey,
			senderPublicKey: senderPublicKey
		};
		await this.signumApi.transaction.sendAmountToSingleRecipient(args);
	}

	async activate(account: string, publicKey: string) {
		const accountId = Address.create(account).getNumericId();
		await this.__validateAddressKeyPair(accountId, publicKey);
		await this.__validateAccount(accountId);
		await this.__validatePendingActivation(accountId);
		if (config.activationAmount === 0) {
			await this.__sendWelcomeMessage(accountId, publicKey);
		} else {
			const amount = Amount.fromSigna(config.activationAmount);
			await this.__sendWelcomeMessageWithAmount(accountId, publicKey, amount.getPlanck());
		}
	}
}

const ledger = LedgerClientFactory.createClient({
	nodeHost: config.nodeHosts || ''
});
export const activatorService = new ActivatorService(ledger);
