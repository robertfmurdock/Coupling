var monk = require('monk');

var GameRunner = require('../../../../server/lib/GameRunner').default;
var CouplingGameFactory = require('../../../../server/lib/CouplingGameFactory').default;
var CouplingDataService = require('../../../../server/lib/CouplingDataService');
var PairAssignmentDocument = require('../../../../common/PairAssignmentDocument').default;
var Comparators = require('../../../../server/lib/Comparators').default;
var config = require('../../../../config');

describe('The game', function () {
  var clark = {name: "Superman"};
  var bruce = {name: "Batman"};
  var diana = {name: "Wonder Woman"};
  var hal = {name: "Green Lantern"};
  var barry = {name: "Flash"};
  var john = {name: "Martian Manhunter"};

  var playerRoster = [
    clark,
    bruce,
    diana,
    hal,
    barry,
    john
  ];

  var mongoUrl = config.testMongoUrl + '/CouplingTest';
  var database = monk(mongoUrl);

  var historyCollection = database.get('history');

  beforeEach(function (done) {
    var playersCollection = database.get('players');
    playersCollection.drop()
      .then(function () {
        return playersCollection.insert(playerRoster);
      })
      .then(done, done.fail)
  });

  beforeEach(function () {
    historyCollection.drop();
  });

  it('works with no history', function (done) {
    var couplingGameFactory = new CouplingGameFactory();
    var gameRunner = new GameRunner(couplingGameFactory);

    new CouplingDataService(mongoUrl).requestPlayersAndHistory(null)
      .then(function (both) {
        var result = gameRunner.run(both.players, [], both.history);
        var foundPlayers = [];
        result.pairs.forEach(function (pair) {
          expect(pair.length).toEqual(2);
          foundPlayers = foundPlayers.concat(pair);
        });

        expect(foundPlayers.length).toEqual(6);
      })
      .then(done, done.fail);
  });

  it('works with an odd number of players history', function (done) {
    var couplingGameFactory = new CouplingGameFactory();
    var gameRunner = new GameRunner(couplingGameFactory);

    new CouplingDataService(mongoUrl).requestHistory(null)
      .then(function (history) {
        var result = gameRunner.run([clark, bruce, diana], [], history);
        expect(result.pairs.length).toEqual(2);
      })
      .then(done, done.fail);
  });

  it('will always pair someone who has paired with everyone but one person with that one person', function (done) {
    var couplingGameFactory = new CouplingGameFactory();
    var gameRunner = new GameRunner(couplingGameFactory);

    var history = [
      new PairAssignmentDocument(new Date(2014, 1, 10), [
        [bruce, clark]
      ]),
      new PairAssignmentDocument(new Date(2014, 1, 9), [
        [bruce, diana]
      ]),
      new PairAssignmentDocument(new Date(2014, 1, 8), [
        [bruce, hal]
      ]),
      new PairAssignmentDocument(new Date(2014, 1, 7), [
        [bruce, barry]
      ])
    ];

    historyCollection.insert(history, function () {
      new CouplingDataService(mongoUrl).requestPlayersAndHistory(null)
        .then(function (both) {
          var pairAssignments = gameRunner.run(both.players, [], both.history);
          var foundBruceAndJohn = pairAssignments.pairs.some(function (pair) {
            return Comparators.areEqualPairs([bruce, john], pair);
          });
          expect(foundBruceAndJohn).toBe(true);
        })
        .then(done, done.fail);
    });
  });
});
