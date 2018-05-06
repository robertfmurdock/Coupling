import * as express from "express";

class HistoryRoutes {
    list(request, response) {
        request.dataService.requestHistory(request.params.tribeId).then(function (history) {
            response.send(history);
        }, function (error) {
            response.statusCode = 500;
            response.send(error.message);
        });
    };

    async savePairs(request, response) {
        const pairs = request.body;
        if (pairs.date && pairs.pairs) {
            pairs.date = new Date(pairs.date as string);

            response.send(await request.dataService.savePairAssignmentsToHistory(pairs));
        } else {
            response.statusCode = 400;
            response.send({error: 'Pairs were not valid.'});
        }
    };

    deleteMember(request, response) {
        request.dataService.removePairAssignments(request.params.id)
            .then(function () {
                response.send({message: 'SUCCESS'});
            }, function (err) {
                response.statusCode = 404;
                response.send({message: err.message});
            });
    }
}

const history = new HistoryRoutes();
const router = express.Router({mergeParams: true});
router.route('')
    .get(history.list)
    .post(history.savePairs);
router.delete('/:id', history.deleteMember);

export default router;