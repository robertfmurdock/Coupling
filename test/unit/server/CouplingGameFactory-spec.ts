import CouplingGameFactory from "../../../server/lib/CouplingGameFactory";
import CouplingGame from "../../../server/lib/CouplingGame";
import CouplingWheel from "../../../server/lib/CouplingWheel";

describe('Coupling Game Factory', function () {
    it('will construct a Coupling Game', function () {
        var factory = new CouplingGameFactory();
        var game = factory.buildGame([]);

        expect(game.constructor).toBe(CouplingGame);
        expect(game.wheel.constructor).toBe(CouplingWheel);
    });
});