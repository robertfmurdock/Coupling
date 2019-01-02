import Tribe from "../../common/Tribe";
import * as Promise from "bluebird";
import * as __ from "ramda/src/__";
import * as filter from "ramda/src/filter";
import * as where from "ramda/src/where";
import * as contains from "ramda/src/contains";
import * as union from "ramda/src/union";
import * as pipe from "ramda/src/pipe";
import * as pluck from "ramda/src/pluck";

function filterToAuthorizedTribes(authorizedTribeIds: string[], tribes2: Tribe[]) {
    return filter(where({id: contains(__, authorizedTribeIds)}), tribes2)
}

export function requestAuthorizedTribes(user, dataService) {
    return Promise.props({
        tribes: dataService.requestTribes(),
        authorizedTribeIds: this.loadAuthorizedTribeIds(user, dataService)
    })
        .then(function (hash: any) {
            const {authorizedTribeIds, tribes} = hash;
            return filterToAuthorizedTribes(authorizedTribeIds, tribes);
        });
}

export function loadAuthorizedTribeIds(user, dataService) {
    const playersCollection = dataService.database.get('players');
    let email = user.email;
    const tempSuffixIndex = email.indexOf('._temp');
    if (tempSuffixIndex != -1) {
        email = email.substring(0, tempSuffixIndex);
    }

    return playersCollection.find({email: email})
        .then(function (documents) {
            return pipe(
                pluck('tribe'),
                union(user.tribes)
            )(documents);
        });
}

export function promiseTribeAndAuthorization(request) {
    return Promise.props({
        tribe: request.dataService.requestTribe(request.params.tribeId),
        authorizedTribeIds: loadAuthorizedTribeIds(request.user, request.dataService)
    }).then(function (hash: any) {
        return {isAuthorized: contains(hash.tribe.id, hash.authorizedTribeIds), tribe: hash.tribe};
    })
}