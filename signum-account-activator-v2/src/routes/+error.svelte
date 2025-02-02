<script>
	import { onDestroy } from 'svelte';
	import { page } from '$app/stores';
	let error = $page.error.message;

	const dev = process.env.NODE_ENV === 'development';

	let timeToRedirect = 10;
	const interval = setInterval(() => {
		if (--timeToRedirect === 0) {
			location.replace('/');
		}
	}, 1000);

	onDestroy(() => {
		clearInterval(interval);
	});
</script>

<svelte:head>
	<title>{error}</title>
</svelte:head>

<section class="hero">
	<div class="hero-body">
		<div class="container center-text">
			<h1
				class="subtitle is-uppercase is-size-2-tablet is-size-4-mobile center-text has-text-white"
			>
				This is not the page you're looking for
			</h1>
			<a href="/api/info" class="button is-primary">Open API Description</a>
			<div class="has-text-white">
				<small>{timeToRedirect ? `Redirecting in ${timeToRedirect} seconds...` : 'Bye'}</small>
			</div>
		</div>
	</div>
</section>

<section class="content center-text">
	<figure>
		<img src="c3po.png" alt="Success" />
	</figure>
</section>

<style>
	h1 {
		margin: 0 auto;
	}

	h1 {
		font-size: 2.8em;
		font-weight: 700;
		margin: 0 0 0.5em 0;
	}

	@media (min-width: 480px) {
		h1 {
			font-size: 4em;
		}
	}

	.center-text {
		text-align: center !important;
	}

	figure {
		margin: 0 0 1em 0;
	}

	.content img {
		max-height: 256px;
	}

	@media (max-width: 400px) {
		.content img {
			width: 112px;
		}
	}
</style>
