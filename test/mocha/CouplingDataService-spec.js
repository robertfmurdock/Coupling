"use strict";
var CouplingDataService = require('../../lib/CouplingDataService');
var should = require('should');
var mongoUrl = 'localhost/CouplingTest';
var monk = require('monk');
var Comparators = require('../../lib/Comparators');
var database = monk(mongoUrl);

describe('CouplingDataService', function () {

    var frodo = {name: 'Frodo'};
    var expectedPlayers = [
        {name: 'Gandalf'},
        {name: 'Sam'},
        {name: 'Merry'},
        {name: 'Pippin'},
        frodo
    ];

    var pairSetOne = {pairs: [
        [
            {name: 'Gandalf'},
            frodo
        ],
        [
            {name: 'Merry'},
            {name: 'Pippin'}
        ]
    ], date: new Date(2013, 8, 1)};

    var pairSetTwo = {pairs: [
        [
            frodo,
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
            frodo
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

    var expectedTribes = [
        {name: "JLA"},
        {name: "Avengers"},
        {name: "Superfriends"}
    ];

    var couplingDatabaseAdapter = new CouplingDataService(mongoUrl);

    var historyCollection = database.get('history');
    var playersCollection = database.get('players');
    var tribesCollection = database.get('tribes');

    beforeEach(function (beforeIsDone) {
        playersCollection.drop();
        playersCollection.insert(expectedPlayers);

        historyCollection.drop();
        historyCollection.insert(unorderedHistory);

        tribesCollection.drop();
        tribesCollection.insert(expectedTribes, beforeIsDone);
    });

    it('can retrieve all the players in the database and all the history in new to old order', function (testIsDone) {
        couplingDatabaseAdapter.requestPlayersAndHistory(null, function (players, history) {
            should(expectedPlayers).eql(players);
            should(expectedHistory).eql(history);
            testIsDone();
        });
    });

    it('can retrieve all the players', function (testIsDone) {
        couplingDatabaseAdapter.requestPlayers(null, function (players) {
            should(expectedPlayers).eql(players);
            testIsDone();
        });
    });

    it('can retrieve all the history in new to old order', function (testIsDone) {
        couplingDatabaseAdapter.requestHistory(null, function (history) {
            should(expectedHistory).eql(history);
            testIsDone();
        });
    });

    it('can retrieve all the tribes.', function (done) {
        couplingDatabaseAdapter.requestTribes(function (tribes) {
            should(expectedTribes).eql(tribes);
            done();
        }, function (error) {
            should.not.exist(error);
            done();
        });
    });

    it('can save a new player', function (testIsDone) {
        var player = {name: 'Tom', email: 'Bombadil@shire.gov'};
        couplingDatabaseAdapter.savePlayer(player, function () {
            couplingDatabaseAdapter.requestPlayers(null, function (players) {
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

    it('can update an existing player', function (testIsDone) {
        frodo.name = "F. Swaggins";
        couplingDatabaseAdapter.savePlayer(frodo, function () {
            couplingDatabaseAdapter.requestPlayers(null, function (players) {
                var found = players.some(function (listedPlayer) {
                    return Comparators.areEqualPlayers(frodo, listedPlayer);
                });
                found.should.be.true;
                testIsDone();
            });
        }, function (error) {
            should.not.exist(error);
            testIsDone();
        });
    });

    describe('will filter based on the tribe name', function () {
        var tribeId = 'Blackrock';
        var ogrim = {tribe: tribeId, name: 'Orgrim' };
        var garrosh = {tribe: tribeId, name: 'Garrosh' };
        var blackrockPlayers = [
            ogrim,
            garrosh
        ];

        var blackrockPairAssignments = {
            tribe: tribeId,
            pairs: [
                [garrosh, ogrim]
            ]
        };

        beforeEach(function (beforeIsDone) {
            playersCollection.insert(blackrockPlayers);
            historyCollection.insert(blackrockPairAssignments, beforeIsDone);
        });

        it('and get the correct players.', function (done) {
            couplingDatabaseAdapter.requestPlayers(tribeId, function (players) {
                should(blackrockPlayers).eql(players);
                done();
            });
        });

        it('and get the correct history.', function (done) {
            couplingDatabaseAdapter.requestHistory(tribeId, function (history) {
                should([blackrockPairAssignments]).eql(history);
                done();
            });
        });

        it('and get the correct player and history together.', function (done) {
            couplingDatabaseAdapter.requestPlayersAndHistory(tribeId, function (players, history) {
                should(blackrockPlayers).eql(players);
                should([blackrockPairAssignments]).eql(history);
                done();
            });
        });

    });

});
