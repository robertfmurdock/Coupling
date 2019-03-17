import * as express from "express";
import {handleRequest} from "./route-helper";

class HistoryRoutes {

    list = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performPairAssignmentDocumentListQuery(request.params.tribeId),
        (response, data) => response.send(data)
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
        (commandDispatcher, request) => commandDispatcher.performDeletePairAssignmentDocumentCommand(request.params.id),
        (response, data) => {
            if (data) {
                response.send({message: 'SUCCESS'});
            } else {
                response.statusCode = 404;
                response.send({message: 'Pair Assignments could not be deleted because they do not exist.'})
            }
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