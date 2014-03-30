var CouplingWheelFactory = require('../../lib/CouplingWheelFactory');
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

    before(function (beforeIsDone) {
        var playersCollection = database.get('players');
        playersCollection.drop();
        playersCollection.insert(expectedPlayers, beforeIsDone);
    });


    it('starts with all the players in the database', function (testIsDone) {
        CouplingWheelFactory(mongoUrl, function(couplingWheel){
            should(expectedPlayers).eql(couplingWheel.players);
            testIsDone();
        });
    });




});
