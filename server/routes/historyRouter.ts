import * as express from "express";
import {handleRequest} from "./route-helper";

class HistoryRoutes {
    list = handleRequest('performPairAssignmentDocumentListQuery');
    savePairs = handleRequest('performSavePairAssignmentDocumentCommand');
    deleteMember = handleRequest('performDeletePairAssignmentDocumentCommand');
}

const history = new HistoryRoutes();
const router = express.Router({mergeParams: true});
router.route('')
    .get(history.list)
    .post(history.savePairs);
router.delete('/:id', history.deleteMember);

export default router;