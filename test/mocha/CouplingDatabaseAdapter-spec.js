var CouplingDatabaseAdapter = require('../../lib/CouplingDatabaseAdapter');
var should = require('should');
var mongoUrl = 'localhost/CouplingTest';
var monk = require('monk');
var database = monk(mongoUrl);

describe('Coupling Wheel Factory', function () {

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

    before(function (beforeIsDone) {
        var playersCollection = database.get('players');
        playersCollection.drop();
        playersCollection.insert(expectedPlayers);

        var historyCollection = database.get('history');
        historyCollection.drop();
        historyCollection.insert(unorderedHistory, beforeIsDone);
    });

    it('starts with all the players in the database and all the history in order from ', function (testIsDone) {
        CouplingDatabaseAdapter(mongoUrl, function (players, history) {
            should(expectedPlayers).eql(players);
            should(expectedHistory).eql(history);
            testIsDone();
        });
    });

});
