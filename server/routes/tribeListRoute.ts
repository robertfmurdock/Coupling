import * as express from "express";
import {handleRequest} from "./route-helper";

class TribeRoutes {

    public list = handleRequest(
        commandDispatcher => commandDispatcher.performTribeListQuery(),
        (response, authorizedTribes) => response.send(authorizedTribes)
    );

    public get = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performTribeQuery(request, response),
        () => {
        }
    );

    public save = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performSaveTribeCommand(request),
        (response, isSuccessful, request) => {
            if (isSuccessful) {
                response.send(request.body)
            } else {
                response.sendStatus(400);
            }
        }
    );

    public delete = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performDeleteTribeCommand(request),
        (response, tribe) => {
            if (tribe !== null) {
                response.send(tribe);
            } else {
                response.statusCode = 404;
                response.send({message: 'Tribe not found.'});
            }
        }
    );
}

const tribes = new TribeRoutes();
const router = express.Router({mergeParams: true});
router.route('/')
    .get(tribes.list)
    .post(tribes.save);

router.route('/:tribeId')
    .get(tribes.get)
    .post(tribes.save)
    .delete(tribes.delete);

export default router