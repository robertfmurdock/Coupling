import CouplingWheel from "../../../server/lib/CouplingWheel";

describe('Coupling Wheel', function () {
    it('randomly chooses a person on the wheel', function () {
        var players = ['Scooby', 'Shaggy', 'Scrappy'];
        var randomStub = spyOn(Math, 'random');

        function checkWorksForIndex(expectedIndex) {
            randomStub.and.returnValue(expectedIndex / players.length);
            var couplingWheel = new CouplingWheel();
            var foundPlayer = couplingWheel.spin(players);
            var expectedPlayer = players[expectedIndex];
            expect(expectedPlayer).toEqual(foundPlayer);
        }

        checkWorksForIndex(1);
        checkWorksForIndex(0);
        checkWorksForIndex(2);
    });


    it('randomly chooses a person on the wheel even without whole numbers', function () {
        var players = ['Scooby', 'Shaggy', 'Scrappy'];
        var randomStub = spyOn(Math, 'random');

        randomStub.and.returnValue(1.7 / players.length);
        var couplingWheel = new CouplingWheel();
        var foundPlayer = couplingWheel.spin(players);
        var expectedPlayer = players[1];
        expect(expectedPlayer).toEqual(foundPlayer);
    });
});

