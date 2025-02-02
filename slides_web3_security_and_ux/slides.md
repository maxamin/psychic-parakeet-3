## Security And Usability Go Hand In Hand

["3 Proposals for Making Web3 a Better Experience"](https://medium.com/@wighawag/3-proposals-for-making-web3-a-better-experience-974f97765700)

<br/> 
<br/> 


<img src="images/wig256x256.png" style="float:left;border:none;", width="80px"/>
<img src="images/wig256x256.png" style="float:right;border:none;transform:scale(-1,1);", width="80px"/>
<center><div style="width:80%;">Ronan Sandford, Creator of [Etherplay](http://etherplay.io) and Lead Blockchain Architect at [Pixowl](http://pixowl.com)</div></center>


----

## Web3 browser today are mostly web3 signers 

* <!-- .element: class="fragment" --> They are not aware of the context 

* <!-- .element: class="fragment" --> Why is that important?

* <!-- .element: class="fragment" --> Because this would allow better UX (by removing unnecessary authorization requests)

----

## The case of cookies and local storage 

And the concept of same-origin policy

* <!-- .element: class="fragment" --> Web Browsers ensure applications are isolated by checking "origin"

* <!-- .element: class="fragment" --> **This is done without user confirmation**

Note:
in the old web :the tuple (protocol, host, port)
in web3 this could be an IPFS or SWARM hash

----

## 1) Non-Interactive Decryption

----

<section data-background-size="contain" data-background-image="images/encryption.png"></section>

----

<section data-background-size="contain" data-background-image="images/automated_decryption.png"></section>

----

## What about signatures?

* <!-- .element: class="fragment" --> origin checks can protect from "authorization fatigue" attack

* <!-- .element: class="fragment" --> Allow for Safe Non-Interactive Signatures

----

## Requires Per User Origin Approval

----

<section data-background-size="contain" data-background-image="images/approve_origin.png"></section>

----

## Non-Interactive Signatures

<!-- .element: class="fragment" --> With origin checks it can be safe for an application to sign on user behalf without user confirmation

* <!-- .element: class="fragment" --> If you can trust the frontend

* <!-- .element: class="fragment" --> think audited or formally proved IPFS hash or user's incentive compatible app

* <!-- .element: class="fragment" --> You can actually relies on it and provide signature without conformation

Note:
Think of social app with their own submit buttons

----

<section data-background-size="contain" data-background-image="images/interactive_origin_signature.png"></section>

----

### Verifier's Role

----

<section data-background-size="contain" data-background-image="images/interactive_origin_verifier.png"></section>

----

## Links

- ["3 Proposals for Making Web3 a Better Experience"](https://medium.com/@wighawag/3-proposals-for-making-web3-a-better-experience-974f97765700)
- [Same Origin Policy](https://www.w3.org/Security/wiki/Same_Origin_Policy)
- [RFC 6454 : The Web Origin Concept](https://tools.ietf.org/html/rfc6454)
- [EIP 712](https://github.com/ethereum/EIPs/pull/712)
- [encryption/decryption discussion on Ethereum magicians](https://ethereum-magicians.org/t/the-ux-of-eip-1024-encrypt-decrypt/1243)

----

## Questions
<br/>
<br/>
<br/> 
<br/> 
<br/> 
<br/>
<br/>

<img src="images/wig256x256.png" style="float:left;border:none;", width="80px"/>
<img src="images/wig256x256.png" style="float:right;border:none;transform:scale(-1,1);", width="80px"/>
<center><div style="width:80%;">Find me on twitter [@wighawag](https://twitter.com/wighawag)</div></center>

