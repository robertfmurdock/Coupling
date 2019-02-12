import * as express from "express";

class TribeRoutes {
    public list = (request, response) => {
        request.commandDispatcher.performTribeListQuery()
            .then(function (authorizedTribes) {
                response.send(authorizedTribes);
            })
            .catch(function (error) {
                console.log(error)
                response.statusCode = 500;
                response.send(error.message);
            });
    };

    public get = (request, response) => {
        request.commandDispatcher.performTribeQuery(request.params.tribeId)
            .then(function (tribe) {
                if (tribe !== null) {
                    response.send(tribe);
                } else {
                    response.statusCode = 404;
                    response.send({message: 'Tribe not found.'});
                }
            })
            .catch(function (error) {
                console.log(error);
                response.statusCode = 500;
                response.send(error.message);
            });
    };

    public save = async (request, response) => {
        request.commandDispatcher.performSaveTribeCommand(request.body)
            .then(function (isSuccessful) {
                if (isSuccessful) {
                    const usersCollection = request.userDataService.database.get('users');
                    usersCollection.update({_id: request.user._id}, {$addToSet: {tribes: request.body.id}});
                    response.send(request.body)
                } else {
                    response.sendStatus(400);
                }
            })
            .catch(function (error) {
                console.log(error);
                response.statusCode = 500;
                response.send(error.message);
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