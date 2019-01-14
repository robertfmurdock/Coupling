import GameRunner from "../../lib/GameRunner";
import * as Clock from '../../lib/Clock';
import PairingRule from "../../../common/PairingRule";
import Pair from "../../../common/Pair";

describe('Game Runner', function () {
    it('will build a game, run with all available players, and then return the results', function () {
        const players = [];
        const history = [];

        const playStub = jasmine.createSpy('play');

        const pairingAssignments: Pair[] = [
            [{tribe: '1'}],
            [{tribe: '1'}]
        ];
        playStub.and.returnValue(pairingAssignments);
        const gameRunner = new GameRunner({runSpinCommand: playStub});

        const expectedDate = new Date();
        spyOn(Clock, 'getDate').and.returnValue(expectedDate);

        const tribe = {id: 'Team Rocket', pairingRule: PairingRule.LongestTime};
        const result = gameRunner.run(players, [], history, tribe);

        expect(result.date).toEqual(expectedDate);
        expect(result.pairs).toEqual(pairingAssignments);
        expect(result.tribe).toEqual(tribe.id);
    });
});