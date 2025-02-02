<svelte:head>
	<title>Why Activation</title>
</svelte:head>

<section class="hero">
	<div class="hero-body">
		<div class="container center-text">
			<h2
				class="subtitle is-uppercase is-size-2-tablet is-size-4-mobile center-text has-text-white"
			>
				Why Account Activation?
			</h2>
		</div>
	</div>
</section>

<figure class="center-text">
	<img src="accounts.png" alt="Account Representations in Signum" />
</figure>

<section class="content">
	<h3 class="has-text-warning has-text-centered">
		Never hodl funds in an account without an assigned public key!
	</h3>
</section>

<section id="text" class="content has-text-justified has-text-white">
	<h2 class="has-text-white">Anatomy of Signum Accounts</h2>
	<p>
		Signum is an account oriented blockchain (in contrast to UTXO based concepts like it used in
		Bitcoin). This concept is a more intuitive, as this is similar to common bank accounts. Other
		than in Ethereum, accounts in Signum can be identified by three different types: Public Key,
		Numerical ID, and the so called Reed-Solomon-Address.
	</p>
	<p>
		The public key serves as the origin and is generated through a specific key generation
		algorithm. The Numeric ID and Reed-Solomon, derived from the public key, are more frequently
		utilized for account identification due to their enhanced readability.
	</p>
	<h2 class="has-text-white">Protect your account</h2>
	<p>
		To transfer values from one account to another, typically the numeric Account ID or the
		alphanumeric Reed-Solomon Address is employed. The "cryptographic namespace" of these
		representations is more compact than that of the public key. To comprehensively secure a Signum
		Account, registration along with the public key is imperative. Only through this association is
		the account adequately safeguarded.
	</p>
	<p>
		Typically, the public key becomes accessible once the account owner has initiated at least one
		outgoing transaction on the blockchain. However, for an account to execute an outgoing
		transaction, it must have a minimal balance. This is where the Activation Service proves
		valuable: by utilizing the recipient's public key, the assignment can be accomplished, thereby
		ensuring the complete security of the account.
	</p>
</section>

<style>
	.center-text {
		text-align: center !important;
	}

	figure {
		margin: 0 0 1em 0;
	}

	img {
		max-height: 180px;
	}

	#text {
		max-height: 45vh;
		overflow-y: scroll;
	}
</style>
