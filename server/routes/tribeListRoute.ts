import * as express from "express";
import {handleRequest} from "./route-helper";

class TribeRoutes {

    public list = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performTribeListQuery(response)
    );

    public get = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performTribeQuery(request, response)
    );

    public save = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performSaveTribeCommand(request, response)
    );

    public delete = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performDeleteTribeCommand(request, response)
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