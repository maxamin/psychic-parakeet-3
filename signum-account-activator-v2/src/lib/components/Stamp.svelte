<script>
	export let text = '';
</script>

<span class="stamp is-nope">{text}</span>

<style>
	.stamp {
		transform: rotate(-12deg);
		color: #555;
		font-size: 3rem;
		font-weight: 700;
		border: 0.25rem solid #555;
		display: inline-block;
		padding: 0.25rem 1rem;
		text-transform: uppercase;
		border-radius: 1rem;
		font-family: 'Courier', serif;
		-webkit-mask-image: url($lib/assets/texture.png);
		-webkit-mask-size: 256px 256px;
	}

	.is-nope {
		color: #d23;
		border: 0.5rem double #d23;
		transform: rotate(-13deg);
		-webkit-mask-position: 2rem 3rem;
		font-size: 2rem;
	}
</style>
