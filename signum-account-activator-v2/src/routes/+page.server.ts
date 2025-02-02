import type { PageServerLoad } from '../../.svelte-kit/types/src/routes/$types';
import { config } from '$lib/server';

export const load: PageServerLoad = ({ url }) => {
	const account = url.searchParams.get('account');
	const publickey = url.searchParams.get('publickey');
	return {
		account,
		publickey,
		isTestnet: config.isTestnet
	};
};
