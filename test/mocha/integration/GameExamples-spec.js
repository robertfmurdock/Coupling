var CouplingGameFactory = require('../../../lib/CouplingGameFactory');

describe('The game', function () {

    it('works', function () {
        var couplingGameFactory = new CouplingGameFactory();
        var game = couplingGameFactory.buildGame();

        var playerRoster = [
            {name: "Superman"},
            {name: "Batman"},
            {name: "Wonder Woman"},
            {name: "Green Lantern"},
            {name: "Flash"},
            {name: "Martian Manhunter"}
        ];
        var result = game.play(playerRoster);

        console.info(result);
    });
});
