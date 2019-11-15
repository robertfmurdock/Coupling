// @ts-ignore
import * as server from "server";
import * as express from "express";
import spin from "./spin";

const {historyRouter, pinRouter, playerRouter} = server.com.zegreatrob.coupling.server.route;

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
router.use('/pins', pinRouter);
export default router