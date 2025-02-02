import { Logger as InternalAxiomLogger } from 'next-axiom';
import { config } from './config.js';

const logger = new InternalAxiomLogger({
	source: 'signum-activation-service',
	autoFlush: true
});

interface LogObject {
	msg: string;
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	[p: string]: any;
}

function log({ msg = '', ...args }: LogObject) {
	logger.info(msg, args);
}

function verbose({ msg = '', ...args }: LogObject) {
	if (config.verboseLog) {
		logger.debug(msg, args);
	}
}

function logError({ msg, ...obj }: LogObject) {
	logger.error(msg, obj);
}

function flush() {
	return logger.flush();
}

export const Logger = {
	logError,
	log,
	verbose,
	flush
};
