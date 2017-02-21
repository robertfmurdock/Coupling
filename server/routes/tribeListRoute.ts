import * as express from "express";
import * as monk from "monk";
import * as Promise from "bluebird";
import * as union from "ramda/src/union";
import * as pipe from "ramda/src/pipe";
import * as pluck from "ramda/src/pluck";
import * as filter from "ramda/src/filter";
import * as where from "ramda/src/where";
import * as contains from "ramda/src/contains";
import * as __ from "ramda/src/__";
import Tribe from "../../common/Tribe";

const config = require('../../config');

function filterToAuthorizedTribes(authorizedTribeIds: string[], tribes2: Tribe[]) {
    return filter(where({id: contains(__, authorizedTribeIds)}), tribes2)
}

class TribeRoutes {

    private loadAuthorizedTribeIds(user, mongoUrl) {
        const database = monk(mongoUrl);
        const playersCollection = database.get('players');
        let email = user.email;
        const tempSuffixIndex = email.indexOf('._temp');
        if (tempSuffixIndex != -1) {
            email = email.substring(0, tempSuffixIndex);
        }

        return playersCollection.find({email: email}).then(function (documents) {
            return pipe(
                pluck('tribe'),
                union(user.tribes)
            )(documents);
        });
    }

    private requestAuthorizedTribes(user, dataService) {
        return Promise.props({
            tribes: dataService.requestTribes(),
            authorizedTribeIds: this.loadAuthorizedTribeIds(user, dataService.mongoUrl)
        })
            .then(function (hash: any) {
                const {authorizedTribeIds, tribes} = hash;
                return filterToAuthorizedTribes(authorizedTribeIds, tribes);
            });
    }

    public list = (request, response) => {
        this.requestAuthorizedTribes(request.user, request.dataService)
            .then(function (authorizedTribes) {
                response.send(authorizedTribes);
            })
            .catch(function (error) {
                response.statusCode = 500;
                response.send(error.message);
            });
    };

    public get = (request, response) => {
        Promise.props({
            tribe: request.dataService.requestTribe(request.params.tribeId),
            authorizedTribeIds: this.loadAuthorizedTribeIds(request.user, request.dataService.mongoUrl)
        })
            .then(function (hash: any) {
                const isAuthorized = contains(hash.tribe.id, hash.authorizedTribeIds);
                if (isAuthorized) {
                    response.send(hash.tribe);
                } else {
                    response.statusCode = 404;
                    response.send({message: 'Tribe not found.'});
                }
                return isAuthorized;
            })
            .catch(function (error) {
                response.statusCode = 500;
                response.send(error.message);
            });
    };

    public save = (request, response) => {
        const database = monk(request.dataService.mongoUrl);
        const tribesCollection = database.get('tribes');
        const usersCollection = monk(config.mongoUrl).get('users');
        const tribeJSON = request.body;
        tribeJSON._id = tribeJSON._id || monk.id();
        tribesCollection.update({id: tribeJSON.id}, tribeJSON, {upsert: true}, function () {
            usersCollection.update({_id: request.user._id}, {$addToSet: {tribes: tribeJSON.id}});
            response.send(request.body);
        });
    };
}

const tribes = new TribeRoutes();
const router = express.Router({mergeParams: true});
router.route('/')
    .get(tribes.list)
    .post(tribes.save);

router.route('/:tribeId')
    .get(tribes.get)
    .post(tribes.save);

export default router