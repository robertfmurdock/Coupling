import * as _ from 'underscore'
import History from "./history/history";
import PairAssignments from "./pair-assignments/pair-assignments";
import PinList from "./pin-list/pin-list";
import PlayerCard from "./player-card/player-card";
import PlayerConfig from './player-config/player-config'
import PlayerRoster from './player-roster/player-roster'
import Prepare from './prepare/prepare'
import TribeCard from './tribe-card/tribe-card'
import TribeConfig from './tribe-config/tribe-config'
import TribeList from './tribe-list/tribe-list'
import Welcome from './welcome/welcome'
import {Tribe} from "../services";

console.log('defining coupling.component');
angular.module('coupling.component', [
        'coupling.services',
        'ngFitText',
        PairAssignments.name,
        History.name,
        PinList.name,
        PlayerConfig.name,
        PlayerCard.name,
        PlayerRoster.name,
        Prepare.name,
        TribeCard.name,
        TribeConfig.name,
        TribeList.name,
        Welcome.name,
    ])
    .config(['fitTextConfigProvider', function (fitTextConfigProvider) {
        fitTextConfigProvider.config = {
            debounce: _.debounce,
            delay: 1000
        };
    }]);