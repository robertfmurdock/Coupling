import * as express from "express";
import * as monk from "monk";
import * as Promise from "bluebird";
import * as _ from "underscore";
import Tribe from "../../common/Tribe";

var config = require('../../config');

class TribeRoutes {

    private loadAuthorizedTribeIds(user, mongoUrl) {
        var database = monk(mongoUrl);
        var playersCollection = database.get('players');
        var email = user.email;
        var tempSuffixIndex = email.indexOf('._temp');
        if (tempSuffixIndex != -1) {
            email = email.substring(0, tempSuffixIndex);
        }

        return playersCollection.find({email: email}).then(function (documents) {
            var allTribesThatHaveMembership = _.pluck(documents, 'tribe');
            return _.union(user.tribes, allTribesThatHaveMembership);
        });
    }

    private requestAuthorizedTribes(user, dataService) {
        return Promise.props({
            tribes: dataService.requestTribes(),
            authorizedTribeIds: this.loadAuthorizedTribeIds(user, dataService.mongoUrl)
        })
            .then(function (hash: any) {
                return _.filter(hash.tribes, function (value: Tribe) {
                    return _.contains(hash.authorizedTribeIds, value.id);
                });
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
            .then(function (hash : any) {
                var isAuthorized = _.contains(hash.authorizedTribeIds, hash.tribe.id);
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
        var database = monk(request.dataService.mongoUrl);
        var tribesCollection = database.get('tribes');
        var usersCollection = monk(config.mongoUrl).get('users');
        var tribeJSON = request.body;
        tribeJSON._id = tribeJSON._id || monk.id();
        tribesCollection.update({id: tribeJSON.id}, tribeJSON, {upsert: true}, function () {
            usersCollection.update({_id: request.user._id}, {$addToSet: {tribes: tribeJSON.id}});
            response.send(request.body);
        });
    };
}

var tribes = new TribeRoutes();
var router = express.Router({mergeParams: true});
router.route('/')
    .get(tribes.list)
    .post(tribes.save);

router.route('/:tribeId')
    .get(tribes.get)
    .post(tribes.save);

export default router