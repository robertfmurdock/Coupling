# News

## September 3, 2020

We're replaced normal Google login with Auth0. Let's see if anyone notices! You should be able to create accounts now using this flow even if you don't actually have a Google email... a long requested feature. Try not to abuse it so Auth0 is nice to me and doesn't send me a bill.

Why did I do this? The Google login system I was using (the Google GAPI Javascript library) apparently breaks security standards that have been warnings in web browsers for *years*, and now browsers like Safari are enforcing them... breaking login. Google didn't fix it or provide a work around, so... I'm cross with them. This is me working out those issues.

As always, if you see any problems, open an issue at [the github](https://www.github.com/robertfmurdock/coupling). I really don't want anyone to lose access to their team info so I *hope* this transition goes completely smoothly.