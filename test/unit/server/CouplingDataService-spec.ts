"use strict";
import * as monk from "monk";
import CouplingDataService from "../../../server/lib/CouplingDataService";
import Comparators from "../../../server/lib/Comparators";
var config = require('../../../config');

var mongoUrl = config.testMongoUrl + '/UsersTest';
var database = monk(mongoUrl);

interface Entity {
    _id: string
}

describe('CouplingDataService', function () {
    var frodo = {name: 'Frodo', _id: undefined};
    var expectedPlayers = [
        {name: 'Gandalf'},
        {name: 'Sam'},
        {name: 'Merry'},
        {name: 'Pippin'},
        frodo
    ];

    var pairSetOne = {
        pairs: [
            [
                {name: 'Gandalf'},
                frodo
            ],
            [
                {name: 'Merry'},
                {name: 'Pippin'}
            ]
        ], date: new Date(2013, 8, 1),
        _id: undefined
    };

    var pairSetTwo = {
        pairs: [
            [
                frodo,
                {name: 'Gandalf'}
            ],
            [
                {name: 'Merry'},
                {name: 'Pippin'}
            ]
        ], date: new Date(2013, 10, 7),
        _id: undefined
    };

    var pairSetThree = {
        pairs: [
            [
                {name: 'Merry'},
                frodo
            ],
            [
                {name: 'Gandalf'},
                {name: 'Pippin'}
            ]
        ], date: new Date(2014, 1, 20),
        _id: undefined
    };

    var expectedHistory = [
        pairSetThree, pairSetTwo, pairSetOne
    ];

    var unorderedHistory = [
        pairSetTwo, pairSetThree, pairSetOne
    ];

    var expectedTribes = [
        {name: "JLA", id: 'JLA'},
        {name: "Avengers", id: 'Avengers'},
        {name: "Superfriends", id: 'sf'}
    ];

    var pinsWithoutTribes = [
        {name: 'pin1'},
        {name: 'pin2'}
    ];

    var couplingDataService = new CouplingDataService(mongoUrl);

    var historyCollection = database.get('history');
    var playersCollection = database.get('players');
    var pinCollection = database.get('pins');
    var tribesCollection = database.get('tribes');

    beforeEach(function (done) {
        playersCollection.drop()
            .then(function () {
                return historyCollection.drop();
            })
            .then(function () {
                return tribesCollection.drop();
            })
            .then(function () {
                return pinCollection.drop();
            })
            .then(function () {
                return playersCollection.insert(expectedPlayers);
            })
            .then(function () {
                return historyCollection.insert(unorderedHistory);
            })
            .then(function () {
                return pinCollection.insert(pinsWithoutTribes);
            })
            .then(function () {
                return tribesCollection.insert(expectedTribes);
            })
            .then(done, done.fail);
    });

    describe('will null tribe id', function () {

        it('can retrieve the players in the database and all the history in new to old order', function (done) {
            couplingDataService.requestPlayersAndHistory(null)
                .then(function (both) {
                    expect(expectedPlayers).toEqual(both.players);
                    expect(expectedHistory).toEqual(both.history);
                })
                .then(done, done.fail);
        });

        it('can retrieve the players', function (done) {
            couplingDataService.requestPlayers(null)
                .then(function (players) {
                    expect(expectedPlayers).toEqual(players);
                })
                .then(done, done.fail);
        });

        it('can retrieve the history in new to old order', function (done) {
            couplingDataService.requestHistory(null)
                .then(function (history) {
                    expect(expectedHistory).toEqual(history);
                })
                .then(done, done.fail);
        });
    });

    it('can retrieve all the tribes.', function (done) {
        couplingDataService.requestTribes()
            .then(function (tribes) {
                expect(expectedTribes).toEqual(tribes);
            })
            .then(done, done.fail);
    });

    it('can save a new player', function (done) {
        var player = {name: 'Tom', email: 'Bombadil@shire.gov'};
        couplingDataService.savePlayer(player)
            .then(function () {
                return couplingDataService.requestPlayers(null);
            })
            .then(function (players) {
                var found = players.some(function (listedPlayer) {
                    return Comparators.areEqualPlayers(player, listedPlayer);
                });
                expect(found).toBe(true);
            })
            .then(done, done.fail);
    });

    describe('can remove an existing player', function () {
        beforeEach(function (done) {
            couplingDataService.removePlayer(frodo._id, done);
        });

        it('such that it no longer appears in the players list', function (done) {
            couplingDataService.requestPlayers(null)
                .then(function (players) {
                    var result = players.some(function (player) {
                        return Comparators.areEqualPlayers(frodo, player);
                    });
                    expect(result).toBe(false);
                })
                .then(done, done.fail);
        });

        it('such that it still exists in the database', function (done) {
            playersCollection.find({_id: frodo._id}, {})
                .then(function (documents) {
                    expect(Comparators.areEqualPlayers(documents[0], frodo)).toBe(true);
                })
                .then(done, done.fail);
        });
    });

    describe('can remove old pair assignments', function () {
        beforeEach(function (done) {
            couplingDataService.removePairAssignments(pairSetOne._id)
                .then(done, done.fail);
        });

        it('such that it no longer appears in history', function (done) {
            couplingDataService.requestHistory(null).then(function (historyDocuments) {
                var result = historyDocuments.some(function (assignments) {
                    return Comparators.areEqualObjectIds(pairSetOne._id, assignments._id);
                });
                expect(result).toBe(false);
                done();
            }).then(done, done.fail);
        });

        it('such that it still exists in the database', function (done) {
            historyCollection.find({_id: pairSetOne._id}, {})
                .then(function (documents: Entity[]) {
                    expect(Comparators.areEqualObjectIds(documents[0]._id, pairSetOne._id)).toBe(true);
                })
                .then(done, done.fail);
        });
    });

    it('will report an error on the callback when it does not remove pair assignments', function (done) {
        couplingDataService.removePairAssignments(monk.id())
            .then(function () {
                fail('This should return an error.')
            }, function (error) {
                expect(error.message).toEqual('Pair Assignments could not be deleted because they do not exist.');
            })
            .then(done, done.fail);
    });

    it('can update an existing player', function (done) {
        frodo.name = "F. Swaggins";
        couplingDataService.savePlayer(frodo)
            .then(function () {
                return couplingDataService.requestPlayers(null)
            })
            .then(function (players) {
                var found = players.some(function (listedPlayer) {
                    return Comparators.areEqualPlayers(frodo, listedPlayer);
                });
                expect(found).toBe(true);
            })
            .then(done, done.fail);
    });

    describe('will filter based on the tribe name', function () {
        var tribeId = 'Blackrock';
        var ogrim = {tribe: tribeId, name: 'Orgrim'};
        var garrosh = {tribe: tribeId, name: 'Garrosh'};
        var blackrockPlayers = [
            ogrim,
            garrosh
        ];

        var blackrockPins = [
            {name: "Chief", tribe: tribeId},
            {name: "Warrior", tribe: tribeId}
        ];

        var blackrockPairAssignments = {
            tribe: tribeId,
            pairs: [
                [garrosh, ogrim]
            ]
        };

        beforeEach(function (done) {
            playersCollection.insert(blackrockPlayers)
                .then(function () {
                    return pinCollection.insert(blackrockPins);
                })
                .then(function () {
                    return historyCollection.insert(blackrockPairAssignments);
                })
                .then(done, done.fail);
        });

        it('and get the correct players.', function (done) {
            couplingDataService.requestPlayers(tribeId).then(function (players) {
                expect(blackrockPlayers).toEqual(players);
            }).then(done, done.fail);
        });

        it('get the correct pins', function (done) {
            couplingDataService.requestPins(tribeId)
                .then(function (pins) {
                    expect(pins).toEqual(blackrockPins);
                })
                .then(done, done.fail);
        });

        it('and get the correct history.', function (done) {
            couplingDataService.requestHistory(tribeId).then(function (history) {
                expect([blackrockPairAssignments]).toEqual(history);
            }).then(done, done.fail);
        });

        it('and get the correct player and history together.', function (done) {
            couplingDataService.requestPlayersAndHistory(tribeId).then(function (both) {
                expect(blackrockPlayers).toEqual(both.players);
                expect([blackrockPairAssignments]).toEqual(both.history);
            }).then(done, done.fail);
        });

        it('and get the correct pins and history together.', function (done) {
            couplingDataService.requestPinsAndHistory(tribeId).then(function (both) {
                expect(blackrockPins).toEqual(both.pins);
                expect([blackrockPairAssignments]).toEqual(both.history);
            }).then(done, done.fail);
        });
    });
});
