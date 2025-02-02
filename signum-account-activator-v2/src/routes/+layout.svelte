<script>
	import Footer from '$lib/components/Footer.svelte';
</script>

<main>
	<slot />
</main>
<Footer />

<style>
	:global(html),
	:global(body) {
		height: 100%;
		margin: 0;
		background-color: #0066ff;
	}

	main {
		overflow-y: auto;
		position: relative;
		max-width: 56em;
		background-color: #0066ff;
		padding: 2em;
		margin: 0 auto;
		box-sizing: border-box;
		height: calc(99vh - var(--dim-footer-height) - var(--dim-small-gap) * 2);
	}
</style>
