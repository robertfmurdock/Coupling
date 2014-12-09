var CouplingWheel = require('../../server/lib/CouplingWheel');
var expect = require('chai').expect;
var sinon = require('sinon');

describe('Coupling Wheel', function () {
    it('randomly chooses a person on the wheel', sinon.test(function () {
        var players = ['Scooby', 'Shaggy', 'Scrappy'];
        var randomStub = this.stub(Math, 'random');

        function checkWorksForIndex(expectedIndex) {
            randomStub.returns(expectedIndex / players.length);
            var couplingWheel = new CouplingWheel();
            var foundPlayer = couplingWheel.spin(players);
            var expectedPlayer = players[expectedIndex];
            expect(expectedPlayer).eql(foundPlayer);
        }

        checkWorksForIndex(1);
        checkWorksForIndex(0);
        checkWorksForIndex(2);
    }));

    it('randomly chooses a person on the wheel even without whole numbers', sinon.test(function () {
        var players = ['Scooby', 'Shaggy', 'Scrappy'];
        var randomStub = this.stub(Math, 'random');

        randomStub.returns(1.7 / players.length);
        var couplingWheel = new CouplingWheel();
        var foundPlayer = couplingWheel.spin(players);
        var expectedPlayer = players[1];
        expect(expectedPlayer).eql(foundPlayer);
    }));
});

