import * as express from "express";
import * as monk from "monk";
import * as AuthorizedTribesFetcher from "../lib/AuthorizedTribesFetcher";

class TribeRoutes {
    public list = (request, response) => {
        AuthorizedTribesFetcher.requestAuthorizedTribes(request.user, request.dataService)
            .then(function (authorizedTribes) {
                response.send(authorizedTribes);
            })
            .catch(function (error) {
                response.statusCode = 500;
                response.send(error.message);
            });
    };

    public get = (request, response) => {
        AuthorizedTribesFetcher.promiseTribeAndAuthorization(request)
            .then(function ({isAuthorized, tribe}) {
                if (isAuthorized) {
                    response.send(tribe);
                } else {
                    response.statusCode = 404;
                    response.send({message: 'Tribe not found.'});
                }
                return isAuthorized;
            })
            .catch(function (error) {
                console.log(error);
                response.statusCode = 500;
                response.send(error.message);
            });
    };

    public save = (request, response) => {
        const database = request.dataService.database;
        const tribesCollection = database.get('tribes');
        const usersCollection = request.userDataService.database.get('users');
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