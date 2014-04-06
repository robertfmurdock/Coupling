var CouplingWheel = require('../../lib/CouplingWheel');
var should = require('should');
var sinon = require('sinon');

describe('Coupling Wheel', function () {

    it('randomly chooses a person on the wheel', function () {
        var players = ['Scooby', 'Shaggy', 'Scrappy'];
        var randomStub = sinon.stub(Math, 'random');

        function checkWorksForIndex(expectedIndex) {
            randomStub.returns(expectedIndex / players.length);
            var couplingWheel = new CouplingWheel();
            var foundPlayer = couplingWheel.spin(players);
            var expectedPlayer = players[expectedIndex];
            should(expectedPlayer).eql(foundPlayer);
        }

        checkWorksForIndex(1);
        checkWorksForIndex(0);
        checkWorksForIndex(2);
    });
});

