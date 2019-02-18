import * as express from "express";
import historyRouter from "./historyRouter";
import pinsRouter from "./pinRouter";
import playerRouter from "./playerRouter";
import spin from './spin';

let router = express.Router({mergeParams: true});

router.all('/*', async function (request, response, next) {
    // @ts-ignore
    const isAuthorized = await request.commandDispatcher.performUserIsAuthorizedAction(request.params.tribeId);
    if (isAuthorized) {
        next()
    } else {
        response.sendStatus(404);
    }
});

router.post('/spin', spin);
router.use('/history', historyRouter);
router.use('/players', playerRouter);
router.use('/pins', pinsRouter);
export default router