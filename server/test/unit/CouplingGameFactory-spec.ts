import CouplingGameFactory from "../../lib/CouplingGameFactory";
import CouplingGame from "../../lib/CouplingGame";
import CouplingWheel from "../../lib/CouplingWheel";

describe('Coupling Game Factory', function () {
    it('will construct a Coupling Game', function () {
        var factory = new CouplingGameFactory();
        var game = factory.buildGame([]);

        expect(game.constructor).toBe(CouplingGame);
        expect(game.wheel.constructor).toBe(CouplingWheel);
    });
});