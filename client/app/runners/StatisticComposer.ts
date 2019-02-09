import PairAssignmentDocument from "../../../common/PairAssignmentDocument";
import Tribe from "../../../common/Tribe";
import Player from "../../../common/Player";
import * as distanceInWords from "date-fns/distance_in_words"
// @ts-ignore
import * as commonKt from 'commonKt'
// @ts-ignore
import {performComposeStatisticsAction} from 'client'

let ComposeStatisticsActionDispatcher = commonKt.com.zegreatrob.coupling.common.ComposeStatisticsActionDispatcher;

const dispatcher = new ComposeStatisticsActionDispatcher();

export default class StatisticComposer {

    compose(tribe: Tribe, players: Player[], history: PairAssignmentDocument[]) {
        const result = performComposeStatisticsAction(dispatcher, tribe, players, history);

        return {
            spinsUntilFullRotation: result.spinsUntilFullRotation,
            pairReports: result.pairReports,
            medianSpinDuration: result.medianSpinDuration === null
                ? 'N/A'
                : distanceInWords(0, result.medianSpinDuration)
        };
    }
}