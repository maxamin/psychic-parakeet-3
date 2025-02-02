import { config as loadConfig } from 'dotenv';

if (process.env.NODE_ENV === 'development') {
	loadConfig();
}

const isTrue = (bool: string) => bool === 'true';

// const array = (csl: string) => (csl ? csl.split(',') : [])
export const config = {
	nodeHosts: process.env.SIGNUM_NODE,
	donationAccount: process.env.DONATION_ACCOUNT,
	accountSecret: process.env.ACTIVATOR_ACCOUNT_SECRET,
	activationAmount: parseFloat(process.env.ACTIVATION_AMOUNT || '0.01'),
	activationFee: parseFloat(process.env.ACTIVATION_FEE || '0.02'),
	isTestnet: isTrue(process.env.TEST_NET || 'true'),
	webUiAvailable: isTrue(process.env.WEB_UI || 'true'),
	verboseLog: isTrue(process.env.VERBOSE_LOG || 'true'),
	redisUrl: process.env.REDIS_URL,
	logzIOApiKey: process.env.LOGZ_IO_SHIPPING_TOKEN
};
