# Want to get started with Contributions?

Coupling has the ability to track how often your party members contribute to your project! With this information, your
party can be better informed as to what's been happening to the team over time.

## What are Contributions?

In Coupling, a contribution is any meaningful change that's been integrated into your project.

Contributions can have properties like:

- All the people who participated in creating the contribution
- When the contribution was integrated
- When the contribution was started
- Its "semantic versioning" increment
- The amount of cycle time the contribution took
- How much joy the team got from making the contribution (
  see [Measuring Joy for Software Developers](https://www.scrumexpert.com/knowledge/measuring-joy-for-software-developers/))
- And more!

## What does tracking Contributions get me?

You can learn things like:

- How frequently each individual pairs/mob integrates their contributions
- How much cycle time your team averages per contribution
- How much cycle time each pair averages per contribution
- Which eras have flurries of activity vs which ones are slow
- How you feel about statistics (you might like them!)

## Cool! I'm sold. How do I start?

## First: Decide when to make a Contribution official

Every team and project is different, and your needs might be different than other teams. For software teams, here are
some reasonable alternatives as to when you might call it a "contribution":

- Immediately on commit and push to a remote "main" branch
- Only after Github Actions successfully builds a "main" branch
- Only when a PR is merged to a "main" branch

Whatever you choose, you'll want to ensure that you have the ability to run a program on your choice of trigger.

For many of us, that'll mean finding the right part of a "Github Action" / "Azure Dev Ops" / "Jenkins" / Build Server
script.

Yep, that means scripting.

## Second: Setup Automatic Coupling Access

In order to write a script that will update your party's contributions, you're going to need to generate a Coupling
secret for your party.

The Coupling secret system is a way of generating a one-time value that grants programmatic access to a single party in
Coupling.

Using this token, you can use the Coupling CLI, or the Coupling GraphQL API.

You can also use the [Coupling Contribution Action](https://github.com/robertfmurdock/coupling-contribution-action), which is a Github Action that will use a Coupling Secret to upload contributions.  

### [Coupling Contribution Action](https://github.com/robertfmurdock/coupling-contribution-action)

If you use Github Actions, using the Coupling Contribution Action is as simple as:

```yml
      steps:
      - name: Update Contributions
        uses: robertfmurdock/coupling-contribution-action@v2
        with:
          coupling-secret: YOUR_COUPLING_SECRET
          party-id: YOUR_PARTY_ID
          save-contribution: ${{ github.ref == 'refs/heads/master' }}
          cycle-time-from-first-commit: true
          contribution-file: build/digger/current.json

```

Please consult the [Github Action](https://github.com/robertfmurdock/coupling-contribution-action) for more information.

The contribution file is in a JSON formatted to a spec defined in the "digger" project. 

[Digger Contribution Specification](https://github.com/robertfmurdock/ze-great-tools/blob/main/tools/digger-json/src/commonMain/kotlin/com/zegreatrob/tools/digger/json/ContributionDataJson.kt)

Any "Instant" in the specification is an ISO 8601 date-time. Any Duration is an ISO 8601 duration.

If you'd like to use the Digger Gradle Plugin to generate the JSON, see [Digger Plugin](https://github.com/robertfmurdock/ze-great-tools/tree/main/tools/digger-plugin).


### Coupling CLI

Coupling provides a CLI that provides a subset of the actions provided by the GraphQL API.

#### Install

The latest version of this CLI is always available from the Coupling website in two variants:

##### [JVM](https://coupling.zegreatrob.com/latest-cli?type=jvm)

Click the link to download, unzip it, and add the bin directory to your path!

##### [JS](https://coupling.zegreatrob.com/latest-cli?type=js)

Click the link to download, unzip it, and in the directory run:

```bash
        npm install
        npm link
```

#### Usage

For the latest available features, use the --help command with the cli:

```bash
coupling --help
```

To upload a contribution to Coupling via the CLI, use the save contribution command:

```bash
coupling party contribution --party-id YOUR_PARTY_ID save --input-json CONTRIBUTION_FIELDS_AS_JSON
```

The contribution command accepts a JSON formatted to a contribution spec defined in the "digger" project. Additional fields are available as CLI arguments.

[Digger Contribution Specification](https://github.com/robertfmurdock/ze-great-tools/blob/main/tools/digger-json/src/commonMain/kotlin/com/zegreatrob/tools/digger/json/ContributionDataJson.kt)

Any "Instant" in the specification is an ISO 8601 date-time. Any Duration is an ISO 8601 duration.

If you'd like to use the Digger Gradle Plugin to generate the JSON, see [Digger Plugin](https://github.com/robertfmurdock/ze-great-tools/tree/main/tools/digger-plugin).

### Coupling GraphQL API

You can acquaint yourself with the Coupling GraphQL API in the playground [here](/graphiql) (which will authenticate
with your current login).

I won't explain the fullness of how one might interact with a [GQL API](https://graphql.org/) here, but here's the simple version:

    You make a POST request including a query, and the response includes the requested data.

To use your Coupling Secret with the API, include it as a Bearer token in the Authorization header.

Here's an example of how one might query the Coupling GQL API with node.js fetch:

```javascript
const response = await fetch("https://coupling.zegreatrob.com/api/graphql", {
  "method": "POST",
  "headers": {
    "accept": "application/json",
    "accept-language": "en-US,en;q=0.9",
    "authorization": `Bearer ${YOUR_SECRET_GOES_HERE}`,
    "content-type": "application/json",
    "Referer": "https://coupling.zegreatrob.com/graphiql",
  },
  "body": JSON.stringify({
    "query": gql`
        query example($partyInput:PartyInput!) {
            party(input:$partyInput) {
                contributionReport { count }
            }
        }`,
    "variables": {
      "partyInput": {
        "partyId": YOUR_PARTY_ID_GOES_HERE
      }
    }
  }),
});
const responseJson = await response.json()
expect({"data":{"party":{"contributionReport":{"count":4770}}}}).toEql(responseJson)

```

With this API, you can design whatever query you like and include it in the body, so it gives you every feature of Coupling that a regular user can do.

For saving contributions, the relevant query is `Mutation.saveContribution`, which lets you save as many contributions as you like in one call.

Please consult the Coupling GQL schema listed at the [playground](/graphiql) for exactly what can be saved in each contribution.
