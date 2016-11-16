import "angular"
import 'angular-resource'
import * as _ from 'underscore'
import IPromise = angular.IPromise;

export class Player {
    _id:string;
    tribe:string
}

interface SelectablePlayerMap {
    [id: string]: SelectablePlayer;
}

class CouplingData {
    selectablePlayers:SelectablePlayerMap
}

export interface Tribe extends ng.resource.IResource<Tribe> {
    id: String;
    name:String
}

interface TribeResource extends ng.resource.IResourceClass<Tribe> {
    name:String
}

interface PairAssignmentSetResource extends ng.resource.IResourceClass<PairAssignmentSet> {
}

export interface PairAssignmentSet extends ng.resource.IResource<PairAssignmentSet> {
    pairs:[[Player]]
}

class Pin {
}

export class SelectablePlayer {
    constructor(public isSelected:boolean, public player:Player) {
    }
}

export class Coupling {
    static $inject = ['$http', '$q', '$resource'];

    data:CouplingData;
    Tribe:TribeResource;
    PairAssignmentSet:PairAssignmentSetResource;

    constructor(public $http:angular.IHttpService, public $q:angular.IQService, $resource:ng.resource.IResourceService) {
        this.Tribe = <TribeResource>$resource('/api/tribes/:tribeId', {tribeId: '@id'});
        this.PairAssignmentSet = <PairAssignmentSetResource>$resource('/api/:tribeId/history/:id', {
            id: '@_id',
            tribeId: '@tribe'
        });
        this.data = new CouplingData();
        this.data.selectablePlayers = {};
    }

    public getTribes():IPromise<ng.resource.IResourceArray<Tribe>> {
        return this.Tribe
            .query()
            .$promise;
    }

    getTribe(tribeId):IPromise<Tribe> {
        return this.Tribe
            .get({tribeId: tribeId})
            .$promise;
    }

    getHistory(tribeId):IPromise<[PairAssignmentSet]> {
        return this.PairAssignmentSet
            .query({tribeId: tribeId})
            .$promise;
    }

    spin(players, tribeId):IPromise<PairAssignmentSet> {
        var url = '/api/' + tribeId + '/spin';
        return this.$http.post(url, players)
            .then((result) => {
                return new this.PairAssignmentSet(result.data);
            });
    }

    saveCurrentPairAssignments(pairAssignments:PairAssignmentSet) {
        return pairAssignments.$save();
    }

    getPlayers(tribeId) {
        var url = '/api/' + tribeId + '/players';
        return this.$http.get(url)
            .then(function (response:angular.IHttpPromiseCallbackArg<[Player]>) {
                return response.data;
            });
    }

    savePlayer(player) {
        return this.post('/api/' + player.tribe + '/players', player);
    }

    removePlayer(player) {
        return this.httpDelete('/api/' + player.tribe + '/players/' + player._id);
    }

    getSelectedPlayers(players:Player[], history) {
        var selectablePlayers = _.map(players, (player)=> {
            var selected = this.playerShouldBeSelected(player, history);
            return [player._id, new SelectablePlayer(selected, player)];
        });

        this.data.selectablePlayers = <SelectablePlayerMap>_.object(selectablePlayers);
        return this.data.selectablePlayers;
    }

    getPins(tribeId):IPromise<[Pin]> {
        var url = '/api/' + tribeId + '/pins';
        return this.$http.get(url)
            .then(function (response) {
                return response.data;
            });
    }

    private post<T>(url, object:T):IPromise<T> {
        return this.$http.post(url, object)
            .then(function (result) {
                return result.data;
            });
    }

    private httpDelete(url):IPromise<void> {
        return this.$http.delete(url)
            .then(function () {
            });
    }

    private isInLastSetOfPairs(player, history) {
        var result = _.find(history[0].pairs, function (pairset:[{}]) {
            if (_.findWhere(pairset, {
                    _id: player._id
                })) {
                return true;
            }
        });
        return !!result;
    }

    private playerShouldBeSelected(player, history) {
        if (this.data.selectablePlayers[player._id]) {
            return this.data.selectablePlayers[player._id].isSelected
        } else if (history.length > 0) {
            return this.isInLastSetOfPairs(player, history);
        } else {
            return true;
        }
    }

}

export class Randomizer {

    next(maxValue:number) {
        var floatValue = Math.random() * maxValue;
        return Math.round(floatValue);
    }
}

angular.module("coupling.services", ['ngResource'])
    .service("Coupling", Coupling)
    .service('randomizer', Randomizer);