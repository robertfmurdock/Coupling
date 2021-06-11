# News

## June 11, 2021

Howdy hi! You may notice things are slightly different here at Coupling... but most likely not. Hopefully not!

What changed you ask?

Well the app is not being served as a serverless function rather than a container. It has been on the docker-container based hosting for many years (ever since it transitioned off of a heroku slug), but it was time to change things up a little. Took a little elbow grease because I still wanted to keep the websocket working, but it looks like I did it! Hooray!

That said, if you want to keep the low-key socket functionality working, you may have to clear your cookies for the site (I tweaked the cookie domain). That should be as simple as logging out and logging back in again.

As always, let me know if you see anything funky - open an issue at [the github](https://www.github.com/robertfmurdock/coupling).

One love.

- RoB