"use strict";
var CouplingDataService = require('../../lib/CouplingDataService');
var should = require('should');
var mongoUrl = 'localhost/CouplingTest';
var monk = require('monk');
var Comparators = require('../../lib/Comparators');
var database = monk(mongoUrl);

describe('Coupling Database Adapter', function () {

    var expectedPlayers = [
        {name: 'Gandalf'},
        {name: 'Sam'},
        {name: 'Merry'},
        {name: 'Pippin'},
        {name: 'Frodo'}
    ];

    var pairSetOne = {pairs: [
        [
            {name: 'Gandalf'},
            {name: 'Frodo'}
        ],
        [
            {name: 'Merry'},
            {name: 'Pippin'}
        ]
    ], date: new Date(2013, 8, 1)};

    var pairSetTwo = {pairs: [
        [
            {name: 'Frodo'},
            {name: 'Gandalf'}
        ],
        [
            {name: 'Merry'},
            {name: 'Pippin'}
        ]
    ], date: new Date(2013, 10, 7)};

    var pairSetThree = {pairs: [
        [
            {name: 'Merry'},
            {name: 'Frodo'}
        ],
        [
            {name: 'Gandalf'},
            {name: 'Pippin'}
        ]
    ], date: new Date(2014, 1, 20)};

    var expectedHistory = [
        pairSetThree, pairSetTwo, pairSetOne
    ];

    var unorderedHistory = [
        pairSetTwo, pairSetThree, pairSetOne
    ];

    var couplingDatabaseAdapter = new CouplingDataService(mongoUrl);

    var historyCollection = database.get('history');

    before(function (beforeIsDone) {
        var playersCollection = database.get('players');
        playersCollection.drop();
        playersCollection.insert(expectedPlayers);

        historyCollection.drop();
        historyCollection.insert(unorderedHistory, beforeIsDone);
    });

    it('can retrieve all the players in the database and all the history in new to old order', function (testIsDone) {
        couplingDatabaseAdapter.requestPlayersAndHistory(function (players, history) {
            should(expectedPlayers).eql(players);
            should(expectedHistory).eql(history);
            testIsDone();
        });
    });

    it('can retrieve all the players', function (testIsDone) {
        couplingDatabaseAdapter.requestPlayers(function (players) {
            should(expectedPlayers).eql(players);
            testIsDone();
        });
    });

    it('can retrieve all the history in new to old order', function (testIsDone) {
        couplingDatabaseAdapter.requestHistory(function (history) {
            should(expectedHistory).eql(history);
            testIsDone();
        });
    });

    it('can save a new player', function (testIsDone) {
        var player = {name: 'Tom', email: 'Bombadil@shire.gov'};
        couplingDatabaseAdapter.savePlayer(player, function () {
            couplingDatabaseAdapter.requestPlayers(function (players) {
                var found = players.some(function (listedPlayer) {
                    return Comparators.areEqualPlayers(player, listedPlayer);
                });
                found.should.be.true;
                testIsDone();
            });
        }, function (error) {
            should.not.exist(error);
            testIsDone();
        });
    });


});
