import { error, type RequestHandler } from '@sveltejs/kit';
import { Address } from '@signumjs/core';
import { activatorService, Logger } from '$lib/server';

export const POST: RequestHandler = async ({ request }) => {
	const body = await request.json();

	let { account, publickey } = body;
	if (!publickey) {
		try {
			const address = Address.create(account);
			account = address.getNumericId();
			publickey = address.getPublicKey();
		} catch (e) {
			const status = 400;
			const msg = 'Field [account] is not a valid Signum address.';
			Logger.logError({ msg, status });
			throw error(status, msg);
		} finally {
			await Logger.flush();
		}
	}

	if (account && publickey) {
		try {
			await activatorService.activate(account, publickey);
			Logger.log({
				success: true,
				...body
			});
			return new Response(null, { status: 204 });
		} catch (e: any) {
			const status = 400;
			const msg = e.message;
			Logger.logError({ msg, status });
			throw error(status, msg);
		} finally {
			await Logger.flush();
		}
	} else {
		const msg = 'Missing fields [account] and/or [publickey]';
		const status = 400;
		Logger.logError({ msg, status });
		await Logger.flush();
		throw error(status, msg);
	}
};
