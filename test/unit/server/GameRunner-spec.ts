import CouplingGameFactory from "../../../server/lib/CouplingGameFactory";
import GameRunner from "../../../server/lib/GameRunner";
import * as Clock from '../../../server/lib/Clock';
import PairingRule from "../../../common/PairingRule";

describe('Game Runner', function () {
    it('will build a game, run with all available players, and then return the results', function () {
        const players = [];
        const history = [];

        const couplingGameFactory = new CouplingGameFactory();
        const buildStub = spyOn(couplingGameFactory, 'buildGame');
        const playStub = jasmine.createSpy('play');
        buildStub.and.returnValue({play: playStub});

        const pairingAssignments = [
            {},
            {}
        ];
        playStub.and.returnValue(pairingAssignments);
        const gameRunner = new GameRunner(couplingGameFactory);

        const expectedDate = new Date();
        spyOn(Clock, 'getDate').and.returnValue(expectedDate);

        const tribe = {id: 'Team Rocket', pairingRule: PairingRule.LongestTime};
        const result = gameRunner.run(players, [], history, tribe);

        expect(result.date).toEqual(expectedDate);
        expect(result.pairs).toEqual(pairingAssignments);
        expect(result.tribe).toEqual(tribe.id);
    });
});