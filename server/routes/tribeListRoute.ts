import * as express from "express";
import {handleRequest} from "./route-helper";

class TribeRoutes {
    public list = handleRequest('performTribeListQuery');
    public get = handleRequest('performTribeQuery');
    public save = handleRequest('performSaveTribeCommand');
    public delete = handleRequest('performDeleteTribeCommand');
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