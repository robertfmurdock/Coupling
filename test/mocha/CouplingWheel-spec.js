var CouplingWheel = require('../../lib/CouplingWheel');
var should = require('should');
var sinon = require('sinon');

describe('Coupling Wheel', function () {

    it('should know the players on the wheel', function () {
        var players = ['Scooby', 'Shaggy', 'Scrappy'];
        var couplingWheel = new CouplingWheel(players);

        should(players).eql(couplingWheel.players);
    });

    it('randomly chooses a person on the wheel', function () {
        var players = ['Scooby', 'Shaggy', 'Scrappy'];
        var randomStub = sinon.stub(Math, 'random');

        function checkWorksForIndex(expectedIndex) {
            randomStub.returns(expectedIndex / players.length);
            var couplingWheel = new CouplingWheel(players);
            var foundPlayer = couplingWheel.spin();
            var expectedPlayer = players[expectedIndex];
            should(expectedPlayer).eql(foundPlayer);
        }

        checkWorksForIndex(1);
        checkWorksForIndex(0);
        checkWorksForIndex(2);
    });
});

