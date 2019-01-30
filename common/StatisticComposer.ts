import PairAssignmentDocument from "./PairAssignmentDocument";
import Tribe from "./Tribe";
import Player from "./Player";
import * as distanceInWords from "date-fns/distance_in_words"
// @ts-ignore
import {ComposeStatisticsActionDispatcher} from 'commonKt'
const dispatcher = new ComposeStatisticsActionDispatcher();

export default class StatisticComposer {

    compose(tribe: Tribe, players: Player[], history: PairAssignmentDocument[]) {
        const result = dispatcher.performComposeStatisticsAction(tribe, players, history);

        return {
            spinsUntilFullRotation: result.spinsUntilFullRotation,
            pairReports: result.pairReports,
            medianSpinDuration: result.medianSpinDuration === null
                ? 'N/A'
                : distanceInWords(0, result.medianSpinDuration)
        };
    }
}