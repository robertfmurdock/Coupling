Running Locally
---

#### --- Out of date ---

Please note, these instructions are pretty out of date, and don't reflect how development is done with it typically. They might work!... but they also might not.  Most functionality is provided via Gradle tasks now, and running "./gradlew tasks --all" should be enough to guess what does what. If looking at script commands sounds too intimidating... may I suggest you take advantage of the [hosted environment](https://coupling.zegreatrob.com)?

To install and run, you'll need to have mongo and node installed... newest versions are preferable. Once you've got that...

1. Clone the Coupling repository.
2. Run npm test. This should download all the dependencies and run the app.
2.5. If you get an error message telling you that mongo isn't working... start mongo. Then run tests again.
3. In the Coupling directory, run the command "npm start"
4. Now you can browse to the Coupling website in a browser. http://localhost:3000
5. Try it out and let me know what you think!

Notice: this app now has a Dockerized development environment! Install docker and docker-compose, clone this repo, then:

    docker-compose build
    docker-compose up

The app will be running on port 3000 and will rerun all tests whenever a file is changed. Whoa!
