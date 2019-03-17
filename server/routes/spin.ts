import {handleRequest} from "./route-helper";

export default handleRequest(
    (commandDispatcher, request) => commandDispatcher.performProposeNewPairsCommand(request.params.tribeId, request.body),
    (response, data) => response.send(data)
);
