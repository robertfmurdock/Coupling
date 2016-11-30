Coupling [![Circle CI](https://circleci.com/gh/robertfmurdock/Coupling.png?style=badge)](https://circleci.com/gh/robertfmurdock/Coupling)
========

This is a web site that will automatically assign pairs for any given iteration.

The latest build is currently hosted at https://armored-coupling.herokuapp.com/. Please note that because the app is currently still developing, there's no guarentee that information stored there currently won't be lost during the course of development (though if it is, that will certainly not be intentional).  


Intended to make it quick and easy to pair off, and encourage people to pair with people who they haven't paired with in a while.

Have fun!


To install and run, you'll need to have mongo and node installed. Once you've got that...

1. Clone the Coupling repository.
2. Run npm rebuild so that the compiled dependencies work on your computer.
3. In the Coupling directory, run the command "npm start"
4. Now you can browse to the Coupling website in a browser. http://localhost:3000
5. If you get an error message telling you that mongo isn't working... start mongo. Then check the website again.
6. Try it out and let me know what you think!

For developers:

7. If you don't already have it, install the grunt command line interface by running: npm install grunt-cli -g
8. You'll need to install the appropriate webdriver tools in order to run the functional tests. You can do this by running: node node_modules/protractor/bin/webdriver-manager update --standalone
9. Run: grunt. All tests should run, including selenium tests. Hooray!

Notice: this app now has a Dockerized development environment! Install docker and docker-compose, clone this repo, then:

    docker-compose build
    docker-compose up

The app will be running on port 3000 and will rerun all tests whenever a file is changed. Whoa!

What is Pairing?
----------------
  Pairing is the act of two specialists in one profession working together on the same task concurrently. This allows for constant collaboration when brainstorming for solutions, reinforcement of standards and cultural norms, and immediate peer-review of all work being produced. This helps build trust that all the content created by the pair is valuable and safe.

Why Should Pairs Be Rotated?
----------------------------
  For better or for worse, teams are made up of people, and people have different skill levels, opinions, temperments and philosophies. Rotating pairs is intended to help make sure that senior people will get to synchronize with other senior people periodically. Similarly, junior team members should get the opportunity to work with other junior team members - this gives them the opportunity to test their growth. Regular rotation of pairs also helps discourage project knowledge from being reserved for a few 'smart' individuals... at the very least, many team members should see and participate in decisions regularly.

What Makes For Good Pairing? What Doesn't?
------------------------------------------

TBD


How Does Coupling Choose Pairs?
-------------------------------

Coupling remembers the history of every pair its ever made. The longer you use it, the more it knows! This is important, because Coupling follows these rules:

1. Whoever has not paired together for the longest time will be automatically paired together.
2. When there are multiple potential pairs that have been apart for the longest time, one is chosen at random. Rules continue as normal with the remaining players.

Those rules are repeated until there are no players remaining to be paired together. This encourages teams to let everybody work together, and that helps build relationships among team members. The best way to learn how to work together is to actually, directly work together.

Oh no! Someone is sick this week! What should I do?
---------------------------------------------------

Fret not! Coupling is prepared to deal with your problem. When you know someone won't be available for a whole pairing cycle, tap on their face in the Player Roster (at the bottom of the screen) before spinning for new pairs. They'll be excluded from this week's roster and Coupling will automatically compensate for their absense.

If someone is only available for part of a pairing cycle, Coupling recommends that you pair them anyway, possibly using the drag-and-drop manual override to pair them with someone similarly partially available. Part-time pairing with someone is better then not pairing with them at all!

How Does my Tribe or Player Card Get One of Those Fancy Pictures?
--------------------------------------------------------

There are multiple options! If you already have the images you want hosted somewhere, you can enter the URL to the image in the 'Image URL' field for your tribe or player. If you don't have hosting, you can associate a tribe or player with an email address and connect it to [Gravatar](http://www.gravatar.com), and [Gravatar](http://www.gravatar.com) will provide your icon.


I Don't Have a Real Team, But I Still Want To Try Coupling!
-----------------------------------------------------------

For people who just want to play with the program and understand its features, I've included a demo dataset in the file Coupling Demo Data.zip. Unzip the file, then use mongorestore to add it to your local mongo database. A tribe with a set of players and history will be available to you the next time you connect to Coupling.

Is This Really Just For Work?
-----------------------------

Nope! Feel free to use this for anything - field trips and buddy systems, pairing up for murder mysteries, impromptu fight clubs... All I ask is that you send me a nice note telling me about your innovative use of the program.

Good luck!
