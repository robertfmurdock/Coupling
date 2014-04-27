"use strict";
var CouplingDataService = require('../../lib/CouplingDataService');
var should = require('should');
var mongoUrl = 'localhost/CouplingTest';
var monk = require('monk');
var _ = require('underscore');
var Comparators = require('../../lib/Comparators');
var database = monk(mongoUrl);

describe('DataService', function () {

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

    var couplingDataService = new CouplingDataService(mongoUrl);

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
        setTimeout(function () {
            couplingDataService.requestPlayersAndHistory(null, function (players, history) {
                should(expectedPlayers).eql(players);
                should(expectedHistory).eql(history);
                testIsDone();
            });
        });
    });

    it('can retrieve all the players', function (testIsDone) {
        couplingDataService.requestPlayers(null, function (players) {
            should(expectedPlayers).eql(players);
            testIsDone();
        });
    });

    it('can retrieve all the history in new to old order', function (testIsDone) {
        couplingDataService.requestHistory(null, function (history) {
            should(expectedHistory).eql(history);
            testIsDone();
        });
    });

    it('can retrieve all the tribes.', function (done) {
        couplingDataService.requestTribes(function (tribes) {
            should(expectedTribes).eql(tribes);
            done();
        }, function (error) {
            should.not.exist(error);
            done();
        });
    });

    it('can save a new player', function (testIsDone) {
        var player = {name: 'Tom', email: 'Bombadil@shire.gov'};
        couplingDataService.savePlayer(player, function () {
            couplingDataService.requestPlayers(null, function (players) {
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

    describe('can remove an existing player', function () {
        beforeEach(function (done) {
            couplingDataService.removePlayer(frodo._id, done);
        });

        it('such that it no longer appears in the players list', function (done) {
            couplingDataService.requestPlayers(null, function (players) {
                setTimeout(function () {
                    var result = players.some(function (player) {
                        return Comparators.areEqualPlayers(frodo, player);
                    });
                    result.should.be.false;
                    done();
                });
            });
        });

        it('such that it still exists in the database', function (done) {
            setTimeout(function () {
                playersCollection.find({_id: frodo._id}, {}, function (error, documents) {
                    should.not.exist(error);
                    Comparators.areEqualPlayers(documents[0], frodo).should.be.true;
                    done();
                });
            });
        });
    });

    describe('can remove old pair assignments', function () {
        beforeEach(function (done) {
            couplingDataService.removePairAssignments(pairSetOne._id, done);
        });

        it('such that it no longer appears in history', function (done) {
            couplingDataService.requestHistory(null, function (historyDocuments) {
                setTimeout(function () {
                    var result = historyDocuments.some(function (assignments) {
                        return Comparators.areEqualObjectIds(pairSetOne._id, assignments._id);
                    });
                    result.should.be.false;
                    done();
                });
            });
        });

        it('such that it still exists in the database', function (done) {
            setTimeout(function () {
                historyCollection.find({_id: pairSetOne._id}, {}, function (error, documents) {
                    should.not.exist(error);
                    Comparators.areEqualObjectIds(documents[0]._id, pairSetOne._id).should.be.true;
                    done();
                });
            });
        });
    });

    it('can update an existing player', function (testIsDone) {
        frodo.name = "F. Swaggins";
        couplingDataService.savePlayer(frodo, function () {
            couplingDataService.requestPlayers(null, function (players) {
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
            couplingDataService.requestPlayers(tribeId, function (players) {
                should(blackrockPlayers).eql(players);
                done();
            });
        });

        it('and get the correct history.', function (done) {
            couplingDataService.requestHistory(tribeId, function (history) {
                should([blackrockPairAssignments]).eql(history);
                done();
            });
        });

        it('and get the correct player and history together.', function (done) {
            couplingDataService.requestPlayersAndHistory(tribeId, function (players, history) {
                should(blackrockPlayers).eql(players);
                should([blackrockPairAssignments]).eql(history);
                done();
            });
        });
    });
});
