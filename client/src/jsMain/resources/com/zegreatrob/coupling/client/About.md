# About This App

## Welcome to Coupling!
### The program that tries to make it as easy to pair as possible.

If you're new... *hi!* Glad to have you. If you don't already have one, you'll create a "party", and then add "players" to it. Once you've got a few players, you can *spin* in order to automatically generate a set of pairs.

Now, the automatic matching is *not* random. The program will do its best to make sure  everyone has an opportunity to pair with everyone else, and it prioritizes "couples" that haven't seen each other for a while. Information about how long its been for each "couple" is available on the party's statistics page.

If you have questions, curses, or compliments, feel free to contact me. My information is on my [homepage](https://robertfmurdock.github.io), and you can always leave issues on [the github page](https://www.github.com/robertfmurdock/coupling)!

Unfamiliar with pairing?
Check out this editorial I wrote on the subject - [Pairing for Outsiders](https://medium.com/@robert.f.murdock/pairing-for-outsiders-f3bb68086de1). That'll get you up to speed I hope.

---

## Tech Notes

As present time, this application is written in Kotlin (both server and client), compiled to Javascript, and hosted in AWS. Historically, it was originally written in pure Javascript, then rebuilt with Webpack, then ported to Typescript, then ported to Kotlin. It is a living example of being able to completely rewrite an application *incrementally*, *repeatedly* with zero downtime *even with* every valid commit being pushed to production immediately. If you don't believe me, look at the commit history. Its all there, including warts. So many warts.

Only information explicitly keyed by the user is consumed by the application. Email addresses are used to provide access to parties, and nothing more... auth is provided by third party vendors so I don't have to manage any of that nonsense.

I operate this app at a loss, so please be nice. Tips appreciated.

#### Thank you for checking it out!