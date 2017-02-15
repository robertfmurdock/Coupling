import StatisticComposer from "../../../server/lib/StatisticComposer";
import Player from "../../../common/Player";
import Tribe from "../../../common/Tribe";
import PairAssignmentDocument from "../../../common/PairAssignmentDocument";

const statComposer = new StatisticComposer();

describe('StatisticComposer', function () {


    describe('will include the full rotation number', function () {

        const tribe: Tribe = {id: 'LOL', name: 'LOL'};
        const history: PairAssignmentDocument[] = [];

        it('and it will be 1 with one player', function () {
            const players: Player[] = [{_id: '1', tribe: tribe.id}];
            const result = statComposer.compose(tribe, players, history);
            expect(result.spinsUntilFullRotation).toBe(1);
        });
    });

});