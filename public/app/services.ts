/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />

import IPromise = angular.IPromise;

class Player {
    _id:string;
    tribe:string
}

interface SelectablePlayerMap {
    [id: string]: SelectablePlayer;
}

class CouplingData {
    selectablePlayers:SelectablePlayerMap
}

interface Tribe extends ng.resource.IResource<Tribe> {
    _id: String;
    name:String
}

interface TribeResource extends ng.resource.IResourceClass<Tribe> {
    name:String
}

interface PairAssignmentSetResource extends ng.resource.IResourceClass<PairAssignmentSet> {
}

interface PairAssignmentSet extends ng.resource.IResource<PairAssignmentSet> {
    pairs:[[Player]]
}

class Pin {
}

class SelectablePlayer {
    constructor(public isSelected:boolean, public player:Player) {
    }
}

class Coupling {
    static $inject = ['$http', '$q', '$resource'];

    data:CouplingData;
    Tribe:TribeResource;
    PairAssignmentSet:PairAssignmentSetResource;

    constructor(public $http:angular.IHttpService, public $q:angular.IQService, $resource:ng.resource.IResourceService) {
        this.Tribe = <TribeResource>$resource('/api/tribes/:tribeId', {tribeId: '@_id'});
        this.PairAssignmentSet = <PairAssignmentSetResource>$resource('/api/:tribeId/history/:id', {id: '@_id'});
        this.data = new CouplingData();
        this.data.selectablePlayers = {};
    }

    getTribes():IPromise<ng.resource.IResourceArray<Tribe>> {
        var url = '/api/tribes';
        var self = this;
        return this.Tribe
            .query()
            .$promise
            .catch(function (response) {
                console.info(response);
                return self.$q.reject(Coupling.errorMessage('GET ' + url, response.data, response.status));
            });
    }

    requestSpecificTribe(tribeId):IPromise<Tribe> {
        var self = this;
        console.log(tribeId);
        return this.Tribe.get({tribeId: tribeId})
            .$promise;
        //.then(function (tribes) {
        //    var tribe = _.findWhere(tribes, {
        //        _id: tribeId
        //    });
        //    if (!tribe) {
        //        return self.$q.reject("Tribe not found");
        //    }
        //    return tribe;
        //})
    }

    getHistory(tribeId):IPromise<[PairAssignmentSet]> {
        return this.PairAssignmentSet
            .query({tribeId: tribeId})
            .$promise;
    }

    spin(players, tribeId):IPromise<PairAssignmentSet> {
        var url = '/api/' + tribeId + '/spin';
        return this.$http.post(url, players)
            .then(function (result) {
                return result.data;
            },
            this.logAndRejectError('POST ' + url));
    }

    saveCurrentPairAssignments(tribeId:String, pairAssignments:PairAssignmentSet) {
        var url = '/api/' + tribeId + '/history';
        return this.$http.post(url, pairAssignments)
            .then(function (result) {
                return result.data;
            },
            this.logAndRejectError('POST ' + url));
    }

    getPlayers(tribeId) {
        var url = '/api/' + tribeId + '/players';
        var self = this;
        return this.$http.get(url)
            .then(function (response:angular.IHttpPromiseCallbackArg<[Player]>) {
                return response.data;
            },
            function (response) {
                var data = response.data;
                var statusCode = response.status;
                var message = Coupling.errorMessage(url, data, statusCode);
                console.error('ALERT!\n' + message);
                return self.$q.reject(message);
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
        var self = this;
        return this.$http.get(url)
            .then(function (response) {
                return response.data;
            },
            function (response) {
                var data = response.data;
                var status = response.status;
                return self.$q.reject(Coupling.errorMessage('GET ' + url, data, status));
            });
    }

    private static errorMessage(url, data, statusCode) {
        return "There was a problem with request " + url + "\n" +
            "Data: <" + data + ">\n" +
            "Status: " + statusCode;
    }

    private logAndRejectError(url) {
        var self = this;
        return function (response) {
            var data = response.data;
            var statusCode = response.status;
            var message = Coupling.errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return self.$q.reject(message);
        }
    }

    private post<T>(url, object:T):IPromise<T> {
        return this.$http.post(url, object)
            .then(function (result) {
                return result.data;
            },
            this.logAndRejectError('POST ' + url));
    }

    private httpDelete(url):IPromise<void> {
        return this.$http.delete(url)
            .then(function () {
            },
            this.logAndRejectError(url));
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

class Randomizer {

    next(maxValue:number) {
        var floatValue = Math.random() * maxValue;
        return Math.round(floatValue);
    }
}

angular.module("coupling.services", ['ngResource'])
    .service("Coupling", Coupling)
    .service('randomizer', Randomizer);