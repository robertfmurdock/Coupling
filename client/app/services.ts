import * as angular from "angular";
import "angular-resource";
import * as map from "ramda/src/map";
import * as find from "ramda/src/find";
import * as mergeAll from "ramda/src/mergeAll";
import * as pipe from "ramda/src/pipe";
import * as flatten from "ramda/src/flatten";
import * as propEq from "ramda/src/propEq";
import Player from "../../common/Player";
import * as common from "../../common/index";
import Randomizer from "./Randomizer";
import IPromise = angular.IPromise;
import IResource = angular.resource.IResource;
import IResourceClass = angular.resource.IResourceClass;
import IResourceService = angular.resource.IResourceService;
import IResourceArray = angular.resource.IResourceArray;
import IQService = angular.IQService;
import IHttpService = angular.IHttpService;
import IHttpPromiseCallbackArg = angular.IHttpPromiseCallbackArg;

interface SelectablePlayerMap {
    [id: string]: SelectablePlayer;
}

class CouplingData {
    selectablePlayers: SelectablePlayerMap
}

interface Tribe extends IResource<Tribe>, common.Tribe {
}

interface TribeResource extends IResourceClass<Tribe> {
}

interface PairAssignmentSetResource extends IResourceClass<PairAssignmentSet> {
}

interface PairAssignmentSet extends IResource<PairAssignmentSet>, common.PairAssignmentSet {
}

class Pin {
}

class SelectablePlayer {
    constructor(public isSelected: boolean, public player: Player) {
    }
}

const makeTribeResource = function ($resource: angular.resource.IResourceService) {
    return <TribeResource>$resource('/api/tribes/:tribeId', {tribeId: '@id'});
};


const makePairAssignmentResource = function ($resource: angular.resource.IResourceService) {
    return <PairAssignmentSetResource>$resource('/api/:tribeId/history/:id', {
        id: '@_id',
        tribeId: '@tribe'
    });
};

class Coupling {
    static $inject = ['$http', '$q', '$resource'];

    data: CouplingData;
    Tribe: TribeResource;
    PairAssignmentSet: PairAssignmentSetResource;

    constructor(public $http: IHttpService, public $q: IQService, $resource: IResourceService) {
        this.Tribe = makeTribeResource($resource);
        this.PairAssignmentSet = makePairAssignmentResource($resource);
        this.data = new CouplingData();
        this.data.selectablePlayers = {};
    }

    public getTribes(): IPromise<IResourceArray<Tribe>> {
        return this.Tribe
            .query()
            .$promise;
    }

    getTribe(tribeId): IPromise<Tribe> {
        return this.Tribe
            .get({tribeId: tribeId})
            .$promise;
    }

    getHistory(tribeId): IPromise<PairAssignmentSet[]> {
        return this.PairAssignmentSet
            .query({tribeId: tribeId})
            .$promise;
    }

    spin(players, tribeId): IPromise<PairAssignmentSet> {
        return this.$http.post(`/api/${tribeId}/spin`, players)
            .then(result => new this.PairAssignmentSet(result.data));
    }

    saveCurrentPairAssignments(pairAssignments: PairAssignmentSet) {
        return pairAssignments.$save();
    }

    getPlayers(tribeId) {
        return this.$http.get(`/api/${tribeId}/players`)
            .then(response => response.data);
    }

    savePlayer(player) {
        return this.post(`/api/${player.tribe}/players`, player);
    }

    removePlayer(player) {
        return this.httpDelete(`/api/${player.tribe}/players/${player._id}`);
    }

    getSelectedPlayers(players: Player[], history) {
        this.data.selectablePlayers = this.makeSelectablePlayerFinder(history)(players);
        return this.data.selectablePlayers;
    }

    getPins(tribeId): IPromise<[Pin]> {
        return this.$http.get(`/api/${tribeId}/pins`)
            .then(response => response.data);
    }

    getRetiredPlayers(tribeId) {
        return this.$http.get(`/api/${tribeId}/players/retired`)
            .then(response => response.data);
    }

    private post<T>(url, object: T): IPromise<T> {
        return this.$http.post(url, object)
            .then(response => response.data);
    }

    private httpDelete(url): IPromise<void> {
        return this.$http.delete(url)
            .then(() => undefined);
    }

    private isInLastSetOfPairs(player, history) {
        const flattenResult = flatten(history[0].pairs);
        const result = find(propEq('_id', player._id), flattenResult);
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

    private makeSelectablePlayerFinder(history: any): (players) => SelectablePlayerMap {
        return pipe(
            this.mapPlayerToSelection(history),
            mergeAll
        )
    }

    private mapPlayerToSelection(history) {
        return map(player => {
            const selected = this.playerShouldBeSelected(player, history);
            return {[player._id]: new SelectablePlayer(selected, player)};
        })
    }

}

angular.module("coupling.services", ['ngResource'])
    .service("Coupling", Coupling)
    .service('randomizer', Randomizer);

export {Player, Tribe, PairAssignmentSet, SelectablePlayer, Coupling, Randomizer}