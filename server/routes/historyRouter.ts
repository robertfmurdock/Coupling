import * as express from "express";
import {handleRequest} from "./route-helper";

class HistoryRoutes {

    list = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performPairAssignmentDocumentListQuery(request, response),
        () => {
        }
    );

    savePairs = handleRequest(
        async (commandDispatcher, request) => {
            const pairs = request.body;
            if (pairs.date && pairs.pairs) {
                pairs.date = new Date(pairs.date as string);
                return await request.commandDispatcher.performSavePairAssignmentDocumentCommand(request.params.tribeId, pairs);
            } else {
                return null
            }
        },
        (response, data) => {
            if (data === null) {
                response.statusCode = 400;
                response.send({error: 'Pairs were not valid.'});
            } else {
                response.send(data);
            }
        }
    );

    deleteMember = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performDeletePairAssignmentDocumentCommand(request, response),
        () => {
        }
    );

}

const history = new HistoryRoutes();
const router = express.Router({mergeParams: true});
router.route('')
    .get(history.list)
    .post(history.savePairs);
router.delete('/:id', history.deleteMember);

export default router;