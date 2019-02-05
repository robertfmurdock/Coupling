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
import axios from 'axios'
import Tribe from "../../common/Tribe";
import PairAssignmentSet from "../../common/PairAssignmentSet";

interface SelectablePlayerMap {
    [id: string]: SelectablePlayer;
}

class CouplingData {
    selectablePlayers: SelectablePlayerMap
}

class Pin {
}

class SelectablePlayer {
    constructor(public isSelected: boolean, public player: Player) {
    }
}

class Coupling {

    static async saveTribe(tribe: common.Tribe) {
        await axios.post(`/api/tribes`, angular.copy(tribe))
    }

    data: CouplingData;

    constructor() {
        this.data = new CouplingData();
        this.data.selectablePlayers = {};
    }

    public async getTribes(): Promise<Tribe[]> {
        const tribesResponse = await axios.get('/api/tribes');
        return tribesResponse.data;
    }

    async getTribe(tribeId): Promise<Tribe> {
        const tribesResponse = await axios.get(`/api/tribes/${tribeId}`);
        return tribesResponse.data;
    }

    async getHistory(tribeId): Promise<PairAssignmentSet[]> {
        const response = await axios.get(`/api/${tribeId}/history`);
        return response.data;
    }

    async removeAssignments(entry: PairAssignmentSet) {
        const response = await axios.delete(`/api/${entry.tribe}/history/${entry._id}`);
        return response.data;
    }

    async spin(players, tribeId): Promise<PairAssignmentSet> {
        const response = await axios.post(`/api/${tribeId}/spin`, angular.copy(players));
        return response.data;
    }

    async saveCurrentPairAssignments(pairAssignments: common.PairAssignmentSet) {
        const response = await axios.post(`/api/${pairAssignments.tribe}/history`, angular.copy(pairAssignments));
        return response.data;
    }

    async getPlayers(tribeId) {
        const response = await axios.get(`/api/${tribeId}/players`);
        return response.data;
    }

    savePlayer(player, tribeId: string) {
        return this.post(`/api/${tribeId}/players`, player);
    }

    removePlayer(player, tribeId: string) {
        return axios.delete(`/api/${tribeId}/players/${player._id}`);
    }

    getSelectedPlayers(players: Player[], history) {
        this.data.selectablePlayers = this.makeSelectablePlayerFinder(history)(players);
        return this.data.selectablePlayers;
    }

    async getPins(tribeId): Promise<Pin[]> {
        const response = await axios.get(`/api/${tribeId}/pins`);
        return response.data;
    }

    async getRetiredPlayers(tribeId) {
        const response = await axios.get(`/api/${tribeId}/players/retired`);
        return response.data;
    }

    private async post<T>(url, object: T): Promise<T> {
        const response = await axios.post(url, angular.copy(object));
        return response.data;
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

export {SelectablePlayer, Coupling, Randomizer}