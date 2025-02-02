<script lang="ts">
	import { fade } from 'svelte/transition';
	import { Address } from '@signumjs/core';
	import { HttpClientFactory, HttpError } from '@signumjs/http';
	import { page } from '$app/stores';
	import Stamp from '$lib/components/Stamp.svelte';

	let { account, publickey, isTestnet } = $page.data;

	const ActivationState = {
		New: 0,
		Activated: 1,
		Failed: 2
	};

	const http = HttpClientFactory.createHttpClient('/api');
	const InitialTitle = 'Activate and protect your account';

	let error = '';
	let isLoading = false;
	let activationState = ActivationState.New;
	let title = InitialTitle;
	$: canActivate = !isLoading && account && publickey;
	$: {
		if (!publickey && account) {
			try {
				const address = Address.create(account);
				account = address.getReedSolomonAddress();
				publickey = address.getPublicKey();
			} catch (e) {
				// noop
			}
		}
	}

	const reset = () => {
		account = null;
		publickey = null;
		error = '';
	};

	const getErrorMessage = (e: HttpError) =>
		(e.data && e.data.message) || e.message || 'An unknown Error occurred';

	const activate = async () => {
		try {
			isLoading = true;
			await http.post('/activate', { account, publickey, ref: 'self' });
			activationState = ActivationState.Activated;
			title = 'Successfully activated';
			reset();
		} catch (e: any) {
			activationState = ActivationState.Failed;
			title = 'Oh, snap. This did not work. Try again';
			error = getErrorMessage(e);
		} finally {
			isLoading = false;
		}
	};

	const closeError = () => {
		title = InitialTitle;
		reset();
	};
</script>

<svelte:head>
	<title>Signum Account Activator</title>
</svelte:head>

<section class="hero">
	<div class="hero-body">
		<div class="container">
			{#if isTestnet}
				<div class="stamp-wrapper">
					<Stamp text="Testnet" />
				</div>
			{/if}
			<a href="https://signum.network/" target="_blank" rel="noopener">
				<figure>
					<img class="is-180px-height" alt="Signum Logo" src="signum.svg" />
				</figure>
			</a>
			<h1 class="subtitle is-uppercase is-size-2-tablet is-size-4-mobile has-text-white">
				{title}
			</h1>
		</div>
	</div>
</section>

<section class="form">
	{#if activationState === ActivationState.Activated}
		<div transition:fade class="success">
			<a href="https://signum.network/" target="_blank" rel="noopener">
				<figure>
					<img class="is-256px-height whitened" src="success.png" alt="Success" />
				</figure>
			</a>
			<small class="has-text-white">
				A welcome message was sent to your account. You'll receive it in a few moments.
			</small>
		</div>
	{:else}
		<div>
			<div class="field">
				<label for="account" class="label has-text-white">Account Address or Id</label>
				<div class="control">
					<input
						name="account"
						class="input is-large"
						type="text"
						placeholder="Enter Account Address or Id"
						bind:value={account}
					/>
				</div>
			</div>
			<div class="field">
				<label for="publickey" class="label has-text-white">Public Key</label>
				<div class="control">
					<input
						name="publickey"
						class="input is-large"
						type="text"
						placeholder="Enter Public Key"
						bind:value={publickey}
					/>
				</div>
			</div>
			{#if error}
				<div in:fade={{ duration: 500 }} class="notification is-danger">
					<button class="delete" on:click={closeError}></button>
					<small>{error}</small>
				</div>
			{:else}
				<div class="is-flex is-flex-direction-column has-text-centered">
					<div class="buttons">
						<button
							class={`button is-primary is-light is-large ${isLoading ? 'is-loading' : ''}`}
							on:click={async () => await activate()}
							disabled={!canActivate}
							>Activate
						</button>
					</div>
					<div class="donation">
						<small>
							<a class="has-text-white" href="/whyactivation">Why Account Activation? 🤔</a>
						</small>
					</div>
				</div>
			{/if}
		</div>
	{/if}
</section>

<style>
	h1,
	figure {
		text-align: center;
		margin: 0 auto;
	}

	figure {
		margin: 0 0 1em 0;
	}

	.is-256px-height {
		height: 100%;
		max-height: 256px;
	}

	.is-180px-height {
		height: 100%;
		max-height: 256px;
	}

	.buttons {
		padding-top: var(--dim-gap);
		display: flex;
		justify-content: center;
	}

	.success {
		text-align: center;
	}

	.stamp-wrapper {
		position: absolute;
		top: 128px;
		left: 256px;
	}

	.whitened {
		filter: invert();
	}
</style>
