import * as express from "express";
import {handleRequest} from "./route-helper";

class HistoryRoutes {

    list = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performPairAssignmentDocumentListQuery(request, response)
    );

    savePairs = handleRequest(
        async (commandDispatcher, request, response) => commandDispatcher.performSavePairAssignmentDocumentCommand(request, response)
    );

    deleteMember = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performDeletePairAssignmentDocumentCommand(request, response)
    );

}

const history = new HistoryRoutes();
const router = express.Router({mergeParams: true});
router.route('')
    .get(history.list)
    .post(history.savePairs);
router.delete('/:id', history.deleteMember);

export default router;