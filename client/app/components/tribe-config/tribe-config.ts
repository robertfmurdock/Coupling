import {module} from "angular";
import * as template from "./tribe-config.pug";
import * as services from "../../services";
import PairingRule from "../../../../common/PairingRule";
import * as _ from "underscore";

export class TribeConfigController {
    static $inject = ['$location'];
    public tribe: services.Tribe;
    public isNew: boolean;
    public pairingRules;

    constructor(public $location: angular.ILocationService) {
        this.pairingRules = [
            {id: PairingRule.LongestTime, description: "Prefer Longest Time"},
            {id: PairingRule.PreferDifferentBadge, description: "Prefer Different Badges (Beta)"},
        ];
    }

    $onInit() {
        _.defaults(this.tribe, {
            pairingRule: PairingRule.LongestTime,
            defaultBadgeName: 'Default',
            alternateBadgeName: 'Alternate',
        });
    }

    clickSaveButton() {
        this.tribe
            .$save()
            .then(() => this.$location.path("/tribes"));
    }

}

export default module("coupling.tribeConfig", [])
    .controller('TribeConfigController', TribeConfigController)
    .directive('tribeConfig', function () {
        return {
            controller: 'TribeConfigController',
            controllerAs: 'self',
            bindToController: true,
            scope: {
                tribe: '=tribe',
                isNew: '=isNew'
            },
            restrict: 'E',
            template: template
        }
    });